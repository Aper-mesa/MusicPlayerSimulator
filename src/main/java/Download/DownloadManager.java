package Download;

import AudioPlayer.AudioPlayer;
import UI.App;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager {
    private final List<String> playlist; // 播放列表
    private final List<String> taskList = new ArrayList<>(); // 下载任务列表

    public DownloadManager() {
        AudioPlayer player = new AudioPlayer();
        playlist = player.getPlaylist();
    }

    public void startDownload(int index) {
        taskList.add(playlist.get(index));
        App.addDownloadRow(index, taskList.indexOf(playlist.get(index)));
        if (index >= playlist.size()) {
            System.err.println("Invalid index: " + index);
            return;
        }

        // TODO wrong way to check existing task. but this check is necessary

        /*// 检查是否已存在任务
        if (taskList.get(index) != null) {
            System.err.println("Task already exists for index: " + index);
            return;
        }*/

        // 获取源文件路径
        String sourcePath = "./src/main/resources/songs/" + playlist.get(index);

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
                System.out.println(progress);
                // 通知 UI 更新进度条
                App.updateDownloadProgress(progress, taskList.indexOf(playlist.get(index)));
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
                // TODO this line will cause error, fix it!
                //App.removeDownloadTask(taskList.indexOf(playlist.get(index)));
                taskList.remove(playlist.get(index));
            }
        };

        // 创建并启动 DownloadTask
        DownloadTask task = new DownloadTask(sourcePath, destinationPath, callback);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
