package com.fittrack.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "users", uniqueConstraints = [UniqueConstraint(columnNames = ["email"])])
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false, unique = true) var email: String = "",
    @Column(nullable = false) var password: String = "",
    @Column(name = "created_at", nullable = false) var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "user_profiles")
class UserProfile(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", unique = true, nullable = false) var user: User,
    @Column(name = "display_name") var displayName: String? = null,
    var gender: String? = null,
    @Column(name = "birth_date") var birthDate: LocalDate? = null,
    @Column(name = "weight_kg", precision = 5, scale = 2) var weightKg: BigDecimal? = null,
    @Column(name = "height_cm", precision = 5, scale = 2) var heightCm: BigDecimal? = null,
    @Column(name = "activity_level") var activityLevel: String? = null,
    var goal: String? = null,
    @Column(name = "daily_kcal_goal") var dailyKcalGoal: Int? = null,
    @Column(name = "avatar_url") var avatarUrl: String? = null
)

@Entity
@Table(name = "food_products")
class FoodProduct(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    @Column(nullable = false) var name: String = "",
    @Column(name = "kcal_per_100g", nullable = false, precision = 7, scale = 2) var kcalPer100g: BigDecimal = BigDecimal.ZERO,
    @Column(name = "protein_g", nullable = false, precision = 6, scale = 2) var proteinG: BigDecimal = BigDecimal.ZERO,
    @Column(name = "fat_g", nullable = false, precision = 6, scale = 2) var fatG: BigDecimal = BigDecimal.ZERO,
    @Column(name = "carbs_g", nullable = false, precision = 6, scale = 2) var carbsG: BigDecimal = BigDecimal.ZERO,
    @Column(name = "brand") var brand: String? = null
)

@Entity
@Table(name = "diary_entries")
class DiaryEntry(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) var user: User,
    @Column(name = "entry_date", nullable = false) var entryDate: LocalDate,
    @Column(name = "meal_type", nullable = false) var mealType: String,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") var product: FoodProduct? = null,
    @Column(name = "recipe_id") var recipeId: Long? = null,
    @Column(name = "custom_name") var customName: String? = null,
    @Column(name = "quantity_g", nullable = false, precision = 8, scale = 2) var quantityG: BigDecimal,
    @Column(nullable = false, precision = 8, scale = 2) var kcal: BigDecimal = BigDecimal.ZERO,
    @Column(name = "protein_g", precision = 6, scale = 2) var proteinG: BigDecimal = BigDecimal.ZERO,
    @Column(name = "fat_g", precision = 6, scale = 2) var fatG: BigDecimal = BigDecimal.ZERO,
    @Column(name = "carbs_g", precision = 6, scale = 2) var carbsG: BigDecimal = BigDecimal.ZERO,
    @Column(name = "photo_path") var photoPath: String? = null,
    var note: String? = null,
    @Column(nullable = false) var synced: Boolean = true,
    @Column(name = "created_at", nullable = false) var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "recipes")
class Recipe(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id", nullable = false) var author: User,
    @Column(nullable = false) var title: String = "",
    @Column(length = 2000) var description: String? = null,
    @Column(name = "image_url") var imageUrl: String? = null,
    @Column(name = "prep_time_min", nullable = false) var prepTimeMin: Int = 0,
    @Column(nullable = false) var servings: Int = 1,
    @Column(name = "kcal_per_serving", precision = 7, scale = 2) var kcalPerServing: BigDecimal = BigDecimal.ZERO,
    @Column(name = "protein_g", precision = 6, scale = 2) var proteinG: BigDecimal = BigDecimal.ZERO,
    @Column(name = "fat_g", precision = 6, scale = 2) var fatG: BigDecimal = BigDecimal.ZERO,
    @Column(name = "carbs_g", precision = 6, scale = 2) var carbsG: BigDecimal = BigDecimal.ZERO,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_tags", joinColumns = [JoinColumn(name = "recipe_id")])
    @Column(name = "tag") var tags: MutableSet<String> = mutableSetOf(),
    @OneToMany(mappedBy = "recipe", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var ingredients: MutableList<RecipeIngredient> = mutableListOf(),
    @Column(name = "is_public", nullable = false) var isPublic: Boolean = true,
    @Column(name = "created_at", nullable = false) var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "recipe_ingredients")
class RecipeIngredient(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recipe_id", nullable = false) var recipe: Recipe,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false) var product: FoodProduct,
    @Column(name = "quantity_g", nullable = false, precision = 8, scale = 2) var quantityG: BigDecimal,
    var unit: String? = "g",
    @Column(name = "sort_order", nullable = false) var sortOrder: Int = 0
)

// NOWA ENCJA — ulubione przepisy
@Entity
@Table(
    name = "favorite_recipes",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "recipe_id"])]
)
class FavoriteRecipe(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) var user: User,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recipe_id", nullable = false) var recipe: Recipe,
    @Column(name = "created_at", nullable = false) var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "workouts")
class WorkoutActivity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) var user: User,
    @Column(name = "activity_date", nullable = false) var activityDate: LocalDate,
    @Column(name = "activity_type", nullable = false) var activityType: String,
    @Column(name = "duration_min", nullable = false) var durationMin: Int,
    @Column(name = "kcal_burned", nullable = false) var kcalBurned: Int,
    @Column(name = "distance_km", precision = 6, scale = 2) var distanceKm: BigDecimal? = null,
    @Column(name = "avg_heart_rate") var avgHeartRate: Int? = null,
    var notes: String? = null,
    @Column(name = "created_at", nullable = false) var createdAt: Instant = Instant.now()
)