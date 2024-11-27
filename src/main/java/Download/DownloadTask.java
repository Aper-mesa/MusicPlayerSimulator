package Download;

import java.io.*;

public class DownloadTask implements Runnable {
    private final String sourcePath; // 源文件路径
    private final String destinationPath; // 目标文件路径
    private volatile boolean isPaused = false; // 是否暂停
    private volatile boolean isCancelled = false; // 是否取消
    private ProgressCallback progressCallback; // 进度回调接口
    private final Object lock = new Object(); // 用于暂停和取消的锁
    private volatile long speed = 1024 * 800; // 下载速度（字节/秒），这里可以限制下载速度大小

    public DownloadTask(String sourcePath, String destinationPath, ProgressCallback progressCallback) {
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
        this.progressCallback = progressCallback;
    }
    //用于设置动态回调
    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }
    @Override
    public void run() {
        File source = new File(sourcePath);
        File destination = new File(destinationPath);

        // 确保流可以在任意情况下正确关闭
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
                // 1. 同步检查暂停和取消状态
                synchronized (lock) {
                    // 如果暂停，等待恢复
                    while (isPaused) {
                        lock.wait();
                    }

                    // 检查是否取消
                    if (isCancelled) {
                        // 2. 确保流被关闭后尝试删除文件
                        closeStreams(in, out);
                        if (destination.exists() && !destination.delete()) {
                            progressCallback.onError(new IOException("Failed to delete incomplete file"));
                        } else {
                            System.out.println("Incomplete file deleted successfully.");
                        }
                        progressCallback.onCancelled();
                        return; // 立即退出
                    }
                }

                // 写入数据并更新进度
                out.write(buffer, 0, bytes);
                bytesRead += bytes;
                bytesDownloadedThisSecond += bytes;

                // 更新进度条
                progressCallback.updateProgress((double) bytesRead / totalBytes);

                // 3. 控制下载速度
                long currentTime = System.currentTimeMillis();
                if (bytesDownloadedThisSecond >= speed) {
                    long timeElapsed = currentTime - lastTime;
                    if (timeElapsed < 1000) {
                        Thread.sleep(1000 - timeElapsed); // 精确控制下载速度
                    }
                    lastTime = System.currentTimeMillis();
                    bytesDownloadedThisSecond = 0;
                }
            }

            // 下载完成，更新进度并回调
            progressCallback.updateProgress(1.0);
            progressCallback.onComplete();

        } catch (IOException | InterruptedException e) {
            // 捕获异常，通知回调
            progressCallback.onError(e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
        }
    }

    // 辅助方法：确保流安全关闭
    private void closeStreams(InputStream in, OutputStream out) {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            System.err.println("Error closing streams: " + e.getMessage());
        }
    }


    // 暂停任务
    public void pause() {
        synchronized (lock) {
            isPaused = true;
        }
    }

    // 恢复任务
    public void resume() {
        synchronized (lock) {
            isPaused = false;
            lock.notifyAll();
        }
    }

    // 取消任务
    public void cancel() {
        synchronized (lock) {
            isCancelled = true;
            lock.notifyAll();
        }
    }

    // 设置下载速度
    public void setSpeed(long speed) {
        this.speed = speed;
    }

    // 获取当前速度
    public long getSpeed() {
        return speed;
    }
}
