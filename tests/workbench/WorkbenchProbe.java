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
        if ("surfaces".equals(System.getProperty("edt.probe.phase"))) try {
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
        if (Set.of("semantic","nativeswitch").contains(System.getProperty("edt.probe.phase"))) { runSemanticChecks(); return; }
        if ("surfaces".equals(System.getProperty("edt.probe.phase"))) { runSurfaceChecks(); return; }
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
    private void runSurfaceChecks() throws Exception {
        var workbench=PlatformUI.getWorkbench();
        var page=workbench.getActiveWorkbenchWindow().getActivePage();
        try(var coordinator=new ThemeCoordinator()) {
            coordinator.start();
            if(coordinator.hasSnapshot()) check(coordinator.restore(true).successful(),"surfaces initial cleanup");
            var dialog=fontDialog(); dialog.setBlockOnOpen(false); dialog.open(); pump(250);
            check(findCombo(dialog.getShell())!=null && findData(dialog.getShell(),"palette.roles") instanceof Table,"theme preference page renders profile, font and palette controls");
            dialog.close();
            var adapter=new EdtSettingsAdapter(workbench.getService(IThemeEngine.class),workbench.getThemeManager());
            var baseline=new LinkedHashMap<SettingKey,OriginalValue>();
            for(var key:adapter.plan(PaletteCatalog.load("darcula"),true).keySet()) baseline.put(key,adapter.read(key));
            for(var key:NativeSettingsCatalog.load().keySet()) baseline.putIfAbsent(key,adapter.read(key));
            coordinator.apply("visual-studio-dark-cpp",true); pump(1800);
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
                var editor=IDE.openEditor(page,file,entry[1],true); pump(1400);
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
                for(String id:PaletteCatalog.ids()) {
                    coordinator.apply(id,true); pump(900);
                    var palette=PaletteCatalog.load(id);
                    for(int i=0;i<queryTexts.size();i++) {
                        StyledText text=queryTexts.get(i);
                        check(rgb(palette,"editor.background").equals(text.getBackground().getRGB()),id+" query surface "+i+" background");
                        check(rgb(palette,"keyword").equals(glyph(text,"ВЫБРАТЬ")),id+" query surface "+i+" keyword");
                        check(rgb(palette,"text").equals(glyph(text,"123")),id+" query surface "+i+" native NUMBER text role expected="+rgb(palette,"text")+" actual="+glyph(text,"123"));
                        check(rgb(palette,"string").equals(glyph(text,"строка")),id+" query surface "+i+" string expected="+rgb(palette,"string")+" actual="+glyph(text,"строка"));
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
            var compare=new org.eclipse.compare.CompareEditorInput(new org.eclipse.compare.CompareConfiguration()) {
                protected Object prepareInput(IProgressMonitor monitor) { return input; }
                @Override public Control createContents(Composite parent) {
                    var viewer=factory.createViewer(parent,getCompareConfiguration(),workbench.getActiveWorkbenchWindow());
                    viewer.setInput(input); return viewer.getControl();
                }
            };
            org.eclipse.compare.CompareUI.openCompareEditor(compare); pump(2500);
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
                if(id.equals("darcula") || id.equals("monokai")) continue;
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
        var list=findFontTable(combo); check(list.getItemCount()==PaletteCatalog.profiles().size()+1,"native plus all registered palettes in popup");
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
        control.getDisplay().update(); long end=System.nanoTime()+350_000_000L;
        while (System.nanoTime()<end) { if (!control.getDisplay().readAndDispatch()) Thread.sleep(15); }
        Point screen=control.toDisplay(0,0); Rectangle visible=new Rectangle(screen.x,screen.y,control.getSize().x,control.getSize().y);
        for (Composite parent=control.getParent();parent!=null;parent=parent.getParent()) {
            Point origin=parent.toDisplay(parent.getClientArea().x,parent.getClientArea().y);
            Rectangle bounds=new Rectangle(origin.x,origin.y,parent.getClientArea().width,parent.getClientArea().height);
            visible=visible.intersection(bounds);
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
            check(profileList.getItemCount() == PaletteCatalog.profiles().size()+1,"profile popup contains native and all registered palettes");
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
            var editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),file,"com._1c.g5.v8.dt.bsl.Bsl",true);
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
