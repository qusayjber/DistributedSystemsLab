package dsl.visualization;

import dsl.model.Edge;
import dsl.model.Message;
import dsl.model.MessageStatus;
import dsl.model.Node;
import dsl.model.NodeRole;
import dsl.model.NodeState;
import dsl.network.NetworkSimulator;
import dsl.simulation.SimulationEngine;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Real‑time network visualisation. Renders nodes, links and messages travelling
 * along links. Supports dragging nodes and click selection.
 */
public final class NetworkCanvas extends Pane {

    private static final double NODE_RADIUS = 26;

    private final Canvas canvas = new Canvas();
    private final NetworkSimulator network;
    private final SimulationEngine engine;

    private final List<Consumer<Node>> nodeSelectionListeners = new ArrayList<>();
    private final List<Consumer<Message>> messageSelectionListeners = new ArrayList<>();

    private final List<MessageHit> renderedMessages = new ArrayList<>();

    private Node selectedNode;
    private Node draggedNode;
    private double dragOffsetX;
    private double dragOffsetY;

    private final AnimationTimer timer;

    public NetworkCanvas(NetworkSimulator network, SimulationEngine engine) {
        this.network = network;
        this.engine = engine;

        getChildren().add(canvas);
        getStyleClass().add("network-canvas");
        setMinSize(320, 240);

        widthProperty().addListener((o, a, b) -> syncSize());
        heightProperty().addListener((o, a, b) -> syncSize());

        setOnMousePressed(e -> {
            Node n = nodeAt(e.getX(), e.getY());
            if (n != null) {
                draggedNode = n;
                dragOffsetX = n.x() - e.getX();
                dragOffsetY = n.y() - e.getY();
                select(n);
                return;
            }
            Message m = messageAt(e.getX(), e.getY());
            if (m != null) {
                messageSelectionListeners.forEach(l -> l.accept(m));
            } else {
                select(null);
            }
        });

        setOnMouseDragged(e -> {
            if (draggedNode != null) {
                draggedNode.setPosition(e.getX() + dragOffsetX, e.getY() + dragOffsetY);
            }
        });

        setOnMouseReleased(e -> draggedNode = null);

        timer = new AnimationTimer() {
            @Override public void handle(long now) { render(); }
        };
        timer.start();
    }

    public void stop() { timer.stop(); }

