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

public final class TwoPhaseCommitView extends BaseView {

    private final TwoPhaseCommit tpc;
    private final Label info = new Label("—");

    public TwoPhaseCommitView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        this.tpc = new TwoPhaseCommit(network, engine, log);
        buildUi();
    }

    private void buildUi() {
        installCanvas();
        HBox bar = createControlBar();

        Button begin = Ui.primary("BEGIN TX", e -> {
            tpc.begin("C", List.of("P1", "P2", "P3"));
        });

        Button voteYes = Ui.success("All vote YES", e -> {
            tpc.vote("P1", true); tpc.vote("P2", true); tpc.vote("P3", true);
        });
        Button voteNo = Ui.danger("P2 votes NO", e -> {
            tpc.vote("P1", true); tpc.vote("P2", false); tpc.vote("P3", true);
        });

        bar.getChildren().addAll(begin, voteYes, voteNo);
        setTop(bar);

        VBox bottom = new VBox(info);
        setBottom(bottom);

        network.onDelivery(m -> { /* view is state-driven, messages are shown in log */ });

        Platform.runLater(() -> {
            network.addNode("C",  "10.5.0.1");
            network.addNode("P1", "10.5.0.2");
            network.addNode("P2", "10.5.0.3");
            network.addNode("P3", "10.5.0.4");
            network.connect("C", "P1");
            network.connect("C", "P2");
            network.connect("C", "P3");
            network.node("C").setPosition(400, 100);
            network.node("P1").setPosition(200, 340);
            network.node("P2").setPosition(400, 380);
            network.node("P3").setPosition(600, 340);
            network.node("C").setRole(dsl.model.NodeRole.COORDINATOR);
            network.node("P1").setRole(dsl.model.NodeRole.PARTICIPANT);
            network.node("P2").setRole(dsl.model.NodeRole.PARTICIPANT);
            network.node("P3").setRole(dsl.model.NodeRole.PARTICIPANT);
        });

        var tl = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(400), e -> refresh()));
        tl.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        tl.play();
    }

    private void refresh() {
        info.setText("TX: " + (tpc.txId() == null ? "—" : tpc.txId())
                + "   Coordinator: " + tpc.coordinator()
                + "   Phase: " + tpc.phase()
                + "   Votes: " + tpc.votes());
    }

    @Override public String title()    { return "Two-Phase Commit"; }
    @Override public String subtitle() { return "PREPARE → COMMIT / ABORT"; }
}