package ru.litonchik.edt.themes.runtime;

import java.util.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;
import ru.litonchik.edt.themes.model.*;

/** Two slots: fully flush the next journal, then publish its pointer. */
public final class SnapshotPreferences implements SnapshotStore {
    public static final String PLUGIN_ID = "ru.litonchik.edt.themes";
    private final IEclipsePreferences root;
    public SnapshotPreferences() { root = InstanceScope.INSTANCE.getNode(PLUGIN_ID); }
    /** Separate journal for the independent font setting. */
    public SnapshotPreferences(String section) {
        if (!"font".equals(section)) throw new IllegalArgumentException("Неизвестный раздел журнала");
        root = (IEclipsePreferences)InstanceScope.INSTANCE.getNode(PLUGIN_ID).node(section);
    }
    @Override public SettingsSnapshot load() throws Exception {
        String slot = root.get("journalSlot",null);
        if (slot == null) return SettingsSnapshot.empty();
        if (!slot.equals("0") && !slot.equals("1")) throw new IllegalArgumentException("Повреждён указатель журнала");
        if (!root.nodeExists("journal/"+slot)) throw new IllegalArgumentException("Не найден журнал восстановления");
        Preferences node = root.node("journal/"+slot);
        var fields = new LinkedHashMap<String,String>();
        for (String key : node.keys()) fields.put(key,node.get(key,null));
        for (String child : node.childrenNames()) {
            Preferences entry = node.node(child);
            for (String key : entry.keys()) fields.put(child+"."+key,entry.get(key,null));
        }
        return SnapshotCodec.decode(fields);
    }
    @Override public void save(SettingsSnapshot snapshot) throws Exception {
        String current = root.get("journalSlot",null);
        String next = "0".equals(current) ? "1" : "0";
        Preferences parent = root.node("journal");
        if (parent.nodeExists(next)) parent.node(next).removeNode();
        Preferences node = parent.node(next);
        for (var field : SnapshotCodec.encode(snapshot).entrySet()) {
            int dot = field.getKey().indexOf('.');
            if (dot < 0) node.put(field.getKey(),field.getValue());
            else node.node(field.getKey().substring(0,dot)).put(field.getKey().substring(dot+1),field.getValue());
        }
        parent.flush();
        root.put("journalSlot",next);
        try { root.flush(); }
        catch (Exception e) { if (current == null) root.remove("journalSlot"); else root.put("journalSlot",current); throw e; }
    }
    public String activeProfile() { return root.get("activeProfile",null); }
    public boolean includeBsl() { return root.getBoolean("includeBsl",true); }
    public void saveProfile(String profile, boolean bsl) throws Exception {
        if (profile == null) root.remove("activeProfile"); else root.put("activeProfile",profile);
        root.putBoolean("includeBsl",bsl); root.flush();
    }
}
