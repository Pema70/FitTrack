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
    private val workoutRepo: WorkoutActivityRepository
) {
    @Transactional
    fun addEntry(email: String, req: DiaryEntryRequest): DiaryEntryResponse {
        val user = userRepo.findByEmail(email).orElseThrow()
        val product = req.productId?.let { foodRepo.findById(it).orElseThrow() }
        val factor = req.quantityG.divide(BigDecimal(100))
        val entry = diaryRepo.save(DiaryEntry(
            user       = user,
            entryDate  = req.entryDate,
            mealType   = req.mealType,
            product    = product,
            recipeId   = req.recipeId,
            customName = req.customName,
            quantityG  = req.quantityG,
            kcal       = product?.kcalPer100g?.multiply(factor) ?: BigDecimal.ZERO,
            proteinG   = product?.proteinG?.multiply(factor) ?: BigDecimal.ZERO,
            fatG       = product?.fatG?.multiply(factor) ?: BigDecimal.ZERO,
            carbsG     = product?.carbsG?.multiply(factor) ?: BigDecimal.ZERO,
            photoPath  = req.photoPath,
            note       = req.note,
            synced     = req.synced
        ))
        return entry.toResponse()
    }

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
        entry.kcal    = entry.product?.kcalPer100g?.multiply(factor) ?: BigDecimal.ZERO
        entry.proteinG = entry.product?.proteinG?.multiply(factor) ?: BigDecimal.ZERO
        entry.fatG     = entry.product?.fatG?.multiply(factor) ?: BigDecimal.ZERO
        entry.carbsG   = entry.product?.carbsG?.multiply(factor) ?: BigDecimal.ZERO
        return diaryRepo.save(entry).toResponse()
    }

    @Transactional
    fun deleteEntry(email: String, entryId: Long) {
        val user  = userRepo.findByEmail(email).orElseThrow()
        val entry = diaryRepo.findById(entryId).orElseThrow()
        require(entry.user.id == user.id) { "Brak uprawnień" }
        diaryRepo.delete(entry)
    }

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
        productName = product?.name ?: customName,
        quantityG   = quantityG,
        kcal        = kcal,
        proteinG    = proteinG,
        fatG        = fatG,
        carbsG      = carbsG,
        photoPath   = photoPath,
        note        = note
    )
}