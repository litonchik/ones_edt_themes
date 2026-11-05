package ru.litonchik.edt.themes.edt;

import com.google.inject.Inject;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.XtextSourceViewer;
import org.eclipse.xtext.ui.editor.XtextSourceViewerConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.HighlightingHelper;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ITextAttributeProvider;

/** Refresh lexical and semantic presentation, including embedded and merge viewers. */
public final class ViewerHighlightingHelper extends HighlightingHelper {
    @Inject private ITextAttributeProvider attributes;
    private XtextSourceViewer viewer;
    private boolean refreshPending;

    @Override public void install(XtextEditor editor,XtextSourceViewer sourceViewer) {
        viewer = sourceViewer;
        super.install(editor,sourceViewer);
    }
    @Override public void install(XtextSourceViewerConfiguration configuration,XtextSourceViewer sourceViewer) {
        viewer = sourceViewer;
        super.install(configuration,sourceViewer);
    }
    @Override public void uninstall() {
        viewer = null;
        super.uninstall();
    }
    @Override public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (!event.getProperty().contains(".syntaxColorer.tokenStyles.")) return;
        if (attributes instanceof IPropertyChangeListener listener) listener.propertyChange(event);
        if (getReconciler() != null) getReconciler().refresh();
        var target = viewer;
        if (target == null || target.getTextWidget().isDisposed() || refreshPending) return;
        refreshPending = true;
        // Run after the native CSS refresh and coalesce the palette's token events.
        target.getTextWidget().getDisplay().asyncExec(() -> {
            refreshPending = false;
            if (viewer == target && !target.getTextWidget().isDisposed()) {
                target.invalidateTextPresentation();
                XtextPresentationRefresh.refresh(target);
            }
        });
    }
}
