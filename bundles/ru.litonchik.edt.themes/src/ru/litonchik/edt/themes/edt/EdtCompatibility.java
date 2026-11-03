package ru.litonchik.edt.themes.edt;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import ru.litonchik.edt.themes.model.RuntimePlatform;

public final class EdtCompatibility {
    private EdtCompatibility() {}
    public static void verify() {
        Bundle bsl = Platform.getBundle("com._1c.g5.v8.dt.bsl.ui");
        String version = bsl == null ? "" : bsl.getVersion().toString();
        int java;
        switch (version) {
            case "23.0.1.v202607090722", "23.0.1.v202609041212" -> {
                require("org.eclipse.platform", "4.30.0");
                require("org.eclipse.xtext.ui", "2.33.0");
                requireExact("com._1c.g5.v8.dt.theming.ui", "2.1.0." + version.substring(version.indexOf('v')));
                java = 17;
            }
            case "24.0.0.v202610211527" -> {
                require("org.eclipse.platform", "4.38.0");
                require("org.eclipse.xtext.ui", "2.41.0");
                requireExact("com._1c.g5.v8.dt.theming.ui", "2.1.1.v202610211527");
                java = 25;
            }
            default -> throw new IllegalStateException("Проверены EDT 2026.1.2, 2026.1.3 и 2026.2.2; отличается BSL UI: " + version);
        }
        RuntimePlatform.verify(Platform.getOS(),Platform.getOSArch(),Runtime.version().feature(),java);
    }
    private static void require(String id, String version) {
        Bundle bundle = Platform.getBundle(id);
        if (bundle == null || !bundle.getVersion().toString().startsWith(version + ".")) throw new IllegalStateException("Неподдерживаемая платформа: " + id + ", ожидается " + version);
    }
    private static void requireExact(String id, String version) {
        Bundle bundle = Platform.getBundle(id);
        if (bundle == null || !bundle.getVersion().toString().equals(version)) throw new IllegalStateException("Непроверенная сборка EDT; отличается " + id + ", ожидается " + version);
    }
}
