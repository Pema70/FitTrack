# Architektura FitTrack

## Stos technologiczny

### Frontend (Android)
| Technologia            | Zastosowanie                                      |
|------------------------|---------------------------------------------------|
| Kotlin                 | Główny język aplikacji                            |
| Jetpack Navigation     | Nawigacja między ekranami (main_nav_graph.xml)    |
| ViewModel + LiveData   | Wzorzec MVVM, zarządzanie stanem UI               |
| Retrofit + OkHttp      | Komunikacja z REST API, AuthInterceptor (JWT)     |
| Room (SQLite)          | Lokalna baza danych, tryb offline-first           |
| WorkManager            | Powiadomienia push (ReminderWorker)               |
| Camera API             | Dokumentowanie posiłków (FoodPhotoFragment)       |

### Backend
| Technologia            | Zastosowanie                                      |
|------------------------|---------------------------------------------------|
| Spring Boot 3.3.2      | Framework aplikacji backendowej                   |
| Kotlin 1.9             | Język backendu                                    |
| Spring Data JPA / Hibernate 6 | ORM, mapowanie encji                     |
| SQLite (dev) / PostgreSQL (prod) | Bazy danych                           |
| JWT / jjwt 0.12        | Autoryzacja (HS256), JwtAuthFilter.kt             |
| BCrypt (siła 10)       | Hashowanie haseł                                  |
| springdoc-openapi 2.6  | Generowanie dokumentacji OpenAPI                  |

## Wzorzec MVVM
View (Fragment/Activity)
│
▼
ViewModel ──► Repository
│ │
▼ ▼
Retrofit Room DB
(API) (Cache)


`Resource<T>` (stany: Loading / Success / Error) zapewnia spójną obsługę stanów UI.

## Tryb offline-first

Aplikacja monitoruje `NetworkCapabilities`. Przy braku połączenia dane są serwowane z lokalnej bazy Room (SQLite). Po przywróceniu połączenia następuje synchronizacja z serwerem.

## CI/CD

Pipeline zdefiniowany w `.github/workflows/ci.yml`:
- Build projektu
- Testy jednostkowe (JUnit / Mockk)
- Testy integracyjne z bazą H2 (in-memory)
- Weryfikacja pokrycia kodu ≥ 60% (JaCoCo)

Każdy Pull Request uruchamia pełny pipeline.

## Encje bazy danych

- `User` – konto użytkownika
- `DiaryEntry` – wpis posiłku (powiązany z User)
- `Workout` – wpis treningu (powiązany z User)
- `Recipe` – przepis (powiązany z autorem User)