    private void syncSize() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
    }

    public void onNodeSelected(Consumer<Node> listener)       { nodeSelectionListeners.add(listener); }
    public void onMessageSelected(Consumer<Message> listener) { messageSelectionListeners.add(listener); }

    public Node selectedNode() { return selectedNode; }

    private void select(Node n) {
        selectedNode = n;
        nodeSelectionListeners.forEach(l -> l.accept(n));
    }

    /* ---------------------------------------------------------------------- */

    private void render() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, w, h);

        drawGrid(g, w, h);
        drawEdges(g);
        drawMessages(g);
        drawNodes(g);
    }

    private void drawGrid(GraphicsContext g, double w, double h) {
        g.setStroke(Color.web("#ffffff", 0.035));
        g.setLineWidth(1);
        for (double x = 0; x < w; x += 40) g.strokeLine(x, 0, x, h);
        for (double y = 0; y < h; y += 40) g.strokeLine(0, y, w, y);
    }

    private void drawEdges(GraphicsContext g) {
        for (Edge e : network.edges()) {
            Node a = network.node(e.a());
            Node b = network.node(e.b());
            if (a == null || b == null) continue;

            boolean cut = network.isPartitioned() && network.groupOf(a.id()) != network.groupOf(b.id());

            g.setStroke(cut ? Color.web("#e74c3c", 0.45) : Color.web("#5b6b8c", 0.55));
            g.setLineWidth(cut ? 1.5 : 2);
            if (cut) g.setLineDashes(8, 8);
            g.strokeLine(a.x(), a.y(), b.x(), b.y());
            g.setLineDashes(null);
        }
    }

    private void drawMessages(GraphicsContext g) {
        renderedMessages.clear();
        long now = engine.currentTime();

        for (Message m : network.inFlight()) {
            Node src = network.node(m.sourceId());
            Node dst = network.node(m.destinationId());
            if (src == null || dst == null) continue;
            if (m.status() != MessageStatus.IN_FLIGHT) continue;

            double span = Math.max(1, m.arrivalTime() - m.departureTime());
            double t = Math.max(0, Math.min(1, (now - m.departureTime()) / span));

            double x = src.x() + (dst.x() - src.x()) * t;
            double y = src.y() + (dst.y() - src.y()) * t;

            Color c = colorFor(m.type());
            g.setFill(new RadialGradient(0, 0, x, y, 14, false, CycleMethod.NO_CYCLE,
                    new Stop(0, c.deriveColor(0, 1, 1, 0.9)),
                    new Stop(1, c.deriveColor(0, 1, 1, 0))));
            g.fillOval(x - 14, y - 14, 28, 28);

            g.setFill(c);
            g.fillOval(x - 4.5, y - 4.5, 9, 9);

            renderedMessages.add(new MessageHit(m, x, y));
        }
    }

    private void drawNodes(GraphicsContext g) {
        g.setTextAlign(TextAlignment.CENTER);
        for (Node n : network.nodes()) {
            Color base = colorFor(n);
            double r = NODE_RADIUS;

            if (n == selectedNode) {
                g.setStroke(Color.web("#6ea8ff", 0.9));
                g.setLineWidth(2);
                g.strokeOval(n.x() - r - 6, n.y() - r - 6, (r + 6) * 2, (r + 6) * 2);
            }

            g.setFill(new RadialGradient(0, 0, n.x(), n.y() - r / 3, r * 1.6, false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, base.deriveColor(0, 1, 1.35, 1)),
                    new Stop(1, base.deriveColor(0, 1, 0.7, 1))));
            g.fillOval(n.x() - r, n.y() - r, r * 2, r * 2);

            g.setStroke(Color.web("#0e1420", 0.9));
            g.setLineWidth(2);
            g.strokeOval(n.x() - r, n.y() - r, r * 2, r * 2);

            g.setFill(Color.WHITE);
            g.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            g.fillText(n.id(), n.x(), n.y() + 5);

            g.setFill(Color.web("#c7d2e5"));
            g.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
            g.fillText(n.getRole().glyph() + " " + n.getRole().label(), n.x(), n.y() + r + 15);

            g.setFill(Color.web("#7d8ba6"));
            g.fillText("L=" + n.getLamportClock().value() + "  ✉" + n.sentCount(), n.x(), n.y() + r + 29);
        }
    }

    private Color colorFor(Node n) {
        if (n.getState() == NodeState.FAILED)     return Color.web("#e74c3c");
        if (n.getState() == NodeState.OFFLINE)    return Color.web("#586179");
        if (n.getState() == NodeState.RECOVERING) return Color.web("#f0a500");

        return switch (n.getRole()) {
            case LEADER      -> Color.web("#f5b301");
            case CANDIDATE   -> Color.web("#ff8a3d");
            case FOLLOWER    -> Color.web("#3d7eff");
            case COORDINATOR -> Color.web("#9b5de5");
            case PRIMARY     -> Color.web("#2ecc71");
            default          -> Color.web("#2ecc71");
        };
    }

    private Color colorFor(dsl.model.MessageType type) {
        return switch (type) {
            case HEARTBEAT      -> Color.web("#8fa3c8");
            case VOTE_REQUEST   -> Color.web("#ff8a3d");
            case VOTE_RESPONSE  -> Color.web("#f5b301");
            case APPEND_ENTRIES -> Color.web("#3d7eff");
            case COMMIT         -> Color.web("#2ecc71");
            case ABORT          -> Color.web("#e74c3c");
            case PREPARE        -> Color.web("#9b5de5");
            default             -> Color.web("#6ea8ff");
        };
    }

    /* ---------------------------------------------------------------------- */

    public Node nodeAt(double x, double y) {
        for (Node n : network.nodes()) {
            double dx = n.x() - x;
            double dy = n.y() - y;
            if (dx * dx + dy * dy <= NODE_RADIUS * NODE_RADIUS * 1.4) return n;
        }
        return null;
    }

    public Message messageAt(double x, double y) {
        for (MessageHit hit : renderedMessages) {
            if (Math.hypot(hit.x - x, hit.y - y) < 12) return hit.message;
        }
        return null;
    }

    /** Arranges all nodes on a circle, useful when a lab creates a topology. */
    public void layoutInCircle(double padding) {
        List<Node> list = network.nodeList();
        if (list.isEmpty()) return;
        double w = Math.max(getWidth(), 600);
        double h = Math.max(getHeight(), 420);
        double cx = w / 2;
        double cy = h / 2;
        double radius = Math.min(w, h) / 2 - padding;

        for (int i = 0; i < list.size(); i++) {
            double angle = -Math.PI / 2 + (2 * Math.PI * i) / list.size();
            list.get(i).setPosition(cx + radius * Math.cos(angle), cy + radius * Math.sin(angle));
        }
    }

    public void layoutFullMesh() {
        List<Node> list = network.nodeList();
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                network.connect(list.get(i).id(), list.get(j).id());
            }
        }
    }

    public void layoutRing() {
        List<Node> list = network.nodeList();
        for (int i = 0; i < list.size(); i++) {
            network.connect(list.get(i).id(), list.get((i + 1) % list.size()).id());
        }
    }

    private record MessageHit(Message message, double x, double y) { }
}