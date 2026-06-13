package com.fittrack.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Worker pokazujacy powiadomienie. Typ przypomnienia (MEAL/WATER/WORKOUT)
 * przekazywany w inputData pod kluczem "type".
 */
class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val type = inputData.getString("type") ?: "WATER"
        val (id, title, body) = when (type) {
            "MEAL"    -> Triple(101, "Czas na posilek", "Pamietaj o regularnym jedzeniu - dodaj wpis do dziennika.")
            "WORKOUT" -> Triple(102, "Trening!", "Czas na ruch - nawet 20 minut robi roznice.")
            else      -> Triple(100, "Pij wode", "Czas na szklanke wody - utrzymuj nawodnienie.")
        }
        NotificationHelper.show(applicationContext, id, title, body)
        return Result.success()
    }
}
