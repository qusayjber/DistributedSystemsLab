module DistributedSystemsLab {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires java.desktop;

    exports dsl.application;
    opens   dsl.ui         to javafx.graphics, javafx.controls;
    opens   dsl.ui.views   to javafx.graphics, javafx.controls;
}