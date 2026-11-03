package ru.litonchik.edt.themes.edt;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.xtext.ui.editor.syntaxcoloring.HighlightingHelper;

/** Xtext's default helper refreshes preference changes only when it has a full editor. */
public final class QueryHighlightingHelper extends HighlightingHelper {
    @Override public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (getReconciler() != null && event.getProperty().contains(".syntaxColorer.tokenStyles."))
            getReconciler().refresh();
    }
}
