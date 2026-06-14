package com.fittrack.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

/**
 * Testy integracyjne REST API. Uruchamiaja pelen kontekst Spring na H2 (in-memory).
 *
 * Scenariusze:
 *  1)  Rejestracja zwraca access + refresh token
 *  2)  GET /api/profile bez tokena → 401
 *  3)  Aktualizacja profilu liczy dailyKcalGoal (TDEE)
 *  4)  Dodanie wpisu w dzienniku (customName + zdjecie) + odczyt podsumowania dnia
 *  5)  Lista przepisow publicznych dostepna bez logowania (GET /api/recipes)
 *  6)  Refresh tokena zwraca nowy access token
 *  7)  Duplikat rejestracji → 4xx
 *  8)  Rejestracja z niepoprawnym emailem → 400 (walidacja DTO)
 *  9)  Rejestracja z za krotkim haslem → 400 (walidacja DTO)
 *  10) Dodanie wpisu z ujemna gramatura → 400 (walidacja DTO)
 *  11) Dodanie wpisu z data z przyszlosci → 400 (walidacja DTO)
 *  12) Niepoprawny JSON → 400
 */
 
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ApiIntegrationTest @Autowired constructor(
    private val mvc: MockMvc,
    private val om: ObjectMapper
) {

    private fun register(email: String, password: String = "Tajne123"): Pair<String, String> {
        val resp = mvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"$email","password":"$password"}""")
        )
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
        val node = om.readTree(resp)
        return node["accessToken"].asText() to node["refreshToken"].asText()
    }

    // ─── scenariusze pozytywne ──────────────────────────────────────────────

    @Test
    @Order(1)
    fun `1 - rejestracja zwraca access i refresh token`() {
        val resp = mvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"alice@fittrack.pl","password":"Tajne123"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
            .andExpect(jsonPath("$.refreshToken").isNotEmpty)
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andReturn().response.contentAsString
        assertTrue(resp.contains("accessToken"))
    }

    @Test
    @Order(2)
    fun `2 - profile bez tokena zwraca 401`() {
        mvc.perform(get("/api/profile"))
            .andExpect(status().isUnauthorized)  // precyzyjnie 401
    }

    @Test
    @Order(3)
    fun `3 - update profilu liczy dailyKcalGoal`() {
        val (token, _) = register("bob@fittrack.pl")
        val body = """
            {
              "displayName": "Bob",
              "gender": "MALE",
              "birthDate": "2000-01-01",
              "weightKg": 80,
              "heightCm": 180,
              "activityLevel": "MODERATELY_ACTIVE",
              "goal": "MAINTAIN"
            }
        """.trimIndent()
        
        mvc.perform(
            put("/api/profile")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.displayName").value("Bob"))
            .andExpect(jsonPath("$.dailyKcalGoal").isNumber)
    }

    @Test
    @Order(4)
    fun `4 - dodanie wpisu z customName i odczyt podsumowania dnia`() {
        val (token, _) = register("carol@fittrack.pl")
        val today = LocalDate.now().toString()
        val body = """
            {
              "entryDate": "$today",
              "mealType": "LUNCH",
              "customName": "Spaghetti domowe",
              "quantityG": 300,
              "photoPath": "/data/food_photos/test.jpg",
              "note": "kcal=550"
            }
        """.trimIndent()

        mvc.perform(
            post("/api/diary")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.productName").value("Spaghetti domowe"))
            .andExpect(jsonPath("$.photoPath").value("/data/food_photos/test.jpg"))

        mvc.perform(
            get("/api/diary/summary")
                .header("Authorization", "Bearer $token")
                .param("date", today)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.date").value(today))
            .andExpect(jsonPath("$.kcalGoal").isNumber)
            .andExpect(jsonPath("$.kcalConsumed").isNumber)
    }

    @Test
    @Order(5)
    fun `5 - lista przepisow publicznych dostepna bez logowania`() {
        mvc.perform(get("/api/recipes"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    }

    @Test
    @Order(6)
    fun `6 - refresh tokena zwraca nowy access`() {
        val (_, refresh) = register("dave@fittrack.pl")

        mvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"refreshToken":"$refresh"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isNotEmpty)
    }

    @Test
    @Order(7)
    fun `7 - duplikat rejestracji zwraca blad`() {
        register("erin@fittrack.pl")
        mvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"erin@fittrack.pl","password":"Tajne123"}""")
        )
            .andExpect(status().is4xxClientError)
    }

    // ─── scenariusze walidacji (nowe po zmianach w DTO) ────────────────────

    @Test
    @Order(8)
    fun `8 - rejestracja z niepoprawnym emailem zwraca 400`() {
        mvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"to-nie-jest-email","password":"Tajne123"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.email").exists())
    }

    @Test
    @Order(9)
    fun `9 - rejestracja z za krotkim haslem zwraca 400`() {
        mvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"short@fittrack.pl","password":"abc"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.password").exists())
    }

    @Test
    @Order(10)
    fun `10 - wpis do dziennika z ujemna gramatura zwraca 400`() {
        val (token, _) = register("frank@fittrack.pl")
        val today = LocalDate.now().toString()
        mvc.perform(
            post("/api/diary")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"entryDate":"$today","mealType":"LUNCH","quantityG":-10}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    @Order(11)
    fun `11 - wpis do dziennika z data z przyszlosci zwraca 400`() {
        val (token, _) = register("grace@fittrack.pl")
        val future = LocalDate.now().plusDays(5).toString()
        mvc.perform(
            post("/api/diary")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"entryDate":"$future","mealType":"LUNCH","quantityG":200}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    @Order(12)
    fun `12 - niepoprawny JSON zwraca 400`() {
        mvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "brak-zamkniecia""")  // złamany JSON
        )
            .andExpect(status().isBadRequest)
    }
}