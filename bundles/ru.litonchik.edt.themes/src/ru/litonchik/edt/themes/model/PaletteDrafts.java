package ru.litonchik.edt.themes.model;

import java.util.*;

/** Dialog-local drafts. Only a successful Apply accepts the explicitly edited roles. */
public final class PaletteDrafts {
    public static final List<String> PROFILES = PaletteCatalog.ids();
    private final Map<String,Map<String,String>> overrides = new LinkedHashMap<>();
    private final Map<String,Set<String>> touched = new LinkedHashMap<>();
    public PaletteDrafts(Map<String,Map<String,String>> saved) {
        for (String profile : PROFILES) {
            overrides.put(profile,new LinkedHashMap<>()); touched.put(profile,new LinkedHashSet<>());
            saved.getOrDefault(profile,Map.of()).forEach((role,hex) -> edit(profile,role,hex));
        }
        accept();
    }
    private void validate(String profile,String role) {
        if (!PROFILES.contains(profile) || !Palette.REQUIRED.contains(role)) throw new IllegalArgumentException("Неизвестная палитра/роль");
    }
    public void edit(String profile,String role,String hex) {
        validate(profile,role);
        if (hex == null || !hex.matches("#[0-9A-Fa-f]{6}")) throw new IllegalArgumentException("Цвет: #RRGGBB (шесть HEX цифр)");
        String normalized = hex.toUpperCase(Locale.ROOT);
        if (normalized.equalsIgnoreCase(PaletteCatalog.load(profile).color(role))) overrides.get(profile).remove(role);
        else overrides.get(profile).put(role,normalized);
        touched.get(profile).add(role);
    }
    public void resetRole(String profile,String role) { validate(profile,role); overrides.get(profile).remove(role); touched.get(profile).add(role); }
    public void resetPalette(String profile) { for (String role : Palette.REQUIRED) resetRole(profile,role); }
    public Palette resolve(String profile) {
        var colors = new LinkedHashMap<>(PaletteCatalog.load(profile).colors());
        colors.putAll(overrides(profile)); return new Palette(profile,colors);
    }
    public Map<String,String> overrides(String profile) {
        if (!PROFILES.contains(profile)) throw new IllegalArgumentException("Неизвестная палитра");
        return Map.copyOf(overrides.get(profile));
    }
    public Set<String> touched(String profile) { return Set.copyOf(touched.get(profile)); }
    public void accept() { touched.values().forEach(Set::clear); }
}
