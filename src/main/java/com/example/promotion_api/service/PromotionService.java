package com.example.promotion_api.service;
// klasy gazetki
import com.example.promotion_api.model.ShopLeaflet;
import com.example.promotion_api.model.ShopLink;
import com.example.promotion_api.service.Utils;

import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/* 
 Trzymamy logikę biznesową. Zwracamy listę ulotek.
 Service - czyli klasa która jest serwisem =  miejsce z logiką biznesową aplikacji 
 logika biznesowa =  zasady, reguły, procesy odwzorowujące prawdziwe działania i potrzeby firmy 
 zasady, reguły (kiedy klient może otrzymać zniżkę itp)
 procesy operacyjne (przetwarzanie płayności, walidacja danych generowanie raportów)
 powiązania między komponentami 
 nie obsługują interfejsu użytkownika, nie zapisują danych bezpośrednio do bazy - bardziej przetwarzanie i łączenie informacji
*/
@Service
public class PromotionService {
    
    // Metoda zwracająca listę przykładowych ulotek 
  /*  public List<ShopLeaflet> getLeaflets(){
        return List.of(
            new ShopLeaflet("Intermarche", "https://example.com/leaflet1", "https://example.com/images/leaflet1.jpg", "2025-08-31", []),
            new ShopLeaflet("Lidl", "https://example.com/leaflet2", "https://example.com/images/leaflet2.jpg", "2025-09-05")
        );
    }
*/

