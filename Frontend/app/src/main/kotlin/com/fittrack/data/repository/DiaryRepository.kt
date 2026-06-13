package com.fittrack.data.repository

import com.fittrack.data.api.FitTrackApi
import com.fittrack.data.model.*
import com.fittrack.util.Resource
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(private val api: FitTrackApi) {
    suspend fun getEntries(date: String): Resource<List<DiaryEntryResponse>> = try {
        Resource.Success(api.getDiary(date))
    } catch (e: HttpException) {
        val errorMsg = e.response()?.errorBody()?.string() ?: e.message()
        Resource.Error(errorMsg)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd pobierania dziennika")
    }

    suspend fun getSummary(date: String): Resource<DailySummaryResponse> = try {
        Resource.Success(api.getDailySummary(date))
    } catch (e: HttpException) {
        val errorMsg = e.response()?.errorBody()?.string() ?: e.message()
        Resource.Error(errorMsg)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd pobierania podsumowania")
    }

    suspend fun addEntry(req: DiaryEntryRequest): Resource<DiaryEntryResponse> = try {
        Resource.Success(api.addDiaryEntry(req))
    } catch (e: HttpException) {
        val errorMsg = e.response()?.errorBody()?.string() ?: e.message()
        Resource.Error(errorMsg)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd dodawania wpisu")
    }

    suspend fun updateEntry(id: Long, quantityG: Double): Resource<DiaryEntryResponse> = try {
        Resource.Success(api.updateDiaryEntry(id, DiaryUpdateRequest(quantityG)))
    } catch (e: HttpException) {
        val errorMsg = e.response()?.errorBody()?.string() ?: e.message()
        Resource.Error(errorMsg)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd aktualizacji wpisu")
    }

    suspend fun deleteEntry(id: Long): Resource<Unit> = try {
        api.deleteDiaryEntry(id)
        Resource.Success(Unit)
    } catch (e: HttpException) {
        val errorMsg = e.response()?.errorBody()?.string() ?: e.message()
        Resource.Error(errorMsg)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd usuwania wpisu")
    }
}
