package Download;

import java.io.*;

public class DownloadTask implements Runnable {
    private String sourceFile; // 源文件路径
    private String destinationFile; // 目标文件路径
    private volatile boolean isPaused = false; // 标记任务是否暂停
    private volatile boolean isCancelled = false; // 标记任务是否取消
    private ProgressCallback progressCallback; // 回调用于更新任务状态
    private final Object lock = new Object(); // 锁对象，用于线程间同步
    private volatile String status = "Waiting"; // 默认状态

    // 构造函数
    public DownloadTask(String sourceFile, String destinationFile, ProgressCallback progressCallback) {
        this.sourceFile = sourceFile;
        this.destinationFile = destinationFile;
        this.progressCallback = progressCallback;
    }

    @Override
    public void run() {
        File source = new File(sourceFile);
        File destination = new File(destinationFile);

        try {
            // 检查源文件是否存在
            if (!source.exists() || !source.isFile()) {
                throw new FileNotFoundException("Source file does not exist: " + sourceFile);
            }

            // 检查目标目录是否存在，不存在则创建
            if (!destination.getParentFile().exists() && !destination.getParentFile().mkdirs()) {
                throw new IOException("Failed to create destination directory: " + destination.getParentFile().getAbsolutePath());
            }

            long totalBytes = source.length(); // 源文件大小
            long bytesRead = 0; // 已复制字节数
            long lastCallbackBytes = 0;

            // 使用文件流进行文件复制
            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(destination)) {
                byte[] buffer = new byte[1024]; // 缓冲区
                int bytes;
                while ((bytes = in.read(buffer)) != -1) {
                    synchronized (lock) {
                        while (isPaused) {
                            lock.wait(); // 如果暂停，等待被唤醒
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

                    // 更新进度（每 100KB 更新一次）
                    if (bytesRead - lastCallbackBytes >= 1024 * 100) {
                        progressCallback.updateProgress((double) bytesRead / totalBytes);
                        lastCallbackBytes = bytesRead;
                    }
                }
                progressCallback.updateProgress(1.0); // 确保完成时进度是 100%
                progressCallback.onComplete(); // 通知任务完成
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
            progressCallback.onError(e); // 通知任务出错
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
            lock.notifyAll(); // 唤醒暂停的线程
        }
    }

    // 取消任务
    public void cancel() {
        synchronized (lock) {
            isCancelled = true;
        }
    }
}
