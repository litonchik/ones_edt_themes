package ru.litonchik.edt.themes.testing;

import java.nio.file.*;
import java.util.*;
import java.util.List;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.equinox.app.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.ui.*;
import org.eclipse.ui.application.*;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.swt.custom.StyledText;
import ru.litonchik.edt.themes.ui.ReadableCombo;
import org.eclipse.ui.ide.IDE;
import java.io.ByteArrayInputStream;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import ru.litonchik.edt.themes.edt.*;
import ru.litonchik.edt.themes.model.*;
import ru.litonchik.edt.themes.runtime.*;

/** Test-only application, packaged only into a probe bundle, never the release. */
public class WorkbenchProbe implements IApplication {
    private boolean success;
    private final StringBuilder report = new StringBuilder();
    private void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        report.append("PASS ").append(message).append('\n');
        if ("surfaces".equals(System.getProperty("edt.probe.phase")) || System.getProperty("edt.probe.phase").startsWith("light") || System.getProperty("edt.probe.phase").startsWith("candidateselection")) try {
            Files.writeString(java.nio.file.Path.of(System.getProperty("edt.probe.report")),report.toString());
        } catch (java.io.IOException e) { throw new IllegalStateException(e); }
    }
    @Override public Object start(IApplicationContext context) throws Exception {
        String disabled = String.join(";",Arrays.stream(Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.startup")).map(e -> e.getContributor().getName()).distinct().filter(id -> !id.equals("com._1c.g5.v8.dt.theming.ui")).toList());
        InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").put("PLUGINS_NOT_ACTIVATED_ON_STARTUP",disabled);
        Display display = PlatformUI.createDisplay();
        try {
            PlatformUI.createAndRunWorkbench(display,new WorkbenchAdvisor() {
                @Override public String getInitialWindowPerspectiveId() { return "ru.litonchik.edt.themes.probe.perspective"; }
                @Override public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer c) {
                    return new WorkbenchWindowAdvisor(c) { @Override public void preWindowOpen() { c.setInitialSize(new Point(900,640)); c.setTitle("EDT Themes — isolated runtime probe"); } };
                }
                @Override public void postStartup() {
                    display.timerExec(5000,() -> {
                        try { runChecks(); success = true; }
                        catch (Throwable e) { report.append("FAIL ").append(e).append('\n'); for (StackTraceElement frame : e.getStackTrace()) report.append(frame).append('\n'); }
                        finally {
                            try { Files.writeString(java.nio.file.Path.of(System.getProperty("edt.probe.report")),report.toString()); } catch (Exception e) { e.printStackTrace(); }
                            for (var window:PlatformUI.getWorkbench().getWorkbenchWindows()) for(var page:window.getPages()) page.closeAllEditors(false);
                            ThemeStartup.stop(); PlatformUI.getWorkbench().close();
                        }
                    });
                }
            });
        } finally { display.dispose(); }
        return success ? IApplication.EXIT_OK : 1;
    }
    private void runChecks() throws Exception {
        EdtCompatibility.verify();
        report.append("JVM ").append(System.getProperty("java.version")).append('\n');
        var workbench = PlatformUI.getWorkbench();
        if (workbench.getIntroManager().getIntro() != null) workbench.getIntroManager().closeIntro(workbench.getIntroManager().getIntro());
        if (System.getProperty("edt.probe.phase").startsWith("candidate") || Set.of("surfaces","lightsurfaces","light","lightrestart","semantic","nativeswitch").contains(System.getProperty("edt.probe.phase"))) {
            var previousWindows=workbench.getWorkbenchWindows();
            var freshWindow=workbench.openWorkbenchWindow("ru.litonchik.edt.themes.probe.gallery",null);
            for(var previous:previousWindows) {
                for(var previousPage:previous.getPages()) previousPage.closeAllEditors(false);
                previous.close();
            }
            var freshShell=freshWindow.getShell(); freshShell.setMinimized(false); freshShell.setMaximized(false); freshShell.setSize(900,640);
            var screen=freshShell.getMonitor().getClientArea(); freshShell.setLocation(screen.x,screen.y); freshShell.forceActive();
            freshWindow.getActivePage().setEditorAreaVisible(true); freshShell.layout(true,true); pump(500);
        }
        if (System.getProperty("edt.probe.phase").startsWith("candidateselection")) { runSelectionChecks(); return; }
        if (System.getProperty("edt.probe.phase").startsWith("candidategit")) { runGitChecks(); return; }
        if (Set.of("candidaterulers","candidaterulersrestart").contains(System.getProperty("edt.probe.phase"))) { runRulerChecks(); return; }
        if ("candidateuigallery".equals(System.getProperty("edt.probe.phase"))) { runUiGallery(); return; }
        if (Set.of("candidateui","candidateuirestart").contains(System.getProperty("edt.probe.phase"))) { runUiChecks(); return; }
        if (System.getProperty("edt.probe.phase").startsWith("candidategallery")) { runLightGallery(); return; }
        if (Set.of("candidatesurfaces","candidateallsurfaces").contains(System.getProperty("edt.probe.phase"))) { runSurfaceChecks(); return; }
        if (System.getProperty("edt.probe.phase").startsWith("candidate")) { runCandidateChecks(); return; }
        if (Set.of("semantic","nativeswitch").contains(System.getProperty("edt.probe.phase"))) { runSemanticChecks(); return; }
        if (Set.of("surfaces","lightsurfaces").contains(System.getProperty("edt.probe.phase"))) { runSurfaceChecks(); return; }
        if (System.getProperty("edt.probe.phase").startsWith("lightgallery")) { runLightGallery(); return; }
        if (System.getProperty("edt.probe.phase").startsWith("light")) { runLightChecks(); return; }
        if ("cleanup".equals(System.getProperty("edt.probe.phase"))) { runCleanup(); return; }
        if (System.getProperty("edt.probe.phase").startsWith("gallery")) { runGallery(); return; }
        if (System.getProperty("edt.probe.phase").startsWith("winterseparators")) { runWinterSeparatorChecks(); return; }
        if (System.getProperty("edt.probe.phase").startsWith("onedark") || System.getProperty("edt.probe.phase").startsWith("dracula") || System.getProperty("edt.probe.phase").startsWith("github") || System.getProperty("edt.probe.phase").startsWith("winter") || System.getProperty("edt.probe.phase").startsWith("nightowl") || System.getProperty("edt.probe.phase").startsWith("ayu") || System.getProperty("edt.probe.phase").startsWith("shades") || System.getProperty("edt.probe.phase").startsWith("noctis") || System.getProperty("edt.probe.phase").startsWith("visualstudio")) { runProfileChecks(); return; }
        if ("preview".equals(System.getProperty("edt.probe.phase"))) { runPreviewChecks(); return; }
        if (System.getProperty("edt.probe.phase").startsWith("palette")) { runPaletteChecks(); return; }
        if (System.getProperty("edt.probe.phase").startsWith("font")) { runFontChecks(); return; }
        var engine = workbench.getService(IThemeEngine.class);
        check(engine != null && engine.getActiveTheme() != null,"native theme engine");
        var prefs = new SnapshotPreferences();
        var adapter = new EdtSettingsAdapter(engine,workbench.getThemeManager());
        var baseline = new LinkedHashMap<SettingKey,OriginalValue>();
        if ("restore".equals(System.getProperty("edt.probe.phase"))) {
            baseline.putAll(prefs.load().original());
            var coordinator = new ThemeCoordinator(); coordinator.start();
            check("monokai".equals(coordinator.activeProfile()),"saved profile after process restart");
            screenshot("monokai-restarted");
            check(workbench.getThemeManager().getCurrentTheme().getColorRegistry().getRGB("com._1c.g5.v8.dt.theming.ui.BACKGROUND_COLOR").equals(new RGB(39,40,34)),"Monokai named UI color after process restart");
            var result = coordinator.restore(true);
            check(result.successful(),"force restoration after restart");
            for (var entry : baseline.entrySet()) check(entry.getValue().equals(adapter.read(entry.getKey())),"restored " + entry.getKey());
            coordinator.close(); return;
        }
        var coordinator = new ThemeCoordinator(); coordinator.start();
        if (coordinator.hasSnapshot()) check(coordinator.restore(true).successful(),"reset previous probe journal before new baseline");
        StyledText bslText = tryOpenBsl();
        for (SettingKey key : adapter.plan(PaletteCatalog.load("darcula"),true).keySet()) baseline.put(key,adapter.read(key));
        for (SettingKey key : NativeSettingsCatalog.load().keySet()) baseline.putIfAbsent(key,adapter.read(key));
        SettingKey custom = new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.Comment.color");
        InstanceScope.INSTANCE.getNode(custom.node()).put(custom.key(),"125,126,127");
        coordinator.apply("darcula",false);
        check(OriginalValue.of("125,126,127").equals(adapter.read(custom)),"first apply with BSL disabled preserves explicit color");
        check(coordinator.restore(true).successful(),"restore after first unchecked BSL apply");
        check(OriginalValue.of("125,126,127").equals(adapter.read(custom)),"restore preserves preexisting BSL custom color");
        adapter.write(custom,baseline.get(custom)); adapter.flush();
        for (int i = 0; i < 10; i++) {
            String id = i % 2 == 0 ? "darcula" : "monokai";
            coordinator.apply(id,true);
            check(ProfileState.DARK.equals(engine.getActiveTheme().getId()),"Dark ID cycle " + i);
            var p = PaletteCatalog.load(id);
            String[] components = p.rgb("editor.background").split(",");
            RGB expected = new RGB(Integer.parseInt(components[0]),Integer.parseInt(components[1]),Integer.parseInt(components[2]));
            check(expected.equals(workbench.getThemeManager().getCurrentTheme().getColorRegistry().getRGB("com._1c.g5.v8.dt.theming.ui.BACKGROUND_COLOR")),id + " named UI color");
            while (workbench.getDisplay().readAndDispatch()) {}
            workbench.getDisplay().update();
            check(expected.equals(workbench.getActiveWorkbenchWindow().getShell().getBackground().getRGB()),id + " SWT shell background: " + workbench.getActiveWorkbenchWindow().getShell().getBackground().getRGB());
            if (bslText != null && !bslText.isDisposed()) {
                long end = System.nanoTime() + 2_000_000_000L;
                while (System.nanoTime() < end) { if (!workbench.getDisplay().readAndDispatch()) Thread.sleep(20); }
                report.append("BSL computed CSS ").append(engine.getStyle(bslText)).append('\n');
                report.append("BSL background ").append(id).append(" actual=").append(bslText.getBackground().getRGB()).append(" global=").append(EditorsUI.getPreferenceStore().getString("AbstractTextEditor.Color.Background")).append(" bslInstance=").append(InstanceScope.INSTANCE.getNode("com._1c.g5.v8.dt.bsl.ui").get("AbstractTextEditor.Color.Background",null)).append(" bslDefault=").append(DefaultScope.INSTANCE.getNode("com._1c.g5.v8.dt.bsl.ui").get("AbstractTextEditor.Color.Background",null)).append(" globalSystem=").append(EditorsUI.getPreferenceStore().getBoolean("AbstractTextEditor.Color.Background.SystemDefault")).append(" bslDefaultSystem=").append(DefaultScope.INSTANCE.getNode("com._1c.g5.v8.dt.bsl.ui").get("AbstractTextEditor.Color.Background.SystemDefault",null)).append('\n');
                check(expected.equals(bslText.getBackground().getRGB()),id + " BSL editor background");
                for (String[] token : new String[][]{{"// Комментарий","comment"},{"Функция","keyword"},{"Function","keyword"},{"123.45","number"},{"Строка с кириллицей","string"},{"#Область","directive"}}) {
                    int offset = bslText.getText().indexOf(token[0]);
                    var style = offset < 0 ? null : bslText.getStyleRangeAtOffset(offset);
                    report.append("TOKEN ").append(token[0]).append(" rawNumbers=").append(InstanceScope.INSTANCE.getNode("com._1c.g5.v8.dt.bsl.ui").get("com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.Numbers.color",null)).append(" range=").append(style).append('\n');
                    check(style != null && style.foreground != null, "BSL style available: " + token[0]);
                    check(style.foreground.getRGB().equals(rgb(p,token[1])),id + " BSL glyph " + token[0] + ": " + style.foreground.getRGB());
                }
            }
            for (var entry : adapter.plan(p,true).entrySet()) check(entry.getValue().equals(adapter.read(entry.getKey())),id + " setting " + entry.getKey());
            screenshot(id);
        }
        var newWindow = workbench.openWorkbenchWindow("ru.litonchik.edt.themes.probe.perspective", null);
        while (workbench.getDisplay().readAndDispatch()) {}
        check(newWindow.getShell().getBackground().getRGB().equals(rgb(PaletteCatalog.load("monokai"),"editor.background")), "new workbench window inherits Monokai");
        newWindow.close();
        var activePage = workbench.getActiveWorkbenchWindow().getActivePage();
        check(activePage.closeEditor(activePage.getActiveEditor(),false), "close test BSL editor");
        bslText = tryOpenBsl();
        check(bslText != null && bslText.getBackground().getRGB().equals(rgb(PaletteCatalog.load("monokai"),"editor.background")), "new BSL editor inherits Monokai background");
        coordinator.apply("monokai",false);
        for (var entry : baseline.entrySet()) if (entry.getKey().node().equals("com._1c.g5.v8.dt.bsl.ui")) check(entry.getValue().equals(adapter.read(entry.getKey())),"BSL checkbox restores " + entry.getKey());
        var result = coordinator.restore(true); check(result.successful(),"force restoration");
        for (var entry : baseline.entrySet()) check(entry.getValue().equals(adapter.read(entry.getKey())),"original value " + entry.getKey());
        coordinator.apply("darcula",true);
        SettingKey edited = new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.Comment.color");
        InstanceScope.INSTANCE.getNode(edited.node()).put(edited.key(),"60,60,60");
        InstanceScope.INSTANCE.getNode(edited.node()).flush();
        coordinator.apply("monokai",true);
        check(OriginalValue.of("60,60,60").equals(adapter.read(edited)),"manual BSL color survives profile switch");
        check(coordinator.restore(false).conflicts().contains(edited),"manual BSL conflict reported");
        check(OriginalValue.of("60,60,60").equals(adapter.read(edited)),"manual BSL color survives safe restore");
        check(coordinator.restore(true).successful(),"explicit force restore of conflict");
        coordinator.apply("monokai",true);
        InstanceScope.INSTANCE.getNode(edited.node()).put(edited.key(),"61,62,63");
        InstanceScope.INSTANCE.getNode(edited.node()).flush();
        engine.setTheme("org.eclipse.e4.ui.css.theme.e4_default",true);
        while (workbench.getDisplay().readAndDispatch()) {}
        check("org.eclipse.e4.ui.css.theme.e4_default".equals(engine.getActiveTheme().getId()),"external Light theme preserved");
        check(coordinator.activeProfile() == null,"profile released after external Light theme");
        check(OriginalValue.of("61,62,63").equals(adapter.read(edited)),"manual BSL color survives external native theme rewrite");
        coordinator.restore(true);
        coordinator.close();
        // Exercise the registered preference page and real Apply/Cancel controls.
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(workbench.getActiveWorkbenchWindow().getShell(),"ru.litonchik.edt.themes.preferences",null,null);
        check(dialog != null,"registered preference page exists");
        PreferencePage page = null;
        for (Object entry : dialog.getPreferenceManager().getElements(PreferenceManager.PRE_ORDER)) {
            IPreferenceNode node = (IPreferenceNode)entry;
            if (node.getId().equals("ru.litonchik.edt.themes.preferences")) page = (PreferencePage)node.getPage();
        }
        check(page != null && page.getControl() != null,"registered page is initialized");
        var combo = findCombo((Composite)page.getControl());
        check(combo != null,"profile selector exists"); combo.select(2);
        check(page.performCancel(),"Cancel accepted");
        check(new SnapshotPreferences().activeProfile() == null,"Cancel before Apply does not activate profile");
        Button apply = findApply(dialog.getShell());
        check(apply != null,"Apply button exists");
        apply.notifyListeners(SWT.Selection,new Event());
        check("monokai".equals(new SnapshotPreferences().activeProfile()),"real Apply commits Monokai");
        page.performCancel(); dialog.close();
        check("monokai".equals(new SnapshotPreferences().activeProfile()),"Cancel after Apply preserves committed profile");
        ThemeStartup.stop();
        // The saved profile is intentionally left for a second JVM process.
    }
    private void screenshot(String profile) {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IThemeEngine engine = PlatformUI.getWorkbench().getService(IThemeEngine.class);
        engine.applyStyles(shell,true);
        while (shell.getDisplay().readAndDispatch()) {}
        shell.getDisplay().update();
        report.append("UI ").append(profile).append(" shell=").append(shell.getBackground().getRGB()).append(" style=").append(engine.getStyle(shell)).append('\n');
        // Only targeted visible controls are captured by the palette/popup probes.
    }
    @Override public void stop() {}
    private void pump(long millis) throws Exception {
        long until=System.nanoTime()+millis*1_000_000;
        Display display=PlatformUI.getWorkbench().getDisplay();
        while(System.nanoTime()<until) { if(!display.readAndDispatch()) Thread.sleep(15); }
    }
    private RGB glyph(StyledText text,String token) {
        int offset=text.getText().indexOf(token);
        if(offset<0) throw new AssertionError("Missing token "+token+" in visible editor: "+text.getText());
        var style=text.getStyleRangeAtOffset(offset);
        return style==null || style.foreground==null ? text.getForeground().getRGB() : style.foreground.getRGB();
    }
    private void runSemanticChecks() throws Exception {
        var workbench=PlatformUI.getWorkbench();
        var engine=workbench.getService(IThemeEngine.class);
        try(var coordinator=new ThemeCoordinator()) {
            coordinator.start();
            if(coordinator.hasSnapshot()) check(coordinator.restore(true).successful(),"semantic initial cleanup");
            engine.setTheme(ProfileState.DARK,true); pump(1500);
            StyledText text=tryOpenBsl(); check(text!=null,"semantic real BSL editor");
            var editor=(org.eclipse.xtext.ui.editor.XtextEditor)workbench.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            text=editor.getInternalSourceViewer().getTextWidget();
            var document=editor.getDocument();
            pump(3500);
            report.append("DOC ").append(document.get()).append("\nWIDGET ").append(text.getText()).append('\n');
            RGB original=glyph(text,"Сообщить");
            report.append("OBS native call ").append(original).append('\n');
            for(String id:"nativeswitch".equals(System.getProperty("edt.probe.phase")) ? List.<String>of() : PaletteCatalog.ids()) {
                String contents=document.get();
                boolean dirty=editor.isDirty();
                long stamp=((org.eclipse.jface.text.IDocumentExtension4)document).getModificationStamp();
                coordinator.apply(id,true); pump(1800);
                check(contents.equals(document.get()) && dirty==editor.isDirty()
                    && stamp==((org.eclipse.jface.text.IDocumentExtension4)document).getModificationStamp(),id+" apply preserves document, dirty state and modification stamp");
                RGB before=glyph(text,"Сообщить");
                document.replace(document.getLength(),0,"\n// reconcile\n"); pump(2800);
                RGB after=glyph(text,"Сообщить");
                report.append("OBS ").append(id).append(" before=").append(before).append(" after=").append(after).append(" expected=").append(rgb(PaletteCatalog.load(id),"accent")).append('\n');
                check(before.equals(after),id+" call stable before/after edit");
                check(after.equals(rgb(PaletteCatalog.load(id),"accent")),id+" semantic call matches palette");
                check(glyph(text,"МойВызов();").equals(after) && glyph(text,"СтрДлина").equals(after),id+" local and nested calls match palette");
                Files.writeString(java.nio.file.Path.of(System.getProperty("edt.probe.report")),report.toString());
            }
            check(coordinator.restore(true).successful(),"semantic restore"); pump(2200);
            RGB restored=glyph(text,"Сообщить");
            document.replace(document.getLength(),0,"\n// native reconcile\n"); pump(2800);
            report.append("OBS restored before=").append(restored).append(" after=").append(glyph(text,"Сообщить")).append('\n');
            check(original.equals(restored),"native call restored without edit");
            check(original.equals(glyph(text,"Сообщить")),"native call restored after edit");
            coordinator.apply("visual-studio-dark-cpp",true); pump(1500);
            document.replace(document.getLength(),0,"\n// before native switch\n"); pump(1800);
            engine.setTheme("org.eclipse.e4.ui.css.theme.e4_default",true); pump(2600);
            RGB nativeSwitch=glyph(text,"Сообщить");
            check(coordinator.activeProfile()==null,"native theme switch releases active profile");
            check(new SnapshotPreferences().load().original().keySet().stream().allMatch(key -> key.equals(EdtSettingsAdapter.CSS) || key.node().equals(EdtSettingsAdapter.CSS_NODE)),"native theme switch retains only intentional theme conflicts");
            document.replace(document.getLength(),0,"\n// after native switch\n"); pump(1800);
            check(nativeSwitch.equals(glyph(text,"Сообщить")),"native theme switch call stable after edit");
            workbench.getActiveWorkbenchWindow().getActivePage().closeEditor(editor,false);
            StyledText reopened=tryOpenBsl(); check(reopened!=null,"native theme opens fresh BSL editor"); pump(1800);
            check(nativeSwitch.equals(glyph(reopened,"Сообщить")),"native theme switch matches fresh editor");
            report.append("OBS native switch call=").append(nativeSwitch).append('\n');
            workbench.getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
            check(coordinator.restore(true).successful() && !coordinator.hasSnapshot(),"native switch final test cleanup clears journal");
        }
    }
    private static final class MemoryElement implements org.eclipse.compare.ITypedElement,org.eclipse.compare.IEncodedStreamContentAccessor {
        private final String name,text;
        MemoryElement(String name,String text) { this.name=name; this.text=text; }
        public String getName() { return name; }
        public String getType() { return "bsl"; }
        public Image getImage() { return null; }
        public java.io.InputStream getContents() { return new ByteArrayInputStream(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
        public String getCharset() { return "UTF-8"; }
    }
    private void collectText(Composite parent,List<StyledText> result) {
        for(var control:parent.getChildren()) {
            if(control instanceof StyledText text) result.add(text);
            if(control instanceof Composite composite) collectText(composite,result);
        }
    }
    private void observeText(String stage,StyledText text,String token) {
        report.append("OBS ").append(stage).append(" bg=").append(text.getBackground().getRGB())
            .append(" fg=").append(text.getForeground().getRGB());
        if(text.getText().contains(token)) report.append(" token=").append(token).append(" color=").append(glyph(text,token));
        report.append('\n');
    }
    private void captureSurfaceShell(Shell shell,String name) throws Exception {
        shell.forceActive(); shell.layout(true,true); shell.redraw(); pump(500);
        Rectangle area=shell.getClientArea();
        Image image=new Image(shell.getDisplay(),area.width,area.height); GC gc=new GC(shell);
        try { gc.copyArea(image,area.x,area.y); ImageLoader loader=new ImageLoader(); loader.data=new ImageData[]{image.getImageData()}; loader.save(System.getProperty("edt.probe.images")+"/"+name+".png",SWT.IMAGE_PNG); }
        finally { gc.dispose(); image.dispose(); }
    }
    private int profileCount(PreferenceDialog dialog) {
        var combo=findCombo(dialog.getShell()); combo.setListVisible(true);
        int count=findFontTable(combo).getItemCount(); combo.closePopup(); return count;
    }
    private void selectMode(PreferenceDialog dialog,ThemeMode mode) {
        var dark=findButton(dialog.getShell(),"Тёмные"); var light=findButton(dialog.getShell(),"Светлые");
        check(dark!=null && light!=null,"light/dark radio buttons exist");
        dark.setSelection(mode==ThemeMode.DARK); light.setSelection(mode==ThemeMode.LIGHT);
        (mode==ThemeMode.DARK?dark:light).notifyListeners(SWT.Selection,new Event());
    }
    private void checkLightRegistry() {
        var palette=PaletteCatalog.load("github-light-default");
        var registry=PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
        for(var entry:SettingCatalog.load().entries()) if(entry.kind().equals("color"))
            check(rgb(palette,entry.role()).equals(registry.getRGB(entry.key())),"light effective registry "+entry.key()+" expected="+rgb(palette,entry.role())+" actual="+registry.getRGB(entry.key()));
    }

    private org.eclipse.core.resources.IProject gitFixture() throws Exception {
        var project=ResourcesPlugin.getWorkspace().getRoot().getProject("ThemeGitProbe");
        if(!project.exists()) {
            project.create(null); project.open(null);
            for(String name:List.of("Modified.bsl","Clean.bsl",".gitignore")) {
                var file=project.getFile(name);
                file.create(new java.io.ByteArrayInputStream((name.equals(".gitignore")?"*.log\n":"// Original\n").getBytes(java.nio.charset.StandardCharsets.UTF_8)),true,null);
            }
            try(var git=org.eclipse.jgit.api.Git.init().setDirectory(project.getLocation().toFile()).call()) {
                git.add().addFilepattern(".").call();
                git.commit().setMessage("Test fixture").setAuthor("EDT Theme Probe","probe@example.invalid").setCommitter("EDT Theme Probe","probe@example.invalid").call();
                new org.eclipse.egit.core.op.ConnectProviderOperation(project,git.getRepository().getDirectory()).execute(null);
            }
            project.getFile("Modified.bsl").setContents(new java.io.ByteArrayInputStream("// Modified\n".getBytes(java.nio.charset.StandardCharsets.UTF_8)),true,false,null);
            project.getFile("Ignored.log").create(new java.io.ByteArrayInputStream("ignored\n".getBytes(java.nio.charset.StandardCharsets.UTF_8)),true,null);
            project.refreshLocal(org.eclipse.core.resources.IResource.DEPTH_INFINITE,null);
        }
        check(org.eclipse.team.core.RepositoryProvider.getProvider(project)!=null,"fixture connected to EGit");
        try(var git=org.eclipse.jgit.api.Git.open(project.getLocation().toFile())) {
            check(git.status().call().getModified().contains("Modified.bsl"),"fixture has tracked modified BSL");
            check(git.status().call().getIgnoredNotInIndex().contains("Ignored.log"),"fixture has Git ignored resource");
        }
        return project;
    }
    private TreeItem gitItem(TreeItem[] items,String name) {
        for(var item:items) {
            if(item.getData() instanceof org.eclipse.core.resources.IResource resource && resource.getName().equals(name)) return item;
            if(item.getText().contains(name)) return item;
            var found=gitItem(item.getItems(),name); if(found!=null)return found;
        }
        return null;
    }
    private static class GitDecoration implements org.eclipse.jface.viewers.IDecoration {
        org.eclipse.swt.graphics.Color background,foreground; String prefix="",suffix="";
        @Override public void addPrefix(String value){prefix+=value;}
        @Override public void addSuffix(String value){suffix+=value;}
        @Override public void addOverlay(org.eclipse.jface.resource.ImageDescriptor image){}
        @Override public void addOverlay(org.eclipse.jface.resource.ImageDescriptor image,int quadrant){}
        @Override public void setBackgroundColor(org.eclipse.swt.graphics.Color value){background=value;}
        @Override public void setForegroundColor(org.eclipse.swt.graphics.Color value){foreground=value;}
        @Override public void setFont(org.eclipse.swt.graphics.Font value){}
        @Override public org.eclipse.jface.viewers.IDecorationContext getDecorationContext(){return org.eclipse.jface.viewers.DecorationContext.DEFAULT_CONTEXT;}
    }
    private org.eclipse.jface.viewers.ILightweightLabelDecorator gitDecorator() throws Exception {
        for(var element:org.eclipse.core.runtime.Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.decorators"))
            if("org.eclipse.egit.ui.internal.decorators.GitLightweightDecorator".equals(element.getAttribute("id")))
                return (org.eclipse.jface.viewers.ILightweightLabelDecorator)element.createExecutableExtension("class");
        throw new AssertionError("EGit decorator extension missing");
    }
    private static final List<String> GIT_COLORS=List.of("UncommittedChangeBackgroundColor","UncommittedChangeForegroundColor","IgnoredResourceBackgroundColor","IgnoredResourceForegroundColor");
    private GitDecoration gitDecoration(org.eclipse.jface.viewers.ILightweightLabelDecorator decorator,org.eclipse.core.resources.IResource resource) {
        var result=new GitDecoration();decorator.decorate(resource,result);return result;
    }
    private RGB gitLabelPixel(Tree tree,TreeItem item) {
        var bounds=item.getBounds();var area=tree.getClientArea();
        int x=Math.min(area.width-3,bounds.x+bounds.width-2),y=bounds.y+1;
        if(x<0 || y<0 || y>=area.height)throw new AssertionError("Git label not visible for pixel check");
        var image=new Image(tree.getDisplay(),1,1);var gc=new GC(tree);
        try{gc.copyArea(image,x,y);var data=image.getImageData();return data.palette.getRGB(data.getPixel(0,0));}
        finally{gc.dispose();image.dispose();}
    }
    private void checkGitColors(Palette palette,Tree tree,org.eclipse.core.resources.IProject project,org.eclipse.jface.viewers.ILightweightLabelDecorator decorator) throws Exception {
        var registry=PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
        var prefs=InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");String id=palette.id();
        pump(250);
        for(String name:GIT_COLORS){String role=name.endsWith("BackgroundColor")?"editor.background":name.startsWith("Uncommitted")?"accent":"disabled";
            check(palette.rgb(role).equals(prefs.get("org.eclipse.egit.ui."+name,null)),id+" Git explicit "+name);
            check(rgb(palette,role).equals(registry.getRGB("org.eclipse.egit.ui."+name)),id+" Git registry "+name+" actual="+registry.getRGB("org.eclipse.egit.ui."+name));
        }
        check(rgb(palette,"editor.background").equals(tree.getBackground().getRGB()),id+" Git Tree background");
        tree.removeAll();
        for(String name:List.of("ThemeGitProbe","Modified.bsl","Clean.bsl","Ignored.log")){
            var resource=name.equals("ThemeGitProbe")?project:project.getFile(name);var d=gitDecoration(decorator,resource);
            var item=new TreeItem(tree,SWT.NONE);item.setText(d.prefix+name+d.suffix);item.setForeground(d.foreground);item.setBackground(d.background);
            check(rgb(palette,"editor.background").equals(item.getBackground().getRGB()),id+" Git label background "+name+" actual="+item.getBackground().getRGB());
            if(name.equals("Modified.bsl"))check(rgb(palette,"accent").equals(item.getForeground().getRGB()),id+" actual EGit modified foreground");
            if(name.equals("Ignored.log"))check(rgb(palette,"disabled").equals(item.getForeground().getRGB()),id+" actual EGit ignored foreground");
        }
        tree.deselectAll();tree.getParent().layout(true,true);tree.redraw();tree.update();pump(120);
        for(var item:tree.getItems())check(rgb(palette,"editor.background").equals(gitLabelPixel(tree,item)),id+" painted Git label pixel "+item.getText());
    }
    private void runGitChecks() throws Exception {
        var wb=PlatformUI.getWorkbench();var page=wb.getActiveWorkbenchWindow().getActivePage();
        org.eclipse.core.runtime.Platform.getBundle("org.eclipse.egit.ui").start(org.osgi.framework.Bundle.START_TRANSIENT);
        for(var element:org.eclipse.core.runtime.Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.core.runtime.preferences"))
            if("org.eclipse.egit.ui".equals(element.getContributor().getName()) && "initializer".equals(element.getName()))
                ((org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer)element.createExecutableExtension("class")).initializeDefaultPreferences();
        var project=gitFixture();var decorator=gitDecorator();
        var view=(UiPanelView)page.showView("ru.litonchik.edt.themes.probe.ui");var tree=(Tree)view.controls.get("tree");
        for(var c:view.root.getChildren())if(c!=tree)c.dispose();view.root.setLayout(new org.eclipse.swt.layout.FillLayout());view.root.layout(true,true);
        var coordinator=ThemeStartup.coordinator();var prefs=InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");
        var originals=new Properties();var saved=java.nio.file.Path.of(System.getProperty("edt.probe.images"),"git-originals.properties");
        boolean restart=System.getProperty("edt.probe.phase").endsWith("restart");
        boolean lightStartup="candidategitlightrestart".equals(System.getProperty("edt.probe.phase"));
        if(restart){try(var reader=Files.newBufferedReader(saved)){originals.load(reader);}check((lightStartup?"github-light-default":"monokai").equals(coordinator.activeProfile()),"Git "+(lightStartup?"light":"dark")+" profile persisted after JVM restart");}
        else {
            if(coordinator.hasSnapshot())check(coordinator.restore(true).successful(),"Git initial restore");
            // Controlled stale state from the installed EGit native Dark stylesheet.
            prefs.put("org.eclipse.egit.ui.UncommittedChangeBackgroundColor","47,47,47");prefs.put("org.eclipse.egit.ui.UncommittedChangeForegroundColor","114,157,186");
            prefs.remove("org.eclipse.egit.ui.IgnoredResourceBackgroundColor");prefs.remove("org.eclipse.egit.ui.IgnoredResourceForegroundColor");prefs.flush();
            for(String name:GIT_COLORS)originals.setProperty(name,prefs.get("org.eclipse.egit.ui."+name,"<absent>"));
            try(var writer=Files.newBufferedWriter(saved)){originals.store(writer,"EGit explicit originals");}
            coordinator.apply("github-light-default",true);
        }
        long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(20);GitDecoration ready;
        do{ready=gitDecoration(decorator,project.getFile("Modified.bsl"));if(ready.foreground!=null || !ready.prefix.isEmpty() || !ready.suffix.isEmpty())break;pump(200);}while(System.nanoTime()<deadline);
        check(ready.foreground!=null || !ready.prefix.isEmpty() || !ready.suffix.isEmpty(),"real EGit index diff computed before rendering");
        if(restart){
            checkGitColors(PaletteCatalog.load(lightStartup?"github-light-default":"monokai"),tree,project,decorator);
            coordinator.apply("github-light-default",true);checkGitColors(PaletteCatalog.load("github-light-default"),tree,project,decorator);
            captureSurfaceShell(wb.getActiveWorkbenchWindow().getShell(),"git-after-restart");
            check(coordinator.restore(true).successful(),"Git restore succeeds");pump(400);
            for(String name:GIT_COLORS)check(originals.getProperty(name).equals(prefs.get("org.eclipse.egit.ui."+name,"<absent>")),"Git exact explicit restore "+name);
            check(!coordinator.hasSnapshot() && coordinator.activeProfile()==null,"Git restore releases journal and profile");
        }else{
            var bsl=tryOpenBsl();String source=bsl.getText();String font=Arrays.toString(bsl.getFont().getFontData());page.activate(view);
            for(String id:PaletteCatalog.ids()){
                coordinator.apply(id,true);checkGitColors(PaletteCatalog.load(id),tree,project,decorator);
                check(source.equals(bsl.getText()) && font.equals(Arrays.toString(bsl.getFont().getFontData())),id+" Git theming preserves BSL source and font");
                if(id.equals("github-light-default"))captureSurfaceShell(wb.getActiveWorkbenchWindow().getShell(),"git-after");
            }
            var drafts=coordinator.paletteDrafts();drafts.edit("github-light-default","editor.background","#F1F5E9");
            coordinator.apply("github-light-default",true,drafts);checkGitColors(drafts.resolve("github-light-default"),tree,project,decorator);
            drafts.resetRole("github-light-default","editor.background");coordinator.apply("github-light-default",true,drafts);checkGitColors(PaletteCatalog.load("github-light-default"),tree,project,decorator);
            prefs.put("org.eclipse.egit.ui.UncommittedChangeForegroundColor","17,34,51");prefs.flush();coordinator.apply("monokai",true);
            check("17,34,51".equals(prefs.get("org.eclipse.egit.ui.UncommittedChangeForegroundColor",null)),"Git later manual label color preserved");
            drafts=coordinator.paletteDrafts();drafts.edit("monokai","accent","#6FD0E0");coordinator.apply("monokai",true,drafts);checkGitColors(drafts.resolve("monokai"),tree,project,decorator);
            drafts.resetRole("monokai","accent");coordinator.apply("monokai",true,drafts);checkGitColors(PaletteCatalog.load("monokai"),tree,project,decorator);
            for(String id:PaletteCatalog.ids())check(coordinator.paletteDrafts().overrides(id).isEmpty(),"Git personal roles clean and independent "+id);
        }
        decorator.dispose();
    }


    private java.util.List<Text> stagingIdentities(Control root) {
        var found=new ArrayList<Text>();
        if(root instanceof Text text && (text.getText().contains("probe@example.invalid") || "textBorder".equals(text.getData("FormWidgetFactory.drawBorder"))))found.add(text);
        if(root instanceof Composite parent)for(var child:parent.getChildren())found.addAll(stagingIdentities(child));
        return found;
    }
    private RGB pointPixel(Control control,int x,int y) {
        var image=new Image(control.getDisplay(),1,1);var gc=new GC(control);
        try{gc.copyArea(image,x,y);var data=image.getImageData();return data.palette.getRGB(data.getPixel(0,0));}
        finally{gc.dispose();image.dispose();}
    }
    private void dumpStagingTexts(Control root){
        if(root instanceof Text text)report.append("OBS TEXT len=").append(text.getText().length()).append(" fake=").append(text.getText().contains("probe@example.invalid")).append(" border=").append(text.getData("FormWidgetFactory.drawBorder")).append(" parent=").append(text.getParent().getClass().getName()).append(" enabled=").append(text.getEnabled()).append(" style=").append(text.getStyle()).append(" selection=").append(text.getSelection()).append('\n');
        if(root instanceof Composite parent)for(var child:parent.getChildren())dumpStagingTexts(child);
    }
    private RGB modalPixel(Control control,org.eclipse.swt.graphics.Rectangle requested) {
        var area=control instanceof Scrollable scroll?scroll.getClientArea():new org.eclipse.swt.graphics.Rectangle(0,0,control.getSize().x,control.getSize().y);
        var bounds=requested.intersection(area);
        if(bounds.width<2 || bounds.height<2)throw new AssertionError("No visible pixels: "+control);
        var image=new Image(control.getDisplay(),bounds.width,bounds.height);var gc=new GC(control);
        try{gc.copyArea(image,bounds.x,bounds.y);var data=image.getImageData();var counts=new HashMap<RGB,Integer>();
            for(int y=0;y<data.height;y++)for(int x=0;x<data.width;x++)counts.merge(data.palette.getRGB(data.getPixel(x,y)),1,Integer::sum);
            return counts.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
        }finally{gc.dispose();image.dispose();}
    }
    private void rowSelectionPixels(Control control,org.eclipse.swt.widgets.Item item,Palette palette,String label) {
        org.eclipse.swt.graphics.Rectangle bounds=item instanceof TreeItem ti?ti.getBounds():((TableItem)item).getBounds(0);
        var actual=modalPixel(control,bounds);
        check(rgb(palette,"selection.background").equals(actual),palette.id()+" "+label+" painted selection BG expected="+rgb(palette,"selection.background")+" got="+actual);
    }
    private void selectionCycle(Tree tree,Text focus,Palette palette,String label) throws Exception {
        var items=tree.getItems();check(items.length>=3,label+" has multiple rows");
        tree.setSelection(items[0]);tree.setFocus();tree.showSelection();tree.redraw();tree.update();pump(180);
        check(tree.isFocusControl(),label+" active focus acquired");rowSelectionPixels(tree,items[0],palette,label+" focused");
        focus.setFocus();tree.redraw();tree.update();pump(180);check(!tree.isFocusControl(),label+" focus moved away");rowSelectionPixels(tree,items[0],palette,label+" inactive");
        tree.setSelection(new TreeItem[]{items[1],items[2]});tree.redraw();tree.update();pump(100);
        check(tree.getSelectionCount()==2,label+" multiselection intact");rowSelectionPixels(tree,items[1],palette,label+" multi1");rowSelectionPixels(tree,items[2],palette,label+" multi2");
        tree.deselectAll();tree.redraw();tree.update();pump(100);
        check(tree.getSelectionCount()==0,label+" deselected");
        check(rgb(palette,"editor.background").equals(modalPixel(tree,items[0].getBounds())),label+" deselected row has normal background");
    }
    private void checkStagingFields(IWorkbenchPage page,IViewPart staging,java.util.List<Text> fields,Palette palette) throws Exception {
        page.activate(staging);page.setPartState(page.findViewReference("org.eclipse.egit.ui.StagingView"),IWorkbenchPage.STATE_MAXIMIZED);pump(350);
        for(int i=0;i<fields.size();i++){
            var field=fields.get(i);String text=field.getText();boolean enabled=field.getEnabled();
            for(boolean state:new boolean[]{true,false,true}){
                field.setEnabled(state);field.redraw();field.update();pump(150);
                check(rgb(palette,"editor.background").equals(field.getBackground().getRGB()),palette.id()+" real staging field "+i+" BG property enabled="+state);
                check(rgb(palette,state?"text":"disabled").equals(field.getForeground().getRGB()),palette.id()+" real staging field "+i+" FG property enabled="+state);
                var actual=modalPixel(field,field.getClientArea());check(rgb(palette,"editor.background").equals(actual),palette.id()+" real staging field "+i+" painted BG enabled="+state+" actual="+actual);
                check(text.equals(field.getText()),"staging identity preserved");
            }
            field.setEnabled(enabled);
        }
    }
    private void runSelectionChecks() throws Exception {
        var wb=PlatformUI.getWorkbench();var page=wb.getActiveWorkbenchWindow().getActivePage();var coordinator=ThemeStartup.coordinator();
        org.eclipse.core.runtime.Platform.getBundle("org.eclipse.egit.ui").start(org.osgi.framework.Bundle.START_TRANSIENT);
        for(var element:org.eclipse.core.runtime.Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.core.runtime.preferences"))
            if("org.eclipse.egit.ui".equals(element.getContributor().getName()) && "initializer".equals(element.getName()))
                ((org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer)element.createExecutableExtension("class")).initializeDefaultPreferences();
        var project=gitFixture();var git=org.eclipse.jgit.api.Git.open(project.getLocation().toFile());var config=git.getRepository().getConfig();
        config.setString("user",null,"name","EDT Theme Probe");config.setString("user",null,"email","probe@example.invalid");config.save();
        var mapped=org.eclipse.egit.core.project.RepositoryMapping.getMapping(project).getRepository();mapped.getConfig().load();
        var warm=gitDecorator();long warmDeadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(20);
        while(System.nanoTime()<warmDeadline){var result=gitDecoration(warm,project.getFile("Modified.bsl"));if(!result.prefix.isEmpty() || !result.suffix.isEmpty())break;pump(200);}warm.dispose();
        var staging=page.showView("org.eclipse.egit.ui.StagingView");
        check(staging instanceof org.eclipse.ui.part.IShowInTarget,"real staging show-in interface");
        check(((org.eclipse.ui.part.IShowInTarget)staging).show(new org.eclipse.ui.part.ShowInContext(null,new org.eclipse.jface.viewers.StructuredSelection(new org.eclipse.egit.ui.internal.repository.tree.RepositoryNode(null,mapped)))),"real staging repository selected");
        var fields=stagingIdentities(wb.getActiveWorkbenchWindow().getShell());long identitiesDeadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(20);
        while(fields.size()!=2 && System.nanoTime()<identitiesDeadline){pump(200);fields=stagingIdentities(wb.getActiveWorkbenchWindow().getShell());}
        if(fields.size()!=2){dumpStagingTexts(wb.getActiveWorkbenchWindow().getShell());captureSurfaceShell(wb.getActiveWorkbenchWindow().getShell(),"staging-diagnostic");}
        check(fields.size()==2,"real author/committer controls located, count="+fields.size());
        for(var field:fields)field.setText("EDT Theme Probe <probe@example.invalid>");
        var fixture=(UiPanelView)page.showView("ru.litonchik.edt.themes.probe.ui");for(var child:fixture.root.getChildren())child.dispose();fixture.root.setLayout(new org.eclipse.swt.layout.GridLayout(2,true));
        var tree=new Tree(fixture.root,SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION|SWT.CHECK);tree.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL,SWT.FILL,true,true));
        for(String name:List.of("First resource","Second resource","Third resource")){var item=new TreeItem(tree,SWT.NONE);item.setText(name);item.setChecked(true);}
        var styled=new org.eclipse.jface.viewers.TreeViewer(fixture.root,SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);styled.getTree().setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL,SWT.FILL,true,true));
        styled.setContentProvider(new org.eclipse.jface.viewers.ITreeContentProvider(){
            @Override public Object[] getElements(Object input){return new String[]{"Styled first","Styled second","Styled third"};}
            @Override public Object[] getChildren(Object parent){return new Object[0];}
            @Override public Object getParent(Object element){return null;}
            @Override public boolean hasChildren(Object element){return false;}
        });
        styled.setLabelProvider(new org.eclipse.jface.viewers.StyledCellLabelProvider(){
            @Override public void update(org.eclipse.jface.viewers.ViewerCell cell){var text=new org.eclipse.jface.viewers.StyledString(cell.getElement().toString());text.append(" [feature]",org.eclipse.jface.viewers.StyledString.DECORATIONS_STYLER);cell.setText(text.getString());cell.setStyleRanges(text.getStyleRanges());super.update(cell);}
        });styled.setInput(project);
        var table=new Table(fixture.root,SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);table.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL,SWT.FILL,true,true));
        new TableColumn(table,SWT.NONE).setWidth(200);for(String name:List.of("First table row","Second table row","Third table row")){var item=new TableItem(table,SWT.NONE);item.setText(name);}
        var focus=new Text(fixture.root,SWT.BORDER);focus.setText("Focus target");focus.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL,SWT.TOP,true,false));
        var saved=java.nio.file.Path.of(System.getProperty("edt.probe.images"),"selection-originals.properties");var originals=new Properties();boolean restart=System.getProperty("edt.probe.phase").endsWith("restart");
        if(restart){try(var reader=Files.newBufferedReader(saved)){originals.load(reader);}check("github-light-default".equals(coordinator.activeProfile()),"selection palette survives restart");}
        else{
            if(coordinator.hasSnapshot())check(coordinator.restore(true).successful(),"selection initial restore");
            originals.setProperty("tree.erase",Integer.toString(tree.getListeners(SWT.EraseItem).length));originals.setProperty("table.erase",Integer.toString(table.getListeners(SWT.EraseItem).length));
            var adapter=new EdtSettingsAdapter(wb.getService(IThemeEngine.class),wb.getThemeManager());var baseline=new LinkedHashMap<SettingKey,OriginalValue>();
            for(String id:List.of("github-light-default","monokai"))for(var key:adapter.plan(PaletteCatalog.load(id),true).keySet())baseline.putIfAbsent(key,adapter.read(key));
            for(var entry:baseline.entrySet())originals.setProperty("pref|"+entry.getKey().scope()+"|"+entry.getKey().node()+"|"+entry.getKey().key(),entry.getValue().present()?entry.getValue().value():"<absent>");
            try(var writer=Files.newBufferedWriter(saved)){originals.store(writer,"Selection originals");}
        }
        var protectedEditor=(ProtectedContentEditor)page.openEditor(new ProtectedInput(),"ru.litonchik.edt.themes.probe.protected");pump(200);
        int protectedErase=protectedEditor.tree.getListeners(SWT.EraseItem).length;
        var profiles=restart?List.of("github-light-default"):PaletteCatalog.ids();
        for(String id:profiles){
            if(!restart)coordinator.apply(id,true);var palette=PaletteCatalog.load(id);
            page.activate(fixture);page.setPartState(page.findViewReference("ru.litonchik.edt.themes.probe.ui"),IWorkbenchPage.STATE_MAXIMIZED);pump(250);
            check(Arrays.stream(protectedEditor.tree.getListeners(SWT.EraseItem)).noneMatch(l -> l.getClass().getName().contains("LightUiStyles")) && noUiMarks(partRoot(protectedEditor)),id+" editor business content has no selection hook or UI marks, listeners="+protectedEditor.tree.getListeners(SWT.EraseItem).length+" initial="+protectedErase+" marks="+!noUiMarks(partRoot(protectedEditor))+" classes="+Arrays.toString(Arrays.stream(protectedEditor.tree.getListeners(SWT.EraseItem)).map(l -> l.getClass().getName()).toArray()));
            selectionCycle(tree,focus,palette,"plain Tree");selectionCycle(styled.getTree(),focus,palette,"JFace styled Tree");
            var ti=table.getItem(0);table.setSelection(ti);table.setFocus();table.redraw();table.update();pump(150);check(table.isFocusControl(),"Table focus acquired");rowSelectionPixels(table,ti,palette,"Table focused");
            focus.setFocus();table.redraw();table.update();pump(150);rowSelectionPixels(table,ti,palette,"Table inactive");table.deselectAll();table.redraw();table.update();pump(100);check(rgb(palette,"editor.background").equals(modalPixel(table,ti.getBounds(0))),"Table deselected background");
            check(tree.getItem(0).getChecked(),"checkbox state preserved");
            if(id.equals("github-light-default")){tree.setSelection(tree.getItem(1));focus.setFocus();pump(150);captureSurfaceShell(wb.getActiveWorkbenchWindow().getShell(),"selection-after");}
            page.setPartState(page.findViewReference("ru.litonchik.edt.themes.probe.ui"),IWorkbenchPage.STATE_RESTORED);
            checkStagingFields(page,staging,fields,palette);
            if(id.equals("github-light-default"))captureSurfaceShell(wb.getActiveWorkbenchWindow().getShell(),"staging-after");
            page.setPartState(page.findViewReference("org.eclipse.egit.ui.StagingView"),IWorkbenchPage.STATE_RESTORED);
        }
        page.closeEditor(protectedEditor,false);
        if(!restart){
            var palette=PaletteCatalog.load("github-light-default");coordinator.apply(palette.id(),true);page.activate(fixture);
            page.setPartState(page.findViewReference("ru.litonchik.edt.themes.probe.ui"),IWorkbenchPage.STATE_MAXIMIZED);
            var late=new Tree(fixture.root,SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);late.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL,SWT.FILL,true,true));
            for(String name:List.of("Late first","Late second","Late third"))new TreeItem(late,SWT.NONE).setText(name);
            fixture.root.layout(true,true);pump(300);selectionCycle(late,focus,palette,"late Tree");late.dispose();
            page.setPartState(page.findViewReference("ru.litonchik.edt.themes.probe.ui"),IWorkbenchPage.STATE_RESTORED);
        }
        if(restart){
            check(coordinator.restore(true).successful(),"selection restore succeeded");pump(300);
            check(Arrays.stream(tree.getListeners(SWT.EraseItem)).noneMatch(l -> l.getClass().getName().contains("LightUiStyles")),"custom Tree listener removed on restore");
            check(Arrays.stream(table.getListeners(SWT.EraseItem)).noneMatch(l -> l.getClass().getName().contains("LightUiStyles")),"custom Table listener removed on restore");
            var adapter=new EdtSettingsAdapter(wb.getService(IThemeEngine.class),wb.getThemeManager());
            for(String key:originals.stringPropertyNames())if(key.startsWith("pref|")){var parts=key.split("\\|",4);var value=originals.getProperty(key);check((value.equals("<absent>")?OriginalValue.absent():OriginalValue.of(value)).equals(adapter.read(new SettingKey(parts[1],parts[2],parts[3]))),"selection exact preference restore "+key);}
            for(var field:fields)check(field.getBackgroundImage()==null,"owned Forms field image removed on restore");
            check(!coordinator.hasSnapshot() && coordinator.activeProfile()==null,"selection journal and profile released");
        }else coordinator.apply("github-light-default",true);
        git.close();
    }

    private void runLightGallery() throws Exception {
        String profile=System.getProperty("edt.probe.phase").startsWith("candidate") ? candidateId() : "github-light-default";
        var coordinator=ThemeStartup.coordinator(); var workbench=PlatformUI.getWorkbench();
        if(System.getProperty("edt.probe.phase").endsWith("galleryprepare")) {
            check(coordinator.paletteDrafts().overrides(profile).isEmpty(),"light gallery uses standard palette");
            coordinator.apply(profile,true); return;
        }
        check(profile.equals(coordinator.activeProfile()),"light gallery starts after native Light restart");
        check(!new CodeFontSettings().hasOriginal(),"light gallery uses restored standard font");
        var shell=workbench.getActiveWorkbenchWindow().getShell();
        var page=workbench.showPerspective("ru.litonchik.edt.themes.probe.gallery",workbench.getActiveWorkbenchWindow()); page.resetPerspective();
        shell.setMaximized(false); shell.setSize(900,640);
        var monitor=shell.getMonitor().getClientArea(); shell.setLocation(monitor.x,monitor.y);
        var editor=tryOpenBsl(); check(editor!=null,"light gallery opens BSL sample"); editor.setTopIndex(0); editor.setSelection(0); pump(1800);
        var palette=PaletteCatalog.load(profile);
        check(rgb(palette,"editor.background").equals(editor.getBackground().getRGB()),"light gallery editor background");
        for(String[] token:new String[][]{{"Функция","keyword"},{"123.45","number"},{"Строка с кириллицей","string"},{"Комментарий","comment"}})
            check(rgb(palette,token[1]).equals(glyph(editor,token[0])),"light gallery "+token[1]+" glyph");
        panelRgb(page,profile);
        checkRuler(page.getActiveEditor(),rgb(palette,"editor.background"),profile+" gallery");
        captureSurfaceShell(shell,"gallery-"+profile);
        if ("noctis-lux".equals(profile)) {
            String text=editor.getText(); int start=text.indexOf("Текст =");
            editor.setSelection(start,start+"Текст".length()); editor.showSelection(); pump(300);
            check(rgb(palette,"selection.background").equals(editor.getSelectionBackground().getRGB()),"Lux selected text uses distinct palette background");
            captureSurfaceShell(shell,"noctis-lux-selection");
            check(text.equals(editor.getText()),"Lux selection capture does not change source");
            editor.setSelection(0);
        }
        check(coordinator.restore(true).successful(),"light gallery restores original settings");
    }

    /** Normal SWT view controls, deliberately outside all editor content. */
    public static class UiPanelView extends org.eclipse.ui.part.ViewPart {
        Composite root;
        final Map<String,Control> controls=new LinkedHashMap<>();
        @Override public void createPartControl(Composite parent) {
            root=new Composite(parent,SWT.NONE); root.setLayout(new org.eclipse.swt.layout.GridLayout());
            controls.put("label",new Label(root,SWT.NONE)); ((Label)controls.get("label")).setText("Панель проверки");
            controls.put("tree",new Tree(root,SWT.BORDER)); controls.put("table",new Table(root,SWT.BORDER));
            controls.put("text",new Text(root,SWT.BORDER)); controls.put("combo",new Combo(root,SWT.READ_ONLY));
            controls.put("button",new Button(root,SWT.PUSH)); ((Button)controls.get("button")).setText("Кнопка");
            controls.put("link",new Link(root,SWT.NONE)); ((Link)controls.get("link")).setText("<a>Ссылка</a>");
            controls.put("hyperlink",new org.eclipse.ui.forms.widgets.Hyperlink(root,SWT.NONE)); ((org.eclipse.ui.forms.widgets.Hyperlink)controls.get("hyperlink")).setText("Ссылка формы");
            controls.put("disabled",new Label(root,SWT.NONE)); controls.get("disabled").setEnabled(false);
        }
        @Override public void setFocus() { root.setFocus(); }
    }
    public static class ProtectedContentEditor extends org.eclipse.ui.part.EditorPart {
        Composite root; Tree tree; Canvas canvas;
        @Override public void init(IEditorSite site,IEditorInput input) { setSite(site); setInput(input); setPartName("Защищённое содержимое"); }
        @Override public void createPartControl(Composite parent) {
            root=new Composite(parent,SWT.NONE); root.setLayout(new org.eclipse.swt.layout.GridLayout());
            tree=new Tree(root,SWT.BORDER); canvas=new Canvas(root,SWT.NONE); new Label(root,SWT.NONE).setText("Содержимое редактора");
        }
        @Override public void setFocus() { tree.setFocus(); }
        @Override public boolean isDirty() { return false; }
        @Override public void doSave(IProgressMonitor monitor) {}
        @Override public void doSaveAs() {}
        @Override public boolean isSaveAsAllowed() { return false; }
    }
    private static class ProtectedInput implements IEditorInput {
        @Override public boolean exists() { return false; }
        @Override public org.eclipse.jface.resource.ImageDescriptor getImageDescriptor() { return null; }
        @Override public String getName() { return "protected"; }
        @Override public IPersistableElement getPersistable() { return null; }
        @Override public String getToolTipText() { return "protected"; }
        @Override public <T> T getAdapter(Class<T> type) { return null; }
    }
    private Control partRoot(IWorkbenchPart part) {
        var model=part.getSite().getService(org.eclipse.e4.ui.model.application.ui.basic.MPart.class);
        if(model==null || !(model.getWidget() instanceof Control)) throw new AssertionError("No public MPart widget: "+part);
        return (Control)model.getWidget();
    }
    private <T extends Control> T firstControl(Control root,Class<T> type) {
        if(type.isInstance(root)) return type.cast(root);
        if(root instanceof Composite composite) for(var child:composite.getChildren()) {
            T found=firstControl(child,type); if(found!=null) return found;
        }
        return null;
    }
    private boolean uiMarked(Control control) {
        String css=(String)control.getData("org.eclipse.e4.ui.css.CssClassName");
        return css!=null && Arrays.asList(css.split("\\s+")).contains("edtThemesLightUi");
    }
    private boolean noUiMarks(Control control) {
        if(uiMarked(control)) return false;
        if(control instanceof Composite composite) for(var child:composite.getChildren()) if(!noUiMarks(child)) return false;
        return true;
    }
    private void uiRgb(Control control,RGB bg,RGB fg,String label) {
        check(control!=null && !control.isDisposed(),label+" exists");
        check(bg.equals(control.getBackground().getRGB()),label+" actual background expected="+bg+" got="+control.getBackground().getRGB());
        check(fg.equals(control.getForeground().getRGB()),label+" actual foreground expected="+fg+" got="+control.getForeground().getRGB());
    }
    private void panelRgb(IWorkbenchPage page,String id) throws Exception {
        var p=PaletteCatalog.load(id);
        var nav=partRoot(page.findView("org.eclipse.ui.navigator.ProjectExplorer"));
        var props=partRoot(page.showView("org.eclipse.ui.views.PropertySheet")); pump(300);
        uiRgb(nav,rgb(p,"panel"),rgb(p,"text"),id+" Navigator container");
        uiRgb(firstControl(nav,Tree.class),rgb(p,"editor.background"),rgb(p,"text"),id+" Navigator Tree");
        uiRgb(props,rgb(p,"panel"),rgb(p,"text"),id+" Properties container");
        var tabs=(org.eclipse.swt.custom.CTabFolder)nav.getParent().getParent();
        check(rgb(p,"text").equals(tabs.getSelectionForeground().getRGB()),id+" selected tab actual foreground="+tabs.getSelectionForeground().getRGB());
        check(rgb(p,"editor.background").equals(tabs.getSelectionBackground().getRGB()),id+" selected tab actual background="+tabs.getSelectionBackground().getRGB());
    }
    private void runUiChecks() throws Exception {
        var wb=PlatformUI.getWorkbench(); var page=wb.getActiveWorkbenchWindow().getActivePage();
        var engine=wb.getService(IThemeEngine.class); var coordinator=ThemeStartup.coordinator();
        var adapter=new EdtSettingsAdapter(engine,wb.getThemeManager());
        var saved=java.nio.file.Path.of(System.getProperty("edt.probe.images"),"ui-originals.properties");
        var expected=new Properties();
        boolean restart="candidateuirestart".equals(System.getProperty("edt.probe.phase"));
        if(restart) {
            try(var reader=Files.newBufferedReader(saved)) { expected.load(reader); }
            check("noctis-lux".equals(coordinator.activeProfile()),"UI Light active profile survives JVM restart");
            pump(800); panelRgb(page,"noctis-lux");
        } else {
            if(coordinator.hasSnapshot()) check(coordinator.restore(true).successful(),"UI initial restore");
            engine.setTheme(ThemeMode.LIGHT.cssId(),true); pump(600);
            var standard=coordinator.paletteDrafts(); for(String id:PaletteCatalog.ids()) standard.resetPalette(id);
            coordinator.apply(null,true,standard);
            var nav=partRoot(page.findView("org.eclipse.ui.navigator.ProjectExplorer"));
            Tree tree=firstControl(nav,Tree.class);
            expected.setProperty("tree.bg",tree.getBackground().getRGB().toString());
            expected.setProperty("tree.fg",tree.getForeground().getRGB().toString());
            expected.setProperty("tree.style",Objects.toString(tree.getData("style"),"<null>"));
            expected.setProperty("tree.css",Objects.toString((String)tree.getData("org.eclipse.e4.ui.css.CssClassName"),"<null>"));
            expected.setProperty("font",InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").get(JFaceResources.TEXT_FONT,"<absent>"));
            for(var key:adapter.plan(PaletteCatalog.load("noctis-lux"),true).keySet()) {
                var value=adapter.read(key); expected.setProperty("pref|"+key.scope()+"|"+key.node()+"|"+key.key(),value.present()?value.value():"<absent>");
            }
            check(noUiMarks(wb.getActiveWorkbenchWindow().getShell()),"installed CSS does not mark native Light without active profile");
        }
        var bsl=tryOpenBsl(); var bslEditor=(org.eclipse.xtext.ui.editor.XtextEditor)page.getActiveEditor();
        String source=bslEditor.getDocument().get(); long stamp=((org.eclipse.jface.text.IDocumentExtension4)bslEditor.getDocument()).getModificationStamp();
        var protectedEditor=(ProtectedContentEditor)page.openEditor(new ProtectedInput(),"ru.litonchik.edt.themes.probe.protected");
        var fixture=(UiPanelView)page.showView("ru.litonchik.edt.themes.probe.ui");
        pump(400);
        Color ownBg=new Color(wb.getDisplay(),45,67,89), ownFg=new Color(wb.getDisplay(),210,220,230);
        try {
            for(var profile:PaletteCatalog.profiles(ThemeMode.LIGHT)) {
                String id=profile.id(); var p=PaletteCatalog.load(id);
                coordinator.apply(id,true); pump(600); panelRgb(page,id);
                for(var e:fixture.controls.entrySet()) {
                    boolean body=Set.of("tree","table","text","combo").contains(e.getKey());
                    uiRgb(e.getValue(),rgb(p,body?"editor.background":"panel"),rgb(p,e.getKey().equals("disabled")?"disabled":Set.of("link","hyperlink").contains(e.getKey())?"accent":"text"),id+" view "+e.getKey());
                }
                check(noUiMarks(partRoot(protectedEditor)),id+" entire arbitrary editor subtree excluded");
                check(noUiMarks(partRoot(bslEditor)),id+" entire BSL editor subtree excluded");
                protectedEditor.tree.setBackground(ownBg); protectedEditor.tree.setForeground(ownFg); protectedEditor.canvas.setBackground(ownBg);
                LightUiStyles.refresh(engine); pump(100);
                check(ownBg.getRGB().equals(protectedEditor.tree.getBackground().getRGB()) && ownFg.getRGB().equals(protectedEditor.tree.getForeground().getRGB()) && ownBg.getRGB().equals(protectedEditor.canvas.getBackground().getRGB()),id+" UI layer preserves editor business colors");
                check(rgb(p,"editor.background").equals(bsl.getBackground().getRGB()) && rgb(p,"keyword").equals(glyph(bsl,"Функция")),id+" BSL background and tokens preserved");
                check(source.equals(bslEditor.getDocument().get()) && stamp==((org.eclipse.jface.text.IDocumentExtension4)bslEditor.getDocument()).getModificationStamp(),id+" source and modification stamp unchanged");
                var late=new Tree(fixture.root,SWT.BORDER); late.moveAbove(null); fixture.root.layout(true,true); late.redraw(); pump(500);
                uiRgb(late,rgb(p,"editor.background"),rgb(p,"text"),id+" newly created view Tree"); late.dispose();
            }
            var draft=coordinator.paletteDrafts(); draft.edit("noctis-lux","panel","#E8E1CE"); coordinator.apply("noctis-lux",true,draft); pump(400);
            check(new RGB(232,225,206).equals(partRoot(page.findView("org.eclipse.ui.navigator.ProjectExplorer")).getBackground().getRGB()),"personal panel color reaches actual Navigator");
            draft=coordinator.paletteDrafts(); draft.resetRole("noctis-lux","panel"); coordinator.apply("noctis-lux",true,draft); pump(300); panelRgb(page,"noctis-lux");
            // Class restoration also preserves classes added by another component while active.
            var tree=firstControl(partRoot(page.findView("org.eclipse.ui.navigator.ProjectExplorer")),Tree.class);
            String marked=(String)tree.getData("org.eclipse.e4.ui.css.CssClassName");
            tree.setData("style",tree.getData("style")+";font-style:normal;");
            tree.setData("org.eclipse.e4.ui.css.CssClassName",marked+" thirdPartyDynamic");
            tree.setForeground(ownFg); LightUiStyles.refresh(engine); pump(200);
            check(Objects.toString(tree.getData("org.eclipse.e4.ui.css.CssClassName"),"").contains("thirdPartyDynamic"),"UI refresh preserves dynamically added third-party class");
            coordinator.apply("darcula",true); pump(500);
            check(noUiMarks(wb.getActiveWorkbenchWindow().getShell()),"Light markers removed on Dark handoff");
            check(Objects.toString(tree.getData("style"),"").contains("font-style:normal") && !Objects.toString(tree.getData("style"),"").contains("edt-themes-light:start"),"Dark handoff preserves third-party inline style");
            tree.setData("style",expected.getProperty("tree.style").equals("<null>")?null:expected.getProperty("tree.style"));
            check(Objects.toString((String)tree.getData("org.eclipse.e4.ui.css.CssClassName"),"").contains("thirdPartyDynamic"),"Dark handoff preserves third-party CSS class");
            tree.setData("org.eclipse.e4.ui.css.CssClassName",expected.getProperty("tree.css").equals("<null>")?null:expected.getProperty("tree.css"));
            coordinator.apply("noctis-lux",true); pump(400);
            var newWindow=wb.openWorkbenchWindow("ru.litonchik.edt.themes.probe.gallery",null); pump(700);
            panelRgb(newWindow.getActivePage(),"noctis-lux");
            newWindow.close(); page.activate(bslEditor); pump(300);
            var dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); pump(300);
            uiRgb((Control)findData(dialog.getShell(),"palette.roles"),rgb(PaletteCatalog.load("noctis-lux"),"editor.background"),rgb(PaletteCatalog.load("noctis-lux"),"text"),"own preferences palette table");
            dialog.close();
        } finally { page.closeEditor(protectedEditor,false); page.hideView(fixture); ownBg.dispose(); ownFg.dispose(); }
        if(!restart) {
            try(var writer=Files.newBufferedWriter(saved)) { expected.store(writer,"Actual widget and preference baseline before Light UI palette"); }
            check("noctis-lux".equals(coordinator.activeProfile()),"UI palette left for restart");
            return;
        }
        check(coordinator.restore(true).successful(),"UI restore succeeds"); pump(500);
        var tree=firstControl(partRoot(page.findView("org.eclipse.ui.navigator.ProjectExplorer")),Tree.class);
        check(expected.getProperty("tree.bg").equals(tree.getBackground().getRGB().toString()),"UI restore actual native Tree background");
        check(expected.getProperty("tree.fg").equals(tree.getForeground().getRGB().toString()),"UI restore actual native Tree foreground");
        check(expected.getProperty("tree.css").equals(Objects.toString((String)tree.getData("org.eclipse.e4.ui.css.CssClassName"),"<null>")),"UI restore original CSS class including absence");
        check(noUiMarks(wb.getActiveWorkbenchWindow().getShell()),"UI restore removes all workbench markers");
        check(expected.getProperty("tree.style").equals(Objects.toString(tree.getData("style"),"<null>")),"UI restore exact original inline style including absence");
        for(String key:expected.stringPropertyNames()) if(key.startsWith("pref|")) {
            String[] parts=key.split("\\|",4); String value=expected.getProperty(key);
            check((value.equals("<absent>")?OriginalValue.absent():OriginalValue.of(value)).equals(adapter.read(new SettingKey(parts[1],parts[2],parts[3]))),"UI exact restore "+key);
        }
        check(expected.getProperty("font").equals(InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").get(JFaceResources.TEXT_FONT,"<absent>")),"UI leaves code font unchanged");
        coordinator.apply("noctis-lux",true); pump(300); engine.setTheme(ThemeMode.DARK.cssId(),true); pump(600);
        check(coordinator.activeProfile()==null && noUiMarks(wb.getActiveWorkbenchWindow().getShell()),"external native Dark releases profile and UI markers");
        check(coordinator.restore(true).successful(),"UI external native switch cleanup");
    }

    private void runUiGallery() throws Exception {
        var wb=PlatformUI.getWorkbench(); var coordinator=ThemeStartup.coordinator();
        check("noctis-lux".equals(coordinator.activeProfile()),"UI gallery starts after persisted native Light restart");
        check(!new CodeFontSettings().hasOriginal(),"UI gallery uses original font");
        var window=wb.getActiveWorkbenchWindow(); var page=wb.showPerspective("ru.litonchik.edt.themes.probe.gallery",window); page.resetPerspective();
        var shell=window.getShell(); shell.setSize(900,640); shell.layout(true,true);
        var editor=tryOpenBsl(); editor.setTopIndex(0); editor.setSelection(0); pump(900);
        for(var profile:PaletteCatalog.profiles(ThemeMode.LIGHT)) {
            String id=profile.id(); var p=PaletteCatalog.load(id);
            check(coordinator.paletteDrafts().overrides(id).isEmpty(),id+" gallery standard colors");
            coordinator.apply(id,true); pump(900); panelRgb(page,id);
            for(String[] token:new String[][]{{"Функция","keyword"},{"123.45","number"},{"Строка с кириллицей","string"},{"Комментарий","comment"}})
                check(rgb(p,token[1]).equals(glyph(editor,token[0])),id+" gallery actual BSL "+token[1]);
            checkRuler(page.getActiveEditor(),rgb(p,"editor.background"),id+" gallery");
            captureSurfaceShell(shell,"gallery-"+id);
        }
        check(coordinator.restore(true).successful(),"UI gallery restores appearance");
    }

    private List<Control> rulerControls(IEditorPart editor) {
        var info=editor.getAdapter(org.eclipse.jface.text.source.IVerticalRulerInfo.class);
        if(!(info instanceof org.eclipse.jface.text.source.CompositeRuler ruler)) throw new AssertionError("No composite ruler for "+editor.getClass());
        var controls=new ArrayList<Control>(); controls.add(ruler.getControl());
        for(var it=ruler.getDecoratorIterator();it.hasNext();) controls.add(it.next().getControl());
        return controls;
    }
    private RGB rulerPixel(Control control) {
        var size=control.getSize(); if(size.x<1 || size.y<1 || !control.isVisible()) return null;
        var image=new Image(control.getDisplay(),size.x,size.y); var gc=new GC(control);
        try { gc.copyArea(image,0,0); var data=image.getImageData(); return data.palette.getRGB(data.getPixel(size.x/2,size.y-3)); }
        finally { gc.dispose(); image.dispose(); }
    }
    private void checkRuler(IEditorPart editor,RGB expected,String label) {
        var controls=rulerControls(editor); check(controls.size()>=3,label+" native ruler columns available");
        for(int i=0;i<controls.size();i++) {
            var control=controls.get(i);
            check(expected.equals(control.getBackground().getRGB()),label+" ruler control "+i+" background expected="+expected+" got="+control.getBackground().getRGB());
            var pixel=rulerPixel(control);
            if(pixel!=null) check(expected.equals(pixel),label+" ruler control "+i+" actual background pixel="+pixel);
        }
    }
    private void runRulerChecks() throws Exception {
        var wb=PlatformUI.getWorkbench(); var page=wb.getActiveWorkbenchWindow().getActivePage();
        var coordinator=ThemeStartup.coordinator(); var engine=wb.getService(IThemeEngine.class);
        var adapter=new EdtSettingsAdapter(engine,wb.getThemeManager());
        var saved=java.nio.file.Path.of(System.getProperty("edt.probe.images"),"ruler-originals.properties");
        var expected=new Properties(); boolean restart="candidaterulersrestart".equals(System.getProperty("edt.probe.phase"));
        if(restart) {
            try(var reader=Files.newBufferedReader(saved)) { expected.load(reader); }
            check("noctis-lux".equals(coordinator.activeProfile()),"ruler profile survives JVM restart");
        } else {
            if(coordinator.hasSnapshot()) check(coordinator.restore(true).successful(),"ruler initial restore");
            engine.setTheme(ThemeMode.LIGHT.cssId(),true); pump(500);
        }
        var text=tryOpenBsl(); check(text!=null,"ruler BSL fixture");
        var editor=page.getActiveEditor(); page.activate(editor); pump(500);
        var font=Arrays.toString(text.getFont().getFontData());
        var document=((org.eclipse.xtext.ui.editor.XtextEditor)editor).getDocument();
        String source=document.get(); long stamp=((org.eclipse.jface.text.IDocumentExtension4)document).getModificationStamp();
        if(restart) checkRuler(editor,rgb(PaletteCatalog.load("noctis-lux"),"editor.background"),"restart");
        else {
            var controls=rulerControls(editor);
            for(int i=0;i<controls.size();i++) {
                expected.setProperty("bg."+i,controls.get(i).getBackground().getRGB().toString());
                var pixel=rulerPixel(controls.get(i)); if(pixel!=null) expected.setProperty("pixel."+i,pixel.toString());
            }
            var baseline=new LinkedHashMap<SettingKey,OriginalValue>();
            for(var key:adapter.plan(PaletteCatalog.load("github-light-default"),true).keySet()) baseline.put(key,adapter.read(key));
            for(var key:adapter.plan(PaletteCatalog.load("darcula"),true).keySet()) baseline.putIfAbsent(key,adapter.read(key));
            for(var key:NativeSettingsCatalog.load().keySet()) baseline.putIfAbsent(key,adapter.read(key));
            for(var e:baseline.entrySet()) expected.setProperty("pref|"+e.getKey().scope()+"|"+e.getKey().node()+"|"+e.getKey().key(),e.getValue().present()?e.getValue().value():"<absent>");
        }
        var originalControls=rulerControls(editor);
        for(String id:PaletteCatalog.ids()) {
            coordinator.apply(id,true); pump(500);
            checkRuler(editor,rgb(PaletteCatalog.load(id),"editor.background"),id);
            check(originalControls.equals(rulerControls(editor)),id+" same native columns and controls");
            check(source.equals(document.get()) && stamp==((org.eclipse.jface.text.IDocumentExtension4)document).getModificationStamp(),id+" source and stamp unchanged");
            check(font.equals(Arrays.toString(text.getFont().getFontData())),id+" code font unchanged");
        }
        var drafts=coordinator.paletteDrafts(); drafts.edit("noctis-lux","editor.background","#F0E8D8"); coordinator.apply("noctis-lux",true,drafts); pump(400);
        checkRuler(editor,new RGB(240,232,216),"personal editor background");
        drafts=coordinator.paletteDrafts(); drafts.resetRole("noctis-lux","editor.background"); coordinator.apply("noctis-lux",true,drafts); pump(400);
        checkRuler(editor,rgb(PaletteCatalog.load("noctis-lux"),"editor.background"),"reset background role");
        page.closeEditor(editor,false); text=tryOpenBsl(); editor=page.getActiveEditor(); pump(500);
        checkRuler(editor,rgb(PaletteCatalog.load("noctis-lux"),"editor.background"),"new BSL editor after palette apply");
        if(!restart) {
            try(var writer=Files.newBufferedWriter(saved)) { expected.store(writer,"Ruler original controls, pixels and exact explicit preferences"); }
            captureSurfaceShell(wb.getActiveWorkbenchWindow().getShell(),"ruler-noctis-lux"); return;
        }
        check(coordinator.restore(true).successful(),"ruler restore succeeds"); pump(500);
        var controls=rulerControls(editor);
        for(int i=0;i<controls.size();i++) {
            check(expected.getProperty("bg."+i).equals(controls.get(i).getBackground().getRGB().toString()),"ruler restore exact control BG "+i+" actual="+controls.get(i).getBackground().getRGB());
            if(expected.containsKey("pixel."+i)) check(expected.getProperty("pixel."+i).equals(Objects.toString(rulerPixel(controls.get(i)))),"ruler restore original pixel "+i);
        }
        for(String key:expected.stringPropertyNames()) if(key.startsWith("pref|")) {
            String[] parts=key.split("\\|",4); String value=expected.getProperty(key);
            check((value.equals("<absent>")?OriginalValue.absent():OriginalValue.of(value)).equals(adapter.read(new SettingKey(parts[1],parts[2],parts[3]))),"ruler exact restore "+key);
        }
        check(!coordinator.hasSnapshot() && coordinator.activeProfile()==null,"ruler restore releases profile and journal");
    }

    private String candidateId() { return System.getProperty("edt.probe.profile","winter-is-coming-light"); }
    private void checkCandidateRegistry(String id) {
        var p=PaletteCatalog.load(id);
        var registry=PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
        for(var entry:SettingCatalog.load().entries()) if(entry.kind().equals("color"))
            check(rgb(p,entry.role()).equals(registry.getRGB(entry.key())),id+" registry "+entry.key());
    }
    private void runCandidateChecks() throws Exception {
        String id=candidateId(); var palette=PaletteCatalog.load(id);
        var workbench=PlatformUI.getWorkbench(); var engine=workbench.getService(IThemeEngine.class);
        var coordinator=ThemeStartup.coordinator(); var prefs=InstanceScope.INSTANCE.getNode("ru.litonchik.edt.themes");
        var adapter=new EdtSettingsAdapter(engine,workbench.getThemeManager());
        var file=java.nio.file.Path.of(System.getProperty("edt.probe.images"),"candidate-originals.properties");
        var expected=new Properties();
        var fontNode=InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");
        if("candidaterestart".equals(System.getProperty("edt.probe.phase"))) {
            try(var reader=Files.newBufferedReader(file)) { expected.load(reader); }
            check(id.equals(coordinator.activeProfile()),"candidate active profile survives restart");
            check(ThemeMode.LIGHT.cssId().equals(engine.getActiveTheme().getId()),"candidate native Light survives restart");
            check("#123456".equals(prefs.get("palette."+id+".keyword",null)),"candidate personal keyword survives restart");
            checkCandidateRegistry(id);
            var editor=tryOpenBsl(); check(editor!=null,"candidate restart BSL editor"); pump(1000);
            check(rgb(palette,"editor.background").equals(editor.getBackground().getRGB()),"candidate restart BSL background");
            check(rgb(palette,"text").equals(editor.getForeground().getRGB()),"candidate restart BSL foreground");
            panelRgb(workbench.getActiveWorkbenchWindow().getActivePage(),id);
            checkRuler(workbench.getActiveWorkbenchWindow().getActivePage().getActiveEditor(),rgb(palette,"editor.background"),id+" candidate restart");
            check(new RGB(18,52,86).equals(glyph(editor,"Функция")),"candidate personal keyword paints after restart");
            var drafts=coordinator.paletteDrafts(); drafts.resetRole(id,"keyword"); coordinator.apply(id,true,drafts); pump(1000);
            check(rgb(palette,"keyword").equals(glyph(editor,"Функция")),"candidate role reset restores style glyph");
            check(prefs.get("palette."+id+".keyword",null)==null,"candidate reset removes personal key");
            var dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); pump(300);
            check(findButton(dialog.getShell(),"Светлые").getSelection(),"candidate restart selects light group");
            check(PaletteCatalog.profiles(ThemeMode.LIGHT).stream().map(PaletteCatalog.Profile::id).toList().indexOf(id)+1==findCombo(dialog.getShell()).getSelectionIndex(),"candidate restart selects its own profile");
            captureSurfaceShell(dialog.getShell(),id+"-settings"); dialog.close();
            var manualBackground=new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Background");
            InstanceScope.INSTANCE.getNode(manualBackground.node()).put(manualBackground.key(),"250,249,240");
            InstanceScope.INSTANCE.getNode(manualBackground.node()).flush(); pump(200);
            coordinator.apply(id,true); pump(800);
            check(OriginalValue.of("250,249,240").equals(adapter.read(manualBackground)),"candidate preserves later explicit background preference");
            check(new RGB(250,249,240).equals(editor.getBackground().getRGB()),"candidate preserves later explicit background glyph surface after CSS");
            check(coordinator.restore(true).successful(),"candidate restores original appearance");
            for(String key:expected.stringPropertyNames()) if(key.startsWith("setting|")) {
                String[] part=key.split("\\|",4); String value=expected.getProperty(key);
                check((value.equals("<absent>")?OriginalValue.absent():OriginalValue.of(value)).equals(adapter.read(new SettingKey(part[1],part[2],part[3]))),"candidate exact restore "+key);
            }
            for(String previous:PaletteCatalog.ids()) if(!previous.equals(id))
                check("#123456".equals(prefs.get("palette."+previous+".comment",null)),"candidate restore preserves personal "+previous);
            check(expected.getProperty("font").equals(fontNode.get(JFaceResources.TEXT_FONT,"<absent>")),"candidate restores appearance without changing font");
            return;
        }
        if(coordinator.hasSnapshot()) check(coordinator.restore(true).successful(),"candidate initial cleanup");
        engine.setTheme(ThemeMode.DARK.cssId(),true); pump(800);
        var baseline=new LinkedHashMap<SettingKey,OriginalValue>();
        for(String profile:List.of("noctis-minimus",id)) for(var key:adapter.plan(PaletteCatalog.load(profile),true).keySet()) baseline.putIfAbsent(key,adapter.read(key));
        for(var key:NativeSettingsCatalog.load().keySet()) baseline.putIfAbsent(key,adapter.read(key));
        for(var entry:baseline.entrySet()) expected.setProperty("setting|"+entry.getKey().scope()+"|"+entry.getKey().node()+"|"+entry.getKey().key(),entry.getValue().present()?entry.getValue().value():"<absent>");
        expected.setProperty("font",fontNode.get(JFaceResources.TEXT_FONT,"<absent>"));
        for(String previous:PaletteCatalog.ids()) if(!previous.equals(id)) prefs.put("palette."+previous+".comment","#123456"); prefs.flush();
        var dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); selectMode(dialog,ThemeMode.LIGHT);
        int index=PaletteCatalog.profiles(ThemeMode.LIGHT).stream().map(PaletteCatalog.Profile::id).toList().indexOf(id)+1;
        selectProfile(dialog,index);
        check(profileCount(dialog)==PaletteCatalog.profiles(ThemeMode.LIGHT).size()+1,"candidate appears beside existing light profiles");
        check(PaletteCatalog.profiles().stream().filter(p -> p.id().equals(id)).findFirst().orElseThrow().label().equals(findCombo(dialog.getShell()).getText()),"candidate label visible in light group");
        check(rgb(palette,"editor.background").equals(findCodePreview(dialog.getShell()).getBackground().getRGB()),"candidate preview background before apply");
        check(ThemeMode.DARK.cssId().equals(engine.getActiveTheme().getId()),"candidate selection stays local before Apply");
        editRole(dialog,"comment","#234567"); dialog.close();
        check(prefs.get("palette."+id+".comment",null)==null,"candidate Cancel discards pending color");
        dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); selectMode(dialog,ThemeMode.LIGHT); selectProfile(dialog,index);
        findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event()); pump(1100); dialog.close();
        check(id.equals(coordinator.activeProfile()),"candidate Apply selects profile");
        check(ThemeMode.LIGHT.cssId().equals(engine.getActiveTheme().getId()),"candidate applies native Light"); checkCandidateRegistry(id);
        var editor=tryOpenBsl(); check(editor!=null,"candidate BSL editor"); pump(1000);
        check(rgb(palette,"editor.background").equals(editor.getBackground().getRGB()),"candidate BSL background");
        check(rgb(palette,"text").equals(editor.getForeground().getRGB()),"candidate BSL foreground");
        panelRgb(workbench.getActiveWorkbenchWindow().getActivePage(),id);
        checkRuler(workbench.getActiveWorkbenchWindow().getActivePage().getActiveEditor(),rgb(palette,"editor.background"),id+" candidate");
        for(String[] token:new String[][]{{"Функция","keyword"},{"123.45","number"},{"Строка с кириллицей","string"},{"Комментарий","comment"}})
            check(rgb(palette,token[1]).equals(glyph(editor,token[0])),"candidate BSL "+token[1]+" glyph");
        check(rgb(palette,"selection.text").equals(editor.getSelectionForeground().getRGB()),"candidate readable selection foreground");
        var active=(org.eclipse.xtext.ui.editor.XtextEditor)workbench.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        String source=active.getDocument().get(); long stamp=((org.eclipse.jface.text.IDocumentExtension4)active.getDocument()).getModificationStamp();
        for(String profile:List.of("noctis-minimus","github-light-default","winter-is-coming-light","night-owl-light","ayu-light","noctis-lux","visual-studio-light-cpp","noctis-hibernus",id)) {
            coordinator.apply(profile,true); pump(600);
            check(PaletteCatalog.mode(profile).cssId().equals(engine.getActiveTheme().getId()),"candidate transition native mode "+profile);
            check(source.equals(active.getDocument().get()) && stamp==((org.eclipse.jface.text.IDocumentExtension4)active.getDocument()).getModificationStamp(),"candidate transition keeps source/stamp "+profile);
        }
        dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); editRole(dialog,"keyword","#123456");
        findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event()); pump(800); dialog.close();
        check("#123456".equals(prefs.get("palette."+id+".keyword",null)),"candidate saves personal keyword");
        check(new RGB(18,52,86).equals(glyph(editor,"Функция")),"candidate personal keyword paints without editing");
        for(String previous:PaletteCatalog.ids()) if(!previous.equals(id))
            check("#123456".equals(prefs.get("palette."+previous+".comment",null)),"candidate preserves personal "+previous);
        check(expected.getProperty("font").equals(fontNode.get(JFaceResources.TEXT_FONT,"<absent>")),"candidate apply leaves shared font unchanged");
        try(var writer=Files.newBufferedWriter(file)) { expected.store(writer,"Candidate original explicit preferences"); }
    }

    private void runLightChecks() throws Exception {
        var workbench=PlatformUI.getWorkbench(); var engine=workbench.getService(IThemeEngine.class);
        var coordinator=ThemeStartup.coordinator(); var prefs=InstanceScope.INSTANCE.getNode("ru.litonchik.edt.themes");
        var adapter=new EdtSettingsAdapter(engine,workbench.getThemeManager());
        var expectedFile=java.nio.file.Path.of(System.getProperty("edt.probe.images"),"light-originals.properties");
        var expected=new Properties();
        if("lightrestart".equals(System.getProperty("edt.probe.phase"))) {
            try(var reader=Files.newBufferedReader(expectedFile)) { expected.load(reader); }
            check("github-light-default".equals(coordinator.activeProfile()),"light active profile survives JVM restart");
            check(ThemeMode.LIGHT.cssId().equals(engine.getActiveTheme().getId()),"native Light survives JVM restart");
            checkLightRegistry();
            var dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); pump(300);
            check(findButton(dialog.getShell(),"Светлые").getSelection(),"restart opens light group");
            check(findCombo(dialog.getShell()).getSelectionIndex()==1,"restart selects GitHub Light");
            captureSurfaceShell(dialog.getShell(),"github-light-settings"); dialog.close();
            StyledText text=tryOpenBsl(); pump(1500);
            check(new RGB(255,255,255).equals(text.getBackground().getRGB()),"light BSL background survives restart");
            var fontNode=InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");
            check(expected.getProperty("font.applied").equals(fontNode.get(JFaceResources.TEXT_FONT,null)),"shared font survives light restart");
            captureSurfaceShell(workbench.getActiveWorkbenchWindow().getShell(),"github-light-workbench");
            check(coordinator.restore(true).successful(),"light restore after restart");
            for(String key:expected.stringPropertyNames()) if(key.startsWith("setting|")) {
                String[] fields=key.substring(8).split("\\|",3); var setting=new SettingKey(fields[0],fields[1],fields[2]);
                String encoded=expected.getProperty(key); var original=encoded.equals("<absent>")?OriginalValue.absent():OriginalValue.of(encoded);
                check(original.equals(adapter.read(setting)),"light exact restore "+setting);
            }
            new CodeFontSettings().restore();
            check(Objects.equals(expected.getProperty("font.original").equals("<absent>")?null:expected.getProperty("font.original"),fontNode.get(JFaceResources.TEXT_FONT,null)),"light restores original shared font separately");
            for(var profile:PaletteCatalog.profiles(ThemeMode.DARK)) check("#123456".equals(prefs.get("palette."+profile.id()+".comment",null)),"light restore preserves old personal "+profile.id());
            return;
        }
        if(coordinator.hasSnapshot()) check(coordinator.restore(true).successful(),"light starts with clean journal");
        engine.setTheme(ThemeMode.DARK.cssId(),true); pump(900);
        var baseline=new LinkedHashMap<SettingKey,OriginalValue>();
        for(String id:List.of("github-dark-default","github-light-default"))
            for(var key:adapter.plan(PaletteCatalog.load(id),true).keySet()) baseline.putIfAbsent(key,adapter.read(key));
        for(var key:NativeSettingsCatalog.load().keySet()) baseline.putIfAbsent(key,adapter.read(key));
        for(var entry:baseline.entrySet()) expected.setProperty("setting|"+entry.getKey().scope()+"|"+entry.getKey().node()+"|"+entry.getKey().key(),entry.getValue().present()?entry.getValue().value():"<absent>");
        var fontNode=InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");
        expected.setProperty("font.original",fontNode.get(JFaceResources.TEXT_FONT,"<absent>"));
        for(var profile:PaletteCatalog.profiles(ThemeMode.DARK)) prefs.put("palette."+profile.id()+".comment","#123456"); prefs.flush();
        coordinator.apply("github-dark-default",true); pump(900);
        var dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); pump(250);
        check(findButton(dialog.getShell(),"Тёмные").getSelection(),"active dark profile opens dark group");
        check(profileCount(dialog)==12,"dark filter shows native plus eleven dark themes");
        selectMode(dialog,ThemeMode.LIGHT);
        check(profileCount(dialog)==PaletteCatalog.profiles(ThemeMode.LIGHT).size()+1,"light filter shows native plus all light profiles");
        check("GitHub Light Default".equals(findCombo(dialog.getShell()).getText()),"light first selection is pending GitHub Light");
        check(new RGB(255,255,255).equals(findCodePreview(dialog.getShell()).getBackground().getRGB()),"light preview renders before apply");
        check(ThemeMode.DARK.cssId().equals(engine.getActiveTheme().getId()),"radio switch does not apply native theme");
        editRole(dialog,"comment","#234567");
        selectMode(dialog,ThemeMode.DARK); selectMode(dialog,ThemeMode.LIGHT);
        check("#234567".equals(((Text)findData(dialog.getShell(),"palette.hex")).getText()),"switching groups preserves hidden color draft");
        dialog.close();
        check(prefs.get("palette.github-light-default.comment",null)==null,"Cancel discards pending light colors");
        check("github-dark-default".equals(coordinator.activeProfile()),"Cancel preserves applied dark profile");
        dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); selectMode(dialog,ThemeMode.LIGHT);
        editRole(dialog,"comment","#broken"); selectMode(dialog,ThemeMode.DARK);
        check(!findApply(dialog.getShell()).getEnabled(),"invalid hidden light HEX blocks Apply in dark group");
        selectMode(dialog,ThemeMode.LIGHT); editRole(dialog,"comment","#656D76");
        var originalFont=JFaceResources.getFontRegistry().getFontData(JFaceResources.TEXT_FONT)[0];
        selectFont(findFontGroup(dialog.getShell()),originalFont.getName(),originalFont.getHeight()+1);
        findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event()); pump(1200);
        check("github-light-default".equals(coordinator.activeProfile()),"Apply activates GitHub Light");
        check(ThemeMode.LIGHT.cssId().equals(engine.getActiveTheme().getId()),"Apply selects native Light");
        checkLightRegistry();
        check(OriginalValue.of("240,240,240").equals(adapter.read(new SettingKey("instance","com._1c.g5.v8.dt.theming.ui","com._1c.g5.v8.dt.theming.ui.BROWSERS_SCROLL_TRACK_COLOR"))),"native Dark scrollbar seed switches to Light");
        expected.setProperty("font.applied",fontNode.get(JFaceResources.TEXT_FONT,"<absent>"));
        captureSurfaceShell(dialog.getShell(),"github-light-settings-applied"); dialog.close();
        StyledText text=tryOpenBsl(); pump(1800);
        var editor=(org.eclipse.xtext.ui.editor.XtextEditor)workbench.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        String source=editor.getDocument().get(); long stamp=((org.eclipse.jface.text.IDocumentExtension4)editor.getDocument()).getModificationStamp();
        for(String id:List.of("github-dark-default","github-light-default","darcula","github-light-default")) {
            coordinator.apply(id,true); pump(1300);
            check(PaletteCatalog.mode(id).cssId().equals(engine.getActiveTheme().getId()),id+" exact native base during mode transition");
            check(rgb(PaletteCatalog.load(id),"accent").equals(glyph(text,"Сообщить")),id+" semantic call refresh without editing");
            check(source.equals(editor.getDocument().get()) && stamp==((org.eclipse.jface.text.IDocumentExtension4)editor.getDocument()).getModificationStamp(),id+" switching modes preserves document stamp");
            check(expected.getProperty("font.applied").equals(fontNode.get(JFaceResources.TEXT_FONT,null)),id+" preserves common font");
        }
        check(new RGB(31,35,40).equals(text.getSelectionForeground().getRGB()),"light selection uses readable foreground");
        text.setSelection(0,Math.min(30,text.getCharCount())); captureControl(text,"github-light-selection"); text.setSelection(0);
        coordinator.apply("github-light-default",false);
        for(var entry:baseline.entrySet()) if(entry.getKey().node().equals("com._1c.g5.v8.dt.bsl.ui"))
            check(entry.getValue().equals(adapter.read(entry.getKey())),"light BSL off restores original "+entry.getKey().key());
        coordinator.apply("github-light-default",true); pump(600);
        engine.setTheme(ThemeMode.DARK.cssId(),true); pump(1600);
        check(coordinator.activeProfile()==null && ThemeMode.DARK.cssId().equals(engine.getActiveTheme().getId()),"external native Dark releases light profile without switching back");
        check(coordinator.restore(true).successful(),"clear native-switch conflicts before restart scenario");
        coordinator.apply("github-light-default",true);
        for(var profile:PaletteCatalog.profiles(ThemeMode.DARK)) check("#123456".equals(prefs.get("palette."+profile.id()+".comment",null)),"old personal palette preserved "+profile.id());
        try(var writer=Files.newBufferedWriter(expectedFile)) { expected.store(writer,"Original explicit values before Light/Dark transitions"); }
    }

    private void runSurfaceChecks() throws Exception {
        String lightProfile=System.getProperty("edt.probe.phase").startsWith("candidate") ? candidateId() : "github-light-default";
        var workbench=PlatformUI.getWorkbench();
        var page=workbench.showPerspective("ru.litonchik.edt.themes.probe.gallery",workbench.getActiveWorkbenchWindow());
        page.resetPerspective(); page.setEditorAreaVisible(true);
        var workbenchShell=workbench.getActiveWorkbenchWindow().getShell();
        workbenchShell.setMinimized(false); workbenchShell.setMaximized(false); workbenchShell.setSize(900,640);
        var monitorArea=workbenchShell.getMonitor().getClientArea(); workbenchShell.setLocation(monitorArea.x,monitorArea.y);
        workbenchShell.forceActive(); workbenchShell.layout(true,true); pump(500);
        try(var coordinator=ThemeStartup.coordinator()) {
            coordinator.start();
            if(coordinator.hasSnapshot()) check(coordinator.restore(true).successful(),"surfaces initial cleanup");
            var dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); pump(250);
            check(findCombo(dialog.getShell())!=null && findData(dialog.getShell(),"palette.roles") instanceof Table,"theme preference page renders profile, font and palette controls");
            dialog.close();
            var adapter=new EdtSettingsAdapter(workbench.getService(IThemeEngine.class),workbench.getThemeManager());
            var baseline=new LinkedHashMap<SettingKey,OriginalValue>();
            for(var key:adapter.plan(PaletteCatalog.load("darcula"),true).keySet()) baseline.put(key,adapter.read(key));
            for(var key:adapter.plan(PaletteCatalog.load("github-light-default"),true).keySet()) baseline.putIfAbsent(key,adapter.read(key));
            for(var key:NativeSettingsCatalog.load().keySet()) baseline.putIfAbsent(key,adapter.read(key));
            if ("candidateallsurfaces".equals(System.getProperty("edt.probe.phase"))) {
                var standard=coordinator.paletteDrafts();
                for(String id:PaletteCatalog.ids()) standard.resetPalette(id);
                coordinator.apply("visual-studio-dark-cpp",true,standard);
                for(String id:PaletteCatalog.ids()) check(coordinator.paletteDrafts().overrides(id).isEmpty(),"standard surface baseline clears test personal variant "+id);
            } else coordinator.apply("visual-studio-dark-cpp",true);
            pump(1800);
            var bsl=tryOpenBsl(); check(bsl!=null,"surfaces BSL fixture");
            var project=ResourcesPlugin.getWorkspace().getRoot().getProject("ThemeProbe");
            // Native EDT emits NUMBER, while its QL mapper recognizes RULE_INT.
            // Preserve its ordinary-text category; LIKE exercises SL_LIKESTRING.
            String query="// Комментарий\nВЫБРАТЬ ПЕРВЫЕ 123\n    Тест.Поле КАК Текст\nИЗ Справочник.Тест КАК Тест\nГДЕ Тест.Поле ПОДОБНО \"строка\"\n    И Тест.Поле = &Параметр";
            var queryTexts=new ArrayList<StyledText>();
            for(var entry:new String[][]{{"sample.txt","org.eclipse.ui.DefaultTextEditor"},{"sample.ql","com._1c.g5.v8.dt.ql.Ql"},{"sample.qldcs","com._1c.g5.v8.dt.ql.dcs.QlDcs"}}) {
                var file=project.getFile(entry[0]);
                byte[] source=query.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                if(file.exists()) file.setContents(new ByteArrayInputStream(source),true,false,null); else file.create(new ByteArrayInputStream(source),true,null);
                file.setCharset("UTF-8",null);
                var editor=IDE.openEditor(page,file,entry[1],true);
                page.setEditorAreaVisible(true); page.activate(editor); page.bringToTop(editor);
                page.setPartState(page.getReference(editor),IWorkbenchPage.STATE_MAXIMIZED); pump(1400);
                var target=editor.getAdapter(org.eclipse.jface.text.ITextOperationTarget.class);
                check(target instanceof org.eclipse.jface.text.ITextViewer,entry[0]+" native text viewer");
                var text=((org.eclipse.jface.text.ITextViewer)target).getTextWidget();
                if(!entry[0].endsWith(".txt")) queryTexts.add(text);
                if(entry[0].equals("sample.ql")) {
                    report.append("QUERY SOURCE ").append(text.getText()).append('\n');
                    var document=((org.eclipse.xtext.ui.editor.XtextEditor)editor).getDocument();
                    document.readOnly(resource -> {
                        var provider=org.eclipse.xtext.resource.IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(resource.getURI());
                        var legacy=provider.get(org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator.class);
                        var modern=provider.get(org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator.class);
                        report.append("QUERY CALCULATORS legacy=").append(legacy==null?null:legacy.getClass().getName()).append(" modern=").append(modern==null?null:modern.getClass().getName()).append(" language=").append(resource.getLanguageName()).append('\n');
                        int number=document.get().indexOf("123");
                        if(legacy!=null) legacy.provideHighlightingFor(resource,(offset,length,ids) -> {if(offset<=number && offset+length>number) report.append("QUERY NUMBER SEMANTIC ").append(offset).append('/').append(length).append(' ').append(Arrays.toString(ids)).append('\n');});
                        for(var leaf:resource.getParseResult().getRootNode().getLeafNodes()) if(leaf.getOffset()<=number && leaf.getEndOffset()>number)
                            report.append("QUERY NUMBER NODE ").append(leaf.getText()).append(" grammar=").append(leaf.getGrammarElement() instanceof org.eclipse.xtext.RuleCall call ? call.getRule().getName() : leaf.getGrammarElement()).append('\n');
                        return null;
                    });
                    observeText("query initial number",text,"123");
                    observeText("query initial string",text,"строка");
                    int offset=document.get().indexOf("123");
                    for(String category:document.getPositionCategories()) for(var position:document.getPositions(category))
                        if(position instanceof org.eclipse.xtext.ui.editor.syntaxcoloring.AttributedPosition attributed && position.overlapsWith(offset,1))
                            report.append("QUERY NUMBER PRESENTED ").append(attributed.createStyleRange()).append('\n');
                }
                observeText(entry[0],text,"ВЫБРАТЬ"); captureControl(text,"surface-"+entry[0]);
            }
            Shell shell=new Shell(workbench.getActiveWorkbenchWindow().getShell(),SWT.SHELL_TRIM);
            shell.setText("EDT query constructor text controls — isolated test"); shell.setLayout(new org.eclipse.swt.layout.GridLayout(2,true));
            try {
                for(boolean dcs:new boolean[]{false,true}) {
                    Composite parent=new Composite(shell,SWT.NONE); parent.setLayout(new org.eclipse.swt.layout.FillLayout());
                    parent.setLayoutData(new org.eclipse.swt.layout.GridData(SWT.FILL,SWT.FILL,true,true));
                    var editor=new com._1c.g5.v8.dt.qw.ui.ql.QlEditor(project,parent,com._1c.g5.v8.dt.qw.ui.utils.ExpressionEditingType.NONE,dcs,false);
                    editor.setFullText(query);
                }
                Rectangle monitor=workbench.getActiveWorkbenchWindow().getShell().getMonitor().getClientArea();
                shell.setSize(Math.min(1100,monitor.width),Math.min(550,monitor.height)); shell.setLocation(monitor.x,monitor.y); shell.open(); pump(1800);
                List<StyledText> texts=new ArrayList<>(); collectText(shell,texts);
                check(texts.size()==2,"query constructor QL and DCS real embedded text controls");
                for(int i=0;i<texts.size();i++) observeText("constructor-"+i,texts.get(i),"ВЫБРАТЬ");
                captureSurfaceShell(shell,"surface-query-constructor");
                queryTexts.addAll(texts);
                var queryOriginals = new java.util.IdentityHashMap<StyledText,String>();
                for(var queryText:queryTexts) queryOriginals.put(queryText,queryText.getText());
                for(String id:PaletteCatalog.ids()) {
                    if ("candidatesurfaces".equals(System.getProperty("edt.probe.phase")) && !id.equals(candidateId())) continue;
                    if ("lightsurfaces".equals(System.getProperty("edt.probe.phase")) && PaletteCatalog.mode(id) != ThemeMode.LIGHT) continue;
                    if ("candidatesurfaces".equals(System.getProperty("edt.probe.phase"))) {
                        for(var entry:new SnapshotPreferences().load().lastApplied().entrySet()) if(!entry.getValue().equals(adapter.read(entry.getKey())))
                            report.append("OBS changed before candidate apply ").append(entry.getKey()).append(" expected=").append(entry.getValue()).append(" actual=").append(adapter.read(entry.getKey())).append('\n');
                    }
                    coordinator.apply(id,true); pump(900);
                    if ("candidatesurfaces".equals(System.getProperty("edt.probe.phase"))) {
                        report.append("OBS candidate state ").append(coordinator.activeProfile()).append(" native=").append(workbench.getService(IThemeEngine.class).getActiveTheme().getId()).append('\n');
                        for(var key:new SnapshotPreferences().load().surrendered()) report.append("OBS candidate surrendered ").append(key).append(" actual=").append(adapter.read(key)).append('\n');
                    }
                    var palette=PaletteCatalog.load(id);
                    long settled=System.nanoTime()+5_000_000_000L;
                    while(System.nanoTime()<settled && queryTexts.stream().anyMatch(t -> !rgb(palette,"editor.background").equals(t.getBackground().getRGB()) || !rgb(palette,"text").equals(t.getForeground().getRGB()) || !rgb(palette,"selection.background").equals(t.getSelectionBackground().getRGB()) || !rgb(palette,"selection.text").equals(t.getSelectionForeground().getRGB()))) pump(100);
                    for(int i=0;i<queryTexts.size();i++) observeText("candidate settled query "+i,queryTexts.get(i),"ВЫБРАТЬ");
                    if(id.equals(lightProfile)) captureSurfaceShell(shell,"surface-light-query-constructor");
                    for(int i=0;i<queryTexts.size();i++) {
                        StyledText text=queryTexts.get(i);
                        check(rgb(palette,"editor.background").equals(text.getBackground().getRGB()),id+" query surface "+i+" background");
                        check(rgb(palette,"text").equals(text.getForeground().getRGB()),id+" query surface "+i+" foreground");
                        check(rgb(palette,"selection.background").equals(text.getSelectionBackground().getRGB()),id+" query surface "+i+" selection background expected="+rgb(palette,"selection.background")+" actual="+text.getSelectionBackground().getRGB());
                        check(rgb(palette,"selection.text").equals(text.getSelectionForeground().getRGB()),id+" query surface "+i+" selection foreground expected="+rgb(palette,"selection.text")+" actual="+text.getSelectionForeground().getRGB());
                        check(rgb(palette,"keyword").equals(glyph(text,"ВЫБРАТЬ")),id+" query surface "+i+" keyword");
                        check(rgb(palette,"text").equals(glyph(text,"123")),id+" query surface "+i+" native NUMBER text role expected="+rgb(palette,"text")+" actual="+glyph(text,"123"));
                        check(rgb(palette,"string").equals(glyph(text,"строка")),id+" query surface "+i+" string expected="+rgb(palette,"string")+" actual="+glyph(text,"строка"));
                        check(rgb(palette,"comment").equals(glyph(text,"Комментарий")),id+" query surface "+i+" comment expected="+rgb(palette,"comment")+" actual="+glyph(text,"Комментарий"));
                        check(rgb(palette,"directive").equals(glyph(text,"Параметр")),id+" query surface "+i+" parameter expected="+rgb(palette,"directive")+" actual="+glyph(text,"Параметр"));
                        check(rgb(palette,"text").equals(glyph(text,"Тест")),id+" query surface "+i+" identifier expected="+rgb(palette,"text")+" actual="+glyph(text,"Тест"));
                        check(queryOriginals.get(text).equals(text.getText()),id+" query surface "+i+" source unchanged");
                    }
                }
            } finally { shell.dispose(); }
            coordinator.apply("visual-studio-dark-cpp",true); pump(900);
            var descriptors=Platform.getExtensionRegistry().getConfigurationElementsFor("com._1c.g5.v8.dt.compare.ui.textMergeViewer");
            com._1c.g5.v8.dt.compare.ui.mergeviewer.IThreeSideTextMergeViewerCreator creator=null;
            for(var descriptor:descriptors) if("DefaultBslThreeSideTextMergeViewerCreator".equals(descriptor.getAttribute("id")))
                creator=(com._1c.g5.v8.dt.compare.ui.mergeviewer.IThreeSideTextMergeViewerCreator)descriptor.createExecutableExtension("class");
            check(creator!=null,"registered EDT BSL merge viewer factory");
            var factory=creator;
            String source="Процедура ПроверитьПалитру()\n    Сообщить(123);\nКонецПроцедуры\n";
            var input=new com._1c.g5.v8.dt.compare.ui.mergeviewer.ThreeSideTextMergeInput(
                new MemoryElement("left.bsl",source),new MemoryElement("right.bsl",source.replace("123","456")),
                new MemoryElement("base.bsl",source.replace("123","100")),new MemoryElement("result.bsl",source),"DefaultBslThreeSideTextMergeViewerCreator");
            java.util.function.Supplier<org.eclipse.compare.CompareEditorInput> compareFactory = () -> new org.eclipse.compare.CompareEditorInput(new org.eclipse.compare.CompareConfiguration()) {
                protected Object prepareInput(IProgressMonitor monitor) { return input; }
                @Override public Control createContents(Composite parent) {
                    getCompareConfiguration().setContainer(getContainer());
                    var site = getContainer().getWorkbenchPart().getSite();
                    check(site != null,"merge uses real editor site for native Xtext panes");
                    var viewer=factory.createViewer(parent,getCompareConfiguration(),site);
                    viewer.setInput(input); return viewer.getControl();
                }
            };
            org.eclipse.compare.CompareUI.openCompareEditor(compareFactory.get()); pump(2500);
            List<StyledText> texts=new ArrayList<>(); collectText(workbench.getActiveWorkbenchWindow().getShell(),texts);
            int count=0;
            for(var text:texts) if(text.getText().contains("Сообщить")) {
                observeText("EDT merge-"+count,text,"Процедура");
                var palette=PaletteCatalog.load("visual-studio-dark-cpp");
                check(rgb(palette,"editor.background").equals(text.getBackground().getRGB()),"EDT merge pane "+count+" palette background");
                check(rgb(palette,"keyword").equals(glyph(text,"Процедура")),"EDT merge pane "+count+" palette keyword");
                count++;
            }
            check(count>=3,"EDT comparison and merge shows at least three real BSL panes");
            page.setPartState(page.getActivePartReference(),IWorkbenchPage.STATE_MAXIMIZED);
            captureSurfaceShell(workbench.getActiveWorkbenchWindow().getShell(),"surface-edt-merge");
            var mergeOriginals = new java.util.IdentityHashMap<StyledText,String>();
            for(var text:texts) if(!text.isDisposed()) mergeOriginals.put(text,text.getText());
            coordinator.apply(lightProfile,true); pump(1400);
            for(var text:texts) if(!text.isDisposed() && text.getText().contains("Сообщить")) {
                observeText("existing merge after mode switch",text,"Процедура");
                check(mergeOriginals.get(text).equals(text.getText()),"existing merge source unchanged by mode switch");
            }
            // EDT caches existing merge panes. Reopening is required for their new palette.
            page.closeAllEditors(false);
            org.eclipse.compare.CompareUI.openCompareEditor(compareFactory.get()); pump(2500);
            texts=new ArrayList<>(); collectText(workbench.getActiveWorkbenchWindow().getShell(),texts);
            int lightPanes=0;
            for(var text:texts) if(!text.isDisposed() && text.getText().contains("Сообщить")) {
                var palette=PaletteCatalog.load(lightProfile);
                check(rgb(palette,"editor.background").equals(text.getBackground().getRGB()),"Light merge pane "+lightPanes+" background");
                observeText("Light merge-"+lightPanes,text,"Процедура");
                check(rgb(palette,"keyword").equals(glyph(text,"Процедура")),"Light merge pane "+lightPanes+" keyword");
                check(text.getText().contains("Процедура ПроверитьПалитру()"),"reopened Light merge pane "+lightPanes+" shows expected fixture");
                lightPanes++;
            }
            check(lightPanes>=3,"Light merge shows three BSL panes");
            captureSurfaceShell(workbench.getActiveWorkbenchWindow().getShell(),"surface-light-edt-merge");
            page.closeAllEditors(false);
            check(coordinator.restore(true).successful(),"surfaces restore");
            for(var entry:baseline.entrySet()) check(entry.getValue().equals(adapter.read(entry.getKey())),"surfaces restores exact original "+entry.getKey());
        }
    }
    private void runGallery() throws Exception {
        var coordinator=ThemeStartup.coordinator();
        for(String id:PaletteCatalog.ids()) check(coordinator.paletteDrafts().overrides(id).isEmpty(),"gallery starts without personal colors: "+id);
        if("galleryprepare".equals(System.getProperty("edt.probe.phase"))) {
            check(!coordinator.hasSnapshot(),"gallery preparation starts from restored test settings");
            coordinator.apply("one-dark-pro",true);
            check(ProfileState.DARK.equals(PlatformUI.getWorkbench().getService(IThemeEngine.class).getActiveTheme().getId()),"gallery prepares native Dark before restart");
            return;
        }
        check("one-dark-pro".equals(coordinator.activeProfile()),"gallery native Dark persists across JVM restart");
        var workbench=PlatformUI.getWorkbench();
        var shell=workbench.getActiveWorkbenchWindow().getShell();
        var page=workbench.showPerspective("ru.litonchik.edt.themes.probe.gallery",workbench.getActiveWorkbenchWindow());
        page.resetPerspective();
        shell.setMaximized(false); shell.setSize(900,640);
        var monitor=shell.getMonitor().getClientArea();
        shell.setLocation(monitor.x+Math.max(0,(monitor.width-shell.getSize().x)/2),monitor.y+Math.max(0,(monitor.height-shell.getSize().y)/2));
        shell.forceActive();
        var baseline=new LinkedHashMap<>(new SnapshotPreferences().load().original());
        var adapter=new EdtSettingsAdapter(workbench.getService(IThemeEngine.class),workbench.getThemeManager());
        Point imageSize=null;
        try {
            for(String id:PaletteCatalog.ids()) {

                if(PaletteCatalog.mode(id) != ThemeMode.DARK || id.equals("darcula") || id.equals("monokai")) continue;
                page.closeAllEditors(false);
                coordinator.apply(id,true);
                var editor=tryOpenBsl(); check(editor!=null,id+" gallery opens real BSL editor");
                editor.setTopIndex(0); editor.setSelection(0);
                shell.layout(true,true); shell.redraw();
                long end=System.nanoTime()+1_500_000_000L;
                while(System.nanoTime()<end) { if(!shell.getDisplay().readAndDispatch()) Thread.sleep(15); }
                shell.getDisplay().update();
                var palette=PaletteCatalog.load(id);
                check(rgb(palette,"editor.background").equals(editor.getBackground().getRGB()),id+" gallery default BSL background");
                for(String[] token:new String[][]{{"Функция","keyword"},{"123.45","number"},{"Строка с кириллицей","string"}}) {
                    var style=editor.getStyleRangeAtOffset(editor.getText().indexOf(token[0]));
                    check(style!=null && style.foreground!=null && rgb(palette,token[1]).equals(style.foreground.getRGB()),id+" gallery default "+token[1]+" glyph");
                }
                // A Shell GC uses client coordinates; getSize() includes non-client borders
                // and produced white bands in the old diagnostic window captures.
                Rectangle area=shell.getClientArea();
                Point origin=shell.toDisplay(area.x,area.y);
                check(shell.getMonitor().getClientArea().contains(origin) && shell.getMonitor().getClientArea().contains(origin.x+area.width-1,origin.y+area.height-1),id+" gallery client fully visible");
                if(imageSize==null) imageSize=new Point(area.width,area.height);
                check(imageSize.equals(new Point(area.width,area.height)),id+" gallery consistent size "+imageSize);
                Image image=new Image(shell.getDisplay(),area.width,area.height); GC gc=new GC(shell);
                try {
                    gc.copyArea(image,area.x,area.y);
                    var data=image.getImageData();
                    ImageLoader loader=new ImageLoader(); loader.data=new ImageData[]{data};
                    loader.save(System.getProperty("edt.probe.images")+"/gallery-"+id+".png",SWT.IMAGE_PNG);
                    int whiteBottom=0,whiteRight=0;
                    for(int x=0;x<data.width;x++) if(data.palette.getRGB(data.getPixel(x,data.height-1)).equals(new RGB(255,255,255))) whiteBottom++;
                    for(int y=0;y<data.height;y++) if(data.palette.getRGB(data.getPixel(data.width-1,y)).equals(new RGB(255,255,255))) whiteRight++;
                    check(whiteBottom<data.width/20 && whiteRight<data.height/20,id+" gallery has no white bottom/right bands");
                } finally { gc.dispose(); image.dispose(); }
            }
        } finally {
            check(coordinator.restore(true).successful(),"gallery restores original test settings");
            for(var entry:baseline.entrySet()) check(entry.getValue().equals(adapter.read(entry.getKey())),"gallery restored "+entry.getKey());
            check(coordinator.activeProfile()==null && !coordinator.hasSnapshot(),"gallery clears active profile and journal");
        }
    }
    private void runCleanup() throws Exception {
        var coordinator=ThemeStartup.coordinator();
        var originals=new SnapshotPreferences().load().original();
        var adapter=new EdtSettingsAdapter(PlatformUI.getWorkbench().getService(IThemeEngine.class),PlatformUI.getWorkbench().getThemeManager());
        check(coordinator.restore(true).successful(),"cleanup restores test settings");
        for(var entry:originals.entrySet()) check(entry.getValue().equals(adapter.read(entry.getKey())),"cleanup restored "+entry.getKey());
        var fonts=new CodeFontSettings(); if(fonts.hasOriginal()) fonts.restore();
        check(!fonts.hasOriginal(),"cleanup font journal cleared");
        var drafts=coordinator.paletteDrafts();
        for(String id:PaletteCatalog.ids()) drafts.resetPalette(id);
        coordinator.apply(null,true,drafts);
        for(String id:PaletteCatalog.ids()) check(coordinator.paletteDrafts().overrides(id).isEmpty(),"cleanup cleared test personal keys "+id);
        check(new SnapshotPreferences().load().original().isEmpty(),"cleanup journal empty");
        check(coordinator.activeProfile()==null,"cleanup active profile cleared");
    }
    private void runWinterSeparatorChecks() throws Exception {
        String id="winter-is-coming-dark-blue";
        var coordinator=ThemeStartup.coordinator();
        var engine=PlatformUI.getWorkbench().getService(IThemeEngine.class);
        var adapter=new EdtSettingsAdapter(engine,PlatformUI.getWorkbench().getThemeManager());
        var shell=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        var page=PlatformUI.getWorkbench().showPerspective("ru.litonchik.edt.themes.probe.separators",PlatformUI.getWorkbench().getActiveWorkbenchWindow());
        page.resetPerspective();
        page.showView("org.eclipse.ui.navigator.ProjectExplorer");
        page.showView("org.eclipse.ui.views.PropertySheet");
        if(System.getProperty("edt.probe.phase").endsWith("restart")) {
            check(id.equals(coordinator.activeProfile()),"Winter remains selected after restart");
            check("#ABCDEF".equals(coordinator.paletteDrafts().resolve(id).color("keyword")),"Winter personal keyword survives restart");
            check("#778899".equals(coordinator.paletteDrafts().resolve("github-dark-default").color("comment")),"GitHub personal color remains independent");
            checkProfileGlyph(id,new RGB(1,22,39),new RGB(171,205,239));
            checkWorkbenchSeparators(shell,new RGB(14,41,63),"restart");
            var originals=new SnapshotPreferences().load().original();
            check(coordinator.restore(true).successful(),"restore Winter separator colors");
            for(var entry:originals.entrySet()) check(entry.getValue().equals(adapter.read(entry.getKey())),"restored "+entry.getKey());
            check("#ABCDEF".equals(coordinator.paletteDrafts().resolve(id).color("keyword")),"restore retains personal colors");
            var cleanup=coordinator.paletteDrafts(); for(String profile:PaletteCatalog.ids()) cleanup.resetPalette(profile);
            coordinator.apply(null,true,cleanup);
            for(String profile:PaletteCatalog.ids()) check(coordinator.paletteDrafts().overrides(profile).isEmpty(),"cleanup "+profile);
            check(!coordinator.hasSnapshot() && coordinator.activeProfile()==null,"cleanup journal and profile");
            return;
        }
        if(coordinator.hasSnapshot()) check(coordinator.restore(true).successful(),"restore previous test state");
        var drafts=coordinator.paletteDrafts(); for(String profile:PaletteCatalog.ids()) drafts.resetPalette(profile);
        coordinator.apply(null,true,drafts);
        String font=InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").get(JFaceResources.TEXT_FONT,null);
        // Reproduce 0.1.9 through a test-only personal panel value; keep its ZIP untouched.
        drafts=coordinator.paletteDrafts(); drafts.edit(id,"panel","#011627"); coordinator.apply(id,true,drafts);
        check(tryOpenBsl()!=null,"open real BSL for divider comparison");
        checkWorkbenchSeparators(shell,new RGB(1,22,39),"before");
        var previous=new LinkedHashMap<SettingKey,OriginalValue>();
        for(var key:adapter.plan(PaletteCatalog.load(id),true).keySet()) previous.put(key,adapter.read(key));
        // Emulate an active 0.1.9 default (managed old RGB, no personal panel key).
        var prefs=InstanceScope.INSTANCE.getNode(SnapshotPreferences.PLUGIN_ID);
        prefs.remove("palette."+id+".panel"); prefs.flush();
        coordinator.apply(id,true);
        var panelKeys=SettingCatalog.load().roleTargets(Set.of("panel"),true,"org.eclipse.ui.defaultTheme");
        for(var entry:previous.entrySet()) if(!panelKeys.contains(entry.getKey())) check(entry.getValue().equals(adapter.read(entry.getKey())),"panel fix preserves "+entry.getKey());
        checkProfileGlyph(id,new RGB(1,22,39),new RGB(0,191,249));
        checkWorkbenchSeparators(shell,new RGB(14,41,63),"after");
        check(Objects.equals(font,InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").get(JFaceResources.TEXT_FONT,null)),"panel fix keeps code font");
        drafts=coordinator.paletteDrafts(); drafts.edit(id,"keyword","#ABCDEF"); drafts.edit("github-dark-default","comment","#778899");
        coordinator.apply(id,true,drafts);
        check("#0E293F".equals(coordinator.paletteDrafts().resolve(id).color("panel")),"default distinct panel retained with personal keyword");
    }
    private void checkWorkbenchSeparators(Shell shell,RGB expected,String stage) throws Exception {
        shell.layout(true,true); shell.getDisplay().update();
        long end=System.nanoTime()+600_000_000L;
        while(System.nanoTime()<end) { if(!shell.getDisplay().readAndDispatch()) Thread.sleep(15); }
        var app=PlatformUI.getWorkbench().getService(MApplication.class);
        check(app!=null,"public workbench application model available");
        var containers=new ArrayList<MPartSashContainer>(); collectSashContainers(app,containers);
        report.append("CONTAINERS ").append(containers.size()).append('\n');
        int dividers=0;
        boolean horizontal=false,vertical=false;
        for(var model:containers) {
            report.append("CONTAINER ").append(model.getElementId()).append(" widget=").append(model.getWidget()).append('\n');
            if(!(model.getWidget() instanceof Composite container) || !container.isVisible()) continue;
            var rectangles=new ArrayList<Rectangle>();
            for(Control control:container.getChildren()) if(control.isVisible() && control.getSize().x>0 && control.getSize().y>0) {
                Point origin=container.toControl(control.toDisplay(0,0));
                rectangles.add(new Rectangle(origin.x,origin.y,control.getSize().x,control.getSize().y));
            }
            report.append("RECTANGLES ").append(rectangles).append('\n');
            for(int i=0;i<rectangles.size();i++) for(int j=0;j<rectangles.size();j++) {
                Rectangle a=rectangles.get(i),b=rectangles.get(j);
                boolean alongX=a.x+a.width<b.x && Math.max(a.y,b.y)<Math.min(a.y+a.height,b.y+b.height);
                boolean alongY=a.y+a.height<b.y && Math.max(a.x,b.x)<Math.min(a.x+a.width,b.x+b.width);
                if(!alongX && !alongY) continue;
                int start=alongX ? a.x+a.width : a.y+a.height, stop=alongX ? b.x : b.y;
                int crossStart=alongX ? Math.max(a.y,b.y) : Math.max(a.x,b.x);
                int crossStop=alongX ? Math.min(a.y+a.height,b.y+b.height) : Math.min(a.x+a.width,b.x+b.width);
                if(stop<=start || crossStop<=crossStart) continue;
                int x=alongX ? (start+stop)/2 : (crossStart+crossStop)/2;
                int y=alongX ? (crossStart+crossStop)/2 : (start+stop)/2;
                if(rectangles.stream().anyMatch(r -> r.contains(x,y))) continue;
                Image image=new Image(container.getDisplay(),1,1); GC gc=new GC(container);
                try { gc.copyArea(image,x,y); var data=image.getImageData(); RGB pixel=data.palette.getRGB(data.getPixel(0,0));
                    report.append("DIVIDER ").append(stage).append(alongX ? " vertical " : " horizontal ").append("pixel=").append(pixel).append(" background=").append(container.getBackground().getRGB()).append('\n');
                    check(expected.equals(pixel),stage+" rendered divider pixel "+pixel+" expected "+expected);
                } finally { gc.dispose(); image.dispose(); }
                dividers++; vertical|=alongX; horizontal|=!alongX;
            }
        }
        captureControl(shell,stage.startsWith("night-owl") || stage.startsWith("ayu-dark") || stage.startsWith("shades-of-purple") || stage.startsWith("noctis-minimus") || stage.startsWith("visual-studio-dark-cpp") ? stage+"-workbench" : "winter-separators-"+stage);
        check(dividers>=2,"visible workbench dividers: "+dividers);
        check(horizontal && vertical,"both horizontal and vertical workbench dividers checked");
    }
    private void collectSashContainers(MElementContainer<?> parent,java.util.List<MPartSashContainer> result) {
        if(parent instanceof MPartSashContainer container) result.add(container);
        for(var child:parent.getChildren()) {
            if(child instanceof MElementContainer<?> container) collectSashContainers(container,result);
        }
    }
    private void runProfileChecks() throws Exception {
        String phase=System.getProperty("edt.probe.phase");
        boolean isDracula=phase.startsWith("dracula");
        boolean isGitHub=phase.startsWith("github");
        boolean isWinter=phase.startsWith("winter");
        boolean isNightOwl=phase.startsWith("nightowl");
        boolean isAyu=phase.startsWith("ayu");
        boolean isShades=phase.startsWith("shades");
        boolean isNoctis=phase.startsWith("noctis");
        boolean isVisualStudio=phase.startsWith("visualstudio");
        String selectedId=isVisualStudio ? "visual-studio-dark-cpp" : isNoctis ? "noctis-minimus" : isShades ? "shades-of-purple" : isAyu ? "ayu-dark" : isNightOwl ? "night-owl" : isWinter ? "winter-is-coming-dark-blue" : isGitHub ? "github-dark-default" : isDracula ? "dracula" : "one-dark-pro";
        String profileLabel=isVisualStudio ? "Visual Studio Dark (C/C++)" : isNoctis ? "Noctis Minimus" : isShades ? "Shades of Purple" : isAyu ? "Ayu Dark (Bordered)" : isNightOwl ? "Night Owl" : isWinter ? "Winter is Coming (Dark Blue)" : isGitHub ? "GitHub Dark Default" : isDracula ? "Dracula" : "One Dark Pro";
        if(isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) {
            var window=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            var page=PlatformUI.getWorkbench().showPerspective("ru.litonchik.edt.themes.probe.separators",window);
            page.resetPerspective();
            page.showView("org.eclipse.ui.navigator.ProjectExplorer");
            page.showView("org.eclipse.ui.views.PropertySheet");
        }
        int profileIndex=PaletteCatalog.ids().indexOf(selectedId)+1;
        RGB baseBackground=isVisualStudio ? new RGB(30,30,30) : isNoctis ? new RGB(27,41,50) : isShades ? new RGB(45,43,85) : isAyu ? new RGB(16,20,28) : isNightOwl ? new RGB(1,22,39) : isWinter ? new RGB(1,22,39) : isGitHub ? new RGB(13,17,23) : isDracula ? new RGB(40,42,54) : new RGB(40,44,52);
        RGB baseKeyword=isVisualStudio ? new RGB(86,156,214) : isNoctis ? new RGB(200,141,162) : isShades ? new RGB(255,157,0) : isAyu ? new RGB(255,143,64) : isNightOwl ? new RGB(199,146,234) : isWinter ? new RGB(0,191,249) : isGitHub ? new RGB(255,123,114) : isDracula ? new RGB(255,121,198) : new RGB(198,120,221);
        String defaultKeyword=isVisualStudio ? "86,156,214" : isNoctis ? "200,141,162" : isShades ? "255,157,0" : isAyu ? "255,143,64" : isNightOwl ? "199,146,234" : isWinter ? "0,191,249" : isGitHub ? "255,123,114" : isDracula ? "255,121,198" : "198,120,221";
        String defaultComment=isVisualStudio ? "87,166,74" : isNoctis ? "94,120,135" : isShades ? "179,98,255" : isAyu ? "90,102,115" : isNightOwl ? "99,119,119" : isWinter ? "153,153,153" : isGitHub ? "139,148,158" : isDracula ? "98,114,164" : "127,132,142";
        var coordinator = ThemeStartup.coordinator();
        var engine = PlatformUI.getWorkbench().getService(IThemeEngine.class);
        var adapter = new EdtSettingsAdapter(engine,PlatformUI.getWorkbench().getThemeManager());
        var prefs = InstanceScope.INSTANCE.getNode(SnapshotPreferences.PLUGIN_ID);
        var keyword = SettingCatalog.load().roleTargets(Set.of("keyword"),true,"org.eclipse.ui.defaultTheme").iterator().next();
        var comment = SettingCatalog.load().roleTargets(Set.of("comment"),true,"org.eclipse.ui.defaultTheme").iterator().next();
        if (phase.endsWith("restart")) {
            check(selectedId.equals(coordinator.activeProfile()),"selected profile saved ID survives JVM restart");
            check("#ABCDEF".equals(coordinator.paletteDrafts().resolve(selectedId).color("keyword")),"selected profile personal palette survives JVM restart");
            check("#123456".equals(coordinator.paletteDrafts().resolve("darcula").color("keyword")),"Darcula override survives selected profile-profile restart");
            check("#112233".equals(coordinator.paletteDrafts().resolve("monokai").color("number")),"Monokai override survives selected profile-profile restart");
            if(isDracula || isGitHub || isWinter || isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) check("#445566".equals(coordinator.paletteDrafts().resolve("one-dark-pro").color("string")),"One Dark Pro personal string survives selected profile restart");
            if(isGitHub || isWinter || isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) check("#667788".equals(coordinator.paletteDrafts().resolve("dracula").color("comment")),"Dracula personal comment survives selected profile restart");
            if(isWinter || isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) check("#778899".equals(coordinator.paletteDrafts().resolve("github-dark-default").color("comment")),"GitHub personal comment survives selected profile restart");
            if(isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) check("#8899AA".equals(coordinator.paletteDrafts().resolve("winter-is-coming-dark-blue").color("number")),"Winter personal number survives selected profile restart");
            if(isAyu || isShades || isNoctis || isVisualStudio) check("#99AABB".equals(coordinator.paletteDrafts().resolve("night-owl").color("comment")),"Night Owl personal comment survives selected profile restart");
            if(isShades || isNoctis || isVisualStudio) check("#AABBCC".equals(coordinator.paletteDrafts().resolve("ayu-dark").color("string")),"Ayu personal string survives selected profile restart");
            if(isNoctis || isVisualStudio) check("#BBCCDD".equals(coordinator.paletteDrafts().resolve("shades-of-purple").color("warning")),"Shades personal warning survives selected profile restart");
            if(isVisualStudio) check("#CCDDEE".equals(coordinator.paletteDrafts().resolve("noctis-minimus").color("error")),"Noctis personal error survives Visual Studio restart");
            var baseline = new SnapshotPreferences().load().original();
            checkProfileGlyph(selectedId,baseBackground,new RGB(171,205,239));
            if(isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) checkWorkbenchSeparators(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),isVisualStudio ? new RGB(48,48,49) : isNoctis ? new RGB(16,18,19) : isShades ? new RGB(34,34,68) : isAyu ? new RGB(27,31,41) : new RGB(14,41,63),selectedId+"-restart");
            var dialog = fontDialog(); dialog.setBlockOnOpen(false); dialog.open();
            check(findCombo(dialog.getShell()).getSelectionIndex()==profileIndex,"preference page restores selected profile profile instead of native/Darcula");
            check(profileLabel.equals(findCombo(dialog.getShell()).getText()),"selected profile profile name restored on preference page");
            captureControl((Table)findData(dialog.getShell(),"palette.roles"),selectedId+"-personal-roles"); dialog.close();
            check(coordinator.restore(true).successful(),"selected profile profile restores originals after restart");
            for(var entry:baseline.entrySet()) check(entry.getValue().equals(adapter.read(entry.getKey())),"selected profile restart restored "+entry.getKey());
            check("#ABCDEF".equals(prefs.get("palette."+selectedId+".keyword",null)),"restore retains selected profile personal key");
            var drafts=coordinator.paletteDrafts(); for(String id:PaletteCatalog.ids()) drafts.resetPalette(id);
            coordinator.apply(null,true,drafts);
            for(String id:PaletteCatalog.ids()) check(coordinator.paletteDrafts().overrides(id).isEmpty(),"cleanup personal keys "+id);
            check(new SnapshotPreferences().load().original().isEmpty(),"selected profile-profile journal empty after cleanup");
            return;
        }
        if(coordinator.hasSnapshot()) check(coordinator.restore(true).successful(),"restore previous probe before selected profile-profile baseline");
        var cleanup=coordinator.paletteDrafts(); for(String id:PaletteCatalog.ids()) cleanup.resetPalette(id);
        coordinator.apply(null,true,cleanup);
        var baseline=new LinkedHashMap<SettingKey,OriginalValue>();
        for(var key:adapter.plan(PaletteCatalog.load(selectedId),true).keySet()) baseline.put(key,adapter.read(key));
        for(var key:NativeSettingsCatalog.load().keySet()) baseline.putIfAbsent(key,adapter.read(key));
        String originalFont=InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").get(JFaceResources.TEXT_FONT,null);
        var dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); selectProfile(dialog,profileIndex);
        check(profileLabel.equals(findCombo(dialog.getShell()).getText()),"selected profile profile available by its own name");
        checkPreview(findCodePreview(dialog.getShell()),selectedId);
        editRole(dialog,"keyword","#654321"); selectProfile(dialog,1); editRole(dialog,"keyword","#123456");
        selectProfile(dialog,profileIndex); check("#654321".equals(((Text)findData(dialog.getShell(),"palette.hex")).getText()),"selected profile draft survives profile switch");
        dialog.close();
        check(prefs.get("palette."+selectedId+".keyword",null)==null && prefs.get("palette.darcula.keyword",null)==null,"Cancel discards selected profile and old profile drafts");
        for(var entry:baseline.entrySet()) check(entry.getValue().equals(adapter.read(entry.getKey())),"selected profile Cancel preserves native "+entry.getKey());
        dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); selectProfile(dialog,profileIndex);
        findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event());
        check(selectedId.equals(coordinator.activeProfile()),"Apply uses selected profile ID instead of Darcula");
        check(ProfileState.DARK.equals(engine.getActiveTheme().getId()),"selected profile profile keeps exact native Dark ID");
        for(var entry:adapter.plan(PaletteCatalog.load(selectedId),true).entrySet()) check(entry.getValue().equals(adapter.read(entry.getKey())),"selected profile profile native target "+entry.getKey());
        checkProfileGlyph(selectedId,baseBackground,baseKeyword);
        if(isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) {
            dialog.close();
            checkWorkbenchSeparators(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),isVisualStudio ? new RGB(48,48,49) : isNoctis ? new RGB(16,18,19) : isShades ? new RGB(34,34,68) : isAyu ? new RGB(27,31,41) : new RGB(14,41,63),selectedId);
            dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); selectProfile(dialog,profileIndex);
        }
        selectRole(dialog,"keyword"); captureControl((Table)findData(dialog.getShell(),"palette.roles"),selectedId+"-roles");
        captureControl(findData(dialog.getShell(),"palette.preview"),selectedId+"-interface-preview");
        var combo=findCombo(dialog.getShell()); combo.setFocus(); combo.setListVisible(true);
        var list=findFontTable(combo); check(list.getItemCount()==PaletteCatalog.profiles(ThemeMode.DARK).size()+1,"native plus all registered palettes in popup");
        check(profileLabel.equals(list.getItem(profileIndex).getText()),"popup includes selected profile label");
        list.setSelection(profileIndex); list.redraw(); list.update(); list.getShell().update();
        long deadline=System.nanoTime()+400_000_000L;
        while(System.nanoTime()<deadline) { if(!list.getDisplay().readAndDispatch()) Thread.sleep(15); }
        saveVisibleShell(list.getShell(),selectedId+"-profile-list"); checkSelectedRowContrast(list); combo.closePopup();
        editRole(dialog,"keyword","#654321"); dialog.close();
        check(OriginalValue.of(defaultKeyword).equals(adapter.read(keyword)),"Cancel after Apply retains committed selected profile palette");
        dialog=fontDialog(); selectProfile(dialog,1); editRole(dialog,"keyword","#123456");
        selectProfile(dialog,2); editRole(dialog,"number","#112233");
        if(isDracula || isGitHub || isWinter || isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) { selectProfile(dialog,PaletteCatalog.ids().indexOf("one-dark-pro")+1); editRole(dialog,"string","#445566"); }
        if(isGitHub || isWinter || isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) { selectProfile(dialog,PaletteCatalog.ids().indexOf("dracula")+1); editRole(dialog,"comment","#667788"); }
        if(isWinter || isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) { selectProfile(dialog,PaletteCatalog.ids().indexOf("github-dark-default")+1); editRole(dialog,"comment","#778899"); }
        if(isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) { selectProfile(dialog,PaletteCatalog.ids().indexOf("winter-is-coming-dark-blue")+1); editRole(dialog,"number","#8899AA"); }
        if(isAyu || isShades || isNoctis || isVisualStudio) { selectProfile(dialog,PaletteCatalog.ids().indexOf("night-owl")+1); editRole(dialog,"comment","#99AABB"); }
        if(isShades || isNoctis || isVisualStudio) { selectProfile(dialog,PaletteCatalog.ids().indexOf("ayu-dark")+1); editRole(dialog,"string","#AABBCC"); }
        if(isNoctis || isVisualStudio) { selectProfile(dialog,PaletteCatalog.ids().indexOf("shades-of-purple")+1); editRole(dialog,"warning","#BBCCDD"); }
        if(isVisualStudio) { selectProfile(dialog,PaletteCatalog.ids().indexOf("noctis-minimus")+1); editRole(dialog,"error","#CCDDEE"); }
        selectProfile(dialog,profileIndex); editRole(dialog,"keyword","#ABCDEF"); findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event());
        check("#123456".equals(prefs.get("palette.darcula.keyword",null)),"selected profile Apply saves independent Darcula draft");
        check("#112233".equals(prefs.get("palette.monokai.number",null)),"selected profile Apply saves independent Monokai draft");
        check("#ABCDEF".equals(prefs.get("palette."+selectedId+".keyword",null)),"selected profile Apply saves its own personal key");
        check(new SnapshotPreferences().load().original().keySet().stream().noneMatch(k->k.key().startsWith("palette.")),"selected profile personal keys excluded from originals");
        dialog.close();
        adapter.write(comment,OriginalValue.of("61,62,63")); coordinator.apply("darcula",true); coordinator.apply(selectedId,true);
        check(OriginalValue.of("61,62,63").equals(adapter.read(comment)),"selected profile profile switch protects external manual comment");
        dialog=fontDialog(); selectProfile(dialog,profileIndex); selectRole(dialog,"keyword");
        findButton(dialog.getShell(),"Вернуть цвет стиля").notifyListeners(SWT.Selection,new Event());
        findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event());
        check(prefs.get("palette."+selectedId+".keyword",null)==null,"selected profile role reset removes only its override");
        check(OriginalValue.of("61,62,63").equals(adapter.read(comment)),"selected profile role reset preserves unrelated external role");
        findButton(dialog.getShell(),"Сбросить палитру").notifyListeners(SWT.Selection,new Event()); findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event());
        check(OriginalValue.of(defaultComment).equals(adapter.read(comment)),"selected profile whole reset explicitly reclaims manual comment");
        check("#123456".equals(prefs.get("palette.darcula.keyword",null)) && "#112233".equals(prefs.get("palette.monokai.number",null)),"selected profile reset preserves both old personal profiles");
        if(isDracula || isGitHub || isWinter || isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) check("#445566".equals(prefs.get("palette.one-dark-pro.string",null)),"selected profile reset preserves One Dark Pro personal string");
        if(isGitHub || isWinter || isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) check("#667788".equals(prefs.get("palette.dracula.comment",null)),"selected profile reset preserves Dracula personal comment");
        if(isWinter || isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) check("#778899".equals(prefs.get("palette.github-dark-default.comment",null)),"selected profile reset preserves GitHub personal comment");
        if(isNightOwl || isAyu || isShades || isNoctis || isVisualStudio) check("#8899AA".equals(prefs.get("palette.winter-is-coming-dark-blue.number",null)),"selected profile reset preserves Winter personal number");
        if(isAyu || isShades || isNoctis || isVisualStudio) check("#99AABB".equals(prefs.get("palette.night-owl.comment",null)),"selected profile reset preserves Night Owl personal comment");
        if(isShades || isNoctis || isVisualStudio) check("#AABBCC".equals(prefs.get("palette.ayu-dark.string",null)),"selected profile reset preserves Ayu personal string");
        if(isNoctis || isVisualStudio) check("#BBCCDD".equals(prefs.get("palette.shades-of-purple.warning",null)),"selected profile reset preserves Shades personal warning");
        if(isVisualStudio) check("#CCDDEE".equals(prefs.get("palette.noctis-minimus.error",null)),"Visual Studio reset preserves Noctis personal error");
        var checkbox=findButton(dialog.getShell(),"Применять палитру BSL"); checkbox.setSelection(false); checkbox.notifyListeners(SWT.Selection,new Event());
        editRole(dialog,"keyword","#ABCDEF"); findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event());
        check(baseline.get(keyword).equals(adapter.read(keyword)),"selected profile BSL off restores baseline keyword");
        check("#ABCDEF".equals(prefs.get("palette."+selectedId+".keyword",null)),"selected profile BSL off still saves personal keyword");
        checkbox.setSelection(true); checkbox.notifyListeners(SWT.Selection,new Event());
        findButton(dialog.getShell(),JFaceResources.getString("PreferencesDialog.okButtonLabel")).notifyListeners(SWT.Selection,new Event());
        check(OriginalValue.of("171,205,239").equals(adapter.read(keyword)),"selected profile OK commits personal BSL keyword");
        check(Objects.equals(originalFont,InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").get(JFaceResources.TEXT_FONT,null)),"selected profile palette preserves independent code font");
    }
    private StyledText findCodePreview(Composite parent) {
        Group group=findFontGroup(parent);
        for(Control child:group.getChildren()) if(child instanceof StyledText text) return text;
        throw new AssertionError("No code preview");
    }
    private void checkProfileGlyph(String profileId,RGB background,RGB expected) throws Exception {
        StyledText editor=tryOpenBsl(); check(editor!=null,"selected profile profile real BSL editor opens");
        long deadline=System.nanoTime()+2_000_000_000L;
        while(System.nanoTime()<deadline) { if(!editor.getDisplay().readAndDispatch()) Thread.sleep(15); }
        check(background.equals(editor.getBackground().getRGB()),"selected profile profile real BSL background");
        var style=editor.getStyleRangeAtOffset(editor.getText().indexOf("Функция"));
        check(style!=null && style.foreground!=null && expected.equals(style.foreground.getRGB()),"selected profile profile real BSL keyword glyph "+expected);
        RGB number=profileId.equals("visual-studio-dark-cpp") ? new RGB(181,206,168) : profileId.equals("noctis-minimus") ? new RGB(142,135,197) : profileId.equals("shades-of-purple") ? new RGB(255,98,140) : profileId.equals("ayu-dark") ? new RGB(210,166,255) : profileId.equals("night-owl") ? new RGB(247,140,108) : profileId.equals("winter-is-coming-dark-blue") ? new RGB(141,236,149) : profileId.equals("github-dark-default") ? new RGB(121,192,255) : profileId.equals("dracula") ? new RGB(189,147,249) : new RGB(209,154,102);
        RGB string=profileId.equals("visual-studio-dark-cpp") ? new RGB(206,145,120) : profileId.equals("noctis-minimus") ? new RGB(114,192,159) : profileId.equals("shades-of-purple") ? new RGB(165,255,144) : profileId.equals("ayu-dark") ? new RGB(170,217,76) : profileId.equals("night-owl") ? new RGB(236,196,141) : profileId.equals("winter-is-coming-dark-blue") ? new RGB(188,240,192) : profileId.equals("github-dark-default") ? new RGB(165,214,255) : profileId.equals("dracula") ? new RGB(241,250,140) : new RGB(152,195,121);
        var numberStyle=editor.getStyleRangeAtOffset(editor.getText().indexOf("123.45"));
        check(numberStyle!=null && numberStyle.foreground!=null && number.equals(numberStyle.foreground.getRGB()),"selected profile real BSL number glyph");
        var stringStyle=editor.getStyleRangeAtOffset(editor.getText().indexOf("Строка с кириллицей"));
        check(stringStyle!=null && stringStyle.foreground!=null && string.equals(stringStyle.foreground.getRGB()),"selected profile real BSL string glyph");
        captureControl(editor,profileId+"-bsl");
    }
    private void runPaletteChecks() throws Exception {
        if ("palettelayout".equals(System.getProperty("edt.probe.phase"))) {
            var layoutCoordinator=ThemeStartup.coordinator(); layoutCoordinator.apply("darcula",true);
            var layoutDialog=fontDialog(); layoutDialog.setBlockOnOpen(false); layoutDialog.open();
            selectProfile(layoutDialog,1);
            var checkbox=findButton(layoutDialog.getShell(),"Применять палитру BSL"); checkbox.setSelection(false); checkbox.notifyListeners(SWT.Selection,new Event());
            Table table=(Table)findData(layoutDialog.getShell(),"palette.roles");
            Label usage=null; for (Control child:table.getParent().getChildren()) if (child instanceof Label label && label.getToolTipText()!=null) usage=label;
            check(usage != null,"usage explanation and target tooltip exist");
            captureControl(table,"palette-roles-before-layout");
            ImageData rows=new ImageLoader().load(System.getProperty("edt.probe.images")+"/palette-roles-before-layout.png")[0];
            Rectangle first=table.getItem(1).getBounds(0);
            boolean uniform=rows.getPixel(10,first.y+first.height-2)==rows.getPixel(165,first.y+first.height-2);
            for (TableItem item:table.getItems()) {
                selectRole(layoutDialog,(String)item.getData());
                report.append("LAYOUT ").append(item.getData()).append(" required=").append(usage.computeSize(usage.getSize().x,SWT.DEFAULT).y).append(" height=").append(usage.getSize().y).append('\n');
                check(usage.computeSize(usage.getSize().x,SWT.DEFAULT).y<=usage.getSize().y,"role explanation fits at current DPI: "+item.getData());
            }
            check(uniform,"unselected role row background fills whole cell");
            captureControl(table,"palette-roles"); captureControl(usage,"palette-usage");
            captureControl(findData(layoutDialog.getShell(),"palette.preview"),"palette-interface-preview");
            layoutDialog.close(); check(layoutCoordinator.restore(true).successful(),"layout probe restores colors"); return;
        }
        var coordinator = ThemeStartup.coordinator();
        var adapter = new EdtSettingsAdapter(PlatformUI.getWorkbench().getService(IThemeEngine.class),PlatformUI.getWorkbench().getThemeManager());
        var prefs = InstanceScope.INSTANCE.getNode(SnapshotPreferences.PLUGIN_ID);
        if ("paletterestart".equals(System.getProperty("edt.probe.phase"))) {
            check("monokai".equals(coordinator.activeProfile()),"personal profile survives JVM restart");
            check("#ABCDEF".equals(coordinator.paletteDrafts().resolve("monokai").color("keyword")),"Monokai override survives JVM restart");
            check("#123456".equals(coordinator.paletteDrafts().resolve("darcula").color("keyword")),"Darcula override survives JVM restart independently");
            var key = SettingCatalog.load().roleTargets(Set.of("keyword"),true,"org.eclipse.ui.defaultTheme").iterator().next();
            check(OriginalValue.of("171,205,239").equals(adapter.read(key)),"persisted personal keyword used by EDT after restart");
            StyledText editor=tryOpenBsl(); check(editor != null,"personal palette opens real BSL editor");
            long deadline=System.nanoTime()+2_000_000_000L;
            while (System.nanoTime()<deadline) { if (!editor.getDisplay().readAndDispatch()) Thread.sleep(15); }
            var style=editor.getStyleRangeAtOffset(editor.getText().indexOf("Функция"));
            check(style != null && style.foreground != null && new RGB(171,205,239).equals(style.foreground.getRGB()),"real BSL keyword glyph uses personal color after restart");
            check(coordinator.restore(true).successful(),"restore colors after personal restart");
            check("#ABCDEF".equals(coordinator.paletteDrafts().resolve("monokai").color("keyword")),"restore retains personal profiles");
            var drafts = coordinator.paletteDrafts(); for (String id : PaletteDrafts.PROFILES) drafts.resetPalette(id);
            coordinator.apply(null,true,drafts);
            check(coordinator.paletteDrafts().overrides("darcula").isEmpty() && coordinator.paletteDrafts().overrides("monokai").isEmpty(),"cleanup test personal colors via native-mode Apply");
            check(new SnapshotPreferences().load().original().isEmpty(),"palette journal empty after cleanup");
            var invalidDialog=fontDialog(); selectProfile(invalidDialog,1); editRole(invalidDialog,"keyword","#12345678");
            check(!findApply(invalidDialog.getShell()).getEnabled(),"oversized HEX rejected without silently truncating input");
            invalidDialog.close(); return;
        }
        if (coordinator.hasSnapshot()) check(coordinator.restore(true).successful(),"palette probe starts with restored baseline");
        var baseline = new LinkedHashMap<SettingKey,OriginalValue>();
        for (var key : adapter.plan(PaletteCatalog.load("darcula"),true).keySet()) baseline.put(key,adapter.read(key));
        for (var key : NativeSettingsCatalog.load().keySet()) baseline.putIfAbsent(key,adapter.read(key));
        String originalFont = InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").get(JFaceResources.TEXT_FONT,null);
        var dialog = fontDialog();
        check(findData(dialog.getShell(),"palette.roles") instanceof Table,"palette role editor exists");
        dialog.setBlockOnOpen(false); dialog.open();
        selectProfile(dialog,1); Table roles = (Table)findData(dialog.getShell(),"palette.roles");
        check(roles.getItemCount() == 17,"all 17 palette roles visible");
        for (TableItem item : roles.getItems()) {
            selectRole(dialog,(String)item.getData());
            check(!((Text)findData(dialog.getShell(),"palette.hex")).getText().isEmpty(),"HEX exists for "+item.getData());
        }
        editRole(dialog,"keyword","#123456"); checkLocalKeyword(dialog,new RGB(18,52,86));
        editRole(dialog,"editor.background","#202128");
        for (Control control : findFontGroup(dialog.getShell()).getChildren()) if (control instanceof StyledText text)
            check(new RGB(32,33,40).equals(text.getBackground().getRGB()),"draft background updates local code preview");
        selectProfile(dialog,2); editRole(dialog,"keyword","#ABCDEF"); checkLocalKeyword(dialog,new RGB(171,205,239));
        selectProfile(dialog,1); check("#123456".equals(((Text)findData(dialog.getShell(),"palette.hex")).getText()),"switching profiles keeps Darcula draft");
        selectProfile(dialog,2); check("#ABCDEF".equals(((Text)findData(dialog.getShell(),"palette.hex")).getText()),"switching profiles keeps Monokai draft");
        for (var entry : baseline.entrySet()) check(entry.getValue().equals(adapter.read(entry.getKey())),"pending palette keeps native "+entry.getKey());
        check(prefs.get("palette.monokai.keyword",null) == null,"pending personal colors not saved");
        ((Text)findData(dialog.getShell(),"palette.hex")).setText("#XYZ");
        check(!findApply(dialog.getShell()).getEnabled(),"invalid HEX blocks Apply and OK");
        selectProfile(dialog,1); check(!findApply(dialog.getShell()).getEnabled(),"invalid HEX in other draft still blocks Apply");
        selectProfile(dialog,2); ((Text)findData(dialog.getShell(),"palette.hex")).setText("#ABCDEF");
        check(findApply(dialog.getShell()).getEnabled(),"corrected HEX allows Apply");
        // Cancel discards both drafts, including the profile that is no longer selected.
        dialog.close(); check(coordinator.paletteDrafts().overrides("darcula").isEmpty(),"Cancel discards Darcula draft");
        check(coordinator.paletteDrafts().overrides("monokai").isEmpty(),"Cancel discards Monokai draft");
        dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); selectProfile(dialog,1); editRole(dialog,"keyword","#123456");
        selectProfile(dialog,2); editRole(dialog,"keyword","#ABCDEF");
        editRole(dialog,"panel","#303136");
        editRole(dialog,"keyword","#ABCDEF");
        findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event());
        check("monokai".equals(coordinator.activeProfile()),"Apply activates selected profile");
        check("#123456".equals(prefs.get("palette.darcula.keyword",null)),"Apply saves other draft");
        check("#ABCDEF".equals(prefs.get("palette.monokai.keyword",null)),"Apply saves selected draft");
        var keyword = SettingCatalog.load().roleTargets(Set.of("keyword"),true,"org.eclipse.ui.defaultTheme").iterator().next();
        var comment = SettingCatalog.load().roleTargets(Set.of("comment"),true,"org.eclipse.ui.defaultTheme").iterator().next();
        check(OriginalValue.of("171,205,239").equals(adapter.read(keyword)),"Apply uses personal keyword in EDT");
        check(new RGB(48,49,54).equals(PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().getRGB("com._1c.g5.v8.dt.theming.ui.BACKGROUND_COLOR_TITLE")),"Apply uses personal panel in native UI registry");
        editRole(dialog,"keyword","#445566"); dialog.close();
        check("#ABCDEF".equals(coordinator.paletteDrafts().resolve("monokai").color("keyword")),"Cancel after Apply keeps applied colors");
        // Explicit role edit reclaims keyword only; a different native manual color stays surrendered.
        InstanceScope.INSTANCE.getNode(comment.node()).put(comment.key(),"61,62,63");
        InstanceScope.INSTANCE.getNode(keyword.node()).put(keyword.key(),"71,72,73");
        coordinator.apply("darcula",true);
        check(OriginalValue.of("61,62,63").equals(adapter.read(comment)),"profile switch protects manual comment");
        dialog=fontDialog(); selectProfile(dialog,2); editRole(dialog,"keyword","#102030"); findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event());
        check(OriginalValue.of("16,32,48").equals(adapter.read(keyword)),"explicit role edit reclaims its surrendered target");
        check(OriginalValue.of("61,62,63").equals(adapter.read(comment)),"explicit keyword leaves manual comment protected");
        findButton(dialog.getShell(),"Вернуть цвет стиля").notifyListeners(SWT.Selection,new Event());
        findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event());
        check(prefs.get("palette.monokai.keyword",null)==null,"role reset removes override");
        check(OriginalValue.of("249,38,114").equals(adapter.read(keyword)),"role reset applies built-in Monokai keyword");
        check("#303136".equals(prefs.get("palette.monokai.panel",null)),"role reset keeps another personal role");
        check(OriginalValue.of("61,62,63").equals(adapter.read(comment)),"role reset protects unrelated manual color");
        findButton(dialog.getShell(),"Сбросить палитру").notifyListeners(SWT.Selection,new Event());
        findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event());
        check(OriginalValue.of("117,113,94").equals(adapter.read(comment)),"palette reset explicitly reclaims all its roles");
        check(prefs.get("palette.monokai.panel",null)==null,"palette reset removes other overrides in selected style");
        check("#123456".equals(prefs.get("palette.darcula.keyword",null)),"palette reset leaves the other style intact");
        var checkbox=findButton(dialog.getShell(),"Применять палитру BSL"); checkbox.setSelection(false); checkbox.notifyListeners(SWT.Selection,new Event());
        editRole(dialog,"keyword","#ABCDEF"); findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event());
        check(baseline.get(keyword).equals(adapter.read(keyword)),"BSL off restores original keyword despite edited role");
        check("#ABCDEF".equals(prefs.get("palette.monokai.keyword",null)),"BSL off saves personal keyword");
        checkbox.setSelection(true); checkbox.notifyListeners(SWT.Selection,new Event());
        Button ok=findButton(dialog.getShell(),JFaceResources.getString("PreferencesDialog.okButtonLabel")); check(ok != null,"palette OK control exists");
        ok.notifyListeners(SWT.Selection,new Event());
        check(OriginalValue.of("171,205,239").equals(adapter.read(keyword)),"OK applies saved personal BSL color");
        check(Objects.equals(originalFont,InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").get(JFaceResources.TEXT_FONT,null)),"palette edits and resets preserve independent font");
        check(coordinator.restore(true).successful(),"restore original EDT colors with personal profiles saved");
        for (var entry : baseline.entrySet()) check(entry.getValue().equals(adapter.read(entry.getKey())),"personal restore exact original "+entry.getKey());
        check("#123456".equals(prefs.get("palette.darcula.keyword",null)),"restore keeps Darcula personal color");
        check("#ABCDEF".equals(prefs.get("palette.monokai.keyword",null)),"restore keeps Monokai personal color");
        coordinator.apply("monokai",true);
        dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open();
        // Safe capture of only the visible, opaque editor control.
        roles=(Table)findData(dialog.getShell(),"palette.roles"); roles.showSelection(); roles.redraw(); roles.update();
        captureControl(roles,"palette-roles");
        captureControl(findData(dialog.getShell(),"palette.preview"),"palette-interface-preview");
        dialog.close();
    }
    private void selectProfile(PreferenceDialog dialog,int index) { var combo=findCombo(dialog.getShell()); combo.select(index); combo.notifyListeners(SWT.Selection,new Event()); }
    private void selectRole(PreferenceDialog dialog,String role) {
        Table table=(Table)findData(dialog.getShell(),"palette.roles");
        for (int i=0;i<table.getItemCount();i++) if (role.equals(table.getItem(i).getData())) { table.select(i); table.showSelection(); table.notifyListeners(SWT.Selection,new Event()); return; }
        throw new AssertionError("Role missing: "+role);
    }
    private void editRole(PreferenceDialog dialog,String role,String hex) { selectRole(dialog,role); ((Text)findData(dialog.getShell(),"palette.hex")).setText(hex); }
    private void checkLocalKeyword(PreferenceDialog dialog,RGB expected) {
        for (Control control : findFontGroup(dialog.getShell()).getChildren()) if (control instanceof StyledText text) {
            int offset=CodePreview.spans().stream().filter(s -> s.role().equals("keyword")).findFirst().get().offset();
            check(expected.equals(text.getStyleRangeAtOffset(offset).foreground.getRGB()),"local code preview uses draft keyword "+expected); return;
        } throw new AssertionError("No preview");
    }
    private void captureControl(Control control,String name) throws Exception {
        for (Composite parent=control.getParent();parent!=null;parent=parent.getParent())
            if (parent instanceof org.eclipse.swt.custom.ScrolledComposite scroll) scroll.showControl(control);
        control.getShell().layout(true,true); control.getShell().redraw();
        control.getDisplay().update(); long end=System.nanoTime()+350_000_000L;
        while (System.nanoTime()<end) { if (!control.getDisplay().readAndDispatch()) Thread.sleep(15); }
        Point screen=control.toDisplay(0,0); Rectangle visible=new Rectangle(screen.x,screen.y,control.getSize().x,control.getSize().y);
        for (Composite parent=control.getParent();parent!=null;parent=parent.getParent()) {
            Point origin=parent.toDisplay(parent.getClientArea().x,parent.getClientArea().y);
            Rectangle bounds=new Rectangle(origin.x,origin.y,parent.getClientArea().width,parent.getClientArea().height);
            visible=visible.intersection(bounds);
        }
        if(visible.isEmpty()) {
            report.append("CAPTURE CONTROL ").append(control).append(" size=").append(control.getSize()).append(" visible=").append(control.isVisible()).append(" screen=").append(screen).append('\n');
            for(Composite parent=control.getParent();parent!=null;parent=parent.getParent()) report.append("CAPTURE PARENT ").append(parent).append(" bounds=").append(parent.getBounds()).append(" client=").append(parent.getClientArea()).append(" visible=").append(parent.isVisible()).append(" deferred=").append(parent.getLayoutDeferred()).append('\n');
            captureSurfaceShell(control.getShell(),"invisible-"+name);
        }
        check(!visible.isEmpty(),"safe capture visible control intersection: "+name);
        Image image=new Image(control.getDisplay(),visible.width,visible.height); GC gc=new GC(control);
        try { gc.copyArea(image,visible.x-screen.x,visible.y-screen.y); ImageLoader loader=new ImageLoader(); loader.data=new ImageData[]{image.getImageData()}; loader.save(System.getProperty("edt.probe.images")+"/"+name+".png",SWT.IMAGE_PNG); }
        finally { gc.dispose(); image.dispose(); }
    }
    private Control findData(Composite parent,String id) {
        for (Control child : parent.getChildren()) {
            if (id.equals(child.getData("probe.id"))) return child;
            if (child instanceof Composite composite) { Control found = findData(composite,id); if (found != null) return found; }
        } return null;
    }
    private void runPreviewChecks() throws Exception {
        var coordinator = ThemeStartup.coordinator();
        for (String id : new String[]{"monokai","darcula"}) {
            coordinator.apply(id,true);
            String originalFont = InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").get(JFaceResources.TEXT_FONT,null);
            var dialog = fontDialog(); dialog.setBlockOnOpen(false); dialog.open();
            Group group = findFontGroup(dialog.getShell()); ReadableCombo combo = findFontPicker(group);
            check(combo != null,"font chooser uses separate popup list");
            StyledText preview = null;
            for (Control control : group.getChildren()) if (control instanceof StyledText text) preview = text;
            check(preview != null,"code preview exists"); checkPreview(preview,id);
            ReadableCombo profiles = findCombo(dialog.getShell());
            check(profiles != null && profiles != combo,"profile chooser uses separate readable popup");
            profiles.setFocus(); profiles.setListVisible(true); Table profileList = findFontTable(profiles);
            check(profileList.getItemCount() == PaletteCatalog.profiles(ThemeMode.DARK).size()+1,"profile popup contains native and all registered palettes");
            for (int index=0;index<3;index++) {
                profileList.setSelection(index); profileList.redraw(); profileList.update(); profileList.getShell().update();
                long deadline = System.nanoTime()+300_000_000L;
                while (System.nanoTime() < deadline) { if (!profileList.getDisplay().readAndDispatch()) Thread.sleep(15); }
                report.append("PROFILE ROW ").append(id).append(' ').append(profileList.getItem(index).getText()).append('\n');
                checkSelectedRowContrast(profileList);
                if (index == 1) saveVisibleShell(profileList.getShell(),id+"-profile-list");
            }
            int otherProfile = id.equals("monokai") ? 1 : 2; profileList.setSelection(otherProfile);
            Event profileClick = new Event(); profileClick.button = 1; profileList.notifyListeners(SWT.MouseUp,profileClick);
            check(!profiles.getListVisible() && profiles.getSelectionIndex() == otherProfile,"mouse confirms pending profile and closes popup");
            checkPreview(preview,id.equals("monokai") ? "darcula" : "monokai");
            check(id.equals(coordinator.activeProfile()),"pending preview palette does not apply native theme");
            profiles.setListVisible(true); profileList = findFontTable(profiles); profileList.setSelection(0);
            Event profileEscape = new Event(); profileEscape.keyCode = SWT.ESC; profileList.notifyListeners(SWT.KeyDown,profileEscape);
            check(!profiles.getListVisible() && profiles.getSelectionIndex() == otherProfile,"Escape cancels pending profile row");
            profiles.setListVisible(true); profileList = findFontTable(profiles); profileList.setSelection(id.equals("monokai") ? 2 : 1);
            Event profileEnter = new Event(); profileEnter.keyCode = SWT.CR; profileList.notifyListeners(SWT.KeyDown,profileEnter);
            check(!profiles.getListVisible() && profiles.getSelectionIndex() == (id.equals("monokai") ? 2 : 1),"Enter confirms profile row");
            check(id.equals(coordinator.activeProfile()),"popup keyboard selection does not apply profile");
            Button checkbox = findButton(dialog.getShell(),"Применять палитру BSL"); checkbox.setSelection(false); checkbox.notifyListeners(SWT.Selection,new Event());
            check(preview.getStyleRanges().length == 0,"unchecked BSL clears preview token styles");
            checkbox.setSelection(true); checkbox.notifyListeners(SWT.Selection,new Event()); checkPreview(preview,id);
            combo.setFocus(); combo.setListVisible(true);
            while (dialog.getShell().getDisplay().readAndDispatch()) {}
            check(combo.getListVisible(),"font popup stays open");
            Table list = null;
            for (Shell shell : dialog.getShell().getDisplay().getShells()) if (shell.isVisible() && shell != dialog.getShell()) {
                for (Control child : shell.getChildren()) if (child instanceof Table candidate && candidate.getSelectionCount() == 1 && candidate.getSelection()[0].getText().equals(combo.getText())) list = candidate;
            }
            check(list != null,"visible font popup list found through public SWT APIs");
            list.redraw(); list.update(); list.getShell().update();
            long paintDeadline = System.nanoTime()+300_000_000L;
            while (System.nanoTime() < paintDeadline) { if (!list.getDisplay().readAndDispatch()) Thread.sleep(15); }
            report.append("POPUP table bg=").append(list.getBackground().getRGB()).append(" fg=").append(list.getForeground().getRGB()).append(" row=").append(list.getSelection()[0].getBounds()).append(" top=").append(list.getTopIndex()).append('\n');
            saveVisibleShell(list.getShell(),id+"-font-list");
            checkSelectedRowContrast(list);
            int target = -1; for (int index=0;index<list.getItemCount();index++) if (list.getItem(index).getText().equals("Consolas")) target = index;
            if (target < 0 || target == list.getSelectionIndex()) target = (list.getSelectionIndex()+1)%list.getItemCount();
            String chosen = list.getItem(target).getText(); list.setSelection(target); list.notifyListeners(SWT.Selection,new Event());
            Event click = new Event(); click.button = 1; list.notifyListeners(SWT.MouseUp,click); check(!combo.getListVisible(),"choosing a font closes popup");
            check(combo.getText().equals(chosen),"popup selection updates font name");
            combo.setListVisible(true); Table keyboardTable = findFontTable(combo);
            int keyboardIndex = (keyboardTable.getSelectionIndex()+1)%keyboardTable.getItemCount();
            keyboardTable.setSelection(keyboardIndex); Event escape = new Event(); escape.keyCode = SWT.ESC; keyboardTable.notifyListeners(SWT.KeyDown,escape);
            check(!combo.getListVisible() && combo.getText().equals(chosen),"Escape closes font popup without changing chosen name");
            combo.setListVisible(true); keyboardTable = findFontTable(combo);
            keyboardTable.setSelection(keyboardIndex); String keyboardFont = keyboardTable.getSelection()[0].getText();
            Event enter = new Event(); enter.keyCode = SWT.CR; keyboardTable.notifyListeners(SWT.KeyDown,enter);
            check(!combo.getListVisible() && combo.getText().equals(keyboardFont),"Enter confirms selected font name");
            check(Objects.equals(originalFont,InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").get(JFaceResources.TEXT_FONT,null)),"preview and font list selection do not write native font preference");
            dialog.close();
        }
        check(coordinator.restore(true).successful(),"preview probe restores test colors");
    }
    private void checkPreview(StyledText preview,String id) {
        var palette = PaletteCatalog.load(id); check(preview.getBackground().getRGB().equals(rgb(palette,"editor.background")),id+" preview background");
        for (var span : CodePreview.spans()) {
            var style = preview.getStyleRangeAtOffset(span.offset());
            check(style != null && style.foreground != null && style.foreground.getRGB().equals(rgb(palette,span.role())),id+" preview "+span.role()+" at "+span.offset());
        }
    }
    private Table findFontTable(ReadableCombo picker) {
        for (Shell shell : picker.getDisplay().getShells()) if (shell.isVisible()) for (Control child : shell.getChildren()) {
            if (child instanceof Table table && table.getSelectionCount() == 1 && table.getSelection()[0].getText().equals(picker.getText())) return table;
        }
        throw new AssertionError("No font popup table");
    }
    private void checkSelectedRowContrast(Table list) {
        int rowY = list.getSelection()[0].getBounds().y;
        check(rowY >= 0 && rowY+list.getItemHeight() <= list.getClientArea().height,"selected popup row is visible");
        Image image = new Image(list.getDisplay(),Math.max(1,list.getClientArea().width-24),Math.max(1,list.getItemHeight()-4)); GC gc = new GC(list);
        try {
            gc.copyArea(image,4,rowY+2); var pixels = image.getImageData(); var histogram = new HashMap<RGB,Integer>();
            for (int y=0;y<pixels.height;y++) for(int x=0;x<pixels.width;x++) histogram.merge(pixels.palette.getRGB(pixels.getPixel(x,y)),1,Integer::sum);
            RGB background = histogram.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
            double contrast = histogram.entrySet().stream().filter(e -> e.getValue() >= 5).mapToDouble(e -> {
                double a = luminance(background), b = luminance(e.getKey()); return (Math.max(a,b)+0.05)/(Math.min(a,b)+0.05);
            }).max().orElse(1);
            report.append("SELECTED ROW background=").append(background).append(" contrast=").append(contrast).append('\n');
            check(contrast >= 3,"rendered selected popup row has readable glyph contrast");
        } finally { gc.dispose(); image.dispose(); }
    }
    private double luminance(RGB rgb) {
        double[] c = {rgb.red/255.0,rgb.green/255.0,rgb.blue/255.0};
        for(int i=0;i<3;i++) c[i] = c[i] <= 0.04045 ? c[i]/12.92 : Math.pow((c[i]+0.055)/1.055,2.4);
        return c[0]*0.2126+c[1]*0.7152+c[2]*0.0722;
    }
    private void saveVisibleShell(Shell shell,String name) {
        while (shell.getDisplay().readAndDispatch()) {} shell.getDisplay().update();
        // Called only for our visible opaque popup; never captures the desktop or another app.
        Rectangle bounds = shell.getClientArea(); Image image = new Image(shell.getDisplay(),bounds.width,bounds.height); GC gc = new GC(shell);
        try { gc.copyArea(image,0,0); ImageLoader loader = new ImageLoader(); loader.data = new ImageData[]{image.getImageData()}; loader.save(System.getProperty("edt.probe.images")+"/"+name+".png",SWT.IMAGE_PNG); }
        finally { gc.dispose(); image.dispose(); }
    }
    private void runFontChecks() throws Exception {
        var workbench = PlatformUI.getWorkbench();
        var nativeFonts = InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench");
        String key = JFaceResources.TEXT_FONT;
        var expectationFile = java.nio.file.Path.of(System.getProperty("edt.probe.images"),"font-expectations.properties");
        var expected = new Properties();
        StyledText editor = tryOpenBsl(); check(editor != null,"font test opens real BSL editor");
        if ("fontrestore".equals(System.getProperty("edt.probe.phase"))) {
            try (var reader = Files.newBufferedReader(expectationFile)) { expected.load(reader); }
            checkFont(editor,expected.getProperty("chosen.name"),Integer.parseInt(expected.getProperty("chosen.size")),"font survives JVM restart");
            var dialog = fontDialog();
            Button reset = findButton(dialog.getShell(),"Вернуть исходный шрифт"); check(reset != null && reset.getEnabled(),"restore font enabled after restart");
            reset.notifyListeners(SWT.Selection,new Event());
            findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event());
            dialog.close();
            String original = Boolean.parseBoolean(expected.getProperty("original.present")) ? expected.getProperty("original.raw") : null;
            check(Objects.equals(original,nativeFonts.get(key,null)),"original explicit font value or absence restored after restart");
            checkFont(editor,expected.getProperty("original.name"),Integer.parseInt(expected.getProperty("original.size")),"original BSL font restored after restart");
            check(!new CodeFontSettings().hasOriginal(),"font journal cleared after restoration");
            return;
        }
        var initialFonts = new CodeFontSettings(); if (initialFonts.hasOriginal()) initialFonts.restore();
        String originalRaw = nativeFonts.get(key,null);
        FontData original = JFaceResources.getFontRegistry().getFontData(key)[0];
        expected.setProperty("original.present",Boolean.toString(originalRaw != null));
        if (originalRaw != null) expected.setProperty("original.raw",originalRaw);
        expected.setProperty("original.name",original.getName()); expected.setProperty("original.size",Integer.toString(original.getHeight()));
        String family = workbench.getDisplay().getFontList("Consolas",true).length > 0 ? "Consolas" : original.getName();
        int size = original.getHeight()+3;
        var dialog = fontDialog();
        Group group = findFontGroup(dialog.getShell()); check(group != null,"font group exists");
        selectFont(group,family,size);
        checkFont(editor,original.getName(),original.getHeight(),"font selection is only a preview before Apply");
        check(Objects.equals(originalRaw,nativeFonts.get(key,null)),"font preview does not write preferences");
        for (Object node : dialog.getPreferenceManager().getElements(PreferenceManager.PRE_ORDER)) {
            IPreferenceNode item = (IPreferenceNode)node;
            if (item.getId().equals("ru.litonchik.edt.themes.preferences")) check(item.getPage().performCancel(),"font Cancel accepted");
        }
        dialog.close();
        checkFont(editor,original.getName(),original.getHeight(),"Cancel before Apply leaves font unchanged");
        dialog = fontDialog(); selectFont(findFontGroup(dialog.getShell()),family,size);
        dialog.setBlockOnOpen(false); dialog.open();
        saveShell(dialog.getShell(),"font-preferences-preview");
        Button apply = findApply(dialog.getShell()); check(apply != null,"font Apply button exists"); apply.notifyListeners(SWT.Selection,new Event());
        checkFont(editor,family,size,"Apply updates already open BSL editor");
        check(org.eclipse.jface.preference.PreferenceConverter.readFontData(nativeFonts.get(key,null))[0].getHeight() == size,"font stored in native EDT preference");
        dialog.close();
        checkFont(editor,family,size,"Cancel after Apply keeps committed font");
        var coordinator = ThemeStartup.coordinator();
        coordinator.apply("darcula",true); coordinator.apply("monokai",true);
        checkFont(editor,family,size,"font survives Darcula Monokai switch");
        check(coordinator.restore(true).successful(),"color restore for independent font test");
        checkFont(editor,family,size,"color restoration leaves font unchanged");
        var page = workbench.getActiveWorkbenchWindow().getActivePage(); page.closeEditor(page.getActiveEditor(),false);
        editor = tryOpenBsl(); check(editor != null,"new BSL editor opens in font test");
        checkFont(editor,family,size,"new BSL editor inherits chosen font");
        // A native settings edit followed by an explicit choice on our page must honor that choice.
        FontData manual = new FontData(family,size+1,SWT.NORMAL);
        nativeFonts.put(key,org.eclipse.jface.preference.PreferenceConverter.getStoredRepresentation(new FontData[]{manual})); nativeFonts.flush();
        workbench.getThemeManager().getCurrentTheme().getFontRegistry().put(key,new FontData[]{manual});
        dialog = fontDialog(); selectFont(findFontGroup(dialog.getShell()),family,size+2);
        findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event()); dialog.close();
        size += 2; checkFont(editor,family,size,"explicit font choice replaces later native font edit");
        dialog = fontDialog(); Button reset = findButton(dialog.getShell(),"Вернуть исходный шрифт");
        check(reset != null && reset.getEnabled(),"restore original font available"); reset.notifyListeners(SWT.Selection,new Event());
        checkFont(editor,family,size,"font reset is pending until Apply"); dialog.close();
        checkFont(editor,family,size,"Cancel discards pending font reset");
        dialog = fontDialog(); findButton(dialog.getShell(),"Вернуть исходный шрифт").notifyListeners(SWT.Selection,new Event());
        findApply(dialog.getShell()).notifyListeners(SWT.Selection,new Event()); dialog.close();
        check(Objects.equals(originalRaw,nativeFonts.get(key,null)),"reset restores exact native value or absence");
        checkFont(editor,original.getName(),original.getHeight(),"reset restores original effective BSL font");
        FontData explicitOriginal = new FontData(family,size+1,SWT.NORMAL);
        String explicitRaw = org.eclipse.jface.preference.PreferenceConverter.getStoredRepresentation(new FontData[]{explicitOriginal});
        nativeFonts.put(key,explicitRaw); nativeFonts.flush();
        workbench.getThemeManager().getCurrentTheme().getFontRegistry().put(key,new FontData[]{explicitOriginal});
        var explicitTest = new CodeFontSettings(); explicitTest.apply(new FontData[]{new FontData(family,size+2,SWT.NORMAL)}); explicitTest.restore();
        check(explicitRaw.equals(nativeFonts.get(key,null)),"restore preserves a preexisting explicit native font value");
        checkFont(editor,family,size+1,"restore preserves preexisting explicit effective font");
        if (originalRaw == null) nativeFonts.remove(key); else nativeFonts.put(key,originalRaw);
        nativeFonts.flush(); workbench.getThemeManager().getCurrentTheme().getFontRegistry().put(key,new FontData[]{original});
        dialog = fontDialog(); selectFont(findFontGroup(dialog.getShell()),family,size);
        // Real OK path as well as Apply; leave committed font for the next JVM process.
        Button ok = findButton(dialog.getShell(),org.eclipse.jface.resource.JFaceResources.getString("PreferencesDialog.okButtonLabel"));
        check(ok != null,"real OK button exists"); ok.notifyListeners(SWT.Selection,new Event());
        checkFont(editor,family,size,"OK commits chosen font");
        expected.setProperty("chosen.name",family); expected.setProperty("chosen.size",Integer.toString(size));
        try (var writer = Files.newBufferedWriter(expectationFile)) { expected.store(writer,"Isolated font probe"); }
        screenshot("font-chosen");
    }
    private void saveShell(Shell shell,String name) {
        while (shell.getDisplay().readAndDispatch()) {} shell.getDisplay().update();
        report.append("UI opened ").append(name).append(" (broad window capture disabled)\n");
    }
    private PreferenceDialog fontDialog() {
        var dialog = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"ru.litonchik.edt.themes.preferences",null,null);
        check(dialog != null,"font preference page exists"); check(findSpinner(dialog.getShell()) != null,"font size selector exists"); return dialog;
    }
    private void selectFont(Group group,String family,int size) {
        ReadableCombo combo = findFontPicker(group); combo.setText(family); combo.notifyListeners(SWT.Selection,new Event());
        Spinner spinner = findSpinner(group); spinner.setSelection(size); spinner.notifyListeners(SWT.Selection,new Event());
    }
    private void checkFont(StyledText editor,String family,int size,String label) {
        while (PlatformUI.getWorkbench().getDisplay().readAndDispatch()) {}
        FontData actual = editor.getFont().getFontData()[0];
        check(actual.getName().equalsIgnoreCase(family) && actual.getHeight() == size,label+": "+actual.getName()+" "+actual.getHeight());
        int offset = editor.getText().indexOf("123.45"); var range = editor.getStyleRangeAtOffset(offset);
        if (range != null && range.font != null) check(range.font.getFontData()[0].getHeight() == size,label+" numeric glyph font size");
    }
    private Group findFontGroup(Composite parent) {
        for (Control child : parent.getChildren()) {
            if (child instanceof Group group && group.getText().equals("Шрифт кода")) return group;
            if (child instanceof Composite composite) { Group found = findFontGroup(composite); if (found != null) return found; }
        } return null;
    }
    private Button findButton(Composite parent,String text) {
        for (Control child : parent.getChildren()) {
            if (child instanceof Button button && button.getText().replace("&","").equals(text.replace("&",""))) return button;
            if (child instanceof Composite composite) { Button found = findButton(composite,text); if (found != null) return found; }
        } return null;
    }
    private Spinner findSpinner(Composite parent) {
        for (Control child : parent.getChildren()) {
            if (child instanceof Spinner spinner) return spinner;
            if (child instanceof Composite composite) { Spinner found = findSpinner(composite); if (found != null) return found; }
        }
        return null;
    }
    private ReadableCombo findFontPicker(Composite parent) {
        for (Control child : parent.getChildren()) {
            if (child instanceof ReadableCombo combo) return combo;
            if (child instanceof Composite composite) { ReadableCombo found = findFontPicker(composite); if (found != null) return found; }
        } return null;
    }
    private RGB rgb(Palette p, String role) { String[] c = p.rgb(role).split(","); return new RGB(Integer.parseInt(c[0]),Integer.parseInt(c[1]),Integer.parseInt(c[2])); }
    private StyledText tryOpenBsl() {
        try {
            var project = ResourcesPlugin.getWorkspace().getRoot().getProject("ThemeProbe");
            if (!project.exists()) project.create(null); if (!project.isOpen()) project.open(null);
            if(!project.hasNature("org.eclipse.xtext.ui.shared.xtextNature")) {
                var description=project.getDescription();
                var natures=new ArrayList<>(Arrays.asList(description.getNatureIds()));
                natures.add("org.eclipse.xtext.ui.shared.xtextNature");
                description.setNatureIds(natures.toArray(String[]::new)); project.setDescription(description,null);
            }
            var file = project.getFile("module.bsl");
            byte[] source = Files.readAllBytes(java.nio.file.Path.of(System.getProperty("edt.probe.fixture")));
            if (!file.exists()) file.create(new ByteArrayInputStream(source),true,null); else file.setContents(new ByteArrayInputStream(source),true,false,null);
            file.setCharset("UTF-8",null);
            var editorPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            editorPage.setEditorAreaVisible(true);
            var editor = IDE.openEditor(editorPage,file,"com._1c.g5.v8.dt.bsl.Bsl",true);
            editorPage.activate(editor); editorPage.bringToTop(editor);
            pump(1500);
            StyledText text = findBsl(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
            if(text==null && editor instanceof org.eclipse.xtext.ui.editor.XtextEditor xtext && xtext.getInternalSourceViewer()!=null) {
                text=xtext.getInternalSourceViewer().getTextWidget();
                report.append("OBS BSL direct widget text=").append(text.getText()).append('\n');
            }
            report.append(text == null ? "SKIP BSL StyledText requires a metadata project\n" : "PASS BSL editor opened on test fixture\n");
            return text;
        } catch (Exception e) { report.append("SKIP BSL editor: ").append(e).append('\n'); return null; }
    }
    private StyledText findBsl(Composite parent) {
        for (Control c : parent.getChildren()) {
            if (c instanceof StyledText text && text.getText().contains("ПроверитьПалитру")) return text;
            if (c instanceof Composite composite) { StyledText found = findBsl(composite); if (found != null) return found; }
        }
        return null;
    }
    private ReadableCombo findCombo(Composite parent) {
        for (Control c : parent.getChildren()) {
            if (c instanceof ReadableCombo combo) return combo;
            if (c instanceof Composite composite) { ReadableCombo found = findCombo(composite); if (found != null) return found; }
        }
        return null;
    }
    private Button findApply(Composite parent) {
        for (Control c : parent.getChildren()) {
            if (c instanceof Button button) {
                report.append("BUTTON ").append(button.getText()).append(" expected=").append(JFaceResources.getString("apply")).append('\n');
                if (button.getText().replace("&", "").equals(JFaceResources.getString("apply").replace("&", ""))) return button;
            }
            if (c instanceof Composite composite) { Button found = findApply(composite); if (found != null) return found; }
        }
        return null;
    }
}
