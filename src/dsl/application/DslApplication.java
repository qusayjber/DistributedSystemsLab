package dsl.application;

import dsl.ui.MainWindow;
import dsl.ui.ThemeManager;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public final class DslApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        AppContext ctx = new AppContext();
        ThemeManager themeManager = new ThemeManager(ctx.settings());
        ctx.attachThemeManager(themeManager);

        ctx.eventLog().info("APP", "Distributed Systems Laboratory starting…");

        SplashScreen.show(() -> {
            MainWindow window = new MainWindow(ctx);

            /* -------- Size the window to fit the real screen ------------- */
            Rectangle2D visual = Screen.getPrimary().getVisualBounds();
            double w = Math.min(1480, visual.getWidth()  * 0.90);
            double h = Math.min( 940, visual.getHeight() * 0.90);

            Scene scene = new Scene(window, w, h);
            themeManager.attach(scene);

            primaryStage.setTitle("Distributed Systems Laboratory");
            primaryStage.setScene(scene);

            /* -------- Force a real, decorated, windowed window ------------ */
            primaryStage.setMaximized(false);
            primaryStage.setFullScreen(false);
            primaryStage.setResizable(true);
            primaryStage.initStyle(StageStyle.DECORATED);

            primaryStage.setMinWidth(1120);
            primaryStage.setMinHeight(720);

            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
            primaryStage.show();

            ctx.eventLog().info("APP", "Workspace ready ("
                    + (int) w + "×" + (int) h + ")");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}