package dsl.ui.views;

import dsl.algorithms.loadbalancing.LoadBalancingStrategy;
import dsl.algorithms.loadbalancing.Strategies;
import dsl.application.AppContext;
import dsl.ui.LabView;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public final class LoadBalancingView extends BorderPane implements LabView {

    private final List<String> servers = List.of("S1", "S2", "S3", "S4");
    private final List<Integer> connections = new ArrayList<>(List.of(0, 0, 0, 0));
    private final List<Integer> weights = List.of(1, 2, 3, 1);

    private LoadBalancingStrategy strategy = new Strategies.RoundRobin();

    private final Label stats = new Label();
    private final Label perServer = new Label();

    private int total, success, failed, latencySum;

    public LoadBalancingView(AppContext ctx, Navigator nav) {
        buildUi();
    }

    private void buildUi() {
        ComboBox<String> strat = Ui.combo("Round Robin", "Weighted Round Robin",
                "Least Connections", "Random", "Least Load");
        strat.valueProperty().addListener((o, a, b) -> strategy = switch (b) {
            case "Weighted Round Robin" -> new Strategies.WeightedRoundRobin();
            case "Least Connections"    -> new Strategies.LeastConnections();
            case "Random"               -> new Strategies.RandomStrategy();
            case "Least Load"           -> new Strategies.LeastLoad();
            default                     -> new Strategies.RoundRobin();
        });

        Button fire = Ui.primary("Fire 100 requests", e -> {
            for (int i = 0; i < 100; i++) dispatch();
            refresh();
        });
        Button reset = Ui.button("Reset", e -> {
            total = success = failed = latencySum = 0;
            for (int i = 0; i < connections.size(); i++) connections.set(i, 0);
            refresh();
        });

        Button tick = Ui.button("One Request", e -> { dispatch(); refresh(); });

        HBox top = new HBox(8, strat, tick, fire, reset);
        top.setPadding(new Insets(12));

        stats.getStyleClass().add("label-title");
        perServer.getStyleClass().add("label-value");

        VBox centre = new VBox(12,
                Ui.card("Statistics", stats),
                Ui.card("Per‑server connections", perServer));
        centre.setPadding(new Insets(16));

        setTop(top);
        setCenter(centre);
        refresh();
    }

    private void dispatch() {
        int idx = strategy.pick(servers, connections, weights);
        total++;
        // random 5% failure, latency 20-120 ms
        if (Math.random() < 0.05) { failed++; }
        else { success++; latencySum += 20 + (int) (Math.random() * 100); }
        connections.set(idx, connections.get(idx) + 1);
    }

    private void refresh() {
        double avgLatency = success == 0 ? 0 : (double) latencySum / success;
        stats.setText("Requests: " + total
                + "   Success: " + success
                + "   Failed: " + failed
                + "   Avg latency: " + String.format("%.1f ms", avgLatency)
                + "\nStrategy: " + strategy.name());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < servers.size(); i++) {
            sb.append(servers.get(i))
              .append("  weight=").append(weights.get(i))
              .append("  conn=").append(connections.get(i))
              .append("\n");
        }
        perServer.setText(sb.toString());

        Timeline tl = new Timeline(new KeyFrame(Duration.millis(0), e -> { }));
        tl.play();
    }

    @Override public String title()    { return "Load Balancing"; }
    @Override public String subtitle() { return "Compare strategies and observe distribution"; }
    @Override public Node content()    { return this; }
}