package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import ru.litonchik.edt.themes.model.*;

public class LightThemeTest {
    @Test public void existingProfilesRemainDarkAndLightHasIndependentIdentity() {
        assertEquals(11,PaletteCatalog.profiles(ThemeMode.DARK).size());
        assertEquals(List.of("github-light-default","winter-is-coming-light","night-owl-light","ayu-light","noctis-lux","visual-studio-light-cpp","noctis-hibernus","noctis-lilac"),PaletteCatalog.profiles(ThemeMode.LIGHT).stream().map(PaletteCatalog.Profile::id).toList());
        Palette light = PaletteCatalog.load("github-light-default");
        assertEquals("#FFFFFF",light.color("editor.background"));
        assertEquals("#656D76",light.color("comment"));
        assertEquals("#CEE1F8",light.color("selection.background"));
        assertEquals(17,light.colors().size());
    }
    @Test public void savedLightSurvivesItsNativeModeAndReleasesOnExternalDark() {
        var state = new ProfileState("github-light-default",true);
        assertFalse(state.onThemeChanged(ThemeMode.LIGHT.cssId()));
        assertEquals("github-light-default",state.activeProfile());
        assertTrue(state.onThemeChanged(ThemeMode.DARK.cssId()));
        assertNull(state.activeProfile());
    }
    @Test public void lightRegistryUsesExactCssAndNativePreferenceScopes() {
        var catalog = SettingCatalog.load();
        var plan = catalog.plan(PaletteCatalog.load("github-light-default"),true,"org.eclipse.ui.defaultTheme");
        String node = "org.eclipse.ui.workbench";
        assertEquals(OriginalValue.of("31,35,40"),plan.get(new SettingKey("instance",node,"com._1c.g5.v8.dt.theming.ui.FOREGROUND_COLOR")));
        assertEquals(OriginalValue.of("246,248,250"),plan.get(new SettingKey("instance",node,ThemeMode.LIGHT.cssId()+".com._1c.g5.v8.dt.theming.ui.BACKGROUND_COLOR_ACCENT")));
        assertFalse(plan.keySet().stream().anyMatch(k -> k.key().startsWith(ThemeMode.DARK.cssId()+".")));
        assertEquals("custom.CONTENT_ASSIST_BACKGROUND_COLOR",SettingCatalog.colorPreferenceKey("CONTENT_ASSIST_BACKGROUND_COLOR","custom",ThemeMode.LIGHT));
        var targets = catalog.roleTargets(Set.of("text"),true,"org.eclipse.ui.defaultTheme",ThemeMode.LIGHT);
        assertTrue(targets.contains(new SettingKey("instance",node,"com._1c.g5.v8.dt.theming.ui.FOREGROUND_COLOR")));
    }
    @Test public void lightNeutralizesDarkOnlySeedsAndUsesNativeLightScrollbar() {
        var dark = NativeSettingsCatalog.load(); var light = NativeSettingsCatalog.load(ThemeMode.LIGHT);
        assertEquals(dark.keySet(),light.keySet());
        assertEquals(10,light.values().stream().filter(OriginalValue::present).count());
        assertEquals(OriginalValue.of("240,240,240"),light.get(new SettingKey("instance","com._1c.g5.v8.dt.theming.ui","com._1c.g5.v8.dt.theming.ui.BROWSERS_SCROLL_TRACK_COLOR")));
        assertEquals(OriginalValue.absent(),light.get(new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.Comment.color")));
    }
    @Test public void editingLightDoesNotChangeAnyOldPersonalPalette() {
        var original = new LinkedHashMap<String,Map<String,String>>();
        for (var profile : PaletteCatalog.profiles(ThemeMode.DARK)) original.put(profile.id(),Map.of("comment","#123456"));
        var drafts = new PaletteDrafts(original);
        drafts.edit("github-light-default","comment","#234567");
        assertEquals("#234567",drafts.resolve("github-light-default").color("comment"));
        for (var profile : PaletteCatalog.profiles(ThemeMode.DARK)) assertEquals(Map.of("comment","#123456"),drafts.overrides(profile.id()));
    }
    @Test public void modeSwitchReplacesKnownNativeSeedsButPreservesManualValues() throws Exception {
        var seeded = new SettingKey("instance","node","seeded");
        var manual = new SettingKey("instance","node","manual");
        var nativeDark = OriginalValue.of("23,23,23"); var nativeLight = OriginalValue.of("240,240,240");
        var custom = OriginalValue.of("11,22,33");
        SettingAccess access = new SettingAccess() {
            public OriginalValue read(SettingKey key) { return key.equals(seeded) ? nativeDark : custom; }
            public void write(SettingKey key,OriginalValue value) {}
            public void flush() {}
        };
        SnapshotStore store = new SnapshotStore() {
            public SettingsSnapshot load() { return SettingsSnapshot.empty(); }
            public void save(SettingsSnapshot value) {}
        };
        var service = new SnapshotService(access,store);
        var plan = service.withNativeDefaults(Map.of(),Map.of(seeded,nativeLight,manual,nativeLight),Map.of(seeded,nativeDark,manual,nativeDark));
        assertEquals(nativeLight,plan.get(seeded)); assertEquals(custom,plan.get(manual));
    }
}
