package dsl.ui;

import javafx.scene.Node;

public interface LabView {

    String title();

    String subtitle();

    Node content();

    default void onActivated()   { }

    default void onDeactivated() { }
}