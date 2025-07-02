/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.deprecated;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Универсальный класс для создания онбординга (интерактивного туториала) в JavaFX.
 */
public class OnboardingOverlayUtility {
    private static final Color BG_COLOR = new Color(0,0,0,0.5);

    private final Stage stage;
    private final StackPane root;
    private final Parent oldRoot;
    private final Pane overlayPane;
    private final List<Instruction> instructions = new ArrayList<>();
    private int currentIndex = -1;

    /**
     * Позиция текста относительно выреза.
     */
    public enum Position {TOP, BOTTOM, LEFT, RIGHT }

    /**
     * Инструкция для показа.
     */
    private static class Instruction {
        final Node targetNode;
        final double x1, y1, x2, y2;
        final String text;
        final Position position;

        // Вариант с Node
        Instruction(Node node, String text, Position pos) {
            this.targetNode = node;
            this.text = text;
            this.position = pos;
            this.x1 = this.y1 = this.x2 = this.y2 = Double.NaN;
        }

        // Вариант с координатами
        Instruction(double x1, double y1, double x2, double y2, String text, Position pos) {
            this.targetNode = null;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.text = text;
            this.position = pos;
        }
    }

    /**
     * Конструктор: оборачивает текущее окно в StackPane и готовит слой для онбординга.
     * @param stage целевое окно
     */
    private final Runnable onFinish;

