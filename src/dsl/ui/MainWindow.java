package dsl.ui;

import dsl.application.AppContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MainWindow extends BorderPane implements Navigator {

    private final AppContext ctx;
    private final ViewManager viewManager;

    private final StackPane contentArea = new StackPane();
    private final Label headerTitle = new Label();
    private final Label headerSubtitle = new Label();
    private final Label clockLabel = new Label("t = 0 ms");

    private final Map<String, ToggleButton> navButtons = new LinkedHashMap<>();
    private final ToggleGroup navGroup = new ToggleGroup();

    private final EventLogPanel eventLogPanel;
    private String currentKey = "dashboard";

    public MainWindow(AppContext ctx) {
        this.ctx = ctx;
        this.viewManager = new ViewManager(ctx, this);
        this.eventLogPanel = new EventLogPanel(ctx.eventLog());

        getStyleClass().add("main-window");
        setLeft(buildSidebar());
        setCenter(buildCenter());
        setRight(buildRightDrawer());
        setBottom(buildStatusBar());

        navigate("dashboard");
    }

    /* ------------------------------- sidebar -------------------------------- */

    private Node buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(232);

        Label logo = new Label("DSL");
        logo.getStyleClass().add("logo-mark");
        Label name = new Label("Distributed Systems\nLaboratory");
        name.getStyleClass().add("logo-text");
        VBox brand = new VBox(2, logo, name);
        brand.getStyleClass().add("brand");
        brand.setPadding(new Insets(18, 16, 18, 16));

        sidebar.getChildren().add(brand);

        String[][] items = {
                {"dashboard",     "Dashboard"},
                {"network",       "Network Simulator"},
                {"election",      "Leader Election"},
                {"raft",          "Raft Consensus"},
                {"lamport",       "Lamport Clock"},
                {"vector",        "Vector Clock"},
                {"chord",         "Chord DHT"},
                {"replication",   "Replication"},
                {"consistency",   "Consistency"},
                {"2pc",           "Two-Phase Commit"},
                {"transactions",  "Distributed Transactions"},
                {"faults",        "Fault Tolerance"},
                {"cap",           "CAP Theorem"},
                {"loadbalancing", "Load Balancing"},
                {"p2p",           "P2P Network"},
                {"dfs",           "Distributed File System"},
                {"log",           "Event Log"},
                {"metrics",       "Metrics"},
                {"settings",      "Settings"}
        };

        for (String[] item : items) {
            ToggleButton b = new ToggleButton(item[1]);
            b.getStyleClass().add("nav-button");
            b.setToggleGroup(navGroup);
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.CENTER_LEFT);
            b.setOnAction(e -> navigate(item[0]));
            navButtons.put(item[0], b);
            sidebar.getChildren().add(b);
        }

        ScrollPane scroll = new ScrollPane(sidebar);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("sidebar-scroll");
        return scroll;
    }

    /* -------------------------------- center -------------------------------- */

    private Node buildCenter() {
        BorderPane center = new BorderPane();
        center.setTop(buildHeader());
        contentArea.getStyleClass().add("content-area");
        center.setCenter(contentArea);
        return center;
    }

    private Node buildHeader() {
        headerTitle.getStyleClass().add("header-title");
        headerSubtitle.getStyleClass().add("header-subtitle");
        VBox titles = new VBox(2, headerTitle, headerSubtitle);

        clockLabel.getStyleClass().add("clock-label");

        Button theme = Ui.button("◐ Theme", e -> ctx.themeManager().toggle());

        HBox header = new HBox(12, titles, Ui.spacer(), clockLabel, theme);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");
        header.setPadding(new Insets(14, 18, 14, 18));

        Timeline tl = new Timeline(new KeyFrame(Duration.millis(200), e -> { }));
        tl.setCycleCount(Timeline.INDEFINITE);
        tl.play();

        return header;
    }

    private Node buildRightDrawer() {
        VBox drawer = new VBox(eventLogPanel);
        drawer.getStyleClass().add("right-drawer");
        drawer.setPrefWidth(360);
        VBox.setVgrow(eventLogPanel, Priority.ALWAYS);
        return drawer;
    }

    private Node buildStatusBar() {
        Label status = new Label("Ready");
        status.getStyleClass().add("status-text");
        HBox bar = new HBox(12, status, Ui.spacer(),
                new Label("JavaFX • Discrete‑event simulation"));
        bar.getStyleClass().add("status-bar");
        bar.setPadding(new Insets(6, 14, 6, 14));
        bar.setAlignment(Pos.CENTER_LEFT);

        ctx.eventLog().onEntry(e -> javafx.application.Platform.runLater(
                () -> status.setText(e.category() + " • " + e.message())));

        return bar;
    }

    /* ------------------------------ navigation ------------------------------ */

    @Override
    public void navigate(String key) {
        LabView previous = null;
        try {
            previous = viewManager.get(currentKey);
        } catch (RuntimeException ignored) { }

        if (previous != null && !key.equals(currentKey)) previous.onDeactivated();

        LabView view = viewManager.get(key);
        currentKey = key;

        contentArea.getChildren().setAll(view.content());
        headerTitle.setText(view.title());
        headerSubtitle.setText(view.subtitle());

        ToggleButton b = navButtons.get(key);
        if (b != null) b.setSelected(true);

        view.onActivated();
    }

    /** Called by labs so they can publish their simulation clock in the header. */
    public void bindClock(dsl.simulation.SimulationEngine engine) {
        engine.onTime(t -> javafx.application.Platform.runLater(
                () -> clockLabel.setText("t = " + t + " ms")));
    }
}