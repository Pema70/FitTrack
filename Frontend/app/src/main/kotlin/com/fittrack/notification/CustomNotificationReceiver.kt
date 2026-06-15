package com.fittrack.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CustomNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Przypomnienie"
        val message = intent.getStringExtra("message") ?: "Czas na FitTrack!"

        NotificationHelper.show(context, 200, title, message)
    }
}