package ru.litonchik.edt.themes.ui;

import java.util.Arrays;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/** Read-only selector with a popup whose selected row is fully painted using public SWT APIs. */
public final class ReadableCombo extends Composite {
    private final Label name;
    private final Button arrow;
    private String[] items = new String[0];
    private Shell popup;
    private Table table;
    private Color selectionBackground, selectionForeground;
    public ReadableCombo(Composite parent) {
        super(parent,SWT.BORDER);
        GridLayout layout = new GridLayout(2,false); layout.marginWidth = 2; layout.marginHeight = 0; layout.horizontalSpacing = 0; setLayout(layout);
        name = new Label(this,SWT.NONE); name.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
        arrow = new Button(this,SWT.ARROW|SWT.DOWN); arrow.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,true));
        selectionBackground = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
        selectionForeground = getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
        name.addListener(SWT.MouseDown,event -> { if (event.button == 1) setListVisible(!getListVisible()); });
        arrow.addListener(SWT.Selection,event -> setListVisible(!getListVisible()));
        arrow.addListener(SWT.KeyDown,event -> {
            if (event.keyCode == SWT.ARROW_DOWN || event.keyCode == SWT.F4) { setListVisible(true); event.doit = false; }
        });
        addListener(SWT.Dispose,event -> closePopup());
    }
    public void setItems(String... values) { items = values.clone(); closePopup(); }
    public void select(int index) { if (index >= 0 && index < items.length) setText(items[index]); }
    public String getText() { return name.getText(); }
    public void setText(String value) { name.setText(value); }
    public int getSelectionIndex() { return Arrays.asList(items).indexOf(getText()); }
    @Override public boolean setFocus() { return arrow.setFocus(); }
    @Override public void setEnabled(boolean enabled) {
        super.setEnabled(enabled); name.setEnabled(enabled); arrow.setEnabled(enabled); if (!enabled) closePopup();
    }
    @Override public void setToolTipText(String text) { super.setToolTipText(text); name.setToolTipText(text); arrow.setToolTipText(text); }
    @Override public void setBackground(Color color) {
        super.setBackground(color); name.setBackground(color); arrow.setBackground(color);
        if (table != null && !table.isDisposed()) { table.setBackground(color); table.redraw(); }
    }
    @Override public void setForeground(Color color) {
        super.setForeground(color); name.setForeground(color); arrow.setForeground(color);
        if (table != null && !table.isDisposed()) { table.setForeground(color); table.redraw(); }
    }
    public void setSelectionColors(Color background,Color foreground) {
        selectionBackground = background; selectionForeground = foreground;
        if (table != null && !table.isDisposed()) table.redraw();
    }
    public boolean getListVisible() { return popup != null && !popup.isDisposed() && popup.isVisible(); }
    public void setListVisible(boolean visible) {
        if (!visible) { closePopup(); return; }
        if (getListVisible() || !isEnabled() || items.length == 0) return;
        popup = new Shell(getShell(),SWT.ON_TOP|SWT.NO_TRIM);
        popup.setLayout(new FillLayout());
        table = new Table(popup,SWT.SINGLE|SWT.FULL_SELECTION|SWT.V_SCROLL|SWT.BORDER);
        table.setFont(getFont()); table.setBackground(getBackground()); table.setForeground(getForeground());
        TableColumn column = new TableColumn(table,SWT.NONE);
        for (String value : items) new TableItem(table,SWT.NONE).setText(value);
        table.addListener(SWT.EraseItem,event -> event.detail &= ~(SWT.FOREGROUND|SWT.BACKGROUND|SWT.SELECTED|SWT.FOCUSED|SWT.HOT));
        table.addListener(SWT.PaintItem,event -> {
            TableItem item = (TableItem)event.item;
            boolean selected = table.getSelectionCount() == 1 && table.getSelection()[0] == item;
            event.gc.setBackground(selected ? selectionBackground : getBackground());
            event.gc.fillRectangle(0,event.y,table.getClientArea().width,event.height);
            event.gc.setForeground(selected ? selectionForeground : getForeground()); event.gc.setFont(table.getFont());
            int y = event.y+Math.max(0,(event.height-event.gc.getFontMetrics().getHeight())/2);
            event.gc.drawText(item.getText(),4,y,true);
        });
        table.addListener(SWT.Selection,event -> table.redraw());
        table.addListener(SWT.MouseUp,event -> { if (event.button == 1) commit(); });
        table.addListener(SWT.KeyDown,event -> {
            if (event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR) { commit(); event.doit = false; }
            else if (event.keyCode == SWT.ESC) { closePopup(); arrow.setFocus(); event.doit = false; }
        });
        popup.addListener(SWT.Deactivate,event -> closePopup());
        int height = table.getItemHeight()*Math.min(12,items.length)+4;
        int width = Math.max(180,getSize().x); Point location = toDisplay(0,getSize().y); Rectangle area = getMonitor().getClientArea();
        if (location.y+height > area.y+area.height) location.y = toDisplay(0,0).y-height;
        location.x = Math.max(area.x,Math.min(location.x,area.x+area.width-width));
        popup.setBounds(location.x,Math.max(area.y,location.y),width,height);
        popup.layout(); column.setWidth(table.getClientArea().width);
        int index = getSelectionIndex(); if (index >= 0) { table.setSelection(index); table.showSelection(); }
        // Show as a dropdown without Shell.open()'s normal top-level activation/focus restoration.
        popup.setVisible(true); table.setFocus();
    }
    private void commit() {
        if (table == null || table.isDisposed() || table.getSelectionCount() != 1) return;
        String value = table.getSelection()[0].getText(); closePopup(); setText(value); arrow.setFocus();
        notifyListeners(SWT.Selection,new Event());
    }
    public void closePopup() {
        Shell old = popup; popup = null; table = null;
        if (old != null && !old.isDisposed()) old.dispose();
    }
}
