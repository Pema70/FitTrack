
## Zakres MVP 

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
- [x] Ekran główny z paskiem postępu kcal i makro

### Przepisy
- [x] Przeglądanie i wyszukiwanie przepisów
- [x] Filtrowanie przepisów
- [x] Dodawanie własnego przepisu
- [x] Usuwanie własnych przepisów
- [x] Zakładka „Wszystkie przepisy", „Ulubione" i „Moje przepisy"

### Trening
- [x] Dodawanie treningu (typ aktywności + czas → automatyczne kcal)
- [x] Ręczne wpisanie spalonych kcal (np. ze smartwatcha)
- [x] Edycja i usuwanie wpisów treningowych

### Funkcje natywne
- [x] Camera API – dokumentowanie posiłków zdjęciem
- [x] WorkManager – powiadomienia push o nawodnieniu/treningu z wlasną treścią
 

---

