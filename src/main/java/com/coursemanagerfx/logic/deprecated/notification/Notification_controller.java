package com.coursemanagerfx.logic.deprecated.notification;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.util.Objects;

public class Notification_controller {
    @FXML
    private Label labelMainText;  // Основной текст
    @FXML
    private Label labelAdditionalText; // Дополнительный
    @FXML
    private ImageView iconView;   // Для иконки
    @FXML
    private BorderPane rootPane;  // Или AnchorPane, если используешь его как корень

    public void setupNotification(NotificationType type, String mainText, String additionalText) {
        labelMainText.setText(mainText);
        labelAdditionalText.setText(additionalText);

        switch (type) {
            case ERROR:
                rootPane.getStyleClass().add("notification-error");
                //labelType.setText("Ошибка");
                iconView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/coursemanagerfx/ui/notifications/icons/error_256x256.png"))));
                break;
            case WARNING:
                rootPane.getStyleClass().add("notification-warning");
                //labelType.setText("Предупреждение");
                iconView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/coursemanagerfx/ui/notifications/icons/warning_256x256.png"))));
                break;
            case INFO:
                rootPane.getStyleClass().add("notification-info");
                //labelType.setText("Информация");
                iconView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/coursemanagerfx/ui/notifications/icons/info_256x256.png"))));
                break;
            case SUCCESS:
                rootPane.getStyleClass().add("notification-success");
                //labelType.setText("Успешно");
                iconView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/coursemanagerfx/ui/notifications/icons/success_256x256.png"))));
                break;
        }
    }
}