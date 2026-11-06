package ru.litonchik.edt.themes.runtime;

import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;

/** Replaces the viewer base before its original decoration stack; owns only its listener bridge. */
public final class ChromaIconDecorator extends LabelDecorator implements IColorDecorator,IFontDecorator {
    private final ILabelDecorator original;
    private final CopyOnWriteArrayList<ILabelProviderListener> listeners=new CopyOnWriteArrayList<>();
    private final ILabelProviderListener changed=event -> {
        var forwarded=new LabelProviderChangedEvent(this,event.getElements());
        for(var listener:listeners)listener.labelProviderChanged(forwarded);
    };
    private boolean closed;
    public ChromaIconDecorator(ILabelDecorator original){this.original=original;if(original!=null)original.addListener(changed);}
    public ILabelDecorator original(){return original;}
    @Override public Image decorateImage(Image image,Object element){return decorateImage(image,element,DecorationContext.DEFAULT_CONTEXT);}
    @Override public Image decorateImage(Image image,Object element,IDecorationContext context){
        Image replacement=ChromaIcons.replacement(image),base=replacement==null?image:replacement;
        Image decorated=original instanceof LabelDecorator d?d.decorateImage(base,element,context):original==null?null:original.decorateImage(base,element);
        return decorated==null?replacement:decorated;
    }
    @Override public String decorateText(String text,Object element){return original==null?null:original.decorateText(text,element);}
    @Override public String decorateText(String text,Object element,IDecorationContext context){return original instanceof LabelDecorator d?d.decorateText(text,element,context):decorateText(text,element);}
    @Override public boolean prepareDecoration(Object element,String text,IDecorationContext context){
        if(original instanceof LabelDecorator d)return d.prepareDecoration(element,text,context);
        return !(original instanceof IDelayedLabelDecorator d) || d.prepareDecoration(element,text);
    }
    @Override public Color decorateForeground(Object element){return original instanceof IColorDecorator d?d.decorateForeground(element):null;}
    @Override public Color decorateBackground(Object element){return original instanceof IColorDecorator d?d.decorateBackground(element):null;}
    @Override public Font decorateFont(Object element){return original instanceof IFontDecorator d?d.decorateFont(element):null;}
    @Override public boolean isLabelProperty(Object element,String property){return original!=null && original.isLabelProperty(element,property);}
    @Override public void addListener(ILabelProviderListener listener){listeners.addIfAbsent(listener);}
    @Override public void removeListener(ILabelProviderListener listener){listeners.remove(listener);}
    /** The original is handed back to its viewer; do not dispose it on disable. */
    void detach(){if(closed)return;closed=true;if(original!=null)original.removeListener(changed);listeners.clear();}
    /** Called by the native viewer on final disposal, preserving its original ownership contract. */
    @Override public void dispose(){if(closed)return;detach();if(original!=null)original.dispose();}
}
