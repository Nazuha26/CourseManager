package com.coursemanagerfx.logic.utilities;

import com.coursemanagerfx.Launcher;
import com.coursemanagerfx.logic.utilities.update.exceptions.NoInternetConnection;
import com.coursemanagerfx.logic.utilities.view.ShowWindowUtility;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class AppUtility {
    /* ========== PUBLIC API ========== */

    public static void startRestartAppScript() throws Exception {
        File appFile = AppUtility.getAppPath();
        Path exeRelauncherPath = appFile.toPath().getParent().resolve("relauncher.exe");

        if (!Files.exists(exeRelauncherPath))
            throw new FileNotFoundException( "Restart failed: relauncher.exe not found at path:\n" + exeRelauncherPath );

        ProcessBuilder pb = new ProcessBuilder(
                exeRelauncherPath.toString(),
                appFile.getAbsolutePath()
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
