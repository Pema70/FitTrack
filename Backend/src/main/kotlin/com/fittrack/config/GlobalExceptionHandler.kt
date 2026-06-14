package com.fittrack.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.NoSuchElementException

data class ApiError(
    val status: Int,
    val message: String,
    val errors: Map<String, String?>? = null
)

@RestControllerAdvice
class GlobalExceptionHandler {

    // 400 — błędy walidacji @Valid
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidation(e: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val errors = e.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, "Błąd walidacji danych", errors))
    }

    // 400 — nieprawidłowe dane wejściowe (np. złe enum, null zamiast liczby)
    @ExceptionHandler(IllegalArgumentException::class)
    fun onIllegalArg(e: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, e.message ?: "Nieprawidłowe żądanie"))

    // 400 — nieparsowany JSON (np. brakujący cudzysłów, zły typ pola)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun onUnreadable(e: HttpMessageNotReadableException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, "Nieprawidłowy format JSON: ${e.mostSpecificCause.message}"))

    // 400 — zły typ parametru URL/query (np. litera zamiast Long w /{id})
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun onTypeMismatch(e: MethodArgumentTypeMismatchException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, "Nieprawidłowy typ parametru '${e.name}': oczekiwano ${e.requiredType?.simpleName}"))

    // 400 — brakujący wymagany @RequestParam
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun onMissingParam(e: MissingServletRequestParameterException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiError(400, "Brakujący parametr '${e.parameterName}' (${e.parameterType})"))

    // 401 — złe hasło / email
    @ExceptionHandler(BadCredentialsException::class)
    fun onBadCreds(e: BadCredentialsException) =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError(401, "Niepoprawny email lub hasło"))

    // 403 — zalogowany, ale brak uprawnień do zasobu
    @ExceptionHandler(AccessDeniedException::class)
    fun onAccessDenied(e: AccessDeniedException) =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiError(403, "Brak dostępu do tego zasobu"))

    // 404 — zasób nie istnieje
    @ExceptionHandler(NoSuchElementException::class)
    fun onNotFound(e: NoSuchElementException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError(404, e.message ?: "Nie znaleziono zasobu"))

    // 500 — catch-all
    @ExceptionHandler(Exception::class)
    fun onGeneric(e: Exception): ResponseEntity<ApiError> {
        e.printStackTrace()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError(500, "Wewnętrzny błąd serwera"))
    }
}