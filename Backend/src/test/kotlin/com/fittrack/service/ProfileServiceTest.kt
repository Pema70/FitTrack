package com.fittrack.service

import com.fittrack.dto.ProfileUpdateRequest
import com.fittrack.entity.User
import com.fittrack.entity.UserProfile
import com.fittrack.repository.UserProfileRepository
import com.fittrack.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

class ProfileServiceTest {

    private val userRepo: UserRepository = mockk()
    private val profileRepo: UserProfileRepository = mockk()
    private val service = ProfileService(userRepo, profileRepo)

    private fun setupUser(): Pair<User, UserProfile> {
        val user = User(id = 1L, email = "u@fittrack.pl", password = "x")
        val profile = UserProfile(id = 1L, user = user)
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(user)
        every { profileRepo.findByUserId(1L) } returns Optional.of(profile)
        every { profileRepo.save(any()) } answers { firstArg() }
        return user to profile
    }

    @Test
    fun updateProfileCalculatesTDEE() {
        setupUser()
        val resp = service.updateProfile(
            "u@fittrack.pl", ProfileUpdateRequest(
                displayName   = "Jakub",
                gender        = "MALE",
                birthDate     = LocalDate.now().minusYears(25),
                weightKg      = BigDecimal("80"),
                heightCm      = BigDecimal("180"),
                activityLevel = "MODERATELY_ACTIVE",
                goal          = "MAINTAIN"
            )
        )
        assertNotNull(resp.dailyKcalGoal)
        assertTrue(resp.dailyKcalGoal!! > 1500, "TDEE > 1500, bylo: ${resp.dailyKcalGoal}")
        assertTrue(resp.dailyKcalGoal!! < 5000, "TDEE < 5000, bylo: ${resp.dailyKcalGoal}")
        assertEquals("Jakub", resp.displayName)
    }

    @Test
    fun femaleHasSensibleTDEE() {
        setupUser()
        val resp = service.updateProfile(
            "u@fittrack.pl", ProfileUpdateRequest(
                gender        = "FEMALE",
                birthDate     = LocalDate.now().minusYears(25),
                weightKg      = BigDecimal("60"),
                heightCm      = BigDecimal("165"),
                activityLevel = "SEDENTARY",
                goal          = "MAINTAIN"
            )
        )
        assertNotNull(resp.dailyKcalGoal)
        // BMR FEMALE = 10*60 + 6.25*165 - 5*25 - 161 = 1345
        // TDEE SEDENTARY (* ~1.2) ≈ 1614
        assertTrue(resp.dailyKcalGoal!! > 1000, "TDEE FEMALE > 1000, bylo: ${resp.dailyKcalGoal}")
        assertTrue(resp.dailyKcalGoal!! < 3000, "TDEE FEMALE < 3000, bylo: ${resp.dailyKcalGoal}")
    }

    @Test
    fun updateProfileSavesDisplayName() {
        setupUser()
        val resp = service.updateProfile(
            "u@fittrack.pl", ProfileUpdateRequest(
                displayName = "Anna"
            )
        )
        assertEquals("Anna", resp.displayName)
    }

    @Test
    fun getProfileReturnsDataFromDatabase() {
        val (_, profile) = setupUser()
        profile.displayName   = "Test"
        profile.dailyKcalGoal = 2000
        val resp = service.getProfile("u@fittrack.pl")
        assertEquals("Test", resp.displayName)
        assertEquals(2000, resp.dailyKcalGoal)
    }

    @Test
    fun updateProfileWithoutBirthDateDoesNotCrash() {
        setupUser()
        val resp = service.updateProfile(
            "u@fittrack.pl", ProfileUpdateRequest(
                displayName = "NoBirthDate",
                weightKg    = BigDecimal("70"),
                heightCm    = BigDecimal("175")
            )
        )
        assertEquals("NoBirthDate", resp.displayName)
    }
}