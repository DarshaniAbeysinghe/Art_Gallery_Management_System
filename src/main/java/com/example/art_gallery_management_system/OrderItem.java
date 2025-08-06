package com.example.art_gallery_management_system;
public class OrderItem {

    private int artworkId;
    private String artworkName;
    private int quantity;
    private double price;

    public OrderItem(int artworkId, String artworkName, int quantity, double price) {
        this.artworkId = artworkId;
        this.artworkName = artworkName;
        this.quantity = quantity;
        this.price = price;
    }


    public int getArtworkId() {
        return artworkId;
    }

    public void setArtworkId(int artworkId) {
        this.artworkId = artworkId;
    }

    public String getArtworkName() {
        return artworkName;
    }

    public void setArtworkName(String artworkName) {
        this.artworkName = artworkName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
