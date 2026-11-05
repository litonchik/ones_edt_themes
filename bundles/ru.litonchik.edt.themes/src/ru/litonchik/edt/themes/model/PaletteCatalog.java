package ru.litonchik.edt.themes.model;

import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.List;

public final class PaletteCatalog {
    public record Profile(String id, String label, String source, ThemeMode mode) {
        public Profile(String id, String label, String source) { this(id,label,source,ThemeMode.DARK); }
        public String resourcePath() { return "/resources/palettes/" + id + ".properties"; }
    }
    private static final List<Profile> PROFILES = List.of(
        new Profile("darcula","Darcula","MVP 0.1.0"),
        new Profile("monokai","Monokai","MVP 0.1.0"),
        new Profile("one-dark-pro","One Dark Pro","Binaryify/OneDark-Pro ce79c5f7116607d635e06b7ef4413d85a77905e4; MIT"),
        new Profile("dracula","Dracula","dracula/visual-studio-code a08a206f2c8420ba3c05f0e8d01d43b2f933fdf8; MIT"),
        new Profile("github-dark-default","GitHub Dark Default","primer/github-vscode-theme cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf; Primer 7.10.0; MIT"),
        new Profile("winter-is-coming-dark-blue","Winter is Coming (Dark Blue)","johnpapa/vscode-winteriscoming 260547834cb6ac37dd5b8bb5842cc1c8d3164946; MIT"),
        new Profile("night-owl","Night Owl","sdras/night-owl-vscode-theme cc291eba7976b20d7c66bde6883c27b902196b07; MIT"),
        new Profile("ayu-dark","Ayu Dark (Bordered)","ayu-theme/vscode-ayu 2c6feb7215bf7ca6c8c9a51066f255025df3a5b9; MIT"),
        new Profile("shades-of-purple","Shades of Purple","ahmadawais/shades-of-purple-vscode e8eb49f33e5db05ceba6677367b33ddb27ad821c; MIT"),
        new Profile("noctis-minimus","Noctis Minimus","liviuschera/noctis 4a82370b2c064e36726c23e94f27ed3782bd6ecd; MIT"),
        new Profile("visual-studio-dark-cpp","Visual Studio Dark (C/C++)","microsoft/vscode-cpptools eb981644369c99d39054578a769af572c24bd1eb; Themes 2.1.0; MIT"),
        new Profile("github-light-default","GitHub Light Default","primer/github-vscode-theme cd78e5e4e7bcf132a6f428ae0f32264bb1b729cf; Primer 7.10.0; MIT",ThemeMode.LIGHT),
        new Profile("winter-is-coming-light","Winter is Coming (Light)","johnpapa/vscode-winteriscoming 260547834cb6ac37dd5b8bb5842cc1c8d3164946; 1.5.0; MIT",ThemeMode.LIGHT),
        new Profile("night-owl-light","Night Owl Light","sdras/night-owl-vscode-theme cc291eba7976b20d7c66bde6883c27b902196b07; 2.1.1; MIT",ThemeMode.LIGHT),
        new Profile("ayu-light","Ayu Light (Bordered)","ayu-theme/vscode-ayu 7f438df22446f02e79cdfc2f3771b0b273f34654; 1.5.0; MIT",ThemeMode.LIGHT),
        new Profile("noctis-lux","Noctis Lux","liviuschera/noctis 4a82370b2c064e36726c23e94f27ed3782bd6ecd; 10.43.3; MIT",ThemeMode.LIGHT),
        new Profile("visual-studio-light-cpp","Visual Studio Light (C/C++)","microsoft/vscode-cpptools 3c14e3155a42976f765828b5ca2bbd9154a9aa54; Themes 2.1.0; MIT",ThemeMode.LIGHT),
        new Profile("noctis-hibernus","Noctis Hibernus","liviuschera/noctis 4a82370b2c064e36726c23e94f27ed3782bd6ecd; 10.43.3; MIT",ThemeMode.LIGHT),
        new Profile("noctis-lilac","Noctis Lilac","liviuschera/noctis 4a82370b2c064e36726c23e94f27ed3782bd6ecd; 10.43.3; MIT",ThemeMode.LIGHT)
    );
    private PaletteCatalog() {}
    public static List<Profile> profiles() { return PROFILES; }
    public static List<Profile> profiles(ThemeMode mode) { return PROFILES.stream().filter(p -> p.mode() == mode).toList(); }
    public static ThemeMode mode(String id) { return PROFILES.stream().filter(p -> p.id().equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("Неизвестный профиль: " + id)).mode(); }
    public static List<String> ids() { return PROFILES.stream().map(Profile::id).toList(); }
    public static boolean isKnown(String id) { return PROFILES.stream().anyMatch(p -> p.id().equals(id)); }
    public static Palette load(String profileId) {
        Profile profile = PROFILES.stream().filter(p -> p.id().equals(profileId)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Неизвестный профиль: " + profileId));
        try (InputStream stream = PaletteCatalog.class.getResourceAsStream(profile.resourcePath())) {
            if (stream == null) throw new IllegalArgumentException("Нет ресурса палитры: " + profileId);
            Properties properties = new Properties();
            properties.load(stream);
            var colors = new LinkedHashMap<String,String>();
            for (String name : properties.stringPropertyNames()) colors.put(name, properties.getProperty(name));
            return new Palette(profileId, colors);
        } catch (IOException e) { throw new IllegalArgumentException("Не удалось прочитать палитру", e); }
    }
}
