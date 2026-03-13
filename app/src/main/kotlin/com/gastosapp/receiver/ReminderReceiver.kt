package com.gastosapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gastosapp.util.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra("TITLE") ?: "Recordatorio de Gasto"
        val message = intent?.getStringExtra("MESSAGE") ?: "Es hora de registrar tu gasto programado"
        
        context?.let {
            val notificationHelper = NotificationHelper(it)
            notificationHelper.showNotification(title, message)
        }
    }
}
