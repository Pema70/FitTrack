package com.fittrack.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

/**
 * Testy integracyjne REST API. Uruchamiaja pelen kontekst Spring na H2 (in-memory).
 *
 * Scenariusze:
 *  1) Rejestracja + login (zwraca JWT)
 *  2) /api/profile bez tokena - 401
 *  3) Aktualizacja profilu liczy dailyKcalGoal (TDEE)
 *  4) Dodanie wpisu w dzienniku (zdjecie potrawy) + odczyt podsumowania dnia
 *  5) Lista przepisow publicznych jest dostepna bez logowania (GET /api/recipes)
 *  6) Refresh tokena zwraca nowy access token
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation::class)
class ApiIntegrationTest @Autowired constructor(
    private val mvc: MockMvc,
    private val om: ObjectMapper
) {

    // helpery
    private fun json(obj: Any) = om.writeValueAsString(obj)

    private fun register(email: String, password: String = "tajne123"): String {
        val resp = mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"$email","password":"$password"}"""))
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
        val node = om.readTree(resp)
        return node["accessToken"].asText()
    }

    @Test
    fun `1 - rejestracja zwraca access i refresh token`() {
        val resp = mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"alice@fittrack.pl","password":"tajne123"}"""))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").isNotEmpty)
            .andReturn().response.contentAsString
        assertTrue(resp.contains("accessToken"))
    }

    @Test
    fun `2 - profile bez tokena zwraca 4xx`() {
        mvc.perform(get("/api/profile"))
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `3 - update profilu liczy dailyKcalGoal`() {
        val token = register("bob@fittrack.pl")
        val body = """
            {
              "displayName":"Bob",
              "gender":"MALE",
              "birthDate":"2000-01-01",
              "weightKg":80,
              "heightCm":180,
              "activityLevel":"MODERATE",
              "goal":"MAINTAIN"
            }
        """.trimIndent()
        mvc.perform(put("/api/profile")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.displayName").value("Bob"))
            .andExpect(jsonPath("$.dailyKcalGoal").isNumber)
    }

    @Test
    fun `4 - dodanie wpisu z customName i odczyt podsumowania dnia`() {
        val token = register("carol@fittrack.pl")
        val today = LocalDate.now().toString()
        val body = """
            {
              "entryDate":"$today",
              "mealType":"LUNCH",
              "customName":"Spaghetti domowe",
              "quantityG":300,
              "photoPath":"/data/food_photos/test.jpg",
              "note":"kcal=550"
            }
        """.trimIndent()
        mvc.perform(post("/api/diary")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.productName").value("Spaghetti domowe"))
            .andExpect(jsonPath("$.photoPath").value("/data/food_photos/test.jpg"))

        mvc.perform(get("/api/diary/summary")
            .header("Authorization", "Bearer $token")
            .param("date", today))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.date").value(today))
            .andExpect(jsonPath("$.kcalGoal").isNumber)
    }

    @Test
    fun `5 - lista przepisow publicznych dostepna bez logowania`() {
        mvc.perform(get("/api/recipes"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `6 - refresh tokena zwraca nowy access`() {
        // rejestrujemy nowego uzytkownika i bierzemy refreshToken
        val resp = mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"dave@fittrack.pl","password":"tajne123"}"""))
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
        val refresh = om.readTree(resp)["refreshToken"].asText()

        mvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"refreshToken":"$refresh"}"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
    }

    @Test
    fun `7 - duplikat rejestracji zwraca blad`() {
        register("erin@fittrack.pl")
        mvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"erin@fittrack.pl","password":"tajne123"}"""))
            .andExpect(status().is4xxClientError)
    }
}
