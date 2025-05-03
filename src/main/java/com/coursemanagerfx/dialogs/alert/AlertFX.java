package com.coursemanagerfx.dialogs.alert;

import com.coursemanagerfx.CM_HELPER;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;

public class AlertFX {


    public static boolean showConfirmDialog(Window owner, AlertFX_type dialogType, boolean showCancelButton, String dialogMainText, String dialogPrompt) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CM_HELPER.class.getResource("/com/coursemanagerfx/ui/dialogs/alert_dialog.fxml")
            );
            Parent root = loader.load();

            AlertFX_controller controller = loader.getController();
            controller.getLabelMain().setText(dialogMainText);
            controller.getLabelPrompt().setText(dialogPrompt);
            controller.getBtnCancel().setManaged(showCancelButton);

            switch (dialogType) {
                case INFO -> controller.getIconType().setImage(new Image("/com/coursemanagerfx/ui/notifications/icons/info_256x256.png"));
                case WARNING -> controller.getIconType().setImage(new Image("/com/coursemanagerfx/ui/notifications/icons/warning_256x256.png"));
                case ERROR -> controller.getIconType().setImage(new Image("/com/coursemanagerfx/ui/notifications/icons/error_256x256.png"));
            }

            Stage dialogStage = new Stage();
            dialogStage.initOwner(owner);
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initModality(Modality.WINDOW_MODAL);

            Scene scene = new Scene(root);
            scene.setFill(null);
            dialogStage.setScene(scene);

            controller.setStage(dialogStage);

            dialogStage.setOnShown(event -> CM_HELPER.animateAppearance(root));
            dialogStage.showAndWait();

            return controller.isConfirmed();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load confirm dialog FXML", e);
        }
    }
}
