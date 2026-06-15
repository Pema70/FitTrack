# Architektura FitTrack

## Stos technologiczny

### Frontend (Android)
| Technologia | Zastosowanie |
|---|---|
| Kotlin | Główny język aplikacji |
| Jetpack Navigation | Nawigacja między ekranami (`main_nav_graph.xml`) |
| ViewModel + LiveData | Wzorzec MVVM, zarządzanie stanem UI |
| Retrofit + OkHttp | Komunikacja z REST API, `AuthInterceptor` dla JWT |
| WorkManager | Zadania cykliczne i przypomnienia (`ReminderWorker`) |
| Camera API | Dokumentowanie posiłków (`FoodPhotoFragment`) |

### Backend
| Technologia | Zastosowanie |
|---|---|
| Spring Boot 3.3.2 | Framework aplikacji backendowej |
| Kotlin 1.9 | Język backendu |
| Spring Data JPA / Hibernate 6 | ORM, mapowanie encji |
| PostgreSQL (prod) / H2 (test) | Bazy danych |
| JWT / jjwt 0.12 | Autoryzacja (HS256), `JwtAuthFilter.kt` |
| BCrypt (siła 10) | Hashowanie haseł |
| springdoc-openapi 2.6 | Generowanie dokumentacji OpenAPI |

### Hosting i środowiska

| Element | Opis |
|---|---|
| Platforma hostingowa | Railway (konfiguracja w `Backend/railway.toml`) |
| Środowisko produkcyjne | `https://fittrack-production-8b88.up.railway.app/api/` |
| Środowisko lokalne | `http://localhost:8080` (backend), `http://10.0.2.2:8080` (Android Emulator) |
| Baza danych produkcyjna | PostgreSQL zarządzany przez Railway |
| Konfiguracja środowiska | Zmienne środowiskowe i sekrety definiowane po stronie Railway |
| Strategia deploymentu | Build Gradle + start Spring Boot na Railway (bez Dockerfile, buildpack platformy) |

Backend jest wdrożony jako pojedyncza aplikacja Spring Boot uruchamiana na Railway, z osobnymi konfiguracjami dla środowiska lokalnego i produkcyjnego (PostgreSQL). Aplikacja Android komunikuje się z instancją produkcyjną za pomocą skonfigurowanej wartości base URL.

## Wzorzec MVVM

```text
View (Fragment/Activity)
│
▼
ViewModel ──► Repository
│            │
▼            ▼
Retrofit   (Brak Cache)
(API)
```

`Resource<T>` (`Loading / Success / Error`) zapewnia spójną obsługę stanów UI.

## CI/CD

Pipeline zdefiniowany w `.github/workflows/ci.yml`:
- Build projektu.
- Testy jednostkowe (JUnit / Mockk).
- Testy integracyjne z bazą H2 (in-memory).
- Weryfikacja pokrycia kodu ≥ 60% (JaCoCo).

Każdy Pull Request uruchamia pełny pipeline.

## Encje bazy danych

Struktura bazy danych opiera się na następujących encjach i relacjach:

- `User` — podstawowe konto użytkownika przechowujące dane autoryzacyjne (email, hasło) oraz datę rejestracji.
- `UserProfile` — profil powiązany z użytkownikiem relacją 1:1. Przechowuje dane fizyczne (waga, wzrost, wiek, płeć), poziom aktywności, cel dietetyczny oraz wyliczony dzienny limit kalorii.
- `FoodProduct` — katalog dostępnych produktów spożywczych, zawierający bazowe wartości makroskładników i kalorii w przeliczeniu na 100 g.
- `DiaryEntry` — wpis pojedynczego posiłku. Powiązany z użytkownikiem (`User`) oraz opcjonalnie ze słownikiem produktów (`FoodProduct`) lub przepisem. Przechowuje gramaturę, wyliczone na jej podstawie kalorie i makroskładniki, a także ścieżkę do opcjonalnego zdjęcia.
- `Recipe` — przepis kulinarny powiązany z użytkownikiem-autorem. Zawiera tytuł, opis przygotowania, czas, liczbę porcji, sumaryczne wartości odżywcze.
- `RecipeTag` - tabela przechowująca tagi przepisów połączona z tabelą `Recipe`.
- `RecipeIngredient` — tabela łącząca przepis (`Recipe`) z produktami (`FoodProduct`), określająca gramaturę danego składnika wymaganą w przepisie.
- `FavoriteRecipe` — tabela realizująca relację wiele-do-wielu pomiędzy użytkownikiem a przepisami, służąca do zapisywania ulubionych dań.
- `WorkoutActivity` — wpis dotyczący aktywności fizycznej (w bazie jako `workouts`). Powiązany z użytkownikiem, rejestruje typ aktywności, czas trwania oraz spalone kalorie.

