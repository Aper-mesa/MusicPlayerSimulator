package Download;
import java.util.concurrent.*;
//这是下载管理器，用来管理多个并发下载任务的
public class DownloadManager {
    //创建线程池，允许最多五个下载任务
    private ExecutorService executorService;

    public DownloadManager() {
        executorService = Executors.newFixedThreadPool(5); // 限制最多5个并发下载
    }
//启动新的下载任务的
    public void startDownload(String sourceFile, String destinationFile, ProgressCallback progressCallback) {
        DownloadTask downloadTask = new DownloadTask(sourceFile, destinationFile, progressCallback);//创造下载任务
        executorService.submit(downloadTask);//提交给线程池
    }

    public void pauseDownload(DownloadTask downloadTask) {
        downloadTask.pause();
    }

    public void resumeDownload(DownloadTask downloadTask) {
        downloadTask.resume();
    }

    public void cancelDownload(DownloadTask downloadTask) {
        downloadTask.cancel();
    }
//关闭线程池————提交任务完成后再关闭
    public void shutdown() {
        executorService.shutdown();
    }
}

