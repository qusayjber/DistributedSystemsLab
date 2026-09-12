package dsl.ui.views;

import dsl.application.AppContext;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class CapTheoremView extends BaseView {

    private final Label info = new Label();
    private boolean cp = true;

    public CapTheoremView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        buildUi();
    }

    private void buildUi() {
        installCanvas();
        HBox bar = createControlBar();

        Button cpBtn = Ui.primary("CP mode", e -> { cp = true;  refresh(); });
        Button apBtn = Ui.button("AP mode", e -> { cp = false; refresh(); });

        Button partition = Ui.button("Partition", e -> {
            var n = network.nodeList();
            int half = n.size() / 2;
            var a = n.subList(0, half).stream().map(x -> x.id()).toList();
            var b = n.subList(half, n.size()).stream().map(x -> x.id()).toList();
            network.partition(a, b);
            refresh();
        });
        Button heal = Ui.button("Heal", e -> { network.healPartition(); refresh(); });

        Button writeLeft = Ui.button("Write on left side", e -> {
            var n = network.nodeList().get(0);
            log.algorithm("CAP", "WRITE " + n.id() + "=v@" + System.currentTimeMillis() % 1000);
            if (network.isPartitioned()) {
                if (cp) log.warn("CAP", "CP system refuses write (no quorum)");
                else    log.algorithm("CAP", "AP system accepts write on " + n.id());
            }
            refresh();
        });

        bar.getChildren().addAll(cpBtn, apBtn, partition, heal, writeLeft);
        setTop(bar);

        setBottom(new VBox(info));

        Platform.runLater(() -> {
            for (int i = 1; i <= 6; i++) network.addNode("S" + i, "10.8.0." + i);
            canvas.layoutInCircle(100);
            canvas.layoutFullMesh();
            refresh();
        });
    }

    private void refresh() {
        info.setText("Mode: " + (cp ? "CP (Consistency + Partition tolerance)" : "AP (Availability + Partition tolerance)")
                + "   Partitioned: " + network.isPartitioned()
                + (network.isPartitioned() ?
                    (cp ? "   → writes refused on minority side" : "   → writes accepted, may diverge")
                    : "   → normal operation"));
    }

    @Override public String title()    { return "CAP Theorem"; }
    @Override public String subtitle() { return "Choose Consistency or Availability under partition"; }
}