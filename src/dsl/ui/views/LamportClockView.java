package dsl.ui.views;

import dsl.application.AppContext;
import dsl.model.MessageType;
import dsl.model.Node;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public final class LamportClockView extends BaseView {

    public LamportClockView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        buildUi();
    }

    private void buildUi() {
        installCanvas();

        HBox bar = createControlBar();

        Button localEvent = Ui.primary("Local Event", e -> {
            Node n = canvas.selectedNode();
            if (n == null) return;
            long v = n.getLamportClock().tick();
            log.algorithm("LAMPORT", n.id() + " local event → L=" + v);
        });

        Button sendPing = Ui.button("Send Tick", e -> {
            Node from = canvas.selectedNode();
            if (from == null) return;
            for (String to : from.neighbors()) {
                send(from.id(), to, MessageType.PING, "tick=" + from.getLamportClock().send());
                return;
            }
        });

        Button burst = Ui.button("Burst ×5", e -> {
            Node from = canvas.selectedNode();
            if (from == null) return;
            for (int i = 0; i < 5; i++) {
                for (String to : from.neighbors()) {
                    send(from.id(), to, MessageType.PING, "burst-" + i);
                }
            }
        });

        bar.getChildren().addAll(localEvent, sendPing, burst);
        setTop(bar);

        Platform.runLater(() -> {
            for (int i = 1; i <= 4; i++) network.addNode("L" + i, "10.1.0." + i);
            canvas.layoutInCircle(90);
            canvas.layoutFullMesh();
            network.onDelivery(m ->
                    log.algorithm("LAMPORT", m.sourceId() + "→" + m.destinationId()
                            + " ts=" + m.logicalTimestamp()));
        });
    }

    @Override public String title()    { return "Lamport Logical Clock"; }
    @Override public String subtitle() { return "Increment on events, take max+1 on receive"; }
}