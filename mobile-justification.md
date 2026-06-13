# Uzasadnienie wyboru platformy mobilnej

FitTrack to narzędzie osobiste, używane wielokrotnie w ciągu dnia w dynamicznych sytuacjach (restauracja, sklep, siłownia). Poniżej przedstawiono kluczowe powody wyboru Androida jako platformy docelowej.

## Mobilność

Użytkownik musi mieć możliwość natychmiastowego zapisania posiłku w dowolnym miejscu. Smartfon jest zawsze w zasięgu ręki – komputer stacjonarny lub przeglądarka internetowa nie zapewniają porównywalnej dostępności.

## Wizualny pamiętnik posiłków (Camera API)

Wbudowana kamera pozwala dokumentować posiłki zdjęciem. Jest to szczególnie przydatne dla dań bez kodów kreskowych (domowe obiady, restauracje). Zwiększa to zaangażowanie użytkowników i ułatwia późniejsze uzupełnianie danych kalorycznych.  
Implementacja: `FoodPhotoFragment` + `ActivityResultContracts.TakePicture()`.

## System powiadomień (WorkManager)

Urządzenie mobilne towarzyszy użytkownikowi przez cały dzień. Powiadomienia push z przypomnieniami o nawodnieniu (NotificationScheduler, ReminderWorker) są możliwe wyłącznie na platformie mobilnej – nawet gdy aplikacja działa w tle.

## Tryb offline-first (Room / SQLite)

Na siłowni w podziemiu, w górach, w metrze – aplikacja musi działać bez internetu. Room (SQLite) przechowuje dane lokalnie, a NetworkCapabilities automatycznie przełącza źródło danych.

## Podsumowanie

| Potrzeba użytkownika            | Funkcja mobilna                         |
|---------------------------------|-----------------------------------------|
| Szybkie logowanie posiłku       | Zawsze dostępna aplikacja natywna       |
| Dokumentowanie zdjęciem         | Camera API                              |
| Przypomnienia w tle             | WorkManager / Notifications             |
| Praca bez internetu             | Room offline-first                      |