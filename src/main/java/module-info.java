module com.example.art_gallery_management_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.desktop;
    requires freetts;
    requires jakarta.mail;


    requires dynamicreports.core;  // Add the Jakarta Mail modul





    opens com.example.art_gallery_management_system to javafx.fxml;
    exports com.example.art_gallery_management_system;
}