package com.coursemanagerfx.logic.utilities;

import com.coursemanagerfx.Launcher;
import com.coursemanagerfx.logic.utilities.exceptions.NoInternetConnection;
import com.coursemanagerfx.logic.utilities.show.ShowWindowUtility;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class AppUtility {
    /* ========== PUBLIC API ========== */

    public static void startRestartAppScript() throws Exception {
        //File exe =
        //new ProcessBuilder("cmd", "/c", "start", "\"\"", "\"" + exe.getAbsolutePath() + "\"").start();
        //String exe = "C:\\Program Files\\CourseManagerFX\\CourseManagerFX.exe";
        //new ProcessBuilder("cmd", "/c", "start", "\"\"", "\"" + exe + "\"").start();

        Path scriptPath = Paths.get(AppUtility.getAppPath().getParent(), "relauncher.vbs");
        //Path scriptPath = Paths.get("C:\\Program Files\\CourseManagerFX", "relauncher.vbs");

        ProcessBuilder pb = new ProcessBuilder(
                "wscript.exe",
                scriptPath.toString()
        );

        pb.inheritIO();
        pb.start();
    }

    public static File getAppPath() throws Exception {
        return new File(Launcher.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
    }

    private static void checkInternetOrThrow() {
        try {
            InetAddress.getByName("google.com");
        } catch (IOException e) {
            throw new NoInternetConnection("No internet connection", e);
        }
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
