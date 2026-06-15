# FitTrack

Aplikacja do śledzenia diety, treningów i aktywności fizycznej.  
Projekt składa się z dwóch niezależnych modułów: backendu REST API oraz aplikacji Android.
FitTrack/
├── Backend/ # Spring Boot 3 + Kotlin + JPA + PostgreSQL + JWT
├── Frontend/ # Aplikacja Android (Kotlin, Hilt, Retrofit, Navigation)
└── docs/ # Dokumentacja aplikacji (Opisy, zrzuty ekranu oraz wygląd makiety)


---

## Wymagania

| Narzędzie | Minimalna wersja |
|---|---|
| JDK | 17+ |
| PostgreSQL | dowolna aktualna |
| pgAdmin | opcjonalnie (do zarządzania bazą) |
| Android Studio | Hedgehog lub nowszy |
| Android SDK | compileSdk 34, minSdk 26 |

> Gradle Wrapper jest dołączony do obu modułów — nie trzeba instalować Gradle ręcznie.

---

## Backend

### Stos technologiczny

- Spring Boot 3 + Kotlin
- Spring Data JPA + Hibernate
- PostgreSQL
- Flyway (migracje schematu)
- JWT (access token 1 h, refresh token 30 dni)
- Swagger UI / OpenAPI (`Backend/openapi.json`)

### Konfiguracja bazy danych

1. Upewnij się, że serwer PostgreSQL działa.
2. Utwórz pustą bazę danych o nazwie `fittrack` (np. przez pgAdmin).
3. W razie potrzeby zaktualizuj dane połączenia w `Backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fittrack
    username: <twój_user>
    password: <twoje_hasło>
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

fittrack:
  jwt:
    secret: <twój_sekret>
```

### Uruchomienie

```bash
cd Backend
./gradlew bootRun
```

Przy pierwszym starcie Flyway automatycznie zastosuje wszystkie migracje i postawi strukturę tabel.  
Aplikacja startuje na **http://localhost:8080**.

Swagger UI dostępny pod: **http://localhost:8080/swagger-ui.html**

### Czyszczenie bazy (reset do stanu początkowego)

> ⚠️ Usuwa wszystkie dane!

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```

Wykonaj w pgAdmin → Query Tool na bazie `fittrack`, a następnie uruchom aplikację ponownie — Flyway ponownie zastosuje migracje.

### Wdrożenie (Railway)

Backend zawiera plik `Backend/railway.toml` skonfigurowany pod wdrożenie na platformie [Railway](https://railway.app).  
Aktualny produkcyjny URL API: `https://fittrack-production-8b88.up.railway.app/api/`

---

## Endpointy API

| Metoda | URL | Auth | Opis |
|---|---|---|---|
| POST | `/api/auth/register` | – | Rejestracja (zwraca JWT) |
| POST | `/api/auth/login` | – | Logowanie |
| POST | `/api/auth/refresh` | – | Odśwież access token |
| GET | `/api/profile` | JWT | Profil użytkownika |
| PUT | `/api/profile` | JWT | Aktualizacja profilu + kalkulacja kcal (Mifflin-St Jeor) |
| GET | `/api/diary?date=YYYY-MM-DD` | JWT | Wpisy z dziennika |
| POST | `/api/diary` | JWT | Dodaj wpis do dziennika |
| DELETE | `/api/diary/{id}` | JWT | Usuń wpis |
| GET | `/api/diary/summary?date=...` | JWT | Dzienne kcal |
| GET | `/api/recipes?q=...&tag=...` | – | Wyszukaj przepisy |
| POST | `/api/recipes` | JWT | Dodaj przepis |
| GET | `/api/workouts?date=...` | JWT | Treningi z dnia |
| POST | `/api/workouts` | JWT | Zaloguj trening |
| POST | `/api/workouts/{id}/track` | JWT | Dodaj punkty GPS do treningu |

Pełna specyfikacja OpenAPI: `Backend/openapi.json`

---

## Frontend (Android)

### Stos technologiczny

- Kotlin + Coroutines
- Hilt (Dependency Injection)
- Retrofit 2 + OkHttp + Moshi (komunikacja z API)
- Navigation Component + Safe Args
- ViewModel + LiveData (Lifecycle)
- DataStore Preferences (przechowywanie tokenów)
- WorkManager
- Glide (ładowanie obrazów)
- View Binding

### Uruchomienie

1. Otwórz folder `Frontend/` jako projekt w Android Studio.
2. Poczekaj na synchronizację Gradle.
3. Uruchom aplikację na emulatorze lub fizycznym urządzeniu.

### Konfiguracja URL backendu

Domyślnie aplikacja korzysta z produkcyjnego serwera na Railway.  
Aby przełączyć się na lokalny backend, edytuj `Frontend/app/build.gradle.kts`:

**Emulator AVD (lokalny backend):**
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080/api/\"")
```

**Fizyczne urządzenie (lokalny backend):**
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://<ip-twojego-PC>:8080/api/\"")
```

**Produkcja (Railway) — domyślne:**
```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://fittrack-production-8b88.up.railway.app/api/\"")
```


---

## Testy

### Backend
```bash
cd Backend
./gradlew test
```

### Frontend (jednostkowe)
```bash
cd Frontend
./gradlew test
```

### Frontend (UI / Espresso)
```bash
cd Frontend
./gradlew connectedAndroidTest
```