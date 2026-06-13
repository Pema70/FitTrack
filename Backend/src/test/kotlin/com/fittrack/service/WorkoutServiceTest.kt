package com.fittrack.service

import com.fittrack.dto.WorkoutRequest
import com.fittrack.entity.User
import com.fittrack.entity.WorkoutActivity
import com.fittrack.repository.UserRepository
import com.fittrack.repository.WorkoutActivityRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Optional

class WorkoutServiceTest {

    private val userRepo: UserRepository = mockk()
    private val workoutRepo: WorkoutActivityRepository = mockk()
    private val service = WorkoutService(userRepo, workoutRepo)

    @Test
    fun `log zapisuje trening i zwraca id`() {
        val u = User(id = 1, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        val slot = slot<WorkoutActivity>()
        every { workoutRepo.save(capture(slot)) } answers { slot.captured.apply { id = 7 } }

        val resp = service.log("u@fittrack.pl", WorkoutRequest(
            activityDate = LocalDate.of(2026, 6, 10),
            activityType = "GYM",
            durationMin  = 45,
            kcalBurned   = 300,
            distanceKm   = null,
            notes        = "klatka + barki"
        ))
        assertEquals(7, resp.id)
        assertEquals("GYM", resp.activityType)
        assertEquals(300, resp.kcalBurned)
    }

    @Test
    fun `getForDate filtruje po dacie`() {
        val u = User(id = 1, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        val date = LocalDate.of(2026, 6, 10)
        every { workoutRepo.findAllByUserIdAndActivityDate(1, date) } returns listOf(
            WorkoutActivity(id=1, user=u, activityDate=date,
                activityType="BIKE", durationMin=60, kcalBurned=500,
                distanceKm = BigDecimal("20"))
        )
        val list = service.getForDate("u@fittrack.pl", date)
        assertEquals(1, list.size)
        assertEquals("BIKE", list[0].activityType)
    }
}
