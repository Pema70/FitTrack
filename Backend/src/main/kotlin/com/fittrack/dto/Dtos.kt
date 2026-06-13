package com.fittrack.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate

// =================== AUTH ===================

data class RegisterRequest(
    @field:Email @field:NotBlank val email: String,
    @field:Size(min = 8, max = 100, message = "Hasło musi mieć minimum 8 znaków")
    @field:Pattern(regexp = ".*\\d.*", message = "Hasło musi zawierać co najmniej jedną cyfrę")
    val password: String
)

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class RefreshRequest(
    @field:NotBlank val refreshToken: String
)

data class ChangePasswordRequest(
    @field:NotBlank val oldPassword: String,
    @field:Size(min = 8, max = 100, message = "Hasło musi mieć minimum 8 znaków")
    @field:Pattern(regexp = ".*\\d.*", message = "Hasło musi zawierać co najmniej jedną cyfrę")
    val newPassword: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

// =================== PROFILE ===================

data class ProfileUpdateRequest(
    val displayName: String? = null,
    val gender: String? = null,
    val birthDate: LocalDate? = null,
    val weightKg: BigDecimal? = null,
    val heightCm: BigDecimal? = null,
    val activityLevel: String? = null,
    val goal: String? = null,
    val avatarUrl: String? = null
)

data class ProfileResponse(
    val displayName: String?,
    val gender: String?,
    val birthDate: LocalDate?,
    val weightKg: BigDecimal?,
    val heightCm: BigDecimal?,
    val activityLevel: String?,
    val goal: String?,
    val dailyKcalGoal: Int?,
    val avatarUrl: String?
)

// =================== DIARY ===================

data class DiaryEntryRequest(
    @field:NotNull val entryDate: LocalDate,
    @field:NotBlank val mealType: String,
    val productId: Long? = null,
    val recipeId: Long? = null,
    val customName: String? = null,
    @field:NotNull @field:DecimalMin("0.1") val quantityG: BigDecimal,
    val photoPath: String? = null,
    val note: String? = null,
    val synced: Boolean = true
)

data class DiaryUpdateRequest(
    @field:NotNull @field:DecimalMin("0.1") val quantityG: BigDecimal
)

data class DiaryEntryResponse(
    val id: Long,
    val entryDate: LocalDate,
    val mealType: String,
    val productName: String?,
    val quantityG: BigDecimal,
    val kcal: BigDecimal,
    val proteinG: BigDecimal,
    val fatG: BigDecimal,
    val carbsG: BigDecimal,
    val photoPath: String?,
    val note: String?
)

data class DailySummaryResponse(
    val date: LocalDate,
    val kcalGoal: Int,
    val kcalConsumed: BigDecimal,
    val kcalBurned: Int,
    val kcalRemaining: BigDecimal,
    val proteinG: BigDecimal,
    val fatG: BigDecimal,
    val carbsG: BigDecimal
)

// =================== RECIPES ===================

data class RecipeIngredientRequest(
    @field:NotNull val productId: Long,
    @field:NotNull val quantityG: BigDecimal,
    val unit: String? = "g"
)

data class RecipeRequest(
    @field:NotBlank val title: String,
    val description: String? = null,
    val imageUrl: String? = null,
    @field:NotNull val prepTimeMin: Int,
    @field:NotNull val servings: Int,
    val isPublic: Boolean = true,
    val tags: Set<String> = emptySet(),
    val ingredients: List<RecipeIngredientRequest> = emptyList()
)

data class RecipeResponse(
    val id: Long,
    val authorId: Long,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val prepTimeMin: Int,
    val servings: Int,
    val kcalPerServing: BigDecimal,
    val proteinG: BigDecimal,
    val fatG: BigDecimal,
    val carbsG: BigDecimal,
    val tags: Set<String>,
    val isPublic: Boolean,
    val isFavorite: Boolean = false
)

// =================== WORKOUT ===================

data class WorkoutRequest(
    @field:NotNull val activityDate: LocalDate,
    @field:NotBlank val activityType: String,
    @field:Min(1) val durationMin: Int,
    val kcalBurned: Int? = null,
    val distanceKm: BigDecimal? = null,
    val avgHeartRate: Int? = null,
    val notes: String? = null
)

data class WorkoutResponse(
    val id: Long,
    val activityDate: LocalDate,
    val activityType: String,
    val durationMin: Int,
    val kcalBurned: Int,
    val distanceKm: BigDecimal?,
    val notes: String?
)
