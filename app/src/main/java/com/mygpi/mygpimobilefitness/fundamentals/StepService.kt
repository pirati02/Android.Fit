package com.mygpi.mygpimobilefitness.fundamentals

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
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder

import com.mygpi.mygpimobilefitness.ApplicationImpl
import com.mygpi.mygpimobilefitness.activity.MainActivity
import com.mygpi.mygpimobilefitness.R
import com.mygpi.mygpimobilefitness.api.StepThread

@SuppressLint("Registered")
class StepService : Service() {
    private var thread: StepThread? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private var startedForeground: Boolean = false

    override fun onCreate() {
        super.onCreate()
        thread = StepThread(this)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val app = application as ApplicationImpl
        app.serviceRun = true
        if (thread?.state == Thread.State.NEW)
            thread?.start()

        if (!startedForeground) {
            startForeground()
            mWakeLock(this)
            startedForeground = true
        }

        return START_STICKY
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun startForeground() {
        var channelId = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val stepCounterChannel = NotificationChannel("step_counter_channel_id",
                    "step_counter_channel_id", NotificationManager.IMPORTANCE_DEFAULT)
            stepCounterChannel.enableVibration(true)
            stepCounterChannel.lightColor = Color.RED
            stepCounterChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            channelId = stepCounterChannel.id
            notificationManager.createNotificationChannel(stepCounterChannel)
        }

        val mBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("step")
                .setContentText("service is running")

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
        val intent = Intent(START)
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
        val app = application as ApplicationImpl
        app.serviceRun = false
        val intent = Intent(START)
        sendBroadcast(intent)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @Synchronized
    private fun mWakeLock(context: Context) {
        if (mWakeLock != null) {
            if (mWakeLock!!.isHeld)
                mWakeLock!!.release()
            mWakeLock = null
        }

        val mgr = context
                .getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                StepService::class.java.name)
        mWakeLock?.setReferenceCounted(true)
        mWakeLock?.acquire(10 * 60 * 1000L)
    }

    companion object {
        val START = "com.mygpi.mygpimobilefitness.start"
    }
}