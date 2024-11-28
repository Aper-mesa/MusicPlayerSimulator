package UI;

import AudioPlayer.AudioPlayer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ASP extends Application {

    @Override
    public void start(Stage primaryStage) {
        AudioPlayer audioPlayer = new AudioPlayer();

        Canvas spectrumCanvas = new Canvas(800, 400);
        audioPlayer.setSpectrumCanvas(spectrumCanvas);

        audioPlayer.play(0);

        StackPane root = new StackPane(spectrumCanvas);
        Scene scene = new Scene(root, 800, 400);

        primaryStage.setTitle("ASP Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
