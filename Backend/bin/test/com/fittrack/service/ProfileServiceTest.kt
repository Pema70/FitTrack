package com.fittrack.service

import com.fittrack.dto.ProfileUpdateRequest
import com.fittrack.entity.User
import com.fittrack.entity.UserProfile
import com.fittrack.repository.UserProfileRepository
import com.fittrack.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
        val user = User(id = 1, email = "u@fittrack.pl", password = "x")
        val profile = UserProfile(id = 1, user = user)
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(user)
        every { profileRepo.findByUserId(1) } returns Optional.of(profile)
        val slot = slot<UserProfile>()
        every { profileRepo.save(capture(slot)) } answers { slot.captured }
        return user to profile
    }

    @Test
    fun `update profilu liczy TDEE dla MALE-MODERATE-MAINTAIN`() {
        val (_, _) = setupUser()
        val resp = service.updateProfile("u@fittrack.pl", ProfileUpdateRequest(
            displayName = "Jakub",
            gender = "MALE",
            birthDate = LocalDate.now().minusYears(25),
            weightKg = BigDecimal("80"),
            heightCm = BigDecimal("180"),
            activityLevel = "MODERATE",
            goal = "MAINTAIN"
        ))
        // BMR = 10*80 + 6.25*180 - 5*25 + 5 = 800 + 1125 - 125 + 5 = 1805
        // TDEE = 1805 * 1.55 = 2797
        assertEquals(2797, resp.dailyKcalGoal)
        assertEquals("Jakub", resp.displayName)
    }

    @Test
    fun `goal LOSE odejmuje 500 kcal`() {
        setupUser()
        val resp = service.updateProfile("u@fittrack.pl", ProfileUpdateRequest(
            gender = "MALE",
            birthDate = LocalDate.now().minusYears(25),
            weightKg = BigDecimal("80"),
            heightCm = BigDecimal("180"),
            activityLevel = "MODERATE",
            goal = "LOSE"
        ))
        assertEquals(2797 - 500, resp.dailyKcalGoal)
    }

    @Test
    fun `goal GAIN dodaje 300 kcal`() {
        setupUser()
        val resp = service.updateProfile("u@fittrack.pl", ProfileUpdateRequest(
            gender = "MALE",
            birthDate = LocalDate.now().minusYears(25),
            weightKg = BigDecimal("80"),
            heightCm = BigDecimal("180"),
            activityLevel = "MODERATE",
            goal = "GAIN"
        ))
        assertEquals(2797 + 300, resp.dailyKcalGoal)
    }

    @Test
    fun `FEMALE ma inny BMR niz MALE`() {
        setupUser()
        val respFemale = service.updateProfile("u@fittrack.pl", ProfileUpdateRequest(
            gender = "FEMALE",
            birthDate = LocalDate.now().minusYears(25),
            weightKg = BigDecimal("60"),
            heightCm = BigDecimal("165"),
            activityLevel = "SEDENTARY",
            goal = "MAINTAIN"
        ))
        // BMR = 10*60 + 6.25*165 - 5*25 - 161 = 600 + 1031.25 - 125 - 161 = 1345.25
        // TDEE = 1345.25 * 1.2 = 1614 (toInt)
        assertEquals(1614, respFemale.dailyKcalGoal)
    }

    @Test
    fun `getProfile zwraca dane z bazy`() {
        val (_, profile) = setupUser()
        profile.displayName = "Test"
        profile.dailyKcalGoal = 2000
        val resp = service.getProfile("u@fittrack.pl")
        assertEquals("Test", resp.displayName)
        assertEquals(2000, resp.dailyKcalGoal)
    }
}
