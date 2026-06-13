# MVP vs. Funkcje Dodatkowe

## Zakres MVP (Minimum Viable Product)

Poniższe funkcje stanowią rdzeń aplikacji i są wymagane do pierwszego wydania.

### Autoryzacja i konto
- [x] Rejestracja (e-mail + hasło, walidacja formatu i siły hasła)
- [x] Logowanie (JWT, przekierowanie na ekran główny)
- [x] Wylogowanie (usunięcie tokenu JWT z pamięci urządzenia)
- [x] Edycja hasła (weryfikacja starego hasła)

### Profil użytkownika
- [x] Uzupełnienie danych (waga, wzrost, wiek, płeć, cel sylwetkowy)
- [x] Automatyczne obliczanie BMR/TDEE (wzór Mifflina–St Jeora)
- [x] Edycja wagi i pozostałych parametrów

### Dziennik posiłków
- [x] Dodawanie posiłku z przeliczaniem kalorii i makroskładników
- [x] Edycja gramów → automatyczne przeliczenie kcal
- [x] Usuwanie wpisu
- [x] Historia posiłków (nawigacja po datach: kalendarz, strzałki)
- [x] Ekran główny z kołowym wskaźnikiem kcal i makro

### Przepisy
- [x] Przeglądanie i wyszukiwanie przepisów
- [x] Filtrowanie przepisów
- [x] Dodawanie własnego przepisu
- [x] Edycja i usuwanie własnych przepisów
- [x] Zakładka „Ulubione" i „Moje przepisy"

### Trening
- [x] Dodawanie treningu (typ aktywności + czas → automatyczne kcal)
- [x] Ręczne wpisanie spalonych kcal (np. ze smartwatcha)
- [x] Edycja i usuwanie wpisów treningowych

### Funkcje natywne
- [x] Camera API – dokumentowanie posiłków zdjęciem
- [x] WorkManager – powiadomienia push o nawodnieniu
- [x] Tryb offline-first (Room/SQLite + NetworkCapabilities)

---

## Funkcje Dodatkowe (poza MVP)

Poniższe funkcje mogą zostać zrealizowane po dostarczeniu MVP, o ile pozwoli na to czas.

| Funkcja                                          | Priorytet | Uwagi                                      |
|--------------------------------------------------|-----------|--------------------------------------------|
| Skanowanie kodów kreskowych produktów            | Wysoki    | Integracja z zewnętrzną bazą żywności      |
| Integracja z Google Fit / Health Connect         | Średni    | Import treningów i kroków                  |
| Wykresy postępów (waga, kalorie w czasie)        | Średni    | Widok statystyk tygodniowych/miesięcznych  |
| Udostępnianie przepisów innym użytkownikom       | Niski     | Widoczność publiczna vs. prywatna          |
| Tryb ciemny (Dark Mode)                          | Niski     | Systemowe ustawienie Android               |
| Eksport danych do CSV/PDF                        | Niski     | Na żądanie użytkownika                     |
| Widget na ekran główny (dzienne kcal)            | Niski     | AppWidget API                              |