package com.fittrack.ui.recipes

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

    init { loadAll() }

    fun loadAll(query: String = "", tag: String? = null) = viewModelScope.launch {
        _activeTab.value = RecipeTab.ALL
        _recipes.value = Resource.Loading
        _recipes.value = repo.search(query, tag)
    }

    fun loadMine() = viewModelScope.launch {
        _activeTab.value = RecipeTab.MINE
        _recipes.value = Resource.Loading
        _recipes.value = repo.getMine()
    }

    fun loadFavorites() = viewModelScope.launch {
        _activeTab.value = RecipeTab.FAVORITES
        _recipes.value = Resource.Loading
        _recipes.value = repo.getFavorites()
    }

    fun toggleFavorite(recipe: RecipeResponse) = viewModelScope.launch {
        val r = if (recipe.isFavorite) repo.removeFavorite(recipe.id)
        else repo.addFavorite(recipe.id)
        _opState.value = r
        // Odśwież aktywną zakładkę
        when (_activeTab.value) {
            RecipeTab.ALL       -> loadAll()
            RecipeTab.MINE      -> loadMine()
            RecipeTab.FAVORITES -> loadFavorites()
        }
    }

    fun deleteRecipe(id: Long) = viewModelScope.launch {
        val r = repo.delete(id)
        _opState.value = r
        if (r is Resource.Success) loadMine()
    }
}

enum class RecipeTab { ALL, MINE, FAVORITES }
