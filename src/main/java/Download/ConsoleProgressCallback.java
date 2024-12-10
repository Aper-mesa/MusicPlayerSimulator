package Download;

//接口方法的具体实现
public class ConsoleProgressCallback implements ProgressCallback {
    @Override
    public void updateProgress(double progress) {
        System.out.printf("Progress: %.2f%%%n", progress * 100);
    }

    @Override
    public void onError(Exception e) {
        System.err.println("Status: Error occurred.");
        System.err.println("Error details: " + e.getMessage());
    }

    @Override
    public void onCancelled() {
        System.out.println("Status: Download was cancelled.");
    }

    @Override
    public void onComplete() {
        System.out.println("Status: Download completed successfully.");
    }
}
