# Figma Spec – FitTrack

## Ekrany aplikacji

### 1. Ekran logowania / rejestracji
- Pola tekstowe z walidacją w czasie rzeczywistym (format e-mail, min. 8 znaków hasła + cyfra)
- Przełącznik widoczności hasła
- Komunikaty błędów z API wyświetlane inline

### 2. Dziennik posiłków (Ekran główny)
- Karta podsumowująca na górze ekranu:
  - Zapełniający się poziomy pasek postępu kalorii (Progress Bar)
  - Licznik spożytych oraz pozostałych kalorii
  - Nawigacja datą: kalendarz / strzałki „poprzedni/następny dzień"
- Lista wpisów posiłków dla wybranego dnia
- Edycja wpisu (gramów) po kliknięciu → automatyczne przeliczenie kcal
- Pływający przycisk (FAB) do szybkiego dodawania nowego posiłku / zdjęcia

### 3. Przepisy
- Pole wyszukiwania tekstowego + filtry
- Lista wyników przepisów
- Zakładka „Wszystkie przepisy" , „Moje przepisy" oraz „Ulubione"
- Formularz dodawania przepisu (nazwa, składniki z gramaturą, opis krok po kroku, kcal, URL zdjęcia)

### 4. Trening
- Panel wyboru aktywności (bieganie, siłownia, rower, inne)
- Pole czasu trwania → automatyczne kcal
- Przełącznik „Własna wartość kcal" (np. ze smartwatcha)
- Lista treningów z opcją edycji / usunięcia

### 5. Profil
- Pola: imię, nazwisko, data urodzenia, waga, wzrost, płeć, tryb życia oraz cel wagowy.
- Wyświetlanie wyliczonego zapotrzebowania kalorycznego aby osiągnąć jeden z wybranych przez siebie celów (utrzymanie wagi, przyrost masy, schudnięcie)
- Przycisk „Wyloguj" (usuwa token JWT, przekierowanie na login)
- Przycisk „Ustawienia powiadomień" po jego kliknięciu wybieramy treść, godzine oraz typ powiadomienia (woda/trening)
- Przycisk „Zmień hasło" po podaniu starego hasła oraz podwójnym wpisaniu nowego.

## Nawigacja
Dolny pasek nawigacji (Bottom Navigation) z czterema zakładkami:
`Dziennik` | `Przepisy` | `Trening` | `Profil`

Graf nawigacji: `main_nav_graph.xml` (Jetpack Navigation Component)