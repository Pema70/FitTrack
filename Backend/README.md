# FitTrack Backend

Spring Boot 3 + Kotlin + JPA + SQLite + JWT.

## Wymagania
- JDK 17+
- (Gradle wrapper jest w repo — nie trzeba własnego Gradle)

## Uruchomienie

```bash
cd backend
./gradlew bootRun
```

Aplikacja startuje na **http://localhost:8080**. Plik bazy `fittrack.db` zostaje utworzony w katalogu uruchomienia.

### Z aplikacji Android (emulator)
Bazowy URL `http://10.0.2.2:8080/` jest już skonfigurowany w `android/app/build.gradle.kts` (`BuildConfig.API_BASE_URL`).

### Z urządzenia fizycznego
Zmień w `android/app/build.gradle.kts`:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://<ip-twojego-PC>:8080/\"")
```

## Endpointy

| Metoda | URL | Auth | Opis |
|---|---|---|---|
| POST | `/api/auth/register` | – | Rejestracja (zwraca JWT) |
| POST | `/api/auth/login` | – | Logowanie |
| POST | `/api/auth/refresh` | – | Odśwież access token |
| GET  | `/api/profile` | JWT | Profil użytkownika |
| PUT  | `/api/profile` | JWT | Aktualizacja profilu + kalkulacja kcal (Mifflin-St Jeor) |
| GET  | `/api/diary?date=YYYY-MM-DD` | JWT | Wpisy z dziennika |
| POST | `/api/diary` | JWT | Dodaj wpis |
| DELETE | `/api/diary/{id}` | JWT | Usuń wpis |
| GET  | `/api/diary/summary?date=...` | JWT | Dzienne kcal |
| GET  | `/api/recipes?q=...&tag=...` | – | Wyszukaj przepisy |
| POST | `/api/recipes` | JWT | Dodaj przepis |
| GET  | `/api/workouts?date=...` | JWT | Treningi z dnia |
| POST | `/api/workouts` | JWT | Zaloguj trening |
| POST | `/api/workouts/{id}/track` | JWT | Dodaj punkty GPS |

Swagger UI: **http://localhost:8080/swagger-ui.html**

## Co nie jest w głównym buildzie

- `NotificationService` (Firebase Cloud Messaging) — w `_external/notification/` z instrukcją włączenia.
- Test integracyjny `FitTrackIntegrationTest.kt.disabled` — wymaga dodatkowych seedów. Aby go włączyć, zmień rozszerzenie z powrotem na `.kt`.

## Konfiguracja

`src/main/resources/application.yml`:
- port 8080
- SQLite w pliku `fittrack.db`
- JWT: access 1 h, refresh 30 dni (secret w `fittrack.jwt.secret`)

## Czyszczenie bazy
```bash
rm backend/fittrack.db
```
Tabele zostaną odtworzone przy następnym starcie (`spring.jpa.hibernate.ddl-auto=update`).
