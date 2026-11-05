package ru.litonchik.edt.themes.edt;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
import org.eclipse.xtext.ide.editor.syntaxcoloring.HighlightingStyles;
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.PreferenceStoreAccessor;
import org.eclipse.xtext.ui.editor.syntaxcoloring.TextAttributeProvider;

/** Use the native QL ordinary-text style for Xtext's otherwise unclassified tokens. */
@Singleton
public final class QueryTextAttributeProvider extends TextAttributeProvider {
    // QlHighlightingConfiguration.DEFAULT_ID in the supported EDT bundles.
    private static final String QL_ORDINARY_TEXT = "Ql_Others";

    @Inject
    public QueryTextAttributeProvider(IHighlightingConfiguration configuration,
            IPreferenceStoreAccess preferences, PreferenceStoreAccessor accessor) {
        super(configuration, preferences, accessor);
    }

    @Override
    protected TextAttribute createTextAttribute(String id,org.eclipse.xtext.ui.editor.utils.TextStyle style) {
        TextAttribute attribute = super.createTextAttribute(id,style);
        if (attribute.getForeground() == null) {
            // TextAttributeProvider builds its cache on the UI thread. Keep SWT work here:
            // semantic reconciliation also reads getAttribute() from background jobs.
            var preferences = EditorsUI.getPreferenceStore();
            var foreground = preferences.getBoolean(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
                    ? Display.getCurrent().getSystemColor(SWT.COLOR_LIST_FOREGROUND)
                    : EditorUtils.colorFromRGB(PreferenceConverter.getColor(preferences,AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND));
            return new TextAttribute(foreground,attribute.getBackground(),attribute.getStyle(),attribute.getFont());
        }
        return attribute;
    }

    @Override
    public TextAttribute getAttribute(String id) {
        return super.getAttribute(HighlightingStyles.DEFAULT_ID.equals(id) ? QL_ORDINARY_TEXT : id);
    }
}
