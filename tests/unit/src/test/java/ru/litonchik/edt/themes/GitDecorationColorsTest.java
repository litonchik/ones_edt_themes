package ru.litonchik.edt.themes;
import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import ru.litonchik.edt.themes.model.*;

public class GitDecorationColorsTest {
    private SettingKey key(String name) { return new SettingKey("instance","org.eclipse.ui.workbench","org.eclipse.egit.ui."+name); }
    @Test public void everyPaletteSetsModifiedAndIgnoredLabelColorsWithoutBsl() {
        var catalog=SettingCatalog.load();
        for(var profile:PaletteCatalog.profiles()) {
            var palette=PaletteCatalog.load(profile.id()); var plan=catalog.plan(palette,false,"org.eclipse.ui.defaultTheme");
            for(String prefix:List.of("UncommittedChange","IgnoredResource"))
                assertEquals(profile.id(),OriginalValue.of(palette.rgb("editor.background")),plan.get(key(prefix+"BackgroundColor")));
            assertEquals(OriginalValue.of(palette.rgb("accent")),plan.get(key("UncommittedChangeForegroundColor")));
            assertEquals(OriginalValue.of(palette.rgb("disabled")),plan.get(key("IgnoredResourceForegroundColor")));
        }
    }
    @Test public void gitColorDefinitionsUsePlainKeysInLightAndDark() {
        for(var mode:ThemeMode.values()) for(String name:List.of("UncommittedChangeBackgroundColor","UncommittedChangeForegroundColor","IgnoredResourceBackgroundColor","IgnoredResourceForegroundColor")) {
            String id="org.eclipse.egit.ui."+name;
            assertEquals(id,SettingCatalog.colorPreferenceKey(id,"org.eclipse.ui.defaultTheme",mode));
            assertEquals("custom."+id,SettingCatalog.colorPreferenceKey(id,"custom",mode));
        }
    }
    @Test public void personalRoleEditsIncludeGitColorsButKeepOtherTargetsSeparate() {
        var catalog=SettingCatalog.load();
        for(var mode:ThemeMode.values()) {
            var backgrounds=catalog.roleTargets(Set.of("editor.background"),false,"org.eclipse.ui.defaultTheme",mode);
            assertTrue(backgrounds.contains(key("UncommittedChangeBackgroundColor")));
            assertTrue(backgrounds.contains(key("IgnoredResourceBackgroundColor")));
            assertFalse(backgrounds.contains(key("UncommittedChangeForegroundColor")));
            assertTrue(catalog.roleTargets(Set.of("accent"),false,"org.eclipse.ui.defaultTheme",mode).contains(key("UncommittedChangeForegroundColor")));
            assertTrue(catalog.roleTargets(Set.of("disabled"),false,"org.eclipse.ui.defaultTheme",mode).contains(key("IgnoredResourceForegroundColor")));
        }
    }
}
