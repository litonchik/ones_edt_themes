package ru.litonchik.edt.themes.model;

import java.net.URI;
import java.util.*;

/** Bounded parser for the flat string-object schema produced by prepare-update-site.py. */
public record ReleaseInfo(ReleaseVersion version) {
    public static final String PROJECT="https://github.com/litonchik/ones_edt_themes/releases/tag/v";
    public ReleaseInfo {Objects.requireNonNull(version);}
    public URI releaseUri(){return URI.create(PROJECT+version);}
    public static ReleaseInfo parse(String json){
        var fields=new FlatObject(json).read();
        var version=ReleaseVersion.parse(fields.get("version"));
        if(!Objects.equals(fields.get("release_url"),PROJECT+version))throw new IllegalArgumentException("Неверная ссылка релиза");
        String bundle=fields.get("bundle_version");
        if(bundle==null || !bundle.matches(java.util.regex.Pattern.quote(version.toString())+"\\.[A-Za-z0-9_-]+"))throw new IllegalArgumentException("Неверная версия bundle");
        return new ReleaseInfo(version);
    }
    private static final class FlatObject {
        private final String s;private int at;
        FlatObject(String s){if(s==null || s.length()>8192)throw new IllegalArgumentException("Размер JSON");this.s=s;}
        Map<String,String> read(){
            var fields=new LinkedHashMap<String,String>();take('{');skip();
            if(peek('}'))at++;else while(true){String key=string();take(':');String value=string();if(fields.putIfAbsent(key,value)!=null || fields.size()>20)bad();skip();if(peek('}')){at++;break;}take(',');}
            skip();if(at!=s.length())bad();return fields;
        }
        private String string(){
            take('"');var out=new StringBuilder();
            while(at<s.length()){
                char c=s.charAt(at++);if(c=='"')return out.toString();if(c<0x20)bad();
                if(c=='\\'){
                    if(at==s.length())bad();char escaped=s.charAt(at++);
                    switch(escaped){
                        case '"','\\','/' -> out.append(escaped);
                        case 'b' -> out.append('\b');case 'f' -> out.append('\f');case 'n' -> out.append('\n');case 'r' -> out.append('\r');case 't' -> out.append('\t');
                        case 'u' -> {if(at+4>s.length())bad();String hex=s.substring(at,at+4);if(!hex.matches("[0-9a-fA-F]{4}"))bad();out.append((char)Integer.parseInt(hex,16));at+=4;}
                        default -> bad();
                    }
                }else out.append(c);
            }
            bad();return "";
        }
        private void take(char c){skip();if(!peek(c))bad();at++;}
        private boolean peek(char c){return at<s.length() && s.charAt(at)==c;}
        private void skip(){while(at<s.length() && " \t\r\n".indexOf(s.charAt(at))>=0)at++;}
        private static void bad(){throw new IllegalArgumentException("Некорректный JSON релиза");}
    }
}
