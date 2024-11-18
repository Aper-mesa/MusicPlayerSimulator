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
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class AudioPlayerIII {
    private MediaPlayer mediaPlayer;
    private List<String> filePlaylist = new ArrayList<>();
    private List<String> playingPlaylist = new ArrayList<>();
    private List<String> playingHistory = new ArrayList<>();
    private int currentTrackIndex = 0;
    private Timeline proTimeline;
    private Media media;
    private BooleanProperty isCycle = new SimpleBooleanProperty(false); // BooleanProperty for cycle mode

    public AudioPlayerIII() {
        loadFilePlaylist();
        playingPlaylist.addAll(filePlaylist);
    }

    private void loadFilePlaylist() {
        filePlaylist.clear();
        try (Stream<Path> files = Files.list(Paths.get("./src/main/resources/songs/"))) {
            files.filter(Files::isRegularFile).forEach(file -> filePlaylist.add(file.getFileName().toString()));
        } catch (IOException e) {
            System.out.println("Error reading the songs directory: " + e.getMessage());
        }
    }

    public List<String> getFilePlaylist() {
        return filePlaylist;
    }

    public List<String> getPlaylist() {
        return playingPlaylist;
    }

    public void addToHistory(String track) {
        playingHistory.add(track);
    }

    public void setIsCycleMode(boolean isCycleMode) {
        this.isCycle.set(isCycleMode);
        if (isCycleMode) {
            playingPlaylist.clear();
            playingPlaylist.addAll(filePlaylist);

            if (mediaPlayer != null) {
                String currentTrack = playingPlaylist.get(currentTrackIndex);
                currentTrackIndex = playingPlaylist.indexOf(currentTrack);
            }
            System.out.println("Cycle mode activated, playing in order.");
        } else {
            shufflePlaylist();
            System.out.println("Shuffle mode activated, playing randomly.");
        }
    }

    public BooleanProperty isCycleProperty() {
        return isCycle;
    }

    private void shufflePlaylist() {
        Collections.shuffle(playingPlaylist);
        if (mediaPlayer != null) {
            String currentTrack = playingPlaylist.get(currentTrackIndex);
            int newIndex = playingPlaylist.indexOf(currentTrack);
            if (newIndex != 0) {
                currentTrackIndex = newIndex + 1;
            }
        } else {
            currentTrackIndex = 0;
        }
        System.out.println("Playing shuffled playlist starting from index: " + currentTrackIndex);
    }

    public void play(int index) {
        currentTrackIndex = index;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        try {
            String trackPath = "/songs/" + playingPlaylist.get(currentTrackIndex);
            URL resource = getClass().getResource(trackPath);
            if (resource != null) {
                media = new Media(resource.toString());
                mediaPlayer = new MediaPlayer(media);
                startProgressUpdater();
                mediaPlayer.setOnEndOfMedia(this::playNext);
            } else {
                System.err.println("Track not found: " + trackPath);
            }
            String trackName = playingPlaylist.get(currentTrackIndex);
            mediaPlayer.setOnReady(() -> App.updatePlayBar(media.getDuration(), trackName));
            mediaPlayer.play();
            System.out.println("Playing: " + trackName);
        } catch (Exception e) {
            System.err.println("Error loading track: " + e.getMessage());
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            String trackName = playingPlaylist.get(currentTrackIndex);
            System.out.println("Resumed: " + trackName);
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            String trackName = playingPlaylist.get(currentTrackIndex);
            System.out.println("Paused: " + trackName);
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            String trackName = playingPlaylist.get(currentTrackIndex);
            System.out.println("Stopped: " + trackName);
        }
    }

    public int playNext() {
        if (isCycle.get()) {
            currentTrackIndex = (currentTrackIndex + 1) % playingPlaylist.size();
        } else {
            currentTrackIndex = (currentTrackIndex + 1) % playingPlaylist.size();
        }
        play(currentTrackIndex);
        return currentTrackIndex;
    }

    public int playPrevious() {
        if (!playingHistory.isEmpty() && currentTrackIndex > 0) {
            currentTrackIndex = playingHistory.size() - 1;
        } else {
            currentTrackIndex = (currentTrackIndex - 1 + playingPlaylist.size()) % playingPlaylist.size();
        }
        play(currentTrackIndex);
        return currentTrackIndex;
    }

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

    public void reloadFilePlaylist() {
        loadFilePlaylist();
        System.out.println("File Playlist reloaded.");
    }
}
