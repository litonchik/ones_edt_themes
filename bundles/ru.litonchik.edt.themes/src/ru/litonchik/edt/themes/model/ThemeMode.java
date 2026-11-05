package ru.litonchik.edt.themes.model;

public enum ThemeMode {
    DARK("org.eclipse.e4.ui.css.theme.e4_dark"),
    LIGHT("org.eclipse.e4.ui.css.theme.e4_default");
    private final String cssId;
    ThemeMode(String cssId) { this.cssId = cssId; }
    public String cssId() { return cssId; }
    public static ThemeMode fromCssId(String cssId) { return LIGHT.cssId.equals(cssId) ? LIGHT : DARK; }
}
