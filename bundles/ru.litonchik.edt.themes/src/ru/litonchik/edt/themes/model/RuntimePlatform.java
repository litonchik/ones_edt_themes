package ru.litonchik.edt.themes.model;

/** Runtime policy separated from OSGi so platform combinations can be checked without SWT. */
public final class RuntimePlatform {
    private RuntimePlatform() {}
    public static void verify(String os,String architecture,int javaVersion) {
        verify(os,architecture,javaVersion,17);
    }
    public static void verify(String os,String architecture,int javaVersion,int requiredJava) {
        if (!("win32".equals(os) || "macosx".equals(os)) || !"x86_64".equals(architecture))
            throw new IllegalStateException("Плагин рассчитан на Windows x64 или macOS x64; обнаружено " + os + "/" + architecture);
        if ((requiredJava != 17 && requiredJava != 25) || javaVersion != requiredJava)
            throw new IllegalStateException("Для этой сборки EDT выберите Java " + requiredJava + " через -vm");
    }
}
