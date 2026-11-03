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
import org.eclipse.swt.custom.ScrolledComposite;

public final class ThemePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    private ReadableCombo profiles;
    private final java.util.List<String> profileIds = PaletteCatalog.ids();
    private Button bsl;
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
        explanation.setText("Палитры используют штатную Dark EDT. Для полного обновления редакторов может потребоваться перезапуск.");
        GridData data = new GridData(SWT.FILL,SWT.TOP,true,false); data.widthHint = 460; explanation.setLayoutData(data);
        profiles = new ReadableCombo(body);
        profiles.setItems(java.util.stream.Stream.concat(java.util.stream.Stream.of("Штатное оформление"),PaletteCatalog.profiles().stream().map(PaletteCatalog.Profile::label)).toArray(String[]::new));
        profiles.select(0);
        GridData profileData = new GridData(SWT.LEFT,SWT.CENTER,false,false); profileData.widthHint = 180; profiles.setLayoutData(profileData);
        bsl = new Button(body,SWT.CHECK); bsl.setText("Применять палитру BSL"); bsl.setSelection(true);
        restore = new Button(body,SWT.PUSH); restore.setText("Восстановить исходное оформление…");
        try {
            coordinator = ThemeStartup.coordinator();
            profiles.select(profileIds.indexOf(coordinator.activeProfile()) + 1); bsl.setSelection(coordinator.includeBsl());
            restore.setEnabled(coordinator.hasSnapshot());
            fonts = new CodeFontSettings();
            drafts = coordinator.paletteDrafts();
        } catch (Exception e) { setErrorMessage(e.getMessage()); setValid(false); profiles.setEnabled(false); bsl.setEnabled(false); restore.setEnabled(false); }
        restore.addListener(SWT.Selection,event -> {
            if (!MessageDialog.openConfirm(getShell(),"Восстановить цвета","Вернуть исходную тему и цвета? Ручные изменения после включения профиля будут сохранены.")) return;
            try {
                RestoreResult result = coordinator.restore(false);
                if (!result.successful()) throw new IllegalStateException("Ошибка восстановления: " + result.errorSummary());
                if (!result.conflicts().isEmpty() && MessageDialog.openConfirm(getShell(),"Ручные цвета сохранены","Сохранено ручных изменений: " + result.conflicts().size() + ". Принудительно вернуть первоначальные цвета вместо этих правок?")) {
                    result = coordinator.restore(true);
                    if (!result.successful()) throw new IllegalStateException("Ошибка восстановления: " + result.errorSummary());
                }
                profiles.select(0); refreshPalette(); restore.setEnabled(coordinator.hasSnapshot()); setMessage("Исходное оформление восстановлено. Личные палитры сохранены. Для редакторов может потребоваться перезапуск.");
            } catch (Exception e) { setErrorMessage(e.getMessage()); ThemeStartup.report(e); }
        });
        if (drafts != null) paletteEditor = new PaletteEditor(body,drafts,this::updatePreviewStyling,error -> { setErrorMessage(error); setValid(error == null); },this::color);
        createFontControls(body);
        profiles.addListener(SWT.Selection,event -> refreshPalette());
        bsl.addListener(SWT.Selection,event -> refreshPalette());
        refreshPalette();
        scroll.setMinSize(body.computeSize(510,SWT.DEFAULT));
        return scroll;
    }
    private String selectedProfile() {
        int index = profiles.getSelectionIndex();
        return index <= 0 ? null : profileIds.get(index - 1);
    }
    private void refreshPalette() { if (paletteEditor != null) paletteEditor.setProfile(selectedProfile(),bsl.getSelection()); updatePreviewStyling(); }
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
                coordinator.apply(null,bsl.getSelection(),drafts);
                restore.setEnabled(coordinator.hasSnapshot()); updatePreviewStyling(); applyFont(); setErrorMessage(null); return true;
            }
            coordinator.apply(selectedProfile(),bsl.getSelection(),drafts);
            updatePreviewStyling();
            applyFont();
            restore.setEnabled(coordinator.hasSnapshot());
            setErrorMessage(null); setMessage("Профиль применён. При необходимости перезапустите EDT."); return true;
        } catch (Exception e) { setErrorMessage(e.getMessage()); ThemeStartup.report(e); return false; }
    }
    @Override protected void performApply() { applyProfile(); }
    @Override public boolean performOk() { return applyProfile(); }
}
