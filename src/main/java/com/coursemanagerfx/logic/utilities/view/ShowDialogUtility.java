package com.coursemanagerfx.logic.utilities.view;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.animations.ShowAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.StageSetupUtility;
import com.coursemanagerfx.controllers.dialogs.InputDialog_controller;
import com.coursemanagerfx.controllers.dialogs.NewCourseDialog_controller;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_controller;
import com.coursemanagerfx.controllers.dialogs.alert.AlertMessageType;
import com.coursemanagerfx.controllers.dialogs.alert.AlertType;
import com.coursemanagerfx.controllers.dialogs.password.InputPass_controller;
import com.coursemanagerfx.logic.utilities.AppUtility;
import com.coursemanagerfx.logic.utilities.view.exceptions.DialogLoadException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.*;

import javax.xml.transform.Source;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

import static com.coursemanagerfx.AppConstants.COURSES_PATH;

public class ShowDialogUtility {

    /* show APPLICATION MODAL dialog */
    private static <T> T showDialog(String fxml, Consumer<T> controllerConfigurator) throws IOException
        { return showDialog(fxml, controllerConfigurator, null); }

    /* show WINDOW MODAL dialog relative to the owner */
    private static <T> T showDialog(String fxml, Consumer<T> controllerConfigurator, Window owner) throws IOException {
        FXMLLoader loader = new FXMLLoader(AppConstants.class.getResource(fxml));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        AppUtility.setAppIcon(stage);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getScene().setFill(null);

        if (owner != null ) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
            System.out.println("WINDOW-MODAL dialog is shown");
        } else {
            stage.initModality(Modality.APPLICATION_MODAL);
            System.out.println("APP-MODAL    dialog is shown");
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

        //if (modal) stage.showAndWait();
        //else stage.show();

        stage.showAndWait();

        return controller;
    }

    public static boolean showAlertDialog( AlertType alertType,
                                           AlertMessageType messageType,
                                           String header,
                                           String content
                                         ) throws IOException {

        String titleText = switch (alertType) {
            case QUESTION -> "Question";
            case NOTIFICATION -> switch (messageType) {
                case ERROR -> "Error";
                case WARNING -> "Warning";
                case INFO -> "Info";
            };
        };

        boolean isQuestion = alertType == AlertType.QUESTION;

        try {
            AlertFX_controller controller = showDialog(
                    "/com/coursemanagerfx/ui/dialogs/alert_dialog.fxml",
                    c -> {
                        c.getLabelTitle().setText(titleText);
                        c.getLabelMain().setText(header);
                        c.getLabelPrompt().setText(content);
                        c.getBtnCancel().setVisible(isQuestion);
                        c.getBtnCancel().setManaged(isQuestion);
                        c.getIconType().setImage(messageType.getIcon());
                    }
            );

            return controller.isConfirmed();
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load AlertFX dialog", ex);
        }
    }

    // === INPUT DIALOG ===
    public static String showInputDialog(Window owner, String dialogTitle, String dialogPrompt, String defTextInField) {
        try {
            InputDialog_controller controller = showDialog(
                    "/com/coursemanagerfx/ui/dialogs/input_dialog.fxml",
                    c -> {
                        c.getLabelTitle().setText(dialogTitle);
                        c.getLabelPrompt().setText(dialogPrompt);
                        c.getTextField().setText(defTextInField);
                    },
                    owner
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
                    null,
                    owner
            );

            return c.getInputPassword();
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load password dialog", ex);
        }
    }

    // === NEW COURSE DIALOG ===
    public static boolean showNewCourseDialog() {
        try {
            NewCourseDialog_controller c = showDialog(
                    "/com/coursemanagerfx/ui/dialogs/new_course_dialog.fxml",
                    null
            );

            return c.wasCourseCreated();
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load new course dialog", ex);
        }
    }

    // === GENERATED PASSWORD DIALOG ===
    public static void showGeneratedPasswordDialog() {
        try {
            showDialog(
                    "/com/coursemanagerfx/ui/dialogs/password/generated_dialog.fxml",
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
                    null,
                    owner
            );
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load about window", ex);
        }
    }
}