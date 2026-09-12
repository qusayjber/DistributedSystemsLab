package dsl.ui.views;

import dsl.application.AppContext;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ConsistencyView extends BaseView {

    private enum Model { STRONG, EVENTUAL }
    private Model model = Model.STRONG;

    private final Map<String, String> primary = new LinkedHashMap<>();
    private final Map<String, String> replica = new LinkedHashMap<>();
    private final TextField keyField = Ui.textField("key");
    private final TextField valueField = Ui.textField("value");
    private final Label status = new Label();

    public ConsistencyView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        buildUi();
    }

    private void buildUi() {
        installCanvas();
        HBox bar = createControlBar();

        Button strong = Ui.button("Strong", e -> { model = Model.STRONG; refresh(); });
        Button eventual = Ui.button("Eventual", e -> { model = Model.EVENTUAL; refresh(); });

        Button write = Ui.primary("WRITE", e -> {
            String k = keyField.getText().isEmpty() ? "k" : keyField.getText();
            String v = valueField.getText().isEmpty() ? "v" : valueField.getText();
            primary.put(k, v);
            long delay = model == Model.STRONG ? 250 : 1500;
            engine.schedule(delay, "propagate", ev -> {
                replica.put(k, v);
                log.algorithm("CONSISTENCY", "Replica caught up " + k + "=" + v);
                Platform.runLater(this::refresh);
            });
            log.algorithm("CONSISTENCY", "WRITE " + k + "=" + v);
            refresh();
        });

        Button read = Ui.button("READ", e -> {
            String k = keyField.getText().isEmpty() ? "k" : keyField.getText();
            String primaryVal = primary.get(k);
            String replicaVal = replica.get(k);
            log.algorithm("CONSISTENCY", "READ " + k
                    + " primary=" + primaryVal + " replica=" + replicaVal
                    + (java.util.Objects.equals(primaryVal, replicaVal) ? " (consistent)" : " (stale)"));
            refresh();
        });

        bar.getChildren().addAll(strong, eventual, keyField, valueField, write, read);
        setTop(bar);

        VBox bottom = new VBox(status);
        setBottom(bottom);

        Platform.runLater(() -> {
            network.addNode("P", "10.4.0.1");
            network.addNode("R", "10.4.0.2");
            network.connect("P", "R");
            network.node("P").setPosition(300, 220);
            network.node("R").setPosition(560, 220);
            refresh();
        });
    }

    private void refresh() {
        status.setText("Model: " + model
                + "   Primary: " + primary
                + "   Replica: " + replica);
    }

    @Override public String title()    { return "Consistency"; }
    @Override public String subtitle() { return "Strong vs Eventual — observe stale reads"; }
}