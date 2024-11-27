package Download;

import AudioPlayer.AudioPlayer;
import UI.App;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

public class DownloadManager {
    private final List<String> playlist; // 播放列表 // Playlist
    private final List<DownloadTask> taskList = new ArrayList<>(); // 下载任务列表 // Download task list
    private final List<String> downloadedFiles = new ArrayList<>(); // 已完成下载的集合 // List of completed downloads
    private final List<String> startedFiles = new ArrayList<>(); // 已开始下载的集合 // List of started downloads

    public DownloadManager() {
        AudioPlayer player = new AudioPlayer();
        playlist = player.getPlaylist(); // 从播放器获取播放列表 // Get the playlist from the audio player
    }

    // 启动下载任务 // Start the download task
    public void startDownload(int index) {
        String sourcePath = "./src/main/resources/songs/" + playlist.get(index);
        String userHome = System.getProperty("user.home");
        String destinationPath = userHome + "/Downloads/" + playlist.get(index);

        // 检查是否开始下载/禁止重复下载 // Check if the download has started to prevent duplicate downloads
        if (startedFiles.contains(playlist.get(index))) {
            System.out.println("This song is already downloading: " + playlist.get(index));
            App.updateWarning("This song is already downloading");
            return;
        }

        startedFiles.add(playlist.get(index));

        // 检查是否已经下载 // Check if the file has already been downloaded
        if (downloadedFiles.contains(destinationPath)) {
            System.out.println("This song has already been downloaded: " + playlist.get(index));
            App.updateWarning("This song has already been downloaded");
            return;
        }

        // 创建任务对象 // Create the task object
        // 先创建任务对象，但暂时不设置回调 // Create the task object without setting a callback yet
        DownloadTask task = new DownloadTask(sourcePath, destinationPath, null);

        // 创建回调并延迟绑定 // Create the callback and bind it later
        ProgressCallback callback = new ProgressCallback() {
            @Override
            public void updateProgress(double progress) {
                App.updateDownloadProgress(progress, taskList.indexOf(task)); // 使用 task // Use the task
            }

            @Override
            public void onError(Exception e) {
                System.err.println("Error downloading " + playlist.get(index) + ": " + e.getMessage());
            }

            @Override
            public void onCancelled() {
                System.out.println("Download cancelled: " + playlist.get(index));
            }

            @Override
            public void onComplete() {
                Platform.runLater(() -> {
                    App.removeDownloadTask(taskList.indexOf(task)); // 从 UI 中移除 // Remove from the UI
                    taskList.remove(task); // 从任务列表中移除 // Remove from the task list
                    downloadedFiles.add(destinationPath); // 标记文件已下载 // Mark the file as downloaded
                    startedFiles.remove(playlist.get(index));
                });
            }
        };

        taskList.add(task);

        App.addDownloadRow(index, taskList.indexOf(task));

        // 设置回调 // Set the callback
        task.setProgressCallback(callback);

        // 启动任务 // Start the task
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // 移除下载任务 // Remove the download task (not yet used)
    public void removeTask(int index) {
        if (index < 0 || index >= taskList.size()) {
            System.err.println("Invalid index: " + index);
            App.updateWarning("Invalid index");
            return;
        }

        DownloadTask task = taskList.get(index);
        if (task != null) {
            task.cancel(); // 取消任务 // Cancel the task
            taskList.remove(index); // 从列表中移除任务 // Remove the task from the list
            System.out.println("Task at index " + index + " has been removed.");
        }
    }

    // 调整下载速度 // Adjust the download speed
    public void setDownloadSpeed(int index, long speed) {
        if (index < 0 || index >= taskList.size()) {
            System.err.println("Invalid index: " + index);
            return;
        }

        DownloadTask task = taskList.get(index);
        if (task != null) {
            task.setSpeed(speed); // 调整任务速度 // Adjust the task speed
            System.out.println("Speed for task " + index + " set to " + speed + " bytes/second.");
        }
    }

    // 暂停任务 // Pause the task
    public void pauseTask(int index) {
        if (index < 0 || index >= taskList.size()) {
            System.err.println("Invalid index: " + index);
            App.updateWarning("Invalid index");
            return;
        }

        DownloadTask task = taskList.get(index);
        if (task != null) {
            task.pause();
            System.out.println("Task at index " + index + " has been paused.");
        }
    }

    // 恢复任务 // Resume the task
    public void resumeTask(int index) {
        if (index < 0 || index >= taskList.size()) {
            System.err.println("Invalid index: " + index);
            App.updateWarning("Invalid index");
            return;
        }

        DownloadTask task = taskList.get(index);
        if (task != null) {
            task.resume();
            System.out.println("Task at index " + index + " has been resumed.");
        }
    }

    // 获取任务列表（仅用于调试或检查） // Get the task list (for debugging or inspection)
    public List<DownloadTask> getTaskList() {
        return taskList;
    }
}
