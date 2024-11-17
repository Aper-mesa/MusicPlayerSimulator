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

import java.net.URL;
import java.util.List;

public class AudioPlayerIV {
    private MediaPlayer mediaPlayer;
    private Playlist playlist;
    private Timeline proTimeline;
    private Media media;
    private BooleanProperty isCycle = new SimpleBooleanProperty(false);
    private int playbackMode = 0;

    public AudioPlayerIV() {
        playlist = new Playlist();
    }

    public List<String> getPlaylist() {
        return playlist.getFilePlaylist();
    }

    public BooleanProperty isCycleProperty() {
        return isCycle;
    }

    public void setIsCycleMode(boolean isCycleMode) {
        isCycle.set(isCycleMode);
        playlist.setCycleMode(isCycleMode);
    }

    public int getPlaybackMode() {
        return playbackMode;
    }

    public void setPlaybackMode(int mode) {
        if (mode < 0 || mode > 2) {
            throw new IllegalArgumentException("Invalid playback mode. Use 0 (Sequential), 1 (Shuffled), or 2 (Single-track loop).");
        }
        this.playbackMode = mode;
        if (playbackMode == 1) {
            shufflePlaylistStartingFromCurrent();
            System.out.println("Shuffled playback mode activated.");
        } else if (playbackMode == 0) {
            playlist.restorePlaylistOrder();
            System.out.println("Sequential playback mode activated.");
        } else {
            System.out.println("Single-track loop mode activated.");
        }
    }

    public void play(int index) {
        playlist.setCurrentTrackIndex(index);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        try {
            String trackPath = "/songs/" + playlist.getCurrentTrack();
            URL resource = getClass().getResource(trackPath);
            if (resource != null) {
                media = new Media(resource.toString());
                mediaPlayer = new MediaPlayer(media);
                startProgressUpdater();
                mediaPlayer.setOnEndOfMedia(this::playNext);
            } else {
                System.err.println("Track not found: " + trackPath);
            }
            String trackName = playlist.getCurrentTrack();
            mediaPlayer.setOnReady(() -> App.updatePlayBar(playlist.getCurrentTrackIndex(), media.getDuration(), trackName));
            mediaPlayer.play();
            System.out.println("Playing: " + trackName);
        } catch (Exception e) {
            System.err.println("Error loading track: " + e.getMessage());
        }
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            System.out.println("Resumed: " + playlist.getCurrentTrack());
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            System.out.println("Paused: " + playlist.getCurrentTrack());
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            System.out.println("Stopped: " + playlist.getCurrentTrack());
        }
    }

    public int playNext() {
        playlist.nextTrack();
        play(playlist.getCurrentTrackIndex());
        return playlist.getCurrentTrackIndex();
    }

    public int playPrevious() {
        playlist.previousTrack();
        play(playlist.getCurrentTrackIndex());
        return playlist.getCurrentTrackIndex();
    }

    private void onTrackEnd() {
        if (playbackMode == 2) {
            play(playlist.getCurrentTrackIndex());
        } else {
            playNext();
        }
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

    private void shufflePlaylistStartingFromCurrent() {
        int currentIndex = playlist.getCurrentTrackIndex();
        playlist.shufflePlaylist();
        String currentTrack = playlist.getFilePlaylist().get(currentIndex);
        playlist.getFilePlaylist().remove(currentIndex);
        playlist.getFilePlaylist().add(0, currentTrack);
        playlist.setCurrentTrackIndex(0);
        System.out.println("Shuffled playlist with current track as first.");
    }

    public void reloadFilePlaylist() {
        playlist.reloadFilePlaylist();
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
    }

    public void jumpToProgress(double progress) {
        if (mediaPlayer != null && mediaPlayer.getTotalDuration() != null) {
            Duration totalDuration = mediaPlayer.getTotalDuration();
            mediaPlayer.seek(totalDuration.multiply(progress));
        }
    }
}
