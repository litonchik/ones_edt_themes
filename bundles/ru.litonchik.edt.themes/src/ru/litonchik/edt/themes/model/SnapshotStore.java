package ru.litonchik.edt.themes.model;

public interface SnapshotStore {
    SettingsSnapshot load() throws Exception;
    void save(SettingsSnapshot snapshot) throws Exception;
}
