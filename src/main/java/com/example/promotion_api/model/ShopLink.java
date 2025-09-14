package com.example.promotion_api.model;
// reprezentuje sklep i link na blix.pl 
public class ShopLink {
    private String shopName;
    private String url;

    public ShopLink(String shopName, String url) {
        this.shopName = shopName;
        this.url = url;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}