package com.fittrack.security

import com.fittrack.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(private val userRepo: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepo.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("Nie znaleziono użytkownika: $email") }
        return User(user.email, user.password, listOf(SimpleGrantedAuthority("ROLE_USER")))
    }
}
