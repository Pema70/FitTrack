package com.fittrack.service

import com.fittrack.dto.DiaryEntryRequest
import com.fittrack.entity.*
import com.fittrack.repository.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

class DiaryServiceTest {

    private val userRepo: UserRepository = mockk()
    private val profileRepo: UserProfileRepository = mockk()
    private val diaryRepo: DiaryEntryRepository = mockk()
    private val foodRepo: FoodProductRepository = mockk()
    private val workoutRepo: WorkoutActivityRepository = mockk()
    private val service = DiaryService(userRepo, profileRepo, diaryRepo, foodRepo, workoutRepo)

    private fun setupUser(): User {
        val u = User(id = 1, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        return u
    }

    @Test
    fun `dodaje wpis z produktu - kcal liczony proporcjonalnie do gramatury`() {
        setupUser()
        val product = FoodProduct(
            id = 10, name = "Banan",
            kcalPer100g = BigDecimal("89"),
            proteinG = BigDecimal("1.1"),
            fatG = BigDecimal("0.3"),
            carbsG = BigDecimal("23")
        )
        every { foodRepo.findById(10) } returns Optional.of(product)
        val slot = slot<DiaryEntry>()
        every { diaryRepo.save(capture(slot)) } answers { slot.captured.apply { id = 99 } }

        val resp = service.addEntry("u@fittrack.pl", DiaryEntryRequest(
            entryDate = LocalDate.of(2026, 6, 10),
            mealType = "BREAKFAST",
            productId = 10,
            quantityG = BigDecimal("150")
        ))

        // 89 * 150 / 100 = 133.5
        assertEquals(0, BigDecimal("133.50").compareTo(resp.kcal))
        assertEquals("Banan", resp.productName)
    }

    @Test
    fun `dodaje wpis z customName (zdjecie potrawy) - bez produktu kcal = 0`() {
        setupUser()
        val slot = slot<DiaryEntry>()
        every { diaryRepo.save(capture(slot)) } answers { slot.captured.apply { id = 1 } }

        val resp = service.addEntry("u@fittrack.pl", DiaryEntryRequest(
            entryDate  = LocalDate.now(),
            mealType   = "LUNCH",
            customName = "Spaghetti domowe",
            quantityG  = BigDecimal("300"),
            photoPath  = "/data/food_photos/meal_x.jpg",
            note       = "kcal=550"
        ))
        assertEquals("Spaghetti domowe", resp.productName)
        assertEquals("/data/food_photos/meal_x.jpg", resp.photoPath)
        assertEquals(0, BigDecimal.ZERO.compareTo(resp.kcal))
    }

    @Test
    fun `usuwa wlasny wpis`() {
        val u = setupUser()
        val entry = DiaryEntry(
            id = 5, user = u,
            entryDate = LocalDate.now(),
            mealType = "DINNER",
            quantityG = BigDecimal("100")
        )
        every { diaryRepo.findById(5) } returns Optional.of(entry)
        every { diaryRepo.delete(entry) } returns Unit

        service.deleteEntry("u@fittrack.pl", 5)
        verify { diaryRepo.delete(entry) }
    }

    @Test
    fun `nie pozwala usunac cudzego wpisu`() {
        setupUser()
        val obcyUser = User(id = 999, email = "x@x.pl", password = "x")
        val entry = DiaryEntry(
            id = 5, user = obcyUser,
            entryDate = LocalDate.now(),
            mealType = "DINNER",
            quantityG = BigDecimal("100")
        )
        every { diaryRepo.findById(5) } returns Optional.of(entry)
        assertThrows(IllegalArgumentException::class.java) {
            service.deleteEntry("u@fittrack.pl", 5)
        }
    }

    @Test
    fun `daily summary sumuje kcal i odejmuje od celu`() {
        val u = setupUser()
        val profile = UserProfile(id = 1, user = u, dailyKcalGoal = 2000)
        every { profileRepo.findByUserId(1) } returns Optional.of(profile)

        val date = LocalDate.of(2026, 6, 10)
        every { diaryRepo.findAllByUserIdAndEntryDate(1, date) } returns listOf(
            DiaryEntry(id=1, user=u, entryDate=date, mealType="BREAKFAST",
                quantityG=BigDecimal("100"), kcal=BigDecimal("500"),
                proteinG=BigDecimal("20"), fatG=BigDecimal("10"), carbsG=BigDecimal("50")),
            DiaryEntry(id=2, user=u, entryDate=date, mealType="LUNCH",
                quantityG=BigDecimal("100"), kcal=BigDecimal("700"),
                proteinG=BigDecimal("30"), fatG=BigDecimal("15"), carbsG=BigDecimal("70"))
        )
        every { workoutRepo.findAllByUserIdAndActivityDate(1, date) } returns listOf(
            WorkoutActivity(id=1, user=u, activityDate=date,
                activityType="GYM", durationMin=60, kcalBurned=300)
        )

        val s = service.getDailySummary("u@fittrack.pl", date)
        assertEquals(2000, s.kcalGoal)
        assertEquals(0, BigDecimal("1200").compareTo(s.kcalConsumed))
        assertEquals(300, s.kcalBurned)
        // remaining = 2000 - 1200 + 300 = 1100
        assertEquals(0, BigDecimal("1100").compareTo(s.kcalRemaining))
        assertEquals(0, BigDecimal("50").compareTo(s.proteinG))
        assertEquals(0, BigDecimal("25").compareTo(s.fatG))
        assertEquals(0, BigDecimal("120").compareTo(s.carbsG))
    }
}
