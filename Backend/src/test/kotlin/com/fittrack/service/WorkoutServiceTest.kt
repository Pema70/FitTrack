package com.fittrack.service

import com.fittrack.dto.WorkoutRequest
import com.fittrack.entity.User
import com.fittrack.entity.WorkoutActivity
import com.fittrack.repository.UserRepository
import com.fittrack.repository.WorkoutActivityRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    fun `log automatycznie wylicza kalorie gdy kcalBurned jest null`() {
        val u = User(id = 1, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        val slot = slot<WorkoutActivity>()
        every { workoutRepo.save(capture(slot)) } answers { slot.captured.apply { id = 8 } }

        val resp = service.log("u@fittrack.pl", WorkoutRequest(
            activityDate = LocalDate.now(),
            activityType = "CYCLING", // MET = 7.5
            durationMin  = 60,        // Wzór: 7.5 * 75.0 * 60 / 60 = 562.5 -> 562
            kcalBurned   = null,      // Brak ręcznie wpisanych kalorii
            distanceKm   = BigDecimal("25.0"),
            notes        = "Szybki przejazd"
        ))
        
        assertEquals(562, resp.kcalBurned)
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

    @Test
    fun `update modyfikuje trening i przelicza kalorie`() {
        val u = User(id = 1, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        
        val existingWorkout = WorkoutActivity(
            id = 5, user = u, activityDate = LocalDate.now(),
            activityType = "WALKING", durationMin = 30, kcalBurned = 150,
            distanceKm = null
        )
        every { workoutRepo.findById(5) } returns Optional.of(existingWorkout)
        val slot = slot<WorkoutActivity>()
        every { workoutRepo.save(capture(slot)) } answers { slot.captured }

        // Aktualizujemy na bieganie z nullowymi kaloriami - serwis powinien przeliczyć
        val req = WorkoutRequest(
            activityDate = LocalDate.now(),
            activityType = "RUNNING", // MET = 9.8
            durationMin  = 60,        // Wzór: 9.8 * 75.0 * 60 / 60 = 735
            kcalBurned   = null,
            distanceKm   = BigDecimal("10.0"),
            notes        = "Bieg wieczorny"
        )
        
        val resp = service.update("u@fittrack.pl", 5, req)
        
        assertEquals("RUNNING", resp.activityType)
        assertEquals(735, resp.kcalBurned)
        assertEquals(BigDecimal("10.0"), resp.distanceKm)
    }

    @Test
    fun `update rzuca wyjatek gdy próbujesz edytować cudzy trening`() {
        val attacker = User(id = 1, email = "haker@fittrack.pl", password = "x")
        val victim = User(id = 2, email = "ofiara@fittrack.pl", password = "y")
        
        every { userRepo.findByEmail("haker@fittrack.pl") } returns Optional.of(attacker)
        
        val victimWorkout = WorkoutActivity(
            id = 10, user = victim, activityDate = LocalDate.now(),
            activityType = "GYM", durationMin = 60, kcalBurned = 400
        )
        every { workoutRepo.findById(10) } returns Optional.of(victimWorkout)

        val req = WorkoutRequest(LocalDate.now(), "GYM", 60, 400, null, null, null)

        val exception = assertThrows<IllegalArgumentException> {
            service.update("haker@fittrack.pl", 10, req)
        }
        assertEquals("Brak uprawnień", exception.message)
    }

    @Test
    fun `delete usuwa trening`() {
        val u = User(id = 1, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        
        val workout = WorkoutActivity(
            id = 5, user = u, activityDate = LocalDate.now(),
            activityType = "WALKING", durationMin = 30, kcalBurned = 150
        )
        every { workoutRepo.findById(5) } returns Optional.of(workout)
        every { workoutRepo.delete(workout) } returns Unit

        assertDoesNotThrow {
            service.delete("u@fittrack.pl", 5)
        }
        
        // Weryfikacja czy repozytorium usunęło encję
        verify(exactly = 1) { workoutRepo.delete(workout) }
    }

    @Test
    fun `delete rzuca wyjatek gdy brak uprawnien do usuniecia`() {
        val attacker = User(id = 1, email = "haker@fittrack.pl", password = "x")
        val victim = User(id = 2, email = "ofiara@fittrack.pl", password = "y")
        
        every { userRepo.findByEmail("haker@fittrack.pl") } returns Optional.of(attacker)
        
        val victimWorkout = WorkoutActivity(
            id = 10, user = victim, activityDate = LocalDate.now(),
            activityType = "GYM", durationMin = 60, kcalBurned = 400
        )
        every { workoutRepo.findById(10) } returns Optional.of(victimWorkout)

        val exception = assertThrows<IllegalArgumentException> {
            service.delete("haker@fittrack.pl", 10)
        }
        assertEquals("Brak uprawnień", exception.message)
        
        // Weryfikacja czy usunięcie na pewno nie zostało wykonane
        verify(exactly = 0) { workoutRepo.delete(any()) }
    }
}