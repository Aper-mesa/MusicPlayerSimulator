package Download;
//用来管理多个下载任务
import java.util.concurrent.*;
import java.util.Map;

public class DownloadManager {
    private ExecutorService executorService;

    private Map<String, DownloadTask> taskMap = new ConcurrentHashMap<>();

    public DownloadManager() {
        executorService = Executors.newFixedThreadPool(5); // 限制最多5个并发下载
    }

    public void startDownload(String taskId, String sourceFile, String destinationFile, ProgressCallback progressCallback) {
        ProgressCallback wrappedCallback = new ProgressCallback() {
            @Override
            public void updateProgress(double progress) {
                progressCallback.updateProgress(progress);
            }

            @Override
            public void onError(Exception e) {
                progressCallback.onError(e);
                taskMap.remove(taskId);
            }

            @Override
            public void onCancelled() {
                progressCallback.onCancelled();
                taskMap.remove(taskId);
            }

            @Override
            public void onComplete() {
                progressCallback.onComplete();
                taskMap.remove(taskId);
            }
        };

        DownloadTask task = new DownloadTask(sourceFile, destinationFile, wrappedCallback);
        taskMap.put(taskId, task);
        executorService.submit(task);
    }

    public void pauseDownload(String taskId) {
        synchronized (taskMap) {
            DownloadTask task = taskMap.get(taskId);
            if (task != null) task.pause();
        }
    }

    public void resumeDownload(String taskId) {
        synchronized (taskMap) {
            DownloadTask task = taskMap.get(taskId);
            if (task != null) task.resume();
        }
    }

    public void cancelDownload(String taskId) {
        synchronized (taskMap) {
            DownloadTask task = taskMap.get(taskId);
            if (task != null) {
                task.cancel();
                taskMap.remove(taskId);
            }
        }
    }

    public void restartDownload(String taskId, String sourceFile, String destinationFile, ProgressCallback progressCallback) {
        cancelDownload(taskId);
        startDownload(taskId, sourceFile, destinationFile, progressCallback);
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
