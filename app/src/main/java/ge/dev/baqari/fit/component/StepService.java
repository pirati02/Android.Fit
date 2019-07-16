package ge.dev.baqari.fit.component;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;

import ge.dev.baqari.fit.R;
import ge.dev.baqari.fit.api.StepThread;
import ge.dev.baqari.fit.utils.LocalKeys;
import ge.dev.baqari.fit.utils.NotificationExtentionKt;
import ge.dev.baqari.fit.utils.SharedPrefsKt;

public class StepService extends Service {

    private StepThread stepThread;
    private PowerManager.WakeLock wakeLock;
    private boolean startedForeground;

    public static final String RESTART_ACTION = "ge.dev.baqari.fit.component.RESTART";
    public static final String STOP_REMOTELY = "ge.dev.baqari.fit.component.REMOTELY";
    public static final String START = "ge.dev.baqari.fit.component.START";

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
        return START_STICKY;
    }

    private void startPushForeground(Long stepCount) {
        NotificationCompat.Builder notificationBuilder = NotificationExtentionKt.startStepPushForeground(this, stepCount);
        startForeground(1024, notificationBuilder.build());
        EventBus.getDefault().post(true);
    }

    private void fireWakeLock() {
        if (wakeLock != null) {
            if (wakeLock.isHeld())
                wakeLock.release();
            wakeLock = null;
        }
        if (getNotificationEnabled()) {
            PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = mgr.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, StepService.class.getName());
            wakeLock.setReferenceCounted(true);
            wakeLock.acquire(1000L);
        }
    }


    @Override
    public void onTaskRemoved(Intent removedIntent) {
        super.onTaskRemoved(removedIntent);
        if (wakeLock != null) {
            if (wakeLock.isHeld())
                wakeLock.release();
            wakeLock = null;
        }

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
        if (wakeLock != null) {
            if (wakeLock.isHeld())
                wakeLock.release();
            wakeLock = null;
        }

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
