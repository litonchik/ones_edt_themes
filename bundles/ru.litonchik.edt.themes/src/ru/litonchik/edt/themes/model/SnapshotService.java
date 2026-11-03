package ru.litonchik.edt.themes.model;

import java.util.*;

/** Journal is flushed before any mutation. No UI or Eclipse dependencies. */
public final class SnapshotService {
    private final SettingAccess access;
    private final SnapshotStore store;
    private SettingsSnapshot snapshot;
    public SnapshotService(SettingAccess access, SnapshotStore store) throws Exception {
        this.access = access; this.store = store; snapshot = store.load();
    }
    public SettingsSnapshot snapshot() { return snapshot; }

    public Map<SettingKey,OriginalValue> withOriginals(Map<SettingKey,OriginalValue> desired, Set<SettingKey> protectedKeys) throws Exception {
        var plan = new LinkedHashMap<>(desired);
        for (SettingKey key : protectedKeys) {
            OriginalValue value = snapshot.original().get(key);
            plan.put(key,value == null ? access.read(key) : value);
        }
        return plan;
    }

    public Map<SettingKey,OriginalValue> withNativeDefaults(Map<SettingKey,OriginalValue> desired, Map<SettingKey,OriginalValue> nativeDefaults) throws Exception {
        var plan = new LinkedHashMap<>(desired);
        for (var entry : nativeDefaults.entrySet()) {
            if (plan.containsKey(entry.getKey())) continue;
            OriginalValue original = snapshot.original().get(entry.getKey());
            if (original == null) original = access.read(entry.getKey());
            plan.put(entry.getKey(),original.present() ? original : entry.getValue());
        }
        return plan;
    }

    public Map<SettingKey,OriginalValue> observeValues(java.util.function.Predicate<SettingKey> filter) throws Exception {
        var observed = new LinkedHashMap<SettingKey,OriginalValue>();
        for (SettingKey key : snapshot.original().keySet()) if (filter.test(key)) observed.put(key,access.read(key));
        return Collections.unmodifiableMap(observed);
    }

    public void capture(Set<SettingKey> keys) throws Exception {
        if (snapshot.pending()) throw new IllegalStateException("Незавершённый журнал");
        var original = new LinkedHashMap<>(snapshot.original());
        for (SettingKey key : keys) if (!original.containsKey(key)) original.put(key,access.read(key));
        SettingsSnapshot captured = new SettingsSnapshot(original,snapshot.lastApplied(),Map.of(),Map.of(),snapshot.surrendered());
        store.save(captured); snapshot = captured;
    }

    public SettingsSnapshot apply(Map<SettingKey,OriginalValue> desired) throws Exception {
        return apply(desired,Set.of(),Map.of());
    }

    /** Explicit user edits (such as picking a font) may replace a later native-settings edit. */
    public SettingsSnapshot applyExplicit(Map<SettingKey,OriginalValue> desired) throws Exception {
        return apply(desired,desired.keySet(),Map.of());
    }

    public SettingsSnapshot apply(Map<SettingKey,OriginalValue> desired, Set<SettingKey> explicit, Map<SettingKey,OriginalValue> personal) throws Exception {
        if (!Collections.disjoint(desired.keySet(),personal.keySet())) throw new IllegalArgumentException("Личные цвета не входят в original snapshot");
        if (snapshot.pending()) throw new IllegalStateException("Сначала восстановите незавершённый журнал");
        var original = new LinkedHashMap<>(snapshot.original());
        var applied = new LinkedHashMap<>(snapshot.lastApplied());
        var surrendered = new HashSet<>(snapshot.surrendered());
        surrendered.removeAll(explicit);
        var changes = new LinkedHashMap<SettingKey,OriginalValue>();
        // Drop targets no longer requested (for example the BSL checkbox).
        for (SettingKey key : snapshot.lastApplied().keySet()) {
            OriginalValue current = access.read(key);
            if (!current.equals(applied.get(key)) && !explicit.contains(key)) { surrendered.add(key); applied.remove(key); }
            else if (!desired.containsKey(key)) { changes.put(key, original.get(key)); applied.remove(key); }
        }
        for (var entry : desired.entrySet()) {
            SettingKey key = entry.getKey();
            if (surrendered.contains(key)) continue;
            OriginalValue current = access.read(key);
            original.putIfAbsent(key,current);
            applied.put(key,entry.getValue()); changes.put(key,entry.getValue());
        }
        // Theme setters may rewrite even relinquished colors. Reassert their current values
        // after the theme transition, without taking ownership of them again.
        for (SettingKey key : surrendered) changes.put(key,access.read(key));
        changes.putAll(personal);
        transact(changes, new SettingsSnapshot(original,applied,Map.of(),Map.of(),surrendered));
        return snapshot;
    }

    public RestoreResult restore(boolean force) { return restore(force,Map.of()); }

