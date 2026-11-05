package ru.litonchik.edt.themes.edt;

import java.util.*;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import ru.litonchik.edt.themes.model.PaletteCatalog;

/** Refreshes native ruler models and controls after the native CSS pass. No preference writes. */
public final class EditorRulerColors {
    private static EditorRulerColors instance;
    private final Set<IWorkbenchWindow> windows=Collections.newSetFromMap(new IdentityHashMap<>());
    private boolean queued,closed;
    private final IPartListener2 parts=new IPartListener2() {
        private void changed(IWorkbenchPartReference ref) {
            if(ref.getPart(false) instanceof IEditorPart editor && editor.getAdapter(IVerticalRulerInfo.class)!=null) schedule();
        }
        @Override public void partOpened(IWorkbenchPartReference ref) { changed(ref); }
        @Override public void partVisible(IWorkbenchPartReference ref) { changed(ref); }
        @Override public void partInputChanged(IWorkbenchPartReference ref) { changed(ref); }
    };
    private final IWindowListener windowListener=new IWindowListener() {
        @Override public void windowOpened(IWorkbenchWindow window) { attach(window); schedule(); }
        @Override public void windowClosed(IWorkbenchWindow window) { windows.remove(window); }
        @Override public void windowActivated(IWorkbenchWindow window) {}
        @Override public void windowDeactivated(IWorkbenchWindow window) {}
    };
    private EditorRulerColors() {
        var workbench=PlatformUI.getWorkbench(); workbench.addWindowListener(windowListener);
        for(var window:workbench.getWorkbenchWindows()) attach(window);
        schedule();
    }
    public static void start() { if(instance==null) instance=new EditorRulerColors(); }
    public static void stop() {
        if(instance==null) return;
        var manager=instance; instance=null; manager.closed=true;
        PlatformUI.getWorkbench().removeWindowListener(manager.windowListener);
        for(var window:manager.windows) window.getPartService().removePartListener(manager.parts);
        manager.windows.clear();
    }
    private void attach(IWorkbenchWindow window) {
        if(windows.add(window)) window.getPartService().addPartListener(parts);
    }
    private void schedule() {
        if(closed || queued) return; queued=true;
        PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
            queued=false; if(!closed && active()) refresh();
        });
    }
    private static boolean active() {
        String profile=InstanceScope.INSTANCE.getNode("ru.litonchik.edt.themes").get("activeProfile",null);
        var engine=PlatformUI.getWorkbench().getService(org.eclipse.e4.ui.css.swt.theme.IThemeEngine.class);
        return PaletteCatalog.isKnown(profile) && engine!=null && engine.getActiveTheme()!=null
            && PaletteCatalog.mode(profile).cssId().equals(engine.getActiveTheme().getId());
    }
    public static void refresh() {
        var controls=new IdentityHashMap<Control,Color>();
        var projections=new IdentityHashMap<Control,org.eclipse.jface.text.ITextViewer>();
        for(var window:PlatformUI.getWorkbench().getWorkbenchWindows()) for(var page:window.getPages())
            for(var reference:page.getEditorReferences()) {
                var editor=reference.getEditor(false); if(editor==null) continue;
                var info=editor.getAdapter(IVerticalRulerInfo.class);
                if(!(info instanceof CompositeRuler ruler)) continue;
                remember(controls,ruler.getControl());
                for(var it=ruler.getDecoratorIterator();it.hasNext();) {
                    var column=it.next(); remember(controls,column.getControl());
                    var viewer=editor.getAdapter(org.eclipse.jface.text.ITextViewer.class);
                    if(column instanceof org.eclipse.jface.text.source.IVerticalRulerInfoExtension extended
                            && extended.getModel() instanceof org.eclipse.jface.text.source.projection.ProjectionAnnotationModel && viewer!=null)
                        projections.put(column.getControl(),viewer);
                }
            }
        var preferences=EditorsUI.getPreferenceStore();
        String key=AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND;
        // Native LineNumberColumn listens to EditorsUI, not to Xtext's language store.
        // This public event refreshes its delegated LineNumberRulerColumn's paint color too.
        preferences.firePropertyChangeEvent(key,null,preferences.getString(key));
        String foreground=org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
        preferences.firePropertyChangeEvent(foreground,null,preferences.getString(foreground));
        Color background=active() && !preferences.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
            ? EditorsUI.getSharedTextColors().getColor(PreferenceConverter.getColor(preferences,key)) : null;
        for(var entry:controls.entrySet()) if(!entry.getKey().isDisposed()) {
            // Without an active explicit palette preserve the colors produced by native CSS.
            var viewer=projections.get(entry.getKey());
            Color original=viewer!=null && viewer.getTextWidget()!=null && !viewer.getTextWidget().isDisposed()
                ? viewer.getTextWidget().getBackground() : entry.getValue();
            entry.getKey().setBackground(background==null?original:background);
            entry.getKey().redraw();
        }
    }
    private static void remember(Map<Control,Color> controls,Control control) {
        if(control!=null && !control.isDisposed()) controls.put(control,control.getBackground());
    }
}
