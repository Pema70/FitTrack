package com.fittrack.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class JwtUtilsTest {

    private val secret = "dGVzdHRlc3R0ZXN0dGVzdHRlc3R0ZXN0dGVzdHRlc3Q="
    private val util = JwtUtils(secret, 60_000L, 600_000L)

    @Test
    fun generateTokenAndExtractEmail() {
        val token = util.generateToken("user@fittrack.pl")
        assertTrue(token.isNotBlank())
        assertEquals("user@fittrack.pl", util.extractEmail(token))
    }

    @Test
    fun validTokenIsValid() {
        val token = util.generateToken("user@fittrack.pl")
        assertTrue(util.isValid(token))
    }

    @Test
    fun corruptedTokenIsNotValid() {
        assertDoesNotThrow { assertFalse(util.isValid("to.nie.jest.token")) }
        assertDoesNotThrow { assertFalse(util.isValid("")) }
        assertDoesNotThrow { assertFalse(util.isValid("   ")) }
    }

    @Test
    fun refreshTokenIsValidAndContainsEmail() {
        val rt = util.generateRefreshToken("a@b.pl")
        assertTrue(util.isValid(rt))
        assertEquals("a@b.pl", util.extractEmail(rt))
    }

    @Test
    fun tokenSignedWithDifferentKeyIsInvalid() {
        val other = JwtUtils(
            "aW5ueWlubnlpbm55aW5ueWlubnlpbm55aW5ueWlubnk=",
            60_000L, 600_000L
        )
        val token = other.generateToken("user@fittrack.pl")
        assertDoesNotThrow { assertFalse(util.isValid(token)) }
    }
}