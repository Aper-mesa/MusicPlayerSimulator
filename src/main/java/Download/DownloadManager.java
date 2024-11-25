package Download;

import AudioPlayer.AudioPlayer;
import UI.App;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager {
    private final List<String> playlist; // 播放列表
    private final List<DownloadTask> taskList = new ArrayList<>(); // 下载任务列表
    private final List<String> downloadedFiles = new ArrayList<>();//已完成下载的集合

    public DownloadManager() {
        AudioPlayer player = new AudioPlayer();
        playlist = player.getPlaylist(); // 从播放器获取播放列表
    }

    // 启动下载任务
    public void startDownload(int index) {
        String sourcePath = "./src/main/resources/songs/" + playlist.get(index);
        String userHome = System.getProperty("user.home");
        String destinationPath = userHome + "/Downloads/" + playlist.get(index);

        // 检查是否已经下载
        if (downloadedFiles.contains(destinationPath)) {
            System.out.println("此曲已下载: " + playlist.get(index));
            return;
        }

        // 创建任务对象
        // 先创建任务对象，但暂时不设置回调
        DownloadTask task = new DownloadTask(sourcePath, destinationPath, null);

        // 创建回调并延迟绑定
        ProgressCallback callback = new ProgressCallback() {
            @Override
            public void updateProgress(double progress) {
                App.updateDownloadProgress(progress, taskList.indexOf(task)); // 使用 task
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
                App.removeDownloadTask(taskList.indexOf(task)); // 从 UI 中移除
                taskList.remove(task); // 从任务列表中移除
                downloadedFiles.add(destinationPath); // 标记文件已下载
            }
        };

        taskList.add(task);

        // 设置回调
        task.setProgressCallback(callback);

        // 启动任务
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // 移除下载任务//这里还没用上
    public void removeTask(int index) {
        if (index < 0 || index >= taskList.size()) {
            System.err.println("Invalid index: " + index);
            return;
        }

        DownloadTask task = taskList.get(index);
        if (task != null) {
            task.cancel(); // 取消任务
            taskList.remove(index); // 从列表中移除任务
            App.removeDownloadTask(index); // 更新 UI
            System.out.println("Task at index " + index + " has been removed.");
        }
    }

    // 调整下载速度
    public void setDownloadSpeed(int index, long speed) {
        if (index < 0 || index >= taskList.size()) {
            System.err.println("Invalid index: " + index);
            return;
        }

        DownloadTask task = taskList.get(index);
        if (task != null) {
            task.setSpeed(speed); // 调整任务速度
            System.out.println("Speed for task " + index + " set to " + speed + " bytes/second.");
        }
    }

    // 暂停任务
    public void pauseTask(int index) {
        if (index < 0 || index >= taskList.size()) {
            System.err.println("Invalid index: " + index);
            return;
        }

        DownloadTask task = taskList.get(index);
        if (task != null) {
            task.pause();
            System.out.println("Task at index " + index + " has been paused.");
        }
    }

    // 恢复任务
    public void resumeTask(int index) {
        if (index < 0 || index >= taskList.size()) {
            System.err.println("Invalid index: " + index);
            return;
        }

        DownloadTask task = taskList.get(index);
        if (task != null) {
            task.resume();
            System.out.println("Task at index " + index + " has been resumed.");
        }
    }

    // 获取任务列表（仅用于调试或检查）
    public List<DownloadTask> getTaskList() {
        return taskList;
    }
}
