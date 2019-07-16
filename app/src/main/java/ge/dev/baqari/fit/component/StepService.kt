package ge.dev.baqari.fit.component

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import ge.dev.baqari.fit.*
import ge.dev.baqari.fit.api.BaseCalculator
import ge.dev.baqari.fit.api.StepThread
import io.realm.Realm
import org.greenrobot.eventbus.EventBus


@SuppressLint("Registered")
open class StepService : Service() {
    private var thread: StepThread? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private var startedForeground: Boolean = false
    private var kilometersCount = 0.0
    private var notificationId = 1024
    private var notificationBuilder: NotificationCompat.Builder? = null
    private val notificationEnabled: Boolean?
        get() {
            return storage()["notification_enabled", true]!!
        }

    override fun onCreate() {
        super.onCreate()
        thread = StepThread(this)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            if (intent != null) {
                if (intent.action == STOP_REMOTELY) {
                    startedForeground = false
                    storage()["notification_enabled"] = false
                    EventBus.getDefault().post(false)
                    stopForeground(true)
                } else {
                    if (thread == null)
                        thread = StepThread(this)
                    thread?.run()

                    if (intent.action == RESTART_ACTION)
                        startedForeground = false

                    if (!startedForeground) {
                        mWakeLock(this)
                        thread?.onStep = {
                            if (notificationEnabled == true) {
                                startPushForeground(it)
                                startedForeground = true
                            }
                            mWakeLock(this)
                        }
                        thread?.invokeOnStep()

                        if (notificationEnabled == true) {
                            startPushForeground(null)
                            startedForeground = true
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return START_STICKY
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun startPushForeground(stepCount: Long?) {
        var stepCountValue = stepCount
        if (stepCountValue == null) {
            stepCountValue = BaseCalculator.currentSteps(Realm.getDefaultInstance())?.toLong()
        }

        var channelId = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val stepCounterChannel = NotificationChannel("step_counter_channel_id",
                    "step_counter_channel_id", NotificationManager.IMPORTANCE_LOW).apply {
                enableVibration(false)
                lightColor = Color.RED
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
            }
            channelId = stepCounterChannel.id
            notificationManager.createNotificationChannel(stepCounterChannel)
        }

        val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)
        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_layout)

        kilometersCount = BaseCalculator.calculateKilometers(stepCountValue)
        notificationLayout.setTextViewText(R.id.notificationStepCount, "${getString(ge.dev.baqari.fit.R.string.steps_)} $stepCountValue")
        notificationLayout.setTextViewText(R.id.notificationKmCount, "${getString(ge.dev.baqari.fit.R.string.km_s)} ${kilometersCount.round(2)}")

        notificationLayoutExpanded.setTextViewText(ge.dev.baqari.fit.R.id.notificationStepCount, "${getString(ge.dev.baqari.fit.R.string.steps_)} $stepCountValue")
        notificationLayoutExpanded.setTextViewText(ge.dev.baqari.fit.R.id.notificationKmCount, "${getString(R.string.km_s)} ${kilometersCount.round(2)}")

        val stopServicePendingIntent = PendingIntent.getService(this, 0, Intent(this, StepService::class.java).apply {
            action = STOP_REMOTELY
        }, 0)

        notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(null)
                .addAction(NotificationCompat.Action.Builder(0, getString(R.string.stop_service), stopServicePendingIntent).build())
                .setVibrate(longArrayOf(0))

        val notificationIntent = Intent(this, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder?.setContentIntent(resultPendingIntent)

        startForeground(notificationId, notificationBuilder?.build())
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        if (mWakeLock != null) {
            if (mWakeLock?.isHeld == true)
                mWakeLock?.release()
            mWakeLock = null
        }

        stopForeground(true)
        thread?.threadStop()
        if (notificationEnabled == true) {
            val intent = Intent(this, BootBroadcastReceiver::class.java).apply { action = START }
            sendBroadcast(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mWakeLock != null) {
            if (mWakeLock?.isHeld == true)
                mWakeLock?.release()
            mWakeLock = null
        }

        stopForeground(true)
        thread?.threadStop()
        if (notificationEnabled == true) {
            val intent = Intent(this, BootBroadcastReceiver::class.java).apply { action = START }
            sendBroadcast(intent)
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @Synchronized
    private fun mWakeLock(context: Context) {
        if (mWakeLock != null) {
            if (mWakeLock?.isHeld == true)
                mWakeLock?.release()
            mWakeLock = null
        }
        if (notificationEnabled == true) {
            val mgr = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            mWakeLock = mgr.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, StepService::class.java.name)
            mWakeLock?.setReferenceCounted(true)
            mWakeLock?.acquire(1000L)
        }
    }

    companion object {
        val RESTART_ACTION = "ge.dev.baqari.fit.component.RESTART"
        val STOP_REMOTELY = "ge.dev.baqari.fit.component.REMOTELY"
        val START = "ge.dev.baqari.fit.component.START"
    }
}

