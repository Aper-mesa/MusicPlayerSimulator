import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class AudioPlayer implements Runnable {
    private Clip clip; // Clip object for songs.
    private long pausePosition = 0; // The position when paused.
    private List<String> playlist = new ArrayList<>(); //Arraylist playlist
    private int currentTrackIndex = 0; //Current index of the playlist
    private int previousTrackIndex = -1; //Previous index of the playlist
    private long lastPlayTime = 0; //Last Play time
    private Thread playThread; //Thread using to play.

    //Initialize the playlist and load the 1st song.
    public AudioPlayer(List<String> playlist) {
        this.playlist = playlist;
        loadTrack(currentTrackIndex);
    }
    //Load the track from specific index
    private void loadTrack(int index) {

        try {
            if (clip != null && clip.isOpen()) {
                clip.close();
            }
            //get the audio input stream and open the clip.
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(playlist.get(index)));
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            //Linelistener for when the song finished, play next automatically.
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP && clip.getMicrosecondPosition() == clip.getMicrosecondLength()) {
                    playNext();
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    //use thread to play
    public void play() {
        playThread = new Thread(this);
        playThread.start();
    }
    //pause the current playing, but not stop the thread.
    public void pause() {
        if (clip != null && clip.isRunning()) {
            pausePosition = clip.getMicrosecondPosition();
            clip.stop();
            System.out.println("Paused: " + playlist.get(currentTrackIndex));
        }
    }
    //stop playing by interrupt the thread.
    public void stop() {
        if (playThread != null) {
            playThread.interrupt();
        }

        if (clip != null) {
            clip.stop();
            clip.close();
            pausePosition = 0; //reset the pausing position
            System.out.println("Stopped: " + playlist.get(currentTrackIndex));
        }
    }

    @Override
    public void run() {
        try {
            if (clip != null) {
                clip.setMicrosecondPosition(pausePosition); //set to the pausing position and then start.
                clip.start();
                lastPlayTime = System.currentTimeMillis(); //record the current playing time
                System.out.println("Playing: " + playlist.get(currentTrackIndex));
                //check the status every 100ms.
                while (clip.isRunning() && !Thread.interrupted()) {
                    Thread.sleep(100);
                }
                if (Thread.interrupted()) {
                    System.out.println("Playback interrupted.");
                } else {
                    System.out.println("Playback completed.");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread interrupted while waiting.");
        }
    }

    //Play next song.
    public void playNext() {
        if (currentTrackIndex < playlist.size() - 1) {
            previousTrackIndex = currentTrackIndex;
            currentTrackIndex++; //set to next song if avaliable.
        } else {
            previousTrackIndex = currentTrackIndex;
            currentTrackIndex = 0; //set to current song if there are no song at left.
        }
        pausePosition = 0; //reset pausing position.
        loadTrack(currentTrackIndex); //load new song.
        play();
    }

    //play previous song.
    public void playPrevious() {
        //
        if (previousTrackIndex != -1) {
            currentTrackIndex = previousTrackIndex;
            previousTrackIndex = -1;
            pausePosition = 0;
            loadTrack(currentTrackIndex);
            play();
        }
    }

    public void playUp() {
        if (clip != null && clip.isRunning()) {
            long currentTime = clip.getMicrosecondPosition();
            //5s principle, if the song started to play after 5s, play up means that replay that song
            if (currentTime >= 5000000) {
                pausePosition = 0;
                loadTrack(currentTrackIndex);
                play();
            } else {
                //5s principle, if pressed it in 5s, call play previous.
                playPrevious();
            }
        } else {
            play();
        }
    }

    public void jumpToTime(long timeInMicroseconds) {
        if (clip != null && clip.isOpen()) {

            //set the clip to the parameter.
            clip.setMicrosecondPosition(timeInMicroseconds);
            pausePosition = timeInMicroseconds;

            //transfer the microsecond to normal time format.
            long currentTimeInSeconds = timeInMicroseconds / 1000000;
            long currentHours = currentTimeInSeconds / 3600;
            long currentMinutes = (currentTimeInSeconds % 3600) / 60;
            long currentSeconds = currentTimeInSeconds % 60;

            System.out.println(String.format("Jumped to: %02d:%02d:%02d", currentHours, currentMinutes, currentSeconds));

            play();
        }
    }



    public static void main(String[] args) {
        List<String> playlist = new ArrayList<>();
        //ArrayList include Two test Track from Local Folder.
        playlist.add("/Users/ZENSOMNIA-If_1/Music/Caught_Fire.wav");
        playlist.add("/Users/ZENSOMNIA-If_1/Music/Project 23 MR - 2021:7:9, 10.00 PM.wav");

        AudioPlayer player = new AudioPlayer(playlist);

        //Play the first song.
        player.play();

        try {
            Thread.sleep(3000);
            //after 3s paused.
            player.pause();
            //after 1s continue.
            Thread.sleep(1000);
            player.play();
            //after 10s, jumped to the 20s of the song, press play.
            Thread.sleep(10000);
            player.jumpToTime(20000000);
            player.play();

            //after 10s, jumped to the 35s of the song, press play.
            Thread.sleep(10000);
            player.jumpToTime(35000000);

            //after 10s, jumped to the 0, press play.
            Thread.sleep(10000);
            player.jumpToTime(10000);

            //after 10s, play next song, press play.
            Thread.sleep(10000);
            player.playNext();

            //after 10s, play up song but will play it again due to 5s principle, press play.
            Thread.sleep(10000);
            player.playUp();

            //after 3s, play up song, press play
            Thread.sleep(3000);
            player.playUp();

            //after 10s, stop.
            Thread.sleep(10000);
            player.stop();


        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
