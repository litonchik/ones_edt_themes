package ru.litonchik.edt.themes.model;

import java.util.*;
import java.util.regex.Pattern;

/** A fixed illustrative sample; does not parse user code or change editor preferences. */
public final class CodePreview {
    private CodePreview() {}
    public static final String SOURCE = """
        // Комментарий / Comment
        #Область Пример
        Функция ПроверитьШрифт()
            Число = 123.45;
            Возврат "Пример / Example 123";
        КонецФункции
        #КонецОбласти

        Function Example()
            Return "Sample";
        EndFunction""";
    public record Span(int offset, int length, String role, boolean bold) {}
    private static final List<Span> SPANS = createSpans();
    public static List<Span> spans() { return SPANS; }
    private static List<Span> createSpans() {
        Pattern tokens = Pattern.compile("//[^\\r\\n]*|\"(?:\"\"|[^\"\\r\\n])*\"|#[\\p{L}\\p{N}_]+|\\b(?:Функция|Возврат|КонецФункции|Function|Return|EndFunction)\\b|\\b\\d+(?:\\.\\d+)?\\b",Pattern.UNICODE_CHARACTER_CLASS);
        var spans = new ArrayList<Span>(); var matcher = tokens.matcher(SOURCE);
        while (matcher.find()) {
            String token = matcher.group();
            String role = token.startsWith("//") ? "comment" : token.startsWith("\"") ? "string" : token.startsWith("#") ? "directive" : Character.isDigit(token.charAt(0)) ? "number" : "keyword";
            spans.add(new Span(matcher.start(),matcher.end()-matcher.start(),role,role.equals("keyword") || role.equals("directive")));
        }
        return List.copyOf(spans);
    }
}
