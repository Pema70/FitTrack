# API Overview

Backend FitTrack udostępnia REST API zbudowane w Spring Boot 3.3.2 (Kotlin 1.9).  
Dokumentacja interaktywna dostępna jest pod adresem `/swagger-ui.html` (OpenAPI 3, springdoc-openapi 2.6).

## Baza URL

| Środowisko    | URL                        |
|---------------|----------------------------|
| Development   | `http://localhost:8080`    |
| Production    | TBD                        |

## Autoryzacja

Wszystkie chronione endpointy wymagają nagłówka: Authorization: Bearer <JWT>


Token JWT (podpis HS256) jest wydawany po pomyślnym logowaniu przez `POST /api/auth/login`.  
Czas życia tokenu oraz logika odświeżania są obsługiwane w `AuthService`.

## Główne grupy endpointów

| Prefix           | Opis                                      |
|------------------|-------------------------------------------|
| `/api/auth`      | Rejestracja, logowanie, odświeżanie tokenu |
| `/api/profile`   | Pobieranie i edycja profilu użytkownika   |
| `/api/diary`     | CRUD wpisów posiłków i treningów          |
| `/api/recipes`   | Przeglądanie, dodawanie i edycja przepisów |

## Obsługa błędów

Wszystkie błędy zwracane są w ujednoliconym formacie przez `GlobalExceptionHandler.kt`:

```json
{
  "timestamp": "2025-06-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Pole email jest wymagane"
}
```