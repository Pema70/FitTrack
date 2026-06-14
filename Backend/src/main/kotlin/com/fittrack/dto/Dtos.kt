package com.fittrack.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate

// =================== AUTH ===================

data class RegisterRequest(
    @field:Email(message = "Nieprawidłowy format email")
    @field:NotBlank(message = "Email nie może być pusty")
    val email: String,

    @field:Size(min = 8, max = 100, message = "Hasło musi mieć minimum 8 znaków")
    @field:Pattern(regexp = ".*\\d.*", message = "Hasło musi zawierać co najmniej jedną cyfrę")
    @field:NotBlank(message = "Hasło nie może być puste")
    val password: String
)

data class LoginRequest(
    @field:Email(message = "Nieprawidłowy format email")
    @field:NotBlank(message = "Email nie może być pusty")
    val email: String,

    @field:NotBlank(message = "Hasło nie może być puste")
    val password: String
)

data class RefreshRequest(
    @field:NotBlank(message = "Refresh token nie może być pusty")
    val refreshToken: String
)

data class ChangePasswordRequest(
    @field:NotBlank(message = "Stare hasło nie może być puste")
    val oldPassword: String,

    @field:Size(min = 8, max = 100, message = "Hasło musi mieć minimum 8 znaków")
    @field:Pattern(regexp = ".*\\d.*", message = "Hasło musi zawierać co najmniej jedną cyfrę")
    @field:NotBlank(message = "Nowe hasło nie może być puste")
    val newPassword: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

// =================== PROFILE ===================

data class ProfileUpdateRequest(
    @field:Size(min = 2, max = 50, message = "Nazwa musi mieć od 2 do 50 znaków")
    val displayName: String? = null,

    @field:Pattern(
        regexp = "^(MALE|FEMALE|OTHER)$",
        message = "Płeć musi być jedną z wartości: MALE, FEMALE, OTHER"
    )
    val gender: String? = null,

    @field:Past(message = "Data urodzenia musi być w przeszłości")
    val birthDate: LocalDate? = null,

    @field:DecimalMin(value = "20.0", message = "Waga minimalna to 20 kg")
    @field:DecimalMax(value = "500.0", message = "Waga maksymalna to 500 kg")
    @field:Digits(integer = 3, fraction = 2, message = "Nieprawidłowy format wagi")
    val weightKg: BigDecimal? = null,

    @field:DecimalMin(value = "50.0", message = "Wzrost minimalny to 50 cm")
    @field:DecimalMax(value = "300.0", message = "Wzrost maksymalny to 300 cm")
    @field:Digits(integer = 3, fraction = 2, message = "Nieprawidłowy format wzrostu")
    val heightCm: BigDecimal? = null,

    @field:Pattern(
        regexp = "^(SEDENTARY|LIGHTLY_ACTIVE|MODERATELY_ACTIVE|VERY_ACTIVE|EXTRA_ACTIVE)$",
        message = "Nieprawidłowy poziom aktywności"
    )
    val activityLevel: String? = null,

    @field:Pattern(
        regexp = "^(LOSE_WEIGHT|MAINTAIN|GAIN_MUSCLE)$",
        message = "Cel musi być jednym z: LOSE_WEIGHT, MAINTAIN, GAIN_MUSCLE"
    )
    val goal: String? = null,

    @field:Size(max = 500, message = "URL avatara może mieć max 500 znaków")
    @field:Pattern(
        regexp = "^(https?://.*)?$",
        message = "URL avatara musi zaczynać się od http:// lub https://"
    )
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
    @field:NotNull(message = "Data wpisu jest wymagana")
    @field:PastOrPresent(message = "Data wpisu nie może być w przyszłości")
    val entryDate: LocalDate,

    @field:NotBlank(message = "Typ posiłku nie może być pusty")
    @field:Pattern(
        regexp = "^(BREAKFAST|SECOND_BREAKFAST|LUNCH|AFTERNOON_SNACK|DINNER|SNACK)$",
        message = "Nieprawidłowy typ posiłku"
    )
    val mealType: String,

    @field:Positive(message = "ID produktu musi być dodatnie")
    val productId: Long? = null,

    @field:Positive(message = "ID przepisu musi być dodatnie")
    val recipeId: Long? = null,

    @field:Size(max = 100, message = "Nazwa własna może mieć max 100 znaków")
    val customName: String? = null,

    @field:NotNull(message = "Ilość jest wymagana")
    @field:DecimalMin(value = "0.1", message = "Ilość musi być większa niż 0.1g")
    @field:DecimalMax(value = "10000.0", message = "Ilość nie może przekraczać 10000g")
    val quantityG: BigDecimal,

    @field:Size(max = 500, message = "Ścieżka zdjęcia może mieć max 500 znaków")
    val photoPath: String? = null,

    @field:Size(max = 500, message = "Notatka może mieć max 500 znaków")
    val note: String? = null,

    val synced: Boolean = true
)

data class DiaryUpdateRequest(
    @field:NotNull(message = "Ilość jest wymagana")
    @field:DecimalMin(value = "0.1", message = "Ilość musi być większa niż 0.1g")
    @field:DecimalMax(value = "10000.0", message = "Ilość nie może przekraczać 10000g")
    val quantityG: BigDecimal
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
    @field:NotNull(message = "ID produktu jest wymagane")
    @field:Positive(message = "ID produktu musi być dodatnie")
    val productId: Long,

    @field:NotNull(message = "Ilość jest wymagana")
    @field:DecimalMin(value = "0.1", message = "Ilość musi być większa niż 0.1g")
    @field:DecimalMax(value = "10000.0", message = "Ilość nie może przekraczać 10000g")
    val quantityG: BigDecimal,

    @field:Size(max = 10, message = "Jednostka może mieć max 10 znaków")
    val unit: String? = "g"
)

data class RecipeRequest(
    @field:NotBlank(message = "Tytuł przepisu nie może być pusty")
    @field:Size(min = 3, max = 150, message = "Tytuł musi mieć od 3 do 150 znaków")
    val title: String,

    @field:Size(max = 2000, message = "Opis może mieć max 2000 znaków")
    val description: String? = null,

    @field:Size(max = 500, message = "URL obrazka może mieć max 500 znaków")
    val imageUrl: String? = null,

    @field:NotNull(message = "Czas przygotowania jest wymagany")
    @field:Min(value = 1, message = "Czas przygotowania musi wynosić co najmniej 1 minutę")
    @field:Max(value = 1440, message = "Czas przygotowania nie może przekraczać 1440 minut (24h)")
    val prepTimeMin: Int,

    @field:NotNull(message = "Liczba porcji jest wymagana")
    @field:Min(value = 1, message = "Liczba porcji musi wynosić co najmniej 1")
    @field:Max(value = 100, message = "Liczba porcji nie może przekraczać 100")
    val servings: Int,

    val isPublic: Boolean = true,

    @field:Size(max = 10, message = "Można dodać maksymalnie 10 tagów")
    val tags: Set<
        @NotBlank(message = "Tag nie może być pusty")
        @Size(max = 30, message = "Tag może mieć max 30 znaków")
        String
    > = emptySet(),

    @field:Size(max = 50, message = "Przepis może mieć maksymalnie 50 składników")
    @field:Valid
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
    @field:NotNull(message = "Data aktywności jest wymagana")
    @field:PastOrPresent(message = "Data aktywności nie może być w przyszłości")
    val activityDate: LocalDate,

    @field:NotBlank(message = "Typ aktywności nie może być pusty")
    @field:Size(max = 100, message = "Typ aktywności może mieć max 100 znaków")
    val activityType: String,

    @field:Min(value = 1, message = "Czas trwania musi wynosić co najmniej 1 minutę")
    @field:Max(value = 1440, message = "Czas trwania nie może przekraczać 1440 minut (24h)")
    val durationMin: Int,

    @field:Min(value = 0, message = "Spalone kalorie nie mogą być ujemne")
    @field:Max(value = 10000, message = "Spalone kalorie nie mogą przekraczać 10000")
    val kcalBurned: Int? = null,

    @field:DecimalMin(value = "0.0", message = "Dystans nie może być ujemny")
    @field:DecimalMax(value = "1000.0", message = "Dystans nie może przekraczać 1000 km")
    val distanceKm: BigDecimal? = null,

    @field:Min(value = 30, message = "Tętno minimalne to 30 bpm")
    @field:Max(value = 300, message = "Tętno maksymalne to 300 bpm")
    val avgHeartRate: Int? = null,

    @field:Size(max = 500, message = "Notatka może mieć max 500 znaków")
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

// =================== PRODUCTS ===================

data class ProductSearchResponse(
    val id: Long,
    val name: String,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val carbsPer100g: Double
)