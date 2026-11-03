package ru.litonchik.edt.themes.model;

public final class ProfileState {
    public static final String DARK = "org.eclipse.e4.ui.css.theme.e4_dark";
    private String activeProfile;
    private boolean includeBsl;
    private boolean applying;
    public ProfileState(String activeProfile, boolean includeBsl) { this.activeProfile = PaletteCatalog.isKnown(activeProfile) ? activeProfile : null; this.includeBsl = includeBsl; }
    public boolean beginApply() { if (applying) return false; applying = true; return true; }
    public void endApply() { applying = false; }
    public boolean onThemeChanged(String cssThemeId) {
        if (applying || activeProfile == null || DARK.equals(cssThemeId)) return false;
        activeProfile = null; return true;
    }
    public void activate(String profile, boolean bsl) { activeProfile = profile; includeBsl = bsl; }
    public void release() { activeProfile = null; }
    public String activeProfile() { return activeProfile; }
    public boolean includeBsl() { return includeBsl; }
}
