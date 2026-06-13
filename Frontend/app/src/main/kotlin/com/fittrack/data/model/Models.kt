package com.fittrack.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// ─── Auth ─────────────────────────────────────────────────────
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String)
data class AuthResponse(val accessToken: String?, val userId: Long?, val email: String?)
data class ChangePasswordRequest(val oldPassword: String, val newPassword: String)

// ─── Profile ──────────────────────────────────────────────────
data class ProfileResponse(
    val userId: Long?,
    val displayName: String?,
    val email: String?,
    val weightKg: Double?,
    val heightCm: Double?,
    val birthDate: String?,
    val gender: String?,
    val activityLevel: String?,
    val goal: String?,
    val dailyKcalGoal: Int?
)

data class ProfileUpdateRequest(
    val displayName: String?,
    val weightKg: Double?,
    val heightCm: Double?,
    val birthDate: String?,
    val gender: String?,
    val activityLevel: String?,
    val goal: String?
)

// ─── Diary ────────────────────────────────────────────────────
@Parcelize
data class DiaryEntryResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val quantityG: Double,
    val kcal: Double,
    val proteinG: Double,
    val fatG: Double,
    val carbsG: Double,
    val mealType: String,
    val date: String
) : Parcelable

data class DailySummaryResponse(
    val date: String,
    val kcalConsumed: Double,
    val kcalBurned: Double,
    val kcalGoal: Int,
    val kcalRemaining: Double,
    val proteinG: Double,
    val fatG: Double,
    val carbsG: Double
)

data class DiaryUpdateRequest(val quantityG: Double)

// ─── Recipes ──────────────────────────────────────────────────
@Parcelize
data class RecipeResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val kcalPerServing: Double,
    val proteinG: Double,
    val fatG: Double,
    val carbsG: Double,
    val prepTimeMin: Int?,
    val servings: Int,
    val imageUrl: String?,
    val tags: List<String>,
    val isFavorite: Boolean,
    val isOwner: Boolean
) : Parcelable

data class RecipeRequest(
    val title: String,
    val description: String?,
    val kcalPerServing: Double,
    val proteinG: Double,
    val fatG: Double,
    val carbsG: Double,
    val prepTimeMin: Int?,
    val servings: Int,
    val imageUrl: String?,
    val tags: List<String>
)

// ─── Workouts ─────────────────────────────────────────────────
data class WorkoutResponse(
    val id: Long,
    val activityDate: String,
    val activityType: String,
    val durationMin: Int,
    val kcalBurned: Int,
    val notes: String?
)

data class WorkoutRequest(
    val activityDate: String,
    val activityType: String,
    val durationMin: Int,
    val kcalBurned: Int?,   // null = oblicz automatycznie
    val notes: String?
)
// --- Food / Diary entry ---
data class DiaryEntryRequest(
    val productId: Long,
    val quantityG: Double,
    val mealType: String,
    val entryDate: String
)

// --- Products (wyszukiwarka) ---
data class ProductResponse(
    val id: Long,
    val name: String,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double,
    val barcode: String?
)