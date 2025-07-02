/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

/*
package com.coursemanagerfx.logic.deprecated.notification;

import animatefx.animation.*;
import eu.iamgio.animated.transition.container.AnimatedVBox; // импорт класса AnimatedVBox
import javafx.animation.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Duration;

import java.io.IOException;

public class NotificationFX {

    private final Node notificationNode;
    private final NotificationPosition position;
    private final AnimatedVBox container; // теперь тип AnimatedVBox
    private static final double ANIM_DURATION = 0.5; // длительность анимаций (сек)
    private static final double SLIDE_OFFSET = 50;   // начальное смещение по X (пикселей)
    private static int count = 1;

    */
/**
     * Конструктор.
     * @param type тип уведомления (для внешнего вида)
     * @param mainText основной текст
     * @param additionalText дополнительный текст
     * @param position позиция уведомления (TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT или BOTTOM_RIGHT)
     * @param container контейнер (AnimatedVBox) в который добавляются уведомления
     *//*

    public NotificationFX(NotificationType type, String mainText, String additionalText,
                          NotificationPosition position, AnimatedVBox container) {
        this.position = position;
        container.setScaleY(position == NotificationPosition.BOTTOM ? -1 : 1);
        this.container = container;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coursemanagerfx/ui/notifications/notification_ui.fxml"));
            notificationNode = loader.load();
            Notification_controller controller = loader.getController();
            controller.setupNotification(type, mainText, additionalText);
            count++;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    */
/**
     * Показывает уведомление с анимацией появления.
     * Если количество уведомлений в контейнере превышает 5, удаляются лишние с анимацией.
     *//*

    public void show() {
        if (container.getChildren().size() >= 5) {
            container.setOut(new FadeOutUp());
            container.getChildren().removeFirst();
            container.setOut(new FadeOutRight());
        }
        notificationNode.setScaleY(position == NotificationPosition.BOTTOM ? -1 : 1);
        //notificationNode.setScaleY(-1);
        container.getChildren().add(notificationNode);

        PauseTransition autoClose = new PauseTransition(Duration.seconds(5));
        autoClose.setOnFinished(event -> container.getChildren().remove(notificationNode));
        autoClose.play();
    }
}
*/
