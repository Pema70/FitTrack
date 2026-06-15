package com.fittrack.service

import com.fittrack.dto.*
import com.fittrack.entity.DiaryEntry
import com.fittrack.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class DiaryService(
    private val userRepo: UserRepository,
    private val profileRepo: UserProfileRepository,
    private val diaryRepo: DiaryEntryRepository,
    private val foodRepo: FoodProductRepository,
    private val recipeRepo: RecipeRepository,
    private val workoutRepo: WorkoutActivityRepository
) {
    @Transactional
    fun addEntry(email: String, req: DiaryEntryRequest): DiaryEntryResponse {
        val user = userRepo.findByEmail(email).orElseThrow()
        val product = req.productId?.let { foodRepo.findById(it).orElseThrow() }
        val recipe = req.recipeId?.let { recipeRepo.findById(it).orElseThrow() }
        
        val factor = req.quantityG.divide(BigDecimal(100))
        
        val kcal = when {
            product != null -> product.kcalPer100g * factor
            recipe != null  -> recipe.kcalPerServing * factor
            else -> BigDecimal.ZERO
        }
        val prot = when {
            product != null -> product.proteinG * factor
            recipe != null  -> recipe.proteinG * factor
            else -> BigDecimal.ZERO
        }
        val fat = when {
            product != null -> product.fatG * factor
            recipe != null  -> recipe.fatG * factor
            else -> BigDecimal.ZERO
        }
        val carbs = when {
            product != null -> product.carbsG * factor
            recipe != null  -> recipe.carbsG * factor
            else -> BigDecimal.ZERO
        }

        val entry = diaryRepo.save(DiaryEntry(
            user       = user,
            entryDate  = req.entryDate,
            mealType   = req.mealType,
            product    = product,
            recipe     = recipe,
            customName = req.customName,
            quantityG  = req.quantityG,
            kcal       = kcal,
            proteinG   = prot,
            fatG       = fat,
            carbsG     = carbs,
            photoPath  = req.photoPath,
            note       = req.note,
            synced     = req.synced
        ))
        return entry.toResponse()
    }

    @Transactional(readOnly = true)  // ← DODANE
    fun getEntriesForDate(email: String, date: LocalDate): List<DiaryEntryResponse> {
        val user = userRepo.findByEmail(email).orElseThrow()
        return diaryRepo.findAllByUserIdAndEntryDate(user.id, date).map { it.toResponse() }
    }

    @Transactional
    fun updateEntry(email: String, entryId: Long, req: DiaryUpdateRequest): DiaryEntryResponse {
        val user  = userRepo.findByEmail(email).orElseThrow()
        val entry = diaryRepo.findById(entryId).orElseThrow()
        require(entry.user.id == user.id) { "Brak uprawnień" }
        
        val factor = req.quantityG.divide(BigDecimal(100))
        entry.quantityG = req.quantityG
        
        entry.kcal = when {
            entry.product != null -> entry.product!!.kcalPer100g * factor
            entry.recipe != null  -> entry.recipe!!.kcalPerServing * factor
            else -> BigDecimal.ZERO
        }
        entry.proteinG = when {
            entry.product != null -> entry.product!!.proteinG * factor
            entry.recipe != null  -> entry.recipe!!.proteinG * factor
            else -> BigDecimal.ZERO
        }
        entry.fatG = when {
            entry.product != null -> entry.product!!.fatG * factor
            entry.recipe != null  -> entry.recipe!!.fatG * factor
            else -> BigDecimal.ZERO
        }
        entry.carbsG = when {
            entry.product != null -> entry.product!!.carbsG * factor
            entry.recipe != null  -> entry.recipe!!.carbsG * factor
            else -> BigDecimal.ZERO
        }

        return diaryRepo.save(entry).toResponse()
    }

    @Transactional
    fun deleteEntry(email: String, entryId: Long) {
        val user  = userRepo.findByEmail(email).orElseThrow()
        val entry = diaryRepo.findById(entryId).orElseThrow()
        require(entry.user.id == user.id) { "Brak uprawnień" }
        diaryRepo.delete(entry)
    }

    @Transactional(readOnly = true)  // ← DODANE
    fun getDailySummary(email: String, date: LocalDate): DailySummaryResponse {
        val user     = userRepo.findByEmail(email).orElseThrow()
        val profile  = profileRepo.findByUserId(user.id).orElseThrow()
        val entries  = diaryRepo.findAllByUserIdAndEntryDate(user.id, date)
        val workouts = workoutRepo.findAllByUserIdAndActivityDate(user.id, date)
        val consumed = entries.fold(BigDecimal.ZERO) { acc, e -> acc + e.kcal }
        val burned   = workouts.sumOf { it.kcalBurned }
        val goal     = profile.dailyKcalGoal ?: 2000
        return DailySummaryResponse(
            date          = date,
            kcalGoal      = goal,
            kcalConsumed  = consumed,
            kcalBurned    = burned,
            kcalRemaining = BigDecimal(goal) - consumed + BigDecimal(burned),
            proteinG      = entries.fold(BigDecimal.ZERO) { a, e -> a + e.proteinG },
            fatG          = entries.fold(BigDecimal.ZERO) { a, e -> a + e.fatG },
            carbsG        = entries.fold(BigDecimal.ZERO) { a, e -> a + e.carbsG }
        )
    }

    private fun DiaryEntry.toResponse() = DiaryEntryResponse(
        id          = id,
        entryDate   = entryDate,
        mealType    = mealType,
        productName = product?.name ?: recipe?.title ?: customName,
        quantityG   = quantityG,
        kcal        = kcal,
        proteinG    = proteinG,
        fatG        = fatG,
        carbsG      = carbsG,
        photoPath   = photoPath,
        note        = note
    )
}