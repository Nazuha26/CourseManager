package com.coursemanagerfx.controllers;


import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.logic.basic.*;
import com.coursemanagerfx.custom_ui.GradientBackground;
import com.coursemanagerfx.dialogs.About_controller;
import com.coursemanagerfx.dialogs.alert.AlertFX_type;
import com.coursemanagerfx.dialogs.NewCourseDialog_controller;
import com.coursemanagerfx.logic.basic.event.EventMods;
import com.coursemanagerfx.logic.basic.event.EventPreset;
import com.coursemanagerfx.logic.basic.event.EventStatus;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.date.DateType;
import com.coursemanagerfx.logic.basic.event.date.EventDate;
import com.coursemanagerfx.logic.basic.event.date.ExpirationRule;
import com.coursemanagerfx.logic.commands.*;
import com.coursemanagerfx.logic.commands.event_comms.AddEventCommand;
import com.coursemanagerfx.logic.commands.event_comms.DeleteEventCommand;
import com.coursemanagerfx.logic.commands.event_comms.EditEventCommand;
import com.coursemanagerfx.logic.commands.student_comms.AddStudentCommand;
import com.coursemanagerfx.logic.security.CmanSecurityParser;
import com.coursemanagerfx.logic.security.CmanSecuritySaver;
import com.coursemanagerfx.logic.utilitys.HistoryUtility;
import com.coursemanagerfx.logic.utilitys.UpdateUtility;
import com.coursemanagerfx.notification.NotificationFX;
import com.coursemanagerfx.notification.NotificationPosition;
import com.coursemanagerfx.notification.NotificationType;
import eu.iamgio.animated.transition.AnimationPair;
import eu.iamgio.animated.transition.container.AnimatedVBox;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.coursemanagerfx.CM_HELPER.*;
import static com.coursemanagerfx.dialogs.alert.AlertFX.showConfirmDialog;

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
    public Label getLblAppName() {
        return lblAppName;
    }

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
    @FXML private Spinner<Integer> spinnerExpTimeCount;
    @FXML private ComboBox<String> comBoxExpiredTime;
    // --- BUTTONS ---
    @FXML private Button btnAddStudent;
    @FXML private Button btnAddEvent;
    @FXML private Button btnCancelEvent;
    @FXML private Button btnCreateEvent;
    @FXML private Button btnClearHistory;
    @FXML private Button btnClose;
    @FXML private Button btnMaximize;
    @FXML private Button btnMinimize;
    @FXML private Button btnToSchedule;
    @FXML private Button btnDeleteEvent;
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

    public Student getSelectedStudent() {
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

    private StudentEvent copyEvent;

    private Button activeEditButton = null;
    @FXML private Label lblEditing;

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

        // === ИНИЦИАЛИЗАЦИЯ ОСНОВНОЙ ПАНЕЛИ ДОБАВЛЕНИЯ ИВЕНТА ===
        mainTopPane.setTranslateX(100);
        mainTopPane.setVisible(false);
        mainTopPane.setManaged(false);
        mainTopPane.setOpacity(0);
        // =======================================================

        lblEditing.setVisible(false);
        lblEditing.setManaged(false);

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

        dtpkCreationDate.setDayCellFactory(setDatePickerStyle());
        dtpkExpirationDate.setDayCellFactory(setDatePickerStyle());


        // выбираем при инициализации первую опцию days/weeks/month
        if (!comBoxExpiredTime.getItems().isEmpty())
            comBoxExpiredTime.getSelectionModel().selectFirst();

        // === SPINNERS INIT ===
        // Exp. Date spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactoryExpTime =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1, 1);
        spinnerExpTimeCount.setValueFactory(valueFactoryExpTime);

        // Mark spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactoryMark =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1, 1);
        spinnerMark.setValueFactory(valueFactoryMark);

        // Ограничение ввода только цифрами
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        };

        TextFormatter<Integer> formatterExp = new TextFormatter<>(
                new IntegerStringConverter(),
                valueFactoryExpTime.getValue(),
                integerFilter
        );
        spinnerExpTimeCount.getEditor().setTextFormatter(formatterExp);

        TextFormatter<Integer> formatterMark = new TextFormatter<>(
                new IntegerStringConverter(),
                valueFactoryMark.getValue(),
                integerFilter
        );
        spinnerMark.getEditor().setTextFormatter(formatterMark);

        spinnerExpTimeCount.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) spinnerExpTimeCount.increment(0); // commit
        });
        spinnerMark.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) spinnerMark.increment(0); // commit
        });
        formatterExp.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                int minExp = valueFactoryExpTime.getMin();
                formatterExp.setValue(minExp);
            }
        });
        formatterMark.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                int minMark = valueFactoryMark.getMin();
                formatterMark.setValue(minMark);
            }
        });
        // =====================


        // === INIT PRESETS COMBOBOX ===
        for (EventMods mod : EventMods.values()) {
            comBoxEventPreset.getItems().add(mod.getPreset().getName());
        }
        comBoxEventPreset.getSelectionModel().selectFirst();    // ← выбираем первый пресет
        applyPresetToFields(
                findModByName(
                        comBoxEventPreset.getSelectionModel().getSelectedItem()     // ← загружаем данные пресета в поля
                )
        );
        comBoxEventPreset.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            EventMods selectedMod = findModByName(newVal);
            applyPresetToFields(selectedMod);
        });
        // =============================

        // === BTN TO SCHEDULE INIT ===
        btnToSchedule.disableProperty().bind(
                dtpkCreationDate.valueProperty().isNull()
                        .or(
                                comBoxEventPreset.getSelectionModel().selectedItemProperty()
                                        .isNotEqualTo(EventMods.OTHER.getPreset().getName())
                        )
        );
        // ============================



        // ============================== ***INIT EVENT TABLE***    ============================== \\
        // *************************************************************************************** \\
        eventsTable.getColumns().forEach(col -> col.setReorderable(false));     // ← убираем драг колонок

        // ========== *КОЛОНКА* NUMBER ==========
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
        // ======================================



        // ========== *КОЛОНКА* CREATION DATE ==========
        //crtDateColumn.setCellValueFactory(new PropertyValueFactory<>("crtDate"));

        // === СОРТИРОВКА ПО ДАТЕ ===
        crtDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCrtDate().toString())
        );
        // =============================================



        // ========== *КОЛОНКА* DESCRIPTION ==========
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(column -> new TableCell<StudentEvent, String>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(4);

                    TextFlow textFlow = createTrimmedTextFlow(item);
                    textFlow.maxWidthProperty().bind(getTableColumn().widthProperty().subtract(6));
                    vbox.getChildren().add(new javafx.scene.Group(textFlow));

                    // Кнопка
                    Button btnEdit = new Button();
                    btnEdit.setPrefWidth(32);
                    btnEdit.setPrefHeight(32);
                    btnEdit.getStyleClass().add("edit-button");
                    StackPane.setAlignment(btnEdit, Pos.TOP_RIGHT);


                    btnEdit.setOnAction(e -> {
                        eventsTable.getColumns().forEach(col -> col.setSortable(false));
                        if (isAnimPlaysFlag) return;

                        StudentEvent event = getTableRow().getItem();
                        if (event == null) return;

                        // Если редактируем другой ивент — сбрасываем прошлое
                        if (editingEvent != null && editingEvent != event) {
                            if (activeEditButton != null) {
                                activeEditButton.setStyle(""); // снимаем выделение с предыдущей кнопки
                            }
                            editingEvent = null;
                        }

                        if (editingEvent == event) {
                            // Повторное нажатие на ту же кнопку — отмена редактирования
                            editingEvent = null;
                            btnEdit.setStyle("");
                            hideMainTopPane(false);
                            activeEditButton = null;
                            stopEditing();
                        } else {
                            // Начинаем редактирование
                            editingEvent = event;
                            openEditPane(event);
                            btnEdit.setStyle("-fx-background-color: rgba(221,0,0,0.5);" +
                                    "-fx-background-image: url(/com/coursemanagerfx/ui/icons/w_minimize_256x256.png)");
                            activeEditButton = btnEdit;
                        }
                    });

                    StackPane stackPane = new StackPane(vbox, btnEdit);
                    setGraphic(stackPane);
                }
            }

            private TextFlow createTrimmedTextFlow(String textStr) {
                TextFlow flow = new TextFlow();
                Pattern pattern = Pattern.compile(
                        "(\\*[^*]+\\*)|(_[^_]+_)|(=[^=]+=)|([^*_=]+)");
                Matcher matcher = pattern.matcher(textStr);

                while (matcher.find()) {
                    String group = matcher.group();

                    Text text = new Text();
                    text.setFill(Color.WHITE);
                    text.setStyle("-fx-font-size: 14px;");

                    if (group.startsWith("*") && group.endsWith("*")) {
                        text.setText(group.substring(1, group.length() - 1));
                        text.setStyle(text.getStyle() + "; -fx-font-weight: bold;");
                    } else if (group.startsWith("_") && group.endsWith("_")) {
                        text.setText(group.substring(1, group.length() - 1));
                        text.setStyle(text.getStyle() + "; -fx-font-style: italic;");
                    } else if (group.startsWith("=") && group.endsWith("=")) {
                        text.setText(group.substring(1, group.length() - 1));
                        text.setUnderline(true);
                    } else {
                        text.setText(group);
                    }

                    flow.getChildren().add(text);
                }

                return flow;
            }
        });
        // ===========================================



        // ========== *КОЛОНКА* MARK ==========
        marksColumn.setCellValueFactory(new PropertyValueFactory<>("mark"));



        // ========== *КОЛОНКА* EXPIRATION DATE ==========
        //expDateColumn.setCellValueFactory(new PropertyValueFactory<>("expDate"));

        // Устанавливаем количество дней в скобках между датой создания и датой окончания
        expDateColumn.setCellValueFactory(cellData -> {
            StudentEvent event = cellData.getValue();
            String formatted = event.getExpDate().toFormattedWithDaysFrom(event.getCrtDate());
            return new SimpleStringProperty(formatted);
        });

        // === СОРТИРОВКА ПО ДАТЕ ===
        expDateColumn.setComparator(Comparator.comparing(s -> {
            String[] parts = s.split(" ")[0].split("\\.");
            return LocalDate.of(
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[0])
            );
        }));
        // === СОРТИРОВКА ПО ЗНАЧЕНИЮ В СКОБКАХ EXP. DATE ===
        /*expDateColumn.setComparator(Comparator.comparingInt(s -> {
            try {
                // Извлекаем число из строки: "dd.MM.yyyy (XX days)"
                int start = s.indexOf('(');
                int end = s.indexOf(" days");
                if (start != -1 && end != -1 && start < end) {
                    String num = s.substring(start + 1, end).trim();
                    return Integer.parseInt(num);
                }
            } catch (Exception e) {
                // В случае ошибки (например, "error") — возвращаем максимум
                return Integer.MAX_VALUE;
            }
            return Integer.MAX_VALUE;
        }));*/
        // ==================================================



        // ========== *КОЛОНКА* STATUS ==========
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
        // ======================================

        // Сортировка по умолчанию — по колонке "Status", по возрастанию
        eventsTable.getSortOrder().add(statusColumn);
        statusColumn.setSortType(TableColumn.SortType.ASCENDING);
        eventsTable.sort();
        // ======================================================================================= \\

    }   // ГЛАВНАЯ ИНИЦИАЛИЗАЦИЯ

    private StudentEvent editingEvent = null;

    private void stopEditing() {
        for (Node node : tabHBox.getChildren()) node.setDisable(false);       // включаем таббар при редактировании ивента
        for (Node node : studentVBox.getChildren()) node.setDisable(false);   // включаем выбор студента при редактировании ивента
        txtFieldSearch.setDisable(false);                                     // включаем поле поиска студентов при редактировании ивента
        btnCancelEvent.setVisible(true);
        btnCancelEvent.setManaged(true);
        comBoxEventPreset.getSelectionModel().select(EventMods.FIRST.getPreset().getName());
        txtAreaEventDescrp.setText("");
        lblEditing.setVisible(false);
        lblEditing.setManaged(false);
        btnDeleteEvent.setVisible(false);
        btnDeleteEvent.setManaged(false);
        btnCreateEvent.setText("Create");
        eventsTable.getColumns().forEach(col -> col.setSortable(true));
        btnCreateEvent.setOnAction(evt -> btnCreateEvent()); // ← оригинальный handler
    }

    public void openEditPane(StudentEvent event) {
        for (Node node : tabHBox.getChildren()) node.setDisable(true);      // отключаем таббар при редактировании ивента
        for (Node node : studentVBox.getChildren()) node.setDisable(true);  // отключаем выбор студента при редактировании ивента
        txtFieldSearch.setDisable(true);                                    // отключаем поле поиска студентов при редактировании ивента
        // 1) Показать панель, если она скрыта
        if (!mainTopPane.isVisible()) {
            showMainTopPane(TopPaneMode.EVENT_INFO);
        }

        // 2) Сохраняем, что редактируем это событие
        editingEvent = event;

        btnCancelEvent.setVisible(false);
        btnCancelEvent.setManaged(false);

        lblEditing.setVisible(true);
        lblEditing.setManaged(true);

        // 3) Заполняем форму
        comBoxEventPreset.getSelectionModel().select(EventMods.OTHER.getPreset().getName());    // ← выбираем "OTHER"

        txtAreaEventDescrp.setText(event.getDescription());

        // — дата создания
        EventDate cd = event.getCrtDate();
        LocalDate creation = LocalDate.of(cd.getYear(), cd.getMonth(), cd.getDay());
        dtpkCreationDate.setValue(creation);

        // — оценка
        spinnerMark.getValueFactory().setValue(event.getMark());

        // — рассчитываем RAW-срок и выставляем спиннер/комбобокс/датапикер
        EventDate ed = event.getExpDate();
        LocalDate expiration = LocalDate.of(ed.getYear(), ed.getMonth(), ed.getDay());
        dtpkExpirationDate.setValue(expiration);

        long days = ChronoUnit.DAYS.between(creation, expiration);
        spinnerExpTimeCount.getValueFactory().setValue((int) days);
        comBoxExpiredTime.getSelectionModel().select("Day (s)");

        // 4) Меняем кнопку «Create» → «Save» и навешиваем сохранение
        btnCreateEvent.setText("Save");
        btnCreateEvent.setOnAction(ae -> saveEditedEvent());

        btnDeleteEvent.setVisible(true);
        btnDeleteEvent.setManaged(true);
        btnDeleteEvent.setOnAction(ae -> {
            if (isAnimPlaysFlag) return;
            boolean isDelete = showConfirmDialog(mainPanel.getScene().getWindow(), AlertFX_type.INFO,
                    true,
                    "Event deleting...",
                    "Do you want to delete this event?");

            if (isDelete) {
                StudentEvent currentEvent = editingEvent;
                DeleteEventCommand cmd = new DeleteEventCommand(
                        getCurrentGroup(),
                        getSelectedStudent(),
                        currentEvent,
                        this
                );
                cmd.execute(false);
                addCommand(cmd);
                stopEditing();
                hideMainTopPane(false);
                editingEvent = null;
            }
        });
    }

    public static Callback<DatePicker, DateCell> setDatePickerStyle() {
        return picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    if (item.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        getStyleClass().add("sunday");
                    }
                    if (item.equals(LocalDate.now())) {
                        getStyleClass().add("today");
                    }
                }
            }
        };
    }

    private void saveEditedEvent() {
        if (editingEvent == null) return;

        // 1) Собираем новые значения из формы
        String newDesc = txtAreaEventDescrp.getText().trim();

        LocalDate createLd = dtpkCreationDate.getValue();
        EventDate newCrt = new EventDate(
                createLd.getDayOfMonth(),
                createLd.getMonthValue(),
                createLd.getYear()
        );

        int newMark = spinnerMark.getValue();

        // expiration date: по DatePicker'у, если он виден, иначе по спиннеру+комбо
        LocalDate newExpLd;
        if (dtpkExpirationDate.isVisible() && dtpkExpirationDate.getValue() != null) {
            newExpLd = dtpkExpirationDate.getValue();
        } else {
            int count = spinnerExpTimeCount.getValue();
            String unit = comBoxExpiredTime.getSelectionModel().getSelectedItem();
            switch (unit) {
                case "Month (s)" -> newExpLd = createLd.plusMonths(count);
                case "Week (s)"  -> newExpLd = createLd.plusWeeks(count);
                default          -> newExpLd = createLd.plusDays(count);
            }
        }
        EventDate newExp = new EventDate(
                newExpLd.getDayOfMonth(),
                newExpLd.getMonthValue(),
                newExpLd.getYear()
        );

        // 2) Создаём копию события с новыми данными
        StudentEvent editedCopy = new StudentEvent(
                editingEvent.getID(),
                newCrt,
                newDesc,
                newMark,
                newExp
        );

        // 3) Выполняем команду редактирования
        EditEventCommand cmd = new EditEventCommand(
                getCurrentGroup(),
                getSelectedStudent(),
                editingEvent,
                editedCopy,
                this
        );
        cmd.execute(false);
        addCommand(cmd);

        // 4) Сбрасываем форму и скрываем панель
        stopEditing();
        editingEvent = null;
        hideMainTopPane(false);
    }


    /**
     * Заполняет spinner + combo и блокирует/разблокирует их
     */
    private void applyPresetToFields(EventMods mod) {
        boolean isCustom = (mod == EventMods.OTHER);
        EventPreset preset = mod.getPreset();

        // Устанавливаем оценку
        spinnerMark.getValueFactory().setValue(preset.getMark());

        // Получаем правило
        ExpirationRule rule = preset.getRule();
        int count = rule.getCount();
        DateType unit = rule.getUnit();

        // Устанавливаем значения в spinner + combo
        spinnerExpTimeCount.getValueFactory().setValue(count);
        switch (unit) {
            case DAY -> comBoxExpiredTime.getSelectionModel().select("Day (s)");
            case WEEK -> comBoxExpiredTime.getSelectionModel().select("Week (s)");
            case MONTH -> comBoxExpiredTime.getSelectionModel().select("Month (s)");
        }

        // Устанавливаем или сбрасываем дату истечения
        if (isCustom) {
            dtpkExpirationDate.setValue(null);
        } else {
            LocalDate creation = dtpkCreationDate.getValue();
            if (creation != null) {
                LocalDate expiration = rule.apply(creation);
                dtpkExpirationDate.setValue(expiration);
            }
            if (showingDatePicker) {
                toggleExpirationInput(); // Анимация возврата
            }
        }

        // Включаем/отключаем поля (все, кроме даты создания)
        spinnerMark.setDisable(!isCustom);
        spinnerExpTimeCount.setDisable(!isCustom);
        comBoxExpiredTime.setDisable(!isCustom);
        dtpkExpirationDate.setDisable(!isCustom);
    }

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
        if (student == null) return;

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
        // 2. Обновляем статус событий
        LocalDate now = LocalDate.now();
        for (StudentEvent event : student.getEvents()) {
            EventDate exp = event.getExpDate();
            LocalDate expDate = LocalDate.of(exp.getYear(), exp.getMonth(), exp.getDay());
            event.setExpired(expDate.isBefore(now));
        }

        // 3. Загружаем события в таблицу
        eventsTable.getItems().setAll(student.getEvents());
        statusColumn.setSortType(TableColumn.SortType.ASCENDING); // направление сортировки
        eventsTable.sort(); // применить сортировку
    }       // ЗАГРУЗКА ИВЕНТОВ ДЛЯ СТУДЕНТА

    // Утилитный метод для установки стиля выбранного студента
    private void setSelectedStyle(Button btn) {
        btn.setStyle("""
                        -fx-background-color: rgba(0,0,0,0.6);
                        -fx-border-color: rgba(255,255,255,0.6);
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
            NotificationFX notificationFX = new NotificationFX(NotificationType.WARNING,
                    "Choose the group",
                    "",
                    NotificationPosition.TOP,
                    getVBoxRight());
            notificationFX.show();
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
            showWarning("Choose the student");
            return;
        }

        LocalDate creationDateRaw = dtpkCreationDate.getValue();
        if (creationDateRaw == null) {
            showWarning("Select creation date");
            return;
        }

        String description = txtAreaEventDescrp.getText().trim();
        if (description.isEmpty()) {
            showWarning("Enter a description of the event");
            return;
        }

        String selectedDesc = comBoxEventPreset.getSelectionModel().getSelectedItem();
        EventMods selectedMod = findModByName(selectedDesc);

        int mark;
        LocalDate expirationDate;

        if (selectedMod == EventMods.OTHER) {
            // Проверки заполненности полей
            if (spinnerMark.getValue() == null || spinnerExpTimeCount.getValue() == null ||
                    comBoxExpiredTime.getSelectionModel().getSelectedItem() == null ||
                    (!showingDatePicker && dtpkExpirationDate.getValue() == null)) {
                showWarning("Fill in all fields for the custom event");
                return;
            }

            mark = spinnerMark.getValue();

            if (showingDatePicker) {
                expirationDate = dtpkExpirationDate.getValue();
            } else {
                ExpirationRule rule = getCustomExpirationRule();
                expirationDate = rule.apply(creationDateRaw);
            }

        } else {
            mark = selectedMod.getPreset().getMark();
            expirationDate = selectedMod.getPreset().getRule().apply(creationDateRaw);
        }

        EventDate creationDate = new EventDate(creationDateRaw.getDayOfMonth(), creationDateRaw.getMonthValue(), creationDateRaw.getYear());
        EventDate expDate = new EventDate(expirationDate.getDayOfMonth(), expirationDate.getMonthValue(), expirationDate.getYear());

        int eventID = genUniqueEventId();
        StudentEvent newEvent = new StudentEvent(eventID, creationDate, description, mark, expDate);

        Command cmd = new AddEventCommand(currentGroup, selectedStudent, newEvent, this);
        cmd.execute(false);
        addCommand(cmd);
    }

    private ExpirationRule getCustomExpirationRule() {
        int count = spinnerExpTimeCount.getValue();
        String unitText = comBoxExpiredTime.getSelectionModel().getSelectedItem();

        return switch (unitText) {
            case "Day (s)" -> new ExpirationRule(count, DateType.DAY);
            case "Week (s)" -> new ExpirationRule(count, DateType.WEEK);
            case "Month (s)" -> new ExpirationRule(count, DateType.MONTH);
            default -> new ExpirationRule(0, DateType.DAY); // fallback
        };
    }

    private void showWarning(String message) {
        new NotificationFX(NotificationType.WARNING, message, "", NotificationPosition.TOP, getVBoxRight()).show();
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


    // === toggleExpirationInput ===
    @FXML private HBox hboxExpiredDate;
    @FXML private DatePicker dtpkExpirationDate;

    private boolean showingDatePicker = false;
    private boolean animationInProgress = false;
    private static final double ANIMATION_SLIDE_OFFSET = 20.0;

    @FXML
    private void toggleExpirationInput() {
        if (animationInProgress) return;
        animationInProgress = true;

        LocalDate creation = dtpkCreationDate.getValue();
        if (!showingDatePicker) {
            // 1) Считаем из spinner+combo дни до expiration
            if (creation != null) {
                int count = spinnerExpTimeCount.getValue();
                String unit = comBoxExpiredTime.getValue();
                long days = count;
                if ("Week (s)".equals(unit))   days = count * 7L;
                else if ("Month (s)".equals(unit)) days = count * 30L;
                dtpkExpirationDate.setValue(creation.plusDays(days));
            }

            // 2) Анимация hide HBox → show DatePicker
            TranslateTransition out1 = new TranslateTransition(Duration.millis(250), hboxExpiredDate);
            out1.setToX(-ANIMATION_SLIDE_OFFSET);
            FadeTransition f1 = new FadeTransition(Duration.millis(250), hboxExpiredDate);
            f1.setToValue(0);
            ParallelTransition hideOld = new ParallelTransition(out1, f1);
            hideOld.setOnFinished(e -> {
                hboxExpiredDate.setVisible(false);
                hboxExpiredDate.setManaged(false);

                dtpkExpirationDate.setVisible(true);
                dtpkExpirationDate.setManaged(true);
                dtpkExpirationDate.setOpacity(0);
                dtpkExpirationDate.setTranslateX(ANIMATION_SLIDE_OFFSET);

                TranslateTransition in1 = new TranslateTransition(Duration.millis(250), dtpkExpirationDate);
                in1.setToX(0);
                FadeTransition f2 = new FadeTransition(Duration.millis(250), dtpkExpirationDate);
                f2.setToValue(1);
                ParallelTransition showNew = new ParallelTransition(in1, f2);
                showNew.setOnFinished(ev -> animationInProgress = false);
                showNew.play();
            });
            hideOld.play();

        } else {
            // 1) Считаем дни между creation и expiration
            LocalDate expire   = dtpkExpirationDate.getValue();
            if (creation != null && expire != null) {
                long days = ChronoUnit.DAYS.between(creation, expire);
                spinnerExpTimeCount.getValueFactory().setValue((int) days);
            }
            // всегда показываем дни
            comBoxExpiredTime.getSelectionModel().select("Day (s)");

            // 2) Анимация hide DatePicker → show HBox
            TranslateTransition out2 = new TranslateTransition(Duration.millis(250), dtpkExpirationDate);
            out2.setToX(ANIMATION_SLIDE_OFFSET);
            FadeTransition f3 = new FadeTransition(Duration.millis(250), dtpkExpirationDate);
            f3.setToValue(0);
            ParallelTransition hideOld = new ParallelTransition(out2, f3);
            hideOld.setOnFinished(e -> {
                dtpkExpirationDate.setVisible(false);
                dtpkExpirationDate.setManaged(false);

                hboxExpiredDate.setVisible(true);
                hboxExpiredDate.setManaged(true);
                hboxExpiredDate.setOpacity(0);
                hboxExpiredDate.setTranslateX(-ANIMATION_SLIDE_OFFSET);

                TranslateTransition in2 = new TranslateTransition(Duration.millis(250), hboxExpiredDate);
                in2.setToX(0);
                FadeTransition f4 = new FadeTransition(Duration.millis(250), hboxExpiredDate);
                f4.setToValue(1);
                ParallelTransition showNew = new ParallelTransition(in2, f4);
                showNew.setOnFinished(ev -> animationInProgress = false);
                showNew.play();
            });
            hideOld.play();
        }

        showingDatePicker = !showingDatePicker;
    }

    // =============================




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
    /*public int genUniqueStudentId() {
        Set<Integer> existingIds = new HashSet<>();

        for (Group group : helper.getCourse()) {
            for (Student student : group.getStudents()) {
                existingIds.add(student.getID());
            }
        }

        int newId;
        do {
            newId = 1_000_000 + (int) (Math.random() * 9_000_000);
        } while (existingIds.contains(newId));

        return newId;
    }*/
    public int genUniqueStudentId() {
        Set<Integer> existingIds = new HashSet<>();

        for (Group group : helper.getCourse()) {
            for (Student student : group.getStudents()) {
                existingIds.add(student.getID());
            }
        }

        int newId = 1_000_000;
        while (existingIds.contains(newId)) {
            newId++;
        }

        return newId;
    }

    // Генерация уникального 5-значного ID ивента
    /*public int genUniqueEventId() {
        Set<Integer> existingIds = new HashSet<>();

        for (Group group : helper.getCourse()) {
            for (Student student : group.getStudents()) {
                for (StudentEvent event : student.getEvents()) {
                    existingIds.add(event.getID());
                }
            }
        }

        int newId;
        do {
            newId = 10_000 + (int) (Math.random() * 90_000);
        } while (existingIds.contains(newId));

        return newId;
    }*/
    public int genUniqueEventId() {
        Set<Integer> existingIds = new HashSet<>();

        for (Group group : helper.getCourse()) {
            for (Student student : group.getStudents()) {
                for (StudentEvent event : student.getEvents()) {
                    existingIds.add(event.getID());
                }
            }
        }

        int newId = 10_000;
        while (existingIds.contains(newId)) {
            newId++;
        }

        return newId;
    }

    // Поиск ивента по его названию и списка пресетов
    public static EventMods findModByName(String name) {
        for (EventMods mod : EventMods.values()) {
            if (mod.getPreset().getName().equals(name)) {
                return mod;
            }
        }
        return EventMods.OTHER;
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
                AlertFX_type.WARNING,
                true,
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

    public void initAfterStageShown(Stage stage, String courseName, File courseFile) {
        new Thread(() -> {
            String new_version = UpdateUtility.checkForUpdates(stage.getScene().getWindow());

            Platform.runLater(() -> {
                if (!new_version.equals("-1")) {
                    UpdateUtility.showUpdateDialog(stage);
                    HistoryUtility.setHistory(
                            richTxtPaneHistory,
                            lblCurHistory,
                            HistoryUtility.Types.INFO,
                            "New version " + new_version + " is available"
                    );
                } else {
                    HistoryUtility.setHistory(
                            richTxtPaneHistory,
                            lblCurHistory,
                            HistoryUtility.Types.INFO,
                            "No updates found"
                    );
                }

                // 💡 Спрашиваем пароль только после обновлений:
                if (getPassword() == null) {
                    String password = CM_HELPER.showCheckPasswordDialog(stage.getScene().getWindow(), courseFile);
                    if (password == null) {
                        Platform.exit();
                        return;
                    }

                    CM_HELPER.setPassword(password);
                }

                Task<Void> loadTask = getLoadDataTask(courseName, courseFile, this, getPassword());
                new Thread(loadTask).start();
            });
        }).start();
    }
    private static final int LOADING_DELAY = 1000;
    // Утилитный метод
    private Task<Void> getLoadDataTask(String courseName, File courseFile, Main_controller mainController, String password) {
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.setPrefHeight(40);
        HBox progressContainer = new HBox(progressBar);
        progressContainer.setAlignment(Pos.CENTER);      // ← выравнивание по центру
        progressContainer.setPadding(new Insets(0, 0, 140, 0)); // ← отступ снизу

        mainController.getNotificationPane().setBottom(progressContainer);

        Task<Void> loadDataTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int totalSteps = 10;
                long stepDelay = LOADING_DELAY / totalSteps;
                for (int i = 0; i < totalSteps; i++) {
                    Thread.sleep(stepDelay);
                    updateProgress(i + 1, totalSteps);
                }
                // Выполнение загрузки данных (убери sleep, если задержка уже учтена)
                loadData(mainController, courseName, courseFile, password);
                return null;
            }
        };

        progressBar.progressProperty().bind(loadDataTask.progressProperty());

        loadDataTask.setOnSucceeded(event -> {
            mainController.getNotificationPane().setBottom(null);
            System.out.println("Data loaded successfully");
        });

        return loadDataTask;
    }
    // Утилитный метод для загрузки данных
    private void loadData(Main_controller mainController, String courseName, File courseFile, String password) {
        try {
            System.out.println("Data loading started...");

            //CM_HELPER helper = new CM_HELPER();
            //helper.setCourse(BinaryCmanParser.parse(courseFile));
            CM_HELPER.setCourse(CmanSecurityParser.parse(courseFile, password));
            CM_HELPER.setCourseName(courseName);

            javafx.application.Platform.runLater(() -> {
                //mainController.init(helper);
                initializeTabs(CM_HELPER.getCourse());
                System.out.println("=== DATA LOADING COMPLETED SUCCESSFULLY ===");
            });
        } catch (IOException e) {
            System.err.println("=== FATAL ERROR OF DATA LOADING ===");
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            //BinaryCmanSaver.save(helper.getCourse(), new File(filePath));
            CmanSecuritySaver.save(helper.getCourse(), new File(filePath), CM_HELPER.getPassword());
            undoStack.clear();
            redoStack.clear();
            //Files.write(helper.TEMP_FILE.toPath(), new byte[0]);

            NotificationFX alert = new NotificationFX(NotificationType.SUCCESS, "Saved successfully", "",
                    NotificationPosition.TOP, vboxRightAnimated);
            alert.show();
            HistoryUtility.setHistory(richTxtPaneHistory,
                    lblCurHistory,
                    HistoryUtility.Types.SUCCESS,
                    "Saved successfully");
            System.out.println("Saved successfully");

        } catch (IOException ex) {
            showConfirmDialog(mainPanel.getScene().getWindow(),
                                AlertFX_type.ERROR,
                                false,
                                "Saving Error:",
                                ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @FXML private void miNew() {
        NewCourseDialog_controller res = showNewCourseDialog(stage);
        if (res == null || res.getCourseName() == null) return;   // Cancel

        String newName = res.getCourseName();
        File   newFile = res.getNewCourseFile();

        actionClose(stage, () -> {
            try {
                openMainWindow(newName, newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    @FXML private void miOpen() {
        File file = CM_HELPER.showOpenCourseDialog(stage);
        if (file == null) return;           // Cancel

        String newName = file.getName().replace(".cman", "");//file.getName().replaceFirst("\\.cman$", "");

        actionClose(stage, () -> {
            try {
                CM_HELPER.openMainWindow(newName, file);
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
    @FXML
    private void setBoldText() {
        toggleStyleAroundSelection("*");
    }

    @FXML
    private void setItalicText() {
        toggleStyleAroundSelection("_");
    }

    @FXML
    private void setUnderlineText() {
        toggleStyleAroundSelection("=");
    }

    // === CORE LOGIC ===
    private void toggleStyleAroundSelection(String marker) {
        String text = txtAreaEventDescrp.getText();
        int start = txtAreaEventDescrp.getSelection().getStart();
        int end = txtAreaEventDescrp.getSelection().getEnd();

        if (start == end) return; // ничего не выделено

        String selected = text.substring(start, end);

        int before = start - marker.length();
        int after = end + marker.length();

        boolean hasBefore = before >= 0 && text.substring(before, start).equals(marker);
        boolean hasAfter = after <= text.length() && text.substring(end, after).equals(marker);

        String newText;
        int caretShift = marker.length();

        if (hasBefore && hasAfter) {
            // Уже есть — убираем маркеры
            newText = text.substring(0, before) + selected + text.substring(after);
            txtAreaEventDescrp.setText(newText);
            txtAreaEventDescrp.selectRange(before, before + selected.length());
        } else {
            // Нет — добавляем маркеры
            newText = text.substring(0, start) + marker + selected + marker + text.substring(end);
            txtAreaEventDescrp.setText(newText);
            txtAreaEventDescrp.selectRange(start + marker.length(), end + marker.length());
        }
    }


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