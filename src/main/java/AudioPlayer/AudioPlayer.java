package AudioPlayer;

import UI.App;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AudioPlayer {
    private MediaPlayer mediaPlayer;
    private final Playlist playlist;
    private Timeline proTimeline;
    private Media media;
    private Canvas spectrumCanvas;
    private int playbackMode = 0;
    public static final int CYCLE = 0;
    public static final int SHUFFLE = 1;
    public static final int SINGLE = 2;

    private static final int DELAY_FRAMES = 3;
    private Queue<float[]> magnitudeQueue = new LinkedList<>();

    public AudioPlayer() {
        playlist = new Playlist();
    }

    public List<String> getPlaylist() {
        return playlist.getFilePlaylist();
    }

    public int getPlaybackMode() {
        return playbackMode;
    }

    public void setSpectrumCanvas(Canvas canvas) {
        this.spectrumCanvas = canvas;
    }

    public boolean isSingle() {
        return playbackMode == SINGLE;
    }

    public boolean isShuffle() {
        return playbackMode == SHUFFLE;
    }

    public boolean isCycle() {
        return playbackMode == CYCLE;
    }

    public void setPlaybackMode(int mode) {
        this.playbackMode = mode;
        if (playbackMode == SHUFFLE) {
            playlist.saveCurrentTrackIndex();
            int currentTrackIndex = playlist.getCurrentTrackIndex();
            playlist.restorePlaylistOrder();
            playlist.selectTrackAndShuffle(currentTrackIndex);
            System.out.println("Shuffled playback mode activated.");
        } else if (playbackMode == CYCLE) {
            String currentTrack = playlist.getCurrentTrack();
            playlist.restorePlaylistOrder();
            playlist.updateCurrentTrackIndexByName(currentTrack);
            System.out.println("Sequential playback mode activated.");
        } else if (playbackMode == SINGLE) {
            playlist.switchToSingleTrackPlaylist();
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
                mediaPlayer.setOnReady(() -> {
                    App.updateAlbum((Image) media.getMetadata().get("image"));
                    String trackName = playlist.getCurrentTrack();
                    App.updatePlayBar(media.getDuration(), trackName);
                    startProgressUpdater();

                    if (spectrumCanvas != null) {
                        enableAudioSpectrum();
                    }
                });
                mediaPlayer.setOnEndOfMedia(this::handleTrackEnd);

                mediaPlayer.play();
            } else {
                System.err.println("Track not found: " + trackPath);
            }
        } catch (Exception e) {
            System.err.println("Error loading track: " + e.getMessage());
        }
    }

    public void playFromUI(int uiIndex) {
        if (uiIndex < 0 || uiIndex >= playlist.getFilePlaylist().size()) {
            System.err.println("Invalid index from UI: " + uiIndex);
            return;
        }

        if (isShuffle()) {
            playlist.selectTrackAndShuffle(uiIndex);
        } else if (isSingle()) {
            playlist.switchToSingleTrackPlaylist(uiIndex);
        } else {
            playlist.setCurrentTrackIndex(uiIndex);
        }
        play(playlist.getCurrentTrackIndex());
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public int playNext() {
        playlist.saveCurrentTrackIndex();
        playlist.nextTrack();
        play(playlist.getCurrentTrackIndex());
        return playlist.getCurrentTrackIndex();
    }

    public int playPrevious() {
        playlist.previousTrack();
        play(playlist.getCurrentTrackIndex());
        return playlist.getCurrentTrackIndex();
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

    public void restorePlaylistOrder() {
        playlist.restorePlaylistOrder();
    }

    public void reloadFilePlaylist() {
        playlist.reloadFilePlaylist();
    }

    public void enableAudioSpectrum() {
        if (mediaPlayer != null && spectrumCanvas != null) {
            mediaPlayer.setAudioSpectrumInterval(1.0 / 60.0);
            mediaPlayer.setAudioSpectrumNumBands(64);
            mediaPlayer.setAudioSpectrumThreshold(-60);

            GraphicsContext gc = spectrumCanvas.getGraphicsContext2D();
            mediaPlayer.setAudioSpectrumListener((timestamp, duration, magnitudes, phases) -> {

                float[] magnitudesCopy = new float[magnitudes.length];
                System.arraycopy(magnitudes, 0, magnitudesCopy, 0, magnitudes.length);

                magnitudeQueue.offer(magnitudesCopy);
                if (magnitudeQueue.size() > DELAY_FRAMES) {
                    float[] delayedMagnitudes = magnitudeQueue.poll();

                    Platform.runLater(() -> {
                        gc.clearRect(0, 0, spectrumCanvas.getWidth(), spectrumCanvas.getHeight());
                        double width = spectrumCanvas.getWidth() / delayedMagnitudes.length;

                        for (int i = 0; i < delayedMagnitudes.length; i++) {
                            double height = Math.max(0,
                                    (spectrumCanvas.getHeight() / 2) * ((delayedMagnitudes[i] + 60) / 60.0));
                            gc.fillRect(i * width, spectrumCanvas.getHeight() - height, width - 2, height);
                        }
                    });
                }
            });
        } else if (spectrumCanvas == null) {
            System.err.println("Spectrum Canvas is not set.");
        } else {
            System.err.println("MediaPlayer is null. Cannot enable audio spectrum.");
        }
    }

    private void startProgressUpdater() {
        if (proTimeline != null) {
            proTimeline.stop();
        }
        proTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), _ -> {
            if (mediaPlayer != null) {
                Duration currentTime = mediaPlayer.getCurrentTime();
                Duration totalDuration = mediaPlayer.getTotalDuration();
                if (currentTime != null && totalDuration != null && totalDuration.greaterThan(Duration.ZERO)) {
                    double progress = currentTime.toMillis() / totalDuration.toMillis();
                    Platform.runLater(() -> {
                        App.updatePlayProgress(progress);
                        App.updateCurrentTime(currentTime);
                    });
                }
            }
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

    private void handleTrackEnd() {
        playNext();
    }

    public boolean hasTrack() {
        return mediaPlayer != null &&
                mediaPlayer.getMedia() != null &&
                mediaPlayer.getStatus() != MediaPlayer.Status.UNKNOWN &&
                mediaPlayer.getStatus() != MediaPlayer.Status.DISPOSED;
    }

}
