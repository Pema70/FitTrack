# User Stories — FitTrack

Poniżej znajduje się zestawienie wybranych historyjek użytkownika (User Stories) wraz z Kryteriami Akceptacji (KA) dla poszczególnych modułów aplikacji.

## Autoryzacja i Konto

* **Jako nowy użytkownik**, chcę założyć konto, podając adres e-mail oraz hasło, aby móc korzystać z personalizowanych funkcji aplikacji.
  > **Kryteria Akceptacji:** Formularz rejestracji wymusza walidację poprawności formatu e-mail oraz wymaga hasła o minimalnej długości 8 znaków zawierającego co najmniej jedną cyfrę. System sprawdza unikalność adresu e-mail w bazie danych. Po uzupełnieniu i walidacji danych tworzone jest nowe konto.

* **Jako użytkownik**, chcę się zalogować przy użyciu mojego adresu e-mail i hasła, aby bezpiecznie uzyskać dostęp do moich danych i historii posiłków.
  > **Kryteria Akceptacji:** Po poprawnym uwierzytelnieniu przez endpoint `/api/auth/login`, aplikacja otrzymuje token JWT i przekierowuje użytkownika na ekran główny. W przypadku błędnych danych użytkownik otrzymuje jasny komunikat o niepowodzeniu logowania.

* **Jako użytkownik**, chcę mieć możliwość edycji mojego hasła, aby dbać o bezpieczeństwo konta.
  > **Kryteria Akceptacji:** System wymaga podania starego hasła przed ustawieniem nowego. Nowe hasło musi spełniać te same wymogi co przy rejestracji.

* **Jako użytkownik**, chcę wylogować się z aplikacji, aby zabezpieczyć moje dane na urządzeniu współdzielonym.
  > **Kryteria Akceptacji:** Kliknięcie przycisku "Wyloguj" usuwa token JWT z pamięci urządzenia i przenosi użytkownika do ekranu logowania.

## Profil Użytkownika

* **Jako użytkownik**, chcę uzupełnić dane profilowe (waga, wzrost, wiek, płeć, cel sylwetkowy), aby system mógł precyzyjnie wyliczyć moje zapotrzebowanie kaloryczne.
  > **Kryteria Akceptacji:** Użytkownik wypełnia formularz profilowy, a po jego zapisaniu backend wykonuje automatyczne przeliczenie zapotrzebowania energetycznego (BMR). Zmienione dane są trwale zapisywane w profilu użytkownika.

* **Jako użytkownik**, chcę mieć możliwość wglądu w mój profil i edycji parametrów (np. zmiana aktualnej wagi), aby moje dzienne limity kalorii pozostawały aktualne.
  > **Kryteria Akceptacji:** Ekran profilu pobiera aktualne dane z endpointu `/api/profile`, a ich edycja wysyła aktualizację do backendu, co natychmiastowo przelicza statystyki na pulpicie głównym.

## Dziennik Posiłków

* **Jako użytkownik**, chcę edytować ilość zjedzonego produktu, jeśli wprowadziłem błędne dane.
  > **Kryteria Akceptacji:** Po kliknięciu na pozycję w dzienniku, użytkownik może zmienić liczbę gramów, co automatycznie przelicza kalorie w podsumowaniu dnia.

* **Jako użytkownik**, chcę widzieć historię moich posiłków z poprzednich dni, aby analizować regularność mojej diety.
  > **Kryteria Akceptacji:** Użytkownik może przełączać widok dziennika między datami za pomocą kalendarza lub strzałek "wczoraj/jutro".

##  Przepisy

* **Jako użytkownik**, chcę dodać przepis do "Ulubionych", aby mieć do niego szybszy dostęp w przyszłości.
  > **Kryteria Akceptacji:** Przepis oznaczony jako ulubiony pojawia się w dedykowanej zakładce w sekcji "Przepisy".

* **Jako użytkownik**, chcę móc dodać własny przepis do bazy, aby udostępnić autorskie danie lub zachować ulubiony sposób przygotowania posiłku.
  > **Kryteria Akceptacji:** Użytkownik wypełnia formularz zawierający: nazwę dania, listę składników (z gramaturą), opis przygotowania krok po kroku, szacowaną kaloryczność oraz opcjonalnie URL do zdjęcia.

* **Jako użytkownik**, chcę edytować lub usunąć stworzony przeze mnie przepis, aby skorygować ewentualne błędy lub zaktualizować dane o kaloriach.
  > **Kryteria Akceptacji:** Akcje "Edytuj" i "Usuń" są dostępne tylko dla przepisów, których autorem jest zalogowany użytkownik (weryfikacja własności po `user_id` na backendzie).

* **Jako użytkownik**, chcę przeglądać moje prywatne przepisy w osobnej zakładce, aby szybko odnaleźć dania, które sam dodałem do aplikacji.
  > **Kryteria Akceptacji:** Aplikacja posiada dedykowany widok "Moje przepisy", który pobiera rekordy przypisane do ID aktualnego użytkownika.

##  Treningi

* **Jako użytkownik**, chcę dodać wykonany trening, wybierając rodzaj czynności (np. bieganie, siłownia, rower) oraz czas jej trwania, aby aplikacja automatycznie przeliczyła spalone kalorie.
  > **Kryteria Akceptacji:** Po wybraniu dyscypliny i wpisaniu czasu trwania, system na podstawie wbudowanych mnożników aktywności wylicza i zapisuje wartość spalonych kalorii w dzienniku użytkownika.

* **Jako użytkownik**, chcę mieć możliwość ręcznego wpisania spalonych kalorii (np. odczytanych z mojego smartwatcha lub zegarka sportowego), aby zachować pełną precyzję danych w dzienniku.
  > **Kryteria Akceptacji:** Formularz dodawania treningu posiada przełącznik lub pole „Własna wartość kcal”, które pozwala pominąć automatyczne wyliczenia i wprowadzić gotową wartość z urządzenia zewnętrznego.

* **Jako użytkownik**, chcę edytować wprowadzone dane treningu (czas lub spalone kalorie), aby skorygować błędy w zapisie aktywności.
  > **Kryteria Akceptacji:** Użytkownik ma dostęp do listy swoich treningów, gdzie każda pozycja może zostać zmodyfikowana lub usunięta z bilansu dnia.