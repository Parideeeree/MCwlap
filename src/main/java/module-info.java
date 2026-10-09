module it.proxy.mcwlap {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens it.proxy.mcwlap to javafx.fxml;
    exports it.proxy.mcwlap;

    opens it.proxy.mcwlap.model to javafx.base, javafx.fxml;
}