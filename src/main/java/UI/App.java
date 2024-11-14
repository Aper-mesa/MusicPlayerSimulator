package UI;

import AudioPlayer.AudioPlayerFX;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class App extends Application {
    final static int BUTTON_SIZE = 25;
    VBox playPage = new VBox();
    BorderPane root = new BorderPane();
    VBox downloadPage = new VBox();
    List<String> playlist;
    AudioPlayerFX player = new AudioPlayerFX();
    static ProgressBar progressBar = new ProgressBar(0.0);

    @Override
    public void start(Stage primaryStage) {
        playlist = player.getPlaylist();
        primaryStage.setTitle("Music Player Simulator");

        initContent();

        loadPlayPage();
        loadDownloadPage();

        Scene scene = new Scene(root, 650, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    //load constant content of the player
    private void initContent() {
        Button downloadPageButton = new Button("Download");
        Button playPageButton = new Button("Play");

        HBox topBar = new HBox(10, downloadPageButton, playPageButton);
        topBar.setStyle("-fx-padding: 6; -fx-background-color: #ececec;");

        VBox playerBar = new VBox(10);
        playerBar.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10;");

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);

        Image prevIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/previous.png")));
        Image hoverPrevIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/previousHover.png")));
        Image nextIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/next.png")));
        Image hoverNextIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/nextHover.png")));

        Button prevButton = getButton(prevIcon, hoverPrevIcon);
        Button playPauseButton = new Button("Play/Pause");
        Button nextButton = getButton(nextIcon, hoverNextIcon);
        Button modeButton = new Button("Mode");

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        controls.getChildren().addAll(spacer1, prevButton, playPauseButton, nextButton, modeButton, spacer2);

        progressBar.setPrefWidth(400);
        HBox progressBarContainer = new HBox(progressBar);
        progressBarContainer.setAlignment(Pos.CENTER);

        playerBar.getChildren().addAll(controls, progressBarContainer);

        root.setTop(topBar);
        root.setCenter(playPage);
        root.setBottom(playerBar);

        downloadPageButton.setOnAction(event -> root.setCenter(downloadPage));
        playPageButton.setOnAction(event -> root.setCenter(playPage));

        playPauseButton.setOnAction(e -> player.pause());

        nextButton.setOnAction(e -> player.playNext());
        prevButton.setOnAction(e -> player.playPrevious());
    }

    private void loadPlayPage() {
        Image playIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/play.png")));
        Image hoverPlayIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/playHover.png")));

        Image downloadIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/download.png")));
        Image hoverDownloadIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/downloadHover.png")));

        System.out.println(playlist.size());
        for (int i = 0; i < playlist.size(); i++) {
            HBox songRow = new HBox(5);
            songRow.setPrefHeight(50);
            songRow.setAlignment(Pos.CENTER_LEFT);

            Label songNumber = new Label("   " + (i + 1));
            songNumber.setPrefWidth(50);
            songNumber.setStyle("-fx-font-size: 16px;");

            Label songName = new Label(playlist.get(i));
            songName.setStyle("-fx-font-size: 16px;");

            Region spacer = new Region();
            spacer.setMinWidth(Region.USE_COMPUTED_SIZE);
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button playButton = getButton(playIcon, hoverPlayIcon);
            Button downloadButton = getButton(downloadIcon, hoverDownloadIcon);

            songRow.getChildren().addAll(songNumber, songName, spacer, playButton, downloadButton);
            playPage.getChildren().add(songRow);

            songRow.setOnMouseEntered(event -> songRow.setStyle("-fx-background-color: #ececec;"));
            songRow.setOnMouseExited(event -> songRow.setStyle("-fx-background-color: transparent;"));

            int finalI = i;
            playButton.setOnAction(e -> {
                player.play(finalI);
            });
        }


    }

    private void loadDownloadPage() {
        Image pauseIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/pause.png")));
        Image hoverPauseIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/pauseHover.png")));

        Image cancelIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/cancel.png")));
        Image hoverCancelIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/cancelHover.png")));

        for (int i = 1; i <= 4; i++) {
            HBox downloadRow = new HBox(5);
            downloadRow.setPrefHeight(50);
            downloadRow.setAlignment(Pos.CENTER_LEFT);

            Label downloadNumber = new Label("   " + i);
            downloadNumber.setPrefWidth(50);
            downloadNumber.setStyle("-fx-font-size: 16px;");

            Label songName = new Label("Song " + i);
            songName.setPrefWidth(100);
            songName.setStyle("-fx-font-size: 16px;");

            Region spacer = new Region();
            spacer.setMinWidth(Region.USE_COMPUTED_SIZE);
            HBox.setHgrow(spacer, Priority.ALWAYS);

            ProgressBar progressBar = new ProgressBar(0.0);
            progressBar.setPrefWidth(200);

            Button pauseButton = getButton(pauseIcon, hoverPauseIcon);
            Button cancelButton = getButton(cancelIcon, hoverCancelIcon);

            downloadRow.getChildren().addAll(downloadNumber, songName, spacer, progressBar, pauseButton, cancelButton);
            downloadPage.getChildren().add(downloadRow);

            downloadRow.setOnMouseEntered(event -> downloadRow.setStyle("-fx-background-color: #ececec;"));
            downloadRow.setOnMouseExited(event -> downloadRow.setStyle("-fx-background-color: transparent;"));
        }
    }

    public static void updatePlayProgress(double progress) {
        progressBar.setProgress(progress);
    }

    @NotNull
    private static Button getButton(Image defaultIcon, Image hoverIcon) {
        Button button = new Button();
        ImageView iconView = new ImageView(defaultIcon);
        iconView.setFitWidth(BUTTON_SIZE);
        iconView.setFitHeight(BUTTON_SIZE);
        iconView.setPreserveRatio(true);
        button.setGraphic(iconView);
        button.setStyle("-fx-background-color: transparent;");

        button.setOnMouseEntered(event -> {
            ImageView hoverIconView = new ImageView(hoverIcon);
            hoverIconView.setFitWidth(BUTTON_SIZE);
            hoverIconView.setFitHeight(BUTTON_SIZE);
            hoverIconView.setPreserveRatio(true);
            button.setGraphic(hoverIconView);
            button.setCursor(Cursor.HAND);
        });
        button.setOnMouseExited(event -> {
            ImageView defaultIconView = new ImageView(defaultIcon);
            defaultIconView.setFitWidth(BUTTON_SIZE);
            defaultIconView.setFitHeight(BUTTON_SIZE);
            defaultIconView.setPreserveRatio(true);
            button.setGraphic(defaultIconView);
            button.setCursor(Cursor.DEFAULT);
        });
        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
