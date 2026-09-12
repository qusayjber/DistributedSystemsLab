package dsl.ui.views;

import dsl.application.AppContext;
import dsl.ui.LabView;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public final class MetricsView extends BorderPane implements LabView {

    private final AppContext ctx;
    private final XYChart.Series<Number, Number> sentSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> recvSeries = new XYChart.Series<>();
    private int tick;

    public MetricsView(AppContext ctx, Navigator nav) {
        this.ctx = ctx;
        buildUi();
    }

    private void buildUi() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        int row = 0;
        for (String k : new String[]{
                "Total Nodes", "Messages Sent", "Messages Received",
                "Messages Lost", "Average Latency", "Steps"}) {
            Label value = new Label();
            value.getStyleClass().add("label-value");
            value.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            grid.add(Ui.card(k, value), 0, row++);

            Timeline refresh = new Timeline(new KeyFrame(Duration.millis(300), e -> {
                value.setText(switch (k) {
                    case "Total Nodes"        -> String.valueOf(ctx.metrics().totalNodes());
                    case "Messages Sent"      -> String.valueOf(ctx.metrics().messagesSent());
                    case "Messages Received"  -> String.valueOf(ctx.metrics().messagesReceived());
                    case "Messages Lost"      -> String.valueOf(ctx.metrics().messagesLost());
                    case "Average Latency"    -> String.format("%.1f ms", ctx.metrics().averageLatency());
                    case "Steps"              -> String.valueOf(ctx.metrics().steps());
                    default                   -> "—";
                });
            }));
            refresh.setCycleCount(Timeline.INDEFINITE);
            refresh.play();
        }

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time (ticks)");
        yAxis.setLabel("Count");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setCreateSymbols(false);
        sentSeries.setName("Sent");
        recvSeries.setName("Received");
        chart.getData().addAll(sentSeries, recvSeries);
        chart.setPrefHeight(320);

        Timeline sample = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            sentSeries.getData().add(new XYChart.Data<>(tick, ctx.metrics().messagesSent()));
            recvSeries.getData().add(new XYChart.Data<>(tick, ctx.metrics().messagesReceived()));
            if (sentSeries.getData().size() > 120) {
                sentSeries.getData().remove(0);
                recvSeries.getData().remove(0);
            }
            tick++;
        }));
        sample.setCycleCount(Timeline.INDEFINITE);
        sample.play();

        VBox right = new VBox(12, Ui.card("Throughput", chart));
        right.setPadding(new Insets(20));

        setCenter(grid);
        setRight(right);
    }

    @Override public String title()    { return "Metrics"; }
    @Override public String subtitle() { return "Live counters and throughput"; }
    @Override public Node content()    { return this; }
}