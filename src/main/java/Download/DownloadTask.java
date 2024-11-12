package Download;

import java.io.*;
//实现runnable接口
public class DownloadTask implements Runnable {
    private String sourceFile;//源文件路径
    private String destinationFile;//目标文件路径
    private volatile boolean isPaused = false;
    private volatile boolean isCancelled = false;//控制下载过程
    private ProgressCallback progressCallback;//下载进度
//构造函数
    public DownloadTask(String sourceFile, String destinationFile, ProgressCallback progressCallback) {
        this.sourceFile = sourceFile;
        this.destinationFile = destinationFile;
        this.progressCallback = progressCallback;
    }
//执行下载
    @Override
    public void run() {
        try {
            File source = new File(sourceFile);
            File destination = new File(destinationFile);
            long totalBytes = source.length();//源文件大小，计算下载进度用的
            long bytesRead = 0;
//从源文件读取数据并写入目标文件
            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(destination)) {
                byte[] buffer = new byte[1024];//每次读取1kb
                int bytes;
                //控制下载过程的具体实现
                while ((bytes = in.read(buffer)) != -1) {
                    if (isCancelled) {
                        break;
                    }
                    while (isPaused) {
                        Thread.sleep(100); // Simulate pause by sleeping the thread
                    }
                    out.write(buffer, 0, bytes);
                    bytesRead += bytes;
                    // Update progress through the callback
                    double progress = (double) bytesRead / totalBytes;
                    progressCallback.updateProgress(progress);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
//控制方法
    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public void cancel() {
        isCancelled = true;
    }
}
