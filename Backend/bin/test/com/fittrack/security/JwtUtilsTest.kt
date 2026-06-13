package com.fittrack.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JwtUtilsTest {

    // Base64 z 32-bajtowego sekretu testowego ("testtest..." x4)
    private val secret = "dGVzdHRlc3R0ZXN0dGVzdHRlc3R0ZXN0dGVzdHRlc3Q="
    private val util = JwtUtils(secret, 60_000L, 600_000L)

    @Test
    fun `generuje token i wyciaga email`() {
        val token = util.generateToken("user@fittrack.pl")
        assertTrue(token.isNotBlank())
        assertEquals("user@fittrack.pl", util.extractEmail(token))
    }

    @Test
    fun `prawidlowy token jest walidny`() {
        val token = util.generateToken("user@fittrack.pl")
        assertTrue(util.isValid(token))
    }

    @Test
    fun `zepsuty token nie jest walidny`() {
        assertFalse(util.isValid("to.nie.jest.token"))
        assertFalse(util.isValid(""))
    }

    @Test
    fun `refresh token tez jest walidny i ma email`() {
        val rt = util.generateRefreshToken("a@b.pl")
        assertTrue(util.isValid(rt))
        assertEquals("a@b.pl", util.extractEmail(rt))
    }

    @Test
    fun `token podpisany innym kluczem jest niewalidny`() {
        val inny = JwtUtils(
            "aW5ueWlubnlpbm55aW5ueWlubnlpbm55aW5ueWlubnk=", // inny secret
            60_000L, 600_000L
        )
        val token = inny.generateToken("user@fittrack.pl")
        assertFalse(util.isValid(token))
    }
}
