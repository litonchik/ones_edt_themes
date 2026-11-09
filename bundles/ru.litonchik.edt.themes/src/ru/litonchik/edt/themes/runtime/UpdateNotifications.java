package ru.litonchik.edt.themes.runtime;

import java.time.Clock;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import ru.litonchik.edt.themes.model.*;

/** Passive status only: no dialog, command, browser, focus or installation from a worker. */
public final class UpdateNotifications {
    public static final String PREFIX="updates.";
    public static final String ENABLED=PREFIX+"enabled",LAST=PREFIX+"lastAttempt",LATEST=PREFIX+"latest";
    public enum Phase {IDLE,CHECKING,CURRENT,AVAILABLE,FAILED,DISABLED}
    public record State(Phase phase,ReleaseInfo release){}
    private static final CopyOnWriteArrayList<Runnable> listeners=new CopyOnWriteArrayList<>();
    private static volatile UpdateNotifications instance;
    private final Display display;private final ReleaseSource source;private final Clock clock;
    private final org.eclipse.ui.IWindowListener windows=new org.eclipse.ui.IWindowListener(){
        @Override public void windowOpened(org.eclipse.ui.IWorkbenchWindow window){changed();}
        @Override public void windowActivated(org.eclipse.ui.IWorkbenchWindow window){}
        @Override public void windowClosed(org.eclipse.ui.IWorkbenchWindow window){}
        @Override public void windowDeactivated(org.eclipse.ui.IWorkbenchWindow window){}
    };
    private final Object lock=new Object();
    private volatile State state=new State(Phase.IDLE,null);
    private volatile boolean closed;private boolean running;private volatile long generation;private Job timer,worker;
    private UpdateNotifications(ReleaseSource source,Clock clock){this.source=source;this.clock=clock;display=PlatformUI.getWorkbench().getDisplay();PlatformUI.getWorkbench().addWindowListener(windows);}
    public static ReleaseVersion installed(){var v=Platform.getBundle(SnapshotPreferences.PLUGIN_ID).getVersion();return new ReleaseVersion(v.getMajor(),v.getMinor(),v.getMicro());}
    private static org.eclipse.core.runtime.preferences.IEclipsePreferences prefs(){return InstanceScope.INSTANCE.getNode(SnapshotPreferences.PLUGIN_ID);}
    public static boolean isEnabled(){return prefs().getBoolean(ENABLED,true);}
    public static boolean isRunning(){var m=instance;return m!=null && !m.closed;}
    public static State state(){var m=instance;return m==null?new State(Phase.IDLE,null):m.state;}
    public static boolean available(){var s=state();return isEnabled() && s.release()!=null && s.release().version().compareTo(installed())>0;}
    public static void addListener(Runnable listener){listeners.addIfAbsent(listener);}
    public static void removeListener(Runnable listener){listeners.remove(listener);}
    public static void start(){start(new ReleaseFeedClient(ReleaseFeedClient.FEED,EclipseProxyConnections::open,3000),Clock.systemUTC());}
    /** Starts with an explicit feed/clock for an embedded workbench; normal startup uses the fixed project feed. */
    public static synchronized void start(ReleaseSource source,Clock clock){
        if(instance!=null || !PlatformUI.isWorkbenchRunning())return;
        var m=new UpdateNotifications(source,clock);instance=m;
        String cached=prefs().get(LATEST,"");
        try{var v=ReleaseVersion.parse(cached);if(v.compareTo(installed())>0)m.state=new State(Phase.AVAILABLE,new ReleaseInfo(v));else prefs().remove(LATEST);}catch(IllegalArgumentException ignored){}
        if(!isEnabled())m.state=new State(Phase.DISABLED,m.state.release());else m.schedule(30000);
        m.changed();
    }
    private void changed(){
        if(display.isDisposed())return;
        display.asyncExec(() -> {if(instance!=this || closed || display.isDisposed())return;ru.litonchik.edt.themes.ui.ShowUpdateDetailsHandler.refreshStatus();for(var listener:listeners)listener.run();});
    }
    private void schedule(long delay){
        synchronized(lock){
            if(closed || !isEnabled())return;
            if(timer!=null)timer.cancel();
            timer=new Job("Темы EDT: ожидание проверки обновлений"){
                @Override protected IStatus run(IProgressMonitor monitor){
                    if(monitor.isCanceled())return Status.CANCEL_STATUS;
                    long wait=UpdateSchedule.delay(clock.millis(),prefs().getLong(LAST,0));
                    if(wait==0)request(false);else schedule(wait);
                    return Status.OK_STATUS;
                }
            };timer.setSystem(true);timer.schedule(Math.max(0,delay));
        }
    }
    public static boolean checkNow(){var m=instance;return m!=null && m.request(true);}
    private boolean request(boolean manual){
        synchronized(lock){
            if(closed || running || (!manual && !isEnabled()))return false;
            if(timer!=null){timer.cancel();timer=null;}
            running=true;long token=++generation;
            state=new State(Phase.CHECKING,state.release());changed();
            worker=new Job("Темы EDT: проверка новой версии"){
                @Override protected IStatus run(IProgressMonitor monitor){
                    ReleaseInfo result=null;boolean success=false;
                    try{result=source.read(() -> monitor.isCanceled() || closed || token!=generation);success=result!=null;}
                    catch(Exception ignored){/* A failed background check has no UI side effects. */}
                    synchronized(lock){
                        if(closed || token!=generation || monitor.isCanceled())return Status.CANCEL_STATUS;
                        running=false;
                        if(success){
                            boolean newer=result.version().compareTo(installed())>0;
                            state=new State(newer?Phase.AVAILABLE:Phase.CURRENT,newer?result:null);
                            if(newer)prefs().put(LATEST,result.version().toString());else prefs().remove(LATEST);
                        }else state=new State(Phase.FAILED,state.release());
                        prefs().putLong(LAST,clock.millis());try{prefs().flush();}catch(org.osgi.service.prefs.BackingStoreException ignored){}
                        changed();schedule(UpdateSchedule.DAY);
                    }
                    return Status.OK_STATUS;
                }
            };worker.setSystem(true);worker.setPriority(Job.DECORATE);worker.schedule();return true;
        }
    }
    public static void setEnabled(boolean enabled) throws org.osgi.service.prefs.BackingStoreException {
        if(enabled==isEnabled())return;
        if(enabled)prefs().remove(ENABLED);else prefs().putBoolean(ENABLED,false);prefs().flush();
        var m=instance;if(m==null)return;
        synchronized(m.lock){
            ++m.generation;m.running=false;if(m.worker!=null)m.worker.cancel();if(m.timer!=null)m.timer.cancel();
            m.state=new State(enabled?(m.state.release()==null?Phase.IDLE:Phase.AVAILABLE):Phase.DISABLED,m.state.release());
            if(enabled)m.schedule(30000);m.changed();
        }
    }
    /** Never waits for network IO or for the UI thread during shutdown. */
    public static synchronized void stop(){
        var m=instance;if(m==null)return;instance=null;if(PlatformUI.isWorkbenchRunning())PlatformUI.getWorkbench().removeWindowListener(m.windows);
        synchronized(m.lock){m.closed=true;++m.generation;m.running=false;if(m.worker!=null)m.worker.cancel();if(m.timer!=null)m.timer.cancel();}
        if(!m.display.isDisposed())m.display.asyncExec(() -> {if(instance==null){ru.litonchik.edt.themes.ui.ShowUpdateDetailsHandler.refreshStatus();for(var listener:listeners)listener.run();}});
    }
}
