package dsl.application;

import dsl.ui.Ui;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public final class SplashScreen {

    private SplashScreen() { }

    public static void show(Runnable onFinished) {
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);

        Label title = new Label("Distributed Systems Laboratory");
        title.getStyleClass().add("splash-title");

        Label subtitle = new Label("Interactive simulation and visualization environment\nfor distributed computing.");
        subtitle.getStyleClass().add("splash-subtitle");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label badge = new Label("DSL");
        badge.getStyleClass().add("splash-badge");

        VBox root = new VBox(14, badge, title, subtitle);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(48, 64, 48, 64));
        root.getStyleClass().add("splash-root");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(
                SplashScreen.class.getResource("/css/app.css").toExternalForm());

        stage.setScene(scene);
        stage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition wait = new PauseTransition(Duration.millis(1400));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeIn.play();
        wait.setOnFinished(e -> fadeOut.play());
        fadeOut.setOnFinished(e -> {
            stage.close();
            onFinished.run();
        });
        wait.play();
    }
}