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
    static ProgressBar progressBar = new ProgressBar(0.0);
    VBox playPage = new VBox();
    VBox downloadPage = new VBox();
    BorderPane root = new BorderPane();
    List<String> playlist;
    AudioPlayerFX player = new AudioPlayerFX();
    Label currentSongName = new Label();
    Button playPauseButton;
    Button modeButton;
    Image pauseIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/pause.png")));
    Image hoverPauseIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/pauseHover.png")));
    boolean isPlaying = false;
    boolean isCycle = true;

    @Override
    public void start(Stage primaryStage) {
        playlist = player.getPlaylist();
        primaryStage.setTitle("Music Player Simulator");
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/play.png")));
        primaryStage.getIcons().add(icon);

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

        downloadPageButton.setOnMouseEntered(_ -> downloadPageButton.setCursor(Cursor.HAND));
        playPageButton.setOnMouseEntered(_ -> playPageButton.setCursor(Cursor.HAND));
        downloadPageButton.setOnMouseExited(_ -> downloadPageButton.setCursor(Cursor.DEFAULT));
        playPageButton.setOnMouseExited(_ -> playPageButton.setCursor(Cursor.DEFAULT));

        HBox topBar = new HBox(10, downloadPageButton, playPageButton);
        topBar.setStyle("-fx-padding: 6; -fx-background-color: #ececec;");

        VBox playerBar = new VBox(10);
        playerBar.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10;");

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);

        HBox currentSong = new HBox(10);
        currentSong.setAlignment(Pos.CENTER);

        currentSongName.setStyle("-fx-font-size: 16px;");

        currentSong.getChildren().add(currentSongName);

        Image prevIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/previous.png")));
        Image hoverPrevIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/previousHover.png")));
        Image nextIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/next.png")));
        Image hoverNextIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/nextHover.png")));

        Image playIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/play.png")));
        Image hoverPlayIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/playHover.png")));
        Image pauseIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/pause.png")));
        Image hoverPauseIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/pauseHover.png")));

        Image cycleIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/cycle.png")));
        Image hoverCycleIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/cycleHover.png")));
        Image randomIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/random.png")));
        Image hoverRandomIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/randomHover.png")));

        Button prevButton = getButton(prevIcon, hoverPrevIcon);
        playPauseButton = isPlaying ? getButton(pauseIcon, hoverPauseIcon) : getButton(playIcon, hoverPlayIcon);
        Button nextButton = getButton(nextIcon, hoverNextIcon);
        modeButton = isCycle ? getButton(cycleIcon, hoverCycleIcon) : getButton(randomIcon, hoverRandomIcon);

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        controls.getChildren().addAll(spacer1, prevButton, playPauseButton, nextButton, modeButton, spacer2);

        progressBar.setPrefWidth(400);
        HBox progressBarContainer = new HBox(progressBar);
        progressBarContainer.setAlignment(Pos.CENTER);

        playerBar.getChildren().addAll(currentSong, controls, progressBarContainer);

        root.setTop(topBar);
        root.setCenter(playPage);
        root.setBottom(playerBar);

        downloadPageButton.setOnAction(_ -> root.setCenter(downloadPage));
        playPageButton.setOnAction(_ -> root.setCenter(playPage));

        playPauseButton.setOnAction(_ -> {
            if (isPlaying) {
                player.pause();
                isPlaying = false;
                modifyButton(playIcon, hoverPlayIcon, playPauseButton);
            } else {
                player.resume();
                isPlaying = true;
                modifyButton(pauseIcon, hoverPauseIcon, playPauseButton);
            }
        });

        nextButton.setOnAction(_ -> {
            int index = player.playNext();
            isPlaying = true;
            currentSongName.setText(playlist.get(index));
            modifyButton(pauseIcon, hoverPauseIcon, playPauseButton);
        });
        prevButton.setOnAction(_ -> {
            int index = player.playPrevious();
            isPlaying = true;
            currentSongName.setText(playlist.get(index));
            modifyButton(pauseIcon, hoverPauseIcon, playPauseButton);
        });

        modeButton.setOnAction(_ -> {
            if (isCycle) {
                isCycle = false;
                modifyButton(randomIcon, hoverRandomIcon, modeButton);
            } else {
                isCycle = true;
                modifyButton(cycleIcon, hoverCycleIcon, modeButton);
            }
        });
    }

    private void loadPlayPage() {
        Image playIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/play.png")));
        Image hoverPlayIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/playHover.png")));

        Image downloadIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/download.png")));
        Image hoverDownloadIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/downloadHover.png")));

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

            songRow.setOnMouseEntered(_ -> songRow.setStyle("-fx-background-color: #ececec;"));
            songRow.setOnMouseExited(_ -> songRow.setStyle("-fx-background-color: transparent;"));

            int finalI = i;
            playButton.setOnAction(_ -> {
                isPlaying = true;
                player.play(finalI);
                currentSongName.setText(songName.getText());
                modifyButton(pauseIcon, hoverPauseIcon, playPauseButton);
            });
        }
    }

    private void loadDownloadPage() {
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

            downloadRow.setOnMouseEntered(_ -> downloadRow.setStyle("-fx-background-color: #ececec;"));
            downloadRow.setOnMouseExited(_ -> downloadRow.setStyle("-fx-background-color: transparent;"));
        }
    }

    public static void updatePlayProgress(double progress) {
        progressBar.setProgress(progress);
    }

    @NotNull
    private static Button getButton(Image defaultIcon, Image hoverIcon) {
        Button button = new Button();
        modifyButton(defaultIcon, hoverIcon, button);
        return button;
    }

    private static void modifyButton(Image defaultIcon, Image hoverIcon, Button button) {
        ImageView iconView = new ImageView(defaultIcon);
        iconView.setFitWidth(BUTTON_SIZE);
        iconView.setFitHeight(BUTTON_SIZE);
        iconView.setPreserveRatio(true);
        button.setGraphic(iconView);
        button.setStyle("-fx-background-color: transparent;");

        button.setOnMouseEntered(_ -> {
            ImageView hoverIconView = new ImageView(hoverIcon);
            hoverIconView.setFitWidth(BUTTON_SIZE);
            hoverIconView.setFitHeight(BUTTON_SIZE);
            hoverIconView.setPreserveRatio(true);
            button.setGraphic(hoverIconView);
            button.setCursor(Cursor.HAND);
        });
        button.setOnMouseExited(_ -> {
            ImageView defaultIconView = new ImageView(defaultIcon);
            defaultIconView.setFitWidth(BUTTON_SIZE);
            defaultIconView.setFitHeight(BUTTON_SIZE);
            defaultIconView.setPreserveRatio(true);
            button.setGraphic(defaultIconView);
            button.setCursor(Cursor.DEFAULT);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
