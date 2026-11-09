package ru.litonchik.edt.themes.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import ru.litonchik.edt.themes.runtime.*;

/** Modeless details opened exclusively by a selection event on the indicator. */
public final class UpdateDetails {
    public static final String ID="themes.update.details";
    private UpdateDetails(){}
    public static void open(IWorkbenchWindow window){
        var old=window.getShell().getData(ID);if(old instanceof Shell existing && !existing.isDisposed()){existing.setActive();return;}
        var shell=new Shell(window.getShell(),SWT.SHELL_TRIM|SWT.MODELESS);shell.setData(ID,Boolean.TRUE);window.getShell().setData(ID,shell);
        shell.setText("Обновление Темы EDT");shell.setLayout(new GridLayout(1,false));
        var title=new Label(shell,SWT.WRAP);title.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
        var description=new Label(shell,SWT.WRAP);var data=new GridData(SWT.FILL,SWT.CENTER,true,false);data.widthHint=450;description.setLayoutData(data);
        description.setText("Кнопка «Обновить» откроет штатный мастер EDT и включит сайт «Темы EDT» в список обновлений. Установка начнётся после вашего подтверждения в мастере.");
        var error=new Label(shell,SWT.WRAP);error.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
        var buttons=new Composite(shell,SWT.NONE);buttons.setLayout(new RowLayout());
        var update=new Button(buttons,SWT.PUSH);update.setText("Обновить");update.setData("themes.update.install",Boolean.TRUE);
        var notes=new Button(buttons,SWT.PUSH);notes.setText("Что нового");
        var close=new Button(buttons,SWT.PUSH);close.setText("Закрыть");close.addListener(SWT.Selection,event -> shell.close());
        Runnable refresh=() -> {
            if(shell.isDisposed())return;var release=UpdateNotifications.state().release();
            title.setText(release==null?"Новая версия не обнаружена":"Доступна версия "+release.version()+". Установлена: "+UpdateNotifications.installed());
            update.setEnabled(release!=null);notes.setEnabled(release!=null);shell.layout(true,true);
        };
        notes.addListener(SWT.Selection,event -> {var release=UpdateNotifications.state().release();if(release!=null)try{PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(release.releaseUri().toURL());}catch(Exception e){error.setText("Не удалось открыть страницу релиза.");shell.layout(true,true);}});
        update.addListener(SWT.Selection,event -> {
            update.setEnabled(false);try{UpdateInstaller.open(window);}catch(Exception e){if(!shell.isDisposed()){error.setText("Не удалось открыть мастер обновлений EDT. Повторите через «Справка → Проверить обновления».");shell.layout(true,true);}}
            finally{if(!shell.isDisposed())refresh.run();}
        });
        UpdateNotifications.addListener(refresh);
        shell.addDisposeListener(event -> {UpdateNotifications.removeListener(refresh);if(!window.getShell().isDisposed() && window.getShell().getData(ID)==shell)window.getShell().setData(ID,null);});
        refresh.run();shell.pack();shell.open();
    }
}
