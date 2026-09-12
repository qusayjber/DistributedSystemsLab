package dsl.ui.views;

import dsl.algorithms.raft.RaftCluster;
import dsl.algorithms.raft.RaftRole;
import dsl.algorithms.raft.RaftState;
import dsl.application.AppContext;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class RaftView extends BaseView {

    private final RaftCluster raft;
    private final Label info = new Label("—");

    public RaftView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        this.raft = new RaftCluster(network, engine, log);
        buildUi();
    }

    private void buildUi() {
        installCanvas();

        HBox bar = createControlBar();

        Button start   = Ui.success("▶ Start",  e -> raft.start());
        Button pause   = Ui.button("⏸ Pause",   e -> raft.stop());
        Button step    = Ui.primary("⏭ Step",   e -> engine.step());
        Button reset   = Ui.button("⟲ Reset",   e -> { raft.reset(); network.reset(); });
        Button fail    = Ui.danger("Fail Leader", e -> raft.failLeader());
        Button recover = Ui.success("Recover",  e -> {
            var n = canvas.selectedNode();
            if (n != null) raft.recover(n.id());
        });
        Button append  = Ui.button("+ Append",  e -> raft.append("cmd-" + System.currentTimeMillis() % 1000));

        bar.getChildren().addAll(start, pause, step, reset, fail, recover, append);
        setTop(bar);

        VBox bottom = new VBox(4, info);
        bottom.setPadding(new Insets(8, 14, 8, 14));
        setBottom(bottom);

        network.onDelivery(raft::onMessage);

        Platform.runLater(() -> {
            for (int i = 1; i <= 5; i++) {
                String id = "R" + i;
                network.addNode(id, "10.0." + i + ".1");
                raft.register(id);
            }
            canvas.layoutInCircle(100);
            canvas.layoutFullMesh();
        });

        // periodic UI refresh
        var refresh = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(300), e -> refreshInfo()));
        refresh.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        refresh.play();
    }

    private void refreshInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Term: ").append(raft.term())
          .append("   Leader: ").append(raft.leaderId() == null ? "—" : raft.leaderId())
          .append("   Quorum: ").append(raft.quorum());
        for (String id : raft.nodeIds()) {
            RaftState st = raft.stateOf(id);
            if (st == null) continue;
            sb.append("\n  ").append(id)
              .append("  role=").append(st.role)
              .append("  term=").append(st.currentTerm)
              .append("  votes=").append(st.votesReceived)
              .append("  log=").append(st.log.size())
              .append("  commit=").append(st.commitIndex);
        }
        info.setText(sb.toString());

        // reflect role on node colours
        for (String id : raft.nodeIds()) {
            RaftState st = raft.stateOf(id);
            var n = network.node(id);
            if (st == null || n == null) continue;
            switch (st.role) {
                case LEADER    -> n.setRole(dsl.model.NodeRole.LEADER);
                case CANDIDATE -> n.setRole(dsl.model.NodeRole.CANDIDATE);
                case FOLLOWER  -> n.setRole(dsl.model.NodeRole.FOLLOWER);
            }
        }
    }

    @Override public String title()    { return "Raft Consensus"; }
    @Override public String subtitle() { return "Election timeouts, RequestVote, log replication"; }
}