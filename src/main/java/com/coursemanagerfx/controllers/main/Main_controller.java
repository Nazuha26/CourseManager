package com.coursemanagerfx.controllers.main;

import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.Launcher;
import com.coursemanagerfx.animations.WindowOutAnimation;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.coursemanagerfx.controllers.dialogs.exceptions.SaveException;
import com.coursemanagerfx.custom_ui.ProgressSpinner;
import com.coursemanagerfx.logic.Actions;
import com.coursemanagerfx.logic.basic.*;
import com.coursemanagerfx.custom_ui.GradientBackground;
import com.coursemanagerfx.logic.basic.event.EventTypes;
import com.coursemanagerfx.logic.basic.event.EventStatus;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.date.ExpDateStrings;
import com.coursemanagerfx.logic.commands.*;
import com.coursemanagerfx.logic.commands.student_comms.AddStudentCommand;
import com.coursemanagerfx.logic.security.CmanSecurityUtility;
import com.coursemanagerfx.logic.utilities.ExcelExportUtility;
import com.coursemanagerfx.logic.utilities.HistoryUtility;
import com.coursemanagerfx.logic.utilities.UpdateUtility;
import com.coursemanagerfx.logic.utilities.show.ShowDialogUtility;
import eu.iamgio.animated.transition.AnimationPair;
import eu.iamgio.animated.transition.container.AnimatedVBox;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.*;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.coursemanagerfx.CM_HELPER.*;

enum TopPaneMode { HISTORY, EVENT_INFO }

public class Main_controller implements StageAttachable {

    public static final int MW_ANIM_STRIPE_COUNT = 20;  // start window

    // ========== FXML ==========

    @FXML private BorderPane rootPane;
    @FXML private BorderPane titleBar;

    @FXML private BorderPane notificationPane;
    public BorderPane getNotificationPane() {
        return notificationPane;
    }

    @FXML private BorderPane eventInfoTopPane;
    @FXML private StackPane historyTopPane;
    @FXML private TextField txtFieldSearch;
    @FXML private TextArea txtPaneHistory;
    @FXML private TextFlow txtFlowHistory;
    @FXML private InlineCssTextArea richTxtPaneHistory;
    @FXML private Label lblCurHistory;

    @FXML private Button btnHistory;
    @FXML private Button btnBackFromHistory;

    @FXML private Label lblAppName;

    @FXML private HBox tabHBox;
    @FXML private VBox studentVBox;
    @FXML private ScrollPane studentsScrollPane;
    @FXML private TextArea txtAreaEventDescrp;
    @FXML private ComboBox<String> comBoxEventType;
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
    @FXML private TableView<StudentEvent> eventsTable;
    @FXML private TableColumn<StudentEvent, Number> numberColumn;
    @FXML private TableColumn<StudentEvent, String> crtDateColumn;
    @FXML private TableColumn<StudentEvent, String> descriptionColumn;
    @FXML private TableColumn<StudentEvent, Number> marksColumn;
    @FXML private TableColumn<StudentEvent, String> expDateColumn;
    @FXML private TableColumn<StudentEvent, EventStatus> statusColumn;

    @FXML private HBox addEventBottomPane;
    @FXML private StackPane mainTopPane;
    @FXML private StackPane tableStackPane;
    @FXML private Label lblEditing;
    @FXML private Label lblGroupNumber;
    @FXML private HBox hboxExpiredDate;
    @FXML private DatePicker dtpkExpirationDate;

    // ========== FXML ==========

    // ========== FXML GETTERS/SETTERS ==========

