package dsl.ui.views;

import dsl.algorithms.transactions.TwoPhaseCommit;
import dsl.application.AppContext;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * A composed transaction lab: 2PC on top of a replica set. Every participant
 * also reports a state string.
 */
public final class DistributedTransactionsView extends BaseView {

    private final TwoPhaseCommit tpc;
    private final Label info = new Label();

    public DistributedTransactionsView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        this.tpc = new TwoPhaseCommit(network, engine, log);
        buildUi();
    }

    private void buildUi() {
        installCanvas();
        HBox bar = createControlBar();

        bar.getChildren().addAll(
                Ui.primary("Start Transaction", e -> tpc.begin("C", List.of("A", "B", "C2"))),
                Ui.success("Vote Commit",       e -> { tpc.vote("A", true);  tpc.vote("B", true);  tpc.vote("C2", true); }),
                Ui.danger ("Vote Abort",        e -> { tpc.vote("A", true);  tpc.vote("B", false); tpc.vote("C2", true); }),
                Ui.button ("Fail A",            e -> tpc.failParticipant("A"))
        );
        setTop(bar);

        setBottom(new VBox(info));

        Platform.runLater(() -> {
            network.addNode("C",  "10.6.0.1");
            network.addNode("A",  "10.6.0.2");
            network.addNode("B",  "10.6.0.3");
            network.addNode("C2", "10.6.0.4");
            network.connect("C", "A");
            network.connect("C", "B");
            network.connect("C", "C2");
            network.connect("A", "B");
            network.connect("B", "C2");
            canvas.layoutInCircle(100);
            network.node("C").setRole(dsl.model.NodeRole.COORDINATOR);
        });

        var tl = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(400), e ->
                        info.setText("TX " + tpc.txId() + "  phase=" + tpc.phase()
                                + "  votes=" + tpc.votes())));
        tl.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        tl.play();
    }

    @Override public String title()    { return "Distributed Transactions"; }
    @Override public String subtitle() { return "2PC across a multi-node cluster with failures"; }
}