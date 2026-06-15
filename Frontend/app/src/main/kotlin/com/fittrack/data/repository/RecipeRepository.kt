package com.fittrack.data.repository

import com.fittrack.data.api.FitTrackApi
import com.fittrack.data.model.*
import com.fittrack.util.Resource
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(private val api: FitTrackApi) {
    suspend fun search(query: String, tag: String?) = try {
        Resource.Success(api.getRecipes(query, tag))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd wyszukiwania przepisów")
    }

    suspend fun getMine() = try {
        Resource.Success(api.getMyRecipes())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd pobierania przepisów")
    }

    suspend fun getFavorites() = try {
        Resource.Success(api.getFavoriteRecipes())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd pobierania ulubionych")
    }

    suspend fun addFavorite(id: Long): Resource<Unit> = try {
        api.addFavorite(id); Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd dodawania do ulubionych")
    }

    suspend fun removeFavorite(id: Long): Resource<Unit> = try {
        api.removeFavorite(id); Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd usuwania z ulubionych")
    }

    suspend fun delete(id: Long): Resource<Unit> = try {
        api.deleteRecipe(id); Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd usuwania przepisu")
    }

    suspend fun addRecipe(request: RecipeRequest): Resource<RecipeResponse> = try {
        Resource.Success(api.addRecipe(request))
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        Resource.Error("Błąd 400: $errorBody")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Błąd dodawania przepisu")
    }
}