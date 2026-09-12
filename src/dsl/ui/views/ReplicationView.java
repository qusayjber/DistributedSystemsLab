package dsl.ui.views;

import dsl.algorithms.replication.ReplicationManager;
import dsl.application.AppContext;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class ReplicationView extends BaseView {

    private final ReplicationManager repl;
    private final TextField keyField = Ui.textField("key");
    private final TextField valueField = Ui.textField("value");
    private final Label state = new Label();

    public ReplicationView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        this.repl = new ReplicationManager(network, engine, log);
        buildUi();
    }

    private void buildUi() {
        installCanvas();
        HBox bar = createControlBar();

        Button sync   = Ui.button("Sync",  e -> { repl.setMode(ReplicationManager.Mode.SYNC); log.info("REPL", "SYNC mode"); });
        Button async  = Ui.button("Async", e -> { repl.setMode(ReplicationManager.Mode.ASYNC); log.info("REPL", "ASYNC mode"); });

        Button write = Ui.primary("WRITE", e -> {
            String k = keyField.getText().isEmpty() ? "k" : keyField.getText();
            String v = valueField.getText().isEmpty() ? "v" : valueField.getText();
            repl.write(k, v);
            refresh();
        });

        Button failPrimary = Ui.danger("Fail Primary", e -> { repl.failPrimary(); refresh(); });
        Button recPrimary  = Ui.success("Recover",    e -> { repl.recoverPrimary(); refresh(); });

        bar.getChildren().addAll(sync, async, keyField, valueField, write, failPrimary, recPrimary);
        setTop(bar);

        VBox bottom = new VBox(state);
        setBottom(bottom);

        Platform.runLater(() -> {
            for (int i = 1; i <= 4; i++) network.addNode("N" + i, "10.3.0." + i);
            canvas.layoutInCircle(100);
            for (int i = 2; i <= 4; i++) network.connect("N1", "N" + i);
            repl.setPrimary("N1");
            for (int i = 2; i <= 4; i++) repl.addReplica("N" + i);
            refresh();
        });
    }

    private void refresh() {
        state.setText("Mode: " + repl.mode()
                + "   Primary: " + repl.primary()
                + "   Replicas: " + repl.replicas()
                + "\nStore: " + repl.store());
    }

    @Override public String title()    { return "Replication"; }
    @Override public String subtitle() { return "Primary/replica, sync vs async"; }
}