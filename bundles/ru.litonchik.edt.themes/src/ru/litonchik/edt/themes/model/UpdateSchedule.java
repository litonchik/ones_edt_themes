package ru.litonchik.edt.themes.model;

public final class UpdateSchedule {
    public static final long DAY=24L*60*60*1000;
    private UpdateSchedule(){}
    public static long delay(long now,long last){if(last<=0 || now<last || now-last>=DAY)return 0;return DAY-(now-last);}
}
