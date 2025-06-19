package com.coursemanagerfx.logic.utilities;

import com.coursemanagerfx.Launcher;
import javafx.application.Platform;

import java.io.File;

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


    /* ===== CORE  (private helpers) ============================ */

}
