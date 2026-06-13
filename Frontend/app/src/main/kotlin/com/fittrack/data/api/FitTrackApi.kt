package com.fittrack.data.api

import com.fittrack.data.model.*
import retrofit2.http.*

interface FitTrackApi {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse

    @PATCH("auth/password")
    suspend fun changePassword(@Body req: ChangePasswordRequest)

    // Profile
    @GET("profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("profile")
    suspend fun updateProfile(@Body req: ProfileUpdateRequest): ProfileResponse

    // Diary
    @GET("diary")
    suspend fun getDiary(@Query("date") date: String): List<DiaryEntryResponse>

    @GET("diary/summary")
    suspend fun getDailySummary(@Query("date") date: String): DailySummaryResponse

    @POST("diary")
    suspend fun addDiaryEntry(@Body req: DiaryEntryRequest): DiaryEntryResponse

    @PATCH("diary/{id}")
    suspend fun updateDiaryEntry(@Path("id") id: Long, @Body req: DiaryUpdateRequest): DiaryEntryResponse

    @DELETE("diary/{id}")
    suspend fun deleteDiaryEntry(@Path("id") id: Long)

    // Recipes
    @GET("recipes")
    suspend fun getRecipes(
        @Query("q")   query: String = "",
        @Query("tag") tag: String?  = null
    ): List<RecipeResponse>

    @GET("recipes/mine")
    suspend fun getMyRecipes(): List<RecipeResponse>

    @GET("recipes/favorites")
    suspend fun getFavoriteRecipes(): List<RecipeResponse>

    @POST("recipes/{id}/favorite")
    suspend fun addFavorite(@Path("id") id: Long)

    @DELETE("recipes/{id}/favorite")
    suspend fun removeFavorite(@Path("id") id: Long)

    @PUT("recipes/{id}")
    suspend fun updateRecipe(@Path("id") id: Long, @Body req: RecipeRequest): RecipeResponse

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: Long)

    // Workouts
    @GET("workouts")
    suspend fun getWorkouts(@Query("date") date: String): List<WorkoutResponse>

    @POST("workouts")
    suspend fun logWorkout(@Body req: WorkoutRequest): WorkoutResponse

    @PUT("workouts/{id}")
    suspend fun updateWorkout(@Path("id") id: Long, @Body req: WorkoutRequest): WorkoutResponse

    @DELETE("workouts/{id}")
    suspend fun deleteWorkout(@Path("id") id: Long)
}
