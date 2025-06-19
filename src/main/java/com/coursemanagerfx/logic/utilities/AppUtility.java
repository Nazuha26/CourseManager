package com.coursemanagerfx.logic.utilities;

import com.coursemanagerfx.Launcher;
import com.coursemanagerfx.logic.utilities.show.ShowWindowUtility;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class AppUtility {
    /* ========== PUBLIC API ========== */

    public static void restartApp() throws Exception {
        File exe = getAppPath();
        new ProcessBuilder("cmd", "/c", "start", "\"\"", "\"" + exe.getAbsolutePath() + "\"").start();
        Platform.exit(); System.exit(0);
    }

    public static File getAppPath() throws Exception {
        return new File(Launcher.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
    }

    public static void setAppIcon(Stage stage) {
        stage.getIcons().addAll(
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-1.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-2.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-3.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-4.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-5.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-6.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-7.png"))),
                new Image(Objects.requireNonNull(ShowWindowUtility.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_icon-8.png")))
        );
    }


    /* ===== CORE  (private helpers) ============================ */

}
