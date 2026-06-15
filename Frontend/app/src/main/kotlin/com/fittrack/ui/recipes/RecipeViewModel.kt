package com.fittrack.ui.recipes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.data.model.*
import com.fittrack.data.repository.RecipeRepository
import com.fittrack.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val repo: RecipeRepository
) : ViewModel() {

    private val _recipes = MutableStateFlow<Resource<List<RecipeResponse>>>(Resource.Loading)
    val recipes: StateFlow<Resource<List<RecipeResponse>>> = _recipes

    private val _opState = MutableStateFlow<Resource<Unit>?>(null)
    val opState: StateFlow<Resource<Unit>?> = _opState

    private val _activeTab = MutableStateFlow(RecipeTab.ALL)
    val activeTab: StateFlow<RecipeTab> = _activeTab

    private val _addRecipeState = MutableStateFlow<Resource<RecipeResponse>?>(null)
    val addRecipeState: StateFlow<Resource<RecipeResponse>?> = _addRecipeState

    init { loadAll() }

    fun loadAll(query: String = "", tag: String? = null) = viewModelScope.launch {
        _activeTab.value = RecipeTab.ALL
        _recipes.value = Resource.Loading
        val result = repo.search(query, tag)
        if (result is Resource.Success) {
            Log.d("RecipeVM", "SUCCESS: Loaded ${result.data.size} recipes")
            result.data.forEach {
                Log.d("RecipeVM", "Item: id=${it.id}, title=${it.title}, kcal=${it.kcalPerServing}, isOwner=${it.isOwner}")
            }
        } else if (result is Resource.Error) {
            Log.e("RecipeVM", "ERROR: ${result.message}")
        }
        _recipes.value = result
    }

    fun loadMine() = viewModelScope.launch {
        _activeTab.value = RecipeTab.MINE
        _recipes.value = Resource.Loading
        val result = repo.getMine()
        if (result is Resource.Success) {
            Log.d("RecipeVM", "MINE SUCCESS: ${result.data.size} raw items")
            val filtered = result.data.filter { it.isOwner == true }
            Log.d("RecipeVM", "MINE FILTERED: ${filtered.size} items where isOwner=true")
            _recipes.value = Resource.Success(filtered)
        } else if (result is Resource.Error) {
            Log.e("RecipeVM", "MINE ERROR: ${result.message}")
            _recipes.value = result
        }
    }

    fun loadFavorites() = viewModelScope.launch {
        _activeTab.value = RecipeTab.FAVORITES
        _recipes.value = Resource.Loading
        val result = repo.getFavorites()
        _recipes.value = result
    }

    fun toggleFavorite(recipe: RecipeResponse) = viewModelScope.launch {
        val r = if (recipe.isFavorite == true) repo.removeFavorite(recipe.id)
        else repo.addFavorite(recipe.id)
        _opState.value = r
        when (_activeTab.value) {
            RecipeTab.ALL       -> loadAll()
            RecipeTab.MINE      -> loadMine()
            RecipeTab.FAVORITES -> loadFavorites()
        }
    }

    fun deleteRecipe(id: Long) = viewModelScope.launch {
        val r = repo.delete(id)
        _opState.value = r
        if (r is Resource.Success) {
            if (_activeTab.value == RecipeTab.MINE) loadMine()
            else if (_activeTab.value == RecipeTab.ALL) loadAll()
        }
    }

    fun addCustomRecipe(request: RecipeRequest) = viewModelScope.launch {
        _addRecipeState.value = Resource.Loading
        Log.d("RecipeVM", "ADDING: $request")
        val r = repo.addRecipe(request)
        _addRecipeState.value = r
        if (r is Resource.Success) {
            Log.d("RecipeVM", "ADD SUCCESS: ${r.data}")
            if (_activeTab.value == RecipeTab.MINE) loadMine()
        } else if (r is Resource.Error) {
            Log.e("RecipeVM", "ADD ERROR: ${r.message}")
        }
    }

    fun resetAddRecipeState() {
        _addRecipeState.value = null
    }
}

enum class RecipeTab { ALL, MINE, FAVORITES }