package UI;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Test extends Application {
    final static int BUTTON_SIZE = 25;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Music Player Simulator");

        Button downloadPageButton = new Button("Download");
        Button playPageButton = new Button("Play");

        HBox topBar = new HBox(10, downloadPageButton, playPageButton);
        topBar.setStyle("-fx-padding: 6; -fx-background-color: #ececec;");

        StackPane downloadPage = new StackPane(new Label("Download Page"));

        VBox playPage = new VBox();

        Image playIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/playButton.png")));
        Image hoverPlayIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/playButtonHover.png")));

        Image downloadIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/downloadButton.png")));
        Image hoverDownloadIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/downloadButtonHover.png")));

        for (int i = 1; i <= 5; i++) {
            HBox songRow = new HBox(10);
            songRow.setPrefHeight(50);
            songRow.setAlignment(Pos.CENTER_LEFT);

            Label songNumber = new Label("   " + i);
            songNumber.setPrefWidth(50);
            songNumber.setStyle("-fx-font-size: 16px;");

            Label songName = new Label("Song " + i);
            songName.setPrefWidth(100);
            songName.setStyle("-fx-font-size: 16px;");

            Region spacer = new Region();
            spacer.setMinWidth(Region.USE_COMPUTED_SIZE);
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Button playButton = getButton(playIcon, hoverPlayIcon);
            Button downloadButton = getButton(downloadIcon, hoverDownloadIcon);

            songRow.getChildren().addAll(songNumber, songName, spacer, playButton, downloadButton);
            playPage.getChildren().add(songRow);

            songRow.setOnMouseEntered(event -> songRow.setStyle("-fx-background-color: #ececec;"));
            songRow.setOnMouseExited(event -> songRow.setStyle("-fx-background-color: transparent;"));
        }

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(playPage);

        downloadPageButton.setOnAction(event -> root.setCenter(downloadPage));
        playPageButton.setOnAction(event -> root.setCenter(playPage));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @NotNull
    private static Button getButton(Image defaultIcon, Image hoverIcon) {
        Button button = new Button();
        ImageView iconView = new ImageView(defaultIcon);
        iconView.setFitWidth(BUTTON_SIZE);
        iconView.setFitHeight(BUTTON_SIZE);
        iconView.setPreserveRatio(true);
        button.setGraphic(iconView);
        button.setStyle("-fx-background-color: transparent;");

        button.setOnMouseEntered(event -> {
            ImageView hoverIconView = new ImageView(hoverIcon);
            hoverIconView.setFitWidth(BUTTON_SIZE);
            hoverIconView.setFitHeight(BUTTON_SIZE);
            hoverIconView.setPreserveRatio(true);
            button.setGraphic(hoverIconView);
            button.setCursor(Cursor.HAND);
        });
        button.setOnMouseExited(event -> {
            ImageView defaultIconView = new ImageView(defaultIcon);
            defaultIconView.setFitWidth(BUTTON_SIZE);
            defaultIconView.setFitHeight(BUTTON_SIZE);
            defaultIconView.setPreserveRatio(true);
            button.setGraphic(defaultIconView);
            button.setCursor(Cursor.DEFAULT);
        });
        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
