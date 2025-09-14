package com.example.promotion_api.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ImageDownloader {

    public static String downloadImage(String imageUrl, String saveDir, String fileName) {
        try (InputStream in = URI.create(imageUrl).toURL().openStream()) {
            Files.createDirectories(Paths.get(saveDir));
            String fullPath = saveDir + "/" + fileName;
            Files.copy(in, Paths.get(fullPath), StandardCopyOption.REPLACE_EXISTING);
            return fullPath;
        } catch (IOException e) {
            System.out.println("Błąd pobierania obrazu: " + e.getMessage());
            return null;
        }
    }
}
