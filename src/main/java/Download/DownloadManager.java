package Download;

import AudioPlayer.AudioPlayer;
import UI.App;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager {
    private final List<String> playlist; // 播放列表
    private final List<DownloadTask> taskList = new ArrayList<>(); // 下载任务列表

    public DownloadManager() {
        AudioPlayer player = new AudioPlayer();
        playlist = player.getPlaylist();
        // 初始化任务列表，确保索引与 UI 对应
        for (int i = 0; i < playlist.size(); i++) {
            taskList.add(null); // 初始为空
        }
    }

    public void startDownload(int index) {
        if (index < 0 || index >= playlist.size()) {
            System.err.println("Invalid index: " + index);
            return;
        }

        // 检查是否已存在任务
        if (taskList.get(index) != null) {
            System.err.println("Task already exists for index: " + index);
            return;
        }

        // 获取源文件路径
        String sourcePath = playlist.get(index);

        // 获取目标文件夹路径
        String userHome = System.getProperty("user.home");
        String destinationFolder = userHome + "/Downloads/MusicPlayerDownloads";

        // 获取文件名并生成目标路径
        String fileName = new File(sourcePath).getName();
        String destinationPath = new File(destinationFolder, fileName).getAbsolutePath();

        // 创建目标文件夹（如果不存在）
        File destinationDir = new File(destinationFolder);
        if (!destinationDir.exists() && !destinationDir.mkdirs()) {
            System.err.println("Failed to create directory: " + destinationFolder);
            return;
        }

        // 创建 ProgressCallback
        ProgressCallback callback = new ProgressCallback() {
            @Override
            public void updateProgress(double progress) {
                // 通知 UI 更新进度条
                App.updateDownloadProgress(progress, index);
            }

            @Override
            public void onError(Exception e) {
                System.err.println("Error downloading " + fileName + ": " + e.getMessage());
            }

            @Override
            public void onCancelled() {
                System.out.println("Download cancelled: " + fileName);
            }

            @Override
            public void onComplete() {
                System.out.println("Download completed: " + destinationPath);
            }
        };

        // 创建并启动 DownloadTask
        DownloadTask task = new DownloadTask(sourcePath, destinationPath, callback);
        taskList.set(index, task); // 将任务存储到任务列表
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public List<DownloadTask> getTaskList() {
        return taskList;
    }
    public List<String> getDownloadList() {
        List<String> downloadList = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i) != null) { // 如果任务存在
                downloadList.add(playlist.get(i)); // 从 playlist 获取文件路径
            }
        }
        return downloadList;
    }

}
