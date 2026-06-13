package com.fittrack.service

import com.fittrack.dto.*
import com.fittrack.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Service
class ProfileService(
    private val userRepo: UserRepository,
    private val profileRepo: UserProfileRepository
) {
    fun getProfile(email: String): ProfileResponse {
        val user = userRepo.findByEmail(email).orElseThrow()
        val p = profileRepo.findByUserId(user.id).orElseThrow()
        return p.toResponse()
    }

    @Transactional
    fun updateProfile(email: String, req: ProfileUpdateRequest): ProfileResponse {
        val user = userRepo.findByEmail(email).orElseThrow()
        val p = profileRepo.findByUserId(user.id).orElseThrow()

        p.displayName  = req.displayName ?: p.displayName
        p.gender       = req.gender ?: p.gender
        p.birthDate    = req.birthDate ?: p.birthDate
        p.weightKg     = req.weightKg ?: p.weightKg
        p.heightCm     = req.heightCm ?: p.heightCm
        p.activityLevel = req.activityLevel ?: p.activityLevel
        p.goal         = req.goal ?: p.goal

        // Harris-Benedict (zrewidowany Mifflin-St Jeor)
        if (p.weightKg != null && p.heightCm != null && p.birthDate != null) {
            val age = LocalDate.now().year - p.birthDate!!.year
            val bmr = when (p.gender) {
                "FEMALE" -> 10 * p.weightKg!!.toDouble() + 6.25 * p.heightCm!!.toDouble() - 5 * age - 161
                else     -> 10 * p.weightKg!!.toDouble() + 6.25 * p.heightCm!!.toDouble() - 5 * age + 5
            }
            val factor = when (p.activityLevel) {
                "SEDENTARY"   -> 1.2
                "LIGHT"       -> 1.375
                "MODERATE"    -> 1.55
                "ACTIVE"      -> 1.725
                "VERY_ACTIVE" -> 1.9
                else          -> 1.2
            }
            val tdee = (bmr * factor).toInt()
            p.dailyKcalGoal = when (p.goal) {
                "LOSE"    -> tdee - 500
                "GAIN"    -> tdee + 300
                else      -> tdee
            }
        }
        return profileRepo.save(p).toResponse()
    }

    private fun com.fittrack.entity.UserProfile.toResponse() = ProfileResponse(
        displayName   = displayName,
        gender        = gender,
        birthDate     = birthDate,
        weightKg      = weightKg,
        heightCm      = heightCm,
        activityLevel = activityLevel,
        goal          = goal,
        dailyKcalGoal = dailyKcalGoal,
        avatarUrl     = avatarUrl
    )
}
