package com.coursemanagerfx.controllers;


import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.custom_ui.GradientBackground;
import com.coursemanagerfx.dialogs.About_controller;
import com.coursemanagerfx.dialogs.ConfirmDialogType;
import com.coursemanagerfx.dialogs.NewCourseDialog_controller;
import com.coursemanagerfx.logic.*;
import com.coursemanagerfx.logic.commands.AddEventCommand;
import com.coursemanagerfx.logic.commands.AddStudentCommand;
import com.coursemanagerfx.logic.commands.Command;
import com.coursemanagerfx.logic.utilitys.HistoryUtility;
import com.coursemanagerfx.notification.AlertFX;
import com.coursemanagerfx.notification.NotificationPosition;
import com.coursemanagerfx.notification.NotificationType;
import eu.iamgio.animated.transition.AnimationPair;
import eu.iamgio.animated.transition.container.AnimatedVBox;
import javafx.animation.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.coursemanagerfx.CM_HELPER.*;

enum TopPaneMode {
    HISTORY, EVENT_INFO
}

public class Main_controller {
    @FXML
    private BorderPane title_bar;
    private boolean isMaximizedFlag = true;
    private Double savedX = null;
    private Double savedY = null;
    private double xOffset = 0;
    private double yOffset = 0;
    private static Rectangle clip;

    @FXML private BorderPane notificationPane;
    public BorderPane getNotificationPane() {
        return notificationPane;
    }

    @FXML private BorderPane eventInfoTopPane;
    @FXML private StackPane historyTopPane;

    @FXML private TextField txtFieldSearch;
    @FXML private TextArea txtPaneHistory;
    public TextArea getTxtPaneHistory() {
        return txtPaneHistory;
    }

    @FXML private TextFlow txtFlowHistory;
    public TextFlow getTxtFlowHistory() { return txtFlowHistory; }

    @FXML private InlineCssTextArea richTxtPaneHistory;
    public InlineCssTextArea getRichTxtPaneHistory() {
        return richTxtPaneHistory;
    }

    @FXML private Label lblCurHistory;
    public Label getLblCurHistory() {
        return lblCurHistory;
    }

    @FXML private Button btnHistory;
    @FXML private Button btnBackFromHistory;
    private ImageView historyIcon_To;
    private ImageView historyIcon_Back;

    @FXML private Label lblAppName;

    private AnimatedVBox vboxLeftAnimated;
    private AnimatedVBox vboxRightAnimated;
    public AnimatedVBox getVBoxLeft() {
        return vboxLeftAnimated;
    }
    public AnimatedVBox getVBoxRight() {
        return vboxRightAnimated;
    }

    @FXML
    private BorderPane mainPanel;
    public BorderPane getMainPanel() {
        return mainPanel;
    }

    private Stage stage;
    @FXML
    private HBox tabHBox;
    @FXML
    private VBox studentVBox;
    @FXML
    private ScrollPane studentsScrollPane;
    @FXML private TextArea txtAreaEventDescrp;
    @FXML private ComboBox<String> comBoxEventPreset;
    @FXML private Spinner<Integer> spinnerMark;
    @FXML private DatePicker dtpkCreationDate;
    @FXML private HBox hboxExpiredDate;
    @FXML private Spinner<Integer> spinnerExpTimeCount;
    @FXML private ComboBox<String> comBoxExpiredTime;
    @FXML private Label labelMark;
    @FXML private Separator btnsSepr;
    @FXML private Label labelExpDate;
    @FXML private Separator btnsSepr2;
    // --- BUTTONS ---
    @FXML private Button btnAddStudent;
    @FXML private Button btnAddEvent;
    @FXML private Button btnCancelEvent;
    @FXML private Button btnCreateEvent;
    @FXML private Button btnClearHistory;
    @FXML private Button btnClose;
    @FXML private Button btnMaximize;
    @FXML private Button btnMinimize;
    // --- BUTTONS ---
    @FXML
    private TableView<StudentEvent> eventsTable;
    @FXML private TableColumn<StudentEvent, Number> numberColumn;
    @FXML private TableColumn<StudentEvent, String> crtDateColumn;
    @FXML private TableColumn<StudentEvent, String> descriptionColumn;
    @FXML private TableColumn<StudentEvent, Number> marksColumn;
    @FXML private TableColumn<StudentEvent, String> expDateColumn;
    @FXML private TableColumn<StudentEvent, EventStatus> statusColumn;

    @FXML
    private HBox addEventBottomPane;
    @FXML
    private StackPane mainTopPane;
    @FXML
    private StackPane tableStackPane;

    private Group currentGroup;
    public Group getCurrentGroup() { return currentGroup; }

    private Student getSelectedStudent() {
        for (Student student : currentGroup.getStudents()) {
            if (student.isSelected()) {
                return student;
            }
        }
        return null;
    }

    private final List<Command> undoStack = new ArrayList<>();
    public List<Command> getUndoStack() { return undoStack; }

    private final List<Command> redoStack = new ArrayList<>();
    public List<Command> getRedoStack() { return redoStack; }

    // Добавить команду в стек undo и очистить стек redo.
    public void addCommand(Command cmd) {
        undoStack.add(cmd);
        redoStack.clear();
    }

