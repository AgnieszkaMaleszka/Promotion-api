package com.example.promotion_api.service;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.HashSet;
import java.text.Normalizer;

public class Utils {
    private static final OkHttpClient client = new OkHttpClient();

    public static List<String> getLeafletsURL(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing or invalid parameter URL");
        }

        List<String> result = new ArrayList<>();
        Set<String> uniqueImages = new HashSet<>();
        int iterator = 1;

        while (true) {
            String presentURL = baseUrl + "?pageNumber=" + iterator;
            Request request = new Request.Builder().url(presentURL).build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    break;
                }

                String html = response.body().string();
                Document doc = Jsoup.parse(html);

                Elements imgElements = doc.select("img.page-img");
                System.out.println("Strona " + iterator + ": znaleziono " + imgElements.size() + " obrazków");

                int addedCount = 0;

                for (Element img : imgElements) {
                    String src = img.attr("data-src");
                    if (src == null || src.isBlank()) {
                        src = img.attr("src");
                    }
                    if (src != null && !src.isBlank() && uniqueImages.add(src)) {
                        result.add(src);
                        addedCount++;
                        System.out.println("Dodano: " + src);
                    }
                }

                // Jeśli na tej stronie nie dodano nic nowego – przerwij pętlę
                if (addedCount == 0) {
                    System.out.println("Brak nowych obrazków na stronie " + iterator + ", kończę.");
                    break;
                }

            } catch (IOException e) {
                throw new RuntimeException("Failed to fetch leaflet page: " + presentURL, e);
            }

            iterator++;
        }

        return result;
    }
    public static String normalizeShopName(String shopName) {
        // Zamiana polskich znaków na podstawowe (ą -> a itd.)
        String normalized = Normalizer.normalize(shopName, Normalizer.Form.NFD)
                            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Zamiana spacji i znaków innych niż litery i cyfry na myślnik
        normalized = normalized.toLowerCase().replaceAll("[^a-z0-9]+", "-");

        // Usuwanie ewentualnych myślników na początku i końcu
        normalized = normalized.replaceAll("^-|-$", "");

        return normalized;
    }
}
