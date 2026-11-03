package ru.litonchik.edt.themes.model;

public record SettingKey(String scope, String node, String key) {
    public SettingKey {
        if (scope == null || node == null || key == null || scope.isBlank() || node.isBlank() || key.isBlank())
            throw new IllegalArgumentException("Пустой адрес настройки");
    }
}
