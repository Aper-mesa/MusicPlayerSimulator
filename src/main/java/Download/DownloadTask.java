package Download;

import java.io.*;

public class DownloadTask implements Runnable {
    private final String sourcePath; // 源文件路径
    private final String destinationPath; // 目标文件路径
    private volatile boolean isPaused = false; // 是否暂停
    private volatile boolean isCancelled = false; // 是否取消
    private ProgressCallback progressCallback; // 进度回调接口
    private final Object lock = new Object(); // 用于暂停和取消的锁
    private volatile long speed = 1024 * 50; // 下载速度（字节/秒），这里可以限制下载速度大小

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

        try {
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

            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(destination)) {
                byte[] buffer = new byte[1024];
                int bytes;

                while ((bytes = in.read(buffer)) != -1) {
                    synchronized (lock) {
                        while (isPaused) {
                            lock.wait();
                        }
                        if (isCancelled) {
                            if (destination.exists() && !destination.delete()) {
                                progressCallback.onError(new IOException("Failed to delete incomplete file"));
                            }
                            progressCallback.onCancelled();
                            return;
                        }
                    }

                    out.write(buffer, 0, bytes);
                    bytesRead += bytes;
                    bytesDownloadedThisSecond += bytes;

                    progressCallback.updateProgress((double) bytesRead / totalBytes);

                    // 控制下载速度
                    long currentTime = System.currentTimeMillis();
                    if (bytesDownloadedThisSecond >= speed) {
                        long timeElapsed = currentTime - lastTime;
                        if (timeElapsed < 1000) {
                            Thread.sleep(1000 - timeElapsed);
                        }
                        lastTime = System.currentTimeMillis();
                        bytesDownloadedThisSecond = 0;
                    }
                }

                progressCallback.updateProgress(1.0);
                progressCallback.onComplete();
            }
        } catch (IOException | InterruptedException e) {
            progressCallback.onError(e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
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
