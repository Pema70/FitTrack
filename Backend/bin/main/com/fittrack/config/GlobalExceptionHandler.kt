package com.fittrack.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.NoSuchElementException

data class ApiError(val status: Int, val message: String, val errors: Map<String, String?>? = null)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val errors = e.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, "Błąd walidacji", errors))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun onIllegalArg(e: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, e.message ?: "Nieprawidłowe żądanie"))

    @ExceptionHandler(BadCredentialsException::class)
    fun onBadCreds(e: BadCredentialsException) =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError(401, "Niepoprawny email lub hasło"))

    @ExceptionHandler(NoSuchElementException::class)
    fun onNotFound(e: NoSuchElementException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError(404, "Nie znaleziono zasobu"))

    @ExceptionHandler(Exception::class)
    fun onGeneric(e: Exception): ResponseEntity<ApiError> {
        e.printStackTrace()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError(500, e.message ?: "Błąd serwera"))
    }
}
