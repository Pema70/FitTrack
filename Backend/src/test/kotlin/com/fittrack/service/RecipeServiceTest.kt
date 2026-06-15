package com.fittrack.service

import com.fittrack.dto.RecipeIngredientRequest
import com.fittrack.dto.RecipeRequest
import com.fittrack.entity.FavoriteRecipe
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
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.Optional

class RecipeServiceTest {

    private val userRepo: UserRepository = mockk()
    private val recipeRepo: RecipeRepository = mockk()
    private val foodRepo: FoodProductRepository = mockk()
    private val favoriteRepo: FavoriteRecipeRepository = mockk()
    private val service = RecipeService(userRepo, recipeRepo, foodRepo, favoriteRepo)

    @Test
    fun `oblicza wartosci odzywcze na porcje podczas tworzenia przepisu`() {
        val u = User(id = 1L, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        every { foodRepo.findById(1L) } returns Optional.of(
            FoodProduct(
                id          = 1L,                                   
                name        = "Ryz",
                kcalPer100g = BigDecimal("130"),
                proteinG    = BigDecimal("2.7"),
                fatG        = BigDecimal("0.3"),
                carbsG      = BigDecimal("28")
            )
        )
        val slot = slot<Recipe>()
        every { recipeRepo.save(capture(slot)) } answers { slot.captured.apply { id = 50L } }
        every { favoriteRepo.findAllByUserId(1L) } returns emptyList()

        val resp = service.create(
            "u@fittrack.pl", RecipeRequest(
                title       = "Ryz na sniadanie",
                description = "Szybki posilek",
                imageUrl    = null,
                prepTimeMin = 15,
                servings    = 2,
                isPublic    = true,
                tags        = mutableSetOf("wege"),
                ingredients = listOf(
                    RecipeIngredientRequest(
                        productId = 1L,                             
                        quantityG = BigDecimal("200"),
                        unit      = "g"
                    )
                )
            )
        )
        // 200g ryzu = 260 kcal → na 2 porcje = 130 kcal/porcja
        assertEquals(0, BigDecimal("130").compareTo(resp.kcalPerServing))
        assertEquals("Ryz na sniadanie", resp.title)
    }

    @Test
    fun `tworzy przepis z recznie podanymi wartosciami odzywczymi bez skladnikow`() {
        val u = User(id = 1L, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        
        val slot = slot<Recipe>()
        every { recipeRepo.save(capture(slot)) } answers { slot.captured.apply { id = 51L } }
        every { favoriteRepo.findAllByUserId(1L) } returns emptyList()

        val resp = service.create(
            "u@fittrack.pl", RecipeRequest(
                title       = "Reczny przepis",
                description = "Bez skladnikow",
                imageUrl    = null,
                prepTimeMin = 10,
                servings    = 1,
                isPublic    = true,
                tags        = mutableSetOf("fast"),
                kcalPerServing = BigDecimal("500"),
                proteinG       = BigDecimal("30"),
                fatG           = BigDecimal("15"),
                carbsG         = BigDecimal("60"),
                ingredients    = emptyList()
            )
        )

        assertEquals(0, BigDecimal("500").compareTo(resp.kcalPerServing))
        assertEquals(0, BigDecimal("30").compareTo(resp.proteinG))
        assertEquals(0, BigDecimal("15").compareTo(resp.fatG))
        assertEquals(0, BigDecimal("60").compareTo(resp.carbsG))
        assertEquals("Reczny przepis", resp.title)
        assertTrue(slot.captured.ingredients.isEmpty())
    }

    @Test
    fun `search bez tagu wola searchPublic`() {
        every { recipeRepo.searchPublic("kurczak") } returns emptyList()
        val results = service.search("kurczak", null, null)
        assertTrue(results.isEmpty())
        verify { recipeRepo.searchPublic("kurczak") }
    }

    @Test
    fun `search z tagiem wola findPublicByTag`() {
        every { recipeRepo.findPublicByTag("wege") } returns emptyList()
        service.search("", "wege", null)
        verify { recipeRepo.findPublicByTag("wege") }
    }

    @Test
    fun `getMine zwraca przepisy autora`() {
        val u = User(id = 1L, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        val recipe = Recipe(id = 10L, author = u, title = "Moj przepis", servings = 1)
        every { recipeRepo.findAllByAuthorId(1L) } returns listOf(recipe)
        every { favoriteRepo.findAllByUserId(1L) } returns emptyList()

        val results = service.getMine("u@fittrack.pl")
        assertEquals(1, results.size)
        assertEquals("Moj przepis", results[0].title)
        assertTrue(results[0].isOwner)
    }

    @Test
    fun `addFavorite dodaje przepis do ulubionych gdy go tam nie ma`() {
        val u = User(id = 1L, email = "u@fittrack.pl", password = "x")
        val recipe = Recipe(id = 99L, author = u, title = "Test", servings = 1)      
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        every { favoriteRepo.existsByUserIdAndRecipeId(1L, 99L) } returns false        
        every { recipeRepo.findById(99L) } returns Optional.of(recipe)               
        every { favoriteRepo.save(any()) } returns FavoriteRecipe(user = u, recipe = recipe)

        assertDoesNotThrow { service.addFavorite("u@fittrack.pl", 99L) }
        verify(exactly = 1) { favoriteRepo.save(any()) }
    }

    @Test
    fun `removeFavorite usuwa przepis z ulubionych`() {
        val u = User(id = 1L, email = "u@fittrack.pl", password = "x")
        val recipe = Recipe(id = 99L, author = u, title = "Test", servings = 1)
        val favorite = FavoriteRecipe(id = 5L, user = u, recipe = recipe)             

        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        every { favoriteRepo.findByUserIdAndRecipeId(1L, 99L) } returns Optional.of(favorite)
        every { favoriteRepo.delete(favorite) } returns Unit

        assertDoesNotThrow { service.removeFavorite("u@fittrack.pl", 99L) }
        verify(exactly = 1) { favoriteRepo.delete(favorite) }
    }

    @Test
    fun `update modyfikuje przepis i przelicza makro`() {
        val u = User(id = 1L, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)

        val recipe = Recipe(id = 10L, author = u, title = "Stary tytul", servings = 1)
        every { recipeRepo.findById(10L) } returns Optional.of(recipe)               

        every { foodRepo.findById(1L) } returns Optional.of(                         
            FoodProduct(
                id          = 1L,
                name        = "Ryz",
                kcalPer100g = BigDecimal("130"),
                proteinG    = BigDecimal.ZERO,
                fatG        = BigDecimal.ZERO,
                carbsG      = BigDecimal.ZERO
            )
        )
        val slot = slot<Recipe>()
        every { recipeRepo.save(capture(slot)) } answers { slot.captured }
        every { favoriteRepo.findAllByUserId(1L) } returns emptyList()

        val req = RecipeRequest(
            title       = "Nowy tytul",
            description = "Opis",
            imageUrl    = null,
            prepTimeMin = 20,
            servings    = 1,
            isPublic    = false,
            tags        = mutableSetOf("nowy_tag"),
            ingredients = listOf(
                RecipeIngredientRequest(productId = 1L, quantityG = BigDecimal("100"), unit = "g")
            )
        )

        val resp = service.update("u@fittrack.pl", 10L, req)
        assertEquals("Nowy tytul", resp.title)
        assertEquals(0, BigDecimal("130").compareTo(resp.kcalPerServing))
        assertTrue(resp.tags.contains("nowy_tag"))
    }

    @Test
    fun `update rzuca wyjatek gdy uzytkownik nie jest autorem`() {
        val attacker = User(id = 1L, email = "haker@fittrack.pl", password = "x")
        val victim   = User(id = 2L, email = "ofiara@fittrack.pl", password = "y")

        every { userRepo.findByEmail("haker@fittrack.pl") } returns Optional.of(attacker)

        val recipe = Recipe(id = 10L, author = victim, title = "Cudzy przepis", servings = 1)
        every { recipeRepo.findById(10L) } returns Optional.of(recipe)
        every { favoriteRepo.findAllByUserId(1L) } returns emptyList()

        val req = RecipeRequest("T", "D", null, 10, 1, true, mutableSetOf(), null, null, null, null, emptyList())

        val ex = assertThrows<IllegalArgumentException> {
            service.update("haker@fittrack.pl", 10L, req)
        }
        assertEquals("Brak uprawnień — nie jesteś autorem przepisu", ex.message)
    }

    @Test
    fun `delete usuwa przepis autoryzowanego uzytkownika`() {
        val u = User(id = 1L, email = "u@fittrack.pl", password = "x")
        val recipe = Recipe(id = 10L, author = u, title = "Moj przepis", servings = 1)

        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        every { recipeRepo.findById(10L) } returns Optional.of(recipe)
        every { recipeRepo.delete(recipe) } returns Unit

        assertDoesNotThrow { service.delete("u@fittrack.pl", 10L) }
        verify(exactly = 1) { recipeRepo.delete(recipe) }
    }

    @Test
    fun `delete rzuca wyjatek gdy probujesz usunac cudzy przepis`() {
        val attacker = User(id = 1L, email = "haker@fittrack.pl", password = "x")
        val victim   = User(id = 2L, email = "ofiara@fittrack.pl", password = "y")

        every { userRepo.findByEmail("haker@fittrack.pl") } returns Optional.of(attacker)

        val recipe = Recipe(id = 10L, author = victim, title = "Cudzy przepis", servings = 1)
        every { recipeRepo.findById(10L) } returns Optional.of(recipe)

        val ex = assertThrows<IllegalArgumentException> {
            service.delete("haker@fittrack.pl", 10L)
        }
        assertEquals("Brak uprawnień — nie jesteś autorem przepisu", ex.message)
        verify(exactly = 0) { recipeRepo.delete(any()) }
    }
}