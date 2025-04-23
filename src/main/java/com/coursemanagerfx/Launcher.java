package com.coursemanagerfx;

import com.coursemanagerfx.logic.utilitys.GetPoint;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static com.coursemanagerfx.CM_HELPER.*;

public class Launcher extends Application {
    private static boolean skipUpdate = false;

    private void checkForUpdates() {
        new Thread(() -> {
            try {
                JSONArray releases = GitHubUpdater.getReleases();
                JSONObject bestRelease = null;
                String bestVersion = "0.0.0";

                for (int i = 0; i < releases.length(); i++) {
                    JSONObject release = releases.getJSONObject(i);
                    String tag = release.getString("tag_name");

                    if (!tag.matches("^v\\d+(\\.\\d+)*-fx$")) continue;

                    String version = tag.replace("-fx", "").replaceFirst("^v", "");
                    if (GitHubUpdater.compareVersions(version, CUR_VERSION) > 0) {
                        bestVersion = version;
                        bestRelease = release;
                    }
                }

                if (bestRelease != null) {
                    JSONArray assets = bestRelease.getJSONArray("assets");
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject asset = assets.getJSONObject(i);
                        String name = asset.getString("name");
                        if (name.endsWith(".msi")) {
                            String downloadUrl = asset.getString("url");
                            String finalBestVersion = bestVersion;
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Update available");
                                alert.setHeaderText("New version available: " + finalBestVersion);
                                alert.setContentText("Install now (you will need to reopen the app again)?");

                                ButtonType yes = new ButtonType("Yes");
                                ButtonType no = new ButtonType("No, Thanks", ButtonBar.ButtonData.CANCEL_CLOSE);
                                alert.getButtonTypes().setAll(yes, no);

                                alert.showAndWait().ifPresent(type -> {
                                    if (type == yes) {
                                        new Thread(() -> {
                                            try {
                                                File tempFile = GitHubUpdater.downloadAsset(downloadUrl, name);
                                                new ProcessBuilder("msiexec", "/i", tempFile.getAbsolutePath()).start();
                                                new ProcessBuilder("powershell", "-Command",
                                                        "Start-Sleep -Seconds 10; Remove-Item -Path '" + tempFile.getAbsolutePath() + "' -Force").start();
                                                Platform.exit();
                                                System.exit(0);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }).start();
                                    }
                                });
                            });
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // логируем
            }
        }).start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (!skipUpdate) checkForUpdates(); // ← запускаем проверку, только если не указан -nu

        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        if (!COURSES_DIR.exists()) COURSES_DIR.mkdirs();

        // Если файл FIRST_RUN существует и содержит имя курса, проверяем, что такой курс есть
        if (CM_HELPER.FIRST_RUN_FILE.exists()) {
            String courseName;
            try (BufferedReader reader = new BufferedReader(new FileReader(CM_HELPER.FIRST_RUN_FILE))) {
                courseName = reader.readLine();
            }
            if (courseName != null && !courseName.trim().isEmpty()) {
                File courseFile = new File(CM_HELPER.COURSES_DIR, courseName + ".cman");
                if (courseFile.exists()) {
                    openMainWindow(primaryStage, courseName, courseFile);   // ← открываем главное окно программы
                    if (printMouseOnP) GetPoint.setupMousePositionLogger(primaryStage.getScene());
                    return;
                }
            }
        }

        openStartWindow();
    }

    private static boolean printMouseOnP = false;

    public static void main(String[] args) {
        for (String arg : args) {
            if ("-p".equalsIgnoreCase(arg)) {
                System.out.println("=== TURN ON GET MOUSE POSITION MODE ===\n" +
                        "===      RELATIVE TO THE SCENE      ===\n" +
                        "X | Y");
                printMouseOnP = true;
            }
            if ("-nu".equalsIgnoreCase(arg)) {
                System.out.println("Updates check skipped...");
                skipUpdate = true;
            }
        }
        launch(args);
    }
}