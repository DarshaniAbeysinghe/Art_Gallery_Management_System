package com.example.art_gallery_management_system;

import javafx.beans.property.*;
import javafx.scene.image.Image;

public class Artwork {

    private final IntegerProperty artworkID;
    private final IntegerProperty artistID;
    private final SimpleStringProperty name;
    private final SimpleStringProperty size;
    private final SimpleStringProperty medium;
    private final SimpleStringProperty subject;
    private final SimpleDoubleProperty price;
    private final SimpleStringProperty description;
    private final SimpleStringProperty artist;
    private final ObjectProperty<Image> image;

    // Constructor
    public Artwork(int artworkID, int artistID, String name, String size, String medium, String subject, double price, String description, String artist, Image image) {
        this.artworkID = new SimpleIntegerProperty(artworkID);
        this.artistID = new SimpleIntegerProperty(artistID);
        this.name = new SimpleStringProperty(name);
        this.size = new SimpleStringProperty(size);
        this.medium = new SimpleStringProperty(medium);
        this.subject = new SimpleStringProperty(subject);
        this.price = new SimpleDoubleProperty(price);
        this.description = new SimpleStringProperty(description);
        this.artist = new SimpleStringProperty(artist);
        this.image = new SimpleObjectProperty<>(image);
    }

    // Getters
    public String getName() {
        return name.get();
    }

    public String getSize() {
        return size.get();
    }

    public String getMedium() {
        return medium.get();
    }

    public String getSubject() {
        return subject.get();
    }

    public double getPrice() {
        return price.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getArtist() {
        return artist.get();
    }

    public Image getImage() {
        return image.get();
    }

    public int getArtworkID() {
        return artworkID.get();
    }

    public int getArtistID() {
        return artistID.get();
    }

    // **New Method: Get ID for deletion**
    public int getId() {
        return getArtworkID();
    }

    // Setters
    public void setName(String name) {
        this.name.set(name);
    }

    public void setSize(String size) {
        this.size.set(size);
    }

    public void setMedium(String medium) {
        this.medium.set(medium);
    }

    public void setSubject(String subject) {
        this.subject.set(subject);
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setArtist(String artist) {
        this.artist.set(artist);
    }

    public void setImage(Image image) {
        this.image.set(image);
    }

    // Property methods for TableView binding
    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty sizeProperty() {
        return size;
    }

    public StringProperty mediumProperty() {
        return medium;
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty artistProperty() {
        return artist;
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public IntegerProperty artworkIDProperty() {
        return artworkID;
    }

    public IntegerProperty artistIDProperty() {
        return artistID;
    }
}
