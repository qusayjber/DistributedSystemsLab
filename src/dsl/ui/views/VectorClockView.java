package dsl.ui.views;

import dsl.application.AppContext;
import dsl.model.CausalRelation;
import dsl.model.MessageType;
import dsl.model.Node;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

public final class VectorClockView extends BaseView {

    private final TextArea timeline = new TextArea();

    public VectorClockView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        buildUi();
    }

    private void buildUi() {
        installCanvas();
        timeline.setEditable(false);
        timeline.setPrefRowCount(8);
        timeline.getStyleClass().add("event-log-panel");

        HBox bar = createControlBar();

        Button concurrent = Ui.primary("Demonstrate Concurrency", e -> demonstrate());
        Button compare    = Ui.button("Compare Selected", e -> compareSelected());
        bar.getChildren().addAll(concurrent, compare);
        setTop(bar);

        VBox bottom = new VBox(6, new Label("Causality timeline"), timeline);
        bottom.setPadding(new Insets(10));
        setBottom(bottom);

        Platform.runLater(() -> {
            for (int i = 1; i <= 4; i++) network.addNode("V" + i, "10.2.0." + i);
            canvas.layoutInCircle(90);
            canvas.layoutFullMesh();
            network.onDelivery(this::logMessage);
        });
    }

    private void logMessage(dsl.model.Message m) {
        Node dst = network.node(m.destinationId());
        if (dst == null) return;
        Map<String, Integer> snap = dst.getVectorClock().snapshot();
        timeline.appendText("[" + m.sourceId() + "→" + m.destinationId() + "] vc=" + snap + "\n");
    }

    private void demonstrate() {
        var nodes = network.nodeList();
        if (nodes.size() < 3) return;
        // A → B, A → C concurrently, then B→C
        network.send(nodes.get(0).id(), nodes.get(1).id(), MessageType.PING, "evt-A");
        network.send(nodes.get(0).id(), nodes.get(2).id(), MessageType.PING, "evt-B");
        engine.schedule(400, "delayed C→B", e ->
                network.send(nodes.get(2).id(), nodes.get(1).id(), MessageType.PING, "evt-C"));
    }

    private void compareSelected() {
        var nodes = network.nodeList();
        if (nodes.size() < 2) return;
        var a = nodes.get(0).getVectorClock().snapshot();
        var b = nodes.get(1).getVectorClock().snapshot();
        CausalRelation rel = dsl.model.VectorClock.compare(a, b);
        log.algorithm("VECTOR", nodes.get(0).id() + " vs " + nodes.get(1).id() + " → " + rel);
        timeline.appendText("Compare " + nodes.get(0).id() + " vs " + nodes.get(1).id() + " = " + rel + "\n");
    }

    @Override public String title()    { return "Vector Clock"; }
    @Override public String subtitle() { return "Track causality — BEFORE / AFTER / CONCURRENT"; }
}