# Dokumentacja Testów – FitTrack

Aplikacja FitTrack wykorzystuje zautomatyzowane testy zarówno po stronie serwera (Backend),
jak i klienta mobilnego (Frontend), co zapewnia stabilność logiki biznesowej, bezpieczeństwo
API oraz poprawne działanie interfejsu użytkownika.

---

## 1. Testy Backend (Spring Boot)

Backend jest w pełni pokryty testami automatycznymi. Testy te są zintegrowane z potokiem
CI/CD (GitHub Actions) i uruchamiają się automatycznie przy każdym Pull Request.

### Stos technologiczny

- **Framework testowy:** JUnit 5
- **Mockowanie:** Mockk / Mockito
- **Testy integracyjne:** Spring Boot Test, MockMvc
- **Baza danych do testów:** H2 (in-memory) skonfigurowana w `application-test.yml`

### Rodzaje testów

#### Testy Jednostkowe (Unit Tests)

Skupiają się na izolowanym testowaniu logiki biznesowej w serwisach oraz klasach
pomocniczych. Zależności (np. repozytoria) są mockowane, aby testy wykonywały się
błyskawicznie i nie zależały od bazy danych.

**Kluczowe pliki:**

- `JwtUtilsTest.kt` – weryfikacja poprawnego generowania, walidacji oraz wygasania tokenów JWT.
- `AuthServiceTest.kt` / `ProfileServiceTest.kt` – testy logiki rejestracji, hashowania
  haseł i aktualizacji danych.
- `DiaryServiceTest.kt` / `WorkoutServiceTest.kt` / `RecipeServiceTest.kt` – weryfikacja
  automatycznego przeliczania kalorii, dodawania i usuwania wpisów.

#### Testy Integracyjne (Integration Tests)

Weryfikują poprawne działanie całych ścieżek w aplikacji – od odebrania żądania HTTP
(kontroler), przez logikę biznesową (serwis), aż po zapis do bazy danych.

**Kluczowe pliki:**

- `ApiIntegrationTest.kt` – kompleksowe testy endpointów REST API przy użyciu `MockMvc`.
  Baza danych używana podczas testów to lekka baza w pamięci (H2), co gwarantuje izolację
  od środowiska deweloperskiego i produkcyjnego.

### Uruchamianie testów

Aby uruchomić wszystkie testy backendowe lokalnie, wykonaj w katalogu głównym backendu polecenie:

```bash
./gradlew test
```

---

## 2. Testy Frontend (Android)

Aplikacja mobilna wykorzystuje podejście testowania warstwowego. Testy dzielą się na
jednostkowe (uruchamiane lokalnie na maszynie JVM) oraz instrumentacyjne (wymagające
emulatora lub fizycznego urządzenia z systemem Android).

### Stos technologiczny

- **Framework testowy:** JUnit 4
- **Mockowanie:** Mockito, kotlinx-coroutines-test
- **Testy UI:** Espresso
- **Dependency Injection:** Hilt Testing (HiltTestRunner, HiltTestActivity)

### Rodzaje testów

#### Testy Jednostkowe (Local Unit Tests)

Znajdują się w katalogu `app/src/test/`. Nie wymagają środowiska Androida. Ich głównym
celem jest weryfikacja logiki prezentacji, formatowania danych oraz poprawności działania
ViewModeli. Zależności sieciowe i lokalne bazy danych są w nich mockowane.

**Kluczowe pliki:**

- `LoginViewModelTest.kt` – testy weryfikujące stany logowania, walidację danych wejściowych
  i zarządzanie błędami z API.
- `ProfileViewModelTest.kt` – testy aktualizacji danych profilowych.

#### Testy Instrumentacyjne / UI (Instrumented Tests)

Znajdują się w katalogu `app/src/androidTest/`. Wymagają uruchomienia na emulatorze lub
prawdziwym urządzeniu. Wykorzystują bibliotekę Espresso do symulacji kliknięć i weryfikacji
widoczności elementów na ekranie. Architektura testów UI jest wspierana przez dedykowany
`HiltTestRunner`, który wstrzykuje testowe zależności.

**Kluczowe pliki:**

- `LoginFragmentTest.kt` – weryfikacja interfejsu logowania, wyświetlania błędów oraz
  poprawnej nawigacji.
- `ProfileFragmentTest.kt` – weryfikacja poprawnego wczytywania danych użytkownika na ekran.
- Narzędzia pomocnicze: `EspressoUtils.kt`, `HiltFragmentFactory.kt`.

### Uruchamianie testów

Aby uruchomić testy jednostkowe (szybkie, bez emulatora), wykonaj w katalogu frontendu polecenie:

```bash
./gradlew testDebugUnitTest
```

Aby uruchomić testy UI / instrumentacyjne (wymaga włączonego emulatora), wykonaj
w katalogu frontendu polecenie:

```bash
./gradlew connectedAndroidTest
```