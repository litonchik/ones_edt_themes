package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import ru.litonchik.edt.themes.model.*;

public class PaletteCoverageTest {
    @Test public void queryColorsAndNativeDefaultsCoverBothLanguages() {
        var plan=SettingCatalog.load().plan(PaletteCatalog.load("visual-studio-dark-cpp"),false,"org.eclipse.ui.defaultTheme");
        var nativeColors=NativeSettingsCatalog.load();
        for(var language:new String[][]{{"com._1c.g5.v8.dt.ql.ui","com._1c.g5.v8.dt.ql.Ql"},{"com._1c.g5.v8.dt.ql.dcs.ui","com._1c.g5.v8.dt.ql.dcs.QlDcs"}}) {
            var keyword=new SettingKey("instance",language[0],language[1]+".syntaxColorer.tokenStyles.Ql_Keywords.color");
            assertEquals(OriginalValue.of("86,156,214"),plan.get(keyword));
            assertEquals(OriginalValue.of("160,202,244"),nativeColors.get(keyword));
        }
    }
    @Test public void visualStudioDarkMapsCppColorsToNativeBslTargets() {
        var palette = PaletteCatalog.load("visual-studio-dark-cpp");
        var plan = SettingCatalog.load().plan(palette,true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("30,30,30"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Background")));
        assertEquals(OriginalValue.of("86,156,214"),plan.get(new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.BSL_Keywords.color")));
        assertEquals(OriginalValue.of("58,61,65"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.SelectionBackground")));
        assertEquals(OriginalValue.of("58,61,65"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","currentLineColor")));
        assertEquals(OriginalValue.of("48,48,49"),plan.get(new SettingKey("instance","org.eclipse.ui.workbench","org.eclipse.e4.ui.css.theme.e4_dark.com._1c.g5.v8.dt.theming.ui.INACTIVE_PART_BACKGROUND_COLOR")));
        assertEquals("#B5CEA8",palette.color("number"));
    }
    @Test public void noctisMinimusMapsAdaptedNumberAndDistinctWorkbenchPanel() {
        var palette = PaletteCatalog.load("noctis-minimus");
        var plan = SettingCatalog.load().plan(palette,true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("27,41,50"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Background")));
        assertEquals(OriginalValue.of("200,141,162"),plan.get(new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.BSL_Keywords.color")));
        assertEquals(OriginalValue.of("42,64,77"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.SelectionBackground")));
        assertEquals(OriginalValue.of("29,52,67"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","currentLineColor")));
        assertEquals(OriginalValue.of("16,18,19"),plan.get(new SettingKey("instance","org.eclipse.ui.workbench","org.eclipse.e4.ui.css.theme.e4_dark.com._1c.g5.v8.dt.theming.ui.INACTIVE_PART_BACKGROUND_COLOR")));
        assertEquals("#8E87C5",palette.color("number"));
    }
    @Test public void shadesOfPurpleMapsSourceColorsAndOpaqueSelection() {
        var palette = PaletteCatalog.load("shades-of-purple");
        var plan = SettingCatalog.load().plan(palette,true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("45,43,85"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Background")));
        assertEquals(OriginalValue.of("255,157,0"),plan.get(new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.BSL_Keywords.color")));
        assertEquals(OriginalValue.of("116,72,176"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.SelectionBackground")));
        assertEquals(OriginalValue.of("31,31,65"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","currentLineColor")));
        assertEquals(OriginalValue.of("34,34,68"),plan.get(new SettingKey("instance","org.eclipse.ui.workbench","org.eclipse.e4.ui.css.theme.e4_dark.com._1c.g5.v8.dt.theming.ui.INACTIVE_PART_BACKGROUND_COLOR")));
        assertEquals("#E43938",palette.color("error"));
    }
    @Test public void ayuBorderedMapsSourceColorsAndDistinctWorkbenchPanel() {
        var palette = PaletteCatalog.load("ayu-dark");
        var plan = SettingCatalog.load().plan(palette,true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("16,20,28"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Background")));
        assertEquals(OriginalValue.of("255,143,64"),plan.get(new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.BSL_Keywords.color")));
        assertEquals(OriginalValue.of("25,49,85"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.SelectionBackground")));
        assertEquals(OriginalValue.of("22,26,36"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","currentLineColor")));
        assertEquals(OriginalValue.of("27,31,41"),plan.get(new SettingKey("instance","org.eclipse.ui.workbench","org.eclipse.e4.ui.css.theme.e4_dark.com._1c.g5.v8.dt.theming.ui.INACTIVE_PART_BACKGROUND_COLOR")));
        assertEquals("#262C38",palette.color("hover"));
    }
    @Test public void nightOwlMapsSourceColorsAndDistinctWorkbenchPanel() {
        var plan = SettingCatalog.load().plan(PaletteCatalog.load("night-owl"),true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("1,22,39"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Background")));
        assertEquals(OriginalValue.of("199,146,234"),plan.get(new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.BSL_Keywords.color")));
        assertEquals(OriginalValue.of("29,59,83"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.SelectionBackground")));
        assertEquals(OriginalValue.of("7,36,53"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","currentLineColor")));
        assertEquals(OriginalValue.of("14,41,63"),plan.get(new SettingKey("instance","org.eclipse.ui.workbench","org.eclipse.e4.ui.css.theme.e4_dark.com._1c.g5.v8.dt.theming.ui.INACTIVE_PART_BACKGROUND_COLOR")));
    }
    @Test public void winterSeparatorsUseDistinctPanelWithoutChangingEditorOrOtherThemes() {
        var palette = PaletteCatalog.load("winter-is-coming-dark-blue");
        var plan = SettingCatalog.load().plan(palette,true,"org.eclipse.ui.defaultTheme");
        assertEquals("#011627",palette.color("editor.background"));
        assertEquals("#0E293F",palette.color("panel"));
        assertEquals(OriginalValue.of("14,41,63"),plan.get(new SettingKey("instance","org.eclipse.ui.workbench","org.eclipse.e4.ui.css.theme.e4_dark.com._1c.g5.v8.dt.theming.ui.INACTIVE_PART_BACKGROUND_COLOR")));
        assertEquals("#161B22",PaletteCatalog.load("github-dark-default").color("panel"));
    }
    @Test public void winterDarkBlueUsesSourceColorsAndCompositedLine() {
        var plan = SettingCatalog.load().plan(PaletteCatalog.load("winter-is-coming-dark-blue"),true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("1,22,39"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Background")));
        assertEquals(OriginalValue.of("167,219,247"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Foreground")));
        assertEquals(OriginalValue.of("0,191,249"),plan.get(new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.BSL_Keywords.color")));
        assertEquals(OriginalValue.of("16,51,98"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.SelectionBackground")));
        assertEquals(OriginalValue.of("4,36,68"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","currentLineColor")));
    }
    @Test public void githubDarkDefaultUsesResolvedOverridesAndOpaqueSelection() {
        var plan = SettingCatalog.load().plan(PaletteCatalog.load("github-dark-default"),true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("13,17,23"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Background")));
        assertEquals(OriginalValue.of("230,237,243"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Foreground")));
        assertEquals(OriginalValue.of("255,123,114"),plan.get(new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.BSL_Keywords.color")));
        assertEquals(OriginalValue.of("20,39,68"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.SelectionBackground")));
        assertEquals(OriginalValue.of("23,27,34"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","currentLineColor")));
    }
    @Test public void draculaUsesItsOwnNativeTargetsRatherThanDarcula() {
        var plan = SettingCatalog.load().plan(PaletteCatalog.load("dracula"),true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("40,42,54"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Background")));
        assertEquals(OriginalValue.of("255,121,198"),plan.get(new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.BSL_Keywords.color")));
        assertEquals(OriginalValue.of("68,71,90"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.SelectionBackground")));
        assertEquals(OriginalValue.of("248,248,242"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.SelectionForeground")));
        assertEquals(OriginalValue.of("53,55,71"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","currentLineColor")));
        assertNotEquals(plan,SettingCatalog.load().plan(PaletteCatalog.load("darcula"),true,"org.eclipse.ui.defaultTheme"));
    }
    @Test public void oneDarkProReachesNativeEditorBslAndSelectionTargets() {
        var plan = SettingCatalog.load().plan(PaletteCatalog.load("one-dark-pro"),true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("40,44,52"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Background")));
        assertEquals(OriginalValue.of("198,120,221"),plan.get(new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.BSL_Keywords.color")));
        assertEquals(OriginalValue.of("64,72,89"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.SelectionBackground")));
        assertEquals(OriginalValue.of("215,218,224"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.SelectionForeground")));
    }
    @Test public void paletteRolesAgreeAndSelectionIsReadable() {
        for (String id : PaletteDrafts.PROFILES) {
            Palette p = PaletteCatalog.load(id);
            assertEquals(Palette.REQUIRED,p.colors().keySet());
            assertNotEquals(p.color("selection.text"),p.color("selection.background"));
        }
    }
    @Test public void mapContainsVerifiedEditorAndBslTargets() {
        var plan = SettingCatalog.load().plan(PaletteCatalog.load("darcula"),true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("43,43,43"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Background")));
        assertEquals(OriginalValue.of("204,120,50"),plan.get(new SettingKey("instance","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.bsl.Bsl.syntaxColorer.tokenStyles.BSL_Keywords.color")));
        assertEquals(OriginalValue.of("false"),plan.get(new SettingKey("instance","org.eclipse.ui.editors","AbstractTextEditor.Color.Background.SystemDefault")));
    }
    @Test public void targetsContainOnlyColorsAndSystemDefaultFlags() {
        for (var entry : SettingCatalog.load().entries()) {
            assertTrue(entry.key(), entry.kind().equals("color") || entry.key().endsWith(".color") || entry.key().endsWith("_COLOR") || entry.key().contains("Color"));
            assertFalse(entry.key().toLowerCase(Locale.ROOT).matches(".*(font|keybinding|formatter|encoding|modeldata).*"));
        }
    }
    @Test public void bslCheckboxDropsOnlyBslTargets() {
        var catalog = SettingCatalog.load(); Palette p = PaletteCatalog.load("monokai");
        var all = catalog.plan(p,true,"org.eclipse.ui.defaultTheme");
        var noBsl = catalog.plan(p,false,"org.eclipse.ui.defaultTheme");
        assertTrue(all.size() > noBsl.size());
        for (var e : all.entrySet()) {
            if (!e.getKey().node().equals("com._1c.g5.v8.dt.bsl.ui")) assertEquals(e.getValue(),noBsl.get(e.getKey()));
        }
    }
    @Test public void inputBackgroundSupportsNativeBslStyledTextRule() {
        var plan = SettingCatalog.load().plan(PaletteCatalog.load("darcula"),true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("43,43,43"),plan.get(new SettingKey("instance","org.eclipse.ui.workbench","org.eclipse.e4.ui.css.theme.e4_dark.com._1c.g5.v8.dt.theming.ui.CONTROLS_BACKGROUND_COLOR")));
    }
}
