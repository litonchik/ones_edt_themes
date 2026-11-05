package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import ru.litonchik.edt.themes.model.*;

public class NoctisLuxTest {
    @Test public void lightHasItsOwnIdentityAndUpstreamColors() {
        Palette p = PaletteCatalog.load("noctis-lux");
        assertEquals(ThemeMode.LIGHT,PaletteCatalog.mode(p.id()));
        assertEquals(19,PaletteCatalog.profiles().size());
        assertEquals(17,p.colors().size());
        assertEquals("#FEF8EC",p.color("editor.background"));
        assertEquals("#F0E7D1",p.color("border"));
        assertEquals("#B6E1E7",p.color("selection.background"));
        assertEquals("#DAEEEE",p.color("line"));
        assertEquals("#005661",p.color("text"));
        assertEquals("#5B6B6B",p.color("comment"));
        assertEquals("#007846",p.color("string"));
        assertEquals("#5842FF",p.color("number"));
        assertEquals("#B33D67",p.color("keyword"));
    }
    @Test public void personalNoctisLuxDoesNotOverwriteAnyExistingPalette() {
        var saved = new LinkedHashMap<String,Map<String,String>>();
        for(String id:PaletteCatalog.ids()) if(!id.equals("noctis-lux")) saved.put(id,Map.of("comment","#123456"));
        var drafts = new PaletteDrafts(saved);
        drafts.edit("noctis-lux","keyword","#234567");
        for(String id:saved.keySet()) assertEquals(saved.get(id),drafts.overrides(id));
        assertEquals("#234567",drafts.resolve("noctis-lux").color("keyword"));
    }
    @Test public void lightPlanUsesNativeLightScopesAndCanReleaseOnExternalDark() {
        var p = PaletteCatalog.load("noctis-lux");
        var plan = SettingCatalog.load().plan(p,true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("0,86,97"),plan.get(new SettingKey("instance","org.eclipse.ui.workbench","com._1c.g5.v8.dt.theming.ui.FOREGROUND_COLOR")));
        assertTrue(plan.keySet().stream().noneMatch(k -> k.key().startsWith(ThemeMode.DARK.cssId()+".")));
        var state = new ProfileState(p.id(),true);
        assertFalse(state.onThemeChanged(ThemeMode.LIGHT.cssId()));
        assertTrue(state.onThemeChanged(ThemeMode.DARK.cssId()));
    }
    @Test public void adaptedTextRemainsReadableOnLineAndHoverAndSelection() {
        var p = PaletteCatalog.load("noctis-lux");
        for(String role:List.of("text","comment","keyword","string","number","directive","accent","disabled"))
            for(String surface:List.of("editor.background","line","hover")) assertTrue(role+" on "+surface,contrast(p.color(role),p.color(surface))>=4.5);
        assertTrue(contrast(p.color("selection.text"),p.color("selection.background"))>=4.5);
        assertTrue("selection must differ from current line",contrast(p.color("selection.background"),p.color("line"))>=1.15);
        assertTrue(contrast(p.color("error"),p.color("panel"))>=4.5);
        assertTrue(contrast(p.color("warning"),p.color("panel"))>=4.5);
    }
    private double contrast(String a,String b) { double x=luminance(a),y=luminance(b); return (Math.max(x,y)+.05)/(Math.min(x,y)+.05); }
    private double luminance(String hex) {
        double[] c=new double[3];
        for(int i=0;i<3;i++) { double x=Integer.parseInt(hex.substring(1+2*i,3+2*i),16)/255.; c[i]=x<=.04045 ? x/12.92 : Math.pow((x+.055)/1.055,2.4); }
        return .2126*c[0]+.7152*c[1]+.0722*c[2];
    }
}
