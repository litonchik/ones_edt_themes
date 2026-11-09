package ru.litonchik.edt.themes.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import ru.litonchik.edt.themes.runtime.UpdateNotifications;

/** Native toolbar command. Its visibility and tooltip follow the public workbench model. */
public final class ShowUpdateDetailsHandler extends AbstractHandler {
    public static final String COMMAND="ru.litonchik.edt.themes.showUpdate",PROPERTY="ru.litonchik.edt.themes.updateAvailable",TOOLBAR="ru.litonchik.edt.themes.updateToolbar";
    @Override public Object execute(ExecutionEvent event){
        var window=HandlerUtil.getActiveWorkbenchWindow(event);if(window!=null && UpdateNotifications.available())UpdateDetails.open(window);return null;
    }
    public static void refreshStatus(){
        if(!PlatformUI.isWorkbenchRunning())return;var wb=PlatformUI.getWorkbench();
        var model=wb.getService(MApplication.class);var models=wb.getService(EModelService.class);
        if(model!=null && models!=null){
            var release=UpdateNotifications.state().release();String tooltip=release==null?"Темы EDT: обновления":"Темы EDT: доступна версия "+release.version();
            for(var item:models.findElements(model,"ru.litonchik.edt.themes.updateStatus",MHandledToolItem.class,null))item.setTooltip(tooltip);
            for(var bar:models.findElements(model,TOOLBAR,MToolBar.class,null))bar.setVisible(UpdateNotifications.available());
        }
        var evaluation=wb.getService(IEvaluationService.class);if(evaluation!=null)evaluation.requestEvaluation(PROPERTY);
    }
}
