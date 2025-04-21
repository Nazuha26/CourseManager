package com.coursemanagerfx.dialogs;

import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.logic.BinaryCmanSaver;
import com.coursemanagerfx.logic.Group;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.coursemanagerfx.CM_HELPER.*;
/*import static com.coursemanagerfx.logic.CmanSaver.createEmptyCourseFile;*/

public class NewCourseDialog_controller {
    @FXML private BorderPane rootPane;
    @FXML private Button btnClose;
    @FXML private Button btnCancel;
    @FXML private Button btnConfirm;
    @FXML private Label errorLabel;
    @FXML private TextField txtFieldCourseName;
    @FXML private Spinner<Integer> spinGroupsCount;

    private Stage stage;

    private String courseName;
    public String getCourseName() {
        return courseName;
    }
    private int groupsCount;
    public int getGroupsCount() {
        return groupsCount;
    }
    private File newCourseFile;   // ← файл нового курса
    public File getNewCourseFile() {
        return newCourseFile;
    }

    // Смещение для перетаскивания окна
    private double xOffset;
    private double yOffset;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        // === СКРУГЛЕННЫЕ КРАЯ ===
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(rootPane.widthProperty());
        clip.heightProperty().bind(rootPane.heightProperty());
        clip.setArcWidth(20);         // ← стартовое значение
        clip.setArcHeight(20);        // ← стартовое значение
        rootPane.setClip(clip);       // ← устанавливаем новые края
        // ========================

        // === НАСТРОЙКА СПИНЕРА ===
        SpinnerValueFactory<Integer> valueFactoryMark =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1, 1);
        spinGroupsCount.setValueFactory(valueFactoryMark);
        // =========================

        // После загрузки интерфейса фокусируем поле ввода
        Platform.runLater(() -> txtFieldCourseName.requestFocus());
        errorLabel.setManaged(false);
    }

    @FXML
    private void btnConfirm() {
        String text = txtFieldCourseName.getText();
        if (text == null || text.trim().isEmpty()) {
            errorLabel.setText("Course name field cannot be empty!");
            errorLabel.setManaged(true);
            Platform.runLater(stage::sizeToScene);
            return;
        }

        courseName   = text.trim().replaceAll("\\s+", " "); // подчистили лишние пробелы
        groupsCount  = spinGroupsCount.getValue();
        newCourseFile = new File(COURSES_DIR, courseName + ".cman");

        try {
            // создаём пустой .cman‑файл
            Group[] groups = new Group[groupsCount];
            for (int i = 0; i < groupsCount; i++) {
                groups[i] = new Group(); // ← создаём пустую группу
            }
            BinaryCmanSaver.save(groups, newCourseFile);

            // фиксируем имя курса в FIRST_RUN
            try (FileWriter w = new FileWriter(FIRST_RUN_FILE)) {
                w.write(courseName);
            }

            errorLabel.setManaged(false);
            actionClose(stage, null);   // закрываем диалог

        } catch (IOException ex) {
            errorLabel.setText("Cannot create course file: " + ex.getMessage());
            errorLabel.setManaged(true);
            Platform.runLater(stage::sizeToScene);
        }
    }


    @FXML
    private void btnClose() {
        cancelAction();
    }

    @FXML
    private void btnCancel() {
        cancelAction();
    }

    private void cancelAction() {
        courseName = null;
        CM_HELPER.actionClose(stage, null);
    }

    @FXML
    private void onTitleBarPressed(javafx.scene.input.MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void onTitleBarDragged(javafx.scene.input.MouseEvent event) {
        CM_HELPER.onDragged(event, stage, xOffset, yOffset);
    }
}
