package com.fittrack.data.repository

import com.fittrack.data.api.FitTrackApi
import com.fittrack.data.model.*
import com.fittrack.util.Resource
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(private val api: FitTrackApi) {
    suspend fun get(): Resource<ProfileResponse> = try {
        Resource.Success(api.getProfile())
    } catch (e: HttpException) {
        val errorMsg = e.response()?.errorBody()?.string() ?: e.message()
        Resource.Error(errorMsg)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd pobierania profilu")
    }

    suspend fun update(req: ProfileUpdateRequest): Resource<ProfileResponse> = try {
        Resource.Success(api.updateProfile(req))
    } catch (e: HttpException) {
        val errorMsg = e.response()?.errorBody()?.string() ?: e.message()
        Resource.Error(errorMsg)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd zapisywania profilu")
    }
}
