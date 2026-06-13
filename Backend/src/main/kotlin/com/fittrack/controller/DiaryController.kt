package com.fittrack.controller

import com.fittrack.dto.*
import com.fittrack.service.DiaryService
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

@Tag(name = "Diary", description = "Dziennik posiłków i dzienne podsumowanie")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/diary")
class DiaryController(private val diaryService: DiaryService) {

    @Operation(summary = "Pobierz wpisy na dany dzień")
    @GetMapping
    fun getEntries(
        @AuthenticationPrincipal ud: UserDetails,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate = LocalDate.now()
    ) = diaryService.getEntriesForDate(ud.username, date)

    @Operation(summary = "Dodaj wpis do dziennika")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addEntry(
        @AuthenticationPrincipal ud: UserDetails,
        @Valid @RequestBody req: DiaryEntryRequest
    ) = diaryService.addEntry(ud.username, req)

    @Operation(summary = "Edytuj ilość (gramy) wpisu — przelicza kcal")
    @PatchMapping("/{id}")
    fun updateEntry(
        @AuthenticationPrincipal ud: UserDetails,
        @PathVariable id: Long,
        @Valid @RequestBody req: DiaryUpdateRequest
    ) = diaryService.updateEntry(ud.username, id, req)

    @Operation(summary = "Usuń wpis")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteEntry(
        @AuthenticationPrincipal ud: UserDetails,
        @PathVariable id: Long
    ) = diaryService.deleteEntry(ud.username, id)

    @Operation(summary = "Dzienne podsumowanie kcal i makroskładników")
    @GetMapping("/summary")
    fun summary(
        @AuthenticationPrincipal ud: UserDetails,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate = LocalDate.now()
    ) = diaryService.getDailySummary(ud.username, date)
}