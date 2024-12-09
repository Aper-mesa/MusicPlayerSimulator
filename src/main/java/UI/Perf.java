package UI;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Perf {
    private final VBox root = new VBox(10);

    // Static variable to track the window instance
    private static Stage perfStage = null;

    // Store memory usage data
    private final XYChart.Series<Number, Number> memorySeries = new XYChart.Series<>();
    private int time = 0;
    private static final int MAX_DATA_POINTS = 50;
    private static final int RIGHT_MARGIN = 10;

    // Store song open time data by index (1-based index)
    private static Map<Integer, Double> songTimeData = new HashMap<>(); // Store index and open time
    private static XYChart.Series<String, Number> songTimeSeries = new XYChart.Series<>(); // For displaying data on the chart

    // Define chart axes
    private NumberAxis xAxis;
    private NumberAxis yAxis;


    public void start(Stage ownerStage) {
        if (perfStage != null) {
            perfStage.toFront();
            return;
        }

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/perf.png")));
        perfStage = new Stage();
        Scene scene = new Scene(root, 700, 850);
        perfStage.getIcons().add(icon);

        perfStage.setTitle("Performance");
        perfStage.setScene(scene);
        perfStage.setResizable(false);

        perfStage.initOwner(ownerStage);

        // Set close event listener
        perfStage.setOnCloseRequest(_ -> closeWindow());

        memory();
        setupMemoryChart();
        setupSongTimeChart();

        perfStage.show();

        initializeSongData();

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

    // Set up memory usage line chart
    private void setupMemoryChart() {
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");
        yAxis.setLabel("Memory Usage (MB)");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Real-Time Memory Usage");
        lineChart.setAnimated(false);
        lineChart.getData().add(memorySeries);
        lineChart.setLegendVisible(false);

        root.getChildren().add(lineChart);

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(MAX_DATA_POINTS + RIGHT_MARGIN);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100); // Initialize to 0-100, will adjust dynamically later
    }

    public void initializeSongData(int playListSize) {
        songTimeData.clear();
        for (int i = 1; i <= playListSize; i++) {
            songTimeData.put(i, 0.0);
        }
        updateSongTimeChart();
    }


    private void setupSongTimeChart() {
        CategoryAxis xSongAxis = new CategoryAxis();
        NumberAxis ySongAxis = new NumberAxis();
        xSongAxis.setLabel("Songs");
        ySongAxis.setLabel("Open Time (s)");

        BarChart<String, Number> songTimeChart = new BarChart<>(xSongAxis, ySongAxis);
        songTimeChart.setTitle("Song Open Times");
        songTimeChart.setAnimated(false);
        songTimeChart.getData().add(songTimeSeries);
        songTimeChart.setLegendVisible(false);

        root.getChildren().add(songTimeChart);
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
        }, 0, 1000); // Update every second
    }

    private void updateMemoryChart() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024); // MB

        Platform.runLater(() -> {
            memorySeries.getData().add(new XYChart.Data<>(time, usedMemory));
            
            if (memorySeries.getData().size() > MAX_DATA_POINTS) {
                memorySeries.getData().remove(0);
            }


            adjustYAxis();

            if (time > MAX_DATA_POINTS) {
                xAxis.setLowerBound(time - MAX_DATA_POINTS);
                xAxis.setUpperBound(time + RIGHT_MARGIN);
            }

            time++;
        });
    }


    private void adjustYAxis() {
        double maxMemory = memorySeries.getData().stream()
                .mapToDouble(data -> data.getYValue().doubleValue())
                .max()
                .orElse(0);

        double minMemory = memorySeries.getData().stream()
                .mapToDouble(data -> data.getYValue().doubleValue())
                .min()
                .orElse(0);

        double lowerBound = Math.max(0, minMemory - 10);
        double upperBound = maxMemory + 10;

        yAxis.setLowerBound(lowerBound);
        yAxis.setUpperBound(upperBound);
    }


    private void initializeSongData() {
        for (int i = 1; i <= 8; i++) {
            songTimeData.put(i, 0.0);
        }
        updateSongTimeChart();
    }


    public static void updateSongOpenTime(int index, double elapsedTimeInSeconds) {
        songTimeData.put(index, elapsedTimeInSeconds);

        updateSongTimeChart();
    }

    private static void updateSongTimeChart() {
        songTimeSeries.getData().clear();
        songTimeData.forEach((index, openTime) -> {
            songTimeSeries.getData().add(new XYChart.Data<>(String.valueOf(index), openTime)); // Use index as the label
        });
    }


    public static void closeWindow() {
        if (perfStage != null) {
            perfStage.close();
            perfStage = null;
        }
    }
}
