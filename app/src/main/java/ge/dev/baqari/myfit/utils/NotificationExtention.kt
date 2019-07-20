package ge.dev.baqari.myfit.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import ge.dev.baqari.myfit.R
import ge.dev.baqari.myfit.api.BaseCalculator
import ge.dev.baqari.myfit.component.MainActivity
import ge.dev.baqari.myfit.component.StepService
import io.realm.Realm

fun startStepPushForeground(context: Context, stepCount: Long?): NotificationCompat.Builder {
    var stepCountValue = stepCount
    if (stepCountValue == null) {
        stepCountValue = BaseCalculator.currentSteps(Realm.getDefaultInstance()).toLong()
    }

    var channelId = ""
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val stepCounterChannel = NotificationChannel("step_counter_channel_id", "myfit channel", NotificationManager.IMPORTANCE_NONE).apply {
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            setSound(null, null)
        }
        channelId = stepCounterChannel.id
        notificationManager.createNotificationChannel(stepCounterChannel)
    }

    val notificationLayout = RemoteViews(context.packageName, R.layout.notification_layout)
    val notificationLayoutExpanded = RemoteViews(context.packageName, R.layout.notification_layout)

    val kilometersCount = BaseCalculator.calculateKilometers(stepCountValue).round(2)
    notificationLayout.setTextViewText(R.id.notificationStepCount, "${context.getString(R.string.steps_)} $stepCountValue")
    notificationLayout.setTextViewText(R.id.notificationKmCount, "${context.getString(R.string.km_s)} $kilometersCount")

    notificationLayoutExpanded.setTextViewText(R.id.notificationStepCount, "${context.getString(R.string.steps_)} $stepCountValue")
    notificationLayoutExpanded.setTextViewText(R.id.notificationKmCount, "${context.getString(R.string.km_s)} $kilometersCount")

    val stopServicePendingIntent = PendingIntent.getService(context, 0, Intent(context, StepService::class.java).apply {
        action = StepService.STOP_REMOTELY
    }, 0)

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayoutExpanded)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setSound(null)
            .addAction(NotificationCompat.Action.Builder(0, context.getString(R.string.stop_service), stopServicePendingIntent).build())

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
        notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())

    val notificationIntent = Intent(context, MainActivity::class.java)
    val stackBuilder = TaskStackBuilder.create(context)
    stackBuilder.addParentStack(MainActivity::class.java)
    stackBuilder.addNextIntent(notificationIntent)
    val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    notificationBuilder?.setContentIntent(resultPendingIntent)
    return notificationBuilder
}