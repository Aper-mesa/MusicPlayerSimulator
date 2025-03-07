package AudioPlayer;

import UI.App;
import UI.Perf;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/* AudioPlayer module use Two Classes to make the playing function works
 * AudioPlayer focus on Creating the Media Player Powered by JavaFX
 * Offers Playback mode management, Play, Pause, Up and Next Function
 * Realtime Progress Update
 * Realtime Audio Spectrum
 * Playlist Management Located at Playlist.java*/

public class AudioPlayer {
    public static final int CYCLE = 0;
    public static final int SHUFFLE = 1;
    public static final int SINGLE = 2;
    private static final int DELAY_FRAMES = 3;
    private final Playlist playlist;
    private final Queue<float[]> magnitudeQueue = new LinkedList<>();
    //Initialize
    private MediaPlayer mediaPlayer;
    private Timeline proTimeline;
    private Media media;
    private Canvas spectrumCanvas;
    private int playbackMode = 0;

    public AudioPlayer() {
        playlist = new Playlist();
        Perf perf = new Perf();
        perf.initializeSongData(playlist.getFilePlaylist().size());
        startMemoryMonitoring();
    }

    public List<String> getPlaylist() {
        return playlist.getFilePlaylist();
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

    //The Playback mode will use Playlist class to re-manage the Playlist, due to the Playback order is managed by the Playlist.java.
    public void setPlaybackMode(int mode) {
        this.playbackMode = mode;
        if (playbackMode == SHUFFLE) {
            //Refresh the Playback Playlist and Shuffle Them
            playlist.saveCurrentTrackIndex();
            int currentTrackIndex = playlist.getCurrentTrackIndex();
            playlist.restorePlaylistOrder();
            playlist.selectTrackAndShuffle(currentTrackIndex);
            System.out.println("Shuffled playback mode activated.");
        } else if (playbackMode == CYCLE) {
            //Refresh the Playback Playlist in the original Order
            String currentTrack = playlist.getCurrentTrack();
            playlist.restorePlaylistOrder();
            playlist.updateCurrentTrackIndexByName(currentTrack);
            System.out.println("Sequential playback mode activated.");
        } else if (playbackMode == SINGLE) {
            playlist.switchToSingleTrackPlaylist();
            System.out.println("Single-track loop mode activated.");
        }
    }

    // Get the track path, then play the songs. This will make a file check in playlist and then play.
    public void play(int index) {
        playlist.setCurrentTrackIndex(index);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            if (proTimeline != null) {
                proTimeline.stop();
                proTimeline.getKeyFrames().clear();
            }
            System.gc();
        }
        try {
            String trackPath = "/songs/" + playlist.getCurrentTrack();
            URL resource = getClass().getResource(trackPath);
            if (resource != null) {
                media = new Media(resource.toString());
                long startTime = System.currentTimeMillis();

                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setOnReady(() -> {
                    long endTime = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;
                    Perf.updateSongOpenTime(index + 1, elapsedTime / 1000.0);

                    App.updateAlbum((Image) media.getMetadata().get("image"),
                            (String) media.getMetadata().get("title"),
                            media.getDuration(), (String) media.getMetadata().get("artist"));
                    startProgressUpdater();
                    if (spectrumCanvas != null) {
                        enableAudioSpectrum();
                    }
                    logMemoryUsage();
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

    //This is the Function from the UI, and identify if the playlist should be shuffled after.
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

    //Simply use Media Player's play for resume.
    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    //Simply use Media Player's pause.
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    //Use the playlist order for the next function
    public int playNext() {
        // Stop and dispose the old media player
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            System.gc();  // Request garbage collection to free memory
        }
        playlist.saveCurrentTrackIndex();
        playlist.nextTrack();
        play(playlist.getCurrentTrackIndex());
        return playlist.getCurrentTrackIndex();
    }

    public int playPrevious() {
        // Stop and dispose the old media player
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            System.gc();  // Request garbage collection to free memory
        }
        playlist.previousTrack();
        play(playlist.getCurrentTrackIndex());
        return playlist.getCurrentTrackIndex();
    }

    //The AudioSpectrum are using the AudioSpectrum from JavaFX Media Player.
    public void enableAudioSpectrum() {
        if (mediaPlayer != null && spectrumCanvas != null) {
            magnitudeQueue.clear();
            mediaPlayer.setAudioSpectrumInterval(1.0 / 60.0);
            mediaPlayer.setAudioSpectrumNumBands(20);
            mediaPlayer.setAudioSpectrumThreshold(-60);

            GraphicsContext gc = spectrumCanvas.getGraphicsContext2D();
            mediaPlayer.setAudioSpectrumListener((_, _, magnitudes, _) -> {

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
                                    (spectrumCanvas.getHeight()) * ((delayedMagnitudes[i] + 60) / 60.0));
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
        proTimeline = new Timeline(new KeyFrame(Duration.seconds(0.01), _ -> {
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

    private void startMemoryMonitoring() {
        ScheduledExecutorService memoryMonitorExecutor = Executors.newScheduledThreadPool(1);
        memoryMonitorExecutor.scheduleAtFixedRate(() -> {
            long usedMemory = getUsedMemory();
            Platform.runLater(() -> Perf.updateMemoryUsage(usedMemory + " MB"));
        }, 0, 1, TimeUnit.SECONDS); // memory usage updates in every 0.5 sec
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024); // 转换为KB
    }

    private void logMemoryUsage() {
        long usedMemory = getUsedMemory();
        System.out.println("Memory Used After Operation: " + usedMemory + " MB");
    }

    //Offer a way to jump to a point of the track according to the progress.
    public void jumpToProgress(double progress) {
        if (mediaPlayer != null && mediaPlayer.getTotalDuration() != null) {
            Duration totalDuration = mediaPlayer.getTotalDuration();
            mediaPlayer.seek(totalDuration.multiply(progress));
        }
    }

    //When the song coming to end, automatically calls the play next.
    private void handleTrackEnd() {
        playNext();
    }

    public boolean noTrack() {
        return mediaPlayer == null ||
                mediaPlayer.getMedia() == null ||
                mediaPlayer.getStatus() == MediaPlayer.Status.UNKNOWN ||
                mediaPlayer.getStatus() == MediaPlayer.Status.DISPOSED;
    }

    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }

    public void setMute(boolean mute) {
        if (mediaPlayer != null) {
            mediaPlayer.setMute(mute);
        }
    }
}