    private final OkHttpClient client = new OkHttpClient();
    /**
     * Pobiera listę gazetek dla podanego sklepu.
     * @param shopName nazwa sklepu
     * @param url adres sklepu do pobrania danych
     * @return lista gazetek
     * @throws IOException w razie błędu pobierania 
     */
    public List<ShopLeaflet> findPromotionalLeaflet(String shopName) throws IOException {
        if (shopName == null || shopName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid shopName");
        }

        // Normalizacja nazwy
        if (shopName.equalsIgnoreCase("Intermarché")) {
            shopName = "Intermarche";
        }

        // Tworzymy URL do strony głównej sklepu - dynamicznie, np. https://shopName.pl
        String cleanedShopName = Utils.normalizeShopName(shopName);
        String baseUrl = "https://blix.pl/sklep/" + cleanedShopName;

        // Pobierz stronę główną sklepu
        Request request = new Request.Builder().url(baseUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP error: " + response);
            }

            String html = response.body().string();
            Document doc = Jsoup.parse(html);

            // Tu szukasz w dokumencie linków do gazetek np.
            Elements leafletLinks = doc.select("a[href*='gazetka'], a[href*='leaflet']");

            if (leafletLinks.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No leaflets found for this shop");
            }

            List<ShopLeaflet> result = new ArrayList<>();
            for (Element a : leafletLinks) {
                String link = a.attr("href"); // link do gazetki
                String image = ""; // link do obrazka
                String validUntil = ""; // data ważności

                Element img = a.selectFirst("img");
                if (img != null){
                    image = img.attr("data-src");
                }

                Element date = a.selectFirst("div.leaflet__availability  span.availability__label");
                if(date != null){
                    validUntil = date.text();
                }
                List<String> leafletsUrl = Utils.getLeafletsURL(link);
                //Dodajemy gazetkę do listy 
                LocalDate downloadDate = LocalDate.now();

                if (link.toLowerCase().contains(cleanedShopName.toLowerCase())) {  // filtrujemy po cleanedShopName
                    // dalsze parsowanie gazetki jeśli chcesz
                    result.add(new ShopLeaflet(shopName, link, image, validUntil, leafletsUrl, downloadDate));
                }
            }

            return result;
        }
    }

    public List<ShopLeaflet> findPromotionalLeaflet_OnlyFirstPage(String shopName) throws IOException {
        if (shopName == null || shopName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid shopName");
        }

        if (shopName.equalsIgnoreCase("Intermarché")) {
            shopName = "Intermarche";
        }

        String cleanedShopName = Utils.normalizeShopName(shopName); // np. "biedronka"
        String baseUrl = "https://blix.pl/sklep/" + cleanedShopName;

        Request request = new Request.Builder().url(baseUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP error: " + response);
            }

            String html = response.body().string();
            Document doc = Jsoup.parse(html);

            // 1) Znajdź sekcję z aktualnymi gazetkami dla brandu
            Element section = doc.selectFirst("section.other-leaflets-section");
            if (section == null) {
                // fallback: sekcja z nagłówkiem zawierającym "gazetki promocyjne"
                section = doc.selectFirst("section.section-n:has(h2:matchesOwn((?i)gazetki\\s+promocyjne))");
            }
            if (section == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No current leaflets section found for this shop");
            }

            // 2) Weź kafelki gazetek tylko z tej sekcji
            Elements leafletCards = section.select("div.leaflet.section-n__item");
            if (leafletCards.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No leaflets found for this shop in the current section");
            }

            List<ShopLeaflet> result = new ArrayList<>();
            LocalDate downloadDate = LocalDate.now();

            for (Element card : leafletCards) {
                // Filtr po sklepie – preferuj atrybut data-brand-slug
                String brandSlug = card.attr("data-brand-slug");
                if (brandSlug != null && !brandSlug.isBlank()
                        && !brandSlug.equalsIgnoreCase(cleanedShopName)) {
                    continue;
                }

                Element a = card.selectFirst("a.leaflet__link[href]");
                if (a == null) continue;

                String link = a.hasAttr("href") ? a.attr("href") : "";
                if (link.isEmpty()) continue;

                // Dodatkowy filtr po URL (gdyby data-brand-slug nie było)
                if (brandSlug == null || brandSlug.isBlank()) {
                    if (!link.toLowerCase().contains("/" + cleanedShopName.toLowerCase() + "/")) {
                        continue;
                    }
                }

                // Obrazek (odporny na lazy-load)
                Element img = a.selectFirst("picture img");
                String image = "";
                if (img != null) {
                    image = img.hasAttr("src") && !img.attr("src").isBlank()
                            ? img.attr("src")
                            : img.attr("data-src");
                }

                // Status/daty (etykieta pod okładką)
                String validUntil = "";
                Element label = a.selectFirst(".leaflet__availability .availability__label");
                if (label != null) {
                    validUntil = label.text(); // np. "od dziś" / "aktualna"
                }

                // 3) Pobierz listę stron i zostaw tylko PIERWSZĄ
                List<String> pages = Utils.getLeafletsURL(link);
                if (pages == null || pages.isEmpty()) {
                    // jeśli parser stron nic nie zwrócił – pomiń tę gazetkę
                    continue;
                }
                List<String> onlyFirstPage = Collections.singletonList(pages.get(0));

                result.add(new ShopLeaflet(
                        shopName,        // nazwa sklepu tak jak podał użytkownik (po korekcie Intermarché)
                        link,            // URL gazetki
                        image,           // okładka
                        validUntil,      // etykieta dostępności
                        onlyFirstPage,   // tylko pierwsza strona!
                        downloadDate
                ));
            }

            if (result.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No matching leaflets for this shop in the current section");
            }

            return result;
        }
    }

    // ze strony głównej blix.pl pobieramy listę sklepów widocznych w sekcji głównych marek, zwracamy jako listę obiektów

    public List<ShopLink> getCurrentOfferShopLinks() throws IOException {
        String baseUrl = "https://blix.pl/sklepy";
        Request request = new Request.Builder().url(baseUrl).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP error : " + response);
            }
            String html = response.body().string();
            Document doc = Jsoup.parse(html);

            Elements links = doc.select("div.section-n__items.section-n__items--brands > a");

            List<ShopLink> result = new ArrayList<>();
            for (Element a : links) {
                String title = a.attr("title");
                String href = a.attr("href");
                if (!href.startsWith("http")) {
                    href = "https://blix.pl" + href;
                }
                result.add(new ShopLink(title, href));
            }
            return result;
        }
    }
    // PromotionService.java (dopisz do klasy)
    public List<String> getLeafletPagesById(long leafletId) throws IOException {
        final String[] candidates = new String[] {
            "https://blix.pl/gazetka/" + leafletId,
            "https://blix.pl/leaflet/" + leafletId
        };

        // mały helper: pobierz pierwszy działający URL
        String workingUrl = null;
        for (String url : candidates) {
            Request head = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build();
            try (Response r = client.newCall(head).execute()) {
                if (r.isSuccessful() && r.body() != null) {
                    workingUrl = url;
                    break;
                }
            }
        }
        if (workingUrl == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Leaflet page not found for id=" + leafletId);
        }

        // pobieramy HTML i parsujemy obrazki stron
        Request get = new Request.Builder()
                .url(workingUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Accept-Language", "pl,en;q=0.8")
                .build();

        try (Response response = client.newCall(get).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("HTTP error: " + response);
            }
            String html = response.body().string();
            // ustawiamy baseUri, żeby absUrl działał
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html, workingUrl);

            // typowe miejsca na obrazki stron (img/source), filtrujemy po /{id}/
            String idNeedle = "/" + leafletId + "/";

            org.jsoup.select.Elements imgs = doc.select(
                    "img[src*='" + idNeedle + "'], img[data-src*='" + idNeedle + "'], " +
                    "source[srcset*='" + idNeedle + "']"
            );

            java.util.LinkedHashSet<String> pages = new java.util.LinkedHashSet<>();

            for (org.jsoup.nodes.Element el : imgs) {
                // 1) data-src
                String u = el.hasAttr("data-src") ? el.absUrl("data-src") : "";
                if (!u.isBlank() && u.contains(idNeedle)) pages.add(u);

                // 2) src
                if (u.isBlank() && el.hasAttr("src")) {
                    u = el.absUrl("src");
                    if (!u.isBlank() && u.contains(idNeedle)) pages.add(u);
                }

                // 3) srcset (może być wiele rozmiarów; bierzemy wszystkie warianty z tym ID)
                if (el.hasAttr("srcset")) {
                    String srcset = el.attr("srcset");
                    for (String part : srcset.split(",")) {
                        String candidate = part.trim().split("\\s+")[0]; // URL bez "1x"/"320w"
                        if (!candidate.isBlank()) {
                            String abs = resolveAbsUrl(doc.baseUri(), candidate);
                            if (!abs.isBlank() && abs.contains(idNeedle)) {
                                pages.add(abs);
                            }
                        }
                    }
                }
            }

            if (pages.isEmpty()) {
                // fallback: czasem viewer ładuje strony dynamicznie – wtedy zostawiamy jasny komunikat
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No page images found for leaflet id=" + leafletId);
            }

            return new java.util.ArrayList<>(pages);
        }
    }

    // mały helper do rozwiązywania URL względem baseUri
    private static String resolveAbsUrl(String baseUri, String relative) {
        try {
            return java.net.URI.create(baseUri).resolve(relative).toString();
        } catch (Exception e) {
            return relative;
        }
    }
}
