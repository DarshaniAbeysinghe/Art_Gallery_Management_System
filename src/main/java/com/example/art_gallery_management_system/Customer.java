package com.example.art_gallery_management_system;

import javafx.beans.property.*;

public class Customer {

    private final IntegerProperty colID;
    private final StringProperty nic;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final StringProperty email;
    private final StringProperty phone;
    private final StringProperty gender;
    private final StringProperty address;


    public Customer(int colID, String nic, String firstName, String lastName, String email, String phone, String gender, String address) {
        this.colID = new SimpleIntegerProperty(colID);
        this.nic = new SimpleStringProperty(nic);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.gender = new SimpleStringProperty(gender);
        this.address = new SimpleStringProperty(address);
    }



    public int getColID() {
        return colID.get();
    }

    public void setColID(int colID) {
        this.colID.set(colID);
    }

    public IntegerProperty colIDProperty() {
        return colID;
    }

    public String getNic() {
        return nic.get();
    }

    public void setNic(String nic) {
        this.nic.set(nic);
    }

    public StringProperty nicProperty() {
        return nic;
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public String getGender() {
        return gender.get();
    }

    public void setGender(String gender) {
        this.gender.set(gender);
    }

    public StringProperty genderProperty() {
        return gender;
    }

    public String getAddress() {
        return address.get();
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public StringProperty addressProperty() {
        return address;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "colID=" + colID.get() +
                ", nic='" + nic.get() + '\'' +
                ", firstName='" + firstName.get() + '\'' +
                ", lastName='" + lastName.get() + '\'' +
                ", email='" + email.get() + '\'' +
                ", phone='" + phone.get() + '\'' +
                ", gender='" + gender.get() + '\'' +
                ", address='" + address.get() + '\'' +
                '}';
    }
}
