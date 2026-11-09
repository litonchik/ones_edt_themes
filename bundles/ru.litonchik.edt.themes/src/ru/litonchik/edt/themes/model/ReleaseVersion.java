package ru.litonchik.edt.themes.model;

/** Stable release number; an installed bundle's build qualifier is not an update. */
public record ReleaseVersion(int major,int minor,int micro) implements Comparable<ReleaseVersion> {
    public ReleaseVersion {if(major<0 || minor<0 || micro<0)throw new IllegalArgumentException("Отрицательная версия");}
    public static ReleaseVersion parse(String text){
        if(text==null || !text.matches("(?:0|[1-9][0-9]{0,9})\\.(?:0|[1-9][0-9]{0,9})\\.(?:0|[1-9][0-9]{0,9})"))throw new IllegalArgumentException("Некорректная версия");
        var p=text.split("\\.");return new ReleaseVersion(Integer.parseInt(p[0]),Integer.parseInt(p[1]),Integer.parseInt(p[2]));
    }
    @Override public int compareTo(ReleaseVersion other){int n=Integer.compare(major,other.major);if(n==0)n=Integer.compare(minor,other.minor);return n==0?Integer.compare(micro,other.micro):n;}
    @Override public String toString(){return major+"."+minor+"."+micro;}
}
