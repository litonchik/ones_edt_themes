package ru.litonchik.edt.themes.model;

public record OriginalValue(boolean present, String value) {
    public OriginalValue {
        if (present != (value != null)) throw new IllegalArgumentException("Значение не соответствует признаку present");
    }
    public static OriginalValue absent() { return new OriginalValue(false, null); }
    public static OriginalValue of(String value) { return new OriginalValue(true, value); }
}
