# Figma Spec – FitTrack

> Plik roboczy Figma: *[link do uzupełnienia przez zespół]*

## Ekrany aplikacji

### 1. Ekran logowania / rejestracji
- Pola tekstowe z walidacją w czasie rzeczywistym (format e-mail, min. 8 znaków hasła + cyfra)
- Przełącznik widoczności hasła
- Komunikaty błędów z API wyświetlane inline

### 2. Ekran główny (Dashboard)
- Kołowy wskaźnik postępu kalorii (Progress Bar)
- Podsumowanie makroskładników: Białko / Tłuszcze / Węglowodany
- Szybki dostęp do dodania posiłku lub treningu

### 3. Dziennik posiłków
- Lista wpisów dla wybranego dnia
- Nawigacja datą: kalendarz + strzałki „wczoraj/jutro"
- Edycja gramów → automatyczne przeliczenie kcal

### 4. Przepisy
- Pole wyszukiwania tekstowego + filtry
- Lista wyników przepisów
- Zakładka „Moje przepisy" i „Ulubione"
- Formularz dodawania przepisu (nazwa, składniki z gramaturą, opis krok po kroku, kcal, URL zdjęcia)

### 5. Trening
- Panel wyboru aktywności (bieganie, siłownia, rower, inne)
- Pole czasu trwania → automatyczne kcal
- Przełącznik „Własna wartość kcal" (np. ze smartwatcha)
- Lista treningów z opcją edycji / usunięcia

### 6. Profil
- Pola: imię, nazwisko, data urodzenia, waga, wzrost, płeć, cel wagowy
- Wyświetlanie wyliczonego BMR i TDEE
- Przycisk „Wyloguj" (usuwa token JWT, przekierowanie na login)

## Nawigacja
Dolny pasek nawigacji (Bottom Navigation) z czterema zakładkami:
`Dziennik` | `Przepisy` | `Trening` | `Profil`

Graf nawigacji: `main_nav_graph.xml` (Jetpack Navigation Component)

