package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import ru.litonchik.edt.themes.model.*;

public class PaletteDraftsTest {
    @Test public void visualStudioResetKeepsAllTenPreviousPersonalPalettes() {
        var saved = new LinkedHashMap<String,Map<String,String>>();
        for (String id : PaletteCatalog.ids()) if (!id.equals("visual-studio-dark-cpp")) saved.put(id,Map.of("comment","#123456"));
        saved.put("visual-studio-dark-cpp",Map.of("keyword","#ABCDEF"));
        var drafts = new PaletteDrafts(saved);
        assertEquals("#ABCDEF",drafts.resolve("visual-studio-dark-cpp").color("keyword"));
        drafts.resetPalette("visual-studio-dark-cpp");
        assertTrue(drafts.overrides("visual-studio-dark-cpp").isEmpty());
        assertEquals(Palette.REQUIRED,drafts.touched("visual-studio-dark-cpp"));
        for (String id : saved.keySet()) if (!id.equals("visual-studio-dark-cpp")) {
            assertEquals(saved.get(id),drafts.overrides(id));
            assertTrue(drafts.touched(id).isEmpty());
        }
    }
    @Test public void noctisResetKeepsAllNinePreviousPersonalPalettes() {
        var saved = new LinkedHashMap<String,Map<String,String>>();
        for (String id : PaletteCatalog.ids()) if (!id.equals("noctis-minimus")) saved.put(id,Map.of("comment","#123456"));
        saved.put("noctis-minimus",Map.of("keyword","#ABCDEF"));
        var drafts = new PaletteDrafts(saved);
        assertEquals("#ABCDEF",drafts.resolve("noctis-minimus").color("keyword"));
        drafts.resetPalette("noctis-minimus");
        assertTrue(drafts.overrides("noctis-minimus").isEmpty());
        assertEquals(Palette.REQUIRED,drafts.touched("noctis-minimus"));
        for (String id : saved.keySet()) if (!id.equals("noctis-minimus")) {
            assertEquals(saved.get(id),drafts.overrides(id));
            assertTrue(drafts.touched(id).isEmpty());
        }
    }
    @Test public void shadesResetKeepsAllEightPreviousPersonalPalettes() {
        var saved = new LinkedHashMap<String,Map<String,String>>();
        for (String id : PaletteCatalog.ids()) if (!id.equals("shades-of-purple")) saved.put(id,Map.of("comment","#123456"));
        saved.put("shades-of-purple",Map.of("keyword","#ABCDEF"));
        var drafts = new PaletteDrafts(saved);
        assertEquals("#ABCDEF",drafts.resolve("shades-of-purple").color("keyword"));
        drafts.resetPalette("shades-of-purple");
        assertTrue(drafts.overrides("shades-of-purple").isEmpty());
        assertEquals(Palette.REQUIRED,drafts.touched("shades-of-purple"));
        for (String id : saved.keySet()) if (!id.equals("shades-of-purple")) {
            assertEquals(saved.get(id),drafts.overrides(id));
            assertTrue(drafts.touched(id).isEmpty());
        }
    }
    @Test public void ayuResetKeepsAllSevenPreviousPersonalPalettes() {
        var saved = new LinkedHashMap<String,Map<String,String>>();
        for (String id : PaletteCatalog.ids()) if (!id.equals("ayu-dark")) saved.put(id,Map.of("comment","#123456"));
        saved.put("ayu-dark",Map.of("keyword","#ABCDEF"));
        var drafts = new PaletteDrafts(saved);
        assertEquals("#ABCDEF",drafts.resolve("ayu-dark").color("keyword"));
        drafts.resetPalette("ayu-dark");
        assertTrue(drafts.overrides("ayu-dark").isEmpty());
        assertEquals(Palette.REQUIRED,drafts.touched("ayu-dark"));
        for (String id : saved.keySet()) if (!id.equals("ayu-dark")) {
            assertEquals(saved.get(id),drafts.overrides(id));
            assertTrue(drafts.touched(id).isEmpty());
        }
    }
    @Test public void nightOwlResetKeepsAllSixPreviousPersonalPalettes() {
        var saved = new LinkedHashMap<String,Map<String,String>>();
        for (String id : PaletteCatalog.ids()) if (!id.equals("night-owl")) saved.put(id,Map.of("comment","#123456"));
        saved.put("night-owl",Map.of("keyword","#ABCDEF"));
        var drafts = new PaletteDrafts(saved);
        assertEquals("#ABCDEF",drafts.resolve("night-owl").color("keyword"));
        drafts.resetPalette("night-owl");
        assertTrue(drafts.overrides("night-owl").isEmpty());
        assertEquals(Palette.REQUIRED,drafts.touched("night-owl"));
        for (String id : saved.keySet()) if (!id.equals("night-owl")) {
            assertEquals(saved.get(id),drafts.overrides(id));
            assertTrue(drafts.touched(id).isEmpty());
        }
    }
    @Test public void winterResetKeepsAllFiveExistingPersonalPalettes() {
        var saved = new LinkedHashMap<String,Map<String,String>>();
        for (String id : List.of("darcula","monokai","one-dark-pro","dracula","github-dark-default"))
            saved.put(id,Map.of("comment","#123456"));
        saved.put("winter-is-coming-dark-blue",Map.of("keyword","#ABCDEF"));
        var drafts = new PaletteDrafts(saved);
        assertEquals("#ABCDEF",drafts.resolve("winter-is-coming-dark-blue").color("keyword"));
        drafts.resetPalette("winter-is-coming-dark-blue");
        assertTrue(drafts.overrides("winter-is-coming-dark-blue").isEmpty());
        assertEquals(Palette.REQUIRED,drafts.touched("winter-is-coming-dark-blue"));
        for (String id : saved.keySet()) if (!id.equals("winter-is-coming-dark-blue")) {
            assertEquals(saved.get(id),drafts.overrides(id));
            assertTrue(drafts.touched(id).isEmpty());
        }
    }
    @Test public void githubResetPreservesBothNewerPersonalPalettes() {
        var drafts = new PaletteDrafts(Map.of("dracula",Map.of("comment","#667788"),
            "one-dark-pro",Map.of("string","#445566"),"github-dark-default",Map.of("keyword","#ABCDEF")));
        assertEquals("#ABCDEF",drafts.resolve("github-dark-default").color("keyword"));
        drafts.resetPalette("github-dark-default");
        assertTrue(drafts.overrides("github-dark-default").isEmpty());
        assertEquals(Palette.REQUIRED,drafts.touched("github-dark-default"));
        assertEquals("#667788",drafts.resolve("dracula").color("comment"));
        assertEquals("#445566",drafts.resolve("one-dark-pro").color("string"));
        assertTrue(drafts.touched("dracula").isEmpty());
    }
    @Test public void draculaResetPreservesDarculaAndOneDarkProPersonalColors() {
        var drafts = new PaletteDrafts(Map.of("darcula",Map.of("keyword","#123456"),
            "one-dark-pro",Map.of("string","#445566"),"dracula",Map.of("keyword","#ABCDEF")));
        assertEquals("#ABCDEF",drafts.resolve("dracula").color("keyword"));
        drafts.edit("dracula","comment","#654321");
        assertEquals(Set.of("comment"),drafts.touched("dracula"));
        assertTrue(drafts.touched("darcula").isEmpty());
        assertTrue(drafts.touched("one-dark-pro").isEmpty());
        drafts.resetPalette("dracula");
        assertTrue(drafts.overrides("dracula").isEmpty());
        assertEquals(Palette.REQUIRED,drafts.touched("dracula"));
        assertEquals("#123456",drafts.resolve("darcula").color("keyword"));
        assertEquals("#445566",drafts.resolve("one-dark-pro").color("string"));
    }
    @Test public void thirdProfileKeepsSavedOverridesAndIndependentExplicitIntent() {
        var drafts = new PaletteDrafts(Map.of("darcula",Map.of("keyword","#123456"),
            "one-dark-pro",Map.of("comment","#ABCDEF")));
        assertEquals("#ABCDEF",drafts.resolve("one-dark-pro").color("comment"));
        assertEquals("#123456",drafts.resolve("darcula").color("keyword"));
        assertTrue(drafts.touched("one-dark-pro").isEmpty());
        drafts.edit("one-dark-pro","keyword","#654321");
        assertEquals(Set.of("keyword"),drafts.touched("one-dark-pro"));
        assertTrue(drafts.touched("darcula").isEmpty());
        assertEquals("#F92672",drafts.resolve("monokai").color("keyword"));
        drafts.resetPalette("one-dark-pro");
        assertTrue(drafts.overrides("one-dark-pro").isEmpty());
        assertEquals(Palette.REQUIRED,drafts.touched("one-dark-pro"));
        assertEquals("#123456",drafts.resolve("darcula").color("keyword"));
    }
    @Test public void editsAreIsolatedNormalizedAndDoNotMutateSavedInput() {
        var saved = Map.of("darcula",Map.of("keyword","#123456"));
        var drafts = new PaletteDrafts(saved);
        drafts.edit("darcula","keyword","#abcdef");
        assertEquals("#ABCDEF",drafts.resolve("darcula").color("keyword"));
        assertEquals("#F92672",drafts.resolve("monokai").color("keyword"));
        assertEquals("#123456",saved.get("darcula").get("keyword"));
        assertEquals(Set.of("keyword"),drafts.touched("darcula"));
        assertTrue(drafts.touched("monokai").isEmpty());
    }
    @Test public void resetsRemainExplicitEvenWithoutOverride() {
        var drafts = new PaletteDrafts(Map.of("darcula",Map.of("keyword","#123456")));
        drafts.resetRole("darcula","keyword");
        assertEquals(PaletteCatalog.load("darcula").color("keyword"),drafts.resolve("darcula").color("keyword"));
        assertTrue(drafts.overrides("darcula").isEmpty());
        drafts.resetPalette("monokai");
        assertEquals(Palette.REQUIRED,drafts.touched("monokai"));
        drafts.accept(); assertTrue(drafts.touched("monokai").isEmpty());
    }
    @Test public void invalidRolesProfilesAndHexNeverEnterDraft() {
        var drafts = new PaletteDrafts(Map.of());
        for (String invalid : List.of("#12345","123456","#GG0000","#12345678"))
            assertThrows(IllegalArgumentException.class,() -> drafts.edit("darcula","keyword",invalid));
        assertThrows(IllegalArgumentException.class,() -> drafts.edit("darcula","wrong","#123456"));
        assertThrows(IllegalArgumentException.class,() -> drafts.edit("other","keyword","#123456"));
        assertTrue(drafts.overrides("darcula").isEmpty());
    }
    @Test public void roleTargetsExcludeOtherRolesAndUncheckedBsl() {
        var catalog = SettingCatalog.load();
        var keys = catalog.roleTargets(Set.of("accent"),false,"org.eclipse.ui.defaultTheme");
        assertEquals(7,keys.size()); // UI, hyperlink flag, two QL function colors and EGit modified labels.
        assertTrue(keys.stream().noneMatch(k -> k.node().equals("com._1c.g5.v8.dt.bsl.ui")));
        assertEquals(8,catalog.roleTargets(Set.of("accent"),true,"org.eclipse.ui.defaultTheme").size());
        assertEquals(5,catalog.roleTargets(Set.of("keyword"),true,"org.eclipse.ui.defaultTheme").size());
        var noBslKeywords=catalog.roleTargets(Set.of("keyword"),false,"org.eclipse.ui.defaultTheme");
        assertEquals(4,noBslKeywords.size());
        assertTrue(noBslKeywords.stream().allMatch(k -> k.node().startsWith("com._1c.g5.v8.dt.ql.")));
    }
}
