package com.fittrack.controller

import com.fittrack.dto.*
import com.fittrack.service.RecipeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "Recipes", description = "Przepisy kulinarne")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/recipes")
@Validated  // ← wymagane do walidacji @RequestParam
class RecipeController(private val recipeService: RecipeService) {

    @Operation(summary = "Wyszukaj publiczne przepisy")
    @GetMapping
    fun search(
        @RequestParam(defaultValue = "")
        @Size(max = 100, message = "Fraza wyszukiwania może mieć max 100 znaków")
        q: String,

        @RequestParam(required = false)
        @Size(max = 30, message = "Tag może mieć max 30 znaków")
        tag: String?
    ) = recipeService.search(q, tag)

    @Operation(summary = "Moje przepisy (tylko autora)")
    @GetMapping("/mine")
    fun mine(@AuthenticationPrincipal ud: UserDetails) =
        recipeService.getMine(ud.username)

    @Operation(summary = "Moje ulubione przepisy")
    @GetMapping("/favorites")
    fun favorites(@AuthenticationPrincipal ud: UserDetails) =
        recipeService.getFavorites(ud.username)

    @Operation(summary = "Dodaj przepis do ulubionych")
    @PostMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun addFavorite(
        @AuthenticationPrincipal ud: UserDetails,
        @PathVariable id: Long
    ) = recipeService.addFavorite(ud.username, id)

    @Operation(summary = "Usuń przepis z ulubionych")
    @DeleteMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeFavorite(
        @AuthenticationPrincipal ud: UserDetails,
        @PathVariable id: Long
    ) = recipeService.removeFavorite(ud.username, id)

    @Operation(summary = "Dodaj nowy przepis")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal ud: UserDetails,
        @Valid @RequestBody req: RecipeRequest
    ) = recipeService.create(ud.username, req)

    @Operation(summary = "Edytuj przepis (tylko autor)")
    @PutMapping("/{id}")
    fun update(
        @AuthenticationPrincipal ud: UserDetails,
        @PathVariable id: Long,
        @Valid @RequestBody req: RecipeRequest
    ) = recipeService.update(ud.username, id, req)

    @Operation(summary = "Usuń przepis (tylko autor)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @AuthenticationPrincipal ud: UserDetails,
        @PathVariable id: Long
    ) = recipeService.delete(ud.username, id)
}