package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import java.util.Map;
import org.junit.Test;
import ru.litonchik.edt.themes.model.*;

public class PaletteCatalogTest {
    @Test public void darculaUsesApprovedEditorColors() {
        Palette p = PaletteCatalog.load("darcula");
        assertEquals("#2B2B2B", p.color("editor.background"));
        assertEquals("169,183,198", p.rgb("text"));
    }
    @Test public void monokaiUsesApprovedEditorColors() {
        Palette p = PaletteCatalog.load("monokai");
        assertEquals("#272822", p.color("editor.background"));
        assertEquals("248,248,242", p.rgb("text"));
    }
    @Test public void unknownProfileFails() { assertThrows(IllegalArgumentException.class, () -> PaletteCatalog.load("other")); }
    @Test public void invalidRgbFails() { assertThrows(IllegalArgumentException.class, () -> new Palette("darcula", Map.of("text", "#ZZ0000"))); }
    @Test public void missingRequiredRoleFails() { assertThrows(IllegalArgumentException.class, () -> new Palette("darcula", Map.of("text", "#FFFFFF"))); }
}
