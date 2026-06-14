package com.fittrack.data.repository

import com.fittrack.data.api.FitTrackApi
import com.fittrack.data.model.ProductResponse
import com.fittrack.util.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(private val api: FitTrackApi) {

    suspend fun search(query: String): Resource<List<ProductResponse>> = runCatching {
        Resource.Success(api.searchProducts(query))
    }.getOrElse { Resource.Error(it.message ?: "Błąd wyszukiwania produktów") }
}