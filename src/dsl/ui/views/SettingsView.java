package dsl.ui.views;

import dsl.application.AppContext;
import dsl.ui.LabView;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

public final class SettingsView extends BorderPane implements LabView {

    private final AppContext ctx;

    public SettingsView(AppContext ctx, Navigator nav) {
        this.ctx = ctx;
        buildUi();
    }

    private void buildUi() {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(24));

        int r = 0;

        ComboBox<String> theme = Ui.combo("dark", "light");
        theme.getSelectionModel().select(ctx.settings().get("theme"));
        theme.valueProperty().addListener((o, a, b) -> {
            ctx.settings().set("theme", b);
            if (!b.equals(ctx.themeManager().isDark() ? "dark" : "light"))
                ctx.themeManager().toggle();
        });
        form.addRow(r++, Ui.caption("Theme"), theme);

        Slider simSpeed = Ui.slider(0.25, 10.0, ctx.settings().getDouble("simulationSpeed", 1.0));
        simSpeed.valueProperty().addListener((o, a, b) ->
                ctx.settings().set("simulationSpeed", String.valueOf(b.doubleValue())));
        form.addRow(r++, Ui.caption("Simulation speed"), simSpeed);

        Spinner<Integer> defaultNodes = new Spinner<>(1, 32,
                ctx.settings().getInt("defaultNodeCount", 5));
        defaultNodes.valueProperty().addListener((o, a, b) ->
                ctx.settings().set("defaultNodeCount", String.valueOf(b)));
        form.addRow(r++, Ui.caption("Default node count"), defaultNodes);

        Slider latency = Ui.slider(0, 2000, ctx.settings().getDouble("latency", 120));
        latency.valueProperty().addListener((o, a, b) ->
                ctx.settings().set("latency", String.valueOf(b.doubleValue())));
        form.addRow(r++, Ui.caption("Default latency (ms)"), latency);

        Slider loss = Ui.slider(0, 1, ctx.settings().getDouble("packetLoss", 0));
        loss.valueProperty().addListener((o, a, b) ->
                ctx.settings().set("packetLoss", String.valueOf(b.doubleValue())));
        form.addRow(r++, Ui.caption("Packet loss"), loss);

        ComboBox<String> logLevel = Ui.combo("DEBUG", "INFO", "WARN", "ERROR", "ALGORITHM");
        logLevel.getSelectionModel().select(ctx.settings().get("logLevel"));
        logLevel.valueProperty().addListener((o, a, b) -> {
            ctx.settings().set("logLevel", b);
            try {
                ctx.eventLog().setThreshold(dsl.services.EventLogService.Level.valueOf(b));
            } catch (Exception ignored) { }
        });
        form.addRow(r++, Ui.caption("Log level"), logLevel);

        Button save = Ui.primary("Save", e -> {
            ctx.settings().save();
            ctx.eventLog().info("SETTINGS", "Settings saved");
        });
        form.addRow(r, new Label(), save);

        setCenter(form);
    }

    @Override public String title()    { return "Settings"; }
    @Override public String subtitle() { return "Theme, simulation defaults and log level"; }
    @Override public Node content()    { return this; }
}