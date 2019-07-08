package com.mygpi.mygpimobilefitness.component

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null && intent.action == StepService.START) {
            val serviceIntent = Intent(context, StepService::class.java)
            context.startService(serviceIntent)
        }
    }
}
