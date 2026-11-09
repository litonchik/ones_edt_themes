package ru.litonchik.edt.themes.runtime;

import java.net.URI;
import org.eclipse.ui.*;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.osgi.framework.FrameworkUtil;

/** Called only by the user's Update button, never by the background checker. */
public final class UpdateInstaller {
    public static final String COMMAND="org.eclipse.equinox.p2.ui.sdk.update";
    public static final URI SITE=URI.create("https://litonchik.github.io/ones_edt_themes/repository/");
    private UpdateInstaller(){}
    public static void open(IWorkbenchWindow window) throws Exception {
        var command=window.getService(ICommandService.class).getCommand(COMMAND);
        if(!command.isDefined() || !command.isEnabled())throw new IllegalStateException("Мастер обновлений EDT сейчас недоступен");
        var context=FrameworkUtil.getBundle(UpdateInstaller.class).getBundleContext();
        var ref=context.getServiceReference(IProvisioningAgent.class);if(ref==null)throw new IllegalStateException("Служба установки EDT недоступна");
        var agent=context.getService(ref);if(agent==null)throw new IllegalStateException("Служба установки EDT недоступна");
        try{
            var metadata=(IMetadataRepositoryManager)agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
            var artifacts=(IArtifactRepositoryManager)agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
            if(metadata==null || artifacts==null)throw new IllegalStateException("Сайт обновлений EDT недоступен");
            metadata.addRepository(SITE);metadata.setEnabled(SITE,true);artifacts.addRepository(SITE);artifacts.setEnabled(SITE,true);
            window.getService(IHandlerService.class).executeCommand(COMMAND,null);
        }finally{context.ungetService(ref);}
    }
}
