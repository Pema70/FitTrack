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
import org.junit.jupiter.api.assertDoesNotThrow   
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
        val u = User(id = 1L, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        val slot = slot<WorkoutActivity>()
        every { workoutRepo.save(capture(slot)) } answers { slot.captured.apply { id = 7L } }

        val resp = service.log(
            "u@fittrack.pl", WorkoutRequest(
                activityDate = LocalDate.of(2026, 6, 10),
                activityType = "GYM",
                durationMin  = 45,
                kcalBurned   = 300,
                distanceKm   = null,
                avgHeartRate = null,           
                notes        = "klatka + barki"
            )
        )
        assertEquals(7L, resp.id)
        assertEquals("GYM", resp.activityType)
        assertEquals(300, resp.kcalBurned)
    }

    @Test
    fun `log automatycznie wylicza kalorie gdy kcalBurned jest null`() {
        val u = User(id = 1L, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        val slot = slot<WorkoutActivity>()
        every { workoutRepo.save(capture(slot)) } answers { slot.captured.apply { id = 8L } } 

        val resp = service.log(
            "u@fittrack.pl", WorkoutRequest(
                activityDate = LocalDate.now(),
                activityType = "CYCLING",      
                durationMin  = 60,            
                kcalBurned   = null,
                distanceKm   = BigDecimal("25.0"),
                avgHeartRate = null,           
                notes        = "Szybki przejazd"
            )
        )
        assertEquals(562, resp.kcalBurned)
    }

    @Test
    fun `getForDate filtruje po dacie`() {
        val u = User(id = 1L, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)
        val date = LocalDate.of(2026, 6, 10)
        every { workoutRepo.findAllByUserIdAndActivityDate(1L, date) } returns listOf(  
            WorkoutActivity(
                id           = 1L,                                                       
                user         = u,
                activityDate = date,
                activityType = "BIKE",
                durationMin  = 60,
                kcalBurned   = 500,
                distanceKm   = BigDecimal("20")
            )
        )
        val list = service.getForDate("u@fittrack.pl", date)
        assertEquals(1, list.size)
        assertEquals("BIKE", list[0].activityType)
    }

    @Test
    fun `update modyfikuje trening i przelicza kalorie`() {
        val u = User(id = 1L, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)

        val existingWorkout = WorkoutActivity(
            id           = 5L,                                                           
            user         = u,
            activityDate = LocalDate.now(),
            activityType = "WALKING",
            durationMin  = 30,
            kcalBurned   = 150,
            distanceKm   = null
        )
        every { workoutRepo.findById(5L) } returns Optional.of(existingWorkout)         
        val slot = slot<WorkoutActivity>()
        every { workoutRepo.save(capture(slot)) } answers { slot.captured }

        val req = WorkoutRequest(
            activityDate = LocalDate.now(),
            activityType = "RUNNING",          
            durationMin  = 60,                 
            kcalBurned   = null,
            distanceKm   = BigDecimal("10.0"),
            avgHeartRate = null,               
            notes        = "Bieg wieczorny"
        )

        val resp = service.update("u@fittrack.pl", 5L, req)
        assertEquals("RUNNING", resp.activityType)
        assertEquals(735, resp.kcalBurned)
        assertEquals(BigDecimal("10.0"), resp.distanceKm)
    }

    @Test
    fun `update rzuca wyjatek gdy probujesz edytowac cudzy trening`() {
        val attacker = User(id = 1L, email = "haker@fittrack.pl", password = "x")
        val victim   = User(id = 2L, email = "ofiara@fittrack.pl", password = "y")

        every { userRepo.findByEmail("haker@fittrack.pl") } returns Optional.of(attacker)

        val victimWorkout = WorkoutActivity(
            id           = 10L,                                                         
            user         = victim,
            activityDate = LocalDate.now(),
            activityType = "GYM",
            durationMin  = 60,
            kcalBurned   = 400
        )
        every { workoutRepo.findById(10L) } returns Optional.of(victimWorkout)

        val req = WorkoutRequest(
            LocalDate.now(), "GYM", 60, 400, null,
            avgHeartRate = null,               
            notes        = null
        )

        val ex = assertThrows<IllegalArgumentException> {
            service.update("haker@fittrack.pl", 10L, req)
        }
        assertEquals("Brak uprawnień", ex.message)
    }

    @Test
    fun `delete usuwa trening`() {
        val u = User(id = 1L, email = "u@fittrack.pl", password = "x")
        every { userRepo.findByEmail("u@fittrack.pl") } returns Optional.of(u)

        val workout = WorkoutActivity(
            id           = 5L,                                                           
            user         = u,
            activityDate = LocalDate.now(),
            activityType = "WALKING",
            durationMin  = 30,
            kcalBurned   = 150
        )
        every { workoutRepo.findById(5L) } returns Optional.of(workout)
        every { workoutRepo.delete(workout) } returns Unit

        assertDoesNotThrow { service.delete("u@fittrack.pl", 5L) }
        verify(exactly = 1) { workoutRepo.delete(workout) }
    }

    @Test
    fun `delete rzuca wyjatek gdy brak uprawnien do usuniecia`() {
        val attacker = User(id = 1L, email = "haker@fittrack.pl", password = "x")
        val victim   = User(id = 2L, email = "ofiara@fittrack.pl", password = "y")

        every { userRepo.findByEmail("haker@fittrack.pl") } returns Optional.of(attacker)

        val victimWorkout = WorkoutActivity(
            id           = 10L,                                                          
            user         = victim,
            activityDate = LocalDate.now(),
            activityType = "GYM",
            durationMin  = 60,
            kcalBurned   = 400
        )
        every { workoutRepo.findById(10L) } returns Optional.of(victimWorkout)

        val ex = assertThrows<IllegalArgumentException> {
            service.delete("haker@fittrack.pl", 10L)
        }
        assertEquals("Brak uprawnień", ex.message)
        verify(exactly = 0) { workoutRepo.delete(any()) }
    }
}