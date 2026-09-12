package dsl.ui;

import dsl.application.AppContext;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Lazy, reflective view loader. Views are instantiated on first access.
 * If a view class is not yet present the lab degrades gracefully instead of
 * preventing the application from starting.
 */
public final class ViewManager {

    private static final Map<String, String> CLASSES = new HashMap<>();
    static {
        CLASSES.put("dashboard",     "dsl.ui.views.DashboardView");
        CLASSES.put("network",       "dsl.ui.views.NetworkSimulatorView");
        CLASSES.put("election",      "dsl.ui.views.LeaderElectionView");
        CLASSES.put("raft",          "dsl.ui.views.RaftView");
        CLASSES.put("lamport",       "dsl.ui.views.LamportClockView");
        CLASSES.put("vector",        "dsl.ui.views.VectorClockView");
        CLASSES.put("chord",         "dsl.ui.views.ChordView");
        CLASSES.put("replication",   "dsl.ui.views.ReplicationView");
        CLASSES.put("consistency",   "dsl.ui.views.ConsistencyView");
        CLASSES.put("2pc",           "dsl.ui.views.TwoPhaseCommitView");
        CLASSES.put("transactions",  "dsl.ui.views.DistributedTransactionsView");
        CLASSES.put("faults",        "dsl.ui.views.FaultToleranceView");
        CLASSES.put("cap",           "dsl.ui.views.CapTheoremView");
        CLASSES.put("loadbalancing", "dsl.ui.views.LoadBalancingView");
        CLASSES.put("p2p",           "dsl.ui.views.P2PNetworkView");
        CLASSES.put("dfs",           "dsl.ui.views.DistributedFileSystemView");
        CLASSES.put("log",           "dsl.ui.views.EventLogView");
        CLASSES.put("metrics",       "dsl.ui.views.MetricsView");
        CLASSES.put("settings",      "dsl.ui.views.SettingsView");
    }

    private final Map<String, LabView> cache = new HashMap<>();
    private final AppContext ctx;
    private final Navigator navigator;

    public ViewManager(AppContext ctx, Navigator navigator) {
        this.ctx = ctx;
        this.navigator = navigator;
    }

    public LabView get(String key) {
        return cache.computeIfAbsent(key, this::instantiate);
    }

    private LabView instantiate(String key) {
        String className = CLASSES.get(key);
        if (className == null) return new UnavailableView(key, "Unknown lab key: " + key);

        try {
            Class<?> cls = Class.forName(className);
            Constructor<?> ctor = cls.getConstructor(AppContext.class, Navigator.class);
            return (LabView) ctor.newInstance(ctx, navigator);
        } catch (ClassNotFoundException e) {
            return new UnavailableView(key, "This lab view is not installed yet.");
        } catch (NoSuchMethodException e) {
            return new UnavailableView(key, "Missing (AppContext, Navigator) constructor.");
        } catch (Throwable t) {
            return new UnavailableView(key,
                    "Failed to instantiate: " + t.getClass().getSimpleName()
                            + (t.getMessage() == null ? "" : " — " + t.getMessage()));
        }
    }

    private static final class UnavailableView implements LabView {
        private final String key;
        private final String reason;
        private final VBox root;

        UnavailableView(String key, String reason) {
            this.key = key;
            this.reason = reason;

            Label title = new Label(pretty(key));
            title.getStyleClass().add("label-title");
            Label msg = new Label(reason);
            msg.getStyleClass().add("label-subtitle");

            root = new VBox(8, title, msg);
            root.setPadding(new Insets(32));
        }

        @Override public String title()    { return pretty(key); }
        @Override public String subtitle() { return "Unavailable"; }
        @Override public Node content()    { return root; }

        private static String pretty(String k) {
            if (k == null || k.isEmpty()) return "Lab";
            return Character.toUpperCase(k.charAt(0)) + k.substring(1);
        }
    }
}