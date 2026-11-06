package ru.litonchik.edt.themes.runtime;

import java.util.Objects;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import ru.litonchik.edt.themes.model.ThemeMode;

/** Native JFace descriptor with independently rendered 100–400% PNG variants. */
final class ChromaIconDescriptor extends ImageDescriptor {
    private static final int[] SIZES={16,20,24,28,32,48,64};
    private final String asset;
    private final ThemeMode mode;
    ChromaIconDescriptor(String asset,ThemeMode mode){this.asset=asset;this.mode=mode;}
    @Override public ImageData getImageData(){return getImageData(100);}
    @Override public ImageData getImageData(int zoom){
        int size=Math.max(1,Math.round(16f*zoom/100));
        int source=SIZES[SIZES.length-1];
        for(int candidate:SIZES)if(candidate>=size){source=candidate;break;}
        String path="/resources/icons/chroma/png/"+mode.name().toLowerCase(java.util.Locale.ROOT)+"/"+source+"/"+asset+".png";
        try(var input=ChromaIconDescriptor.class.getResourceAsStream(path)){
            if(input==null)throw new IllegalStateException("Нет значка: "+path);
            var data=new ImageData(input);return data.width==size?data:data.scaledTo(size,size);
        }catch(java.io.IOException e){throw new IllegalStateException(e);}
    }
    @Override public boolean equals(Object other){return other instanceof ChromaIconDescriptor d && asset.equals(d.asset) && mode==d.mode;}
    @Override public int hashCode(){return Objects.hash(asset,mode);}
}
