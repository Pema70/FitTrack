package com.fittrack.controller

import com.fittrack.dto.*
import com.fittrack.service.ProfileService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@Tag(name = "Profile", description = "Profil użytkownika i kalkulacja kcal")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/profile")
class ProfileController(private val profileService: ProfileService) {

    @Operation(summary = "Pobierz profil zalogowanego użytkownika")
    @GetMapping
    fun get(@AuthenticationPrincipal ud: UserDetails) = profileService.getProfile(ud.username)

    @Operation(summary = "Aktualizuj profil i przelicz dzienny cel kcal (Mifflin–St Jeor)")
    @PutMapping
    fun update(@AuthenticationPrincipal ud: UserDetails, @Valid @RequestBody req: ProfileUpdateRequest) =
        profileService.updateProfile(ud.username, req)
}
