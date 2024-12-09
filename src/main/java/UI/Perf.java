package UI;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Perf {
    private final VBox root = new VBox(10);

    // 静态变量，用于跟踪窗口实例
    private static Stage perfStage = null;

    private final XYChart.Series<Number, Number> memorySeries = new XYChart.Series<>();
    private int time = 0;
    private static final int MAX_DATA_POINTS = 50;
    private static final int RIGHT_MARGIN = 10;


    private NumberAxis xAxis;
    private NumberAxis yAxis;

    public void start(Stage ownerStage) {
        if (perfStage != null) {
            perfStage.toFront();
            return;
        }

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/perf.png")));
        perfStage = new Stage();
        Scene scene = new Scene(root, 700, 900);
        perfStage.getIcons().add(icon);

        perfStage.setTitle("Performance");
        perfStage.setScene(scene);
        perfStage.setResizable(false);

        perfStage.initOwner(ownerStage); // 绑定到主窗口

        // 设置关闭事件监听器
        perfStage.setOnCloseRequest(_ -> closeWindow()); // 窗口关闭时调用

        memory();
        setupMemoryChart();

        perfStage.show();


        startPerformanceUpdater();
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

    private void setupMemoryChart() {
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        yAxis.setLabel("Memory Usage (MB)");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Real-Time Memory Usage");
        lineChart.setAnimated(false);

        memorySeries.setName("Memory Usage");
        lineChart.getData().add(memorySeries);

        root.getChildren().add(lineChart);

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(MAX_DATA_POINTS + RIGHT_MARGIN);
    }

    public static void updateMemoryUsage(String value) {
        memoryUsage.setText(value);
    }

    private void startPerformanceUpdater() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateMemoryChart();
            }
        }, 0, 1000);
    }

    private void updateMemoryChart() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024); // MB

        Platform.runLater(() -> {

            memorySeries.getData().add(new XYChart.Data<>(time, usedMemory));

            if (time > MAX_DATA_POINTS) {
                xAxis.setLowerBound(time - MAX_DATA_POINTS);
                xAxis.setUpperBound(time + RIGHT_MARGIN); // 保持右边留白
            }

            if (memorySeries.getData().size() > MAX_DATA_POINTS) {
                memorySeries.getData().removeFirst();
            }
            
            time++;
        });
    }


    // 关闭窗口时清除静态变量，允许重新打开
    public static void closeWindow() {
        if (perfStage != null) {
            perfStage.close();
            perfStage = null;
        }
    }
}
