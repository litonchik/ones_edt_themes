package ru.litonchik.edt.themes.model;

public interface SettingAccess {
    OriginalValue read(SettingKey key) throws Exception;
    void write(SettingKey key, OriginalValue value) throws Exception;
    void flush() throws Exception;
}
