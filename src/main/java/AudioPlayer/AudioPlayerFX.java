package AudioPlayer;

import UI.App;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import java.util.Collections;

//This is the JavaFX edition of the AudioPlayer.
public class AudioPlayerFX {
    private MediaPlayer mediaPlayer; // MediaPlayer object for songs.
    public List<String> playlist = new ArrayList<>(); // Arraylist playlist.
    private List<String> shuffledPlaylist = new ArrayList<>();
    private final List<Integer> historyList = new ArrayList<>();

    private int currentTrackIndex = 0; // Current index of the playlist
    private final BooleanProperty isCycleMode = new SimpleBooleanProperty(true);

    // Removed Previous index of the playlist & Last Play time
    // Solution 1, changed the Thread to the Timeline, where it will not block the
    // download progress*/

    /*
     * private Thread progressUpdater;
     * private boolean isRunning = true; // Check if thread is running.
     */
    private Timeline proTimeline;
    private final Random random = new Random();
    Media media;

    // Initialize the Playlist. This time playlist is initialized at APFX3.java
    public AudioPlayerFX() {
        try (Stream<Path> files = Files.list(Paths.get("./src/main/resources/songs/"))) {
            files.filter(Files::isRegularFile).forEach(file -> playlist.add(file.getFileName().toString()));
        } catch (IOException e) {
            System.out.println("读取文件夹时出错: " + e.getMessage());
        }
        shuffledPlaylist.addAll(playlist);
    }

    // Set the cycle mode.
    public void setIsCycleMode(boolean isCycle) {
        this.isCycleMode.set(isCycle);
        if (!isCycle) {
            shufflePlaylist();
        } else {
            restoreOriginalPlaylist();
        }
    }

    private void shufflePlaylist() {
        Collections.shuffle(shuffledPlaylist);
    }

    private void restoreOriginalPlaylist() {
        shuffledPlaylist.clear();
        shuffledPlaylist.addAll(playlist);
    }

    // Use mediaPlayer to play. Changed the resumed to playing.
    public void play(int index) {
        if (historyList.isEmpty() || historyList.get(historyList.size() - 1) != index) {
            historyList.add(index);
        }
        currentTrackIndex = index;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        try {
            String trackPath = "/songs/" + (isCycleMode.get() ? playlist.get(index) : shuffledPlaylist.get(index));
            URL resource = getClass().getResource(trackPath);
            if (resource != null) {
                media = new Media(resource.toString());
                mediaPlayer = new MediaPlayer(media);
                startProgressUpdater();
                mediaPlayer.setOnEndOfMedia(this::playNext);
            } else {
                System.err.println("Track not found: " + trackPath);
            }
            String trackName = isCycleMode.get() ? playlist.get(index) : shuffledPlaylist.get(index);
            mediaPlayer.setOnReady(() -> App.updatePlayBar(media.getDuration(), trackName));
            mediaPlayer.play();
            System.out.println("Playing: " + playlist.get(index));
        } catch (Exception e) {
            System.err.println("Error loading track: " + e.getMessage());
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            System.out.println("Resumed: " + playlist.get(currentTrackIndex));
        }
    }

    // pause the current playing, but not stop the thread.
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            // Removed the saving state of the pausePosition in AP(1st Generation)
            System.out.println("Paused: " + playlist.get(currentTrackIndex));
        }
    }

    // Stop playing by add false state for isRunning() the thread.
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            System.out.println("Stopped: " + playlist.get(currentTrackIndex));
        }
    }

    // Play next song
    public int playNext() {
        if (isCycleMode.get()) {
            currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
        } else {
            if (historyList.size() == shuffledPlaylist.size()) {
                historyList.clear();
                shufflePlaylist();
            }
            for (int i = 0; i < (shuffledPlaylist.size()); i++) {
                if (!historyList.contains(i)) {
                    play(i);
                    return i;
                }
            }

        }
        play(currentTrackIndex);
        return currentTrackIndex;
    }

    // Play previous song, but not directly called by the Testing APFX3, called via
    // playUp()
    public int playPrevious() {
        if (historyList.size() > 1) {
            historyList.removeLast();
            currentTrackIndex = historyList.getLast();
        } else {
            System.out.println("No previous track available.");
            return currentTrackIndex;
        }

        play(currentTrackIndex);
        return currentTrackIndex;
    }

    // Get the percentage of the playing status.
    public double getPlaybackProgress() {
        if (mediaPlayer != null) {
            Duration currentTime = mediaPlayer.getCurrentTime();
            Duration totalDuration = mediaPlayer.getTotalDuration();
            if (totalDuration != null && totalDuration.greaterThan(Duration.ZERO)) {
                return (currentTime.toMillis() / totalDuration.toMillis());
            }
        }
        return 0.0;
    }

    // Timeline method of the progress updater:
    private void startProgressUpdater() {
        if (proTimeline != null) {
            proTimeline.stop();
        }
        proTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), _ -> {
            double progress = getPlaybackProgress();
            Platform.runLater(() -> App.updatePlayProgress(progress));
        }));
        proTimeline.setCycleCount(Timeline.INDEFINITE);
        proTimeline.play();
        Timeline currentTime = new Timeline(new KeyFrame(Duration.seconds(1),
                _ -> Platform.runLater(() -> App.updateCurrentTime(mediaPlayer.getCurrentTime()))));
        currentTime.setCycleCount(Timeline.INDEFINITE);
        currentTime.play();
    }

    public List<String> getPlaylist() {
        return playlist;
    }

    // Use double progressinstead of clip.setMicrosecond from AP(1st
    // Generation)
    public void jumpToProgress(double progress) {
        if (mediaPlayer != null && mediaPlayer.getTotalDuration() != null) {
            Duration totalDuration = mediaPlayer.getTotalDuration();
            double targetTimeInSeconds = totalDuration.toSeconds() * progress;

            mediaPlayer.seek(Duration.seconds(targetTimeInSeconds));
            System.out.println("Jumped to: " + (progress * 100) + "% of the track");
        } else {
            System.out.println("MediaPlayer or track duration is not available.");
        }
    }

}
