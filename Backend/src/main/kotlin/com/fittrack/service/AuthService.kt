package com.fittrack.service

import com.fittrack.dto.*
import com.fittrack.entity.User
import com.fittrack.entity.UserProfile
import com.fittrack.repository.*
import com.fittrack.security.JwtUtils
import org.springframework.security.authentication.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepo: UserRepository,
    private val profileRepo: UserProfileRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authManager: AuthenticationManager,
    private val jwtUtils: JwtUtils
) {
    @Transactional
    fun register(req: RegisterRequest): TokenResponse {
        if (userRepo.existsByEmail(req.email))
            throw IllegalArgumentException("Email jest już zarejestrowany")
        val user = userRepo.save(
            User(email = req.email, password = passwordEncoder.encode(req.password))
        )
        profileRepo.save(UserProfile(user = user))
        return TokenResponse(
            accessToken  = jwtUtils.generateToken(user.email),
            refreshToken = jwtUtils.generateRefreshToken(user.email)
        )
    }

    fun login(req: LoginRequest): TokenResponse {
        authManager.authenticate(UsernamePasswordAuthenticationToken(req.email, req.password))
        return TokenResponse(
            accessToken  = jwtUtils.generateToken(req.email),
            refreshToken = jwtUtils.generateRefreshToken(req.email)
        )
    }

    fun refresh(req: RefreshRequest): TokenResponse {
        if (!jwtUtils.isValid(req.refreshToken))
            throw IllegalArgumentException("Nieprawidłowy refresh token")
        val email = jwtUtils.extractEmail(req.refreshToken)
        return TokenResponse(
            accessToken  = jwtUtils.generateToken(email),
            refreshToken = jwtUtils.generateRefreshToken(email)
        )
    }

    @Transactional
    fun changePassword(email: String, req: ChangePasswordRequest) {
        val user = userRepo.findByEmail(email).orElseThrow()
        if (!passwordEncoder.matches(req.oldPassword, user.password))
            throw IllegalArgumentException("Stare hasło jest nieprawidłowe")
        user.password = passwordEncoder.encode(req.newPassword)
        userRepo.save(user)
    }
}