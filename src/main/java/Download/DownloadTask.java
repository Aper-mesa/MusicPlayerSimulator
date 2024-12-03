package Download;

import UI.App;

import java.io.*;

public class DownloadTask implements Runnable {
    private String taskId;  // 唯一标识任务
    private final String sourcePath; // 源文件路径 // Source file path
    private final String destinationPath; // 目标文件路径 // Destination file path
    private volatile boolean isPaused = false; // 是否暂停 // Whether the task is paused
    private volatile boolean isCancelled = false; // 是否取消 // Whether the task is cancelled
    private ProgressCallback progressCallback; // 进度回调接口 // Progress callback interface
    private final Object lock = new Object(); // 用于暂停和取消的锁 // Lock for pause and cancel synchronization
    private volatile long speed = 1024 * 200; // 下载速度（字节/秒），这里可以限制下载速度大小 // Download speed (bytes/second), can limit download speed

    public DownloadTask(String sourcePath, String destinationPath, ProgressCallback progressCallback,String taskId) {
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
        this.progressCallback = progressCallback;
        this.taskId = taskId;
    }
    public String getTaskId() {
        return taskId;
    }
    //用于设置动态回调 // For setting a dynamic callback
    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    @Override
    public void run() {
        File source = new File(sourcePath);
        File destination = new File(destinationPath);

        // 确保流可以在任意情况下正确关闭 // Ensure streams are closed properly in any situation
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(destination)) {

            if (!source.exists() || !source.isFile()) {
                throw new FileNotFoundException("Source file does not exist: " + sourcePath);
            }

            if (!destination.getParentFile().exists() && !destination.getParentFile().mkdirs()) {
                throw new IOException("Failed to create destination directory: " + destination.getParentFile().getAbsolutePath());
            }

            long totalBytes = source.length();
            long bytesRead = 0;
            long lastTime = System.currentTimeMillis();
            long bytesDownloadedThisSecond = 0;

            byte[] buffer = new byte[1024];
            int bytes;

            while ((bytes = in.read(buffer)) != -1) {
                // 1. 同步检查暂停和取消状态 // 1. Synchronize to check pause and cancel status
                synchronized (lock) {
                    // 如果暂停，等待恢复 // If paused, wait for resume
                    while (isPaused) {
                        lock.wait();
                    }

                    // 检查是否取消 // Check if cancelled
                    if (isCancelled) {
                        // 2. 确保流被关闭后尝试删除文件 // 2. Ensure streams are closed, then delete file
                        closeStreams(in, out);
                        if (destination.exists() && !destination.delete()) {
                            progressCallback.onError(new IOException("Failed to delete incomplete file"));
                        } else {
                            System.out.println("Incomplete file deleted successfully.");
                        }
                        progressCallback.onCancelled();
                        return; // 立即退出 // Exit immediately
                    }
                }

                // 写入数据并更新进度 // Write data and update progress
                out.write(buffer, 0, bytes);
                bytesRead += bytes;
                bytesDownloadedThisSecond += bytes;

                // 更新进度条 // Update progress bar
                progressCallback.updateProgress((double) bytesRead / totalBytes);

                // 3. 控制下载速度 // 3. Control download speed
                synchronized (lock) {
                    if (bytesDownloadedThisSecond >= speed) {
                        long currentTime = System.currentTimeMillis();
                        long timeElapsed = currentTime - lastTime;
                        if (timeElapsed < 1000) {
                            lock.wait(1000 - timeElapsed); // 进入等待，减少资源消耗
                        }
                        lastTime = System.currentTimeMillis();
                        bytesDownloadedThisSecond = 0;
                    }
                }
            }

            // 下载完成，更新进度并回调 // Download complete, update progress and callback
            progressCallback.updateProgress(1.0);
            progressCallback.onComplete();

        } catch (IOException | InterruptedException e) {
            // 捕获异常，通知回调 // Catch exceptions and notify callback
            progressCallback.onError(e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt(); // 恢复中断状态 // Restore interrupt status
            }
        }
    }

    // 辅助方法：确保流安全关闭 // Helper method: ensure streams are safely closed
    private void closeStreams(InputStream in, OutputStream out) {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            System.err.println("Error closing streams: " + e.getMessage());
            App.updateWarning("Error closing streams");
        }
    }

    // 暂停任务 // Pause the task
    public void pause() {
        synchronized (lock) {
            isPaused = true;
        }
    }

    // 恢复任务 // Resume the task
    public void resume() {
        synchronized (lock) {
            isPaused = false;
            lock.notifyAll();
        }
    }

    // 取消任务 // Cancel the task
    public void cancel() {
        synchronized (lock) {
            isCancelled = true;
            lock.notifyAll();
        }
    }

    // 设置下载速度 // Set download speed
    public void setSpeed(long speed) {
        this.speed = speed;
    }

    // 获取当前速度 // Get current speed
    public long getSpeed() {
        return speed;
    }
}
