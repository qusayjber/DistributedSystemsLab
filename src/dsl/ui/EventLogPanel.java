package dsl.ui;

import dsl.services.EventLogService;
import dsl.services.ExportService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class EventLogPanel extends BorderPane {

    private final EventLogService log;
    private final ObservableList<EventLogService.Entry> entries = FXCollections.observableArrayList();
    private final FilteredList<EventLogService.Entry> filtered = new FilteredList<>(entries, e -> true);

    public EventLogPanel(EventLogService log) {
        this.log = log;
        getStyleClass().add("event-log-panel");

        ListView<EventLogService.Entry> list = new ListView<>(filtered);
        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(EventLogService.Entry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(item.formatted());
                getStyleClass().removeAll("log-debug", "log-info", "log-warn",
                        "log-error", "log-algorithm");
                getStyleClass().add("log-" + item.level().name().toLowerCase());
            }
        });
        list.setFocusTraversable(false);

        TextField search = Ui.textField("Search events…");
        search.textProperty().addListener((o, a, b) -> applyFilter(b, null));

        ComboBox<String> level = Ui.combo("ALL", "DEBUG", "INFO", "WARN", "ERROR", "ALGORITHM");
        level.valueProperty().addListener((o, a, b) -> applyFilter(search.getText(), b));

        Button clear = Ui.button("Clear", e -> entries.clear());
        Button export = Ui.button("Export", e -> export());

        HBox top = new HBox(6, search, level, clear, export);
        top.setPadding(new Insets(8));
        HBox.setHgrow(search, Priority.ALWAYS);

        setTop(top);
        setCenter(list);

        log.onEntry(entry -> Platform.runLater(() -> {
            entries.add(entry);
            if (entries.size() > 5_000) entries.remove(0);
        }));
        entries.addAll(log.entries());
    }

    private void applyFilter(String text, String level) {
        String needle = text == null ? "" : text.toLowerCase();
        String lvl = level;
        filtered.setPredicate(e -> {
            if (lvl != null && !"ALL".equals(lvl) && !e.level().name().equals(lvl)) return false;
            if (needle.isBlank()) return true;
            return e.formatted().toLowerCase().contains(needle);
        });
    }

    private void export() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export event log");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        chooser.setInitialFileName("dsl-event-log.csv");
        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file == null) return;
        try {
            ExportService.exportEventLog(file.toPath(), List.copyOf(entries));
        } catch (IOException ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage());
            a.showAndWait();
        }
    }
}