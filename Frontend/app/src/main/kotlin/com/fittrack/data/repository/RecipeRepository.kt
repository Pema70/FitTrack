package com.fittrack.data.repository

import com.fittrack.data.api.FitTrackApi
import com.fittrack.data.model.*
import com.fittrack.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(private val api: FitTrackApi) {
    suspend fun search(query: String, tag: String?) = runCatching {
        Resource.Success(api.getRecipes(query, tag))
    }.getOrElse { Resource.Error(it.message ?: "Błąd wyszukiwania przepisów") }

    suspend fun getMine() = runCatching {
        Resource.Success(api.getMyRecipes())
    }.getOrElse { Resource.Error(it.message ?: "Błąd pobierania przepisów") }

    suspend fun getFavorites() = runCatching {
        Resource.Success(api.getFavoriteRecipes())
    }.getOrElse { Resource.Error(it.message ?: "Błąd pobierania ulubionych") }

    suspend fun addFavorite(id: Long): Resource<Unit> = runCatching {
        api.addFavorite(id); Resource.Success(Unit)
    }.getOrElse { Resource.Error(it.message ?: "Błąd dodawania do ulubionych") }

    suspend fun removeFavorite(id: Long): Resource<Unit> = runCatching {
        api.removeFavorite(id); Resource.Success(Unit)
    }.getOrElse { Resource.Error(it.message ?: "Błąd usuwania z ulubionych") }

    suspend fun delete(id: Long): Resource<Unit> = runCatching {
        api.deleteRecipe(id); Resource.Success(Unit)
    }.getOrElse { Resource.Error(it.message ?: "Błąd usuwania przepisu") }
}