    // Отмена последней команды (Undo).
    public void undo() {
        if (!undoStack.isEmpty()) {
            Command cmd = undoStack.removeLast();
            cmd.undo();
            HistoryUtility.setHistory(richTxtPaneHistory, lblCurHistory,
                    HistoryUtility.Types.INFO, "Undo: " + cmd.getDescription());
            redoStack.add(cmd);
        }
    }

    // Повтор последней отменённой команды (Redo).
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command cmd = redoStack.removeLast();
            cmd.execute(true);
            HistoryUtility.setHistory(richTxtPaneHistory, lblCurHistory,
                    HistoryUtility.Types.INFO, "Redo: " + cmd.getDescription());
            undoStack.add(cmd);
        }
    }



    private CM_HELPER helper;

    public void init(CM_HELPER helper) {
        this.helper = helper;
        initializeTabs(helper.getCourse());
        lblAppName.setText("CourseManagerFX – " + helper.getCourseName());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        // Слушатель восстановления окна
        stage.iconifiedProperty().addListener((obs, wasMinimized, isNowMinimized) -> {
            if (!isNowMinimized) {
                animateRestoration(stage);
            }
        });
    }

    @FXML
    public void initialize() {
        new GradientBackground(mainPanel, 0.005, 2); // ← ГРАДИЕНТОВЫЙ ФОН

        // === СКРУГЛЕННЫЕ КРАЯ ===
        clip = new Rectangle();
        clip.widthProperty().bind(mainPanel.widthProperty());
        clip.heightProperty().bind(mainPanel.heightProperty());
        clip.setArcWidth(0);        // ← стартовое значение
        clip.setArcHeight(0);       // ← стартовое значение
        mainPanel.setClip(clip);    // ← устанавливаем новые края
        // ========================

        // === ДЛЯ УВЕДОМЛЕНИЙ ===
        vboxLeftAnimated = new AnimatedVBox(new AnimationPair(new animatefx.animation.FadeInLeft(), new animatefx.animation.FadeOutLeft()).setSpeed(3, 3));
        vboxLeftAnimated.setPrefWidth(200);
        vboxLeftAnimated.setSpacing(5);

        vboxRightAnimated = new AnimatedVBox(new AnimationPair(new animatefx.animation.FadeInRight(), new animatefx.animation.FadeOutRight()).setSpeed(3, 3));
        vboxRightAnimated.setPrefWidth(200);
        vboxRightAnimated.setSpacing(5);

        notificationPane.setLeft(vboxLeftAnimated);
        notificationPane.setRight(vboxRightAnimated);
        // =======================

        // === НАСТРОЙКА ИКОНОК НА *КНОПКАХ ИСТОРИИ* ===
        Image arrowImg = new Image(Objects.requireNonNull(getClass().getResource("/com/coursemanagerfx/ui/icons/w_arrow_256x256.png")).toExternalForm());

        historyIcon_To = new ImageView(arrowImg);
        historyIcon_To.setFitWidth(16);
        historyIcon_To.setFitHeight(16);
        btnHistory.setGraphic(historyIcon_To);
        btnHistory.setContentDisplay(ContentDisplay.RIGHT);

        historyIcon_Back = new ImageView(arrowImg);
        historyIcon_Back.setFitWidth(16);
        historyIcon_Back.setFitHeight(16);
        historyIcon_Back.setRotate(180);    // ← перевернуть по умолчанию
        btnBackFromHistory.setGraphic(historyIcon_Back);
        btnBackFromHistory.setContentDisplay(ContentDisplay.RIGHT);
        // =============================================

        // === СЛУШАТЕЛЬ ДЛЯ ПОИСКА СТУДЕНТОВ ===
        txtFieldSearch.textProperty().addListener((observable, oldText, newText) -> {
            if (currentGroup != null) {
                if (newText.trim().isEmpty()) {
                    displayStudents(currentGroup);
                } else {
                    filterStudents(newText);
                }
            }
        });
        // ======================================

        // выбираем первую опцию в days/weeks/month
        if (!comBoxExpiredTime.getItems().isEmpty())
            comBoxExpiredTime.getSelectionModel().selectFirst();


        for (EventMods mod : EventMods.values())
            comBoxEventPreset.getItems().add(mod.getPreset().getPresetName());
        comBoxEventPreset.getSelectionModel().selectFirst();
        comBoxEventPreset.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean showExtras = EventMods.OTHER.getPreset().getPresetName().equals(newVal);
            btnsSepr2.setVisible(showExtras);
            btnsSepr2.setManaged(showExtras);
            labelExpDate.setVisible(showExtras);
            labelExpDate.setManaged(showExtras);
            btnsSepr.setVisible(showExtras);
            btnsSepr.setManaged(showExtras);
            labelMark.setVisible(showExtras);
            labelMark.setManaged(showExtras);
            spinnerMark.setVisible(showExtras);
            spinnerMark.setManaged(showExtras);
            hboxExpiredDate.setVisible(showExtras);
            hboxExpiredDate.setManaged(showExtras);
        });

        SpinnerValueFactory<Integer> valueFactoryExpTime =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1, 1);
        spinnerExpTimeCount.setValueFactory(valueFactoryExpTime);

        SpinnerValueFactory<Integer> valueFactoryMark =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1, 1);
        spinnerMark.setValueFactory(valueFactoryMark);

        btnsSepr2.setVisible(false);
        btnsSepr2.setManaged(false);
        labelExpDate.setVisible(false);
        labelExpDate.setManaged(false);
        btnsSepr.setVisible(false);
        btnsSepr.setManaged(false);
        labelMark.setVisible(false);
        labelMark.setManaged(false);
        spinnerMark.setVisible(false);
        spinnerMark.setManaged(false);
        hboxExpiredDate.setVisible(false);
        hboxExpiredDate.setManaged(false);

        mainTopPane.setTranslateX(100);
        mainTopPane.setVisible(false);
        mainTopPane.setManaged(false);
        mainTopPane.setOpacity(0);

        eventsTable.getColumns().forEach(col -> col.setReorderable(false));

        numberColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(eventsTable.getItems().indexOf(cellData.getValue()) + 1)
        );
        numberColumn.setSortable(false);
        numberColumn.setCellFactory(column -> new TableCell<StudentEvent, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        crtDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(column -> {
            return new TableCell<StudentEvent, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setStyle("-fx-alignment: CENTER-LEFT;"); // выравнивание по левому краю
                }
            };
        });

        marksColumn.setCellValueFactory(new PropertyValueFactory<>("mark"));
        expDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiredDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<StudentEvent, EventStatus>() {
            @Override
            protected void updateItem(EventStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(""); // сброс
                } else {
                    setText(item.name());

                    // Цвет фона по статусу
                    if (item == EventStatus.ACTIVE) {
                        setStyle("-fx-background-color: rgba(0,255,0,0.3);" +
                                "-fx-text-fill: #00d800;" +
                                "-fx-font-weight: bold");
                    } else if (item == EventStatus.COMPLETED) {
                        setStyle("-fx-background-color: rgba(255,0,0,0.3);" +
                                "-fx-text-fill: #da0000;" +
                                "-fx-font-weight: bold");
                    } else {
                        setStyle(""); // fallback
                    }
                }
            }
        });

         //stage = (Stage) mainPanel.getScene().getWindow();
    }   // ГЛАВНАЯ ИНИЦИАЛИЗАЦИЯ

    public void initializeTabs(Group[] course) {
        tabHBox.getChildren().clear();
        for (int i = 0; i < course.length; i++) {
            final int index = i;

            Button tabButton = new Button("Group " + (i + 1));
            tabButton.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(tabButton, Priority.ALWAYS);
            tabButton.getStyleClass().add("tab-button");

            tabButton.setOnAction(event -> {
                displayStudents(course[index]);
                if (!course[index].getStudents().isEmpty()) {
                    selectGroupAndStudent(course[index], course[index].getStudents().getFirst().getID());
                } else {
                    // Если группа пуста, всё равно выделяем вкладку и очищаем таблицу событий
                    selectGroupAndStudent(course[index], -1);
                }
            });

            tabHBox.getChildren().add(tabButton);
        }

        // Задаём первую группу как текущую
        currentGroup = course[0];
        // Если в первой группе есть хотя бы один студент, выделяем его,
        // иначе вызываем selectGroupAndStudent, который очистит список событий
        displayStudents(currentGroup);
        if (!currentGroup.getStudents().isEmpty()) {
            selectGroupAndStudent(currentGroup, currentGroup.getStudents().getFirst().getID());
        } else {
            selectGroupAndStudent(currentGroup, -1);
        }
    }         // INIT ГРУПП
    public void displayStudents(Group group) {
        studentVBox.getChildren().clear();

        if (group.getStudents().isEmpty()) {
            showEmptyLabel("There are no students in the group yet");
            return;
        }

        // Сортировка студентов по алфавиту без учета регистра
        group.getStudents().sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));

        // Получаем выбранного студента, если он есть
        Student selectedStudent = getSelectedStudent();

        int number = 1;
        for (Student student : group.getStudents()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coursemanagerfx/ui/forms/student_panel.fxml"));
                BorderPane studentPanel = loader.load();

                StudentPanel_controller spc = loader.getController();
                spc.setStudent(student);
                spc.setMainController(this);

                Button studentButton = spc.getBtnStudent();
                studentButton.setText(student.getName());
                studentButton.setOnAction(e -> {
                    // Сбрасываем выделение у всех студентов
                    for (Student s : group.getStudents()) {
                        s.setSelected(false);
                    }
                    studentVBox.getChildren().forEach(node -> {
                        if (node instanceof BorderPane bp) {
                            Button btn = findButton(bp.getCenter());
                            if (btn != null) btn.setStyle("");
                        }
                    });
                    setSelectedStyle(studentButton);
                    student.setSelected(true);
                    loadStudentEvents(student.getID());
                });
                studentButton.setUserData(student);

                Label studentNumber = (Label) studentPanel.getLeft();
                studentNumber.setText(String.valueOf(number));

                // Если этот студент выбран, применяем стиль выделения
                if (selectedStudent != null && student.getID() == selectedStudent.getID()) {
                    setSelectedStyle(studentButton);
                }

                studentVBox.getChildren().add(studentPanel);
                number++;
            } catch (IOException e) {
                System.err.println("Error loading student_panel.fxml: " + e.getMessage());
            }
        }
    }           // ПОКАЗ СТУДЕНТОВ
    public void loadStudentEvents(int studentID) {
        Student student = findStudentById(studentID);
        // Сначала сбрасываем стили у всех кнопок
        for (Node node : studentVBox.getChildren()) {
            if (node instanceof BorderPane borderPane) {
                Node center = borderPane.getCenter();
                if (center instanceof Button btn) {
                    btn.setStyle(""); // сброс
                }
            }
        }

        // Затем выделяем кнопку нужного студента
        for (Node node : studentVBox.getChildren()) {
            if (node instanceof BorderPane borderPane) {
                Node center = borderPane.getCenter();
                if (center instanceof Button btn && btn.getUserData() instanceof Student btnStudent) {
                    if (btnStudent == student) {
                        setSelectedStyle(btn);
                        break;
                    }
                }
            }
        }

        // Загружаем события
        eventsTable.getItems().clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        for (StudentEvent event : student.getEvents()) {
            String rawDate = event.getExpiredDate().split(" ")[0];
            LocalDate expiredDate = LocalDate.parse(rawDate, formatter);
            if (expiredDate.isBefore(LocalDate.now())) {
                event.setExpired(true);
            }
        }
        eventsTable.getItems().addAll(student.getEvents());
    }       // ЗАГРУЗКА ИВЕНТОВ ДЛЯ СТУДЕНТА

    // Утилитный метод для установки стиля выбранного студента
    private void setSelectedStyle(Button btn) {
        btn.setStyle("""
                        -fx-background-color: rgba(0,0,0,0.6);
                        -fx-border-color: rgba(255,255,255,0.8);
                        -fx-border-width: 2;
                    """);
    }

    // Утилитный метод для поиска кнопок студентов в Vbox
    private Button findButton(Node root) {
        if (root instanceof Button btn) return btn;
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Button found = findButton(child);
                if (found != null) return found;
            }
        }
        return null;
    }



    private static final double ANIM_DURATION = 0.5;
    private boolean isAnimPlaysFlag = false;

    @FXML
    private void btnAddStudent() {
        if (currentGroup == null) {
            AlertFX alertFX = new AlertFX(NotificationType.WARNING,
                    "Choose the group",
                    "",
                    NotificationPosition.TOP,
                    getVBoxRight());
            alertFX.show();
            return;
        }

        String rawStudentName = showInputDialog(mainPanel.getScene().getWindow(),
                                    "Add student",
                                    "Enter the student's name:");

        if (rawStudentName == null || rawStudentName.trim().isEmpty()) return; // ← пользователь нажал Cancel или оставил поле пустым

        String studentName = rawStudentName.trim().replaceAll("\\s+", " ");
        Command cmd = new AddStudentCommand(currentGroup, studentName, this);
        cmd.execute(false);
        addCommand(cmd);
    }

    @FXML
    private void btnAddEvent() { showMainTopPane(TopPaneMode.EVENT_INFO); }

    @FXML
    private void btnCancelEvent() { hideMainTopPane(false); }

    @FXML
    private void btnCreateEvent() {
        Student selectedStudent = getSelectedStudent();
        if (selectedStudent == null) {
            AlertFX alertFX = new AlertFX(NotificationType.WARNING,
                    "Choose the student",
                    "",
                    NotificationPosition.TOP,
                    getVBoxRight());
            alertFX.show();
            return;
        }

        String selectedDesc = comBoxEventPreset.getSelectionModel().getSelectedItem();
        EventMods selectedMod = findModByDescription(selectedDesc);

        int mark;
        String expiredRaw;

        if (selectedMod == EventMods.OTHER) {
            mark = spinnerMark.getValue();
            expiredRaw = getExpiredDate();
        } else {
            mark = selectedMod.getPreset().getMark();
            expiredRaw = selectedMod.getPreset().getExpiredTime();
        }

        LocalDate creationDateRaw = dtpkCreationDate.getValue();
        String description = txtAreaEventDescrp.getText().replaceAll("\\s*[\r\n]+\\s*", " ").trim();

        // Проверки на пустые поля
        if (creationDateRaw == null) {
            AlertFX alertFX = new AlertFX(NotificationType.WARNING,
                    "Select creation date",
                    "",
                    NotificationPosition.TOP,
                    getVBoxRight());
            alertFX.show();
            return;
        }

        if (description.isEmpty()) {
            AlertFX alertFX = new AlertFX(NotificationType.WARNING,
                    "Enter a description of the event",
                    "",
                    NotificationPosition.TOP,
                    getVBoxRight());
            alertFX.show();
            return;
        }

        // Дополнительная проверка для режима OTHER
        if (selectedMod == EventMods.OTHER) {
            if (spinnerMark.getValue() == null || spinnerExpTimeCount.getValue() == null ||
                    comBoxExpiredTime.getSelectionModel().getSelectedItem() == null) {
                AlertFX alertFX = new AlertFX(NotificationType.WARNING,
                        "Fill in all fields for the custom event",
                        "",
                        NotificationPosition.TOP,
                        getVBoxRight());
                alertFX.show();
                return;
            }
        }


        String creationDate = creationDateRaw.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        // --- Парсим срок
        String[] parts = expiredRaw.split(" ");
        int value = Integer.parseInt(parts[0]);
        String type = parts[1];

        LocalDate expDate;
        switch (type) {
            case "%d" -> expDate = creationDateRaw.plusDays(value);
            case "%w" -> expDate = creationDateRaw.plusWeeks(value);
            case "%m" -> expDate = creationDateRaw.plusMonths(value);
            default -> expDate = creationDateRaw;
        }

        long daysBetween = ChronoUnit.DAYS.between(creationDateRaw, expDate);
        String formattedExpDate = expDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " (" + daysBetween + " days)";

        StudentEvent newEvent = new StudentEvent(creationDate, description, mark, formattedExpDate);

        // Создаем и выполняем команду добавления события
        Command cmd = new AddEventCommand(currentGroup, selectedStudent, newEvent, this);
        cmd.execute(false);
        addCommand(cmd);
    }

    @FXML
    private void btnHistory() { showMainTopPane(TopPaneMode.HISTORY); }

    @FXML
    private void btnBackFromHistory() { hideMainTopPane(true); }

    @FXML
    private void btnClearHistory() {
        richTxtPaneHistory.clear();
        lblCurHistory.setText("");
    }


    // ===== *УТИЛИТНЫЕ МЕТОДЫ* =====
    // Показ панели нового ивента / истории
    private void showMainTopPane(TopPaneMode topPaneMode) {
        if (isAnimPlaysFlag) return;

        if (topPaneMode == TopPaneMode.HISTORY) {
            historyIcon_Back.setRotate(180);
            RotateTransition rotate = new RotateTransition(Duration.seconds(0.3), historyIcon_To);
            rotate.setByAngle(-180);
            rotate.setInterpolator(Interpolator.EASE_BOTH);
            rotate.play();
        }

        this.isAnimPlaysFlag = true;
        // Анимация исчезновения нижней панели
        TranslateTransition slide = new TranslateTransition(Duration.seconds(ANIM_DURATION / 2), addEventBottomPane);
        slide.setByY(30);
        slide.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fade = new FadeTransition(Duration.seconds(ANIM_DURATION / 2), addEventBottomPane);
        fade.setToValue(0);

        ParallelTransition animation = new ParallelTransition(slide, fade);

        // Анимация прозрачности tableStackPane
        blinkingAnim(tableStackPane).play();

        // Анимация появления второй панели
        animation.setOnFinished(e -> {
            addEventBottomPane.setVisible(false);
            addEventBottomPane.setManaged(false);

            if (topPaneMode == TopPaneMode.HISTORY) {    // ← показываем панель истории
                eventInfoTopPane.setVisible(false);
                eventInfoTopPane.setManaged(false);
                historyTopPane.setVisible(true);
                historyTopPane.setManaged(true);
            }
            else {                                               // ← показываем панель создания ивента
                historyTopPane.setVisible(false);
                historyTopPane.setManaged(false);
                eventInfoTopPane.setVisible(true);
                eventInfoTopPane.setManaged(true);
            }


            mainTopPane.setManaged(true);
            mainTopPane.setVisible(true);

            FadeTransition showFade = new FadeTransition(Duration.seconds(ANIM_DURATION / 2), mainTopPane);
            showFade.setToValue(1);
            showFade.setInterpolator(Interpolator.EASE_BOTH);

            TranslateTransition showSlide = new TranslateTransition(Duration.seconds(ANIM_DURATION / 2), mainTopPane);
            showSlide.setToX(0);
            showSlide.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition showAnim = new ParallelTransition(showFade, showSlide);
            showAnim.setOnFinished(ev -> {
                this.isAnimPlaysFlag = false;
            });
            showAnim.play();
        });

        animation.play();
    }
    // Скрытие панели нового ивента / истории
    private void hideMainTopPane(boolean isBackHistoryBtn) {
        if (isAnimPlaysFlag) return;
        this.isAnimPlaysFlag = true;

        if (isBackHistoryBtn) {
            historyIcon_To.setRotate(0);
            RotateTransition rotate = new RotateTransition(Duration.seconds(0.3), historyIcon_Back);
            rotate.setByAngle(180);
            rotate.setInterpolator(Interpolator.EASE_BOTH);
            rotate.play();
        }

        // Анимация исчезновения верхней панели
        TranslateTransition slideOut = new TranslateTransition(Duration.seconds(ANIM_DURATION / 2), mainTopPane);
        slideOut.setToX(50);
        slideOut.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(ANIM_DURATION / 2), mainTopPane);
        fadeOut.setToValue(0);

        ParallelTransition hideTopPane = new ParallelTransition(fadeOut, slideOut);
        hideTopPane.setOnFinished(e -> {
            mainTopPane.setVisible(false);
            mainTopPane.setManaged(false);

            addEventBottomPane.setVisible(true);
            addEventBottomPane.setManaged(true);

            // Анимация появления нижней панели
            TranslateTransition slideIn = new TranslateTransition(Duration.seconds(ANIM_DURATION / 2), addEventBottomPane);
            slideIn.setByY(-30);
            slideIn.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(ANIM_DURATION / 2), addEventBottomPane);
            fadeIn.setToValue(1);
            fadeIn.setOnFinished(ev -> this.isAnimPlaysFlag = false);

            new ParallelTransition(slideIn, fadeIn).play();
        });

        // Анимация обратной прозрачности для tableStackPane
        blinkingAnim(tableStackPane).play();

        hideTopPane.play();
    }
    // Генерация уникального 7-значного ID студента
    public int generateUniqueStudentId() {
        List<Integer> existingIds = new ArrayList<>();

        // Собираем id студентов из всех групп курса
        for (Group group : helper.getCourse()) {
            for (Student student : group.getStudents()) {
                if (!existingIds.contains(student.getID())) {
                    existingIds.add(student.getID());
                }
            }
        }

        int newId;
        do {
            // Генерируем число от 1,000,000 до 9,999,999 (7-значное)
            newId = 1000000 + (int) (Math.random() * 9000000);
        } while (existingIds.contains(newId));

        return newId;
    }
    // Поиск ивента по его названию и списка пресетов
    public static EventMods findModByDescription(String desc) {
        for (EventMods mod : EventMods.values()) {
            if (mod.getPreset().getPresetName().equals(desc)) {
                return mod;
            }
        }
        return EventMods.OTHER;
    }
    // Получить Exp. Date из спинера и комбобокса
    private String getExpiredDate() {
        int count_time = spinnerExpTimeCount.getValue();
        String type_time = comBoxExpiredTime.getSelectionModel().getSelectedItem();

        return switch (type_time) {
            case "Day (s)" -> count_time + " %d";
            case "Week (s)" -> count_time + " %w";
            case "Month (s)" -> count_time + " %m";
            default -> count_time + " %d"; // по умолчанию — дни
        };
    }
    // Анимация мигания
    private Timeline blinkingAnim(Node node) {
        return new Timeline(
                new KeyFrame(Duration.seconds(0.0),
                        new KeyValue(node.opacityProperty(), 1, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(Main_controller.ANIM_DURATION / 2),
                        new KeyValue(node.opacityProperty(), 0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(Main_controller.ANIM_DURATION),
                        new KeyValue(node.opacityProperty(), 1, Interpolator.EASE_BOTH))
        );
    }
    // Найти студента по указанному ID
    public Student findStudentById(int id) {
        if (currentGroup == null) return null;
        for (Student student : currentGroup.getStudents()) {
            if (student.getID() == id) {
                return student;
            }
        }
        return null;
    }
    // Выделить конкретного СТУДЕНТА в конкретной ГРУППЕ
    public void selectGroupAndStudent(Group group, int studentID) {
        // 1. Выделяем вкладку группы: сначала снимаем выделение со всех вкладок
        Group[] groups = helper.getCourse();
        for (int i = 0; i < groups.length; i++) {
            Node node = tabHBox.getChildren().get(i);
            if (node instanceof Button tabButton) {
                tabButton.getStyleClass().remove("selected-tab");
            }
        }
        // Затем добавляем выделение только для нужной вкладки
        for (int i = 0; i < groups.length; i++) {
            if (groups[i] == group) {
                Node node = tabHBox.getChildren().get(i);
                if (node instanceof Button tabButton) {
                    tabButton.getStyleClass().add("selected-tab");
                }
            }
        }
        currentGroup = group;

        // Если студент не задан или группа пуста – очищаем таблицу и выходим
        if (studentID == -1 || group.getStudents().isEmpty()) {
            eventsTable.getItems().clear();
            return;
        }

        // 2. Сбрасываем флаги и стили выделения у всех студентов выбранной группы
        for (Student s : group.getStudents()) {
            s.setSelected(false);
        }
        for (Node node : studentVBox.getChildren()) {
            if (node instanceof BorderPane borderPane) {
                Button btn = findButton(borderPane.getCenter());
                if (btn != null) {
                    btn.setStyle(""); // сброс стиля
                }
            }
        }

        // 3. Ищем в studentVBox кнопку, соответствующую нужному студенту (сравнение по ID)
        boolean found = false;
        for (Node node : studentVBox.getChildren()) {
            if (node instanceof BorderPane borderPane) {
                Button btn = findButton(borderPane.getCenter());
                if (btn != null && btn.getUserData() instanceof Student btnStudent) {
                    if (btnStudent.getID() == studentID) {
                        setSelectedStyle(btn);
                        btnStudent.setSelected(true);
                        found = true;
                        break;
                    }
                }
            }
        }

        // 4. Если студент найден, загружаем его события, иначе очищаем таблицу событий
        if (found) {
            loadStudentEvents(studentID);
        } else {
            eventsTable.getItems().clear();
        }
    }

    // Действие для правильного выхода из главного окна программы
    private void actionMainExit() {
        if (stage == null) return;

        boolean hasUnsavedChanges = !undoStack.isEmpty();

        if (!hasUnsavedChanges) {
            actionClose(stage, null);
            return;
        }

        boolean confirmed = showConfirmDialog(
                mainPanel.getScene().getWindow(),
                ConfirmDialogType.WARNING,
                "You have unsaved changes.",
                "Do you want to exit without saving?"
        );

        if (confirmed) {
            actionClose(stage, null);
        }
    }
    // Утилитный метод для лабеля в Vbox студентов
    private void showEmptyLabel(String text) {
        Pane spacerTop = new Pane();
        spacerTop.setPrefHeight(9999);
        Pane spacerBottom = new Pane();
        spacerBottom.setPrefHeight(9999);
        Label emptyLabel = new Label(text);
        emptyLabel.getStyleClass().add("empty-label");
        emptyLabel.setMaxWidth(Double.MAX_VALUE);
        emptyLabel.setAlignment(Pos.CENTER);
        studentVBox.getChildren().addAll(spacerTop, emptyLabel, spacerBottom);
    }
    // Метод для фильтра студентов при поиске
    private void filterStudents(String query) {
        List<Student> filteredStudents = new ArrayList<>();
        for (Student student : currentGroup.getStudents()) {
            if (student.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredStudents.add(student);
            }
        }
        filteredStudents.sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));

        studentVBox.getChildren().clear();

        // Сохраняем выбранного студента до фильтрации
        Student selectedStudent = getSelectedStudent();

        if (filteredStudents.isEmpty()) {
            showEmptyLabel("No matching students found");
        } else {
            int number = 1;
            for (Student student : filteredStudents) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coursemanagerfx/ui/forms/student_panel.fxml"));
                    BorderPane studentPanel = loader.load();
                    studentPanel.setStyle("-fx-background-color: rgba(255,144,0,0.3)");

                    StudentPanel_controller spc = loader.getController();
                    spc.setStudent(student);
                    spc.setMainController(this);

                    Button studentButton = spc.getBtnStudent();
                    studentButton.setText(student.getName());
                    studentButton.setOnAction(e -> {
                        // Сбрасываем выделение у всех студентов
                        for (Student s : currentGroup.getStudents()) {
                            s.setSelected(false);
                        }
                        studentVBox.getChildren().forEach(node -> {
                            if (node instanceof BorderPane bp) {
                                Button btn = findButton(bp.getCenter());
                                if (btn != null) btn.setStyle("");
                            }
                        });
                        setSelectedStyle(studentButton);
                        student.setSelected(true);
                        loadStudentEvents(student.getID());
                    });
                    studentButton.setUserData(student);

                    Label studentNumber = (Label) studentPanel.getLeft();
                    studentNumber.setText(String.valueOf(number));

                    // Если этот студент ранее был выбран, то выделяем его
                    if (selectedStudent != null && student.getID() == selectedStudent.getID()) {
                        setSelectedStyle(studentButton);
                    }

                    studentVBox.getChildren().add(studentPanel);
                    number++;
                } catch (IOException e) {
                    System.err.println("Error loading student_panel.fxml: " + e.getMessage());
                }
            }
        }
    }

    // ========== TITLE BAR ==========

    // ===== MENU BAR =====
    // === FILE ===
    @FXML private void miSave() {
        try {
            // Формируем путь к файлу, можно использовать helper.COURSES_DIR и имя курса
            String filePath = helper.COURSES_DIR.getAbsolutePath() +
                    File.separator + helper.getCourseName() + ".cman";
            CmanSaver.save(filePath, helper.getCourse());
            undoStack.clear();
            redoStack.clear();
            //Files.write(helper.TEMP_FILE.toPath(), new byte[0]);

            AlertFX alert = new AlertFX(NotificationType.SUCCESS, "Saved successfully", "",
                    NotificationPosition.TOP, vboxRightAnimated);
            alert.show();
            HistoryUtility.setHistory(richTxtPaneHistory,
                    lblCurHistory,
                    HistoryUtility.Types.SUCCESS,
                    "Saved successfully");
            System.out.println("Saved successfully");

        } catch (IOException ex) {
            showConfirmDialog(mainPanel.getScene().getWindow(),
                                ConfirmDialogType.ERROR,
                                "Saving Error:",
                                ex.getMessage());
        }
    }
    @FXML private void miNew() {
        // показываем диалог и получаем результат
        NewCourseDialog_controller res = showNewCourseDialog(stage);
        if (res == null || res.getCourseName() == null) return;   // отменили

        String newName = res.getCourseName();
        File   newFile = res.getNewCourseFile();

        // запоминаем в FirstRun
        try (FileWriter w = new FileWriter(FIRST_RUN_FILE)) {
            w.write(newName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // закрываем текущее главное окно и в колбэке открываем новое
        actionClose(stage, () -> {
            try {
                openMainWindow(newName, newFile);   // создаёт свежий Stage
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    @FXML private void miOpen() {
        File file = CM_HELPER.showOpenCourseDialog(stage);
        if (file == null) return;           // Cancel

        String newName = file.getName().replaceFirst("\\.cman$", "");

        // Запоминаем последний открытый курс
        try (FileWriter w = new FileWriter(CM_HELPER.FIRST_RUN_FILE)) {
            w.write(newName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Плавно закрываем текущее окно и в колбэке открываем новое
        actionClose(stage, () -> {
            try {
                CM_HELPER.openMainWindow(newName, file);   // версия без Stage — создаёт новый
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    @FXML private void miExport() {
        // TODO EXPORT
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Soon...");
        alert.showAndWait();
    }
    @FXML private void miQuit() {
        actionMainExit();
    }

    // === EDIT ===
    // === действия ОТМЕНИТЬ / ПЕРЕДЕЛАТЬ ===
    @FXML private void miUndo() {
        undo();
    }
    @FXML private void miRedo() {
        redo();
    }

    @FXML private void miOptions() {
        // TODO EXPORT
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Soon...");
        alert.showAndWait();
    }

    // === HELP ===
    @FXML private void miAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/coursemanagerfx/ui/dialogs/about_dialog.fxml"));
            Parent root = loader.load();

            About_controller controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainPanel.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.setFill(null);
            dialogStage.setScene(scene);

            controller.setStage(dialogStage);

            dialogStage.setOnShown(e -> CM_HELPER.animateAppearance(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // ======================================

    // === SET STYLE FOR TEXT ===
    @FXML private void setBoldText() {
        // TODO EXPORT
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Soon...");
        alert.showAndWait();
    }
    @FXML private void setItalicText() {
        // TODO EXPORT
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Soon...");
        alert.showAndWait();
    }
    @FXML private void setUnderlineText() {
        // TODO EXPORT
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Soon...");
        alert.showAndWait();
    }
    // ======================================

    // === КНОПКА "ЗАКРЫТЬ" ===
    @FXML private void windowClose() {
        actionMainExit();
    }
    // ========================

    // === УВЕЛИЧИТЬ ОКНО ===
    @FXML private void windowMaximize() {
        if (stage == null) return;

        Parent root = stage.getScene().getRoot();
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        if (isMaximizedFlag) {
            // ПОЛНОЭКРАННЫЙ -> ОКОННЫЙ
            //System.out.println("Полноэкранный -> оконный");

            // === делаем ЗАКРУГЛЕННЫЕ КРАЯ и ИСЧЕЗНОВЕНИЕ ОКНА ===
            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.millis((double) ANIMATION_DURATION / 2),
                            new KeyValue(root.opacityProperty(), 0),
                            new KeyValue(clip.arcWidthProperty(), 20),
                            new KeyValue(clip.arcHeightProperty(), 20)
                    )
            );
            // ====================================================
            fadeOut.setOnFinished(e -> {
                double posX = savedX != null ? savedX : screenBounds.getMinX() + (screenBounds.getWidth() - MAIN_SMALL_WINDOW_WIDTH) / 2;
                double posY = savedY != null ? savedY : screenBounds.getMinY() + (screenBounds.getHeight() - MAIN_SMALL_WINDOW_HEIGHT) / 2;
                stage.setX(posX);
                stage.setY(posY);
                stage.setWidth(MAIN_SMALL_WINDOW_WIDTH);
                stage.setHeight(MAIN_SMALL_WINDOW_HEIGHT);
                btnMaximize.getStyleClass().removeAll("window-button-maximize", "window-button-restore");
                btnMaximize.getStyleClass().add("window-button-maximize");

                // === ПОКАЗЫВАЕМ УМЕНЬШЕННОЕ ОКНО ===
                Timeline fadeIn = new Timeline(
                        new KeyFrame(Duration.millis((double) ANIMATION_DURATION / 2),
                                new KeyValue(root.opacityProperty(), 1)
                        )
                );
                fadeIn.play();
                // ===================================
            });
            fadeOut.play();
            isMaximizedFlag = false;
        } else {
            // ОКОННЫЙ -> ПОЛНОЭКРАННЫЙ
            //System.out.println("Оконный -> полноэкранный");

            savedX = stage.getX();
            savedY = stage.getY();
            // === делаем ПРЯМЫЕ КРАЯ и ИСЧЕЗНОВЕНИЕ ОКНА ===
            Timeline fadeOut = new Timeline(
                    new KeyFrame(Duration.millis((double) ANIMATION_DURATION / 2),
                            new KeyValue(root.opacityProperty(), 0),
                            new KeyValue(clip.arcWidthProperty(), 0),
                            new KeyValue(clip.arcHeightProperty(), 0)
                    )
            );
            // ==============================================
            fadeOut.setOnFinished(e -> {
                stage.setX(screenBounds.getMinX());
                stage.setY(screenBounds.getMinY());
                stage.setWidth(screenBounds.getWidth());
                stage.setHeight(screenBounds.getHeight());
                btnMaximize.getStyleClass().removeAll("window-button-maximize", "window-button-restore");
                btnMaximize.getStyleClass().add("window-button-restore");

                // === ПОКАЗЫВАЕМ МАКСИМИЗИРОВАННОЕ ОКНО ===
                Timeline fadeIn = new Timeline(
                        new KeyFrame(Duration.millis((double) ANIMATION_DURATION / 2),
                                new KeyValue(root.opacityProperty(), 1)
                        )
                );
                fadeIn.play();
                // =========================================
            });
            fadeOut.play();

            isMaximizedFlag = true;
        }
    }

    // === УМЕНЬШИТЬ ОКНО ===
    @FXML private void windowMinimize() {
        actionMinimize(stage);
    }

    // === ПЕРЕМЕЩЕНИЕ (ДРАГ) ОКНА ===
    @FXML private void onTitleBarPressed(javafx.scene.input.MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }
    @FXML private void onTitleBarDragged(javafx.scene.input.MouseEvent event) {
        if (isMaximizedFlag) return;
        onDragged(event, stage, xOffset, yOffset);
    }
    // ===============================
}