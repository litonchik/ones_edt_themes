package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import ru.litonchik.edt.themes.model.CodePreview;
import ru.litonchik.edt.themes.model.PaletteCatalog;

public class CodePreviewTest {
    @Test public void previewIncludesBothLanguagesAndAllIllustratedTokenKinds() {
        assertTrue(CodePreview.SOURCE.contains("Функция")); assertTrue(CodePreview.SOURCE.contains("Function"));
        Set<String> roles = new HashSet<>(); CodePreview.spans().forEach(s -> roles.add(s.role()));
        assertEquals(Set.of("comment","keyword","string","number","directive"),roles);
    }
    @Test public void spansAreWithinUtf16SampleAndDoNotOverlap() {
        int previousEnd = 0;
        for (var span : CodePreview.spans()) {
            assertTrue(span.offset() >= previousEnd); assertTrue(span.length() > 0);
            previousEnd = span.offset()+span.length(); assertTrue(previousEnd <= CodePreview.SOURCE.length());
            for(String id : PaletteCatalog.ids()) assertNotNull(PaletteCatalog.load(id).color(span.role()));
        }
    }
    @Test public void quotedDigitsUseStringColorAndNumericLiteralUsesNumberColor() {
        int quoted = CodePreview.SOURCE.indexOf("Example 123");
        assertEquals("string",CodePreview.spans().stream().filter(s -> s.offset() <= quoted && quoted < s.offset()+s.length()).findFirst().orElseThrow().role());
        int number = CodePreview.SOURCE.indexOf("123.45");
        assertEquals("number",CodePreview.spans().stream().filter(s -> s.offset() == number).findFirst().orElseThrow().role());
    }
}
