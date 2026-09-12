package dsl.ui.views;

import dsl.application.AppContext;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public final class FaultToleranceView extends BaseView {

    public FaultToleranceView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        buildUi();
    }

    private void buildUi() {
        installCanvas();
        HBox bar = createControlBar();

        Slider loss    = Ui.slider(0, 1, 0);
        Slider latency = Ui.slider(0, 2000, 120);
        Slider jitter  = Ui.slider(0, 500, 40);
        Slider dup     = Ui.slider(0, 1, 0);
        Slider timeout = Ui.slider(100, 5000, 1500);

        loss.valueProperty().addListener((o, a, b) -> network.conditions().setPacketLoss(b.doubleValue()));
        latency.valueProperty().addListener((o, a, b) -> network.conditions().setLatencyMs(b.doubleValue()));
        jitter.valueProperty().addListener((o, a, b) -> network.conditions().setJitterMs(b.doubleValue()));
        dup.valueProperty().addListener((o, a, b) -> network.conditions().setDuplicateRate(b.doubleValue()));
        timeout.valueProperty().addListener((o, a, b) -> network.conditions().setTimeoutMs(b.longValue()));

        Button crash = Ui.danger("Crash Selected", e -> {
            var n = canvas.selectedNode();
            if (n != null) { n.fail(); log.warn("FAULT", n.id() + " crashed"); }
        });
        Button recover = Ui.success("Recover Selected", e -> {
            var n = canvas.selectedNode();
            if (n != null) { n.recover(); log.info("FAULT", n.id() + " recovered"); }
        });
        Button partition = Ui.button("Partition", e -> {
            var nodes = network.nodeList();
            int half = nodes.size() / 2;
            var a = nodes.subList(0, half).stream().map(dsl.model.Node::id).toList();
            var b = nodes.subList(half, nodes.size()).stream().map(dsl.model.Node::id).toList();
            network.partition(a, b);
        });
        Button heal = Ui.button("Heal", e -> network.healPartition());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(6);
        form.setPadding(new Insets(10));
        int r = 0;
        form.addRow(r++, Ui.caption("Packet loss"),     loss);
        form.addRow(r++, Ui.caption("Latency (ms)"),    latency);
        form.addRow(r++, Ui.caption("Jitter (ms)"),     jitter);
        form.addRow(r++, Ui.caption("Duplicate rate"),  dup);
        form.addRow(r++, Ui.caption("Timeout (ms)"),    timeout);

        HBox controls = new HBox(8, crash, recover, partition, heal);
        form.add(controls, 0, r, 2, 1);

        setTop(bar);
        setRight(form);

        Platform.runLater(() -> {
            for (int i = 1; i <= 5; i++) network.addNode("F" + i, "10.7.0." + i);
            canvas.layoutInCircle(100);
            canvas.layoutFullMesh();
        });
    }

    @Override public String title()    { return "Fault Tolerance"; }
    @Override public String subtitle() { return "Inject crashes, packet loss, partitions in real time"; }
}