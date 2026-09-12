package dsl.ui.views;

import dsl.application.AppContext;
import dsl.ui.LabView;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public final class DashboardView extends BorderPane implements LabView {

    private final AppContext ctx;
    private final Navigator nav;

    public DashboardView(AppContext ctx, Navigator nav) {
        this.ctx = ctx;
        this.nav = nav;
        buildUi();
    }

    private void buildUi() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));

        Label h = new Label("Distributed Systems Laboratory");
        h.getStyleClass().add("label-title");
        Label sub = new Label(
                "Interactive simulation and visualization environment for distributed computing.");
        sub.getStyleClass().add("label-subtitle");

        FlowPane actions = new FlowPane(12, 12);
        actions.getChildren().addAll(
                action("Create Network",  "network"),
                action("Start Raft",      "raft"),
                action("Open Chord",      "chord"),
                action("Open Clock Lab",  "lamport"),
                action("Fault Injection", "faults"),
                action("View Metrics",    "metrics")
        );

        Label nodesVal  = bold();
        Label sentVal   = bold();
        Label recvVal   = bold();
        Label lostVal   = bold();
        Label latVal    = bold();
        Label eventsVal = bold();

        HBox stats = new HBox(12,
                statCard("Total Nodes",    nodesVal),
                statCard("Messages Sent",  sentVal),
                statCard("Received",       recvVal),
                statCard("Lost",           lostVal),
                statCard("Avg Latency",    latVal),
                statCard("Events",         eventsVal));

        Runnable refresh = () -> {
            nodesVal.setText(String.valueOf(ctx.metrics().totalNodes()));
            sentVal.setText(String.valueOf(ctx.metrics().messagesSent()));
            recvVal.setText(String.valueOf(ctx.metrics().messagesReceived()));
            lostVal.setText(String.valueOf(ctx.metrics().messagesLost()));
            latVal.setText(String.format("%.1f ms", ctx.metrics().averageLatency()));
            eventsVal.setText(String.valueOf(ctx.eventLog().entries().size()));
        };
        refresh.run();

        Timeline tl = new Timeline(new KeyFrame(Duration.millis(500), e -> refresh.run()));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();

        content.getChildren().addAll(
                h, sub,
                Ui.card("Quick Actions", actions),
                Ui.card("Live Metrics",  stats));

        setCenter(content);
    }

    private Button action(String label, String key) {
        Button b = Ui.primary(label, e -> nav.navigate(key));
        b.setPrefWidth(180);
        return b;
    }

    private static Label bold() {
        Label l = new Label("—");
        l.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        l.getStyleClass().add("label-value");
        return l;
    }

    private VBox statCard(String key, Label value) {
        Label k = new Label(key);
        k.getStyleClass().add("label-caption");
        VBox card = Ui.card(null, k, value);
        card.setMinWidth(150);
        return card;
    }

    @Override public String title()    { return "Dashboard"; }
    @Override public String subtitle() { return "Overview and quick actions"; }
    @Override public Node content()    { return this; }
}