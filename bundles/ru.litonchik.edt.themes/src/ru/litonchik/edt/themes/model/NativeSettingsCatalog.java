package ru.litonchik.edt.themes.model;

import java.io.*;
import java.util.*;

/** Exact color writes of the target's native Dark CSS, including settings outside palette roles. */
public final class NativeSettingsCatalog {
    private NativeSettingsCatalog() {}
    public static Map<SettingKey,OriginalValue> load() {
        try (InputStream stream = NativeSettingsCatalog.class.getResourceAsStream("/resources/edt-2026.1-native-preferences.properties")) {
            if (stream == null) throw new IllegalArgumentException("Нет карты штатных цветовых preferences");
            Properties p = new Properties(); p.load(stream);
            var plan = new LinkedHashMap<SettingKey,OriginalValue>();
            Set<String> nodes = Set.of("org.eclipse.ui.workbench","org.eclipse.ui.editors","com._1c.g5.lwt","com._1c.g5.v8.dt.theming.ui","com._1c.g5.v8.dt.bsl.ui","com._1c.g5.v8.dt.ql.ui","com._1c.g5.v8.dt.ql.dcs.ui");
            for (String id : new TreeSet<>(p.stringPropertyNames())) {
                String[] fields = p.getProperty(id).split("\\|",-1);
                if (fields.length != 3 || !nodes.contains(fields[0]) || fields[1].toLowerCase(Locale.ROOT).matches(".*(font|keybinding|formatter|encoding|modeldata).*")) throw new IllegalArgumentException("Неверная native карта: " + id);
                String value = fields[2];
                if (fields[1].endsWith(".SystemDefault")) {
                    if (!value.equals("true") && !value.equals("false")) throw new IllegalArgumentException("Неверный SystemDefault");
                } else {
                    if (!value.matches("\\d{1,3},\\d{1,3},\\d{1,3}")) throw new IllegalArgumentException("Неверный native RGB");
                    for (String c : value.split(",")) if (Integer.parseInt(c) > 255) throw new IllegalArgumentException("RGB > 255");
                }
                if (plan.put(new SettingKey("instance",fields[0],fields[1]),OriginalValue.of(value)) != null) throw new IllegalArgumentException("Повторный native key");
            }
            return Collections.unmodifiableMap(plan);
        } catch (IOException e) { throw new IllegalArgumentException("Не удалось прочитать native карту",e); }
    }
}
