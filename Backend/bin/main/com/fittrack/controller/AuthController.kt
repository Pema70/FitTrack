package com.fittrack.controller

import com.fittrack.dto.*
import com.fittrack.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth", description = "Rejestracja, logowanie i zarządzanie kontem")
@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @Operation(summary = "Rejestracja nowego użytkownika")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody req: RegisterRequest) = authService.register(req)

    @Operation(summary = "Logowanie — zwraca JWT")
    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest) = authService.login(req)

    @Operation(summary = "Odśwież access token")
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody req: RefreshRequest) = authService.refresh(req)

    @Operation(summary = "Zmiana hasła (wymaga starego hasła)")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changePassword(
        @AuthenticationPrincipal ud: UserDetails,
        @Valid @RequestBody req: ChangePasswordRequest
    ) = authService.changePassword(ud.username, req)
}