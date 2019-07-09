package com.mygpi.mygpimobilefitness.component

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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

import com.mygpi.mygpimobilefitness.R
import com.mygpi.mygpimobilefitness.api.BaseCalculator
import com.mygpi.mygpimobilefitness.api.StepThread
import com.mygpi.mygpimobilefitness.round
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@SuppressLint("Registered")
class StepService : Service() {
    private var thread: StepThread? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private var startedForeground: Boolean = false
    private var kilometersCount = 0.0

    override fun onCreate() {
        super.onCreate()
        thread = StepThread(this)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (thread?.state == Thread.State.NEW)
            thread?.start()

        if (!startedForeground) {
            mWakeLock(this)
            startedForeground = true
            thread?.onStep = {
                startForeground(it)
            }
            thread?.invokeOnStep()
        }

        return START_STICKY
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun startForeground(stepCount: Long) {
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

        val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)
        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_layout)

        kilometersCount += BaseCalculator.calculateKilometers(stepCount)
        notificationLayout.setTextViewText(R.id.notificationStepCount, "${getString(R.string.steps_)} $stepCount")
        notificationLayout.setTextViewText(R.id.notificationKmCount, "${getString(R.string.km_s)} ${kilometersCount.round(2)}")

        notificationLayoutExpanded.setTextViewText(R.id.notificationStepCount, "${getString(R.string.steps_)} $stepCount")
        notificationLayoutExpanded.setTextViewText(R.id.notificationKmCount, "${getString(R.string.km_s)} ${kilometersCount.round(2)}")

        val mBuilder = NotificationCompat.Builder(this, channelId)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setSmallIcon(R.mipmap.ic_launcher)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)

        startForeground(1024, mBuilder.build())
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
        if (mWakeLock != null) {
            if (mWakeLock?.isHeld == true)
                mWakeLock?.release()
            mWakeLock = null
        }

        stopForeground(true)
        thread?.threadStop()
        val intent = Intent(this, BootBroadcastReceiver::class.java).apply { action = START }
        sendBroadcast(intent)
        super.onDestroy()
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
        mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, StepService::class.java.name)
        mWakeLock?.setReferenceCounted(true)
        mWakeLock?.acquire(1000L)
    }

    companion object {
        val START = "com.mygpi.mygpimobilefitness.start"
    }
}