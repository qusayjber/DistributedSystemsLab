package dsl.ui;

import dsl.model.Message;
import dsl.model.Node;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Map;

public final class InspectorPane extends VBox {

    private final VBox body = new VBox(4);

    public InspectorPane() {
        getStyleClass().add("card");
        setPadding(new Insets(12));
        setSpacing(8);
        Label t = new Label("Inspector");
        t.getStyleClass().add("card-title");
        getChildren().addAll(t, body);
        showEmpty();
    }

    public void showEmpty() {
        body.getChildren().setAll(Ui.caption("Select a node or a message on the canvas."));
    }

    public void showNode(Node n) {
        body.getChildren().setAll(
                Ui.keyValue("Node ID", n.id()),
                Ui.keyValue("Address", n.address()),
                Ui.keyValue("State", n.getState().label()),
                Ui.keyValue("Role", n.getRole().label()),
                Ui.keyValue("Lamport", String.valueOf(n.getLamportClock().value())),
                Ui.keyValue("Vector", vectorString(n.getVectorClock().snapshot())),
                Ui.keyValue("Sent", String.valueOf(n.sentCount())),
                Ui.keyValue("Received", String.valueOf(n.receivedCount())),
                Ui.keyValue("Uptime", dsl.utils.TimeFormat.duration(n.uptimeMillis())),
                Ui.keyValue("Restarts", String.valueOf(n.restartCount())),
                Ui.keyValue("Neighbors", String.join(", ", n.neighbors()))
        );
    }

    public void showMessage(Message m) {
        body.getChildren().setAll(
                Ui.keyValue("Message ID", m.id()),
                Ui.keyValue("Source", m.sourceId()),
                Ui.keyValue("Destination", m.destinationId()),
                Ui.keyValue("Type", m.type().name()),
                Ui.keyValue("Payload", m.payload().isBlank() ? "—" : m.payload()),
                Ui.keyValue("Logical TS", String.valueOf(m.logicalTimestamp())),
                Ui.keyValue("Vector", vectorString(m.vector())),
                Ui.keyValue("Latency", m.latency() + " ms"),
                Ui.keyValue("Status", m.status().name()),
                Ui.keyValue("Duplicate", String.valueOf(m.duplicate()))
        );
    }

    private static String vectorString(Map<String, Integer> v) {
        if (v == null || v.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        v.forEach((k, val) -> sb.append(k).append('=').append(val).append(' '));
        return sb.toString().trim() + "]";
    }
}