package AudioPlayer;

import javafx.application.Platform;
import javafx.scene.media.*;
import javafx.util.*;
import java.io.*;
import java.util.*;

//This is the JavaFX edition of the AudioPlayer.
public class AudioPlayerFX {
    private MediaPlayer mediaPlayer; // MediaPlayer object for songs.
    private List<String> playlist; // Arraylist playlist.
    private int currentTrackIndex = 0; // Current index of the playlist
    // Removed Previous index of the playlist & Last Play time
    private Thread progressUpdater; // New Thread progress Updater
    private boolean isRunning = true; // Check if thread is running.

    // Initialize the Playlist. This time playlist is initialized at APFX3.java
    public AudioPlayerFX(List<String> playlist) {
        this.playlist = playlist;
        loadTrack(currentTrackIndex);
    }

    // Load the track from specific index
    private void loadTrack(int index) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        // Get the media instead of get the audio input stream in AP(1st Generation)
        try {
            Media media = new Media(new File(playlist.get(index)).toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            startProgressUpdater();
        } catch (Exception e) {
            System.err.println("Error loading track: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Use mediaplayer to play. Changed the resumed to playing.
    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            System.out.println("Playing: " + playlist.get(currentTrackIndex));
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
    public void playNext() {
        if (currentTrackIndex < playlist.size() - 1) {
            currentTrackIndex++; // set to next song if avaliable
        } else {
            currentTrackIndex = 0; // if not set the original
        }
        loadTrack(currentTrackIndex);
        play();
    }

    // Play previous song, but not directly called by the Testing APFX3, called via playUp()
    public void playPrevious() {
        if (currentTrackIndex > 0) {
            currentTrackIndex--;
        } else {
            currentTrackIndex = playlist.size() - 1;
        }
        loadTrack(currentTrackIndex);
        play();
    }

    public void playUp() {
        if (mediaPlayer != null) {
            // 5s principle, if pressed it in 5s, call play previous.
            // if the song started to play after 5s, play up means that replay
            if (mediaPlayer.getCurrentTime().toSeconds() < 5) {
                playPrevious();
            } else {

                mediaPlayer.seek(Duration.ZERO);
                play();
            }
        }
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
                return (currentTime.toMillis() / totalDuration.toMillis()) * 100;
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
                Platform.runLater(() -> System.out.println("Current Progress: " + progress + "%"));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
