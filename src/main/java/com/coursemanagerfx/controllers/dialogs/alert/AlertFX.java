package com.coursemanagerfx.controllers.dialogs.alert;

import com.coursemanagerfx.logic.utilities.view.ShowDialogUtility;
import com.coursemanagerfx.logic.utilities.view.exceptions.DialogLoadException;
import javafx.stage.Window;

import java.io.IOException;

public final class AlertFX {

    /** Show QUESTION-dialog
     *  @return true if pressed OK
     */
    public static boolean showQuestion(String header,
                                       String content,
                                       String btnOkText) {

        try {
            return ShowDialogUtility.showAlertDialog(
                    AlertType.QUESTION,
                    AlertMessageType.WARNING,
                    header,
                    content,
                    btnOkText
            );
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load Question dialog", ex);
        }
    }

    public static boolean showQuestion(String header, String content)
        { return showQuestion(header, content, "OK"); }


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
                    content,
                    "OK"
            );
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load notification dialog", ex);
        }
    }
}