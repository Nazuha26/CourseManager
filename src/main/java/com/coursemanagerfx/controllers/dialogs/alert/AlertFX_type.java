package com.coursemanagerfx.controllers.dialogs.alert;

import javafx.scene.image.Image;

public enum AlertFX_type {
    WARNING("/com/coursemanagerfx/ui/notifications/icons/warning_256x256.png"),
    INFO   ("/com/coursemanagerfx/ui/notifications/icons/info_256x256.png"),
    ERROR  ("/com/coursemanagerfx/ui/notifications/icons/error_256x256.png");

    private final String path;
    private final Image image;

    AlertFX_type(String path) {
        this.path = path;
        this.image = new Image(path);  // загружается один раз
    }

    public Image getIcon() {
        return image;
    }
}
