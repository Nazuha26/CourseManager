/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

/*
package com.coursemanagerfx.logic.deprecated.notification;

import animatefx.animation.FadeInLeft;
import animatefx.animation.FadeOutLeft;
import eu.iamgio.animated.binding.Animated;
import eu.iamgio.animated.binding.presets.AnimatedOpacity;
import eu.iamgio.animated.common.Curve;
import eu.iamgio.animated.transition.AnimationPair;
import eu.iamgio.animated.transition.container.AnimatedVBox;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.Random;

// This demo animates the content of multi-children containers, such as VBoxes.

public class Test extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Setup scene
        Pane root = new Pane();
        Scene scene = new Scene(root, 700, 600);

        // Setup container
        AnimatedVBox vBox = new AnimatedVBox(new AnimationPair(new FadeInLeft(), new FadeOutLeft()).setSpeed(3, 3));

        // Also try HBox:
        // AnimatedHBox hBox = new AnimatedHBox(new AnimationPair(new FadeInDown(), new SlideOutUp()).setSpeed(3, 3));

        // Setup + buttons

        Button topButton = new Button("+ Add on top");
        topButton.setPrefWidth(150);
        topButton.relocate(400, 100);
        topButton.setOnAction(e -> vBox.getChildren().add(0, new RectangleButton(vBox)));

        Button bottomButton = new Button("+ Add on bottom");
        bottomButton.setPrefWidth(150);
        bottomButton.relocate(400, 150);
        bottomButton.setOnAction(e -> vBox.getChildren().add(new RectangleButton(vBox)));

        root.getChildren().addAll(vBox, topButton, bottomButton);

        // Show
        primaryStage.setTitle("AnimatedContainer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static class RectangleButton extends AnchorPane {

        // Random-colored rectangle with a "Click to remove" text that appears on hover on it.

        public RectangleButton(Pane parent) {
            Rectangle rectangle = TestUtil.randomRectangle();
            setPrefSize(100, 100);
            rectangle.setWidth(100);
            rectangle.setHeight(100);

            Label text = new Label("Click to remove");
            text.setPrefSize(100, 100);
            text.setAlignment(Pos.CENTER);
            text.setOpacity(0);

            hoverProperty().addListener((observable, oldValue, isHover) -> text.setOpacity(isHover ? 1 : 0));

            StackPane.setAlignment(text, Pos.CENTER);

            getChildren().addAll(
                    rectangle,
                    new Animated(text, new AnimatedOpacity())
                            .custom(settings -> settings.withDuration(Duration.millis(150)).withCurve(Curve.EASE_IN_OUT))
            );

            setOnMouseClicked(ev -> parent.getChildren().remove(this));
        }
    }
}

*/
/**
 * @author Giorgio Garofalo
 *//*

class TestUtil {

    public static final double SCENE_WIDTH = 650;
    public static final double SCENE_HEIGHT = 500;

    */
/**
     * Centers a node (rectangles wrapped in Panes)
     * @param parent parent of the rectangle
     * @param scene scene
     *//*

    public static void center(Region parent, Scene scene) {
        parent.layoutXProperty().bind(scene.widthProperty().divide(2).subtract(parent.widthProperty().divide(2)));
        parent.layoutYProperty().bind(scene.heightProperty().divide(2).subtract(parent.heightProperty().divide(2)));
    }

    */
/**
     * @return randomly generated rectangle
     *//*

    public static Rectangle randomRectangle() {
        Random random = new Random();
        // Width and height go from 100 to 300
        double width = random.nextInt(200) + 100;
        double height = random.nextInt(200) + 100;
        // R, G and B values in 0.5-1 range
        Color color = randomColor(random);
        return new Rectangle(width, height, color);
    }

    */
/**
     * @param random {@link Random} object
     * @return randomly generated color in 0.5-1 range
     *//*

    public static Color randomColor(Random random) {
        return new Color((random.nextInt(5) + 5) / 10F, (random.nextInt(5) + 5) / 10F, (random.nextInt(5) + 5) / 10F, 1);
    }

    */
/**
     * @return randomly generated color in 0.5-1 range
     *//*

    public static Color randomColor() {
        return randomColor(new Random());
    }

    */
/**
     * @param color base color
     * @return complementary (opposite) color
     *//*

    public static Color complementaryColor(Color color) {
        return Color.color(1 - color.getRed(), 1 - color.getGreen(), 1 - color.getBlue());
    }
}*/
