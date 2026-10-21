package com.example.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class WaterReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WaterReminderReceiver", "Received intent action: ${intent.action}")
        when (intent.action) {
            WaterReminderManager.ACTION_TRIGGER_REMINDER,
            Intent.ACTION_BOOT_COMPLETED -> {
                WaterReminderManager.showHydrationNotification(context)
            }
        }
    }
}
