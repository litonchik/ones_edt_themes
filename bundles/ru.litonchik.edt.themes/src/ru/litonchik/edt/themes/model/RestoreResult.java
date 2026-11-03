package ru.litonchik.edt.themes.model;

import java.util.*;

public record RestoreResult(Set<SettingKey> restored, Set<SettingKey> conflicts, Map<SettingKey,String> errors, String operationError) {
    public RestoreResult(Set<SettingKey> restored, Set<SettingKey> conflicts, Map<SettingKey,String> errors) { this(restored,conflicts,errors,null); }
    public RestoreResult { restored = Set.copyOf(restored); conflicts = Set.copyOf(conflicts); errors = Map.copyOf(errors); }
    public boolean successful() { return errors.isEmpty() && operationError == null; }
    public String errorSummary() { return operationError == null ? errors.toString() : operationError + "; " + errors; }
}
