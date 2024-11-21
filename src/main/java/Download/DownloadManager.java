package Download;

import AudioPlayer.AudioPlayer;
import UI.App;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager {
    private final List<String> downloadList = new ArrayList<>(); // 下载列表
    private final List<String> playlist;

    public DownloadManager() {
        AudioPlayer player = new AudioPlayer();
        playlist = player.getPlaylist();
    }

    public void startDownload(int index) {
        downloadList.add(playlist.get(index));

        if (index >= downloadList.size()) {
            System.err.println("Invalid index: " + index);
            return;
        }

        // 获取源文件路径
        String sourcePath = downloadList.get(index);

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
                // 输出当前进度
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
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public List<String> getDownloadList() {
        return downloadList;
    }
}
