/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.utilities;

import com.coursemanagerfx.logic.utilities.view.ShowWindowUtility;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public final class AppUtility {
    /* ========== PUBLIC API ========== */

    private AppUtility() {
    }

    public static void setAppIcon(Stage stage) {
        stage.getIcons().addAll(
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/16.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/20.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/24.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/32.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/40.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/48.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/64.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/256.png")))
        );
    }


    /* ===== CORE  (private helpers) ============================ */

}
