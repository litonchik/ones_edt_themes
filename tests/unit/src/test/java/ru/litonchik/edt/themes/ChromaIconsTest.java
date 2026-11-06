package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import java.util.*;
import java.io.*;
import javax.imageio.ImageIO;
import org.junit.Test;
import ru.litonchik.edt.themes.model.*;

public class ChromaIconsTest {
    @Test public void catalogUsesOnlyVerifiedNativeNamesAndKnownArtwork(){
        assertEquals(64,IconCatalog.nativeNames().size());assertEquals(49,IconCatalog.assets().size());
        assertEquals("meta-catalog",IconCatalog.nativeNames().get("catalog"));
        assertEquals("meta-module",IconCatalog.nativeNames().get("common_module"));
        assertEquals(IconCatalog.nativeNames().get("enum"),IconCatalog.nativeNames().get("enum_dark"));
        assertNull(IconCatalog.nativeNames().get("unknown"));
        assertThrows(UnsupportedOperationException.class,() -> IconCatalog.nativeNames().put("unknown","other"));
    }
    @Test public void everyIconHasTransparentPngsAtAllSupportedScales() throws Exception {
        for(String mode:List.of("light","dark"))for(String asset:IconCatalog.assets())for(int size:new int[]{16,20,24,28,32,48,64}){
            var path="/resources/icons/chroma/png/"+mode+"/"+size+"/"+asset+".png";
            try(var input=getClass().getResourceAsStream(path)){
                assertNotNull(path,input);var image=ImageIO.read(input);assertNotNull(path,image);
                assertEquals(path,size,image.getWidth());assertEquals(path,size,image.getHeight());
                boolean empty=false,ink=false;for(int y=0;y<size;y++)for(int x=0;x<size;x++){int a=image.getRGB(x,y)>>>24;empty|=a==0;ink|=a>180;}
                assertTrue(path+" transparent background",empty);assertTrue(path+" visible artwork",ink);
            }
        }
    }
    private String svg(String mode,String asset) throws IOException {
        try(var input=getClass().getResourceAsStream("/resources/icons/chroma/svg/"+mode+"/"+asset+".svg")){assertNotNull(input);return new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}
    }
    @Test public void pairedIconsPreserveTheSameGeometry() throws Exception {
        for(String asset:IconCatalog.assets())assertEquals(asset,svg("light",asset).replaceAll("stroke=\"[^\"]+\"","stroke=\"color\""),svg("dark",asset).replaceAll("stroke=\"[^\"]+\"","stroke=\"color\""));
    }
    private double luminance(int rgb){double sum=0;int[] shifts={16,8,0};double[] weights={.2126,.7152,.0722};for(int i=0;i<3;i++){double v=((rgb>>shifts[i])&255)/255.;sum+=weights[i]*(v<=.04045?v/12.92:Math.pow((v+.055)/1.055,2.4));}return sum;}
    @Test public void iconStrokeRemainsVisibleOnAllNineteenStandardPalettes() throws Exception {
        for(var profile:PaletteCatalog.profiles()){
            var palette=PaletteCatalog.load(profile.id());String mode=profile.mode().name().toLowerCase(Locale.ROOT);
            for(String asset:IconCatalog.assets()){
                var image=ImageIO.read(getClass().getResourceAsStream("/resources/icons/chroma/png/"+mode+"/32/"+asset+".png"));var colors=new HashMap<Integer,Integer>();
                for(int y=0;y<32;y++)for(int x=0;x<32;x++){int argb=image.getRGB(x,y);if((argb>>>24)>=245)colors.merge(argb&0xFFFFFF,1,Integer::sum);}
                int stroke=colors.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
                for(String role:List.of("panel","editor.background")){
                    int bg=Integer.parseInt(palette.color(role).substring(1),16);double a=luminance(stroke),b=luminance(bg),ratio=(Math.max(a,b)+.05)/(Math.min(a,b)+.05);
                    assertTrue(profile.id()+" "+asset+" "+role+" contrast "+ratio,ratio>=3.0);
                }
            }
        }
    }
    @Test public void upstreamLicenseAndPinnedProvenanceAreShipped() throws Exception {
        try(var input=getClass().getResourceAsStream("/resources/licenses/v8-chroma-LICENSE.txt")){
            assertNotNull(input);String license=new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);assertTrue(license.contains("MIT License"));assertTrue(license.contains("Aleksandr Ponkratov"));
        }
        try(var input=getClass().getResourceAsStream("/resources/icons/chroma/source.json")){
            assertNotNull(input);String source=new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);assertTrue(source.contains("5105ccf35ae63122a2a98c98230bebfd4ef849af"));assertTrue(source.contains("APonkratov/v8-chroma"));
        }
    }
}
