package UI;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Test extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Music Player Simulator");

        Button downloadButton = new Button("Download");
        Button playButton = new Button("Play");

        HBox topBar = new HBox(10, downloadButton, playButton); // 10 是按钮间距
        topBar.setStyle("-fx-padding: 6; -fx-background-color: #c9c9c9;"); // 设置背景色和内边距

        StackPane downloadPage = new StackPane(new Label("Download Page"));

        VBox playPage = new VBox(10); // 每行歌曲条目的间距
        playPage.setStyle("-fx-padding: 10;");

        // 添加占位符歌曲信息
        for (int i = 1; i <= 5; i++) {
            HBox songRow = new HBox(20); // 每项之间的间距
            Label songNumber = new Label(i + "");
            songNumber.setPrefWidth(50);

            Label songName = new Label("Song " + i);
            songName.setPrefWidth(200);

            Button playBtn = new Button("Play");
            Button downloadBtn = new Button("Download");

            songRow.getChildren().addAll(songNumber, songName, playBtn, downloadBtn);
            playPage.getChildren().add(songRow);
        }

        // 主布局
        BorderPane root = new BorderPane();
        root.setTop(topBar);

        // 默认显示播放页面
        root.setCenter(playPage);

        // 按钮事件：切换页面
        downloadButton.setOnAction(event -> root.setCenter(downloadPage));
        playButton.setOnAction(event -> root.setCenter(playPage));

        // 场景设置
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
