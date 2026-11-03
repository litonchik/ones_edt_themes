package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import org.junit.Test;
import ru.litonchik.edt.themes.model.RuntimePlatform;

public class RuntimePlatformTest {
    @Test public void java25IsAllowedOnlyWhenRequiredByRelease() {
        RuntimePlatform.verify("win32","x86_64",25,25);
        assertThrows(IllegalStateException.class,() -> RuntimePlatform.verify("win32","x86_64",17,25));
        assertThrows(IllegalStateException.class,() -> RuntimePlatform.verify("win32","x86_64",25,17));
        assertThrows(IllegalStateException.class,() -> RuntimePlatform.verify("win32","x86_64",21,21));
    }
    @Test public void windowsX64Java17RemainsAllowed() {
        RuntimePlatform.verify("win32","x86_64",17);
    }
    @Test public void macX64Java17IsAllowedIncludingRosettaRuntime() {
        RuntimePlatform.verify("macosx","x86_64",17);
    }
    @Test public void unsupportedOperatingSystemsRemainRejected() {
        for (String os : new String[]{"linux","darwin","unknown"}) {
            var error = assertThrows(IllegalStateException.class,() -> RuntimePlatform.verify(os,"x86_64",17));
            assertTrue(error.getMessage().contains(os));
        }
    }
    @Test public void unverifiedArchitecturesRemainRejectedOnBothSystems() {
        for (String os : new String[]{"win32","macosx"}) for (String arch : new String[]{"aarch64","x86","unknown"}) {
            var error = assertThrows(IllegalStateException.class,() -> RuntimePlatform.verify(os,arch,17));
            assertTrue(error.getMessage().contains(arch));
        }
    }
    @Test public void java17IsStillRequiredOnBothSystems() {
        for (String os : new String[]{"win32","macosx"}) for (int version : new int[]{11,21,25}) {
            var error = assertThrows(IllegalStateException.class,() -> RuntimePlatform.verify(os,"x86_64",version));
            assertTrue(error.getMessage().contains("Java 17"));
        }
    }
}
