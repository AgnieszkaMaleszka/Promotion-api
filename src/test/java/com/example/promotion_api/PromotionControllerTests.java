package com.example.promotion_api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class PromotionControllerTests {

    @Test
    public void testImageUrlAccessibility() {
        String imageUrl = "https://img.blix.pl/api/52/440804/756013d632f0541bd77083776da223fa.jpg?bucket=400";
        assertTrue(isImageAccessible(imageUrl), "Obrazek powinien być dostępny");
    }

    public boolean isImageAccessible(String imageUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();
            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (IOException e) {
            return false;
        }
    }
}
