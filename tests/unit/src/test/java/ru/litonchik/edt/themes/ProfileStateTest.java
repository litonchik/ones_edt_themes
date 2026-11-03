package ru.litonchik.edt.themes;

import static org.junit.Assert.*;
import org.junit.Test;
import ru.litonchik.edt.themes.model.*;

public class ProfileStateTest {
    @Test public void unavailableSavedProfileDoesNotBecomeAnActiveFallback() {
        ProfileState state = new ProfileState("missing-theme",false);
        assertNull(state.activeProfile());
        assertFalse(state.includeBsl());
        assertFalse(state.onThemeChanged(ProfileState.DARK));
    }
    @Test public void thirdProfileSurvivesRestartAndReleasesOnExternalLight() {
        ProfileState state = new ProfileState("one-dark-pro",true);
        assertEquals("one-dark-pro",state.activeProfile());
        assertFalse(state.onThemeChanged(ProfileState.DARK));
        assertTrue(state.onThemeChanged("org.eclipse.e4.ui.css.theme.e4_default"));
        assertNull(state.activeProfile());
    }
    @Test public void externalLightThemeDoesNotSwitchBackToDark() {
        ProfileState s = new ProfileState("darcula",true);
        assertTrue(s.onThemeChanged("org.eclipse.e4.ui.css.theme.e4_default"));
        assertNull(s.activeProfile()); assertFalse(s.onThemeChanged(ProfileState.DARK));
    }
    @Test public void ownApplyDoesNotReenterCoordinator() {
        ProfileState s = new ProfileState(null,true);
        assertTrue(s.beginApply()); assertFalse(s.beginApply());
        assertFalse(s.onThemeChanged("light")); s.endApply(); assertTrue(s.beginApply());
    }
    @Test public void restartUsesSavedProfileWithoutReplacingSnapshot() {
        ProfileState s = new ProfileState("monokai",false);
        assertEquals("monokai",s.activeProfile()); assertFalse(s.includeBsl());
        assertFalse(s.onThemeChanged(ProfileState.DARK)); assertEquals("monokai",s.activeProfile());
    }
}
