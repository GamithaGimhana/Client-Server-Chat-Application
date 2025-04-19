module com.gdse.chatApplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.gdse.chatApplication.controller to javafx.fxml;
    opens com.gdse.chatApplication to javafx.fxml;

    exports com.gdse.chatApplication;
    exports com.gdse.chatApplication.controller;
}