package com.fittrack.service

import com.fittrack.dto.*
import com.fittrack.entity.*
import com.fittrack.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class WorkoutService(
    private val userRepo: UserRepository,
    private val workoutRepo: WorkoutActivityRepository
) {
    private val metMultipliers = mapOf(
        "RUNNING"  to 9.8,
        "CYCLING"  to 7.5,
        "SWIMMING" to 8.0,
        "GYM"      to 6.0,
        "WALKING"  to 3.5,
        "YOGA"     to 3.0,
        "OTHER"    to 5.0
    )

    private fun calcKcal(activityType: String, durationMin: Int): Int {
        val met = metMultipliers[activityType.uppercase()] ?: 5.0
        return (met * 75.0 * durationMin / 60).toInt()
    }

    @Transactional
    fun log(email: String, req: WorkoutRequest): WorkoutResponse {
        val user = userRepo.findByEmail(email).orElseThrow()
        val kcal = req.kcalBurned ?: calcKcal(req.activityType, req.durationMin)
        val w = workoutRepo.save(
            WorkoutActivity(
                user         = user,
                activityDate = req.activityDate,
                activityType = req.activityType,
                durationMin  = req.durationMin,
                kcalBurned   = kcal,
                distanceKm   = req.distanceKm,
                avgHeartRate = req.avgHeartRate,
                notes        = req.notes
            )
        )
        return w.toResponse()
    }

    fun getForDate(email: String, date: LocalDate): List<WorkoutResponse> {
        val user = userRepo.findByEmail(email).orElseThrow()
        return workoutRepo.findAllByUserIdAndActivityDate(user.id, date).map { it.toResponse() }
    }

    @Transactional
    fun update(email: String, workoutId: Long, req: WorkoutRequest): WorkoutResponse {
        val user = userRepo.findByEmail(email).orElseThrow()
        val w = workoutRepo.findById(workoutId)
            .orElseThrow { NoSuchElementException("Trening nie istnieje") }
        require(w.user.id == user.id) { "Brak uprawnień" }
        w.activityType = req.activityType
        w.durationMin  = req.durationMin
        w.kcalBurned   = req.kcalBurned ?: calcKcal(req.activityType, req.durationMin)
        w.distanceKm   = req.distanceKm
        w.avgHeartRate = req.avgHeartRate
        w.notes        = req.notes
        return workoutRepo.save(w).toResponse()
    }

    @Transactional
    fun delete(email: String, workoutId: Long) {
        val user = userRepo.findByEmail(email).orElseThrow()
        val w = workoutRepo.findById(workoutId)
            .orElseThrow { NoSuchElementException("Trening nie istnieje") }
        require(w.user.id == user.id) { "Brak uprawnień" }
        workoutRepo.delete(w)
    }

    private fun WorkoutActivity.toResponse() = WorkoutResponse(
        id           = id,
        activityDate = activityDate,
        activityType = activityType,
        durationMin  = durationMin,
        kcalBurned   = kcalBurned,
        distanceKm   = distanceKm,
        notes        = notes
    )
}
