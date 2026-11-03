package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import java.io.IOException;
import java.util.*;
import org.junit.Test;
import ru.litonchik.edt.themes.model.*;

public class SnapshotServiceTest {
    @Test public void pendingRecoveryDoesNotPretendPersonalSaveSucceeded() throws Exception {
        SettingKey personal = new SettingKey("instance","plugin","palette.darcula.keyword");
        Memory m = new Memory();
        m.saved = new SettingsSnapshot(Map.of(A,ORIGINAL),Map.of(),Map.of(A,ORIGINAL),Map.of(A,PROFILE),Set.of());
        m.values.put(A,PROFILE);
        var result = m.service().restore(false,Map.of(),Map.of(personal,USER));
        assertFalse(result.successful()); assertFalse(m.read(personal).present());
    }
    @Test public void completedJournalFailureRollsBackColorsAndPersonalValues() throws Exception {
        SettingKey personal = new SettingKey("instance","plugin","palette.darcula.keyword");
        Memory m = new Memory() {
            boolean failCommit=true;
            @Override public void save(SettingsSnapshot snapshot) throws IOException {
                if (failCommit && !snapshot.pending() && !snapshot.lastApplied().isEmpty()) { failCommit=false; throw new IOException("commit failure"); }
                super.save(snapshot);
            }
        };
        SnapshotService s = m.service();
        assertThrows(IOException.class,() -> s.apply(Map.of(A,PROFILE),Set.of(),Map.of(personal,USER)));
        assertFalse(m.read(A).present()); assertFalse(m.read(personal).present());
        assertFalse(m.saved.pending()); assertTrue(m.saved.original().isEmpty());
    }
    @Test public void personalPendingEntriesRoundTripWithoutBecomingOriginals() {
        SettingKey personal = new SettingKey("instance","plugin","palette.darcula.keyword");
        var snapshot = new SettingsSnapshot(Map.of(A,ORIGINAL),Map.of(A,PROFILE),Map.of(personal,OriginalValue.absent()),Map.of(personal,USER),Set.of());
        assertEquals(snapshot,SnapshotCodec.decode(SnapshotCodec.encode(snapshot)));
    }
    @Test public void nativeSideEffectsCannotOverwriteSurrenderedManualTarget() throws Exception {
        Memory m = new Memory() {
            @Override public void write(SettingKey key,OriginalValue value) throws IOException {
                super.write(key,value); if (key.equals(A)) values.put(B,PROFILE);
            }
        };
        SnapshotService s = m.service(); s.apply(Map.of(A,PROFILE,B,PROFILE)); m.values.put(B,USER);
        s.apply(Map.of(A,ORIGINAL,B,ORIGINAL),Set.of(A),Map.of());
        assertEquals(USER,m.read(B));
    }
    @Test public void recoveryKeepsPersonalConflictOutsideOriginalJournal() throws Exception {
        SettingKey personal = new SettingKey("instance","plugin","palette.monokai.keyword");
        Memory m = new Memory();
        m.saved = new SettingsSnapshot(Map.of(A,ORIGINAL),Map.of(),Map.of(personal,ORIGINAL),Map.of(personal,PROFILE),Set.of());
        m.values.put(personal,USER);
        var s = m.service(); assertTrue(s.recover().successful());
        assertEquals(USER,m.read(personal)); assertFalse(s.snapshot().surrendered().contains(personal));
        assertFalse(s.snapshot().pending());
    }
    @Test public void selectiveEditReclaimsOnlyRequestedTargets() throws Exception {
        Memory m = new Memory(); SnapshotService s = m.service();
        s.apply(Map.of(A,PROFILE,B,PROFILE)); m.values.put(A,USER); m.values.put(B,USER);
        s.apply(Map.of(A,ORIGINAL,B,ORIGINAL),Set.of(A),Map.of());
        assertEquals(ORIGINAL,m.read(A)); assertEquals(USER,m.read(B));
        s.apply(Map.of(A,PROFILE,B,PROFILE));
        assertEquals(USER,m.read(B)); s.restore(true); assertFalse(m.read(A).present());
    }
    @Test public void personalValuesSurviveRestoreButRollbackWithFailedApply() throws Exception {
        SettingKey personal = new SettingKey("instance","plugin","palette.darcula.keyword");
        Memory m = new Memory(); SnapshotService s = m.service();
        s.apply(Map.of(A,PROFILE),Set.of(),Map.of(personal,USER));
        assertFalse(s.snapshot().original().containsKey(personal));
        s.restore(true); assertEquals(USER,m.read(personal)); assertFalse(m.read(A).present());
        m.failKey = personal;
        assertThrows(IOException.class,() -> s.apply(Map.of(A,PROFILE),Set.of(),Map.of(personal,ORIGINAL)));
        assertEquals(USER,m.read(personal)); assertFalse(m.read(A).present());
    }
    @Test public void failedFlushRecoveryAlsoRollsBackPersonalValues() throws Exception {
        SettingKey personal = new SettingKey("instance","plugin","palette.monokai.keyword");
        Memory m = new Memory(); SnapshotService s = m.service(); m.failFlush = true;
        assertThrows(IOException.class,() -> s.apply(Map.of(A,PROFILE),Set.of(),Map.of(personal,USER)));
        assertTrue(m.saved.pending()); m.failFlush = false;
        assertTrue(m.service().recover().successful()); assertFalse(m.read(personal).present());
        assertFalse(m.read(A).present());
    }
    @Test public void restoreAndPersonalSaveAreAtomic() throws Exception {
        SettingKey personal = new SettingKey("instance","plugin","palette.darcula.keyword");
        Memory m = new Memory(); SnapshotService s = m.service(); s.apply(Map.of(A,PROFILE));
        m.failKey = personal;
        assertFalse(s.restore(false,Map.of(),Map.of(personal,USER)).successful());
        assertEquals(PROFILE,m.read(A)); assertFalse(m.read(personal).present());
        assertTrue(s.restore(false,Map.of(),Map.of(personal,USER)).successful());
        assertFalse(m.read(A).present()); assertEquals(USER,m.read(personal));
    }
    static final SettingKey A = new SettingKey("instance", "test", "a");
    static final SettingKey B = new SettingKey("instance", "test", "b");
    static final OriginalValue ORIGINAL = OriginalValue.of("10,10,10");
    static final OriginalValue PROFILE = OriginalValue.of("43,43,43");
    static final OriginalValue USER = OriginalValue.of("60,60,60");

