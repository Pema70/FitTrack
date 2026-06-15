package com.fittrack.config

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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

@RestControllerAdvice
class GlobalExceptionHandler {

    private fun text(status: HttpStatus, msg: String) =
        ResponseEntity.status(status)
            .contentType(MediaType.TEXT_PLAIN)
            .body(msg)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidation(e: MethodArgumentNotValidException): ResponseEntity<String> {
        val fields = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return text(HttpStatus.BAD_REQUEST, "Błąd walidacji: $fields")
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun onIllegalArg(e: IllegalArgumentException) =
        text(HttpStatus.BAD_REQUEST, e.message ?: "Nieprawidłowe żądanie")

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun onUnreadable(e: HttpMessageNotReadableException) =
        text(HttpStatus.BAD_REQUEST, "Nieprawidłowy format JSON: ${e.mostSpecificCause.message}")

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun onTypeMismatch(e: MethodArgumentTypeMismatchException) =
        text(HttpStatus.BAD_REQUEST, "Nieprawidłowy typ parametru '${e.name}': oczekiwano ${e.requiredType?.simpleName}")

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun onMissingParam(e: MissingServletRequestParameterException) =
        text(HttpStatus.BAD_REQUEST, "Brakujący parametr '${e.parameterName}' (${e.parameterType})")

    @ExceptionHandler(BadCredentialsException::class)
    fun onBadCreds(e: BadCredentialsException) =
        text(HttpStatus.UNAUTHORIZED, "Niepoprawny email lub hasło")

    @ExceptionHandler(AccessDeniedException::class)
    fun onAccessDenied(e: AccessDeniedException) =
        text(HttpStatus.FORBIDDEN, "Brak dostępu do tego zasobu")

    @ExceptionHandler(NoSuchElementException::class)
    fun onNotFound(e: NoSuchElementException) =
        text(HttpStatus.NOT_FOUND, e.message ?: "Nie znaleziono zasobu")

    @ExceptionHandler(Exception::class)
    fun onGeneric(e: Exception): ResponseEntity<String> {
        e.printStackTrace()
        return text(HttpStatus.INTERNAL_SERVER_ERROR, "Wewnętrzny błąd serwera")
    }
}