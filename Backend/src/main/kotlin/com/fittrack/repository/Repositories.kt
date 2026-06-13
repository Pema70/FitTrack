package com.fittrack.repository

import com.fittrack.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean
}

@Repository
interface UserProfileRepository : JpaRepository<UserProfile, Long> {
    fun findByUserId(userId: Long): Optional<UserProfile>
}

@Repository
interface FoodProductRepository : JpaRepository<FoodProduct, Long>

@Repository
interface DiaryEntryRepository : JpaRepository<DiaryEntry, Long> {
    fun findAllByUserIdAndEntryDate(userId: Long, entryDate: LocalDate): List<DiaryEntry>
}

@Repository
interface RecipeRepository : JpaRepository<Recipe, Long> {
    @Query("""
        SELECT DISTINCT r FROM Recipe r LEFT JOIN r.tags t
        WHERE r.isPublic = true
          AND (:q = '' OR LOWER(r.title) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY r.createdAt DESC
    """)
    fun searchPublic(@Param("q") q: String): List<Recipe>

    @Query("""
        SELECT DISTINCT r FROM Recipe r JOIN r.tags t
        WHERE r.isPublic = true AND LOWER(t) = LOWER(:tag)
        ORDER BY r.createdAt DESC
    """)
    fun findPublicByTag(@Param("tag") tag: String): List<Recipe>

    fun findAllByAuthorId(authorId: Long): List<Recipe>
}

@Repository
interface FavoriteRecipeRepository : JpaRepository<FavoriteRecipe, Long> {
    fun findAllByUserId(userId: Long): List<FavoriteRecipe>
    fun findByUserIdAndRecipeId(userId: Long, recipeId: Long): Optional<FavoriteRecipe>
    fun existsByUserIdAndRecipeId(userId: Long, recipeId: Long): Boolean
}

@Repository
interface WorkoutActivityRepository : JpaRepository<WorkoutActivity, Long> {
    fun findAllByUserIdAndActivityDate(userId: Long, activityDate: LocalDate): List<WorkoutActivity>
    fun findAllByUserId(userId: Long): List<WorkoutActivity>
}