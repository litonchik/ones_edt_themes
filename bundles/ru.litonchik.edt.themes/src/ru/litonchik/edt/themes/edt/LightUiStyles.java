package ru.litonchik.edt.themes.edt;

import java.util.*;
import java.util.List;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Hyperlink;
import ru.litonchik.edt.themes.model.*;

/** Scoped chrome/forms styling and public SWT selection painting; editor content is excluded. */
public final class LightUiStyles {
    private static final String UI = "edtThemesLightUi";
    private static final String BODY = "edtThemesLightBody";
    private static final String LINK = "edtThemesLightLink";
    private static final String STATE = LightUiStyles.class.getName()+".classes";
    private static final String CSS_CLASS = "org.eclipse.e4.ui.css.CssClassName";
    private static final String INLINE = "style";
    private static final String START = ";/*edt-themes-light:start*/";
    private static final String END = "/*edt-themes-light:end*/";
    private record Mark(String original, String applied, Object originalStyle, String appliedStyle) {}
    private static LightUiStyles instance;
    private final Display display;
    private IThemeEngine engine;
    private final Set<Composite> preferences = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Control> editorRoots = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Listener paint;
    private boolean enabled, managed, scheduled, closed;
    private final Map<Control,Listener> selectionListeners = new IdentityHashMap<>();
    private final Listener selectionDispose = event -> selectionListeners.remove((Control)event.widget);
    private org.eclipse.swt.graphics.Color selectionBackground, selectionForeground;
    private record FieldBackground(org.eclipse.swt.graphics.RGB rgb, org.eclipse.swt.graphics.Image image) {}
    private final Map<Text,FieldBackground> fieldBackgrounds = new IdentityHashMap<>();
    private final Listener fieldDispose = event -> { var value=fieldBackgrounds.remove((Text)event.widget);if(value!=null)value.image().dispose(); };