    // ===== GETTERS =====
    public TextArea getTxtPaneHistory() {
        return txtPaneHistory;
    }
    public TextFlow getTxtFlowHistory() { return txtFlowHistory; }
    public InlineCssTextArea getRichTxtPaneHistory() {
        return richTxtPaneHistory;
    }
    public Label getLblCurHistory() {
        return lblCurHistory;
    }
    public Label getLblAppName() {
        return lblAppName;
    }
    public VBox getStudentVBox() {
        return studentVBox;
    }
    public HBox getTabHBox() {
        return tabHBox;
    }
    public TableView<StudentEvent> getEventsTable() {
        return eventsTable;
    }
    public TableColumn<StudentEvent, EventStatus> getStatusColumn() {
        return statusColumn;
    }
    public TextField getTxtFieldSearch() {
        return txtFieldSearch;
    }
    public Button getBtnCancelEvent() {
        return btnCancelEvent;
    }
    public ComboBox<String> getComBoxEventType() {
        return comBoxEventType;
    }
    public TextArea getTxtAreaEventDescrp() {
        return txtAreaEventDescrp;
    }
    public Label getLblEditing() {
        return lblEditing;
    }
    public Button getBtnDeleteEvent() {
        return btnDeleteEvent;
    }
    public Button getBtnCreateEvent() {
        return btnCreateEvent;
    }
    public StackPane getMainTopPane() {
        return mainTopPane;
    }
    public DatePicker getDtpkCreationDate() {
        return dtpkCreationDate;
    }
    public DatePicker getDtpkExpirationDate() {
        return dtpkExpirationDate;
    }
    public Spinner<Integer> getSpinnerMark() {
        return spinnerMark;
    }
    public Spinner<Integer> getSpinnerExpTimeCount() {
        return spinnerExpTimeCount;
    }
    public ComboBox<String> getComBoxExpiredTime() {
        return comBoxExpiredTime;
    }
    public Button getBtnAddStudent() {
        return btnAddStudent;
    }
    public HBox getAddEventBottomPane() {
        return addEventBottomPane;
    }
    public StackPane getTableStackPane() {
        return tableStackPane;
    }
    public HBox getHboxExpiredDate() {
        return hboxExpiredDate;
    }
    public StackPane getHistoryTopPane() {
        return historyTopPane;
    }
    public BorderPane getEventInfoTopPane() {
        return eventInfoTopPane;
    }

    public Label getLblGroupNumber() {
        return lblGroupNumber;
    }

    public BorderPane getTitleBar() {
        return titleBar;
    }
    public BorderPane getRootPane() {
        return rootPane;
    }
    // ===== GETTERS =====

    // ========== FXML GETTERS/SETTERS ==========



    // ========== FIELDS ==========

    private ImageView historyIcon_To;
    private ImageView historyIcon_Back;
    private AnimatedVBox vboxLeftAnimated;
    private AnimatedVBox vboxRightAnimated;
    private Stage stage;
    //private Group currentGroup;
    private Button activeEditButton = null;

    // ========== FIELDS ==========



    // ========== FIELDS GETTERS/SETTERS ==========

    // ===== GETTERS =====
    public AnimatedVBox getVBoxLeft() {
        return vboxLeftAnimated;
    }
    public AnimatedVBox getVBoxRight() {
        return vboxRightAnimated;
    }

    public Stage getStage() {
        return stage;
    }

    // ===== GETTERS =====

    // ===== SETTERS =====
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    // ===== SETTERS =====

    // ========== FIELDS GETTERS/SETTERS ==========



    // ========== UTILITY METHODS ==========


