package Download;

import AudioPlayer.AudioPlayer;
import UI.App;
import javafx.application.Platform;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DownloadManager {
    private final List<String> playlist; // 播放列表 // Playlist
    private final List<DownloadTask> taskList = new ArrayList<>(); // 下载任务列表 // Download task list
    private final List<String> downloadedFiles = Collections.synchronizedList(new ArrayList<>());
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

        System.out.println("Current downloadedFiles: " + downloadedFiles);
        checkDownloadedFiles();  // 清理已删除文件的路径


        // 检查是否开始下载/禁止重复下载
        if (startedFiles.contains(playlist.get(index))) {
            System.out.println("This song is already downloading: " + playlist.get(index));
            App.updateWarning("This song is already downloading");
            return;
        }

        startedFiles.add(playlist.get(index));

        System.out.println("2:After checkDownloadedFiles(), downloadedFiles: " + downloadedFiles);
        if (downloadedFiles.contains(destinationPath)) {
                System.out.println("This song has already been downloaded: " + playlist.get(index));
                App.updateWarning("This song has already been downloaded");
                return;
        }

        // 创建任务对象
        DownloadTask task = new DownloadTask(sourcePath, destinationPath, null);

        // 创建回调并延迟绑定
        ProgressCallback callback = new ProgressCallback() {
            @Override
            public void updateProgress(double progress) {
                App.updateDownloadProgress(progress, taskList.indexOf(task));
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
                    App.removeDownloadTask(taskList.indexOf(task));
                    taskList.remove(task);
                    downloadedFiles.add(destinationPath); // 标记文件已下载
                    startedFiles.remove(playlist.get(index));
                });
            }
        };

        taskList.add(task);
        App.addDownloadRow(index, taskList.indexOf(task));

        // 设置回调
        task.setProgressCallback(callback);

        // 启动任务
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void checkDownloadedFiles() {
        System.out.println("2: Before cleaning, downloadedFiles: " + downloadedFiles);
        // 遍历下载目录中的所有文件，移除已删除的文件路径
        List<String> filesToRemove = new ArrayList<>();
        for (String filePath : downloadedFiles) {
            File file = new File(filePath);
            if (!file.exists()) {
                filesToRemove.add(filePath); // 如果文件不存在，添加到移除列表
                System.out.println("removelist"+filesToRemove);
            }
        }
        downloadedFiles.removeAll(filesToRemove); // 从下载记录中移除这些文件
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