    private LightUiStyles(IThemeEngine engine) {
        this.engine=engine; display=PlatformUI.getWorkbench().getDisplay();
        paint=event -> {
            if(closed || !managed || scheduled || !(event.widget instanceof Control control)
                    || control.isDisposed() || !eligible(control) || isEditorContent(control)) return;
            if(!enabled && !inForm(control) && (!rowControl(control) || selectionListeners.containsKey(control))) return;
            if(control.getData(STATE) instanceof Mark && has(cssClass(control),UI) && colorsMatch(control)
                    && (!rowControl(control) || selectionListeners.containsKey(control))) return;
            boolean allowed=Arrays.stream(PlatformUI.getWorkbench().getWorkbenchWindows())
                    .anyMatch(w -> w.getShell()==control.getShell());
            if(!allowed) allowed=preferences.stream().anyMatch(root -> !root.isDisposed() && within(control,root));
            if(!allowed) return;
            scheduled=true;
            display.asyncExec(() -> { scheduled=false; if(!closed) sync(true); });
        };
        for(int event:new int[]{SWT.Paint,SWT.Resize,SWT.Show}) display.addFilter(event,paint);
    }
    private static LightUiStyles get(IThemeEngine engine) {
        if(instance==null) instance=new LightUiStyles(engine);
        instance.engine=engine; return instance;
    }
    public static void prepare(IThemeEngine engine) { if(PlatformUI.isWorkbenchRunning()) get(engine).syncMarked(false,false); }
    public static void refresh(IThemeEngine engine) { if(PlatformUI.isWorkbenchRunning()) get(engine).sync(true); }
    public static void registerPreferences(Composite root) {
        if(!PlatformUI.isWorkbenchRunning()) return;
        var manager=get(PlatformUI.getWorkbench().getService(IThemeEngine.class));
        manager.preferences.add(root);
        root.addDisposeListener(event -> manager.preferences.remove(root));
        manager.sync(true);
    }
    public static void stop() {
        if(instance==null) return;
        var manager=instance; instance=null; manager.closed=true;
        if(!manager.display.isDisposed()) {
            for(int event:new int[]{SWT.Paint,SWT.Resize,SWT.Show}) manager.display.removeFilter(event,manager.paint);
            manager.enabled=false;
            manager.syncMarked(false,true);
        }
        for(var entry:new ArrayList<>(manager.fieldBackgrounds.entrySet())){
            if(!entry.getKey().isDisposed())manager.setFieldBackground(entry.getKey(),false);else entry.getValue().image().dispose();
        }
        manager.fieldBackgrounds.clear();manager.preferences.clear(); manager.editorRoots.clear();
        if(manager.selectionBackground!=null)manager.selectionBackground.dispose();
        if(manager.selectionForeground!=null)manager.selectionForeground.dispose();
    }
    private boolean managedActive() {
        if(closed || engine==null || engine.getActiveTheme()==null) return false;
        String profile=InstanceScope.INSTANCE.getNode("ru.litonchik.edt.themes").get("activeProfile",null);
        return PaletteCatalog.isKnown(profile) && PaletteCatalog.mode(profile).cssId().equals(engine.getActiveTheme().getId());
    }
    private void sync(boolean apply) {
        if(closed || display.isDisposed()) return;
        managed=managedActive(); enabled=managed && ThemeMode.LIGHT.cssId().equals(engine.getActiveTheme().getId()); syncMarked(enabled,apply);
    }
    private void syncMarked(boolean mark,boolean apply) {
        var changed=new ArrayList<Control>(); editorRoots.clear();
        for(var window:PlatformUI.getWorkbench().getWorkbenchWindows()) {
            var shell=window.getShell(); if(shell==null || shell.isDisposed()) continue;
            boolean classified=true;
            for(var page:window.getPages()) for(var reference:page.getEditorReferences()) {
                var editor=reference.getEditor(false); if(editor==null) continue;
                MPart part=editor.getSite().getService(MPart.class);
                if(part!=null && part.getWidget() instanceof Control root && !root.isDisposed()) editorRoots.add(root);
                else classified=false; // Fail closed while a new editor has no public model widget yet.
            }
            walk(shell,mark && classified,apply && managedActive() && classified,changed);
        }
        for(var root:new ArrayList<>(preferences)) if(!root.isDisposed()) walk(root,mark,apply && managedActive(),changed);
        if(apply) for(var control:changed) if(!control.isDisposed()) {
            engine.applyStyles(control,false);
            if(mark && control instanceof org.eclipse.swt.custom.CTabFolder tabs) {
                // SWT exposes the selected text/background independently of renderer fill.
                var registry=PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
                tabs.setSelectionForeground(registry.get(colorId("FOREGROUND_COLOR")));
                tabs.setSelectionBackground(registry.get(colorId("BACKGROUND_COLOR_TREES_AND_TABLES")));
            }
        }
    }
    private void walk(Control control,boolean mark,boolean manage,List<Control> changed) {
        if(control.isDisposed()) return;
        if(editorRoots.contains(control)) { clearMarked(control,changed); return; }
        if(control instanceof Canvas && !(control instanceof CLabel) && !(control instanceof Shell) && !(control instanceof Hyperlink) && !(control instanceof org.eclipse.ui.forms.widgets.ExpandableComposite)) return;
        if(rowControl(control)) setSelectionManaged(control,manage);
        if(eligible(control)) setMarked(control,mark || manage && inForm(control),changed);
        if(control instanceof Text text) setFieldBackground(text,(mark || manage) && inForm(control));
        if(control instanceof Composite composite) for(var child:composite.getChildren()) walk(child,mark,manage,changed);
    }
    private void clearMarked(Control control,List<Control> changed) {
        setMarked(control,false,changed); setSelectionManaged(control,false);
        if(control instanceof Text text)setFieldBackground(text,false);
        if(control instanceof Composite composite) for(var child:composite.getChildren()) clearMarked(child,changed);
    }
    private void setMarked(Control control,boolean mark,List<Control> changed) {
        String current=cssClass(control); Object inline=control.getData(INLINE);
        if(mark) {
            Mark previous=control.getData(STATE) instanceof Mark state ? state : null;
            String classes=UI+(body(control)?" "+BODY:"")+((control instanceof Link || control instanceof Hyperlink)?" "+LINK:"");
            String applied=previous!=null && has(current,UI)?current:current==null || current.isBlank()?classes:current+" "+classes;
            Object originalStyle=previous==null?inline:Objects.equals(inline,previous.appliedStyle())?previous.originalStyle():withoutStyle(inline);
            String style=Objects.toString(originalStyle,"")+START+literalStyle(control)+END;
            if(previous!=null && Objects.equals(style,inline) && Objects.equals(applied,current) && colorsMatch(control)) return;
            String original=previous==null?current:Objects.equals(current,previous.applied())?previous.original():withoutOurs(current);
            control.setData(STATE,new Mark(original,applied,originalStyle,style));
            control.setData(CSS_CLASS,applied); control.setData(INLINE,style); changed.add(control);
        } else if(control.getData(STATE) instanceof Mark state) {
            control.setData(CSS_CLASS,Objects.equals(current,state.applied())?state.original():withoutOurs(current));
            control.setData(INLINE,Objects.equals(inline,state.appliedStyle())?state.originalStyle():withoutStyle(inline));
            control.setData(STATE,null); changed.add(control);
        }
    }
    private void setFieldBackground(Text text,boolean mark) {
        var previous=fieldBackgrounds.get(text);var current=text.getBackgroundImage();
        if(mark && (current==null || previous!=null && current==previous.image())) {
            var rgb=color(background(text));
            if(previous!=null && rgb.equals(previous.rgb()) && current==previous.image())return;
            var data=new org.eclipse.swt.graphics.ImageData(1,1,24,new org.eclipse.swt.graphics.PaletteData(0xFF0000,0xFF00,0xFF));
            data.setPixel(0,0,(rgb.red<<16)|(rgb.green<<8)|rgb.blue);
            var image=new org.eclipse.swt.graphics.Image(display,data);text.setBackgroundImage(image);
            fieldBackgrounds.put(text,new FieldBackground(rgb,image));
            if(previous==null)text.addListener(SWT.Dispose,fieldDispose);else previous.image().dispose();
        } else if(previous!=null) {
            if(current==previous.image())text.setBackgroundImage(null);
            fieldBackgrounds.remove(text);text.removeListener(SWT.Dispose,fieldDispose);previous.image().dispose();
        }
    }
    private static boolean inForm(Control control) {
        for(Control node=control;node!=null;node=node.getParent())
            if(node instanceof org.eclipse.ui.forms.widgets.Form || node instanceof org.eclipse.ui.forms.widgets.ExpandableComposite
                    || node instanceof org.eclipse.ui.forms.widgets.SharedScrolledComposite) return true;
        return false;
    }
    private static boolean rowControl(Control control) { return control instanceof Tree || control instanceof Table; }
    private void setSelectionManaged(Control control,boolean manage) {
        if(!rowControl(control) || control.isDisposed())return;
        if(manage && !selectionListeners.containsKey(control)) {
            Listener listener=this::eraseSelection;selectionListeners.put(control,listener);
            control.addListener(SWT.EraseItem,listener);control.addListener(SWT.Dispose,selectionDispose);control.redraw();
        } else if(!manage) {
            Listener listener=selectionListeners.remove(control);
            if(listener!=null){control.removeListener(SWT.EraseItem,listener);control.removeListener(SWT.Dispose,selectionDispose);control.redraw();}
        }
    }
    private void selectionColors() {
        var store=org.eclipse.ui.editors.text.EditorsUI.getPreferenceStore();
        String bgKey="AbstractTextEditor.Color.SelectionBackground",fgKey="AbstractTextEditor.Color.SelectionForeground";
        var bg=store.getBoolean(bgKey+".SystemDefault")?display.getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB():org.eclipse.jface.preference.PreferenceConverter.getColor(store,bgKey);
        var fg=store.getBoolean(fgKey+".SystemDefault")?display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT).getRGB():org.eclipse.jface.preference.PreferenceConverter.getColor(store,fgKey);
        if(selectionBackground==null || !bg.equals(selectionBackground.getRGB())) {
            if(selectionBackground!=null)selectionBackground.dispose();selectionBackground=new org.eclipse.swt.graphics.Color(display,bg);
        }
        if(selectionForeground==null || !fg.equals(selectionForeground.getRGB())) {
            if(selectionForeground!=null)selectionForeground.dispose();selectionForeground=new org.eclipse.swt.graphics.Color(display,fg);
        }
    }
    private void eraseSelection(Event event) {
        if(!managedActive() || !(event.widget instanceof Control control) || control.isDisposed())return;
        boolean selected=control instanceof Tree tree?Arrays.asList(tree.getSelection()).contains(event.item)
                :control instanceof Table table && Arrays.asList(table.getSelection()).contains(event.item);
        if(!selected || (control.getStyle() & SWT.HIDE_SELECTION)!=0 && !control.isFocusControl())return;
        selectionColors();var gc=event.gc;var oldBackground=gc.getBackground();
        gc.setBackground(selectionBackground);gc.setForeground(selectionForeground);
        gc.fillRectangle(event.x,event.y,event.width,event.height);gc.setBackground(oldBackground);
        event.detail &= ~(SWT.SELECTED | SWT.HOT | SWT.BACKGROUND);
    }
    private static String cssClass(Widget widget) { return (String)widget.getData(CSS_CLASS); }
    private static Object withoutStyle(Object value) {
        if(!(value instanceof String text)) return value;
        int start=text.indexOf(START), end=text.indexOf(END,start);
        return start<0 || end<0?text:text.substring(0,start)+text.substring(end+END.length());
    }
    private static String colorId(String name) { return "com._1c.g5.v8.dt.theming.ui."+name; }
    private static org.eclipse.swt.graphics.RGB color(String name) {
        var rgb=PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().getRGB(colorId(name));
        if(rgb==null) throw new IllegalStateException("Нет зарегистрированного цвета: "+name);
        return rgb;
    }
    private static String hex(String name) {
        var rgb=color(name); return String.format("#%02X%02X%02X",rgb.red,rgb.green,rgb.blue);
    }
    private static String background(Control control) { return body(control)?"BACKGROUND_COLOR_TREES_AND_TABLES":"BACKGROUND_COLOR_ACCENT"; }
    private static String foreground(Control control) {
        return !control.isEnabled()?"CONTROLS_DISABLED_FOREGROUND_COLOR":control instanceof Link || control instanceof Hyperlink?"FOREGROUND_COLOR_ACCENT":"FOREGROUND_COLOR";
    }
    private static String literalStyle(Control control) {
        // Literal HEX values bypass Eclipse's conversion cache for named colors.
        String result="background-color:"+hex(background(control))+" !important;color:"+hex(foreground(control))+" !important;";
        if(control instanceof org.eclipse.swt.custom.CTabFolder)
            result+="swt-tab-outline:"+hex("BORDER_COLOR")+";swt-outer-keyline-color:"+hex("BORDER_COLOR")
                +";swt-unselected-tabs-color:"+hex("BACKGROUND_COLOR_ACCENT")+" "+hex("BACKGROUND_COLOR_ACCENT")+" 100% 100%;"
                +"swt-selected-tab-fill:"+hex("BACKGROUND_COLOR_TREES_AND_TABLES")+";swt-selected-tab-highlight:none;";
        return result;
    }
    private boolean colorsMatch(Control control) {
        if(control instanceof Text text && fieldBackgrounds.containsKey(text) && text.getBackgroundImage()!=fieldBackgrounds.get(text).image())return false;
        if(!color(background(control)).equals(control.getBackground().getRGB()) || !color(foreground(control)).equals(control.getForeground().getRGB())) return false;
        return !(control instanceof org.eclipse.swt.custom.CTabFolder tabs)
            || color("FOREGROUND_COLOR").equals(tabs.getSelectionForeground().getRGB()) && color("BACKGROUND_COLOR_TREES_AND_TABLES").equals(tabs.getSelectionBackground().getRGB());
    }
    private static boolean has(String value,String token) {
        return value!=null && Arrays.asList(value.trim().split("\\s+")).contains(token);
    }
    private static String withoutOurs(String value) {
        if(value==null) return null;
        return String.join(" ",Arrays.stream(value.trim().split("\\s+"))
                .filter(s -> !Set.of(UI,BODY,LINK).contains(s)).toList());
    }
    private boolean isEditorContent(Control control) {
        for(Control node=control;node!=null;node=node.getParent()) if(editorRoots.contains(node)) return true;
        return false;
    }
    private static boolean within(Control control,Control root) {
        for(Control node=control;node!=null;node=node.getParent()) if(node==root) return true;
        return false;
    }
    private static boolean body(Control c) {
        return c instanceof Tree || c instanceof Table || c instanceof org.eclipse.swt.widgets.List
                || c instanceof Text || c instanceof Combo || c instanceof org.eclipse.swt.custom.CCombo;
    }
    private static boolean eligible(Control c) {
        return c instanceof Shell || c instanceof CLabel || c instanceof Hyperlink || !(c instanceof Canvas) && (c instanceof Composite || c instanceof Label
                || c instanceof Link || c instanceof Button || c instanceof Text || c instanceof Sash || c instanceof org.eclipse.swt.widgets.List);
    }
}
