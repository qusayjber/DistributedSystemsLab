package dsl.ui;

import dsl.services.SettingsService;
import javafx.scene.Scene;

import java.util.Objects;

public final class ThemeManager {

    private static final String CSS_PATH = "/css/app.css";

    private final SettingsService settings;
    private Scene scene;
    private boolean dark;

    public ThemeManager(SettingsService settings) {
        this.settings = settings;
        this.dark = !"light".equalsIgnoreCase(settings.get("theme"));
    }

    public void attach(Scene scene) {
        this.scene = scene;
        apply();
    }

    public boolean isDark() { return dark; }

    public void toggle() {
        dark = !dark;
        settings.set("theme", dark ? "dark" : "light");
        settings.save();
        apply();
    }

    private void apply() {
        if (scene == null) return;
        scene.getStylesheets().clear();
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource(CSS_PATH)).toExternalForm());
        scene.getRoot().getStyleClass().removeAll("theme-dark", "theme-light");
        scene.getRoot().getStyleClass().add(dark ? "theme-dark" : "theme-light");
    }
}