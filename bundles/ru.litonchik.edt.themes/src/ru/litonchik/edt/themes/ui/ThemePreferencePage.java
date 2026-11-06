package ru.litonchik.edt.themes.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.*;
import ru.litonchik.edt.themes.runtime.*;
import ru.litonchik.edt.themes.model.RestoreResult;
import ru.litonchik.edt.themes.model.CodePreview;
import ru.litonchik.edt.themes.model.Palette;
import ru.litonchik.edt.themes.model.PaletteDrafts;
import ru.litonchik.edt.themes.model.PaletteCatalog;
import ru.litonchik.edt.themes.model.ThemeMode;
import org.eclipse.swt.custom.ScrolledComposite;

public final class ThemePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    private ReadableCombo profiles;
    private java.util.List<String> profileIds = java.util.List.of();
    private ThemeMode mode = ThemeMode.DARK;
    private Button darkMode, lightMode;
    private final java.util.Map<ThemeMode,String> lastSelected = new java.util.EnumMap<>(ThemeMode.class);
    private Button bsl, chromaIcons;
    private Group iconPreview;
    private final java.util.List<Label> iconSamples=new java.util.ArrayList<>();
    private ThemeCoordinator coordinator;
    private Button restore;
    private CodeFontSettings fonts;
    private ReadableCombo fontFamily;
    private Spinner fontSize;
    private StyledText fontPreview;
    private Font previewFont;
    private Button restoreFont;
    private FontData[] pendingFont;
    private boolean fontDirty, fontReset;
    private PaletteDrafts drafts;
    private PaletteEditor paletteEditor;
    private final java.util.Map<RGB,Color> previewColors = new java.util.HashMap<>();
    public ThemePreferencePage() { setTitle("Темы EDT"); noDefaultButton(); }
    @Override public void init(IWorkbench workbench) {}
    @Override protected Control createContents(Composite parent) {
        ScrolledComposite scroll = new ScrolledComposite(parent,SWT.V_SCROLL|SWT.H_SCROLL);
        scroll.setExpandHorizontal(true); scroll.setExpandVertical(true);
        Composite body = new Composite(scroll,SWT.NONE); body.setLayout(new GridLayout(1,false)); scroll.setContent(body);
        Label explanation = new Label(body,SWT.WRAP);
        explanation.setText("Выберите палитру. После перехода между светлой и тёмной основой примените тему и перезапустите EDT.");
        GridData data = new GridData(SWT.FILL,SWT.TOP,true,false); data.widthHint = 460; explanation.setLayoutData(data);
        Composite modes = new Composite(body,SWT.NONE); modes.setLayout(new GridLayout(3,false));
        new Label(modes,SWT.NONE).setText("Палитры:");
        darkMode = new Button(modes,SWT.RADIO); darkMode.setText("Тёмные"); darkMode.setData("theme.mode",ThemeMode.DARK);
        lightMode = new Button(modes,SWT.RADIO); lightMode.setText("Светлые"); lightMode.setData("theme.mode",ThemeMode.LIGHT);
        profiles = new ReadableCombo(body);
        showMode(ThemeMode.DARK,null);
        darkMode.addListener(SWT.Selection,event -> { if (darkMode.getSelection()) selectMode(ThemeMode.DARK); });
        lightMode.addListener(SWT.Selection,event -> { if (lightMode.getSelection()) selectMode(ThemeMode.LIGHT); });
        GridData profileData = new GridData(SWT.LEFT,SWT.CENTER,false,false); profileData.widthHint = 280; profiles.setLayoutData(profileData);
        bsl = new Button(body,SWT.CHECK); bsl.setText("Применять палитру BSL"); bsl.setSelection(true);
        chromaIcons=new Button(body,SWT.CHECK);chromaIcons.setText("Значки Visual Studio в Навигаторе");chromaIcons.setData(ChromaIcons.KEY,Boolean.TRUE);chromaIcons.setSelection(ChromaIcons.isEnabled());
        iconPreview=new Group(body,SWT.NONE);iconPreview.setText("Visual Studio Dark");iconPreview.setLayout(new RowLayout(SWT.HORIZONTAL));iconPreview.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));
        for(String[] sample:new String[][]{{"meta-catalog","Справочник"},{"meta-document","Документ"},{"meta-enumeration","Перечисление"},{"meta-module","Модуль"}}){
            var item=new Composite(iconPreview,SWT.NONE);item.setLayout(new RowLayout(SWT.HORIZONTAL));var image=new Label(item,SWT.NONE);image.setData("chroma.asset",sample[0]);iconSamples.add(image);new Label(item,SWT.NONE).setText(sample[1]);
        }
        restore = new Button(body,SWT.PUSH); restore.setText("Восстановить исходное оформление…");
        try {
            coordinator = ThemeStartup.coordinator();
            String active = coordinator.activeProfile();
            var engine = PlatformUI.getWorkbench().getService(org.eclipse.e4.ui.css.swt.theme.IThemeEngine.class);
            ThemeMode initialMode = active == null ? ThemeMode.fromCssId(engine.getActiveTheme().getId()) : PaletteCatalog.mode(active);
            showMode(initialMode,active); lastSelected.put(initialMode,active);
            bsl.setSelection(coordinator.includeBsl());
            restore.setEnabled(coordinator.hasSnapshot() || ChromaIcons.isEnabled());
            fonts = new CodeFontSettings();
            drafts = coordinator.paletteDrafts();
        } catch (Exception e) { setErrorMessage(e.getMessage()); setValid(false); profiles.setEnabled(false); darkMode.setEnabled(false); lightMode.setEnabled(false); bsl.setEnabled(false); chromaIcons.setEnabled(false); restore.setEnabled(false); }
        restore.addListener(SWT.Selection,event -> {
            if (!MessageDialog.openConfirm(getShell(),"Восстановить цвета","Вернуть исходную тему и цвета? Ручные изменения после включения профиля будут сохранены.")) return;
            try {
                RestoreResult result = coordinator.restore(false);
                if (!result.successful()) throw new IllegalStateException("Ошибка восстановления: " + result.errorSummary());
                if (!result.conflicts().isEmpty() && MessageDialog.openConfirm(getShell(),"Ручные цвета сохранены","Сохранено ручных изменений: " + result.conflicts().size() + ". Принудительно вернуть первоначальные цвета вместо этих правок?")) {
                    result = coordinator.restore(true);
                    if (!result.successful()) throw new IllegalStateException("Ошибка восстановления: " + result.errorSummary());
                }
                ChromaIcons.setEnabled(false); chromaIcons.setSelection(false); profiles.select(0); refreshPalette(); restore.setEnabled(coordinator.hasSnapshot() || ChromaIcons.isEnabled()); setMessage("Исходное оформление восстановлено. Личные палитры сохранены. Для редакторов может потребоваться перезапуск.");
            } catch (Exception e) { setErrorMessage(e.getMessage()); ThemeStartup.report(e); }
        });
        if (drafts != null) paletteEditor = new PaletteEditor(body,drafts,this::updatePreviewStyling,error -> { setErrorMessage(error); setValid(error == null); },this::color);
        createFontControls(body);
        profiles.addListener(SWT.Selection,event -> refreshPalette());
        bsl.addListener(SWT.Selection,event -> refreshPalette());
        refreshPalette();
        scroll.setMinSize(body.computeSize(510,SWT.DEFAULT));
        ru.litonchik.edt.themes.edt.LightUiStyles.registerPreferences(body);
        return scroll;
    }
    private String selectedProfile() {
        int index = profiles.getSelectionIndex();
        return index <= 0 ? null : profileIds.get(index - 1);
    }
    private void showMode(ThemeMode next,String selected) {
        mode = next; darkMode.setSelection(mode == ThemeMode.DARK); lightMode.setSelection(mode == ThemeMode.LIGHT);
        var visible = PaletteCatalog.profiles(mode); profileIds = visible.stream().map(PaletteCatalog.Profile::id).toList();
        profiles.setItems(java.util.stream.Stream.concat(java.util.stream.Stream.of("Штатное оформление"),visible.stream().map(PaletteCatalog.Profile::label)).toArray(String[]::new));
        profiles.select(profileIds.indexOf(selected)+1);
    }
    private void selectMode(ThemeMode next) {
        if (mode == next) return;
        lastSelected.put(mode,selectedProfile());
        String selected = lastSelected.containsKey(next) ? lastSelected.get(next) : PaletteCatalog.profiles(next).get(0).id();
        showMode(next,selected); refreshPalette();
    }
    private void refreshPalette() { if(iconPreview!=null)iconPreview.setText(mode==ThemeMode.LIGHT?"Visual Studio Light":"Visual Studio Dark"); if(coordinator!=null)for(var label:iconSamples)label.setImage(ChromaIcons.image((String)label.getData("chroma.asset"),mode)); if (paletteEditor != null) paletteEditor.setProfile(selectedProfile(),bsl.getSelection()); updatePreviewStyling(); }
    private void createFontControls(Composite parent) {
        Group group = new Group(parent,SWT.NONE); group.setText("Шрифт кода");
        group.setLayout(new GridLayout(2,false)); group.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));
        fontFamily = new ReadableCombo(group);
        fontFamily.setToolTipText("Установленный шрифт текстовых редакторов");
        fontFamily.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
        fontSize = new Spinner(group,SWT.BORDER); fontSize.setMinimum(1); fontSize.setMaximum(200);
        fontSize.setToolTipText("Размер шрифта в пунктах");
        var names = new java.util.TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (FontData data : group.getDisplay().getFontList(null,true)) names.add(data.getName());
        if (fonts != null) for (FontData data : fonts.currentFont()) names.add(data.getName());
        fontFamily.setItems(names.toArray(String[]::new));
        fontPreview = new StyledText(group,SWT.BORDER|SWT.READ_ONLY|SWT.MULTI|SWT.H_SCROLL|SWT.V_SCROLL);
        fontPreview.setText(CodePreview.SOURCE);
        GridData previewData = new GridData(SWT.FILL,SWT.FILL,true,false,2,1); previewData.heightHint = 90; previewData.widthHint = 430;
        fontPreview.setLayoutData(previewData);
        restoreFont = new Button(group,SWT.PUSH); restoreFont.setText("Вернуть исходный шрифт");
        restoreFont.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,false,false,2,1));
        Label note = new Label(group,SWT.WRAP); note.setText("Шрифт сохраняется независимо от темы. Используется общий текстовый шрифт EDT, в том числе в других текстовых редакторах.");
        GridData noteData = new GridData(SWT.FILL,SWT.TOP,true,false,2,1); noteData.widthHint = 430; note.setLayoutData(noteData);
        if (fonts != null) showFont(fonts.currentFont());
        else { fontFamily.setEnabled(false); fontSize.setEnabled(false); }
        restoreFont.setEnabled(fonts != null && fonts.hasOriginal());
        org.eclipse.swt.widgets.Listener changed = event -> {
            if (fontFamily.getSelectionIndex() < 0) return;
            pendingFont = new FontData[]{new FontData(fontFamily.getText(),fontSize.getSelection(),SWT.NORMAL)};
            fontDirty = true; fontReset = false; updatePreview();
        };
        fontFamily.addListener(SWT.Selection,changed); fontSize.addListener(SWT.Selection,changed);
        fontSize.addListener(SWT.Modify,changed);
        restoreFont.addListener(SWT.Selection,event -> {
            showFont(fonts.originalFont()); fontReset = true; fontDirty = false;
            setMessage("Исходный шрифт выбран. Нажмите «Применить» или «ОК».");
        });
    }
    private void showFont(FontData[] data) {
        // Spinner Modify events during a programmatic refresh must not turn restoration into a new apply.
        pendingFont = data; fontFamily.setText(data[0].getName()); fontSize.setSelection(data[0].getHeight());
        pendingFont = data; fontDirty = false; fontReset = false; updatePreview();
    }
    private void updatePreview() {
        Font next = new Font(fontPreview.getDisplay(),pendingFont);
        fontPreview.setFont(next); if (previewFont != null) previewFont.dispose(); previewFont = next;
        updatePreviewStyling();
    }
    private Color color(Palette palette,String role) {
        String[] components = palette.rgb(role).split(",");
        RGB rgb = new RGB(Integer.parseInt(components[0]),Integer.parseInt(components[1]),Integer.parseInt(components[2]));
        return previewColors.computeIfAbsent(rgb,key -> new Color(profiles.getDisplay(),key));
    }
    private void updatePreviewStyling() {
        if (fontPreview == null || fontPreview.isDisposed()) return;
        Palette palette = selectedProfile() == null || drafts == null ? null : drafts.resolve(selectedProfile());
        if (palette == null) {
            fontPreview.setStyleRanges(new StyleRange[0]);
            fontPreview.setBackground(fontPreview.getParent().getBackground()); fontPreview.setForeground(fontPreview.getParent().getForeground());
            fontPreview.setSelectionBackground(null); fontPreview.setSelectionForeground(null);
            fontFamily.setBackground(fontFamily.getParent().getBackground()); fontFamily.setForeground(fontFamily.getParent().getForeground());
            fontFamily.setSelectionColors(fontFamily.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION),fontFamily.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
            profiles.setBackground(profiles.getParent().getBackground()); profiles.setForeground(profiles.getParent().getForeground());
            profiles.setSelectionColors(profiles.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION),profiles.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
            return;
        }
        fontPreview.setBackground(color(palette,"editor.background")); fontPreview.setForeground(color(palette,"text"));
        fontPreview.setSelectionBackground(color(palette,"selection.background")); fontPreview.setSelectionForeground(color(palette,"selection.text"));
        fontFamily.setBackground(color(palette,"panel")); fontFamily.setForeground(color(palette,"text"));
        fontFamily.setSelectionColors(color(palette,"selection.background"),color(palette,"selection.text"));
        profiles.setBackground(color(palette,"panel")); profiles.setForeground(color(palette,"text"));
        profiles.setSelectionColors(color(palette,"selection.background"),color(palette,"selection.text"));
        StyleRange[] styles = bsl.getSelection() ? CodePreview.spans().stream().map(span -> new StyleRange(span.offset(),span.length(),color(palette,span.role()),null,span.bold() ? SWT.BOLD : SWT.NORMAL)).toArray(StyleRange[]::new) : new StyleRange[0];
        fontPreview.setStyleRanges(styles);
    }
    private void applyFont() throws Exception {
        boolean modified = fontReset || fontDirty;
        if (fontReset) fonts.restore(); else if (fontDirty) fonts.apply(pendingFont);
        if (fontReset || fontDirty) showFont(fonts.currentFont());
        restoreFont.setEnabled(fonts != null && fonts.hasOriginal());
        if (modified) setMessage("Шрифт кода применён.");
    }
    @Override public void dispose() {
        if (profiles != null && !profiles.isDisposed()) profiles.closePopup();
        if (fontFamily != null && !fontFamily.isDisposed()) fontFamily.closePopup();
        if (previewFont != null) { previewFont.dispose(); previewFont = null; }
        previewColors.values().forEach(Color::dispose); previewColors.clear(); super.dispose();
    }
    private boolean applyProfile() {
        if (coordinator == null || paletteEditor == null || !paletteEditor.valid()) return false;
        try {
            if (profiles.getSelectionIndex() == 0) {
                // An icons/font-only Apply on native appearance must not reload CSS
                // or materialize previously absent native theme preferences.
                if(coordinator.hasSnapshot() || PaletteCatalog.ids().stream().anyMatch(id -> !drafts.touched(id).isEmpty()))
                    coordinator.apply(null,bsl.getSelection(),drafts);
                applyFont(); ChromaIcons.setEnabled(chromaIcons.getSelection()); restore.setEnabled(coordinator.hasSnapshot() || ChromaIcons.isEnabled()); updatePreviewStyling(); setErrorMessage(null); return true;
            }
            coordinator.apply(selectedProfile(),bsl.getSelection(),drafts);
            updatePreviewStyling();
            applyFont();
            ChromaIcons.setEnabled(chromaIcons.getSelection());
            restore.setEnabled(coordinator.hasSnapshot() || ChromaIcons.isEnabled());
            setErrorMessage(null); setMessage("Профиль применён. При необходимости перезапустите EDT."); return true;
        } catch (Exception e) { setErrorMessage(e.getMessage()); ThemeStartup.report(e); return false; }
    }
    @Override protected void performApply() { applyProfile(); }
    @Override public boolean performOk() { return applyProfile(); }
}
