package com.fittrack.controller

import com.fittrack.dto.*
import com.fittrack.service.WorkoutService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Workouts", description = "Treningi i spalone kalorie")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/workouts")
class WorkoutController(private val workoutService: WorkoutService) {

    @Operation(summary = "Pobierz treningi na dany dzień")
    @GetMapping
    fun getForDate(
        @AuthenticationPrincipal ud: UserDetails,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate = LocalDate.now()
    ) = workoutService.getForDate(ud.username, date)

    @Operation(summary = "Dodaj trening (kcalBurned=null → auto z MET)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun log(
        @AuthenticationPrincipal ud: UserDetails,
        @Valid @RequestBody req: WorkoutRequest
    ) = workoutService.log(ud.username, req)

    @Operation(summary = "Edytuj trening")
    @PutMapping("/{id}")
    fun update(
        @AuthenticationPrincipal ud: UserDetails,
        @PathVariable id: Long,
        @Valid @RequestBody req: WorkoutRequest
    ) = workoutService.update(ud.username, id, req)

    @Operation(summary = "Usuń trening")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal ud: UserDetails,
        @PathVariable id: Long
    ) = workoutService.delete(ud.username, id)
}