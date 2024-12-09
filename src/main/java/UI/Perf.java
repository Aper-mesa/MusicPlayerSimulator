package UI;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class Perf {
    private final VBox root = new VBox(10);

    // 静态变量，用于跟踪窗口实例
    private static Stage perfStage = null;

    public void start(Stage ownerStage) {
        // 如果窗口已经创建过，就不再创建新的窗口
        if (perfStage != null) {
            perfStage.toFront();  // 如果窗口已经存在，显示在前面
            return;  // 直接返回，不再创建新的窗口
        }

        // 创建新窗口
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/perf.png")));
        perfStage = new Stage();
        Scene scene = new Scene(root, 700, 600);
        perfStage.getIcons().add(icon);

        perfStage.setTitle("Performance");
        perfStage.setScene(scene);
        perfStage.setResizable(false);

        perfStage.initOwner(ownerStage);  // 绑定到主窗口

        // 设置关闭事件监听器
        perfStage.setOnCloseRequest(_ -> closeWindow()); // 窗口关闭时调用

        memory();

        perfStage.show();
    }

    private static final Label memoryUsage = new Label("-");

    private void memory() {
        Label memoryLabel = new Label("Music player memory usage: ");
        memoryLabel.setStyle("-fx-font-size: 16px;");
        memoryUsage.setStyle("-fx-font-size: 16px;");
        HBox memoryBox = new HBox(10);
        memoryBox.getChildren().addAll(memoryLabel, memoryUsage);
        root.getChildren().add(memoryBox);
    }

    public static void updateMemoryUsage(String value) {
        memoryUsage.setText(value);
    }

    // 关闭窗口时清除静态变量，允许重新打开
    public static void closeWindow() {
        if (perfStage != null) {
            perfStage.close();
            perfStage = null;
        }
    }
}
