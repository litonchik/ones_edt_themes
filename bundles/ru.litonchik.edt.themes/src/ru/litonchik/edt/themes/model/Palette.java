package ru.litonchik.edt.themes.model;

import java.util.Map;
import java.util.Set;

public record Palette(String id, Map<String,String> colors) {
    public static final Set<String> REQUIRED = Set.of("editor.background", "text", "panel", "border", "selection.background", "selection.text", "line", "comment", "keyword", "string", "number", "directive", "accent", "error", "warning", "disabled", "hover");
    public Palette {
        if (id == null || id.isBlank() || colors == null || !colors.keySet().containsAll(REQUIRED))
            throw new IllegalArgumentException("Неполная палитра: " + id);
        colors = Map.copyOf(colors);
        for (String color : colors.values())
            if (!color.matches("#[0-9A-Fa-f]{6}")) throw new IllegalArgumentException("Некорректный RGB: " + color);
    }
    public String color(String role) {
        String value = colors.get(role);
        if (value == null) throw new IllegalArgumentException("Нет роли: " + role);
        return value;
    }
    public String rgb(String role) {
        String hex = color(role);
        return Integer.parseInt(hex.substring(1,3),16) + "," + Integer.parseInt(hex.substring(3,5),16) + "," + Integer.parseInt(hex.substring(5,7),16);
    }
}
