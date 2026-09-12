package dsl.ui.views;

import dsl.application.AppContext;
import dsl.model.MessageType;
import dsl.network.NetworkSimulator;
import dsl.services.EventLogService;
import dsl.services.MetricsService;
import dsl.simulation.SimulationEngine;
import dsl.ui.InspectorPane;
import dsl.ui.LabView;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import dsl.visualization.NetworkCanvas;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * Common scaffolding for canvas‑based labs: an event‑driven engine, a network
 * simulator, a canvas, an inspector and a set of playback controls.
 */
public abstract class BaseView extends BorderPane implements LabView {

    protected final AppContext ctx;
    protected final Navigator navigator;

    protected final EventLogService log;
    protected final MetricsService metrics;
    protected final SimulationEngine engine;
    protected final NetworkSimulator network;
    protected final NetworkCanvas canvas;
    protected final InspectorPane inspector;

    protected BaseView(AppContext ctx, Navigator navigator) {
        this.ctx = ctx;
        this.navigator = navigator;
        this.log = ctx.eventLog();
        this.metrics = ctx.metrics();
        this.engine = new SimulationEngine();
        this.network = new NetworkSimulator(engine, log, metrics);
        this.canvas = new NetworkCanvas(network, engine);
        this.inspector = new InspectorPane();

        canvas.onNodeSelected(n -> {
            if (n != null) inspector.showNode(n);
            else inspector.showEmpty();
        });
        canvas.onMessageSelected(inspector::showMessage);

        ctx.eventLog().info("LAB", title() + " opened");
    }

    /** Places the canvas in the centre and the inspector on the right. */
    protected void installCanvas() {
        BorderPane centre = new BorderPane(canvas);
        centre.setRight(inspector);
        setCenter(centre);
    }

    /** Creates a control bar with Play/Pause/Step/Reset and speed selector. */
    protected HBox createControlBar() {
        HBox bar = new HBox(8);
        bar.setPadding(new Insets(10));

        bar.getChildren().addAll(
                Ui.success("▶ Play",  e -> engine.play()),
                Ui.button("⏸ Pause", e -> engine.pause()),
                Ui.primary("⏭ Step", e -> { engine.pause(); engine.step(); }),
                Ui.button("⟲ Reset", e -> reset())
        );

        ComboBox<String> speed = Ui.combo("0.25x", "0.5x", "1x", "2x", "5x", "10x");
        speed.getSelectionModel().select("1x");
        speed.valueProperty().addListener((o, a, b) ->
                engine.setSpeed(Double.parseDouble(b.replace("x", ""))));
        bar.getChildren().add(speed);

        return bar;
    }

    /** Convenience: send a message between two nodes. */
    protected void send(String from, String to, MessageType type, String payload) {
        network.send(from, to, type, payload);
    }

    protected void reset() {
        engine.reset();
        network.reset();
        log.info("LAB", "Simulation reset");
    }

    @Override public Node content() { return this; }

    @Override public void onActivated() { }

    @Override public void onDeactivated() { engine.pause(); }
}