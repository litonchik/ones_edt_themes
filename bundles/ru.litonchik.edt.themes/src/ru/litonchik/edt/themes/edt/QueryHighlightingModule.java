package ru.litonchik.edt.themes.edt;

import org.eclipse.xtext.service.AbstractGenericModule;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingHelper;

/** Public EDT language module extension; includes embedded constructor text viewers. */
public final class QueryHighlightingModule extends AbstractGenericModule {
    public Class<? extends IHighlightingHelper> bindIHighlightingHelper() {
        return QueryHighlightingHelper.class;
    }
}