    public OnboardingOverlayUtility(Stage stage, Runnable onFinish) {
        this.stage = stage;
        this.onFinish = onFinish;

        Scene scene = stage.getScene();
        oldRoot = scene.getRoot();
        this.root = new StackPane(oldRoot);

        if (oldRoot instanceof Region region) {
            region.setMouseTransparent(true);
            region.prefWidthProperty().bind(scene.widthProperty());
            region.prefHeightProperty().bind(scene.heightProperty());
        }

        scene.setRoot(this.root);

        overlayPane = new Pane();
        overlayPane.setMouseTransparent(true);
        overlayPane.prefWidthProperty().bind(root.widthProperty());
        overlayPane.prefHeightProperty().bind(root.heightProperty());

        root.getChildren().add(overlayPane);

        scene.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKey);
        Platform.runLater(() -> {
            overlayPane.requestFocus();
            root.requestFocus();
            showNext();
        });
    }

    /**
     * Добавить инструкцию по Node.
     * @param node целевой элемент
     * @param description текст описания
     * @param pos позиция текста
     */
    public void addInstruction(Node node, String description, Position pos) {
        instructions.add(new Instruction(node, description, pos));
    }

    /**
     * Добавить инструкцию по координатам в окне.
     * @param x1 левый верхний X
     * @param y1 левый верхний Y
     * @param x2 правый нижний X
     * @param y2 правый нижний Y
     * @param description текст описания
     * @param pos позиция текста
     */
    public void addInstruction(double x1, double y1, double x2, double y2,
                               String description, Position pos) {
        instructions.add(new Instruction(x1, y1, x2, y2, description, pos));
    }

    /**
     * Показывает следующую инструкцию при нажатии пробела.
     */
    private void handleKey(KeyEvent event) {
        if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.UP) {
            showNext();
        } else if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.DOWN) {
            showPrevious();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            tutorialEnd();
        }
    }

    public void showPrevious() {
        if (currentIndex > 0) {
            currentIndex -= 2; // вернуться на предыдущий
            showNext();
        }
    }

    public void tutorialEnd() {
        root.getChildren().remove(overlayPane);
        oldRoot.setMouseTransparent(false);
        stage.getScene().removeEventHandler(KeyEvent.KEY_PRESSED, this::handleKey);
        if (onFinish != null) {
            onFinish.run();
        }
    }

    private Label showBottomText() {
        Label bottomLabel = new Label("Press the arrows (right – forward, left – back) to go through the quick tutorial");
        bottomLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-text-alignment: center");
        bottomLabel.setWrapText(true);
        bottomLabel.setAlignment(Pos.CENTER);
        return bottomLabel;
    }
    private void showIntroAndOutro(String mainText) {
        BorderPane lblPane = new BorderPane();
        lblPane.prefWidthProperty().bind(overlayPane.widthProperty());
        lblPane.prefHeightProperty().bind(overlayPane.heightProperty());

        Label introLabel = new Label(mainText);
        introLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white; -fx-text-alignment: center");
        introLabel.setWrapText(true);
        introLabel.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(introLabel, Pos.CENTER);
        lblPane.setCenter(introLabel);

        Label btmLabel = showBottomText();
        BorderPane.setAlignment(btmLabel, Pos.CENTER);
        lblPane.setBottom(btmLabel);

        overlayPane.getChildren().add(lblPane);
    }


    /**
     * Отображает следующий шаг онбординга.
     */
    public void showNext() {
        currentIndex++;
        overlayPane.getChildren().clear();

        // === КОНЕЦ ТУТОРИАЛА ===
        if (currentIndex >= instructions.size() + 2) {
            tutorialEnd();
            return;
        }
        // =======================

        // === Стартовый экран ===
        if (currentIndex == 0) {
            Rectangle full = new Rectangle(
                    stage.getScene().getWidth(),
                    stage.getScene().getHeight(),
                    BG_COLOR
            );
            overlayPane.getChildren().add(full);
            showIntroAndOutro("Welcome to CourseManagerFX!\nLet's take a quick tour");
            return;
        }

        // === Финальный экран ===
        if (currentIndex == instructions.size() + 1) {

            Rectangle full = new Rectangle(
                    stage.getScene().getWidth(),
                    stage.getScene().getHeight(),
                    BG_COLOR
            );
            overlayPane.getChildren().add(full);
            showIntroAndOutro("That's it!\nYou're ready to start working with CourseManagerFX");
            return;
        }

        // === Показываем обычную инструкцию ===
        Instruction inst = instructions.get(currentIndex - 1);
        Bounds bounds;
        if (inst.targetNode != null) {
            bounds = inst.targetNode.localToScene(inst.targetNode.getBoundsInLocal());
        } else {
            bounds = new javafx.geometry.BoundingBox(
                    inst.x1, inst.y1,
                    inst.x2 - inst.x1, inst.y2 - inst.y1
            );
        }

        // Полупрозрачный фон
        Rectangle full = new Rectangle(
                stage.getScene().getWidth(),
                stage.getScene().getHeight()
        );

        Rectangle hole = new Rectangle(
                bounds.getMinX(), bounds.getMinY(),
                bounds.getWidth(), bounds.getHeight()
        );
        hole.setArcWidth(20);
        hole.setArcHeight(20);

        Shape mask = Shape.subtract(full, hole);
        mask.setFill(BG_COLOR);
        overlayPane.getChildren().add(mask);

        // Нижняя подпись
        /*Label btmLabel = showBottomText();
        btmLabel.setLayoutX((stage.getScene().getWidth() - 400) / 2); // по центру
        btmLabel.setLayoutY(stage.getScene().getHeight() - 60); // внизу
        overlayPane.getChildren().add(btmLabel);*/

        // Описание
        Label label = new Label(inst.text);
        label.setWrapText(true);
        label.setTextFill(Color.WHITE);
        // Расположение текста
        Point2D pt = switch (inst.position) {
            case TOP -> new Point2D(bounds.getMinX(), bounds.getMinY() - 20);
            case BOTTOM -> new Point2D(bounds.getMinX(), bounds.getMaxY() + 10);
            case LEFT -> new Point2D(bounds.getMinX() - 200, bounds.getMinY());
            default -> new Point2D(bounds.getMaxX() + 10, bounds.getMinY());
        };
        label.relocate(pt.getX(), pt.getY());
        overlayPane.getChildren().add(label);
    }


}
