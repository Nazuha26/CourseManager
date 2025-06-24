package com.coursemanagerfx.controllers.dialogs.alert;

import javafx.scene.image.Image;

public enum AlertMessageType {
    WARNING    ("/com/coursemanagerfx/ui/notifications/icons/warning_256x256.png"),
    INFO       ("/com/coursemanagerfx/ui/notifications/icons/info_256x256.png"),
    ERROR      ("/com/coursemanagerfx/ui/notifications/icons/error_256x256.png");

    private final Image image;

    AlertMessageType(String path) {
        this.image = new Image(path);
    }

    public Image getIcon() {
        return image;
    }
}
