package ru.litonchik.edt.themes.ui;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import ru.litonchik.edt.themes.runtime.*;

public final class UpdatesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    private Button enabled,check,details;private Label status;private final Runnable changed=this::refresh;
    public UpdatesPreferencePage(){setTitle("Обновления Темы EDT");}
    @Override public void init(IWorkbench workbench){}
    @Override protected Control createContents(Composite parent){
        var body=new Composite(parent,SWT.NONE);body.setLayout(new GridLayout(1,false));
        var version=new Label(body,SWT.NONE);version.setText("Установлена версия "+UpdateNotifications.installed());
        enabled=new Button(body,SWT.CHECK);enabled.setText("Проверять обновления в фоне и показывать индикатор");enabled.setSelection(UpdateNotifications.isEnabled());enabled.setData("themes.update.enabled",Boolean.TRUE);
        var help=new Label(body,SWT.WRAP);var data=new GridData(SWT.FILL,SWT.TOP,true,false);data.widthHint=450;help.setLayoutData(data);
        help.setText("Проверка выполняется не чаще раза в сутки. При новой версии появляется индикатор в строке состояния. Всплывающих окон и автоматической установки нет.");
        status=new Label(body,SWT.WRAP);status.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,false));status.setData("themes.update.status",Boolean.TRUE);
        check=new Button(body,SWT.PUSH);check.setText("Проверить сейчас");check.setData("themes.update.check",Boolean.TRUE);check.addListener(SWT.Selection,event -> {UpdateNotifications.checkNow();refresh();});
        details=new Button(body,SWT.PUSH);details.setText("Сведения об обновлении");details.addListener(SWT.Selection,event -> UpdateDetails.open(PlatformUI.getWorkbench().getActiveWorkbenchWindow()));
        try{ThemeStartup.coordinator();}catch(Exception e){setErrorMessage("Проверка обновлений недоступна для этой сборки EDT.");setValid(false);enabled.setEnabled(false);check.setEnabled(false);}
        UpdateNotifications.addListener(changed);refresh();return body;
    }
    private void refresh(){
        if(status==null || status.isDisposed())return;
        var state=UpdateNotifications.state();
        String text=switch(state.phase()){
            case IDLE -> "Проверка ещё не выполнялась.";
            case CHECKING -> "Проверяем наличие новой версии…";
            case CURRENT -> "Установленная версия актуальна.";
            case AVAILABLE -> "Доступна версия "+state.release().version()+".";
            case FAILED -> "Не удалось проверить обновления. Проверьте соединение и настройки прокси EDT.";
            case DISABLED -> "Фоновая проверка и индикатор выключены. Можно проверить вручную.";
        };
        status.setText(text);details.setEnabled(state.release()!=null);check.setEnabled(isValid() && UpdateNotifications.isRunning() && state.phase()!=UpdateNotifications.Phase.CHECKING);status.getParent().layout(true,true);
    }
    private boolean apply(){try{UpdateNotifications.setEnabled(enabled.getSelection());setErrorMessage(null);return true;}catch(Exception e){setErrorMessage("Не удалось сохранить настройку проверки обновлений.");return false;}}
    @Override public boolean performOk(){return apply();}
    @Override protected void performApply(){apply();}
    @Override protected void performDefaults(){enabled.setSelection(true);super.performDefaults();}
    @Override public void dispose(){UpdateNotifications.removeListener(changed);super.dispose();}
}
