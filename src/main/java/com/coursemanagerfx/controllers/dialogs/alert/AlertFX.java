package com.coursemanagerfx.controllers.dialogs.alert;

import com.coursemanagerfx.logic.utilities.show.ShowDialogUtility;
import com.coursemanagerfx.logic.utilities.show.exceptions.DialogLoadException;
import javafx.stage.Window;

import java.io.IOException;

public final class AlertFX {

    /** Show CONFIRM-dialog
     *  @return true if pressed OK
     */
    public static boolean showConfirm(Window owner,
                                      AlertFX_type type,
                                      String     main,
                                      String     prompt) {

        try {
            AlertFX_controller c = ShowDialogUtility.showAlertDialog(owner, true, type, main, prompt, true);
            return c.isConfirmed();
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load confirm dialog", ex);
        }
    }

    /** Show Notification */
    public static void showNotification(Window owner,
                                        AlertFX_type type,
                                        String    main,
                                        String    prompt,
                                        boolean modal) {

        try {
            ShowDialogUtility.showAlertDialog(owner, modal, type, main, prompt, false);
        } catch (IOException ex) {
            throw new DialogLoadException("Failed to load notification dialog", ex);
        }
    }
}