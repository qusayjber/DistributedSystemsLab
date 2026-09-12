package dsl.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public final class Ui {

    private Ui() { }

    public static Label title(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("label-title");
        return l;
    }

    public static Label subtitle(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("label-subtitle");
        return l;
    }

    public static Label caption(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("label-caption");
        return l;
    }

    public static Label value(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("label-value");
        return l;
    }

    public static Button button(String text, EventHandler<ActionEvent> handler) {
        Button b = new Button(text);
        b.getStyleClass().add("btn");
        if (handler != null) b.setOnAction(handler);
        return b;
    }

    public static Button primary(String text, EventHandler<ActionEvent> handler) {
        Button b = button(text, handler);
        b.getStyleClass().add("btn-primary");
        return b;
    }

    public static Button danger(String text, EventHandler<ActionEvent> handler) {
        Button b = button(text, handler);
        b.getStyleClass().add("btn-danger");
        return b;
    }

    public static Button success(String text, EventHandler<ActionEvent> handler) {
        Button b = button(text, handler);
        b.getStyleClass().add("btn-success");
        return b;
    }

    public static ToggleButton toggle(String text) {
        ToggleButton t = new ToggleButton(text);
        t.getStyleClass().add("btn");
        return t;
    }

    public static VBox card(String title, Node... content) {
        VBox box = new VBox(10);
        box.getStyleClass().add("card");
        if (title != null && !title.isBlank()) {
            Label t = new Label(title);
            t.getStyleClass().add("card-title");
            box.getChildren().add(t);
        }
        box.getChildren().addAll(content);
        return box;
    }

    public static HBox row(Node... children) {
        HBox box = new HBox(8, children);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    public static HBox rowSpaced(Node... children) {
        HBox box = new HBox(8, children);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0));
        return box;
    }

    public static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    public static Label chip(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().addAll("chip", styleClass);
        return l;
    }

    public static TextField textField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("text-field");
        return f;
    }

    public static ComboBox<String> combo(String... items) {
        ComboBox<String> c = new ComboBox<>();
        c.getItems().addAll(items);
        c.getSelectionModel().selectFirst();
        c.getStyleClass().add("combo");
        return c;
    }

    public static Slider slider(double min, double max, double value) {
        Slider s = new Slider(min, max, value);
        s.setShowTickMarks(true);
        s.setShowTickLabels(false);
        return s;
    }

    public static Label keyValue(String key, String value) {
        Label l = new Label(key + ":  " + value);
        l.getStyleClass().add("kv");
        return l;
    }
}