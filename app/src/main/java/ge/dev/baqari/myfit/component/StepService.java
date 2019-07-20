package ge.dev.baqari.myfit.component;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;

import ge.dev.baqari.myfit.api.StepThread;
import ge.dev.baqari.myfit.utils.LocalKeys;
import ge.dev.baqari.myfit.utils.NotificationExtentionKt;
import ge.dev.baqari.myfit.utils.SharedPrefsKt;

public class StepService extends Service {

    private StepThread stepThread;
    private boolean startedForeground;

    public static final String RESTART_ACTION = "ge.dev.baqari.myfit.RESTART";
    public static final String STOP_REMOTELY = "ge.dev.baqari.myfit.REMOTELY";
    public static final String START = "ge.dev.baqari.myfit.START";

    public boolean getNotificationEnabled() {
        return SharedPrefsKt.get().getBoolean(LocalKeys.INSTANCE.getNotificationEnabledKey(), true);
    }

    public void setNotificationEnabled(boolean enabled) {
        SharedPrefsKt.get().edit().putBoolean(LocalKeys.INSTANCE.getNotificationEnabledKey(), enabled).apply();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        stepThread = new StepThread(this);
        if (Build.VERSION.SDK_INT >= 26 && getNotificationEnabled()) {
            startPushForeground(null);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (stepThread == null)
                stepThread = new StepThread(this);
            stepThread.run();

            if (intent.getAction() != null) {
                if (intent.getAction().equals(STOP_REMOTELY)) {
                    EventBus.getDefault().post(false);
                    setNotificationEnabled(false);
                    stopForeground(true);
                } else {
                    if (intent.getAction().equals(RESTART_ACTION) || intent.getAction().equals(START))
                        startedForeground = false;

                    if (!startedForeground) {
                        stepThread.setOnStep(new StepThread.OnStepUpdateListener() {
                            @Override
                            public void onStep(long step) {
                                if (getNotificationEnabled()) {
                                    startPushForeground(step);
                                    startedForeground = true;
                                }
                                fireWakeLock();
                            }
                        });
                        stepThread.invokeOnStep();
                        if (getNotificationEnabled()) {
                            startPushForeground(null);
                            startedForeground = true;
                        }
                        fireWakeLock();
                    }
                }
            }
        }
        return START_STICKY;
    }

    private void startPushForeground(Long stepCount) {
        NotificationCompat.Builder notificationBuilder = NotificationExtentionKt.startStepPushForeground(this, stepCount);
        startForeground(1024, notificationBuilder.build());
        EventBus.getDefault().post(true);
    }

    private void fireWakeLock() {
        //if (getNotificationEnabled()) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(this, BootBroadcastReceiver.class);
            intent.setAction(START);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000,
                    PendingIntent.getBroadcast(this, 0, intent, 0));
        }
        //}
    }


    @Override
    public void onTaskRemoved(Intent removedIntent) {
        super.onTaskRemoved(removedIntent);
        stopForeground(true);
        stepThread.threadStop();
        stepThread = null;
        //if (getNotificationEnabled()) {
        Intent intent = new Intent(this, BootBroadcastReceiver.class);
        intent.setAction(START);
        sendBroadcast(intent);
        //}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        stepThread.threadStop();
        stepThread = null;
        //if (getNotificationEnabled()) {
        Intent intent = new Intent(this, BootBroadcastReceiver.class);
        intent.setAction(START);
        sendBroadcast(intent);
        //}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
