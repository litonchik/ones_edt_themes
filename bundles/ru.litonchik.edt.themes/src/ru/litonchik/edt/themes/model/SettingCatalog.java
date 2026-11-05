package ru.litonchik.edt.themes.model;

import java.io.*;
import java.util.*;

public final class SettingCatalog {
    public record Entry(String kind, String node, String key, String role) {}
    private final List<Entry> entries;
    private SettingCatalog(List<Entry> entries) { this.entries = List.copyOf(entries); }
    public List<Entry> entries() { return entries; }
    public static SettingCatalog load() {
        try (InputStream stream = SettingCatalog.class.getResourceAsStream("/resources/edt-2026.1-settings.properties")) {
            if (stream == null) throw new IllegalArgumentException("Нет карты EDT");
            Properties p = new Properties(); p.load(stream);
            List<Entry> entries = new ArrayList<>(); Set<String> addresses = new HashSet<>();
            for (String id : new TreeSet<>(p.stringPropertyNames())) {
                String[] fields = p.getProperty(id).split("\\|",-1);
                if (fields.length != 4 || !Set.of("color","preference","flag").contains(fields[0])) throw new IllegalArgumentException("Некорректная карта: " + id);
                Entry e = new Entry(fields[0],fields[1],fields[2],fields[3]);
                if (!addresses.add(e.node()+"/"+e.key())) throw new IllegalArgumentException("Повторная настройка: " + id);
                if (!e.kind().equals("flag") && !Palette.REQUIRED.contains(e.role())) throw new IllegalArgumentException("Неизвестная роль: " + e.role());
                if (e.kind().equals("flag") && (!e.key().endsWith(".SystemDefault") || !e.role().equals("false"))) throw new IllegalArgumentException("Некорректный флаг");
                entries.add(e);
            }
            return new SettingCatalog(entries);
        } catch (IOException e) { throw new IllegalArgumentException("Не удалось прочитать карту",e); }
    }
    public static String colorPreferenceKey(String colorId, String legacyTheme) {
        return colorPreferenceKey(colorId,legacyTheme,ThemeMode.DARK);
    }
    public static String colorPreferenceKey(String colorId, String legacyTheme, ThemeMode mode) {
        String legacy = "org.eclipse.ui.defaultTheme".equals(legacyTheme) ? "" : legacyTheme + ".";
        if (UNSCOPED_COLORS.contains(colorId)) return legacy + colorId;
        return (mode == ThemeMode.DARK || LIGHT_SCOPED.contains(colorId) ? mode.cssId() + "." : "") + legacy + colorId;
    }
    // EGit's CSS writes preferences directly; these ColorDefinitions are not CSS-overridden.
    private static final Set<String> UNSCOPED_COLORS = Set.of(
        "org.eclipse.egit.ui.UncommittedChangeBackgroundColor", "org.eclipse.egit.ui.UncommittedChangeForegroundColor",
        "org.eclipse.egit.ui.IgnoredResourceBackgroundColor", "org.eclipse.egit.ui.IgnoredResourceForegroundColor");
    private static final Set<String> LIGHT_SCOPED = loadLightScopes();
    private static Set<String> loadLightScopes() {
        try (InputStream stream = SettingCatalog.class.getResourceAsStream("/resources/edt-2026.1-light-css-colors.properties")) {
            if (stream == null) throw new IllegalArgumentException("Нет карты светлых CSS цветов");
            Properties properties = new Properties(); properties.load(stream);
            return Set.copyOf(properties.values().stream().map(Object::toString).toList());
        } catch (IOException e) { throw new IllegalArgumentException("Не удалось прочитать карту светлых CSS цветов",e); }
    }
    /** Flags enabling a particular editor color belong to that role as well. */
    public Set<SettingKey> roleTargets(Set<String> roles, boolean bsl, String legacyTheme) {
        return roleTargets(roles,bsl,legacyTheme,ThemeMode.DARK);
    }
    public Set<SettingKey> roleTargets(Set<String> roles, boolean bsl, String legacyTheme, ThemeMode mode) {
        var keys = new LinkedHashSet<SettingKey>();
        for (Entry e : entries) {
            if (!bsl && e.node().equals("com._1c.g5.v8.dt.bsl.ui")) continue;
            String role = e.role();
            if (e.kind().equals("flag")) {
                String rgbKey = e.key().replace(".SystemDefault","");
                role = entries.stream().filter(c -> c.node().equals(e.node()) && c.key().equals(rgbKey)).map(Entry::role).findFirst().orElse("");
            }
            if (roles.contains(role)) keys.add(new SettingKey("instance",e.node(),e.kind().equals("color") ? colorPreferenceKey(e.key(),legacyTheme,mode) : e.key()));
        }
        return Set.copyOf(keys);
    }
    public Map<SettingKey,OriginalValue> plan(Palette palette, boolean bsl, String legacyTheme) {
        Map<SettingKey,OriginalValue> plan = new LinkedHashMap<>();
        for (Entry e : entries) {
            if (!bsl && e.node().equals("com._1c.g5.v8.dt.bsl.ui")) continue;
            String key = e.kind().equals("color") ? colorPreferenceKey(e.key(),legacyTheme,PaletteCatalog.mode(palette.id())) : e.key();
            plan.put(new SettingKey("instance",e.node(),key),OriginalValue.of(e.kind().equals("flag") ? e.role() : palette.rgb(e.role())));
        }
        return plan;
    }
}
