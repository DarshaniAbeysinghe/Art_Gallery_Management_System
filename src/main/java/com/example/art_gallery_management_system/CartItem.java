package com.example.art_gallery_management_system;

import javafx.beans.property.*;

import javafx.scene.control.Button;

public class CartItem {
    private SimpleIntegerProperty artworkId;
    private SimpleStringProperty name;
    private SimpleDoubleProperty price;
    private SimpleIntegerProperty quantity;
    private SimpleDoubleProperty totalPrice;

    private SimpleObjectProperty<Button> removeButton;




    public CartItem(int artworkId, String name, double price, int quantity) {
        this.artworkId = new SimpleIntegerProperty(artworkId);
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.totalPrice = new SimpleDoubleProperty(price * quantity);


        Button button = new Button("Remove");
        button.setOnAction(e -> remove());
        this.removeButton = new SimpleObjectProperty<>(button);


        this.totalPrice.bind(this.price.multiply(this.quantity));
    }


    public int getArtworkId() {
        return artworkId.get();
    }

    public SimpleIntegerProperty artworkIdProperty() {
        return artworkId;
    }

    public void setArtworkId(int artworkId) {
        this.artworkId.set(artworkId);
    }

    // Getters and Setters for name
    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }


    public double getPrice() {
        return price.get();
    }

    public SimpleDoubleProperty priceProperty() {
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }


    public int getQuantity() {
        return quantity.get();
    }

    public SimpleIntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity >= 0) {
            this.quantity.set(quantity);
        }
    }


    public double getTotalPrice() {
        return totalPrice.get();
    }

    public SimpleDoubleProperty totalPriceProperty() {
        return totalPrice;
    }

    // Method to remove the item
    public void remove() {
        this.quantity.set(0);
    }


    public Button getRemoveButton() {
        return removeButton.get();
    }

    public SimpleObjectProperty<Button> removeButtonProperty() {
        return removeButton;
    }

    public void setRemoveButton(Button button) {
        this.removeButton.set(button);
    }

    // Method to increase the quantity by 1
    public void increaseQuantity() {
        this.quantity.set(this.quantity.get() + 1);
    }


    public void decreaseQuantity() {
        if (this.quantity.get() > 1) {
            this.quantity.set(this.quantity.get() - 1);
        } else {
            this.remove(); // Consider the item removed if quantity is reduced to 0
        }
    }

    @Override
    public String toString() {
        return String.format("ID: %d, Name: %s, Unit Price: %.2f, Quantity: %d, Total Price: %.2f",
                artworkId.get(), name.get(), price.get(), quantity.get(), getTotalPrice());
    }
}