    public RestoreResult restore(boolean force, Map<SettingKey,OriginalValue> observedBeforeNativeChange) {
        return restore(force,observedBeforeNativeChange,Map.of());
    }

    public RestoreResult restore(boolean force, Map<SettingKey,OriginalValue> observedBeforeNativeChange, Map<SettingKey,OriginalValue> personal) {
        if (snapshot.pending() && !personal.isEmpty())
            return new RestoreResult(Set.of(),Set.of(),Map.of(),"Сначала восстановите незавершённый журнал; личные цвета ещё не сохранены");
        if (snapshot.pending()) return recover();
        var changes = new LinkedHashMap<SettingKey,OriginalValue>();
        var conflicts = new HashSet<SettingKey>();
        var errors = new LinkedHashMap<SettingKey,String>();
        for (var entry : snapshot.original().entrySet()) {
            SettingKey key = entry.getKey();
            try {
                OriginalValue current = access.read(key);
                OriginalValue compared = observedBeforeNativeChange.getOrDefault(key,current);
                if (force || compared.equals(snapshot.lastApplied().get(key)) || compared.equals(entry.getValue())) changes.put(key,entry.getValue());
                else {
                    conflicts.add(key);
                    if (!current.equals(compared)) changes.put(key,compared); // Undo the native overwrite of a manual color.
                }
            } catch (Exception e) { errors.put(key,e.toString()); }
        }
        if (!errors.isEmpty()) return new RestoreResult(Set.of(),conflicts,errors);
        var remaining = new LinkedHashMap<>(snapshot.original());
        changes.keySet().stream().filter(k -> !conflicts.contains(k)).forEach(remaining::remove);
        var result = new SettingsSnapshot(remaining,Map.of(),Map.of(),Map.of(),conflicts);
        changes.putAll(personal);
        try { transact(changes,result); }
        catch (Exception e) { changes.keySet().forEach(key -> errors.put(key,e.toString())); return new RestoreResult(Set.of(),conflicts,errors,e.toString()); }
        var restored = new HashSet<>(changes.keySet()); restored.removeAll(conflicts);
        return new RestoreResult(restored,conflicts,errors);
    }

    private void transact(Map<SettingKey,OriginalValue> changes, SettingsSnapshot completed) throws Exception {
        var before = new LinkedHashMap<SettingKey,OriginalValue>();
        for (SettingKey key : changes.keySet()) before.put(key,access.read(key));
        SettingsSnapshot old = snapshot;
        // Keep the old managed values until commit, plus newly captured originals.
        var originals = new LinkedHashMap<>(old.original());
        completed.original().forEach(originals::putIfAbsent);
        SettingsSnapshot journal = new SettingsSnapshot(originals,old.lastApplied(),before,changes,old.surrendered());
        store.save(journal);
        snapshot = journal;
        try {
            for (var entry : changes.entrySet()) access.write(entry.getKey(),entry.getValue());
            access.flush();
            store.save(completed); snapshot = completed;
        } catch (Exception failure) {
            boolean rolledBack = true;
            // Theme setters can seed other managed preferences before their explicit write.
            // Restore in plan order: native theme first, exact preferences afterwards.
            for (SettingKey key : changes.keySet()) {
                try { access.write(key,before.get(key)); }
                catch (Exception e) { rolledBack = false; failure.addSuppressed(e); }
            }
            try { access.flush(); } catch (Exception e) { rolledBack = false; failure.addSuppressed(e); }
            if (rolledBack) {
                try { store.save(old); snapshot = old; }
                catch (Exception e) { failure.addSuppressed(e); }
            }
            throw failure;
        }
    }

    public RestoreResult recover() {
        var restored = new HashSet<SettingKey>();
        var conflicts = new HashSet<>(snapshot.surrendered());
        var errors = new LinkedHashMap<SettingKey,String>();
        if (!snapshot.pending()) return new RestoreResult(restored,conflicts,errors);
        for (var entry : snapshot.pendingBefore().entrySet()) {
            SettingKey key = entry.getKey();
            try {
                OriginalValue current = access.read(key);
                if (current.equals(snapshot.pendingAfter().get(key)) || current.equals(entry.getValue())) {
                    access.write(key,entry.getValue()); restored.add(key);
                } else conflicts.add(key);
            } catch (Exception e) { errors.put(key,e.toString()); }
        }
        try {
            access.flush();
            if (errors.isEmpty()) {
                var applied = new LinkedHashMap<>(snapshot.lastApplied());
                conflicts.forEach(applied::remove);
                var managedConflicts = new HashSet<>(conflicts); managedConflicts.retainAll(snapshot.original().keySet());
                SettingsSnapshot recovered = new SettingsSnapshot(snapshot.original(),applied,Map.of(),Map.of(),managedConflicts);
                store.save(recovered); snapshot = recovered;
            }
        } catch (Exception e) { snapshot.pendingBefore().keySet().forEach(key -> errors.put(key,e.toString())); }
        return new RestoreResult(restored,conflicts,errors);
    }
}
