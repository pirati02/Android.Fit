package ge.dev.baqari.api.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import android.widget.TextView

import androidx.annotation.ColorInt
import androidx.core.app.NotificationCompat

object NotificationExtras {

    fun buildWithBackgroundColor(context: Context, builder: NotificationCompat.Builder, @ColorInt color: Int, remoteViews: RemoteViews?): Notification {
        val notification = buildNotification(builder)

        if (remoteViews != null) {
            val v = LayoutInflater.from(context).inflate(remoteViews.layoutId, null)
            remoteViews.setInt(v.id, "setBackgroundColor", color)

            val useDarkText = Color.red(color) * 0.299 + Color.green(color) * 0.587 + Color.blue(color) * 0.114 > 186
            val textColor = if (useDarkText) -0x1000000 else -0x1
            applyTextColorToRemoteViews(remoteViews, v, textColor)
        }

        return notification
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun buildNotification(builder: NotificationCompat.Builder): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.build()
        } else {
            builder.notification
        }
    }

    private fun applyTextColorToRemoteViews(remoteViews: RemoteViews, view: View, color: Int) {
        if (view is ViewGroup) {
            var i = 0
            val count = view.childCount
            while (i < count) {
                applyTextColorToRemoteViews(remoteViews, view.getChildAt(i), color)
                i++
            }
        } else if (view is TextView) {
            remoteViews.setTextColor(view.getId(), color)
        }
    }
}