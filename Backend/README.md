# FitTrack Backend

Spring Boot 3 + Kotlin + JPA + PostgreSQL + JWT.

## Wymagania
-JDK 17+
-Zainstalowany PostgreSQL oraz narzędzie pgAdmin
-Utworzona czysta baza danych w PostgreSQL o nazwie fittrack
-(Gradle wrapper jest w repo — nie trzeba własnego Gradle)

## Uruchomienie

1. Upewnij się, że Twój serwer PostgreSQL działa.
2. Otwórz pgAdmin i utwórz bazę danych o nazwie fittrack.
3. W razie potrzeby zaktualizuj dane logowania do bazy (host, user, password) w pliku src/main/resources/application.yml.
4. Uruchom aplikację:
```bash
cd backend
./gradlew bootRun
```

Podczas pierwszego startu Flyway automatycznie utworzy historię migracji i postawi strukturę tabel na podstawie skryptów migracyjnych. Aplikacja startuje na 
http://localhost:8080.

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

`src/main/resources/application.yml:`
port: 8080
database: PostgreSQL (domyślnie jdbc:postgresql://localhost:5432/fittrack)
orm: Hibernate z walidacją struktury bazy (spring.jpa.hibernate.ddl-auto=validate)
migration: Flyway automatycznie zarządzający schematem bazy danych
JWT: access 1 h, refresh 30 dni (secret w fittrack.jwt.secret)

## Czyszczenie bazy
1. W środowisku produkcyjnym/deweloperskim z PostgreSQL nie usuwamy pliku bazy danych. Aby wyczyścić bazę do stanu początkowego:

2. Otwórz pgAdmin.

3. Kliknij prawym przyciskiem myszy na bazę fittrack i wybierz Query Tool.

4. Wykonaj polecenie czyszczące schemat (uwaga: usuwa wszystkie dane!):
```SQL
    DROP SCHEMA public CASCADE;
    CREATE SCHEMA public;
```

5. Uruchom aplikację ponownie – Flyway automatycznie zaaplikuje wszystkie migracje strukturalne (np. V1__init_schema.sql) na czystym schemacie.