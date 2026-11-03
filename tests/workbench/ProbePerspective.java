package ru.litonchik.edt.themes.testing;

import org.eclipse.ui.*;

public class ProbePerspective implements IPerspectiveFactory {
    public static class Gallery implements IPerspectiveFactory {
        @Override public void createInitialLayout(IPageLayout layout) {
            layout.addView("org.eclipse.ui.navigator.ProjectExplorer",IPageLayout.LEFT,0.22f,layout.getEditorArea());
            layout.addView("org.eclipse.ui.views.PropertySheet",IPageLayout.RIGHT,0.65f,layout.getEditorArea());
        }
    }
    @Override public void createInitialLayout(IPageLayout layout) {
        layout.addView("org.eclipse.ui.navigator.ProjectExplorer",IPageLayout.LEFT,0.28f,layout.getEditorArea());
        layout.addView("org.eclipse.ui.views.PropertySheet",IPageLayout.BOTTOM,0.7f,layout.getEditorArea());
    }
}
