package ru.litonchik.edt.themes.edt;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.text.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ITextAttributeProvider;
import org.eclipse.xtext.util.CancelIndicator;

/** Repaints unchanged semantic tokens through public Xtext services, without a document edit. */
final class XtextPresentationRefresh {
    private static final String KEY = XtextPresentationRefresh.class.getName();
    private record Span(int offset, int length, String[] ids) {}

    static void refresh(ITextViewer viewer) {
        if (!(viewer.getDocument() instanceof IXtextDocument document)
                || !(viewer instanceof ITextViewerExtension4 extension)) return;
        var widget = viewer.getTextWidget();
        if (widget == null || widget.isDisposed()) return;
        if (widget.getData(KEY) instanceof Runnable previous) previous.run();
        String language = document.readOnly(resource -> resource == null ? null : resource.getLanguageName());
        boolean query = "com._1c.g5.v8.dt.ql.Ql".equals(language) || "com._1c.g5.v8.dt.ql.dcs.QlDcs".equals(language);
        if (!query && !"com._1c.g5.v8.dt.bsl.Bsl".equals(language)) { viewer.invalidateTextPresentation(); return; }
        var provider = document.readOnly(resource -> resource == null ? null
                : IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(resource.getURI()));
        if (provider == null) { viewer.invalidateTextPresentation(); return; }
        // The native BSL calculator remembers the last model stamp. A fresh service instance
        // can calculate the unchanged model; the shared attribute provider reads current colors.
        var calculator = query ? null : provider.get(ISemanticHighlightingCalculator.class);
        var legacy = query ? provider.get(org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator.class) : null;
        var attributes = provider.get(ITextAttributeProvider.class);
        if ((calculator == null && legacy == null) || attributes == null) { viewer.invalidateTextPresentation(); return; }
        List<Span> spans = document.readOnly(resource -> {
            var result = new ArrayList<Span>();
            if (resource != null) {
                if (legacy != null) legacy.provideHighlightingFor(resource,
                        (offset,length,ids) -> result.add(new Span(offset,length,ids.clone())));
                else calculator.provideHighlightingFor(resource,
                        (offset,length,ids) -> result.add(new Span(offset,length,ids.clone())),CancelIndicator.NullImpl);
            }
            return result;
        });
        ITextPresentationListener presentation = update -> {
            var extent = update.getExtent();
            if (extent == null) return;
            for (var span : spans) {
                int start = Math.max(span.offset(),extent.getOffset());
                int end = Math.min(span.offset()+span.length(),extent.getOffset()+extent.getLength());
                if (end <= start || span.ids().length == 0) continue;
                var attribute = span.ids().length == 1 ? attributes.getAttribute(span.ids()[0]) : attributes.getMergedAttributes(span.ids());
                if (attribute == null) continue;
                var style = new StyleRange(start,end-start,attribute.getForeground(),attribute.getBackground(),attribute.getStyle() & (SWT.BOLD|SWT.ITALIC));
                style.font = attribute.getFont();
                style.underline = (attribute.getStyle() & TextAttribute.UNDERLINE) != 0;
                style.strikeout = (attribute.getStyle() & TextAttribute.STRIKETHROUGH) != 0;
                update.mergeStyleRange(style);
            }
        };
        var listener = new IDocumentListener() {
            @Override public void documentAboutToBeChanged(DocumentEvent event) {
                if (widget.getData(KEY) instanceof Runnable cleanup) cleanup.run();
            }
            @Override public void documentChanged(DocumentEvent event) {}
        };
        var dispose = new org.eclipse.swt.events.DisposeListener[1];
        Runnable cleanup = () -> {
            extension.removeTextPresentationListener(presentation);
            document.removeDocumentListener(listener);
            if (!widget.isDisposed()) {
                widget.removeDisposeListener(dispose[0]);
                widget.setData(KEY,null);
            }
        };
        widget.setData(KEY,cleanup);
        dispose[0] = event -> cleanup.run();
        widget.addDisposeListener(dispose[0]);
        document.addDocumentListener(listener);
        extension.addTextPresentationListener(presentation);
        // Xtext prepends its native presenter. This temporary listener runs after it and is
        // removed before the next edit, when native semantic reconciliation resumes normally.
        viewer.invalidateTextPresentation();
    }
}
