package ru.litonchik.edt.themes.edt;

import org.eclipse.xtext.service.AbstractGenericModule;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingHelper;

/** Public EDT language module extension; includes embedded constructor text viewers. */
public final class QueryHighlightingModule extends AbstractGenericModule {
    public Class<? extends org.eclipse.xtext.ui.editor.syntaxcoloring.ITextAttributeProvider> bindITextAttributeProvider() { return QueryTextAttributeProvider.class; }
    public Class<? extends org.eclipse.xtext.ui.editor.syntaxcoloring.TextAttributeProvider> bindTextAttributeProvider() { return QueryTextAttributeProvider.class; }
    // EmbeddedEditorFactory injects the concrete helper, full editors its interface.
    public Class<? extends org.eclipse.xtext.ui.editor.syntaxcoloring.HighlightingHelper> bindHighlightingHelper() { return ViewerHighlightingHelper.class; }
    public Class<? extends IHighlightingHelper> bindIHighlightingHelper() {
        return ViewerHighlightingHelper.class;
    }
}
