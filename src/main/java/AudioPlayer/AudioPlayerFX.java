package AudioPlayer;

import UI.App;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.animation.*;
import javafx.beans.property.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

//This is the JavaFX edition of the AudioPlayer.
public class AudioPlayerFX {
    private MediaPlayer mediaPlayer; // MediaPlayer object for songs.
    public List<String> playlist = new ArrayList<>(); // Arraylist playlist.
    private List<Integer> historyList = new ArrayList<>();
    private List<Integer> playedIndices = new ArrayList<>();

    private int currentTrackIndex = 0; // Current index of the playlist
    private BooleanProperty isCycleMode = new SimpleBooleanProperty(true);
    // Removed Previous index of the playlist & Last Play time
    // Solution 1, changed the Thread to the Timeline, where it will not block the
    // download prograss*/

    /*
     * private Thread progressUpdater;
     * private boolean isRunning = true; // Check if thread is running.
     */
    private Timeline proTimeline;
    private final Random random = new Random();
    private int previousTrackIndex = -1;

    public BooleanProperty isBooleanProperty() {
        return isCycleMode;
    }

    public void setIsCycleMode(boolean isCycle) {
        this.isCycleMode.set(isCycle);
    }

    // Initialize the Playlist. This time playlist is initialized at APFX3.java
    public AudioPlayerFX() {
        try (Stream<Path> files = Files.list(Paths.get("./src/main/resources/songs/"))) {
            files.filter(Files::isRegularFile).forEach(file -> playlist.add(file.getFileName().toString()));
        } catch (IOException e) {
            System.out.println("读取文件夹时出错: " + e.getMessage());
        }
    }

    // Use mediaplayer to play. Changed the resumed to playing.
    public void play(int index) {
        if (historyList.isEmpty() || historyList.get(historyList.size() - 1) != index) {
            historyList.add(index);
        }
        currentTrackIndex = index;
    
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    
        try {
            String trackPath = "/songs/" + playlist.get(index);
            URL resource = getClass().getResource(trackPath);
            if (resource != null) {
                Media media = new Media(resource.toString());
                mediaPlayer = new MediaPlayer(media);
                startProgressUpdater();
                mediaPlayer.setOnEndOfMedia(this::playNext);
            } else {
                System.err.println("Track not found: " + trackPath);
            }
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
            /* isRunning = false; */
            System.out.println("Stopped: " + playlist.get(currentTrackIndex));
        }
    }

    // Play next song
    public int playNext() {
        if (isCycleMode.get()) {
            currentTrackIndex = (currentTrackIndex + 1) % playlist.size();
        } else {
            if (playedIndices.size() >= playlist.size()) {
                playedIndices.clear();
            }
    
            int nextTrackIndex;
            do {
                nextTrackIndex = random.nextInt(playlist.size());
            } while (playedIndices.contains(nextTrackIndex));
            
            playedIndices.add(nextTrackIndex);
            currentTrackIndex = nextTrackIndex;
        }
    
        play(currentTrackIndex);
        return currentTrackIndex;
    }
    
    
    

    // Play previous song, but not directly called by the Testing APFX3, called via
    // playUp()
    public int playPrevious() {
        if (historyList.size() > 1) {
            historyList.remove(historyList.size() - 1);
            currentTrackIndex = historyList.get(historyList.size() - 1);
        } else {
            System.out.println("No previous track available.");
            return currentTrackIndex;
        }
    
        play(currentTrackIndex);
        return currentTrackIndex;
    }
    

    // Use Media's duration seconds instead of clip.setMicrosecond from AP(1st
    // Generation)
    public void jumpToTime(double seconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(seconds));
            System.out.println("Jumped to: " + seconds + " seconds");
        }
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

    // This progressUpdater would not be implement yet.
    /*
     * private void startProgressUpdater() {
     * isRunning = true;
     * progressUpdater = new Thread(new ProgressUpdater());
     * progressUpdater.setDaemon(true);
     * progressUpdater.start();
     * }
     * 
     * private class ProgressUpdater implements Runnable {
     * 
     * @Override
     * public void run() {
     * while (isRunning && mediaPlayer != null) {
     * double progress = getPlaybackProgress();
     * Platform.runLater(() -> App.updatePlayProgress(progress));
     * try {
     * Thread.sleep(500);
     * } catch (InterruptedException e) {
     * e.printStackTrace();
     * }
     * }
     * }
     * }
     */

    // Timeline mehod of the progress updater:

    private void startProgressUpdater() {
        if (proTimeline != null) {
            proTimeline.stop();
        }
        proTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            double progress = getPlaybackProgress();
            Platform.runLater(() -> App.updatePlayProgress(progress));
        }));
        proTimeline.setCycleCount(Timeline.INDEFINITE);
        proTimeline.play();
    }

    public List<String> getPlaylist() {
        return playlist;
    }
}
