package com.fittrack.service

import com.fittrack.dto.RecipeIngredientRequest
import com.fittrack.dto.RecipeRequest
import com.fittrack.entity.FoodProduct
import com.fittrack.entity.Recipe
import com.fittrack.entity.User
import com.fittrack.repository.FavoriteRecipeRepository
import com.fittrack.repository.FoodProductRepository
import com.fittrack.repository.RecipeRepository
import com.fittrack.repository.UserRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Optional

class RecipeServiceTest {

    private val userRepo: UserRepository = mockk()
    private val recipeRepo: RecipeRepository = mockk()
    private val foodRepo: FoodProductRepository = mockk()
    private val favoriteRepo: FavoriteRecipeRepository = mockk()
    private val service = RecipeService(userRepo, recipeRepo, foodRepo, favoriteRepo)

    @Test
    fun `oblicza wartosci odzywcze na porcje`() {
        val u = User(id = 1, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        every { foodRepo.findById(1) } returns Optional.of(FoodProduct(
            id=1, name="Ryz",
            kcalPer100g = BigDecimal("130"),
            proteinG    = BigDecimal("2.7"),
            fatG        = BigDecimal("0.3"),
            carbsG      = BigDecimal("28")
        ))
        val slot = slot<Recipe>()
        every { recipeRepo.save(capture(slot)) } answers { slot.captured.apply { id = 50 } }

        val resp = service.create("u@fittrack.pl", RecipeRequest(
            title = "Ryz na sniadanie",
            prepTimeMin = 15,
            servings = 2,
            ingredients = listOf(RecipeIngredientRequest(productId = 1, quantityG = BigDecimal("200")))
        ))
        assertEquals(0, BigDecimal("130").compareTo(resp.kcalPerServing))
        assertEquals("Ryz na sniadanie", resp.title)
    }

    @Test
    fun `search bez tagu woła searchPublic`() {
        every { recipeRepo.searchPublic("kurczak") } returns emptyList()
        val results = service.search("kurczak", null)
        assertTrue(results.isEmpty())
        verify { recipeRepo.searchPublic("kurczak") }
    }

    @Test
    fun `search z tagiem woła findPublicByTag`() {
        every { recipeRepo.findPublicByTag("wege") } returns emptyList()
        service.search("", "wege")
        verify { recipeRepo.findPublicByTag("wege") }
    }
}
