package ru.litonchik.edt.themes.runtime;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public final class ThemePlugin extends AbstractUIPlugin {
    @Override public void stop(BundleContext context) throws Exception {
        try { ThemeStartup.stop(); } finally { super.stop(context); }
    }
}
