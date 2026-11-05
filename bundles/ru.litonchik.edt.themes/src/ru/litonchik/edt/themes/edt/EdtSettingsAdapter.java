package ru.litonchik.edt.themes.edt;

import java.util.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.ui.themes.IThemeManager;
import org.eclipse.ui.PlatformUI;
import ru.litonchik.edt.themes.model.*;

/** Only public APIs. CSS engine reloads named colors from scoped overrides. */
public final class EdtSettingsAdapter implements SettingAccess {
    public static final String CSS_NODE = "org.eclipse.e4.ui.css.swt.theme";
    public static final SettingKey CSS = new SettingKey("theme","css","id");
    public static final SettingKey LEGACY = new SettingKey("theme","legacy","id");
    private final IThemeEngine engine;
    private final IThemeManager themes;
    private final SettingCatalog catalog = SettingCatalog.load();
    private final Map<SettingKey,OriginalValue> touched = new LinkedHashMap<>();
    public EdtSettingsAdapter(IThemeEngine engine, IThemeManager themes) { this.engine = engine; this.themes = themes; }
    public Map<SettingKey,OriginalValue> plan(Palette palette, boolean includeBsl) {
        var desired = new LinkedHashMap<SettingKey,OriginalValue>();
        String cssId = PaletteCatalog.mode(palette.id()).cssId();
        desired.put(CSS,OriginalValue.of(cssId));
        desired.put(LEGACY,OriginalValue.of(themes.getCurrentTheme().getId()));
        desired.putAll(catalog.plan(palette,includeBsl,themes.getCurrentTheme().getId()));
        // Native setter persists this key; capture its presence separately from the effective CSS ID.
        desired.put(new SettingKey("instance",CSS_NODE,"themeid"),OriginalValue.of(cssId));
        return desired;
    }
    @Override public OriginalValue read(SettingKey key) {
        if (key.equals(CSS)) {
            if (engine.getActiveTheme() == null) throw new IllegalStateException("CSS theme engine ещё не инициализирован");
            return OriginalValue.of(engine.getActiveTheme().getId());
        }
        if (key.equals(LEGACY)) return OriginalValue.of(themes.getCurrentTheme().getId());
        String value = node(key).get(key.key(),null);
        return value == null ? OriginalValue.absent() : OriginalValue.of(value);
    }
    @Override public void write(SettingKey key, OriginalValue value) {
        if (key.equals(CSS)) {
            LightUiStyles.prepare(engine);
            if (!value.present() || engine.getThemes().stream().noneMatch(t -> t.getId().equals(value.value()))) throw new IllegalArgumentException("Неизвестная CSS-тема");
            engine.setTheme(value.value(),true);
            // Same-ID call settles Eclipse's previousThemeId/currentThemeId before preference writes.
            engine.setTheme(value.value(),true);
        } else if (key.equals(LEGACY)) {
            if (!value.present()) throw new IllegalArgumentException("Отсутствующий legacy ID");
            themes.setCurrentTheme(value.value());
        } else {
            put(key,value); touched.put(key,value);
        }
    }
    private IEclipsePreferences node(SettingKey key) {
        if (!key.scope().equals("instance")) throw new IllegalArgumentException("Неподдерживаемый scope: " + key.scope());
        return InstanceScope.INSTANCE.getNode(key.node());
    }
    private void put(SettingKey key, OriginalValue value) {
        if (value.present()) node(key).put(key.key(),value.value()); else node(key).remove(key.key());
    }
    /** Restore factory defaults owned by the profile and reseed actual Light registry RGBs.
     * Native Dark-created definitions without a Light CSS default can retain Eclipse's
     * EMPTY_COLOR_VALUE even when the exact explicit preference was reasserted unchanged.
     * Read the current explicit value, including a later manual edit, without taking ownership.
     */
    public void restoreLightRegistry(Map<SettingKey,OriginalValue> applied) throws Exception {
        Set<String> factoryColors = Set.of("org.eclipse.ui.workbench.HOVER_BACKGROUND","org.eclipse.ui.workbench.HOVER_FOREGROUND",
                "org.eclipse.ui.workbench.FORM_HEADING_ERROR_COLOR","org.eclipse.ui.workbench.FORM_HEADING_WARNING_COLOR");
        var registry = themes.getCurrentTheme().getColorRegistry();
        for (var entry : catalog.entries()) if (entry.kind().equals("color")) {
            String id = entry.key();
            SettingKey key = new SettingKey("instance",entry.node(),SettingCatalog.colorPreferenceKey(id,themes.getCurrentTheme().getId(),ThemeMode.LIGHT));
            OriginalValue saved = applied.get(key);
            OriginalValue current = read(key);
            if (saved != null && saved.present() && factoryColors.contains(id) && !current.present()) { put(key,saved); current = saved; }
            if (current.present()) {
                String[] rgb = current.value().split(",");
                registry.put(id,new org.eclipse.swt.graphics.RGB(Integer.parseInt(rgb[0]),Integer.parseInt(rgb[1]),Integer.parseInt(rgb[2])));
            }
        }
        InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").flush();
    }
    @Override public void flush() throws Exception {
        LightUiStyles.prepare(engine);
        Set<String> nodes = new LinkedHashSet<>(); touched.keySet().forEach(k -> nodes.add(k.node()));
        for (String id : nodes) InstanceScope.INSTANCE.getNode(id).flush();
        if (engine.getActiveTheme() != null) {
            var current = engine.getActiveTheme();
            engine.setTheme(current,true);
            // Eclipse 4.30 caches converted named CSS colors. Public same-ID reapply does not clear them.
            // A transient native theme transition rebuilds CSS resources without persisting another ID.
            String transientId = current.getId().equals(ProfileState.DARK)
                    ? "org.eclipse.e4.ui.css.theme.e4_default" : ProfileState.DARK;
            engine.setTheme(transientId,false);
            engine.setTheme(current,false);
        }
        // Apply to existing windows before restoring exact explicit preference states.
        for (var window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
            if (!window.getShell().isDisposed()) engine.applyStyles(window.getShell(),true);
        }
        // A native theme refresh may seed absent editor keys. Restore the exact explicit state after styling.
        for (var entry : touched.entrySet()) put(entry.getKey(),entry.getValue());
        for (String id : nodes) InstanceScope.INSTANCE.getNode(id).flush();
        EditorRulerColors.refresh();
        // Xtext's lexical presentation may retain old token attributes after native CSS rewrites.
        // Refresh through the public text viewer adapter, keeping documents and undo history intact.
        for (var window : PlatformUI.getWorkbench().getWorkbenchWindows()) for (var page : window.getPages()) {
            for (var reference : page.getEditorReferences()) {
                var editor = reference.getEditor(false);
                if (editor == null) continue;
                var target = editor.getAdapter(org.eclipse.jface.text.ITextOperationTarget.class);
                if (target instanceof org.eclipse.jface.text.ITextViewer viewer) {
                    viewer.invalidateTextPresentation();
                    XtextPresentationRefresh.refresh(viewer);
                }
            }
        }
        String active = InstanceScope.INSTANCE.getNode("ru.litonchik.edt.themes").get("activeProfile",null);
        if (engine.getActiveTheme() != null && ThemeMode.LIGHT.cssId().equals(engine.getActiveTheme().getId())
                && PaletteCatalog.isKnown(active) && PaletteCatalog.mode(active) == ThemeMode.LIGHT)
            restoreLightRegistry(touched);
        LightUiStyles.refresh(engine);
        touched.clear();
    }
}
