package dsl.ui.views;

import dsl.algorithms.election.BullyElection;
import dsl.algorithms.election.RingElection;
import dsl.application.AppContext;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

public final class LeaderElectionView extends BaseView {

    private final BullyElection bully;
    private final RingElection  ring;
    private volatile String mode = "Bully";

    public LeaderElectionView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        this.bully = new BullyElection(network, engine, log);
        this.ring  = new RingElection(network, engine, log);
        buildUi();
    }

    private void buildUi() {
        installCanvas();

        HBox bar = createControlBar();

        ComboBox<String> algorithm = Ui.combo("Bully", "Ring");
        algorithm.valueProperty().addListener((o, a, b) -> mode = b);

        Button start = Ui.primary("Start Election", e -> {
            var n = canvas.selectedNode();
            if (n == null) return;
            if ("Bully".equals(mode)) bully.startElection(n.id());
            else                      ring.startElection(n.id());
        });

        Button fail = Ui.danger("Fail Selected", e -> {
            var n = canvas.selectedNode();
            if (n != null) { n.fail(); log.warn("ELECTION", n.id() + " failed"); }
        });

        Button recover = Ui.success("Recover Selected", e -> {
            var n = canvas.selectedNode();
            if (n != null) { n.recover(); log.info("ELECTION", n.id() + " recovered"); }
        });

        bar.getChildren().addAll(algorithm, start, fail, recover);
        setTop(bar);

        network.onDelivery(m -> {
            if ("Bully".equals(mode)) bully.onMessage(m);
            else                      ring.onMessage(m);
        });

        Platform.runLater(() -> {
            for (int i = 1; i <= 6; i++) network.addNode("E" + i, "10.1.0." + i);
            canvas.layoutInCircle(90);
            canvas.layoutRing();
        });
    }

    @Override public String title()    { return "Leader Election"; }
    @Override public String subtitle() { return "Bully and Ring algorithms, animated"; }
}