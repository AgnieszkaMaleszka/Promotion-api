package com.example.promotion_api.model;

import java.util.List;
import java.time.LocalDate;
// Model reprezentujący gazetkę sklepu
public class ShopLeaflet {
    private String shopName;
    private String leafletLink;
    private String imageUrl;
    private String validUntil;
    private String localImagePath;
    private List<String> leafletsUrl;
    private LocalDate downloadDate;
    // Konstruktor tworzący obiekt ze wszystkimi polami
    public ShopLeaflet(String shopName, String leafletLink, String imageUrl, String validUntil, List<String> leafletsUrl, LocalDate downloadDate) {
        this.shopName = shopName;
        this.leafletLink = leafletLink;
        this.imageUrl = imageUrl;
        this.validUntil = validUntil;
        this.leafletsUrl = leafletsUrl;
        this.downloadDate = downloadDate;
    }

    // Getter dla nazwy sklepu
    public String getShopName() {
        return shopName;
    }

    // Setter dla nazwy sklepu — ustawia nową wartość
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    // Getter dla linku do gazetki
    public String getLeafletLink() {
        return leafletLink;
    }

    // Setter dla linku do gazetki
    public void setLeafletLink(String leafletLink) {
        this.leafletLink = leafletLink;
    }

    // Getter dla URL obrazka gazetki
    public String getImageUrl() {
        return imageUrl;
    }

    // Setter dla URL obrazka gazetki
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Getter dla daty ważności gazetki
    public String getValidUntil() {
        return validUntil;
    }

    // Setter dla daty ważności gazetki
    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }
    public String getLocalImagePath() {
        return localImagePath;
    }

    public void setLocalImagePath(String localImagePath) {
        this.localImagePath = localImagePath;
    }

    public List<String> getLeafletsURL() {
        return leafletsUrl;
    }

    public void setLeafletsURL(List<String> leafletsUrl) {
        this.leafletsUrl = leafletsUrl;
    }
    public LocalDate  getDownloadDate() {
        return downloadDate;
    }

    public void setLeafletsURL(LocalDate downloadDate) {
        this.downloadDate = downloadDate;
    }
}
