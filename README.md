# Promotion API

Prosty backend w **Spring Boot**, który pobiera aktualne promocje i gazetki ze strony **blix.com/gazetki** dla wybranych sklepów. Projekt powstał w celach **edukacyjnych, niekomercyjnych** i jest wykorzystywany w aplikacji **Smart‑Cooking**.

> ℹDane pochodzą z publicznie dostępnych stron blix.com. API służy wyłącznie do nauki i testów.

---

## Szybki start

### Wymagania

* Java 17+
* Maven 3.9+

### Uruchomienie

```bash
mvn spring-boot:run
# domyślnie: http://localhost:8080
```

---

## Endpointy

> Bazowy URL: `http://localhost:8080`

### Lista sklepów

**GET** `/shops`

* Zwraca listę dostępnych sklepów i linków do ich ofert.

**Przykładowa odpowiedź (200):**

```json
[
  { "title": "Biedronka", "url": "https://blix.pl/sklep/biedronka" },
  { "title": "Lidl", "url": "https://blix.pl/sklep/lidl" }
]
```

---

### Gazetki dla sklepu (pełny crawl)

**GET** `/promotions?shopName={shopName}`

* Zwraca listę gazetek promocyjnych dla wskazanego sklepu. Filtry i dane pochodzą ze `findPromotionalLeaflet`.

**Przykładowa odpowiedź (200):**

```json
[
  {
    "shop": "Lidl",
    "url": "https://blix.pl/gazetka/123456",
    "image": "https://cdn.blix.pl/lidl/123456/cover.jpg",
    "validUntil": "aktualna",
    "pages": [
      "https://cdn.blix.pl/lidl/123456/page-1.jpg",
      "https://cdn.blix.pl/lidl/123456/page-2.jpg"
    ],
    "downloadDate": "2025-09-14"
  }
]
```

---

### Gazetki – tylko pierwsza strona wyników

**GET** `/promotionsFirst?shopName={shopName}`

* Szybsze zapytanie: zwraca listę gazetek, ale w polu `pages` znajduje się **wyłącznie pierwsza strona** (pierwszy URL) — implementacja `findPromotionalLeaflet_OnlyFirstPage`.

**Przykładowa odpowiedź (200):**

```json
[
  {
    "shop": "Biedronka",
    "url": "https://blix.pl/gazetka/654321",
    "image": "https://cdn.blix.pl/biedronka/654321/cover.jpg",
    "validUntil": "od dziś",
    "pages": [
      "https://cdn.blix.pl/biedronka/654321/page-1.jpg"
    ],
    "downloadDate": "2025-09-14"
  }
]
```

---

### Strony gazetki

**GET** `/leaflet/{id}/pages`

* Zwraca listę adresów URL obrazów stron wybranej gazetki; implementacja korzysta z selektorów `img[data-src|src]` i `srcset`, filtrując po `/{id}/`.

**Przykładowa odpowiedź (200):**

```json
[
  "https://cdn.blix.pl/lidl/123456/page-1.jpg",
  "https://cdn.blix.pl/lidl/123456/page-2.jpg",
  "https://cdn.blix.pl/lidl/123456/page-3.jpg"
]
```

---

## Obsługa błędów

* `400 Bad Request` – brak parametru `shopName` lub nieobsługiwany sklep
* `404 Not Found` – brak gazetki o podanym `id`
* `5xx` – błędy serwera

---

## Licencja

Projekt jest **edukacyjny, niekomercyjny**.
