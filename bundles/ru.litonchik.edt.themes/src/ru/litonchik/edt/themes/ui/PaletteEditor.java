package ru.litonchik.edt.themes.ui;

import java.util.*;
import java.util.List;
import java.util.function.*;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import ru.litonchik.edt.themes.model.*;

/** Workspace drafts only; native EDT preferences are written by the coordinator on Apply. */
final class PaletteEditor {
    private record Role(String id,String title,String usage) {}
    private static final List<Role> ROLES = List.of(
        new Role("editor.background","Фон редактора","Редактор, базовый фон EDT, деревья/таблицы, контролы, невыбранные вкладки и консоль."),
        new Role("text","Основной текст","EDT, вкладки, контролы, подсказки, консоль; текст, операторы и скобки BSL."),
        new Role("panel","Фон панелей","Заголовки, панели, выбранные вкладки, подсказки, неактивные/отключённые элементы."),
        new Role("border","Границы","Границы EDT и отключённых контролов."),
        new Role("selection.background","Фон выделения","Выделение текста редактора; выделенная строка списков тем и шрифтов плагина."),
        new Role("selection.text","Текст выделения","Текст выделения редактора и списков тем и шрифтов плагина."),
        new Role("line","Текущая строка","Фон текущей строки редактора."),
        new Role("comment","Комментарии","Комментарии BSL."),
        new Role("keyword","Ключевые слова","Ключевые слова BSL."),
        new Role("string","Строки","Строковые литералы BSL."),
        new Role("number","Числа","Числовые литералы BSL."),
        new Role("directive","Препроцессор","Проверенная категория препроцессора BSL."),
        new Role("accent","Акцент","Акцент EDT, границы при наведении, ссылки редактора и встроенные функции BSL."),
        new Role("error","Ошибки","Заголовок ошибки формы. Не все маркеры и потоки консоли."),
        new Role("warning","Предупреждения","Заголовок предупреждения формы. Не все маркеры и потоки консоли."),
        new Role("disabled","Неактивный текст","Отключённые контролы, невыбранные вкладки и номера строк редактора."),
        new Role("hover","Фон при наведении","Фон штатных контролов EDT при наведении. Hover строк списков плагина отложен.")
    );
    private final PaletteDrafts drafts;
    private final Runnable changed;
    private final Consumer<String> validation;
    private final BiFunction<Palette,String,Color> color;
    private final Group group;
    private final Table table;
    private final Text hex;
    private final Label usage;
    private final ColorSelector picker;
    private final Canvas preview;
    private final Button resetRole,resetPalette;
    private final Map<String,String> invalid = new LinkedHashMap<>();
    private String profile;
    private boolean refreshing,bsl = true;
    PaletteEditor(Composite parent,PaletteDrafts drafts,Runnable changed,Consumer<String> validation,BiFunction<Palette,String,Color> color) {
        this.drafts = drafts; this.changed = changed; this.validation = validation; this.color = color;
        group = new Group(parent,SWT.NONE); group.setText("Палитра — 17 ролей"); group.setLayout(new GridLayout(4,false)); group.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));
        table = new Table(group,SWT.BORDER|SWT.SINGLE|SWT.FULL_SELECTION|SWT.V_SCROLL);
        table.setData("probe.id","palette.roles"); table.setHeaderVisible(true);
        for (String title : List.of("Роль","Цвет","HEX","Значение")) { TableColumn column = new TableColumn(table,SWT.LEFT); column.setText(title); }
        for (Role role : ROLES) { TableItem item = new TableItem(table,SWT.NONE); item.setData(role.id()); }
        int[] widths = {180,48,86,90}; for (int i=0;i<widths.length;i++) table.getColumn(i).setWidth(widths[i]);
        GridData listData = new GridData(SWT.FILL,SWT.FILL,true,false,4,1); listData.heightHint = table.getItemHeight()*6; listData.widthHint = 420; table.setLayoutData(listData);
        // Draw selected foreground and background together, avoiding native white-on-white Dark selection.
        table.addListener(SWT.EraseItem,e -> e.detail &= ~(SWT.FOREGROUND|SWT.BACKGROUND|SWT.SELECTED|SWT.HOT|SWT.FOCUSED));
        table.addListener(SWT.PaintItem,e -> {
            TableItem item = (TableItem)e.item; boolean selected = table.indexOf(item) == table.getSelectionIndex();
            e.gc.setBackground(selected ? table.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION) : table.getBackground());
            Rectangle cell = item.getBounds(e.index);
            e.gc.fillRectangle(cell.x,e.y,table.getColumn(e.index).getWidth(),e.height);
            if (e.index == 1 && profile != null) {
                e.gc.setBackground(color.apply(drafts.resolve(profile),(String)item.getData())); e.gc.fillRectangle(e.x+7,e.y+3,28,Math.max(8,e.height-6));
                e.gc.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND)); e.gc.drawRectangle(e.x+7,e.y+3,28,Math.max(8,e.height-6));
            } else {
                e.gc.setForeground(selected ? table.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT) : table.getForeground());
                e.gc.drawText(item.getText(e.index),e.x+4,e.y+(e.height-e.gc.getFontMetrics().getHeight())/2,true);
            }
        });
        picker = new ColorSelector(group); picker.getButton().setToolTipText("Выбрать цвет роли…");
        hex = new Text(group,SWT.BORDER); hex.setData("probe.id","palette.hex"); hex.setToolTipText("#RRGGBB, например #A6E22E");
        GridData hexData = new GridData(SWT.LEFT,SWT.CENTER,false,false); hexData.widthHint=86; hex.setLayoutData(hexData);
        resetRole = new Button(group,SWT.PUSH); resetRole.setText("Вернуть цвет стиля");
        resetPalette = new Button(group,SWT.PUSH); resetPalette.setText("Сбросить палитру");
        usage = new Label(group,SWT.WRAP); GridData usageData = new GridData(SWT.FILL,SWT.TOP,true,false,4,1); usageData.widthHint=430; usageData.heightHint=55; usage.setLayoutData(usageData);
        preview = new Canvas(group,SWT.DOUBLE_BUFFERED); preview.setData("probe.id","palette.preview");
        GridData previewData = new GridData(SWT.FILL,SWT.TOP,true,false,4,1); previewData.heightHint=78; preview.setLayoutData(previewData);
        preview.addPaintListener(e -> paint(e.gc));
        Label note = new Label(group,SWT.WRAP); note.setText("Образец интерфейса условный. Здесь цвета профиля; ручные настройки EDT и настройки проекта могут отличаться. Явная правка/сброс роли заменяет только её настройки.");
        GridData noteData = new GridData(SWT.FILL,SWT.TOP,true,false,4,1); noteData.widthHint=430; note.setLayoutData(noteData);
        table.addListener(SWT.Selection,e -> showRole());
        hex.addModifyListener(e -> {
            if (refreshing || profile == null) return;
            String key = profile+"/"+role().id();
            try { drafts.edit(profile,role().id(),hex.getText()); invalid.remove(key); refreshColors(); changed.run(); }
            catch (IllegalArgumentException error) { invalid.put(key,hex.getText()); }
            validate();
        });
        picker.addListener(e -> {
            RGB rgb = picker.getColorValue(); hex.setText(String.format(Locale.ROOT,"#%02X%02X%02X",rgb.red,rgb.green,rgb.blue));
        });
        resetRole.addListener(SWT.Selection,e -> {
            drafts.resetRole(profile,role().id()); invalid.remove(profile+"/"+role().id()); refreshColors(); showRole(); validate(); changed.run();
        });
        resetPalette.addListener(SWT.Selection,e -> {
            drafts.resetPalette(profile); invalid.keySet().removeIf(k -> k.startsWith(profile+"/")); refreshColors(); showRole(); validate(); changed.run();
        });
        table.select(0); setProfile(null,true);
    }
    private Role role() { return ROLES.get(Math.max(0,table.getSelectionIndex())); }
    private void validate() { validation.accept(invalid.isEmpty() ? null : "Некорректный HEX в "+invalid.keySet().iterator().next()+". Используйте #RRGGBB или сбросьте цвет."); }
    boolean valid() { return invalid.isEmpty(); }
    void setProfile(String profile,boolean bsl) {
        this.profile=profile; this.bsl=bsl;
        for (Control control : List.of(table,hex,picker.getButton(),resetRole,resetPalette)) control.setEnabled(profile != null);
        refreshColors(); showRole(); preview.redraw();
    }
    private void refreshColors() {
        Palette palette = profile == null ? null : drafts.resolve(profile);
        for (int i=0;i<ROLES.size();i++) {
            Role role = ROLES.get(i); table.getItem(i).setText(new String[]{role.title(),"",palette == null ? "—" : palette.color(role.id()),profile != null && drafts.overrides(profile).containsKey(role.id()) ? "Личный" : "Стиль"});
        }
        if (palette != null) picker.setColorValue(color.apply(palette,role().id()).getRGB());
        table.redraw(); preview.redraw();
    }
    private void showRole() {
        refreshing=true;
        try {
            if (profile == null) { hex.setText(""); usage.setText("Выберите палитру для настройки цветов."); return; }
            Role role = role(); hex.setText(invalid.getOrDefault(profile+"/"+role.id(),drafts.resolve(profile).color(role.id())));
            var targets = SettingCatalog.load().entries().stream().filter(e -> e.role().equals(role.id())).toList();
            boolean affectsBsl = targets.stream().anyMatch(e -> e.node().equals("com._1c.g5.v8.dt.bsl.ui"));
            usage.setText(role.usage()+" Настроек RGB: "+targets.size()+"."+(!bsl && affectsBsl ? " BSL выключен: цвет сохранится, но к BSL сейчас не применяется." : ""));
            usage.setToolTipText(targets.stream().map(e -> e.node()+" / "+e.key()).collect(java.util.stream.Collectors.joining("\n")));
            picker.setColorValue(color.apply(drafts.resolve(profile),role.id()).getRGB());
        } finally { refreshing=false; }
    }
    private void paint(GC gc) {
        if (profile == null) { gc.setBackground(preview.getParent().getBackground()); gc.fillRectangle(preview.getClientArea()); return; }
        Palette p = drafts.resolve(profile); Rectangle r = preview.getClientArea();
        gc.setBackground(color.apply(p,"editor.background")); gc.fillRectangle(r);
        gc.setBackground(color.apply(p,"panel")); gc.fillRectangle(1,1,r.width-2,24);
        gc.setForeground(color.apply(p,"text")); gc.drawText("Панель · текст",8,5,true);
        gc.setForeground(color.apply(p,"disabled")); gc.drawText("Неактивный",155,5,true);
        gc.setBackground(color.apply(p,"selection.background")); gc.fillRectangle(6,31,115,21);
        gc.setForeground(color.apply(p,"selection.text")); gc.drawText("Выделение",10,33,true);
        gc.setBackground(color.apply(p,"hover")); gc.fillRectangle(129,31,125,21);
        gc.setForeground(color.apply(p,"text")); gc.drawText("При наведении",133,33,true);
        gc.setForeground(color.apply(p,"accent")); gc.drawText("Ссылка",265,33,true);
        gc.setForeground(color.apply(p,"error")); gc.drawText("Ошибка формы",8,57,true);
        gc.setForeground(color.apply(p,"warning")); gc.drawText("Предупреждение",155,57,true);
        gc.setForeground(color.apply(p,"border")); gc.drawRectangle(0,0,r.width-1,r.height-1);
    }
}
