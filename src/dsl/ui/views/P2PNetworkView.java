package dsl.ui.views;

import dsl.application.AppContext;
import dsl.model.MessageType;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public final class P2PNetworkView extends BaseView {

    private int nextId = 1;

    public P2PNetworkView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        buildUi();
    }

    private void buildUi() {
        installCanvas();
        HBox bar = createControlBar();

        Button add = Ui.success("+ Peer", e -> {
            String id = "P" + nextId++;
            network.addNode(id, "p2p-" + id);
            log.info("P2P", "Peer " + id + " joined");
            connectRandom(id);
        });
        Button leave = Ui.danger("Leave Selected", e -> {
            var n = canvas.selectedNode();
            if (n != null) {
                network.removeNode(n.id());
                log.warn("P2P", "Peer " + n.id() + " left");
            }
        });
        Button discovery = Ui.primary("Discovery", e -> {
            var n = canvas.selectedNode();
            if (n == null) return;
            network.broadcast(n.id(), MessageType.DISCOVERY, "ping from " + n.id());
            log.algorithm("P2P", n.id() + " broadcast DISCOVERY");
        });
        Button direct = Ui.button("Send Direct", e -> {
            var n = canvas.selectedNode();
            if (n == null) return;
            for (String peer : n.neighbors()) {
                network.send(n.id(), peer, MessageType.PING, "direct");
                return;
            }
        });
        Button fail = Ui.danger("Fail Selected", e -> {
            var n = canvas.selectedNode();
            if (n != null) { n.fail(); log.warn("P2P", n.id() + " failed"); }
        });
        Button recover = Ui.success("Recover Selected", e -> {
            var n = canvas.selectedNode();
            if (n != null) { n.recover(); log.info("P2P", n.id() + " recovered"); }
        });

        bar.getChildren().addAll(add, leave, discovery, direct, fail, recover);
        setTop(bar);

        Platform.runLater(() -> {
            for (int i = 0; i < 6; i++) {
                String id = "P" + nextId++;
                network.addNode(id, "p2p-" + id);
            }
            canvas.layoutInCircle(100);
            // sparse mesh: 2 random links per node
            for (var n : network.nodeList()) {
                for (String peer : n.neighbors()) {
                    if (Math.random() < 0.7) network.connect(n.id(), peer);
                }
            }
        });
    }

    private void connectRandom(String id) {
        var list = network.nodeList();
        if (list.size() < 2) return;
        var peer = list.get((int) (Math.random() * list.size()));
        if (!peer.id().equals(id)) network.connect(id, peer.id());
    }

    @Override public String title()    { return "P2P Network"; }
    @Override public String subtitle() { return "Peer discovery, broadcast, direct messaging"; }
}