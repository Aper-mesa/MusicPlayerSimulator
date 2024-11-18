package Download;

public interface ProgressCallback {
    void updateProgress(double progress); // 更新进度
    void onError(Exception e); // 处理错误
    void onCancelled(); // 通知任务已取消
    void onComplete(); // 通知任务已完成
}
