package com.fittrack.notification

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Planuje cykliczne przypomnienia (woda, posilki, trening) przez WorkManager.
 * Najprostsza wersja: WATER co 2h, MEAL co 4h, WORKOUT raz dziennie.
 *
 * Uwaga: PeriodicWorkRequest ma minimalny interwal 15 minut.
 */
object NotificationScheduler {

    private const val WATER   = "fittrack_water_reminder"
    private const val MEAL    = "fittrack_meal_reminder"
    private const val WORKOUT = "fittrack_workout_reminder"

    fun scheduleAll(context: Context) {
        schedule(context, WATER,   "WATER",   2,  TimeUnit.HOURS)
        schedule(context, MEAL,    "MEAL",    4,  TimeUnit.HOURS)
        schedule(context, WORKOUT, "WORKOUT", 24, TimeUnit.HOURS)
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WATER)
        WorkManager.getInstance(context).cancelUniqueWork(MEAL)
        WorkManager.getInstance(context).cancelUniqueWork(WORKOUT)
    }

    private fun schedule(
        context: Context,
        uniqueName: String,
        type: String,
        interval: Long,
        unit: TimeUnit
    ) {
        val data = workDataOf("type" to type)
        val req = PeriodicWorkRequestBuilder<ReminderWorker>(interval, unit)
            .setInputData(data)
            .setInitialDelay(interval, unit)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueName,
            ExistingPeriodicWorkPolicy.UPDATE,
            req
        )
    }
}
