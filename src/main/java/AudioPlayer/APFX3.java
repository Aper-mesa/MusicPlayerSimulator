package AudioPlayer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

// Test Class for the AudioPlayerFX, configured JavaFX 23 natively on Z's Mac, but need to change some argument in Maven.
public class APFX3 extends Application {
    private AudioPlayerFX audioPlayer;
    private Slider progressSlider;

    @Override
    public void start(Stage primaryStage) {

        audioPlayer = new AudioPlayerFX();

        Button playButton = new Button("Play");
        playButton.setOnAction(e -> audioPlayer.play());

        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(e -> audioPlayer.pause());

        Button stopButton = new Button("Stop");
        stopButton.setOnAction(e -> audioPlayer.stop());

        Button nextButton = new Button("Next");
        nextButton.setOnAction(e -> audioPlayer.playNext());

        Button previousButton = new Button("Previous");
        previousButton.setOnAction(e -> audioPlayer.playUp());

        progressSlider = new Slider(0, 100, 0);
        progressSlider.setDisable(true);

        Label progressLabel = new Label("Progress:");
        VBox layout = new VBox(10, playButton, pauseButton, stopButton, nextButton, previousButton, progressLabel, progressSlider);

        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("JavaFX Audio Player");
        primaryStage.show();

        javafx.animation.AnimationTimer timer = new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                updateProgress();
            }
        };
        timer.start();
    }

    private void updateProgress() {
        if (audioPlayer != null && audioPlayer.getPlaybackProgress() >= 0) {
            progressSlider.setValue(audioPlayer.getPlaybackProgress());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
