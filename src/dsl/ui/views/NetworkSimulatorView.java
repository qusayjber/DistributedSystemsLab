package dsl.ui.views;

import dsl.application.AppContext;
import dsl.model.MessageType;
import dsl.model.Node;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public final class NetworkSimulatorView extends BaseView {

    private int nextId = 1;

    public NetworkSimulatorView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        buildUi();
    }

    private void buildUi() {
        installCanvas();

        HBox bar = createControlBar();

        Button addNode   = Ui.success("+ Node",     e -> addNode());
        Button remove    = Ui.button("Remove",      e -> removeSelected());
        Button connect   = Ui.button("Connect",     e -> connectSelected());
        Button broadcast = Ui.button("Broadcast",   e -> broadcastFromSelected());
        Button fail      = Ui.danger("Fail",        e -> failSelected());
        Button recover   = Ui.success("Recover",    e -> recoverSelected());
        Button partition = Ui.button("Partition",   e -> createPartition());
        Button heal      = Ui.button("Heal",        e -> { network.healPartition(); });

        bar.getChildren().addAll(addNode, remove, connect, broadcast, fail, recover, partition, heal);
        setTop(bar);

        Platform.runLater(() -> {
            for (int i = 0; i < 5; i++) addNode();
            canvas.layoutInCircle(90);
            canvas.layoutFullMesh();
        });
    }

    private void addNode() {
        String id = "N" + nextId++;
        network.addNode(id, "10.0.0." + nextId);
    }

    private void removeSelected() {
        Node n = canvas.selectedNode();
        if (n == null) return;
        network.removeNode(n.id());
    }

    private void connectSelected() {
        List<Node> list = network.nodeList();
        if (list.size() < 2) return;
        // connect first two unconnected, otherwise first two
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (!network.areConnected(list.get(i).id(), list.get(j).id())) {
                    network.connect(list.get(i).id(), list.get(j).id());
                    return;
                }
            }
        }
    }

    private void broadcastFromSelected() {
        Node n = canvas.selectedNode();
        if (n == null) return;
        network.broadcast(n.id(), MessageType.REQUEST, "broadcast " + System.currentTimeMillis() % 1000);
    }

    private void failSelected() {
        Node n = canvas.selectedNode();
        if (n == null) return;
        n.fail();
        log.warn("NETWORK", n.id() + " failed");
    }

    private void recoverSelected() {
        Node n = canvas.selectedNode();
        if (n == null) return;
        n.recover();
        log.info("NETWORK", n.id() + " recovered");
    }

    private void createPartition() {
        List<Node> nodes = network.nodeList();
        if (nodes.size() < 4) return;
        int half = nodes.size() / 2;
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            if (i < half) a.add(nodes.get(i).id());
            else          b.add(nodes.get(i).id());
        }
        network.partition(a, b);
    }

    @Override public String title()    { return "Network Simulator"; }
    @Override public String subtitle() { return "Add nodes, connect, send messages, simulate failures"; }
}