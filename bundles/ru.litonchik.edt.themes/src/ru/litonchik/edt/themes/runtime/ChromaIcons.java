package ru.litonchik.edt.themes.runtime;

import java.util.*;
import java.security.MessageDigest;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.EventHandler;
import ru.litonchik.edt.themes.model.*;

/** Optional reversible label decoration; native images and their registries are never changed. */
public final class ChromaIcons {
    public static final String ID="ru.litonchik.edt.themes.chromaIcons";
    public static final String KEY="icons.chroma.enabled";
    private static ChromaIcons instance;
    private final LocalResourceManager resources=new LocalResourceManager(JFaceResources.getResources(PlatformUI.getWorkbench().getDisplay()));
    private final Map<ChromaIconDescriptor,Image> images=new HashMap<>();
    private Map<String,String> nativeImages;
    private final IEventBroker broker=PlatformUI.getWorkbench().getService(IEventBroker.class);
    private final EventHandler themeChanged=event -> refresh();
    private record Binding(DecoratingStyledCellLabelProvider provider,ILabelDecorator original,ChromaIconDecorator applied) {}
    private final Map<ColumnViewer,Binding> bindings=new IdentityHashMap<>();
    private final Set<ColumnViewer> viewers=Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<IWorkbenchWindow> windows=Collections.newSetFromMap(new IdentityHashMap<>());
    private final IPartListener2 parts=new IPartListener2(){
        @Override public void partOpened(IWorkbenchPartReference part){refresh();}
        @Override public void partVisible(IWorkbenchPartReference part){refresh();}
        @Override public void partClosed(IWorkbenchPartReference part){refresh();}
        @Override public void partActivated(IWorkbenchPartReference part){if("com._1c.g5.v8.dt.ui2.navigator".equals(part.getId()))refresh();}
        @Override public void partBroughtToTop(IWorkbenchPartReference part){if("com._1c.g5.v8.dt.ui2.navigator".equals(part.getId()))refresh();}
        @Override public void partDeactivated(IWorkbenchPartReference part){}
        @Override public void partHidden(IWorkbenchPartReference part){}
        @Override public void partInputChanged(IWorkbenchPartReference part){}
    };
    private final IWindowListener windowEvents=new IWindowListener(){
        @Override public void windowOpened(IWorkbenchWindow window){refresh();}
        @Override public void windowActivated(IWorkbenchWindow window){refresh();}
        @Override public void windowClosed(IWorkbenchWindow window){windows.remove(window);}
        @Override public void windowDeactivated(IWorkbenchWindow window){}
    };
    private boolean closed,scheduled,watching;
    // EDT may replace its label provider after partOpened. Observe only public viewer
    // identities while enabled; unchanged viewers are never refreshed by this timer.
    private final Runnable watch=new Runnable(){
        @Override public void run(){
            if(closed || !isEnabled()){watching=false;return;}
            sync(false);
            PlatformUI.getWorkbench().getDisplay().timerExec(250,this);
        }
    };
    private void ensureWatching(){
        if(!watching && !closed && isEnabled()){
            watching=true;PlatformUI.getWorkbench().getDisplay().timerExec(250,watch);
        }
    }
    private ChromaIcons(){if(broker!=null)broker.subscribe(IThemeEngine.Events.THEME_CHANGED,themeChanged);PlatformUI.getWorkbench().addWindowListener(windowEvents);}
    public static void start(){
        if(instance==null && PlatformUI.isWorkbenchRunning()){instance=new ChromaIcons();instance.sync(true);}
    }
    public static boolean isEnabled(){return InstanceScope.INSTANCE.getNode(SnapshotPreferences.PLUGIN_ID).getBoolean(KEY,false);}
    public static void setEnabled(boolean enabled) throws org.osgi.service.prefs.BackingStoreException {
        var node=InstanceScope.INSTANCE.getNode(SnapshotPreferences.PLUGIN_ID);
        if(enabled)node.putBoolean(KEY,true);else node.remove(KEY);
        node.flush();refresh();
    }
    public static void refresh(){
        if(instance==null || instance.closed || instance.scheduled || !PlatformUI.isWorkbenchRunning())return;
        var manager=instance;var display=PlatformUI.getWorkbench().getDisplay();
        if(!display.isDisposed()){manager.scheduled=true;display.asyncExec(() -> {
            manager.scheduled=false;if(!manager.closed && PlatformUI.isWorkbenchRunning())manager.sync(true);
        });}
    }
    /** Register an already constructed public styled viewer; actual EDT navigators are discovered automatically. */
    public static void registerViewer(ColumnViewer viewer){if(instance!=null)instance.track(viewer);refresh();}
    private void track(ColumnViewer viewer){
        if(viewer==null || viewer.getControl().isDisposed() || !viewers.add(viewer))return;
        viewer.getControl().addDisposeListener(event -> {viewers.remove(viewer);bindings.remove(viewer);});
    }
    private void restore(ColumnViewer viewer){
        var binding=bindings.remove(viewer);if(binding==null || viewer.getControl().isDisposed())return;
        if(viewer.getLabelProvider()==binding.provider() && binding.provider().getLabelDecorator()==binding.applied()){
            binding.provider().setLabelDecorator(binding.original());binding.applied().detach();viewer.refresh();
        }
    }
    private void sync(boolean refreshImages){
        ensureWatching();
        for(var window:PlatformUI.getWorkbench().getWorkbenchWindows()){
            if(windows.add(window))window.getPartService().addPartListener(parts);
            for(var page:window.getPages())if(page.findView("com._1c.g5.v8.dt.ui2.navigator") instanceof CommonNavigator navigator)track(navigator.getCommonViewer());
        }
        for(var viewer:new ArrayList<>(viewers)){
            if(viewer.getControl().isDisposed()){viewers.remove(viewer);bindings.remove(viewer);continue;}
            if(!isEnabled()){restore(viewer);continue;}
            if(!(viewer.getLabelProvider() instanceof DecoratingStyledCellLabelProvider provider)){restore(viewer);continue;}
            var previous=bindings.get(viewer);boolean changed=false;
            if(previous==null || previous.provider()!=provider || provider.getLabelDecorator()!=previous.applied()){
                if(previous!=null && previous.provider()==provider && provider.getLabelDecorator()==previous.original())previous.applied().detach();
                // Respect an external replacement; its current decorator becomes the new baseline.
                var original=provider.getLabelDecorator();var applied=new ChromaIconDecorator(original);
                bindings.put(viewer,new Binding(provider,original,applied));provider.setLabelDecorator(applied);changed=true;
            }
            if(changed || refreshImages)viewer.refresh();
        }
    }
    public static Image image(String asset,ThemeMode mode){
        if(!IconCatalog.assets().contains(asset))throw new IllegalArgumentException("Неизвестный значок: "+asset);
        start();if(instance==null || instance.closed)throw new IllegalStateException("Workbench недоступен");
        var descriptor=new ChromaIconDescriptor(asset,mode);
        return instance.images.computeIfAbsent(descriptor,d -> instance.resources.createImage(d));
    }
    public static Image replacement(Image original){
        if(!isEnabled() || instance==null || instance.closed || original==null || original.isDisposed())return null;
        var manager=instance;
        if(manager.nativeImages==null)manager.nativeImages=manager.loadNativeImages();
        String asset=manager.nativeImages.get(fingerprint(original.getImageData(100)));
        if(asset==null)return null; // Unknown/composite/disabled images keep every original badge and pixel.
        var engine=PlatformUI.getWorkbench().getService(IThemeEngine.class);
        var mode=engine==null || engine.getActiveTheme()==null?ThemeMode.LIGHT:ThemeMode.fromCssId(engine.getActiveTheme().getId());
        return image(asset,mode);
    }
    private Map<String,String> loadNativeImages(){
        var result=new HashMap<String,String>();var ambiguous=new HashSet<String>();
        var bundle=Platform.getBundle("com._1c.g5.v8.dt.md.ui.shared");if(bundle==null)return result;
        for(var entry:IconCatalog.nativeNames().entrySet()){
            var url=bundle.getEntry("icons/obj16/"+entry.getKey()+".png");if(url==null)continue;
            Image reference=ImageDescriptor.createFromURL(url).createImage();if(reference==null)continue;
            ImageData data;try{data=reference.getImageData(100);}finally{reference.dispose();}
            String key=fingerprint(data),previous=result.putIfAbsent(key,entry.getValue());
            if(previous!=null && !previous.equals(entry.getValue()))ambiguous.add(key);
        }
        ambiguous.forEach(result::remove);return Collections.unmodifiableMap(result);
    }
    private static String fingerprint(ImageData data){
        try{
            var digest=MessageDigest.getInstance("SHA-256");
            digest.update(java.nio.ByteBuffer.allocate(8).putInt(data.width).putInt(data.height).array());
            var mask=data.getTransparencyMask();
            for(int y=0;y<data.height;y++)for(int x=0;x<data.width;x++){
                int pixel=data.getPixel(x,y),alpha=mask.getPixel(x,y)==0?0:data.alpha>=0?data.alpha:data.getAlpha(x,y);
                var rgb=data.palette.getRGB(pixel);digest.update((byte)(alpha==0?0:rgb.red));digest.update((byte)(alpha==0?0:rgb.green));digest.update((byte)(alpha==0?0:rgb.blue));digest.update((byte)alpha);
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        }catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    public static void stop(){
        var manager=instance;if(manager==null)return;instance=null;manager.closed=true;
        if(manager.broker!=null)manager.broker.unsubscribe(manager.themeChanged);
        if(PlatformUI.isWorkbenchRunning()){PlatformUI.getWorkbench().removeWindowListener(manager.windowEvents);var display=PlatformUI.getWorkbench().getDisplay();if(!display.isDisposed())display.timerExec(-1,manager.watch);}
        for(var window:new ArrayList<>(manager.windows))if(window.getShell()!=null && !window.getShell().isDisposed())window.getPartService().removePartListener(manager.parts);
        for(var viewer:new ArrayList<>(manager.viewers))manager.restore(viewer);
        manager.windows.clear();manager.viewers.clear();manager.bindings.clear();
        manager.resources.dispose();manager.images.clear();if(manager.nativeImages!=null)manager.nativeImages=Map.of();
    }
}
