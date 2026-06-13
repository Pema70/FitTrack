package com.fittrack.data.repository

import com.fittrack.data.api.FitTrackApi
import com.fittrack.data.model.*
import com.fittrack.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(private val api: FitTrackApi) {
    suspend fun getForDate(date: String): Resource<List<WorkoutResponse>> = runCatching {
        Resource.Success(api.getWorkouts(date))
    }.getOrElse { Resource.Error(it.message ?: "Błąd pobierania treningów") }

    suspend fun log(req: WorkoutRequest): Resource<WorkoutResponse> = runCatching {
        Resource.Success(api.logWorkout(req))
    }.getOrElse { Resource.Error(it.message ?: "Błąd dodawania treningu") }

    suspend fun update(id: Long, req: WorkoutRequest): Resource<WorkoutResponse> = runCatching {
        Resource.Success(api.updateWorkout(id, req))
    }.getOrElse { Resource.Error(it.message ?: "Błąd aktualizacji treningu") }

    suspend fun delete(id: Long): Resource<Unit> = runCatching {
        api.deleteWorkout(id); Resource.Success(Unit)
    }.getOrElse { Resource.Error(it.message ?: "Błąd usuwania treningu") }
}
