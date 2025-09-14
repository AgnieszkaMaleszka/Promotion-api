package com.example.promotion_api.controller;

import com.example.promotion_api.model.ShopLeaflet;
import com.example.promotion_api.model.ShopLink;
import com.example.promotion_api.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List; 
import java.io.IOException;

// @RestController - klasa obsługująca zapytania RESTowe
//@CrossOrigin w Spring Boot odblokowuje dostęp do backendu z innych domen (originów), np. gdy frontend i backend są uruchomione na różnych portach lub serwerach.
@CrossOrigin(origins = "*")
@RestController
public class PromotionController {
    /*  wstrzykujemy serwis przez konstruktor 
        pole klasy - serwis  z logiką, final - nie można zmienić wartości po przepisaniu */
    private final PromotionService promotionService;

    /*
        konstruktor, wstrzykiwanie zależności (dependency injection)
        spring tworzy instancję PromotionService - przekazuje ją do kontrolera 
        kontroler może korzystać z metod serwisu 
        efekt = rozdzielenie logiki (kontroler -  obsługa zapytań, service - wyrzuca gazetki )
    */
    @Autowired
    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    // Endpoint HTTP GET pod adresem /leaflets 
    // Zwraca listę ulotek jako JSON
   /* @GetMapping("/leaflets")
    public List<ShopLeaflet> getLaLeaflets(){
        //pobieramy dane z serwisu 
        return promotionService.getLeaflets();
    }*/
    /**
     * Endpoint do pobrania gazetek dla danego sklepu i URL. 
     * Przykład wywołania: /api/promotions?shopName=Intermarche&url=https://...
     * 
     * @param ShopName nazwa sklepu
     * @param url URL do strony sklepu z gazetkami 
     * @return listagazetek w formacie JSON
     * @throws IOException w razie błędu pobierania/parsing
     */
    @GetMapping("/promotions")
    public List<ShopLeaflet> getPromotionalLeaflets(@RequestParam String shopName) throws IOException {
        return promotionService.findPromotionalLeaflet(shopName);
    }

    @GetMapping("/promotionsFirst")
    public List<ShopLeaflet> getPromotionalLeaflets_firstPage(@RequestParam String shopName) throws IOException {
        return promotionService.findPromotionalLeaflet_OnlyFirstPage(shopName);
    }
    @GetMapping("/shops")
    public List<ShopLink> getMainShopLinks() throws IOException {
        return promotionService.getCurrentOfferShopLinks();
    }

    @GetMapping("/leaflet/{id}/pages")
    public List<String> getLeafletPages(@PathVariable("id") long id) throws IOException {
        return promotionService.getLeafletPagesById(id);
    }

}
