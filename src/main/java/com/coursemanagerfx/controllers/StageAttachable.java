package com.coursemanagerfx.controllers;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public interface StageAttachable {
    BorderPane getRootPane();         // root pane
    Node getTitleBar();               // hbox/borderpane ... some node - title bar
    void setStage(Stage stage);       // for set stage
}
