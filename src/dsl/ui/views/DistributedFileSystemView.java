package dsl.ui.views;

import dsl.application.AppContext;
import dsl.model.MessageType;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simplified distributed file system. Files are chunked; each chunk is
 * replicated on 2 nodes; killing a node forces the master to re-replicate
 * the affected chunks onto a surviving node.
 */
public final class DistributedFileSystemView extends BaseView {

    private static final int REPLICATION = 2;
    private final Map<String, List<List<String>>> files = new LinkedHashMap<>(); // file → chunks → nodeIds
    private final Label info = new Label();

    public DistributedFileSystemView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        buildUi();
    }

    private void buildUi() {
        installCanvas();
        HBox bar = createControlBar();

        Button create = Ui.primary("+ File", e -> {
            String name = "file-" + (files.size() + 1) + ".txt";
            int chunks = ThreadLocalRandom.current().nextInt(2, 5);
            List<List<String>> placement = new ArrayList<>();
            var nodes = network.nodeList();
            for (int c = 0; c < chunks; c++) {
                List<String> targets = new ArrayList<>();
                while (targets.size() < REPLICATION) {
                    var n = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
                    if (!targets.contains(n.id())) targets.add(n.id());
                }
                placement.add(targets);
            }
            files.put(name, placement);
            log.algorithm("DFS", "Created " + name + " with " + chunks + " chunks");
            refresh();
        });

        Button fail = Ui.danger("Fail Selected", e -> {
            var n = canvas.selectedNode();
            if (n == null) return;
            n.fail();
            log.warn("DFS", n.id() + " failed — re-replicating");
            rereplicate(n.id());
            refresh();
        });

        Button repair = Ui.success("Repair All", e -> {
            for (String id : files.keySet()) {
                log.algorithm("DFS", "Verifying " + id);
            }
            refresh();
        });

        bar.getChildren().addAll(create, fail, repair);
        setTop(bar);

        setBottom(new VBox(info));

        Platform.runLater(() -> {
            for (int i = 1; i <= 5; i++) network.addNode("D" + i, "10.9.0." + i);
            canvas.layoutInCircle(100);
            canvas.layoutFullMesh();
        });
    }

    private void rereplicate(String lostNode) {
        for (var entry : files.entrySet()) {
            for (var chunk : entry.getValue()) {
                if (!chunk.contains(lostNode)) continue;
                chunk.remove(lostNode);
                // pick a fresh node
                for (var n : network.nodeList()) {
                    if (n.isOperational() && !chunk.contains(n.id())) {
                        chunk.add(n.id());
                        log.algorithm("DFS", "Re-replicated chunk of " + entry.getKey() + " onto " + n.id());
                        network.send("master", n.id(), MessageType.REPLICATION,
                                "chunk of " + entry.getKey());
                        break;
                    }
                }
            }
        }
    }

    private void refresh() {
        StringBuilder sb = new StringBuilder("Files:\n");
        files.forEach((name, chunks) -> {
            sb.append("  ").append(name).append(":\n");
            for (int i = 0; i < chunks.size(); i++) {
                sb.append("    chunk ").append(i + 1).append(" → ")
                  .append(String.join(", ", chunks.get(i))).append('\n');
            }
        });
        info.setText(sb.toString());
    }

    @Override public String title()    { return "Distributed File System"; }
    @Override public String subtitle() { return "Chunking, placement, failure & recovery"; }
}