package ru.litonchik.edt.themes.model;

import java.io.*;
import java.util.*;

/** Exact native metadata resource names, independently mapped to upstream artwork. */
public final class IconCatalog {
    private static final Map<String,String> NAMES=load();
    private static final Set<String> ASSETS=Set.copyOf(NAMES.values());
    private IconCatalog() {}
    private static Map<String,String> load() {
        var properties=new Properties();
        try(var input=IconCatalog.class.getResourceAsStream("/resources/chroma-icons.properties")) {
            if(input==null)throw new IllegalStateException("Нет карты значков v8-chroma");
            properties.load(new InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8));
        } catch(IOException e){throw new IllegalStateException(e);}
        var result=new TreeMap<String,String>();
        for(String name:properties.stringPropertyNames()) {
            String asset=properties.getProperty(name);
            if(!name.matches("[a-z0-9_]+") || !asset.matches("meta-[a-z0-9-]+"))throw new IllegalStateException("Неверная карта значков");
            result.put(name,asset);
        }
        return Collections.unmodifiableMap(result);
    }
    public static Map<String,String> nativeNames(){return NAMES;}
    public static Set<String> assets(){return ASSETS;}
}
