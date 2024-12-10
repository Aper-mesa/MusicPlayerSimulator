package UI;

import AudioPlayer.AudioPlayer;
import Download.DownloadManager;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class App extends Application {
    // some components are moved to member places since there are used across multiple methods
    private final static int BUTTON_SIZE = 25;
    private final static int PROGRESS_WIDTH = 475;
    private static final ProgressBar progressBar = new ProgressBar(0.0);
    private static final int ALBUM_SIZE = 113;
    private static final VBox downloadPage = new VBox();
    private static final DownloadManager dm = new DownloadManager();
    private static final Label currentSongName = new Label();
    private static final Image pauseIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("/icons/pause.png")));
    private static final Image hoverPauseIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("/icons/pauseHover.png")));
    private static final Image playIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("/icons/play.png")));
    private static final Image hoverPlayIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("/icons/playHover.png")));
    private static final Image cancelIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("/icons/cancel.png")));
    private static final Image hoverCancelIcon = new Image(
            Objects.requireNonNull(App.class.getResourceAsStream("/icons/cancelHover.png")));
    private static final Label currentTimeLabel = new Label("00: 00");
    private static final Label songDuration = new Label("00: 00");
    private static final List<HBox> downloadRows = new ArrayList<>();
    private static final Label warningLabel = new Label();
    private static final ImageView album = new ImageView();
    private static final Label artistsLabel = new Label();
    public static boolean isMute = false;
    private static List<String> playlist;
    private static Button modeButton;
    private static HBox noDownloadMessage;
    private final VBox playPage = new VBox();
    private final ScrollPane playPageScrollPane = new ScrollPane();
    private final VBox playPageContent = new VBox();
    private final BorderPane root = new BorderPane();
    private final AudioPlayer player = new AudioPlayer();
    private Button playPauseButton;
    private boolean isPlaying = false;
    private Stage primaryStage;

    //create a new download row in the download page
    public static void addDownloadRow(int index, int downloadIndex) {
        downloadPage.getChildren().remove(noDownloadMessage);
        final boolean[] isDownloading = {true};
        HBox downloadRow = new HBox(5);

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

        Button pauseButton = getButton(pauseIcon, hoverPauseIcon, BUTTON_SIZE);
        Button cancelButton = getButton(cancelIcon, hoverCancelIcon, BUTTON_SIZE);

        downloadRow.getChildren().addAll(downloadNumber, songName, spacer, progressBar, pauseButton, cancelButton);
        downloadRows.add(downloadRow);

        downloadPage.getChildren().add(downloadRow);

        downloadRow.setOnMouseEntered(_ -> downloadRow.setStyle("-fx-background-color: #ececec;"));
        downloadRow.setOnMouseExited(_ -> downloadRow.setStyle("-fx-background-color: transparent;"));

        pauseButton.setOnAction(_ -> {
            if (isDownloading[0]) {
                dm.pauseTask(playlist.get(index));
                modifyButton(playIcon, hoverPlayIcon, pauseButton);
                isDownloading[0] = false;
            } else {
                dm.resumeTask(playlist.get(index));
                modifyButton(pauseIcon, hoverPauseIcon, pauseButton);
                isDownloading[0] = true;
            }
        });

        cancelButton.setOnAction(_ -> {
            dm.removeTask(playlist.get(index));
            removeDownloadTask(playlist.get(index));
        });
    }

    //called by download manager to remove the download row in the gui
    public static void removeDownloadTask(String taskID) {
        for (HBox downloadRow : downloadRows) {
            if (((Label) downloadRow.getChildren().get(1)).getText().equals(taskID)) {
                downloadPage.getChildren().remove(downloadRow);
                downloadRows.remove(downloadRow);
                return;
            }
        }
    }

    //called when there is no download task
    public static void showNoDownloadMessage() {
        downloadPage.getChildren().add(noDownloadMessage);
    }

    //called by the audio player to update to progress bar of the currently playing song
    public static void updatePlayProgress(double progress) {
        progressBar.setProgress(progress);
    }

    //called by the download manager to update the progress bar of each download task
    public static void updateDownloadProgress(double progress, int index) {
        ((ProgressBar) downloadRows.get(index).getChildren().get(3)).setProgress(progress);
    }

    //called when the program wants to warn the user
    public static void updateWarning(String warning) {
        warningLabel.setText(warning);
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(_ -> warningLabel.setText(""));
        pause.play();
    }

    //update album cover when a new song is playing
    public static void updateAlbum(Image cover, String name, Duration duration, String artists) {
        album.setImage(cover);
        currentSongName.setText(name);
        artistsLabel.setText(artists);

        // display song duration
        formatTime(duration, songDuration);
    }

    //format seconds into minute: second
    private static void formatTime(Duration duration, Label label) {
        double totalSeconds = duration.toSeconds();
        long minutes = TimeUnit.SECONDS.toMinutes((long) totalSeconds);
        long seconds = (long) totalSeconds - TimeUnit.MINUTES.toSeconds(minutes);
        String formattedDuration = String.format("%02d: %02d", minutes, seconds);
        label.setText(formattedDuration);
    }

    //called each second to display the current time of the song
    public static void updateCurrentTime(Duration duration) {
        formatTime(duration, currentTimeLabel);
    }

    //used to create and return a button
    private static Button getButton(Image defaultIcon, Image hoverIcon, int size) {
        Button button = new Button();
        modifyButton(defaultIcon, hoverIcon, button, size);
        return button;
    }

    //used to change the icons of a button
    private static void modifyButton(Image defaultIcon, Image hoverIcon, Button button) {
        modifyButtonCore(defaultIcon, hoverIcon, button, BUTTON_SIZE);
    }

    private static void modifyButton(Image defaultIcon, Image hoverIcon, Button button, int size) {
        modifyButtonCore(defaultIcon, hoverIcon, button, size);
    }

    private static void modifyButtonCore(Image defaultIcon, Image hoverIcon, Button button, int size) {
        ImageView iconView = new ImageView(defaultIcon);
        iconView.setFitWidth(size);
        iconView.setFitHeight(size);
        iconView.setPreserveRatio(true);
        button.setGraphic(iconView);
        button.setStyle("-fx-background-color: transparent;");

        button.setOnMouseEntered(_ -> {
            ImageView hoverIconView = new ImageView(hoverIcon);
            hoverIconView.setFitWidth(size);
            hoverIconView.setFitHeight(size);
            hoverIconView.setPreserveRatio(true);
            button.setGraphic(hoverIconView);
            button.setCursor(Cursor.HAND);
        });
        button.setOnMouseExited(_ -> {
            ImageView defaultIconView = new ImageView(defaultIcon);
            defaultIconView.setFitWidth(size);
            defaultIconView.setFitHeight(size);
            defaultIconView.setPreserveRatio(true);
            button.setGraphic(defaultIconView);
            button.setCursor(Cursor.DEFAULT);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        playlist = player.getPlaylist();
        System.out.println("playlist size: " + playlist.size());
        primaryStage.setTitle("Music Player Simulator");
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/play.png")));
        primaryStage.getIcons().add(icon);

        initContent();

        Label noDownloadLabel = new Label("No Download");
        noDownloadMessage = new HBox(noDownloadLabel);
        noDownloadMessage.setAlignment(Pos.CENTER);
        downloadPage.getChildren().add(noDownloadMessage);
        noDownloadLabel.setStyle("-fx-font-size: 16px;");

        loadPlayPage();

        root.setCenter(playPageScrollPane);

        Scene scene = new Scene(root, 700, 700);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // load constant content of the player
    private void initContent() {
        playPageScrollPane.setContent(playPageContent);
        playPageScrollPane.setFitToWidth(true); // Makes the content fit the width of the scroll pane
        playPageScrollPane.setStyle("-fx-background-color: transparent;"); // Optional: To match the background

        playPageContent.setSpacing(5); // Add some spacing between songs
        playPageContent.setPadding(new Insets(10)); // Optional: Add padding
        playPageContent.setStyle("-fx-background-color: #f8f8f8;"); // Set a background color
        root.setCenter(playPageScrollPane);
        Image prevIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/previous.png")));
        Image hoverPrevIcon = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/previousHover.png")));
        Image nextIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/next.png")));
        Image hoverNextIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/nextHover.png")));

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
        Image volumeIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/volume.png")));
        Image hoverVolumeIcon = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/volumeHover.png")));
        Image muteIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/mute.png")));
        Image hoverMuteIcon = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/icons/muteHover.png")));

        Button downloadPageButton = new Button("Download");
        Button playPageButton = new Button("Play");
        Button perfPageButton = new Button("Performance");

        //hovered cursor will turn to hand shape for better interaction
        downloadPageButton.setOnMouseEntered(_ -> downloadPageButton.setCursor(Cursor.HAND));
        playPageButton.setOnMouseEntered(_ -> playPageButton.setCursor(Cursor.HAND));
        downloadPageButton.setOnMouseExited(_ -> downloadPageButton.setCursor(Cursor.DEFAULT));
        playPageButton.setOnMouseExited(_ -> playPageButton.setCursor(Cursor.DEFAULT));
        perfPageButton.setOnMouseEntered(_ -> perfPageButton.setCursor(Cursor.HAND));
        perfPageButton.setOnMouseExited(_ -> perfPageButton.setCursor(Cursor.DEFAULT));

        // use a spacer to make the warning on the right side
        Region spacer = new Region();
        spacer.setMinWidth(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        //font size of the warning
        warningLabel.setStyle("-fx-font-size: 16px;");

        // volume control
        Slider volumeBar = new Slider(0, 1, 1);
        volumeBar.setPrefWidth(100);
        // use a container to wrap the volume bar for better positioning
        VBox volumeContainer = new VBox(volumeBar);
        volumeContainer.setAlignment(Pos.CENTER);
        Button volumeButton = getButton(volumeIcon, hoverVolumeIcon, 15);

        volumeBar.valueProperty().addListener(_ -> player.setVolume(volumeBar.getValue()));

        volumeBar.setOnMouseEntered(_ -> volumeBar.setCursor(Cursor.HAND));
        volumeBar.setOnMouseExited(_ -> volumeBar.setCursor(Cursor.DEFAULT));

        //click the volume button to mute/unmute
        volumeButton.setOnMouseClicked(_ -> {
            if (isMute) {
                isMute = false;
                modifyButton(volumeIcon, hoverVolumeIcon, volumeButton, 15);
            } else {
                isMute = true;
                modifyButton(muteIcon, hoverMuteIcon, volumeButton, 15);
            }
            player.setMute(isMute);
        });

        HBox topBar = new HBox(10, downloadPageButton, playPageButton, perfPageButton, volumeButton, volumeContainer, spacer, warningLabel);
        topBar.setStyle("-fx-padding: 6; -fx-background-color: #ececec;");

        // the largest box, contains the album image and the player info
        StackPane bottomArea = new StackPane();

        VBox playerBar = new VBox(10);
        playerBar.setStyle("-fx-background-color: #ececec; -fx-padding: 10;");

        bottomArea.getChildren().addAll(playerBar, album);
        StackPane.setAlignment(album, Pos.CENTER_LEFT);

        album.setFitWidth(ALBUM_SIZE);
        album.setFitHeight(ALBUM_SIZE);
        album.setPreserveRatio(true);

        //contains the artists name and the controls buttons
        HBox middleBar = new HBox(10);
        HBox controls = new HBox(10);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        Region spacer2 = new Region();
        spacer2.setPrefWidth(30);
        middleBar.getChildren().addAll(artistsLabel, spacer1, controls, spacer2);

        controls.setAlignment(Pos.CENTER);

        HBox currentSong = new HBox(10);
        currentSong.setPadding(new Insets(0, 25, 0, 115));

        //audio visualization
        Canvas spectrum = new Canvas(100, 20);
        player.setSpectrumCanvas(spectrum);

        currentSongName.setStyle("-fx-font-size: 16px;");
        Region spacer3 = new Region();
        HBox.setHgrow(spacer3, Priority.ALWAYS);

        currentSong.getChildren().addAll(currentSongName, spacer3, spectrum);

        Button prevButton = getButton(prevIcon, hoverPrevIcon, BUTTON_SIZE);
        playPauseButton = isPlaying ? getButton(pauseIcon, hoverPauseIcon, BUTTON_SIZE) : getButton(playIcon, hoverPlayIcon, BUTTON_SIZE);
        Button nextButton = getButton(nextIcon, hoverNextIcon, BUTTON_SIZE);
        player.setPlaybackMode(AudioPlayer.CYCLE);
        modeButton = getButton(cycleIcon, hoverCycleIcon, BUTTON_SIZE);

        artistsLabel.setPadding(new Insets(0, 0, 0, 115));
        artistsLabel.setTextFill(Color.GRAY);

        controls.getChildren().addAll(prevButton, playPauseButton, nextButton, modeButton);

        progressBar.setPrefWidth(PROGRESS_WIDTH);
        HBox progressBarContainer = new HBox(10);
        progressBarContainer.getChildren().addAll(currentTimeLabel, progressBar, songDuration);

        progressBarContainer.setAlignment(Pos.CENTER_RIGHT);

        playerBar.getChildren().addAll(currentSong, middleBar, progressBarContainer);

        root.setTop(topBar);
        root.setCenter(playPage);
        root.setBottom(bottomArea);

        downloadPageButton.setOnAction(_ -> root.setCenter(downloadPage));
        playPageButton.setOnAction(_ -> root.setCenter(playPageScrollPane));
        perfPageButton.setOnAction(_ -> {
            Perf perf = new Perf();
            perf.start(primaryStage);
        });

        //click the progress bar to jump to any timeline of the song
        progressBar.setOnMouseClicked(e -> {
            if (player.noTrack()) return;
            double mouseX = e.getX();
            double progressBarWidth = progressBar.getWidth();
            double progress = mouseX / progressBarWidth;
            progress = Math.max(0, Math.min(1, progress));

            progressBar.setProgress(progress);
            player.jumpToProgress(progress);
        });

        playPauseButton.setOnAction(_ -> {
            if (player.noTrack()) return;
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
            player.setMute(isMute);
            isPlaying = true;
            currentSongName.setText(playlist.get(index));
            modifyButton(pauseIcon, hoverPauseIcon, playPauseButton);
        });
        prevButton.setOnAction(_ -> {
            int index = player.playPrevious();
            player.setMute(isMute);
            isPlaying = true;
            currentSongName.setText(playlist.get(index));
            modifyButton(pauseIcon, hoverPauseIcon, playPauseButton);
        });

        //cycle mode, single mode, shuffle mode
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

        album.setOnMouseClicked(_ -> System.out.println("album clicked"));

        album.setOnMouseEntered(_ -> album.setCursor(Cursor.HAND));
        album.setOnMouseExited(_ -> album.setCursor(Cursor.DEFAULT));
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

            Label songName = new Label(playlist.get(i).split("\\.mp3")[0]);
            songName.setStyle("-fx-font-size: 16px;");

            Region spacer = new Region();
            spacer.setMinWidth(Region.USE_COMPUTED_SIZE);
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button playButton = getButton(playIcon, hoverPlayIcon, BUTTON_SIZE);
            Button downloadButton = getButton(downloadIcon, hoverDownloadIcon, BUTTON_SIZE);

            songRow.getChildren().addAll(songNumber, songName, spacer, playButton, downloadButton);
            playPageContent.getChildren().add(songRow);

            songRow.setOnMouseEntered(_ -> songRow.setStyle("-fx-background-color: #ececec;"));
            songRow.setOnMouseExited(_ -> songRow.setStyle("-fx-background-color: transparent;"));

            int finalI = i;
            //the play button on each row of a song
            playButton.setOnAction(_ -> {
                isPlaying = true;
                player.playFromUI(finalI);
                player.setMute(isMute);
                modifyButton(pauseIcon, hoverPauseIcon, playPauseButton);
            });

            // create a download task
            downloadButton.setOnAction(_ -> dm.startDownload(finalI));
        }
    }
}