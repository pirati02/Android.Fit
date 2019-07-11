package ge.dev.baqari.fit.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            StepService.START,
            Intent.ACTION_MEDIA_MOUNTED,
            Intent.ACTION_BOOT_COMPLETED -> {
                val serviceIntent = Intent(context, StepService::class.java)
                context.startService(serviceIntent)
            }
        }
    }
}