    static class Memory implements SettingAccess, SnapshotStore {
        final Map<SettingKey,OriginalValue> values = new LinkedHashMap<>();
        SettingsSnapshot saved = SettingsSnapshot.empty();
        SettingKey failKey;
        boolean failFlush, failSave;
        public OriginalValue read(SettingKey key) { return values.getOrDefault(key, OriginalValue.absent()); }
        public void write(SettingKey key, OriginalValue value) throws IOException {
            if (key.equals(failKey)) { failKey = null; throw new IOException("write failure"); }
            values.put(key,value);
        }
        public void flush() throws IOException { if (failFlush) throw new IOException("flush failure"); }
        public SettingsSnapshot load() { return saved; }
        public void save(SettingsSnapshot snapshot) throws IOException {
            if (failSave) throw new IOException("journal failure");
            saved = snapshot;
        }
        SnapshotService service() throws Exception { return new SnapshotService(this,this); }
    }
    @Test public void unsetValueRestoresToUnset() throws Exception {
        Memory m = new Memory(); SnapshotService s = m.service();
        s.apply(Map.of(A,PROFILE)); s.restore(false);
        assertFalse(m.read(A).present());
        assertTrue(s.snapshot().original().isEmpty());
    }
    @Test public void explicitFontChoiceOverridesManualEditWithoutLosingOriginal() throws Exception {
        Memory m = new Memory(); SnapshotService s = m.service();
        s.apply(Map.of(A,PROFILE)); m.values.put(A,USER);
        s.applyExplicit(Map.of(A,OriginalValue.of("new font")));
        assertEquals(OriginalValue.of("new font"),m.read(A));
        s.restore(true); assertFalse(m.read(A).present());
    }
    @Test public void explicitFontChoiceReclaimsPreviouslySurrenderedSetting() throws Exception {
        Memory m = new Memory(); m.values.put(A,ORIGINAL); SnapshotService s = m.service();
        s.apply(Map.of(A,PROFILE)); m.values.put(A,USER); s.restore(false);
        s.applyExplicit(Map.of(A,PROFILE));
        assertEquals(PROFILE,m.read(A)); assertFalse(s.snapshot().surrendered().contains(A));
        s.restore(true); assertEquals(ORIGINAL,m.read(A));
    }
    @Test public void switchingProfilesKeepsFirstSnapshot() throws Exception {
        Memory m = new Memory(); m.values.put(A,ORIGINAL); SnapshotService s = m.service();
        s.apply(Map.of(A,PROFILE)); s.apply(Map.of(A,USER)); s.restore(true);
        assertEquals(ORIGINAL,m.read(A));
    }
    @Test public void userEditSurvivesAutomaticRestore() throws Exception {
        Memory m = new Memory(); m.values.put(A,ORIGINAL); SnapshotService s = m.service();
        s.apply(Map.of(A,PROFILE)); m.values.put(A,USER);
        assertEquals(Set.of(A),s.restore(false).conflicts());
        assertEquals(USER,m.read(A));
    }
    @Test public void forceRestoreUsesOriginalSnapshot() throws Exception {
        Memory m = new Memory(); m.values.put(A,ORIGINAL); SnapshotService s = m.service();
        s.apply(Map.of(A,PROFILE)); m.values.put(A,USER); s.restore(false); s.restore(true);
        assertEquals(ORIGINAL,m.read(A));
    }
    @Test public void writeFailureRollsBackPreviousWrites() throws Exception {
        Memory m = new Memory(); m.values.put(A,ORIGINAL); m.failKey = B; SnapshotService s = m.service();
        var desired = new LinkedHashMap<SettingKey,OriginalValue>(); desired.put(A,PROFILE); desired.put(B,USER);
        assertThrows(IOException.class, () -> s.apply(desired));
        assertEquals(ORIGINAL,m.read(A)); assertFalse(m.read(B).present());
    }
    @Test public void flushFailureKeepsRecoverableJournal() throws Exception {
        Memory m = new Memory(); m.values.put(A,ORIGINAL); m.failFlush = true; SnapshotService s = m.service();
        assertThrows(IOException.class, () -> s.apply(Map.of(A,PROFILE)));
        assertTrue(m.saved.pending());
        m.failFlush = false; m.service().recover();
        assertEquals(ORIGINAL,m.read(A)); assertFalse(m.saved.pending());
    }
    @Test public void journalFailureDoesNotMutateSettings() throws Exception {
        Memory m = new Memory(); m.values.put(A,ORIGINAL); SnapshotService s = m.service(); m.failSave = true;
        assertThrows(IOException.class, () -> s.apply(Map.of(A,PROFILE)));
        assertEquals(ORIGINAL,m.read(A));
    }
    @Test public void restartUsesSavedSnapshot() throws Exception {
        Memory m = new Memory(); m.values.put(A,ORIGINAL); m.service().apply(Map.of(A,PROFILE));
        m.service().apply(Map.of(A,USER)); m.service().restore(true);
        assertEquals(ORIGINAL,m.read(A));
    }
    @Test public void userEditSurvivesProfileSwitch() throws Exception {
        Memory m = new Memory(); m.values.put(A,ORIGINAL); SnapshotService s = m.service();
        s.apply(Map.of(A,PROFILE)); m.values.put(A,USER); s.apply(Map.of(A,OriginalValue.of("90,90,90")));
        assertEquals(USER,m.read(A)); s.restore(true); assertEquals(ORIGINAL,m.read(A));
    }
    @Test public void disableBslRestoresOnlyManagedBslKeys() throws Exception {
        Memory m = new Memory(); m.values.put(A,ORIGINAL); SnapshotService s = m.service();
        s.apply(Map.of(A,PROFILE,B,PROFILE)); s.apply(Map.of(A,USER));
        assertFalse(m.read(B).present()); assertEquals(USER,m.read(A));
        s.restore(true); assertEquals(ORIGINAL,m.read(A));
    }
    @Test public void crashRecoveryPreservesUserConflict() throws Exception {
        Memory m = new Memory();
        m.saved = new SettingsSnapshot(Map.of(A,ORIGINAL),Map.of(),Map.of(A,ORIGINAL),Map.of(A,PROFILE),Set.of());
        m.values.put(A,USER);
        assertEquals(Set.of(A), m.service().recover().conflicts());
        assertEquals(USER,m.read(A));
    }
    @Test public void captureBeforeNativeThemePreservesOriginal() throws Exception {
        Memory m = new Memory(); SnapshotService s = m.service();
        s.capture(Set.of(A)); m.values.put(A,PROFILE);
        s.apply(Map.of(A,USER)); s.restore(true);
        assertFalse(m.read(A).present());
    }
    @Test public void nativeThemeSideEffectIsCompensatedOnFailure() throws Exception {
        SettingKey c = new SettingKey("instance","test","c");
        Memory m = new Memory() {
            @Override public void write(SettingKey key, OriginalValue value) throws IOException {
                super.write(key,value);
                if (key.equals(A)) values.put(B,USER); // Native theme setter also changes editor preferences.
            }
        };
        m.values.put(A,ORIGINAL); m.values.put(B,ORIGINAL); m.failKey = c;
        var changes = new LinkedHashMap<SettingKey,OriginalValue>(); changes.put(A,PROFILE); changes.put(B,PROFILE); changes.put(c,PROFILE);
        assertThrows(IOException.class, () -> m.service().apply(changes));
        assertEquals(ORIGINAL,m.read(A)); assertEquals(ORIGINAL,m.read(B));
    }
    @Test public void excludedBslIsPreservedOnFirstThemeSwitch() throws Exception {
        Memory m = new Memory() {
            @Override public void write(SettingKey key, OriginalValue value) throws IOException {
                super.write(key,value); if (key.equals(A)) values.put(B,PROFILE);
            }
        };
        m.values.put(B,USER); SnapshotService s = m.service();
        s.apply(s.withOriginals(Map.of(A,PROFILE),Set.of(B)));
        assertEquals(USER,m.read(B)); s.restore(true); assertEquals(USER,m.read(B));
    }
    @Test public void allConflictsStillReportsJournalFailure() throws Exception {
        Memory m = new Memory(); m.values.put(A,ORIGINAL); SnapshotService s = m.service();
        s.apply(Map.of(A,PROFILE)); m.values.put(A,USER); m.failSave = true;
        assertFalse(s.restore(false).successful());
        assertEquals(USER,m.read(A));
    }
    @Test public void nativeThemeOverwriteDoesNotEraseObservedUserColor() throws Exception {
        Memory m = new Memory(); m.values.put(A,ORIGINAL); SnapshotService s = m.service();
        s.apply(Map.of(A,PROFILE)); m.values.put(A,USER);
        Map<SettingKey,OriginalValue> observed = s.observeValues(k -> true);
        m.values.put(A,ORIGINAL); // External native theme writes its own default before the deferred callback.
        RestoreResult result = s.restore(false,observed);
        assertEquals(Set.of(A),result.conflicts()); assertEquals(USER,m.read(A));
        s.restore(true); assertEquals(ORIGINAL,m.read(A));
    }
    @Test public void nativeDefaultsPreserveExplicitUnmanagedColors() throws Exception {
        Memory m = new Memory(); m.values.put(B,USER); SnapshotService s = m.service();
        Map<SettingKey,OriginalValue> plan = s.withNativeDefaults(Map.of(A,PROFILE),Map.of(B,ORIGINAL));
        s.apply(plan); assertEquals(USER,m.read(B)); s.restore(true); assertEquals(USER,m.read(B));
        Memory unset = new Memory(); SnapshotService fresh = unset.service();
        fresh.apply(fresh.withNativeDefaults(Map.of(A,PROFILE),Map.of(B,ORIGINAL))); assertEquals(ORIGINAL,unset.read(B));
        fresh.restore(true); assertFalse(unset.read(B).present());
    }
}
