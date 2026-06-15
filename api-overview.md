
- `access_token` — wydawany po pomyślnym logowaniu, czas życia konfigurowany w `AuthService`
- `refresh_token` — przekazywany w body żądania `POST /api/auth/refresh`, służy do uzyskania nowego `access_token` bez ponownego logowania
- Endpointy nieoznaczone jako `JWT` są publiczne (nie wymagają nagłówka `Authorization`)

---

## Endpointy

### Auth — `/api/auth`

| Metoda | Ścieżka              | Auth | Opis                                                    |
|--------|----------------------|------|---------------------------------------------------------|
| POST   | `/api/auth/register` | —    | Rejestracja nowego użytkownika                          |
| POST   | `/api/auth/login`    | —    | Logowanie → zwraca `access_token` i `refresh_token`     |
| POST   | `/api/auth/refresh`  | —    | Odświeżenie tokenu dostępu na podstawie `refresh_token` |
| PATCH  | `/api/auth/password` | JWT  | Zmiana hasła (wymaga podania aktualnego hasła)          |

**Przykład — rejestracja:**
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Secret123!",
  "name": "Jan Kowalski"
}
```

**Odpowiedź (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

**Przykład — odświeżenie tokenu:**
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2g..."
}
```

---

### Profile — `/api/profile`

| Metoda | Ścieżka        | Auth | Opis                                                                        |
|--------|----------------|------|-----------------------------------------------------------------------------|
| GET    | `/api/profile` | JWT  | Pobranie profilu aktualnie zalogowanego użytkownika                         |
| PUT    | `/api/profile` | JWT  | Aktualizacja profilu z automatycznym przeliczeniem dziennego celu kalorii   |

**Przykład — aktualizacja profilu:**
```http
PUT /api/profile
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "Jan Kowalski",
  "age": 28,
  "weight": 75.5,
  "height": 180,
  "goal": "WEIGHT_LOSS"
}
```

---

### Dziennik posiłków — `/api/diary`

Parametr `date` we wszystkich endpointach przyjmuje format **`YYYY-MM-DD`** (np. `date=2025-06-01`).

| Metoda | Ścieżka             | Auth | Opis                                                                        |
|--------|---------------------|------|-----------------------------------------------------------------------------|
| GET    | `/api/diary`        | JWT  | Pobranie wpisów posiłków na dany dzień (`?date=YYYY-MM-DD`)                 |
| POST   | `/api/diary`        | JWT  | Dodanie nowego wpisu do dziennika                                           |
| PATCH  | `/api/diary/{id}`   | JWT  | Edycja ilości gramów wpisu (automatyczne przeliczenie kalorii i makro)      |
| DELETE | `/api/diary/{id}`   | JWT  | Usunięcie wpisu posiłku                                                     |
| GET    | `/api/diary/summary`| JWT  | Dziennie podsumowanie kalorii i makroskładników (`?date=YYYY-MM-DD`)        |

**Przykład — dodanie wpisu:**
```http
POST /api/diary
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "productId": 42,
  "grams": 150,
  "mealType": "BREAKFAST",
  "date": "2025-06-01"
}
```

**Przykład odpowiedzi — summary:**
```json
{
  "date": "2025-06-01",
  "totalCalories": 1850,
  "protein": 95.5,
  "carbs": 210.0,
  "fat": 62.3,
  "dailyGoal": 2000
}
```

---

### Produkty spożywcze — `/api/products`

| Metoda | Ścieżka        | Auth | Opis                                                         |
|--------|----------------|------|--------------------------------------------------------------|
| GET    | `/api/products`| JWT  | Wyszukiwanie produktów spożywczych (`?q=<fraza>`)            |

- Parametr `q` — fraza wyszukiwania (np. `?q=kurczak`); wyszukiwanie pełnotekstowe po nazwie produktu
- Zwraca listę produktów z wartościami odżywczymi na 100 g

**Przykład:**
```http
GET /api/products?q=kurczak
Authorization: Bearer <access_token>
```

**Przykład odpowiedzi:**
```json
[
  {
    "id": 42,
    "name": "Kurczak pieczony",
    "calories": 165,
    "protein": 31.0,
    "carbs": 0.0,
    "fat": 3.6
  }
]
```

