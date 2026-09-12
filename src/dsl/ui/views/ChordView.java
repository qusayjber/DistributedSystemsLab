package dsl.ui.views;

import dsl.algorithms.chord.ChordRing;
import dsl.application.AppContext;
import dsl.ui.Navigator;
import dsl.ui.Ui;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class ChordView extends BaseView {

    private final ChordRing chord;
    private final Label info = new Label();
    private final TextField keyField = Ui.textField("key (e.g. hello)");

    public ChordView(AppContext ctx, Navigator nav) {
        super(ctx, nav);
        this.chord = new ChordRing(log);
        buildUi();
    }

    private void buildUi() {
        installCanvas();

        HBox bar = createControlBar();

        Button join = Ui.success("+ Random Node", e -> {
            int id = (int) (Math.random() * ChordRing.SPACE);
            chord.join(id);
            String nid = "C" + id;
            network.addNode(nid, "10.2." + (id % 250) + ".1");
            canvas.layoutInCircle(100);
            canvas.layoutRing();
        });

        Button leave = Ui.danger("Leave Selected", e -> {
            var n = canvas.selectedNode();
            if (n == null) return;
            try {
                int id = Integer.parseInt(n.id().substring(1));
                chord.leave(id);
            } catch (NumberFormatException ignored) { }
            network.removeNode(n.id());
        });

        Button lookup = Ui.primary("Lookup", e -> {
            int key = ChordRing.hash(keyField.getText().isEmpty() ? "key" : keyField.getText());
            var path = chord.lookupPath(key);
            log.algorithm("CHORD", "lookup(" + key + ") → path " + path);
            animate(path, key);
        });

        bar.getChildren().addAll(join, leave, keyField, lookup);
        setTop(bar);

        VBox bottom = new VBox(info);
        setBottom(bottom);

        Platform.runLater(() -> {
            for (int i = 0; i < 5; i++) {
                int id = (int) (Math.random() * ChordRing.SPACE);
                chord.join(id);
                network.addNode("C" + id, "10.2." + (id % 250) + ".1");
            }
            canvas.layoutInCircle(100);
            canvas.layoutRing();
            refresh();
        });
    }

    private void animate(java.util.List<Integer> path, int key) {
        long delay = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            String a = "C" + path.get(i);
            String b = "C" + path.get(i + 1);
            delay += 350;
            engine.schedule(delay, "hop", ev ->
                    network.send(a, b, dsl.model.MessageType.LOOKUP, "key=" + key));
        }
        engine.schedule(delay + 400, "done", ev -> {
            int owner = chord.findSuccessor(key).id;
            log.algorithm("CHORD", "key " + key + " stored at C" + owner);
            refresh();
        });
    }

    private void refresh() {
        StringBuilder sb = new StringBuilder("Ring size: " + chord.nodes().size());
        for (var n : chord.nodes()) {
            sb.append("\n  C").append(n.id)
              .append("  succ=C").append(n.successor)
              .append("  pred=C").append(n.predecessor)
              .append("  finger[0]=").append(n.finger[0])
              .append("  finger[3]=").append(n.finger[3]);
        }
        info.setText(sb.toString());
    }

    @Override public String title()    { return "Chord DHT"; }
    @Override public String subtitle() { return "Consistent hashing ring, successor lookup, finger tables"; }
}