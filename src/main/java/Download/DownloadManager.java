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
        String taskId = playlist.get(index);
        String sourcePath = "./src/main/resources/songs/" + taskId;
        String userHome = System.getProperty("user.home");
        String destinationPath = userHome + "/Downloads/" + taskId;

        System.out.println("Current downloadedFiles: " + downloadedFiles);
        checkDownloadedFiles();  // 清理已删除文件的路径


        // 检查是否开始下载/禁止重复下载//check if start download/avoid repeat download operation
        if (startedFiles.contains(taskId)) {
            System.out.println("This song is already downloading: " + taskId);
            App.updateWarning("This song is already downloading");
            return;
        }

        startedFiles.add(playlist.get(index));

        System.out.println("2:After checkDownloadedFiles(), downloadedFiles: " + downloadedFiles);
        if (downloadedFiles.contains(destinationPath)) {
            System.out.println("This song has already been downloaded: " + taskId);
            App.updateWarning("This song has already been downloaded");
            return;
        }

        // 创建任务对象//creat task
        DownloadTask task = new DownloadTask(sourcePath, destinationPath, null, taskId);

        // 创建回调并延迟绑定//callback
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
                    App.removeDownloadTask(task.getTaskId());
                    taskList.remove(task);
                    downloadedFiles.add(destinationPath); // 标记文件已下载
                    startedFiles.remove(playlist.get(index));
                    if (taskList.isEmpty()) App.showNoDownloadMessage();
                });
            }
        };

        taskList.add(task);
        App.addDownloadRow(index, taskList.indexOf(task));

        // 设置回调//set callback
        task.setProgressCallback(callback);

        // 启动任务//start
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        App.updateWarning("Start download");
    }

    public void checkDownloadedFiles() {
        System.out.println("2: Before cleaning, downloadedFiles: " + downloadedFiles);
        // 遍历下载目录中的所有文件，移除已删除的文件路径 //travel all file in dir, remove the file in "downloadedFiles" which already deleted in dir
        List<String> filesToRemove = new ArrayList<>();
        for (String filePath : downloadedFiles) {
            File file = new File(filePath);
            if (!file.exists()) {
                filesToRemove.add(filePath); // 如果文件不存在，添加到移除列表//if file not exist, add to arraylist "filesToRemove"
                System.out.println("remove list" + filesToRemove);
            }
        }
        downloadedFiles.removeAll(filesToRemove); // 从下载记录中移除这些文件//remove file from downloadFiles
    }

    // 移除下载任务 // Remove the download task (not yet used)
    public void removeTask(String taskId) {
        for (DownloadTask task : taskList) {
            if (task.getTaskId().equals(taskId)) {
                task.cancel();
                taskList.remove(task);
                System.out.println("Task with ID " + taskId + " has been removed.");
                return;
            }
        }
        System.err.println("Task with ID " + taskId + " not found.");
    }

    // 暂停任务 // Pause the task
    public void pauseTask(String taskId) {
        for (DownloadTask task : taskList) {
            if (task.getTaskId().equals(taskId)) {
                task.pause();
                System.out.println("Task with ID " + taskId + " has been paused.");
                return;
            }
        }
        System.err.println("Task with ID " + taskId + " not found.");
    }


    // 恢复任务 // Resume the task
    public void resumeTask(String taskId) {
        for (DownloadTask task : taskList) {
            if (task.getTaskId().equals(taskId)) {
                task.resume();
                System.out.println("Task with ID " + taskId + " has been resumed.");
                return;
            }
        }
        System.err.println("Task with ID " + taskId + " not found.");
    }
}
