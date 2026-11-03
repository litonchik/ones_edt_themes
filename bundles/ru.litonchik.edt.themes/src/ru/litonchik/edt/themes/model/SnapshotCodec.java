package ru.litonchik.edt.themes.model;

import java.util.*;

/** Flat fields are persisted as child nodes, never by splitting preference keys. */
public final class SnapshotCodec {
    private SnapshotCodec() {}
    public static Map<String,String> encode(SettingsSnapshot s) {
        var fields = new LinkedHashMap<String,String>(); fields.put("version","1");
        var keys = new LinkedHashSet<>(s.original().keySet()); keys.addAll(s.pendingBefore().keySet());
        fields.put("count",Integer.toString(keys.size()));
        int i = 0;
        for (SettingKey key : keys) {
            String p = "e" + i++ + ".";
            fields.put(p+"scope",key.scope()); fields.put(p+"node",key.node()); fields.put(p+"key",key.key());
            encodeValue(fields,p+"original",s.original().get(key));
            encodeValue(fields,p+"applied",s.lastApplied().get(key));
            encodeValue(fields,p+"before",s.pendingBefore().get(key));
            encodeValue(fields,p+"after",s.pendingAfter().get(key));
            fields.put(p+"surrendered",Boolean.toString(s.surrendered().contains(key)));
        }
        return fields;
    }
    private static void encodeValue(Map<String,String> fields, String p, OriginalValue value) {
        fields.put(p+".state",value == null ? "none" : value.present() ? "present" : "absent");
        if (value != null && value.present()) fields.put(p+".value",value.value());
    }
    public static SettingsSnapshot decode(Map<String,String> fields) {
        if (!"1".equals(fields.get("version"))) throw new IllegalArgumentException("Неизвестная версия снимка");
        try {
            int count = Integer.parseInt(required(fields,"count"));
            if (count < 0 || count > 1000) throw new IllegalArgumentException("Некорректный размер снимка");
            var original = new LinkedHashMap<SettingKey,OriginalValue>(); var applied = new LinkedHashMap<SettingKey,OriginalValue>();
            var before = new LinkedHashMap<SettingKey,OriginalValue>(); var after = new LinkedHashMap<SettingKey,OriginalValue>();
            var surrendered = new HashSet<SettingKey>(); var seen = new HashSet<SettingKey>();
            for (int i = 0; i < count; i++) {
                String p = "e" + i + ".";
                SettingKey key = new SettingKey(required(fields,p+"scope"),required(fields,p+"node"),required(fields,p+"key"));
                if (!seen.add(key)) throw new IllegalArgumentException("Повторный ключ");
                decodeValue(fields,p+"original",original,key); decodeValue(fields,p+"applied",applied,key);
                decodeValue(fields,p+"before",before,key); decodeValue(fields,p+"after",after,key);
                String flag = required(fields,p+"surrendered");
                if (!flag.equals("true") && !flag.equals("false")) throw new IllegalArgumentException("Некорректный флаг");
                if (flag.equals("true")) surrendered.add(key);
            }
            return new SettingsSnapshot(original,applied,before,after,surrendered);
        } catch (RuntimeException e) { throw new IllegalArgumentException("Повреждённый снимок; сохраните журнал для восстановления",e); }
    }
    private static void decodeValue(Map<String,String> fields, String p, Map<SettingKey,OriginalValue> dest, SettingKey key) {
        switch (required(fields,p+".state")) {
            case "none": break;
            case "absent": dest.put(key,OriginalValue.absent()); break;
            case "present": dest.put(key,OriginalValue.of(required(fields,p+".value"))); break;
            default: throw new IllegalArgumentException("Некорректное состояние значения");
        }
    }
    private static String required(Map<String,String> fields, String name) {
        String value = fields.get(name);
        if (value == null) throw new IllegalArgumentException("Нет поля: " + name);
        return value;
    }
}
