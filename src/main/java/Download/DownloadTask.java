package Download;

import java.io.*;

public class DownloadTask implements Runnable {
    private String sourceFile;
    private String destinationFile;
    private volatile boolean isPaused = false;
    private volatile boolean isCancelled = false;
    private ProgressCallback progressCallback;
    private final Object lock = new Object();
    private volatile String status = "Waiting";

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
            setStatus("Running");

            if (!source.exists() || !source.isFile()) {
                throw new FileNotFoundException("Source file does not exist: " + sourceFile);
            }

            if (!destination.getParentFile().exists() && !destination.getParentFile().mkdirs()) {
                throw new IOException("Failed to create destination directory: " + destination.getParentFile().getAbsolutePath());
            }

            long totalBytes = source.length();
            long bytesRead = 0;
            long lastCallbackBytes = 0;

            try (InputStream in = new FileInputStream(source);
                 OutputStream out = new FileOutputStream(destination)) {
                byte[] buffer = new byte[1024];
                int bytes;
                while ((bytes = in.read(buffer)) != -1) {
                    synchronized (lock) {
                        while (isPaused) {
                            setStatus("Paused");
                            lock.wait();
                        }
                        if (isCancelled) {
                            setStatus("Cancelled");
                            if (destination.exists() && !destination.delete()) {
                                progressCallback.onError(new IOException("Failed to delete incomplete file"));
                            }
                            progressCallback.onCancelled();
                            return;
                        }
                    }
                    out.write(buffer, 0, bytes);
                    bytesRead += bytes;

                    if (bytesRead - lastCallbackBytes >= 1024 * 100) {
                        progressCallback.updateProgress((double) bytesRead / totalBytes);
                        lastCallbackBytes = bytesRead;
                    }
                }
                progressCallback.updateProgress(1.0);
                setStatus("Completed");
                progressCallback.onComplete();
            }
        } catch (IOException | InterruptedException e) {
            setStatus("Error");
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            progressCallback.onError(e);
        }
    }

    public void pause() {
        synchronized (lock) {
            isPaused = true;
        }
    }

    public void resume() {
        synchronized (lock) {
            isPaused = false;
            lock.notifyAll();
            setStatus("Running");
        }
    }

    public void cancel() {
        synchronized (lock) {
            isCancelled = true;
            setStatus("Cancelled");
        }
    }

    public String getStatus() {
        return status;
    }

    private void setStatus(String newStatus) {
        synchronized (lock) {
            status = newStatus;
        }
    }
}
