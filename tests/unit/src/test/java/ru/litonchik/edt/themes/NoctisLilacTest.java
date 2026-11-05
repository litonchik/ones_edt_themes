package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import ru.litonchik.edt.themes.model.*;

public class NoctisLilacTest {
    @Test public void lightHasItsOwnIdentityAndUpstreamColors() {
        Palette p = PaletteCatalog.load("noctis-lilac");
        assertEquals(ThemeMode.LIGHT,PaletteCatalog.mode(p.id()));
        assertEquals(19,PaletteCatalog.profiles().size());
        assertEquals(17,p.colors().size());
        assertEquals("#F2F1F8",p.color("editor.background"));
        assertEquals("#D4D0F1",p.color("border"));
        assertEquals("#BCB6E7",p.color("selection.background"));
        assertEquals("#EDECF8",p.color("panel"));
        assertEquals("#DEDBF5",p.color("hover"));
        assertEquals(8,PaletteCatalog.profiles(ThemeMode.LIGHT).size());
        assertEquals("#E1DEF3",p.color("line"));
        assertEquals("#0C006B",p.color("text"));
        assertEquals("#615E74",p.color("comment"));
        assertEquals("#006F41",p.color("string"));
        assertEquals("#543FF2",p.color("number"));
        assertEquals("#A6395F",p.color("keyword"));
    }
    @Test public void personalNoctisLilacDoesNotOverwriteAnyExistingPalette() {
        var saved = new LinkedHashMap<String,Map<String,String>>();
        for(String id:PaletteCatalog.ids()) if(!id.equals("noctis-lilac")) saved.put(id,Map.of("comment","#123456"));
        var drafts = new PaletteDrafts(saved);
        drafts.edit("noctis-lilac","keyword","#234567");
        for(String id:saved.keySet()) assertEquals(saved.get(id),drafts.overrides(id));
        assertEquals("#234567",drafts.resolve("noctis-lilac").color("keyword"));
    }
    @Test public void lightPlanUsesNativeLightScopesAndCanReleaseOnExternalDark() {
        var p = PaletteCatalog.load("noctis-lilac");
        var plan = SettingCatalog.load().plan(p,true,"org.eclipse.ui.defaultTheme");
        assertEquals(OriginalValue.of("12,0,107"),plan.get(new SettingKey("instance","org.eclipse.ui.workbench","com._1c.g5.v8.dt.theming.ui.FOREGROUND_COLOR")));
        assertTrue(plan.keySet().stream().noneMatch(k -> k.key().startsWith(ThemeMode.DARK.cssId()+".")));
        var state = new ProfileState(p.id(),true);
        assertFalse(state.onThemeChanged(ThemeMode.LIGHT.cssId()));
        assertTrue(state.onThemeChanged(ThemeMode.DARK.cssId()));
    }
    @Test public void adaptedTextRemainsReadableOnLineAndHoverAndSelection() {
        var p = PaletteCatalog.load("noctis-lilac");
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
