package com.coursemanagerfx.custom_ui;

import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.*;
import javafx.util.Duration;

public class GradientBackground {
    private final Canvas mainCanvas;
    private final Canvas blurCanvas;
    private double time = 0;
    private long lastFrameTime = 0;
    private double speed; // скорость анимации
    private long frameInterval; // интервал между кадрами в наносекундах

    // Константные массивы Stop для градиентов
    private static final Stop[] STOPS1 = {
            new Stop(0, Color.hsb(330, 0.6, 1.0, 0.6)),
            new Stop(1, Color.hsb(210, 0.6, 1.0, 0.6))
    };

    private static final Stop[] STOPS2 = {
            new Stop(0, Color.hsb(210, 0.6, 1.0, 0.3)),
            new Stop(1, Color.hsb(330, 0.6, 1.0, 0.3))
    };

    private static final Stop[] STOPS_BLUR = {
            new Stop(0, Color.rgb(255, 255, 255, 0.4)),
            new Stop(0.5, Color.rgb(245, 245, 245, 0.3)),
            new Stop(1, Color.rgb(245, 245, 245, 0.0))
    };

    /**
     * Конструктор с параметром Pane, используется скорость по умолчанию (0.002) и FPS = 5.
     */
    public GradientBackground(BorderPane pane) {
        this(pane, 0.1, 5);
    }

    /**
     * Конструктор с параметром Pane и скоростью анимации, используется FPS = 5.
     */
    public GradientBackground(BorderPane pane, double speed) {
        this(pane, speed, 5);
    }

    /**
     * Конструктор, позволяющий задать Pane, скорость анимации и FPS.
     * @param pane Панель, на которой рисуется градиент.
     * @param speed Коэффициент скорости анимации.
     * @param fps Количество кадров в секунду.
     */
    public GradientBackground(BorderPane pane, double speed, double fps) {
        this.speed = speed;
        setFPS(fps);

        // Создаем canvases и привязываем их размеры к размеру панели
        mainCanvas = new Canvas(pane.getWidth(), pane.getHeight());
        blurCanvas = new Canvas(pane.getWidth(), pane.getHeight());
        mainCanvas.widthProperty().bind(pane.widthProperty());
        mainCanvas.heightProperty().bind(pane.heightProperty());
        blurCanvas.widthProperty().bind(pane.widthProperty());
        blurCanvas.heightProperty().bind(pane.heightProperty());

        // Устанавливаем эффект размытия для верхнего слоя
        GaussianBlur blur = new GaussianBlur(20);
        blurCanvas.setEffect(blur);

        // Добавляем слои: сначала основной, затем слой с blur
        pane.getChildren().add(0, mainCanvas);
        pane.getChildren().add(1, blurCanvas);

        startAnimation();
    }

    /**
     * Сеттер для FPS. Например, если передать 0.5, то между кадрами будет задержка 2 сек.
     * @param fps Количество кадров в секунду.
     */
    public void setFPS(double fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("FPS должен быть положительным");
        }
        // 1 секунда = 1e9 наносекунд
        frameInterval = (long) (1_000_000_000 / fps);
    }

    private void startAnimation() {
        GraphicsContext gcMain = mainCanvas.getGraphicsContext2D();
        GraphicsContext gcBlur = blurCanvas.getGraphicsContext2D();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastFrameTime < frameInterval) return;
                lastFrameTime = now;
                draw(gcMain, gcBlur);
            }
        };
        timer.start();
    }

    private void draw(GraphicsContext gcMain, GraphicsContext gcBlur) {
        double w = mainCanvas.getWidth();
        double h = mainCanvas.getHeight();

        // Очищаем canvas'ы
        gcMain.clearRect(0, 0, w, h);
        gcBlur.clearRect(0, 0, w, h);

        // Обновляем время с учётом скорости анимации
        time += speed;

        // Предвычисляем синус и косинус для time
        double sinTime = Math.sin(time);
        double cosTime = Math.cos(time);

        // Центр и смещения для первого и второго градиентов
        double centerX = w / 2;
        double centerY = h / 2;
        double offsetX = sinTime * (w / 4);
        double offsetY = cosTime * (h / 4);
        double radius = Math.max(w, h) * 0.8;

        // Первый радиальный градиент
        RadialGradient gradient1 = new RadialGradient(
                0, 0,
                centerX + offsetX,
                centerY + offsetY,
                radius,
                false,
                CycleMethod.NO_CYCLE,
                STOPS1
        );
        gcMain.setFill(gradient1);
        gcMain.fillRect(0, 0, w, h);

        // Второй градиент – симметричное отражение первого
        RadialGradient gradient2 = new RadialGradient(
                0, 0,
                centerX - offsetX,
                centerY - offsetY,
                radius,
                false,
                CycleMethod.NO_CYCLE,
                STOPS2
        );
        gcMain.setFill(gradient2);
        gcMain.fillRect(0, 0, w, h);

        // Для blur-градиента используем time * 0.5
        double sinHalfTime = Math.sin(time * 0.5);
        double cosHalfTime = Math.cos(time * 0.5);
        double blurCenterX = centerX + sinHalfTime * (w / 8);
        double blurCenterY = centerY + cosHalfTime * (h / 8);
        double blurRadius = Math.max(w, h) * 0.7;

        RadialGradient blurGradient = new RadialGradient(
                0, 0,
                blurCenterX,
                blurCenterY,
                blurRadius,
                false,
                CycleMethod.NO_CYCLE,
                STOPS_BLUR
        );
        gcBlur.setFill(blurGradient);
        gcBlur.fillRect(0, 0, w, h);
    }
}