package ru.litonchik.edt.themes.runtime;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;

public final class ThemeStartup implements IStartup {
    private static ThemeCoordinator coordinator;
    public static ThemeCoordinator coordinator() throws Exception {
        if (coordinator == null) { coordinator = new ThemeCoordinator(); coordinator.start(); }
        return coordinator;
    }
    @Override public void earlyStartup() {
        PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
            try { coordinator(); new CodeFontSettings(); } catch (Exception e) { report(e); }
        });
    }
    public static void stop() { if (coordinator != null) { coordinator.close(); coordinator = null; } }
    public static void report(Throwable e) {
        Platform.getLog(ThemeStartup.class).log(new Status(IStatus.ERROR,SnapshotPreferences.PLUGIN_ID,"Темы EDT: " + e.getMessage(),e));
        if (PlatformUI.isWorkbenchRunning()) MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null ? null : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"Темы EDT",e.getMessage());
    }
    public static void inform(String message) {
        MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null ? null : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),"Темы EDT",message);
    }
}
