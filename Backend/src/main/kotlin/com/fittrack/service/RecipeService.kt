package com.fittrack.service

import com.fittrack.dto.*
import com.fittrack.entity.*
import com.fittrack.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class RecipeService(
    private val userRepo: UserRepository,
    private val recipeRepo: RecipeRepository,
    private val foodRepo: FoodProductRepository,
    private val favoriteRepo: FavoriteRecipeRepository
) {
    @Transactional(readOnly = true)
    fun search(query: String, tag: String?, email: String?): List<RecipeResponse> {
        val results = if (!tag.isNullOrBlank()) recipeRepo.findPublicByTag(tag)
                      else recipeRepo.searchPublic(query)
        
        val user = email?.let { userRepo.findByEmail(it).orElse(null) }
        val favorites = user?.let { favoriteRepo.findAllByUserId(it.id).map { f -> f.recipe.id }.toSet() } ?: emptySet()
        
        return results.map { it.toResponse(user?.id, favorites) }
    }

    @Transactional(readOnly = true)
    fun getMine(email: String): List<RecipeResponse> {
        val user = userRepo.findByEmail(email).orElseThrow()
        val favorites = favoriteRepo.findAllByUserId(user.id).map { f -> f.recipe.id }.toSet()
        return recipeRepo.findAllByAuthorId(user.id).map { it.toResponse(user.id, favorites) }
    }

    @Transactional(readOnly = true)
    fun getFavorites(email: String): List<RecipeResponse> {
        val user = userRepo.findByEmail(email).orElseThrow()
        val favoriteRecipes = favoriteRepo.findAllByUserId(user.id).map { it.recipe }
        val favoriteIds = favoriteRecipes.map { it.id }.toSet()
        return favoriteRecipes.map { it.toResponse(user.id, favoriteIds) }
    }

    @Transactional
    fun addFavorite(email: String, recipeId: Long) {
        val user = userRepo.findByEmail(email).orElseThrow()
        if (favoriteRepo.existsByUserIdAndRecipeId(user.id, recipeId)) return
        val recipe = recipeRepo.findById(recipeId).orElseThrow()
        favoriteRepo.save(FavoriteRecipe(user = user, recipe = recipe))
    }

    @Transactional
    fun removeFavorite(email: String, recipeId: Long) {
        val user = userRepo.findByEmail(email).orElseThrow()
        favoriteRepo.findByUserIdAndRecipeId(user.id, recipeId)
            .ifPresent { favoriteRepo.delete(it) }
    }

    @Transactional
    fun create(email: String, req: RecipeRequest): RecipeResponse {
        val user = userRepo.findByEmail(email).orElseThrow()
        val recipe = Recipe(
            author      = user,
            title       = req.title,
            description = req.description,
            imageUrl    = req.imageUrl,
            prepTimeMin = req.prepTimeMin,
            servings    = req.servings,
            isPublic    = req.isPublic
        )
        recipe.tags.addAll(req.tags)
        
        if (req.ingredients.isEmpty()) {
            recipe.kcalPerServing = req.kcalPerServing ?: BigDecimal.ZERO
            recipe.proteinG       = req.proteinG       ?: BigDecimal.ZERO
            recipe.fatG           = req.fatG           ?: BigDecimal.ZERO
            recipe.carbsG         = req.carbsG         ?: BigDecimal.ZERO
        } else {
            var totalKcal  = BigDecimal.ZERO
            var totalProt  = BigDecimal.ZERO
            var totalFat   = BigDecimal.ZERO
            var totalCarbs = BigDecimal.ZERO
            req.ingredients.forEachIndexed { i, ing ->
                val product = foodRepo.findById(ing.productId).orElseThrow()
                val factor  = ing.quantityG.divide(BigDecimal(100))
                totalKcal  += product.kcalPer100g * factor
                totalProt  += product.proteinG    * factor
                totalFat   += product.fatG        * factor
                totalCarbs += product.carbsG      * factor
                recipe.ingredients.add(RecipeIngredient(
                    recipe    = recipe,
                    product   = product,
                    quantityG = ing.quantityG,
                    unit      = ing.unit,
                    sortOrder = i
                ))
            }
            val s = BigDecimal(req.servings)
            recipe.kcalPerServing = totalKcal  / s
            recipe.proteinG       = totalProt  / s
            recipe.fatG           = totalFat   / s
            recipe.carbsG         = totalCarbs / s
        }
        
        val saved = recipeRepo.save(recipe)
        val favorites = favoriteRepo.findAllByUserId(user.id).map { f -> f.recipe.id }.toSet()
        return saved.toResponse(user.id, favorites)
    }

    @Transactional
    fun update(email: String, recipeId: Long, req: RecipeRequest): RecipeResponse {
        val user   = userRepo.findByEmail(email).orElseThrow()
        val recipe = recipeRepo.findById(recipeId).orElseThrow()
        require(recipe.author.id == user.id) { "Brak uprawnień — nie jesteś autorem przepisu" }
        recipe.title       = req.title
        recipe.description = req.description
        recipe.imageUrl    = req.imageUrl
        recipe.prepTimeMin = req.prepTimeMin
        recipe.servings    = req.servings
        recipe.isPublic    = req.isPublic
        recipe.tags.clear()
        recipe.tags.addAll(req.tags)
        recipe.ingredients.clear()

        if (req.ingredients.isEmpty()) {
            recipe.kcalPerServing = req.kcalPerServing ?: BigDecimal.ZERO
            recipe.proteinG       = req.proteinG       ?: BigDecimal.ZERO
            recipe.fatG           = req.fatG           ?: BigDecimal.ZERO
            recipe.carbsG         = req.carbsG         ?: BigDecimal.ZERO
        } else {
            var totalKcal  = BigDecimal.ZERO
            var totalProt  = BigDecimal.ZERO
            var totalFat   = BigDecimal.ZERO
            var totalCarbs = BigDecimal.ZERO
            req.ingredients.forEachIndexed { i, ing ->
                val product = foodRepo.findById(ing.productId).orElseThrow()
                val factor  = ing.quantityG.divide(BigDecimal(100))
                totalKcal  += product.kcalPer100g * factor
                totalProt  += product.proteinG    * factor
                totalFat   += product.fatG        * factor
                totalCarbs += product.carbsG      * factor
                recipe.ingredients.add(RecipeIngredient(
                    recipe    = recipe,
                    product   = product,
                    quantityG = ing.quantityG,
                    unit      = ing.unit,
                    sortOrder = i
                ))
            }
            val s = BigDecimal(req.servings)
            recipe.kcalPerServing = totalKcal  / s
            recipe.proteinG       = totalProt  / s
            recipe.fatG           = totalFat   / s
            recipe.carbsG         = totalCarbs / s
        }
        
        val saved = recipeRepo.save(recipe)
        val favorites = favoriteRepo.findAllByUserId(user.id).map { f -> f.recipe.id }.toSet()
        return saved.toResponse(user.id, favorites)
    }

    @Transactional
    fun delete(email: String, recipeId: Long) {
        val user   = userRepo.findByEmail(email).orElseThrow()
        val recipe = recipeRepo.findById(recipeId).orElseThrow()
        require(recipe.author.id == user.id) { "Brak uprawnień — nie jesteś autorem przepisu" }
        recipeRepo.delete(recipe)
    }

    private fun Recipe.toResponse(currentUserId: Long? = null, favorites: Set<Long> = emptySet()) = RecipeResponse(
        id             = id,
        authorId       = author.id,
        title          = title,
        description    = description,
        imageUrl       = imageUrl,
        prepTimeMin    = prepTimeMin,
        servings       = servings,
        kcalPerServing = kcalPerServing,
        proteinG       = proteinG,
        fatG           = fatG,
        carbsG         = carbsG,
        tags           = tags,
        isPublic       = isPublic,
        isFavorite     = favorites.contains(id),
        isOwner        = currentUserId != null && author.id == currentUserId
    )
}