# API Overview — FitTrack Backend

Wersja API: v1 (Spring Boot 3.3.2)
Base URL (lokalnie): [http://10.0.2.2:8080](http://10.0.2.2:8080) (Android Emulator) | http://localhost:8080
Dokumentacja interaktywna: GET /swagger-ui.html (OpenAPI 3)

## Autentykacja

API używa JWT Bearer Token.
Authorization: Bearer <JWT>

access_token — wydawany po pomyślnym logowaniu (podpis HS256)
Czas życia tokenu oraz logika odświeżania są obsługiwane w AuthService.
Odświeżanie: POST /api/auth/refresh

## Endpointy

### Auth — /api/auth

| Metoda | Ścieżka                 | Auth | Opis                                                        |
|--------|-------------------------|------|-------------------------------------------------------------|
| POST   | `/api/auth/register`    | —    | Rejestracja nowego użytkownika                              |
| POST   | `/api/auth/login`       | —    | Logowanie → zwraca token JWT                                |
| POST   | `/api/auth/refresh`     | JWT  | Odświeżenie tokenu dostępu                                  |
| PATCH  | `/api/auth/password`    | JWT  | Zmiana hasła (wymaga podania starego hasła)                 |

### Profile — /api/profile

| Metoda | Ścieżka                 | Auth | Opis                                                                   |
|--------|-------------------------|------|------------------------------------------------------------------------|
| GET    | `/api/profile`          | JWT  | Pobranie profilu obecnie zalogowanego użytkownika                      |
| PUT    | `/api/profile`          | JWT  | Aktualizacja profilu wraz z automatycznym przeliczeniem dziennego celu |

### Dziennik posiłków — /api/diary

| Metoda | Ścieżka                 | Auth | Opis                                                                |
|--------|-------------------------|------|---------------------------------------------------------------------|
| GET    | `/api/diary`            | JWT  | Pobranie wpisów posiłków na dany dzień                              |
| POST   | `/api/diary`            | JWT  | Dodanie nowego wpisu do dziennika                                   |
| PATCH  | `/api/diary/{id}`       | JWT  | Edycja ilości gramów wpisu (automatycznie przelicza kalorie)        |
| DELETE | `/api/diary/{id}`       | JWT  | Usunięcie wpisu posiłku                                             |
| GET    | `/api/diary/summary`    | JWT  | Pobranie dziennego podsumowania spożytych kalorii i makroskładników |

### Przepisy — /api/recipes

| Metoda | Ścieżka                          | Auth | Opis                                                                      |
|--------|----------------------------------|------|---------------------------------------------------------------------------|
| GET    | `/api/recipes`                   | JWT  | Wyszukiwanie publicznych przepisów                                        |
| GET    | `/api/recipes/mine`              | JWT  | Pobranie własnych przepisów (tylko zdefiniowanych przez autora zapytania) |
| GET    | `/api/recipes/favorites`         | JWT  | Pobranie ulubionych przepisów użytkownika                                 |
| POST   | `/api/recipes/{id}/favorite`     | JWT  | Dodanie przepisu o danym ID do ulubionych                                 |
| DELETE | `/api/recipes/{id}/favorite`     | JWT  | Usunięcie przepisu o danym ID z ulubionych                                |
| POST   | `/api/recipes`                   | JWT  | Dodanie (stworzenie) całkowicie nowego przepisu                           |
| PUT    | `/api/recipes/{id}`              | JWT  | Edycja przepisu (operacja dozwolona tylko dla jego autora)                |
| DELETE | `/api/recipes/{id}`              | JWT  | Usunięcie przepisu (operacja dozwolona tylko dla jego autora)             |

### Treningi — /api/workouts

| Metoda | Ścieżka                 | Auth | Opis                                                                     |
|--------|-------------------------|------|--------------------------------------------------------------------------|
| GET    | `/api/workouts`         | JWT  | Pobranie historii treningów na dany dzień (po dacie)                     |
| POST   | `/api/workouts`         | JWT  | Dodanie nowego treningu                                                  |
| PUT    | `/api/workouts/{id}`    | JWT  | Edycja istniejącego wpisu treningowego                                   |
| DELETE | `/api/workouts/{id}`    | JWT  | Usunięcie istniejącego wpisu treningowego                                |

## Kody błędów

Wszystkie błędy zwracane są w ujednoliconym formacie JSON obsługiwanym przez GlobalExceptionHandler.kt.

| Kod | Opis                                                                    |
|-----|-------------------------------------------------------------------------|
| 400 | Bad Request – Błędne dane wejściowe (np. błędy walidacji)               |
| 401 | Unauthorized – Brak lub nieważny token JWT                              |
| 403 | Forbidden – Brak uprawnień (np. edycja cudzego przepisu)                |
| 404 | Not Found – Zasób nie istnieje (np. przepis o podanym ID)               |
| 500 | Internal Server Error – Wewnętrzny błąd serwera                         |

Przykład odpowiedzi z błędem:
```json
{
  "timestamp": "2025-06-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Pole email jest wymagane"
}