package ru.litonchik.edt.themes.model;

import java.util.*;

/** Immutable durable journal. Originals also retain relinquished user conflicts. */
public record SettingsSnapshot(Map<SettingKey,OriginalValue> original,
        Map<SettingKey,OriginalValue> lastApplied, Map<SettingKey,OriginalValue> pendingBefore,
        Map<SettingKey,OriginalValue> pendingAfter, Set<SettingKey> surrendered) {
    public SettingsSnapshot {
        original = copy(original); lastApplied = copy(lastApplied);
        pendingBefore = copy(pendingBefore); pendingAfter = copy(pendingAfter);
        surrendered = Set.copyOf(surrendered);
        if (!original.keySet().containsAll(lastApplied.keySet()) || !original.keySet().containsAll(surrendered)
                || !pendingBefore.keySet().equals(pendingAfter.keySet()))
            throw new IllegalArgumentException("Повреждённый снимок");
    }
    private static Map<SettingKey,OriginalValue> copy(Map<SettingKey,OriginalValue> map) {
        map.forEach((k,v) -> { Objects.requireNonNull(k); Objects.requireNonNull(v); });
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }
    public static SettingsSnapshot empty() { return new SettingsSnapshot(Map.of(),Map.of(),Map.of(),Map.of(),Set.of()); }
    public boolean pending() { return !pendingBefore.isEmpty(); }
}
