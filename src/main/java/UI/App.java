package UI;

import AudioPlayer.AudioPlayer;
import Download.DownloadManager;
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
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class App extends Application {
    private final static int BUTTON_SIZE = 25;
    private final static int PROGRESS_WIDTH = 400;
    private static final ProgressBar progressBar = new ProgressBar(0.0);
    private final VBox playPage = new VBox();
    private static final VBox downloadPage = new VBox();
    private final BorderPane root = new BorderPane();
    private static List<String> playlist;
    private final AudioPlayer player = new AudioPlayer();
    private final DownloadManager dm = new DownloadManager();
    private static final Label currentSongName = new Label();
    private Button playPauseButton;
    private Button modeButton;
    private static final Image pauseIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("/icons/pause.png")));
    private static final Image hoverPauseIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("/icons/pauseHover.png")));
    private static final Image cancelIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("/icons/cancel.png")));
    private static final Image hoverCancelIcon = new Image(
            Objects.requireNonNull(App.class.getResourceAsStream("/icons/cancelHover.png")));
    private boolean isPlaying = false;
    private static final Label currentTimeLabel = new Label("00: 00");
    private static final Label songDuration = new Label("00: 00");
    private static final List<HBox> downloadRows = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        playlist = player.getPlaylist();
        primaryStage.setTitle("Music Player Simulator");
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/play.png")));
        primaryStage.getIcons().add(icon);

        initContent();

        loadPlayPage();

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // load constant content of the player
    private void initContent() {
        Image prevIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/previous.png")));
        Image hoverPrevIcon = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/previousHover.png")));
        Image nextIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/next.png")));
        Image hoverNextIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/nextHover.png")));

        Image playIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/play.png")));
        Image hoverPlayIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/playHover.png")));
        Image pauseIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/pause.png")));
        Image hoverPauseIcon = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/pauseHover.png")));

        Image cycleIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/cycle.png")));
        Image hoverCycleIcon = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/cycleHover.png")));
        Image shuffleIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/shuffle.png")));
        Image hoverShuffleIcon = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/shuffleHover.png")));
        Image singleIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/single.png")));
        Image hoverSingleIcon = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/singleHover.png")));

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

        Button prevButton = getButton(prevIcon, hoverPrevIcon);
        playPauseButton = isPlaying ? getButton(pauseIcon, hoverPauseIcon) : getButton(playIcon, hoverPlayIcon);
        Button nextButton = getButton(nextIcon, hoverNextIcon);
        player.setPlaybackMode(AudioPlayer.CYCLE);
        modeButton = getButton(cycleIcon, hoverCycleIcon);

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        controls.getChildren().addAll(spacer1, prevButton, playPauseButton, nextButton, modeButton, spacer2);

        progressBar.setPrefWidth(PROGRESS_WIDTH);
        HBox progressBarContainer = new HBox(10);
        progressBarContainer.getChildren().addAll(currentTimeLabel, progressBar, songDuration);

        progressBarContainer.setAlignment(Pos.CENTER);

        playerBar.getChildren().addAll(currentSong, controls, progressBarContainer);

        root.setTop(topBar);
        root.setCenter(playPage);
        root.setBottom(playerBar);

        downloadPageButton.setOnAction(_ -> root.setCenter(downloadPage));
        playPageButton.setOnAction(_ -> root.setCenter(playPage));

        progressBar.setOnMouseClicked(e -> {
            double mouseX = e.getX();
            double progressBarWidth = progressBar.getWidth();
            double progress = mouseX / progressBarWidth;
            progress = Math.max(0, Math.min(1, progress));

            progressBar.setProgress(progress);
            player.jumpToProgress(progress);
        });

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
            if (player.isCycle()) {
                player.setPlaybackMode(AudioPlayer.SINGLE);
                modifyButton(singleIcon, hoverSingleIcon, modeButton);
            } else if (player.isSingle()) {
                player.setPlaybackMode(AudioPlayer.SHUFFLE);
                modifyButton(shuffleIcon, hoverShuffleIcon, modeButton);
            } else if (player.isShuffle()) {
                player.setPlaybackMode(AudioPlayer.CYCLE);
                modifyButton(cycleIcon, hoverCycleIcon, modeButton);
            }
        });
    }

    private void loadPlayPage() {
        Image playIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/play.png")));
        Image hoverPlayIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/playHover.png")));

        Image downloadIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/download.png")));
        Image hoverDownloadIcon = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/downloadHover.png")));

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
                player.playFromUI(finalI);
                currentSongName.setText(songName.getText());
                modifyButton(pauseIcon, hoverPauseIcon, playPauseButton);
            });

            // create a download task
            downloadButton.setOnAction(_ -> {
                        // TODO temporary, used to rapidly switch to download page to see.
                        //TODO  will remove after slow download speed
                        root.setCenter(downloadPage);
                        dm.startDownload(finalI);
                    }
            );
        }
    }

    //create a new download row in the download page
    public static void addDownloadRow(int index, int downloadIndex) {
        HBox downloadRow = new HBox(5);
        downloadRows.add(downloadRow);
        downloadRow.setPrefHeight(50);
        downloadRow.setAlignment(Pos.CENTER_LEFT);

        Label downloadNumber = new Label("   " + (downloadIndex + 1));
        downloadNumber.setPrefWidth(50);
        downloadNumber.setStyle("-fx-font-size: 16px;");

        Label songName = new Label(playlist.get(index));
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

    public static void removeDownloadTask(int index) {
        System.out.println("remove download row with index " + index);
        downloadRows.get(index).getChildren().clear();
        downloadPage.getChildren().remove(downloadRows.get(index));
        downloadRows.remove(index);
    }

    public static void updatePlayProgress(double progress) {
        progressBar.setProgress(progress);
    }

    // used when a song starts
    public static void updatePlayBar(Duration duration, String trackName) {
        // update song name on the play bar
        currentSongName.setText(trackName);
        // display song duration
        formatTime(duration, songDuration);
    }

    public static void updateDownloadProgress(double progress, int index) {
        ((ProgressBar) downloadRows.get(index).getChildren().get(3)).setProgress(progress);
    }

    private static void formatTime(Duration duration, Label label) {
        double totalSeconds = duration.toSeconds();
        long minutes = TimeUnit.SECONDS.toMinutes((long) totalSeconds);
        long seconds = (long) totalSeconds - TimeUnit.MINUTES.toSeconds(minutes);
        String formattedDuration = String.format("%02d: %02d", minutes, seconds);
        label.setText(formattedDuration);
    }

    public static void updateCurrentTime(Duration duration) {
        formatTime(duration, currentTimeLabel);
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