package AudioPlayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Playlist {
    private List<String> filePlaylist = new ArrayList<>();
    private ObservableList<String> playingPlaylist = FXCollections.observableArrayList();

    private List<String> playingHistory = new ArrayList<>();
    private int currentTrackIndex = 0;
    private int savedCurrentTrackIndex = -1;

    public Playlist() {
        loadFilePlaylist();
        playingPlaylist.addAll(filePlaylist);
    }

    private void loadFilePlaylist() {
        filePlaylist.clear();
        try (Stream<Path> files = Files.list(Paths.get("./src/main/resources/songs/"))) {
            files.filter(Files::isRegularFile)
                    .map(file -> file.getFileName().toString())
                    .sorted()
                    .forEach(filePlaylist::add);
        } catch (IOException e) {
            System.out.println("Error reading the songs directory: " + e.getMessage());
        }
    }

    public List<String> getFilePlaylist() {
        return filePlaylist;
    }

    public List<String> getPlayingPlaylist() {
        return playingPlaylist;
    }

    public int getCurrentTrackIndex() {
        return currentTrackIndex;
    }

    public void setCurrentTrackIndex(int index) {
        if (index >= 0 && index < playingPlaylist.size()) {
            currentTrackIndex = index;
        }
    }

    public void addToHistory(String track) {
        playingHistory.add(track);
    }

    public void saveCurrentTrackIndex() {
        savedCurrentTrackIndex = currentTrackIndex;
    }

    public void switchToSingleTrackPlaylist() {
        String currentTrack = getCurrentTrack();

        playingPlaylist.clear();
        playingPlaylist.add(currentTrack);

        currentTrackIndex = 0;
    }

    public void switchToSingleTrackPlaylist(int trackIndex) {
        if (trackIndex >= 0 && trackIndex < filePlaylist.size()) {
            String selectedTrack = filePlaylist.get(trackIndex);
            playingPlaylist.clear();
            playingPlaylist.add(selectedTrack);
            currentTrackIndex = 0;
        } else {
            System.err.println("Invalid track index for single-track mode: " + trackIndex);
        }
    }

    public void restorePlaylistOrder() {
        playingPlaylist.clear();
        playingPlaylist.addAll(filePlaylist);

        currentTrackIndex = 1;

        if (savedCurrentTrackIndex >= 0 && savedCurrentTrackIndex < filePlaylist.size()) {
            String currentTrackName = filePlaylist.get(savedCurrentTrackIndex);
            currentTrackIndex = getTrackIndexByName(currentTrackName);
        }
    }

    private int getTrackIndexByName(String trackName) {
        for (int i = 0; i < filePlaylist.size(); i++) {
            if (filePlaylist.get(i).equals(trackName)) {
                return i;
            }
        }
        return -1;
    }

    public void nextTrack() {
        if (playingPlaylist.size() > 1) {
            currentTrackIndex = (currentTrackIndex + 1) % playingPlaylist.size();
        }
    }

    public void previousTrack() {
        if (currentTrackIndex > 0) {
            currentTrackIndex--;
        } else {
            currentTrackIndex = playingPlaylist.size() - 1;
        }
    }

    public String getCurrentTrack() {
        return playingPlaylist.get(currentTrackIndex);
    }

    public void updateCurrentTrackIndexByName(String trackName) {
        int index = getTrackIndexByName(trackName);
        if (index != -1) {
            currentTrackIndex = index;
        } else {
            System.err.println("Track not found in the playlist: " + trackName);
        }
    }

    public void selectTrackAndShuffle(int trackIndex) {
        if (trackIndex >= 0 && trackIndex < filePlaylist.size()) {
            String selectedTrack = filePlaylist.get(trackIndex);

            List<String> shuffledList = new ArrayList<>(filePlaylist);
            shuffledList.remove(selectedTrack);
            shuffledList.add(0, selectedTrack);

            List<String> remainingTracks = shuffledList.subList(1, shuffledList.size());
            Collections.shuffle(remainingTracks);

            playingPlaylist.clear();
            playingPlaylist.add(selectedTrack);
            playingPlaylist.addAll(remainingTracks);

            currentTrackIndex = 0;
        }
    }

    public void reloadFilePlaylist() {
        loadFilePlaylist();
        playingPlaylist.clear();
        playingPlaylist.addAll(filePlaylist);
    }
}
