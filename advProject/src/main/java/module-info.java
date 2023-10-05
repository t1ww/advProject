module se233.advproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires net.coobird.thumbnailator;
    requires java.desktop;
    requires javafx.graphics;


    exports se233.advproject;
    exports se233.advproject.view;

    opens se233.advproject to javafx.fxml;
    opens se233.advproject.view to javafx.fxml;

}