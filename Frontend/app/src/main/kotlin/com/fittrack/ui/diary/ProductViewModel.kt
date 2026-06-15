package com.fittrack.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.data.model.ProductResponse
import com.fittrack.data.repository.ProductRepository
import com.fittrack.data.repository.RecipeRepository
import com.fittrack.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepo: ProductRepository,
    private val recipeRepo: RecipeRepository
) : ViewModel() {

    private val _products = MutableStateFlow<Resource<List<ProductResponse>>>(Resource.Success(emptyList()))
    val products: StateFlow<Resource<List<ProductResponse>>> = _products

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _products.value = Resource.Loading

            val productsRes = productRepo.search(query)
            val recipesRes = recipeRepo.search(query, null)

            val combined = mutableListOf<ProductResponse>()

            if (productsRes is Resource.Success) {
                combined.addAll(productsRes.data)
            }
            if (recipesRes is Resource.Success) {
                combined.addAll(recipesRes.data.map { recipe ->
                    ProductResponse(
                        id = recipe.id,
                        name = "Recipe: ${recipe.title}",
                        kcalPer100g = recipe.kcalPerServing ?: 0.0,
                        proteinPer100g = recipe.proteinG ?: 0.0,
                        fatPer100g = recipe.fatG ?: 0.0,
                        carbsPer100g = recipe.carbsG ?: 0.0,
                        isRecipe = true
                    )
                })
            }

            _products.value = Resource.Success(combined)
        }
    }
}