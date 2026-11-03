package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import ru.litonchik.edt.themes.model.*;

public class SnapshotEncodingTest {
    @Test public void snapshotRoundTripPreservesUnicodeAndSpaces() {
        SettingKey k = new SettingKey("instance", "узел", "ключ с пробелами / ? .");
        SettingsSnapshot snapshot = new SettingsSnapshot(Map.of(k,OriginalValue.absent()),Map.of(k,OriginalValue.of("значение \n= ")),Map.of(),Map.of(),Set.of(k));
        assertEquals(snapshot, SnapshotCodec.decode(SnapshotCodec.encode(snapshot)));
    }
    @Test public void unknownVersionFails() { assertThrows(IllegalArgumentException.class, () -> SnapshotCodec.decode(Map.of("version","99"))); }
    @Test public void corruptSnapshotFails() { assertThrows(IllegalArgumentException.class, () -> SnapshotCodec.decode(Map.of("version","1","count","1"))); }
}
