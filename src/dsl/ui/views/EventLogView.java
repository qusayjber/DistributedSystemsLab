package dsl.ui.views;

import dsl.application.AppContext;
import dsl.ui.EventLogPanel;
import dsl.ui.LabView;
import dsl.ui.Navigator;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

public final class EventLogView extends BorderPane implements LabView {

    public EventLogView(AppContext ctx, Navigator nav) {
        EventLogPanel panel = new EventLogPanel(ctx.eventLog());
        panel.setPadding(new Insets(12));
        setCenter(panel);
    }

    @Override public String title()    { return "Event Log"; }
    @Override public String subtitle() { return "Filter, search and export the full event stream"; }
    @Override public Node content()    { return this; }
}