package UI;

import AudioPlayer.AudioPlayer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.InputStream;

public class ASP extends Application {
    private final AudioPlayer audioPlayer = new AudioPlayer();

    @Override
    public void start(Stage stage) {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER);

        Button playButton = createIconButton("/icons/play.png", "/icons/playHover.png");
        Button pauseButton = createIconButton("/icons/pause.png", "/icons/pauseHover.png");
        Button nextButton = createIconButton("/icons/next.png", "/icons/nextHover.png");

        playButton.setOnAction(e -> audioPlayer.resume());
        pauseButton.setOnAction(e -> audioPlayer.pause());
        nextButton.setOnAction(e -> audioPlayer.playNext());

        Canvas spectrumCanvas = new Canvas(300, 100);
        audioPlayer.setSpectrumCanvas(spectrumCanvas);

        hbox.getChildren().addAll(playButton, pauseButton, nextButton, spectrumCanvas);

        Scene scene = new Scene(hbox, 600, 150);
        stage.setScene(scene);
        stage.setTitle("ASP Test 2");
        stage.show();
    }

    private Button createIconButton(String iconPath, String hoverIconPath) {
        Image defaultImage = loadIcon(iconPath);
        Image hoverImage = loadIcon(hoverIconPath);

        ImageView imageView = new ImageView(defaultImage);
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);

        Button button = new Button();
        button.setGraphic(imageView);
        button.setStyle("-fx-background-color: transparent;");

        button.setOnMouseEntered(e -> imageView.setImage(hoverImage));
        button.setOnMouseExited(e -> imageView.setImage(defaultImage));

        return button;
    }

    private Image loadIcon(String path) {
        InputStream inputStream = getClass().getResourceAsStream(path);
        if (inputStream == null) {
            System.err.println("Icon not found: " + path);
            throw new IllegalArgumentException("Icon not found: " + path);
        }
        return new Image(inputStream);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
