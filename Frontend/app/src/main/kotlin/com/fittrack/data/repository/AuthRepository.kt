package com.fittrack.data.repository

import com.fittrack.data.api.FitTrackApi
import com.fittrack.data.model.*
import com.fittrack.util.Resource
import com.fittrack.util.TokenManager
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: FitTrackApi,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Resource<AuthResponse> = try {
        val resp = api.login(LoginRequest(email, password))
        resp.accessToken?.let { tokenManager.saveToken(it) }
        Resource.Success(resp)
    } catch (e: HttpException) {
        val errorMsg = e.response()?.errorBody()?.string() ?: e.message()
        Resource.Error(errorMsg)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd logowania")
    }

    suspend fun register(email: String, password: String): Resource<AuthResponse> = try {
        val resp = api.register(RegisterRequest(email, password))
        resp.accessToken?.let { tokenManager.saveToken(it) }
        Resource.Success(resp)
    } catch (e: HttpException) {
        val errorMsg = e.response()?.errorBody()?.string() ?: e.message()
        Resource.Error(errorMsg)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd rejestracji")
    }

    suspend fun changePassword(old: String, new: String): Resource<Unit> = try {
        api.changePassword(ChangePasswordRequest(old, new))
        Resource.Success(Unit)
    } catch (e: HttpException) {
        val errorMsg = e.response()?.errorBody()?.string() ?: e.message()
        Resource.Error(errorMsg)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd zmiany hasła")
    }

    suspend fun logout() { tokenManager.clearToken() }
}
