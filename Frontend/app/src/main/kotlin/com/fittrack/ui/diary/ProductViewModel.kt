package com.fittrack.ui.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fittrack.data.model.ProductResponse
import com.fittrack.data.repository.ProductRepository
import com.fittrack.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repo: ProductRepository
) : ViewModel() {

    private val _products = MutableStateFlow<Resource<List<ProductResponse>>>(Resource.Success(emptyList()))
    val products: StateFlow<Resource<List<ProductResponse>>> = _products

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _products.value = repo.search(query)
        }
    }
}