---

### Przepisy — `/api/recipes`

| Metoda | Ścieżka                      | Auth | Opis                                                                   |
|--------|------------------------------|------|------------------------------------------------------------------------|
| GET    | `/api/recipes`               | JWT  | Wyszukiwanie publicznych przepisów (`?q=<fraza>&tag=<tag>`)            |
| GET    | `/api/recipes/mine`          | JWT  | Pobranie własnych przepisów zalogowanego użytkownika                   |
| GET    | `/api/recipes/favorites`     | JWT  | Pobranie ulubionych przepisów użytkownika                              |
| POST   | `/api/recipes`               | JWT  | Dodanie nowego przepisu                                                |
| PUT    | `/api/recipes/{id}`          | JWT  | Edycja przepisu (tylko autor)                                          |
| DELETE | `/api/recipes/{id}`          | JWT  | Usunięcie przepisu (tylko autor)                                       |
| POST   | `/api/recipes/{id}/favorite` | JWT  | Dodanie przepisu do ulubionych                                         |
| DELETE | `/api/recipes/{id}/favorite` | JWT  | Usunięcie przepisu z ulubionych                                        |

**Parametry wyszukiwania (`GET /api/recipes`):**
- `q` — fraza wyszukiwania pełnotekstowego po nazwie przepisu (opcjonalny)
- `tag` — filtr po tagu (opcjonalny); przykładowe wartości: `HIGH_PROTEIN`, `LOW_CARB`, `VEGAN`, `VEGETARIAN`, `QUICK`

**Przykład — dodanie przepisu:**
```http
POST /api/recipes
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "Omlet z warzywami",
  "description": "Szybkie śniadanie bogate w białko",
  "tag": "HIGH_PROTEIN",
  "ingredients": [
    { "productId": 10, "grams": 200 },
    { "productId": 55, "grams": 50 }
  ]
}
```

---

### Treningi — `/api/workouts`

Parametr `date` przyjmuje format **`YYYY-MM-DD`** (np. `date=2025-06-01`).

| Metoda | Ścieżka               | Auth | Opis                                                         |
|--------|-----------------------|------|--------------------------------------------------------------|
| GET    | `/api/workouts`       | JWT  | Pobranie historii treningów na dany dzień (`?date=YYYY-MM-DD`) |
| POST   | `/api/workouts`       | JWT  | Dodanie nowego treningu                                      |
| PUT    | `/api/workouts/{id}`  | JWT  | Edycja istniejącego wpisu treningowego                       |
| DELETE | `/api/workouts/{id}`  | JWT  | Usunięcie istniejącego wpisu treningowego                    |

**Przykład — dodanie treningu:**
```http
POST /api/workouts
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "name": "Bieganie",
  "durationMinutes": 45,
  "caloriesBurned": 380,
  "date": "2025-06-01"
}
```

---

## Kody błędów

Wszystkie błędy zwracane są w ujednoliconym formacie JSON obsługiwanym przez `GlobalExceptionHandler`.

| Kod | Opis                                                                     |
|-----|--------------------------------------------------------------------------|
| 400 | Bad Request — błędne dane wejściowe lub błędy walidacji pól              |
| 401 | Unauthorized — brak, nieważny lub wygasły token JWT                      |
| 403 | Forbidden — brak uprawnień (np. edycja cudzego przepisu)                 |
| 404 | Not Found — zasób nie istnieje (np. przepis o podanym ID)                |
| 500 | Internal Server Error — wewnętrzny błąd serwera                          |

**Format odpowiedzi z błędem:**
```json
{
  "timestamp": "2025-06-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Pole email jest wymagane"
}
```

---

## Znane ograniczenia

- Brak obsługi offline po stronie aplikacji — wszystkie dane pobierane są w czasie rzeczywistym z API (brak lokalnego cache Room).
- Endpoint `GET /api/products` zwraca wyniki z bazy wewnętrznej; integracja z zewnętrznym źródłem danych (katalog `Backend/_external/`) może nie być kompletna.
- Czas życia `access_token` i `refresh_token` nie jest publicznie udokumentowany — do weryfikacji w `AuthService`.