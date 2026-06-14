package com.fittrack.controller

import com.fittrack.dto.ProductSearchResponse
import com.fittrack.repository.FoodProductRepository
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "Products", description = "Wyszukiwarka produktów spożywczych")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/products")
@Validated  // ← wymagane do walidacji @RequestParam
class ProductController(private val repo: FoodProductRepository) {

    @GetMapping
    fun search(
        @RequestParam(defaultValue = "")
        @Size(max = 100, message = "Fraza wyszukiwania może mieć max 100 znaków")
        q: String
    ): List<ProductSearchResponse> =
        repo.findByNameContainingIgnoreCase(q).take(20).map {
            ProductSearchResponse(
                id             = it.id,
                name           = it.name,
                kcalPer100g    = it.kcalPer100g.toDouble(),
                proteinPer100g = it.proteinG.toDouble(),
                fatPer100g     = it.fatG.toDouble(),
                carbsPer100g   = it.carbsG.toDouble()
            )
        }
}