package com.example.promotion_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Klasa obsługuje żądania HTTP (GET POST itp) i zwraca dane (JSON lub tekst)
@RestController
public class HelloController {
    // @GetMapping definiuje endpoint HTTP GET pod ścieżką "/"
    // Gdy użytkownik odwiedza http://localhost:8080/ metoda zostanie wywołana
    @GetMapping("/")
    public String hello(){
        return "API is running. Try /shops or /promotions.";
    }
}
