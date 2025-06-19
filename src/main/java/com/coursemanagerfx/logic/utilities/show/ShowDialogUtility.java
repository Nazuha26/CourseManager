package com.coursemanagerfx.logic.utilities.show;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.animations.ShowAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.StageSetupUtility;
import com.coursemanagerfx.controllers.dialogs.InputDialog_controller;
import com.coursemanagerfx.controllers.dialogs.NewCourseDialog_controller;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_controller;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.coursemanagerfx.controllers.dialogs.password.InputPass_controller;
import com.coursemanagerfx.custom_ui.ProgressSpinner;
import com.coursemanagerfx.logic.Actions;
import com.coursemanagerfx.logic.utilities.AppUtility;
import com.coursemanagerfx.logic.utilities.UpdateUtility;
import com.coursemanagerfx.logic.utilities.exceptions.NoInternetConnection;
import com.coursemanagerfx.logic.utilities.show.exceptions.DialogLoadException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;

import static com.coursemanagerfx.AppConstants.COURSES_PATH;

public class ShowDialogUtility {
    private static <T> T showDialog(String fxml, Window owner, boolean modal, Consumer<T> controllerConfigurator) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppConstants.class.getResource(fxml));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        AppUtility.setAppIcon(stage);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getScene().setFill(null);

        if (modal) {
            stage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) stage.initOwner(owner);
        } else {
            stage.initModality(Modality.NONE);
        }

        T controller = loader.getController();

        if (controller instanceof StageAttachable dialogController) {
            dialogController.setStage(stage);
            StageSetupUtility.setup(dialogController, stage);
        }

        if (controllerConfigurator != null) {
            controllerConfigurator.accept(controller);
        }

        stage.setOnShown(e -> {
            ShowAnimation.play(stage, null);
            Platform.runLater(stage::sizeToScene);
        });

        if (modal) stage.showAndWait();
        else stage.show();

        return controller;
    }

    public static AlertFX_controller showAlertDialog( Window owner,
                                                      boolean modal,
                                                      AlertFX_type type,
                                                      String main,
                                                      String prompt,
                                                      boolean showCancel) throws IOException {
        return showDialog(
                "/com/coursemanagerfx/ui/dialogs/alert_dialog.fxml",
                owner,
                modal,
                c -> {
                    c.getLabelMain().setText(main);
                    c.getLabelPrompt().setText(prompt);
                    c.getBtnCancel().setVisible(showCancel);
                    c.getBtnCancel().setManaged(showCancel);
                    c.getIconType().setImage(type.getIcon());
                }
        );
    }

    // === INPUT DIALOG ===
    public static String showInputDialog(Window owner, String dialogTitle, String dialogPrompt, String defTextInField) {
        try {
            InputDialog_controller controller = showDialog(
                    "/com/coursemanagerfx/ui/dialogs/input_dialog.fxml",
                    owner,
                    true,
                    c -> {
                        c.getLabelTitle().setText(dialogTitle);
                        c.getLabelPrompt().setText(dialogPrompt);
                        c.getTextField().setText(defTextInField);
                    }
            );

            return controller.getInputText();
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load input dialog", ex);
        }
    }

    // === CHECK PASSWORD DIALOG ===
    public static String showCheckPasswordDialog(Window owner) {
        try {
            InputPass_controller c = showDialog(
                    "/com/coursemanagerfx/ui/dialogs/password/input_password_dialog.fxml",
                    owner,
                    true,
                    null);

            return c.getInputPassword();
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load password dialog", ex);
        }
    }

    // === NEW COURSE DIALOG ===
    public static boolean showNewCourseDialog(Window owner) {
        try {
            NewCourseDialog_controller c = showDialog(
                    "/com/coursemanagerfx/ui/dialogs/new_course_dialog.fxml",
                    owner,
                    true,
                    null);

            return c.wasCourseCreated();
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load new course dialog", ex);
        }
    }

    // === GENERATED PASSWORD DIALOG ===
    public static void showGeneratedPasswordDialog(Window owner) {
        try {
            showDialog(
                    "/com/coursemanagerfx/ui/dialogs/password/generated_dialog.fxml",
                    owner,
                    true,
                    null
            );
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load generated password dialog", ex);
        }
    }

    // === OPEN COURSE WINDOW ===
    public static File showOpenCourse(Window owner) {
        FileChooser fc = new FileChooser();
        if (Files.exists(COURSES_PATH))
            fc.setInitialDirectory(COURSES_PATH.toFile());

        fc.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("CMan course (*.cman)", "*.cman"));
        fc.setTitle("Select an existing course");

        return fc.showOpenDialog(owner);      // null, if Cancel
    }

    // === ABOUT WINDOW ===
    public static void showAboutWindow(Window owner) {
        try {
            showDialog(
                    "/com/coursemanagerfx/ui/dialogs/about_dialog.fxml",
                    owner,
                    true,
                    null
            );
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load about window", ex);
        }
    }
}