    // init data picker custom style
    public static Callback<DatePicker, DateCell> addDatePickerCSS() {
        return picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("odd-month", "even-month", "sunday", "today");
                if (!empty && item != null) {
                    if (item.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        getStyleClass().add("sunday");
                    }
                    if (item.equals(LocalDate.now())) {
                        getStyleClass().add("today");
                    }
                    if (item.getMonthValue() % 2 == 1) {
                        getStyleClass().add("odd-month");
                    }
                }
            }
        };
    }

    // init data picker custom format
    public static void applyDateFormat(DatePicker datePicker, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        datePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty())
                        ? LocalDate.parse(string, formatter)
                        : null;
            }
        });
    }



    @FXML
    public void initialize() {
        new GradientBackground(rootPane, 0.005, 2); // gradient bg
        Actions.getInstance().setController(this);

        // === NOTIFICATIONS ===
        vboxLeftAnimated = new AnimatedVBox(new AnimationPair(new animatefx.animation.FadeInLeft(), new animatefx.animation.FadeOutLeft()).setSpeed(3, 3));
        vboxLeftAnimated.setPrefWidth(200);
        vboxLeftAnimated.setSpacing(5);

        vboxRightAnimated = new AnimatedVBox(new AnimationPair(new animatefx.animation.FadeInRight(), new animatefx.animation.FadeOutRight()).setSpeed(3, 3));
        vboxRightAnimated.setPrefWidth(200);
        vboxRightAnimated.setSpacing(5);

        //notificationPane.setLeft(vboxLeftAnimated);       // slide notifications
        //notificationPane.setRight(vboxRightAnimated);     // slide notifications
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
            Group selectedGroup = Actions.getInstance().select().getSelectedGroup();
            if (selectedGroup != null) {
                List<Student> visibleStudents = Actions.getInstance().repaint().repaintStudentPanels(selectedGroup);

                if (!visibleStudents.isEmpty())
                    Actions.getInstance().select().selectStudentPanel(visibleStudents.getFirst(), selectedGroup);
                else
                    Actions.getInstance().select().selectStudentPanel(null, selectedGroup);
            }
        });
        // ======================================

        dtpkCreationDate.setDayCellFactory(addDatePickerCSS());
        dtpkExpirationDate.setDayCellFactory(addDatePickerCSS());
        applyDateFormat(dtpkCreationDate, "dd.MM.yyyy");
        applyDateFormat(dtpkExpirationDate, "dd.MM.yyyy");


        // выбираем при инициализации первую опцию days/weeks/month
        /* INIT EXP DATE COMBOBOX */
        comBoxExpiredTime.setItems(FXCollections.observableArrayList(
                ExpDateStrings.DAYS,
                ExpDateStrings.WEEKS,
                ExpDateStrings.MONTHS
        ));
        //if (!comBoxExpiredTime.getItems().isEmpty())
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


        // === INIT MODS COMBOBOX ===
        for (EventTypes type : EventTypes.values())
            comBoxEventType.getItems().add(type.getEventType().name());
        comBoxEventType.getSelectionModel().selectFirst();    // select first type by default

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
                        StudentEvent event = getTableRow().getItem();
                        if (event == null) return;
                        if (Actions.getInstance().formAnims().isAnimPlaysFlag()) return;

                        ObservableList<String> styleClass = btnEdit.getStyleClass();

                        if (Actions.getInstance().uiActions().getEditingEvent() == event) {
                            Actions.getInstance().uiActions().stopEditing();

                            styleClass.removeAll("edit-button", "edit-button-selected");
                            styleClass.add("edit-button");

                            activeEditButton = null;
                        } else {
                            if (activeEditButton != null) {
                                activeEditButton.getStyleClass().removeAll("edit-button", "edit-button-selected");
                                activeEditButton.getStyleClass().add("edit-button");
                            }

                            Actions.getInstance().uiActions().startEditing(event);

                            styleClass.removeAll("edit-button", "edit-button-selected");
                            styleClass.add("edit-button-selected");

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

    }   // MAIN INITIALIZATION


    // сделано
    @FXML private void btnAddStudent() {
        Window owner = stage.getScene().getWindow();

        Group selectedGroup = Actions.getInstance().select().getSelectedGroup();

        if (selectedGroup == null) {
            AlertFX.showConfirm(owner,
                    AlertFX_type.WARNING,
                    "Group not selected",
                    "Choose the group please.");
            return;
        }

        String rawStudentName = ShowDialogUtility.showInputDialog(owner,
                                    "Add student",
                                    "Enter the student's name:",
                                    "");

        if (rawStudentName == null) return;     // pressed cancel

        String studentName = rawStudentName.trim().replaceAll("\\s+", " ");
        if (studentName.split(" ").length != 3) {
            AlertFX.showConfirm(owner,
                    AlertFX_type.WARNING,
                    "Invalid input",
                    "Enter full name in format: Firstname Lastname Patronymic");
            return;
        }

        int studentID = Actions.getInstance().getIdGenerator().genUniqueStudentId();
        Student newStudent = new Student(studentName, studentID);

        Command cmd = new AddStudentCommand(selectedGroup, newStudent);
        cmd.execute();
        Actions.getInstance().undoRedo().addCommand(cmd);
    }

    // сделано
    @FXML
    private void btnAddEvent() {
        Actions.getInstance().formAnims().loadEventInfoPane();
        Actions.getInstance().formAnims().mainTopPanelInOut(Actions.FormAnims.State.SHOW);
    }

    // сделано
    @FXML
    private void btnCancelEvent()
        { Actions.getInstance().formAnims().mainTopPanelInOut(Actions.FormAnims.State.HIDE); }

    // сделано
    @FXML
    private void btnCreateEvent()
        { Actions.getInstance().uiActions().createEventAction(); }

    // сделано
    @FXML
    private void btnHistory() {
        Actions.getInstance().formAnims().loadHistoryPane();
        Actions.getInstance().formAnims().mainTopPanelInOut(Actions.FormAnims.State.SHOW);
    }

    // сделано
    @FXML
    private void btnBackFromHistory()
        { Actions.getInstance().formAnims().mainTopPanelInOut(Actions.FormAnims.State.HIDE); }

    // сделано
    @FXML
    private void btnClearHistory() {
        richTxtPaneHistory.clear();
        lblCurHistory.setText("");
    }

    // сделано
    @FXML
    private void toggleExpirationInput() {
        if (Actions.getInstance().formAnims().isToggleAnimPlaysFlag()) return;
        Actions.getInstance().uiActions().toggleExpInputAction();
    }


    // ==== СДЕЛАТЬ
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

                // Спрашиваем пароль только после обновлений:
                if (getPassword() == null) {
                    String password = "";//CM_HELPER.showCheckPasswordDialog(stage.getScene().getWindow(), courseFile);

                    CM_HELPER.setPassword(password);
                }


            });
        }).start();
    }

    private static final int LOADING_DELAY = 5000;

    public void dataLoaderTask() {
        /* task for loading */
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int totalSteps = 10;
                long baseDelay = LOADING_DELAY / totalSteps;


                return null;
            }
        };

        ProgressSpinner progressSpinner = new ProgressSpinner();
        progressSpinner.setFont(new Font("Roboto", 32));
        progressSpinner.progressProperty().bind(loadTask.progressProperty());

        StackPane root = new StackPane(progressSpinner);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: transparent"); // transparent

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // Модальное окно
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(getStage());
        dialog.setScene(scene);
        dialog.setAlwaysOnTop(true);

        /* === centering === */
        dialog.setOnShown(ev -> {
            Window owner = dialog.getOwner();
            if (owner != null) {
                dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
                dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
            }
        });
        /* ================= */

        // При завершении задачи
        loadTask.setOnSucceeded(e -> {
            dialog.close();
            Actions.getInstance().repaint().initGroupTabs();
            System.out.println("=== DATA LOADING COMPLETED SUCCESSFULLY ===");
        });

        loadTask.setOnFailed(e -> {
            dialog.close();
            System.err.println("DATA LOADING FAILED");
            e.getSource().getException().printStackTrace();
        });

        // Запуск
        new Thread(loadTask).start();
        dialog.show();
    }   // УБРАТЬ

    public Stage createLoadingDialog(Task<?> task) {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(250);
        progressBar.setPrefHeight(30);
        progressBar.progressProperty().bind(task.progressProperty());

        VBox root = new VBox(progressBar);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT); // прозрачный фон

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);           // без рамки
        dialog.initModality(Modality.APPLICATION_MODAL);    // модальное
        dialog.initOwner(this.getStage());                  // главное окно
        dialog.setScene(scene);
        dialog.setAlwaysOnTop(true);

        return dialog;
    }


    // ==== СДЕЛАТЬ
    private void filterStudents(String query) {
        Group selectedGroup = Actions.getInstance().select().getSelectedGroup();

        List<Student> filteredStudents = new ArrayList<>();
        for (Student student : selectedGroup.getStudents()) {
            if (student.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredStudents.add(student);
            }
        }
        filteredStudents.sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));

        studentVBox.getChildren().clear();

        Student selectedStudent = Actions.getInstance().select().getSelectedStudent();

        /*if (filteredStudents.isEmpty()) {
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
                    spc.setCtrl(this);

                    Button studentButton = spc.getBtnStudent();
                    studentButton.setText(student.getName());
                    studentButton.setOnAction(e -> {
                        // Сбрасываем выделение у всех студентов
                        for (Student s : currentGroup.getStudents()) {
                            s.setSelected(false);
                        }
                        studentVBox.getChildren().forEach(node -> {
                            if (node instanceof BorderPane bp) {
                                Button btn = findStudentBtn(bp.getCenter());
                                if (btn != null) btn.setStyle("");
                            }
                        });
                        setSelectedStyle(studentButton);
                        student.setSelected(true);
                        loadStudentEvents(student.getStudentID());
                    });
                    studentButton.setUserData(student);

                    Label studentNumber = (Label) studentPanel.getLeft();
                    studentNumber.setText(String.valueOf(number));

                    // Если этот студент ранее был выбран, то выделяем его
                    if (selectedStudent != null && student.getStudentID() == selectedStudent.getStudentID()) {
                        setSelectedStyle(studentButton);
                    }

                    studentVBox.getChildren().add(studentPanel);
                    number++;
                } catch (IOException e) {
                    System.err.println("Error loading student_panel.fxml: " + e.getMessage());
                }
            }
        }*/
    }

    // ========== TITLE BAR ==========

    // ===== MENU BAR =====
    // === FILE ===
    @FXML private void miHome()
        { Actions.getInstance().menuActions().toHomeAction(); }
    @FXML private void miSave()
        { Actions.getInstance().menuActions().saveAction(); }
    @FXML private void miExport()
        { Actions.getInstance().menuActions().exportAction(); }
    @FXML private void miQuit()
        { Actions.getInstance().uiActions().mainExitAction(); }

    // === EDIT ===
    @FXML private void miUndo()
        { Actions.getInstance().undoRedo().undo(); }
    @FXML private void miRedo()
        { Actions.getInstance().undoRedo().redo(); }
    @FXML private void miOptions()
        { Actions.getInstance().menuActions().optionsAction(); }

    // === HELP ===
    @FXML private void miAbout()
        { ShowDialogUtility.showAboutWindow(stage.getScene().getWindow()); }
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

        if (start == end) return;

        String selected = text.substring(start, end);

        int before = start - marker.length();
        int after = end + marker.length();

        boolean hasBefore = before >= 0 && text.substring(before, start).equals(marker);
        boolean hasAfter = after <= text.length() && text.substring(end, after).equals(marker);

        String newText;
        int newStart, newEnd;

        if (hasBefore && hasAfter) {
            /* removing markers */
            newText = text.substring(0, before) + selected + text.substring(after);
            newStart = before;
            newEnd = before + selected.length();
        } else {
            /* adding markers */
            newText = text.substring(0, start) + marker + selected + marker + text.substring(end);
            newStart = start + marker.length();
            newEnd = end + marker.length();
        }

        txtAreaEventDescrp.setText(newText);
        txtAreaEventDescrp.selectRange(newStart, newEnd);
        txtAreaEventDescrp.positionCaret(newEnd);
    }


    // === КНОПКА "ЗАКРЫТЬ" ===
    @FXML private void windowClose()
        { Actions.getInstance().uiActions().mainExitAction(); }
    // ========================

    // === УМЕНЬШИТЬ ОКНО ===
    @FXML private void windowMinimize()
        { stage.setIconified(true); }
    // ===============================
}