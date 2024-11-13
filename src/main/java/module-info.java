module edu.kean.musicplayersimulator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires annotations;
    requires java.desktop;
    requires javafx.media;

    opens edu.kean.musicplayersimulator to javafx.fxml;
    opens AudioPlayer to javafx.graphics;
    exports edu.kean.musicplayersimulator;
    exports UI;
}