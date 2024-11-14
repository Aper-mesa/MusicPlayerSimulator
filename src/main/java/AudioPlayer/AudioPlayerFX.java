package AudioPlayer;

import UI.App;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.util.Objects;

//This is the JavaFX edition of the AudioPlayer.
public class AudioPlayerFX {
    private MediaPlayer mediaPlayer; // MediaPlayer object for songs.
    public List<String> playlist = new ArrayList<>(); // Arraylist playlist.
    private int currentTrackIndex = 0; // Current index of the playlist
    // Removed Previous index of the playlist & Last Play time
    private Thread progressUpdater; // New Thread progress Updater
    private boolean isRunning = true; // Check if thread is running.


    // Initialize the Playlist. This time playlist is initialized at APFX3.java
    public AudioPlayerFX() {
        playlist.add("/songs/Dissonant Harmony.wav");
        playlist.add("/songs/Harmonious Dissonance.wav");
        playlist.add("/songs/Half Moon.mp3");
    }

    // Use mediaplayer to play. Changed the resumed to playing.
    public void play(int index) {
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
        }
        try {
            String trackPath = playlist.get(index);
            URL resource = getClass().getResource(trackPath);
            if (resource != null) {
                Media media = new Media(resource.toString());
                mediaPlayer = new MediaPlayer(media);
                startProgressUpdater();
            } else {
                System.err.println("Track not found: " + trackPath);
            }

            startProgressUpdater();
        } catch (Exception e) {
            System.err.println("Error loading track: " + e.getMessage());
            e.printStackTrace();
        }
        if (mediaPlayer != null) {
            mediaPlayer.play();
            System.out.println("Playing: " + playlist.get(currentTrackIndex));
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
            isRunning = false;
            System.out.println("Stopped: " + playlist.get(currentTrackIndex));
        }
    }

    // Play next song
    public int playNext() {
        if (currentTrackIndex < playlist.size() - 1) {
            currentTrackIndex++; // set to next song if avaliable
        } else {
            currentTrackIndex = 0; // if not set the original
        }
        play(currentTrackIndex);
        return currentTrackIndex;
    }

    // Play previous song, but not directly called by the Testing APFX3, called via playUp()
    public int playPrevious() {
        if (currentTrackIndex > 0) {
            currentTrackIndex--;
        } else {
            currentTrackIndex = playlist.size() - 1;
        }
        play(currentTrackIndex);
        return currentTrackIndex;
    }

    // Use Media's duration seconds instead of clip.setMicrosecond from AP(1st Generation)
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

    // Start the thread via progressUpdater
    private void startProgressUpdater() {
        isRunning = true;
        progressUpdater = new Thread(new ProgressUpdater());
        progressUpdater.setDaemon(true);
        progressUpdater.start();
    }

    private class ProgressUpdater implements Runnable {
        @Override
        public void run() {
            while (isRunning && mediaPlayer != null) {
                double progress = getPlaybackProgress();
                Platform.runLater(() -> App.updatePlayProgress(progress));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<String> getPlaylist()
    {
        List<String> formattedPlaylist = new ArrayList<>();
        for (String track : playlist) {
            String[] parts = track.split("/");
            String fileName = parts[parts.length - 1];

            String formattedName = fileName.split("\\.")[0];

            formattedPlaylist.add(formattedName);
        }
        return formattedPlaylist;
    }
}
