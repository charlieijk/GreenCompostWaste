module GreenCompostWaste {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires java.base;
    requires org.xerial.sqlitejdbc;
    
    opens controllers to javafx.fxml;
    opens views to javafx.fxml;
    opens styles to javafx.base;
    
    opens com.greencompost to javafx.base;
    opens com.greencompost.controller to javafx.base;
    opens com.greencompost.service to javafx.base;
    opens com.greencompost.main to javafx.fxml, javafx.graphics;
    
    exports controllers;
    exports com.greencompost;
    exports com.greencompost.controller;
    exports com.greencompost.service;
    exports com.greencompost.main;
}
