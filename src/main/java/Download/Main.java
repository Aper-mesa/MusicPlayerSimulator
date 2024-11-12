package Download;

public class Main {
    public static void main(String[] args) {
        DownloadManager downloadManager = new DownloadManager();

        // 模拟下载进度更新的回调
        ProgressCallback callback = progress -> {
            System.out.println("下载进度: " + (progress * 100) + "%");
        };

        // 启动下载任务
        String sourceFile = "path/to/source/music/song1.mp3";
        String destinationFile = "path/to/destination/music/song1.mp3";
        downloadManager.startDownload(sourceFile, destinationFile, callback);

        // 你可以在外部调用来控制暂停、恢复、取消等操作
        // downloadManager.pauseDownload(downloadTask);
        // downloadManager.resumeDownload(downloadTask);
        // downloadManager.cancelDownload(downloadTask);
    }
}
