package ge.dev.baqari.fit.component

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import ge.dev.baqari.fit.R
import ge.dev.baqari.fit.api.BaseCalculator
import ge.dev.baqari.fit.api.StepThread
import ge.dev.baqari.fit.round


@SuppressLint("Registered")
class StepService : Service() {
    private var thread: StepThread? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private var startedForeground: Boolean = false
    private var kilometersCount = 0.0
    private var notificationId = 1024
    private var notificationBuilder: NotificationCompat.Builder? = null

    override fun onCreate() {
        super.onCreate()
        thread = StepThread(this)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (thread?.state == Thread.State.NEW || thread?.state == Thread.State.TERMINATED) {
            if (thread?.state == Thread.State.TERMINATED) {
                thread?.reinitializeSensor = true
                startedForeground = false
            }
            thread?.start()
        }

        if (!startedForeground) {
            mWakeLock(this)
            thread?.onStep = {
                startPushForeground(it)
                mWakeLock(this)
            }
            thread?.invokeOnStep()
            startedForeground = true
        }

        return START_STICKY
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun startPushForeground(stepCount: Long) {
        var channelId = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val stepCounterChannel = NotificationChannel("step_counter_channel_id",
                    "step_counter_channel_id", NotificationManager.IMPORTANCE_DEFAULT)
            stepCounterChannel.enableVibration(true)
            stepCounterChannel.lightColor = Color.RED
            stepCounterChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channelId = stepCounterChannel.id
            notificationManager.createNotificationChannel(stepCounterChannel)
        }

        val notificationLayout = RemoteViews(packageName, ge.dev.baqari.fit.R.layout.notification_layout)
        val notificationLayoutExpanded = RemoteViews(packageName, ge.dev.baqari.fit.R.layout.notification_layout)

        kilometersCount = BaseCalculator.calculateKilometers(stepCount)
        notificationLayout.setTextViewText(ge.dev.baqari.fit.R.id.notificationStepCount, "${getString(ge.dev.baqari.fit.R.string.steps_)} $stepCount")
        notificationLayout.setTextViewText(ge.dev.baqari.fit.R.id.notificationKmCount, "${getString(ge.dev.baqari.fit.R.string.km_s)} ${kilometersCount.round(2)}")

        notificationLayoutExpanded.setTextViewText(ge.dev.baqari.fit.R.id.notificationStepCount, "${getString(ge.dev.baqari.fit.R.string.steps_)} $stepCount")
        notificationLayoutExpanded.setTextViewText(ge.dev.baqari.fit.R.id.notificationKmCount, "${getString(R.string.km_s)} ${kilometersCount.round(2)}")

        notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setSmallIcon(R.mipmap.ic_launcher)
                .extend(NotificationCompat.WearableExtender().apply {

                })

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
        val intent = Intent(this, BootBroadcastReceiver::class.java).apply { action = START }
        sendBroadcast(intent)
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
        val intent = Intent(this, BootBroadcastReceiver::class.java).apply { action = START }
        sendBroadcast(intent)
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

        val mgr = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = mgr.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, StepService::class.java.name)
        mWakeLock?.setReferenceCounted(true)
        mWakeLock?.acquire(1000L)
    }

    companion object {
        val START = "com.mygpi.mygpimobilefitness.start"
    }
}

