package com.coursemanagerfx.controllers.dialogs.alert;

import com.coursemanagerfx.logic.utilities.view.ShowDialogUtility;
import com.coursemanagerfx.logic.utilities.view.exceptions.DialogLoadException;
import javafx.stage.Window;

import java.io.IOException;

public final class AlertFX {

    /** Show QUESTION-dialog
     *  @return true if pressed OK
     */
    public static boolean showQuestion(String    header,
                                       String    content) {

        try {
            return ShowDialogUtility.showAlertDialog(
                    AlertType.QUESTION,
                    AlertMessageType.WARNING,
                    header,
                    content
            );
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load Question dialog", ex);
        }
    }

    /** Show NOTIFICATION-dialog */
    public static void showNotification(AlertMessageType    messageType,
                                        String              header,
                                        String              content
                                       ) {

        try {
            ShowDialogUtility.showAlertDialog(
                    AlertType.NOTIFICATION,
                    messageType,
                    header,
                    content
            );
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load notification dialog", ex);
        }
    }
}