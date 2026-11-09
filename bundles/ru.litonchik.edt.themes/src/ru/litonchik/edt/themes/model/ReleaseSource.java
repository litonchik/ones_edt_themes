package ru.litonchik.edt.themes.model;

import java.io.IOException;
import java.util.function.BooleanSupplier;

/** Explicit feed dependency; production uses only the project's fixed HTTPS endpoint. */
@FunctionalInterface public interface ReleaseSource {
    ReleaseInfo read(BooleanSupplier cancelled) throws IOException;
}
