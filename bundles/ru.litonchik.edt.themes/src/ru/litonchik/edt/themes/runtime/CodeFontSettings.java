package ru.litonchik.edt.themes.runtime;

import java.util.*;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.PlatformUI;
import ru.litonchik.edt.themes.edt.EdtCompatibility;
import ru.litonchik.edt.themes.model.*;

/** Native text font, independently journalled from color profiles. Public Eclipse APIs only. */
public final class CodeFontSettings {
    private static final SettingKey BASELINE = new SettingKey("instance",SnapshotPreferences.PLUGIN_ID,"fontOriginalEffective");
    private final SettingKey fontKey;
    private final SnapshotService snapshots;
    private FontData[] fallback;

    public CodeFontSettings() throws Exception {
        EdtCompatibility.verify();
        String themeId = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getId();
        String key = "org.eclipse.ui.defaultTheme".equals(themeId) ? JFaceResources.TEXT_FONT : themeId+'.'+JFaceResources.TEXT_FONT;
        fontKey = new SettingKey("instance","org.eclipse.ui.workbench",key);
        var journal = new SnapshotPreferences("font");
        var loaded = journal.load();
        String original = InstanceScope.INSTANCE.getNode(BASELINE.node()).get(BASELINE.key(),null);
        if (original == null && loaded.pendingAfter().containsKey(BASELINE)) original = loaded.pendingAfter().get(BASELINE).value();
        fallback = original == null ? currentFont() : decode(original);
        snapshots = new SnapshotService(new SettingAccess() {
            @Override public OriginalValue read(SettingKey setting) {
                String value = InstanceScope.INSTANCE.getNode(setting.node()).get(setting.key(),null);
                return value == null ? OriginalValue.absent() : OriginalValue.of(value);
            }
            @Override public void write(SettingKey setting, OriginalValue value) {
                var node = InstanceScope.INSTANCE.getNode(setting.node());
                if (value.present()) node.put(setting.key(),value.value()); else node.remove(setting.key());
            }
            @Override public void flush() throws Exception {
                // Include keys from an earlier process, including its original legacy-theme key.
                InstanceScope.INSTANCE.getNode("org.eclipse.ui.workbench").flush();
                InstanceScope.INSTANCE.getNode(SnapshotPreferences.PLUGIN_ID).flush();
                var value = read(fontKey);
                FontData[] data = value.present() ? decode(value.value()) : copy(fallback);
                PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().put(JFaceResources.TEXT_FONT,data);
            }
        },journal);
        if (snapshots.snapshot().pending()) {
            var recovered = snapshots.recover();
            if (!recovered.successful()) throw new IllegalStateException("Не удалось восстановить журнал шрифта: "+recovered.errorSummary());
        }
    }
    public FontData[] currentFont() { return copy(JFaceResources.getFontRegistry().getFontData(JFaceResources.TEXT_FONT)); }
    public boolean hasOriginal() { return !snapshots.snapshot().original().isEmpty(); }
    public FontData[] originalFont() { return hasOriginal() ? copy(fallback) : currentFont(); }
    public void apply(FontData[] data) throws Exception {
        EdtCompatibility.verify(); validate(data);
        if (!hasOriginal()) fallback = currentFont();
        var desired = new LinkedHashMap<SettingKey,OriginalValue>();
        desired.put(fontKey,OriginalValue.of(PreferenceConverter.getStoredRepresentation(data)));
        desired.put(BASELINE,OriginalValue.of(PreferenceConverter.getStoredRepresentation(fallback)));
        snapshots.applyExplicit(desired);
    }
    public void restore() throws Exception {
        EdtCompatibility.verify();
        var result = snapshots.restore(true);
        if (!result.successful()) throw new IllegalStateException("Не удалось восстановить шрифт: "+result.errorSummary());
    }
    private static void validate(FontData[] data) {
        if (data == null || data.length == 0) throw new IllegalArgumentException("Не выбран шрифт");
        for (FontData font : data) if (font == null || font.getName().isBlank() || font.getHeight() <= 0) throw new IllegalArgumentException("Некорректный шрифт или размер");
    }
    private static FontData[] decode(String value) {
        FontData[] data = PreferenceConverter.readFontData(value); validate(data); return data;
    }
    private static FontData[] copy(FontData[] data) { return decode(PreferenceConverter.getStoredRepresentation(data)); }
}
