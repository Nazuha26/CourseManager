package com.coursemanagerfx.logic.deprecated;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.awt.*;

public class GetPoint {
    public static void setupMousePositionLogger(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.P) {
                Point2D mousePos = scene.getWindow().getScene().getRoot().screenToLocal(MouseInfo.getPointerInfo().getLocation().getX(),
                        MouseInfo.getPointerInfo().getLocation().getY());
                System.out.printf("%.0f , %.0f%n", mousePos.getX(), mousePos.getY());
            }
        });
    }

}
