package com.fittrack.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fittrack.MainActivity
import com.fittrack.R

/**
 * Najprostsze powiadomienia LOKALNE (bez Firebase).
 * - Tworzy jeden kanal "fittrack_general".
 * - Wyswietla powiadomienie z tytulem i trescia.
 */
object NotificationHelper {

    const val CHANNEL_ID = "fittrack_general"
    private const val CHANNEL_NAME = "FitTrack"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Przypomnienia o posilkach, wodzie i treningu"
        }
        nm.createNotificationChannel(channel)
    }

    /**
     * Pokaz powiadomienie. Wymaga (na Android 13+) uprawnienia POST_NOTIFICATIONS
     * - jesli brak, system po prostu nic nie pokaze (bez wyjatku).
     */
    fun show(context: Context, notificationId: Int, title: String, body: String) {
        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = android.app.PendingIntent.getActivity(
            context, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notif)
        } catch (_: SecurityException) {
            // Brak uprawnienia POST_NOTIFICATIONS na Android 13+ - ignorujemy.
        }
    }
}
