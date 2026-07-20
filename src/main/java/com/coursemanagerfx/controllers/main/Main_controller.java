/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.controllers.main;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertMessageType;
import com.coursemanagerfx.logic.Actions;
import com.coursemanagerfx.logic.basic.*;
import com.coursemanagerfx.custom_ui.GradientBackground;
import com.coursemanagerfx.logic.basic.event.category.EventCategories;
import com.coursemanagerfx.logic.basic.event.EventStatus;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.category.EventCategory;
import com.coursemanagerfx.logic.basic.event.date.ExpDateStrings;
import com.coursemanagerfx.logic.commands.*;
import com.coursemanagerfx.logic.commands.student_comms.AddStudentCommand;
import com.coursemanagerfx.logic.config_api.ConfigManager;
import com.coursemanagerfx.logic.utilities.view.ShowDialogUtility;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.*;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.fxmisc.richtext.InlineCssTextArea;

import java.text.DecimalFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main_controller implements StageAttachable {

    /* === CONSTANTS === */
    public static final int MW_ANIM_STRIPE_COUNT = 20;  // const for anim main window

    private static final String BOLD_MARKER = "*";
    private static final String ITALIC_MARKER = "_";
    private static final String UNDERLINE_MARKER = "=";

    /* ==================== FXML ==================== */
    @FXML private BorderPane rootPane;      // root
    @FXML private BorderPane titleBar;      // title bar
    @FXML private Label lblAppName;         // app name
    @FXML private HBox tabHBox;
    @FXML private Button btnClose;
    @FXML private Button btnMinimize;

    /* --- info top panel --- */
    @FXML private StackPane infoTopPane;

    @FXML private BorderPane eventInfoMaskPane;
    @FXML private StackPane historyInfoMaskPane;

    @FXML private TextArea txtAreaEventDescrp;
    @FXML private ComboBox<String> comBoxEventCategory;
    @FXML private Spinner<Double> spinnerMark;
    @FXML private DatePicker dtpkCreationDate;
    @FXML private DatePicker dtpkExpirationDate;

    @FXML private Spinner<Integer> spinnerExpTimeCount;
    @FXML private ComboBox<String> comBoxExpTimeType;
    @FXML private HBox hboxExpiredTime;

    @FXML private Button btnToSchedule;
    @FXML private Label lblEditing;
    @FXML private Label lblTextAreaPlaceholder;

    @FXML private Button btnCreateEvent;
    @FXML private Button btnCancelEvent;
    @FXML private Button btnDeleteEvent;

    @FXML private InlineCssTextArea historyTxtArea;
    @FXML private Button btnClearHistory;
    @FXML private Button btnBackFromHistory;

    /* --- info bottom panel --- */
    @FXML private HBox infoBotPane;

    @FXML private Label lblActiveEvents;
    @FXML private Label lblTotalMark;
    @FXML private Label lblCurHistory;
    @FXML private Button btnAddEvent;
    @FXML private Button btnOpenHistory;

    /* --- students vbox panel --- */
    @FXML private VBox studentVBox;

    @FXML private TextField txtFieldSearch;
    @FXML private Button btnAddStudent;
    @FXML private Label lblGroupNumber;

    /* --- TABLE --- */
    @FXML private StackPane tableStackPane;

    @FXML private TableView<StudentEvent> eventsTable;
    @FXML private TableColumn<StudentEvent, Number> numberColumn;
    @FXML private TableColumn<StudentEvent, LocalDate> crtDateColumn;
    @FXML private TableColumn<StudentEvent, String> categoryColumn;
    @FXML private TableColumn<StudentEvent, String> descriptionColumn;
    @FXML private TableColumn<StudentEvent, Number> marksColumn;
    @FXML private TableColumn<StudentEvent, String> expDateColumn;
    @FXML private TableColumn<StudentEvent, EventStatus> statusColumn;

    /* --- TABLE --- */

    /* --- MENU --- */
    @FXML private MenuItem miUndo;
    @FXML private MenuItem miRedo;
    @FXML private Button btnUndo;
    @FXML private Button btnRedo;

    @FXML private Menu menuFile;
    @FXML private Button btnExport;
    /* --- MENU --- */
    /* ============================================== */

    /* ==================== FXML GETTERS/SETTERS ==================== */
    /* --- GETTERS --- */
    public BorderPane getTitleBar() { return titleBar; }
    public BorderPane getRootPane() { return rootPane; }

    public InlineCssTextArea getHistoryTxtArea() { return historyTxtArea; }

    public Label getLblActiveEvents() { return lblActiveEvents; }

    public Label getLblTotalMark() { return lblTotalMark; }

    public Label getLblCurHistory() { return lblCurHistory; }

    public Label getLblAppName() { return lblAppName; }

    public VBox getStudentVBox() { return studentVBox; }

    public HBox getTabHBox() {
        return tabHBox;
    }

    public TableView<StudentEvent> getEventsTable() {
        return eventsTable;
    }

    public TextField getTxtFieldSearch() {
        return txtFieldSearch;
    }
    public Button getBtnCancelEvent() {
        return btnCancelEvent;
    }
    public ComboBox<String> getComBoxEventCategory() {
        return comBoxEventCategory;
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
    public StackPane getInfoTopPane() {
        return infoTopPane;
    }
    public DatePicker getDtpkCreationDate() {
        return dtpkCreationDate;
    }
    public DatePicker getDtpkExpirationDate() {
        return dtpkExpirationDate;
    }
    public Spinner<Double> getSpinnerMark() {
        return spinnerMark;
    }
    public Spinner<Integer> getSpinnerExpTimeCount() {
        return spinnerExpTimeCount;
    }
    public ComboBox<String> getComBoxExpTimeType() {
        return comBoxExpTimeType;
    }
    public Button getBtnAddStudent() {
        return btnAddStudent;
    }
    public HBox getInfoBotPane() {
        return infoBotPane;
    }
    public StackPane getTableStackPane() {
        return tableStackPane;
    }
    public HBox getHboxExpiredTime() {
        return hboxExpiredTime;
    }
    public StackPane getHistoryInfoMaskPane() {
        return historyInfoMaskPane;
    }
    public BorderPane getEventInfoMaskPane() {
        return eventInfoMaskPane;
    }
    public Label getLblGroupNumber() {
        return lblGroupNumber;
    }
    public Menu getMenuFile() {
        return menuFile;
    }
    public Button getBtnExport() {
        return btnExport;
    }

    public TableColumn<StudentEvent, String> getCategoryColumn() {
        return categoryColumn;
    }
    public TableColumn<StudentEvent, LocalDate> getCrtDateColumn() {
        return crtDateColumn;
    }
    public TableColumn<StudentEvent, Number> getMarksColumn() {
        return marksColumn;
    }
    /* --- GETTERS --- */
    /* ============================================================== */



    /* ==================== FIELDS ==================== */
    private ImageView historyIcon_To;
    private ImageView historyIcon_Back;
    private Stage stage;
    /* ================================================ */

    /* ==================== FIELDS GETTERS/SETTERS ==================== */
    /* --- GETTERS --- */
    public Stage getStage() { return stage; }

    /* --- SETTERS --- */
    public void setStage(Stage stage) { this.stage = stage; }
    /* ================================================================ */


    /* ==================== FXML METHODS ==================== */
    @FXML public void initialize() {
        new GradientBackground(rootPane, 0.005, 2); // gradient bg
        Actions.getInstance().setController(this);

        initAutoSaveAction();       // init auto saving

        Actions.getInstance().historyActions().setHistory(
                Actions.HistoryActions.HistoryType.INFO,
                ConfigManager.isAutoSaveEnabled() ?
                        "Autosave is enabled. Saving will occur every " + ConfigManager.getAutoSaveSecInterval()
                                + " seconds, you can change these parameters in the program config file (config.json)"
                        : "Autosave disabled"
        );

        Actions.UndoRedo undoManager = Actions.getInstance().undoRedo();

        /* bind disable properties for undo/redo buttons and menus */
        btnUndo.disableProperty().bind(Bindings.not(undoManager.canUndoProperty()));
        btnRedo.disableProperty().bind(Bindings.not(undoManager.canRedoProperty()));
        miUndo.disableProperty().bind(Bindings.not(undoManager.canUndoProperty()));
        miRedo.disableProperty().bind(Bindings.not(undoManager.canRedoProperty()));
        /* ------------------------------------------------------- */

        /* --- LISTENERS --- */
        /* listener for visibility of placeholder on the text area description */
        txtAreaEventDescrp.textProperty().addListener((obs, oldText, newText) -> lblTextAreaPlaceholder.setVisible(newText.isEmpty()) );

        /* listener for search text field */
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

        /* init infoTopPane */
        infoTopPane.setManaged(false);
        infoTopPane.setVisible(false);

        // === НАСТРОЙКА ИКОНОК НА *КНОПКАХ ИСТОРИИ* ===
        /*Image arrowImg = new Image(Objects.requireNonNull(getClass().getResource("/com/coursemanagerfx/ui/icons/w_arrow_256x256.png")).toExternalForm());
        historyIcon_To = new ImageView(arrowImg);
        historyIcon_To.setFitWidth(16);
        historyIcon_To.setFitHeight(16);
        btnOpenHistory.setGraphic(historyIcon_To);
        btnOpenHistory.setContentDisplay(ContentDisplay.RIGHT);

        historyIcon_Back = new ImageView(arrowImg);
        historyIcon_Back.setFitWidth(16);
        historyIcon_Back.setFitHeight(16);
        historyIcon_Back.setRotate(180);    // ← перевернуть по умолчанию
        btnBackFromHistory.setGraphic(historyIcon_Back);
        btnBackFromHistory.setContentDisplay(ContentDisplay.RIGHT);*/
        // =============================================

        /* init custom style for data pickers */
        dtpkCreationDate.setDayCellFactory(addDatePickerCSS());
        dtpkExpirationDate.setDayCellFactory(addDatePickerCSS());
        applyDateFormat(dtpkCreationDate, "dd.MM.yyyy");
        applyDateFormat(dtpkExpirationDate, "dd.MM.yyyy");



        /* init expiration combobox */
        comBoxExpTimeType.setItems(FXCollections.observableArrayList(
                ExpDateStrings.DAYS,
                ExpDateStrings.WEEKS,
                ExpDateStrings.MONTHS
        ));
        comBoxExpTimeType.getSelectionModel().selectFirst();    // and select DAYS by default

        /* init event categories combobox */
        comBoxEventCategory.setVisibleRowCount(15);
        for (EventCategories type : EventCategories.values())
            comBoxEventCategory.getItems().add(type.getDisplayName());
        comBoxEventCategory.getSelectionModel().selectFirst();      // and select first type by default

        initSpinners();     /* init mark and expiration time spinners */

        initTable();        /* init event table */

        /* sort by CATEGORY column in ascending type by default */
        //eventsTable.getSortOrder().add(categoryColumn);
        //categoryColumn.setSortType(TableColumn.SortType.ASCENDING);
        //eventsTable.sort();
    }

    @FXML private void btnAddStudent() {
        Window owner = stage.getScene().getWindow();

        Group selectedGroup = Actions.getInstance().select().getSelectedGroup();

        if (selectedGroup == null) {
            AlertFX.showNotification(
                    AlertMessageType.WARNING,
                    "Group not selected",
                    "Choose the group please."
            );
            return;
        }

        String rawStudentName = ShowDialogUtility.showInputDialog(owner,
                                    "Add student",
                                    "Enter the student's name:",
                                    "");

        if (rawStudentName == null) return;     // pressed cancel

        String studentName = rawStudentName.trim().replaceAll("\\s+", " ");
        if (studentName.split(" ").length != 3) {
            AlertFX.showNotification(
                    AlertMessageType.WARNING,
                    "Invalid input",
                    "Enter full name in format:\nFirstname Lastname Patronymic"
            );
            return;
        }

        int studentID = Actions.getInstance().idGenerator().genGlobalStudentId();
        Student newStudent = new Student(studentName, studentID);

        Command cmd = new AddStudentCommand(selectedGroup, newStudent);
        cmd.execute();
        Actions.getInstance().undoRedo().addCommandToStack(cmd);

        Actions.getInstance().historyActions().setHistory(
                Actions.HistoryActions.HistoryType.SUCCESS,
                "Successfully added student: \"" + studentName + "\""
        );
    }

    @FXML private void btnAddEvent() {
        Actions.getInstance().formAnims().loadEventInfoPane();
        Actions.getInstance().formAnims().mainTopPanelInOut(Actions.FormAnims.State.SHOW);
    }

    @FXML private void btnCancelEvent()
        { Actions.getInstance().formAnims().mainTopPanelInOut(Actions.FormAnims.State.HIDE); }

    @FXML private void btnCreateEvent()
        { Actions.getInstance().uiActions().createEventAction(); }

    @FXML private void btnHistory() {
        Actions.getInstance().formAnims().loadHistoryPane();
        Actions.getInstance().formAnims().mainTopPanelInOut(Actions.FormAnims.State.SHOW);
    }

    @FXML private void btnBackFromHistory()
        { Actions.getInstance().formAnims().mainTopPanelInOut(Actions.FormAnims.State.HIDE); }

    @FXML private void btnClearHistory() {
        historyTxtArea.clear();
        lblCurHistory.setText("");
    }

    @FXML private void toggleExpirationInput() {
        if (Actions.getInstance().formAnims().isToggleAnimPlaysFlag()) return;
        Actions.getInstance().uiActions().toggleExpInputAction();
    }

    // === SET STYLE FOR TEXT ===
    @FXML private void setBoldText()
        { toggleStyleAroundSelection(BOLD_MARKER); }

    @FXML private void setItalicText()
        { toggleStyleAroundSelection(ITALIC_MARKER); }

    @FXML private void setUnderlineText()
        { toggleStyleAroundSelection(UNDERLINE_MARKER); }

    @FXML private void btnClearEventInfo()
        { Actions.getInstance().uiActions().clearAllEventInfo(); }

    /* ==================== TITLE BAR ==================== */
    /* ========== MENU ========== */
    /* === FILE === */
    @FXML private void miHome()
        { Actions.getInstance().menuActions().toHomeAction(); }
    @FXML private void miSave()
        { Actions.getInstance().menuActions().saveAction(); }
    @FXML private void miExport()
        { Actions.getInstance().menuActions().exportAction(); }
    @FXML private void miQuit()
        { Actions.getInstance().uiActions().mainExitAction(); }

    /* === EDIT === */
    @FXML private void miUndo()
        { Actions.getInstance().undoRedo().undo(); }
    @FXML private void miRedo()
        { Actions.getInstance().undoRedo().redo(); }
    @FXML private void miOptions()
        { Actions.getInstance().menuActions().optionsAction(); }

    /* === HELP === */
    @FXML private void miAbout()
        { ShowDialogUtility.showAboutWindow(stage.getScene().getWindow()); }
    /* ========================== */

    /* === BTN "Close" === */
    @FXML private void windowClose()
        { Actions.getInstance().uiActions().mainExitAction(); }
    /* =================== */

    /* === BTN "Minimize" === */
    @FXML private void windowMinimize()
        { stage.setIconified(true); }
    /* ====================== */

    /* =============== CORE =============== */

    /* init spinners */
    private void initSpinners() {

        /* ---------- expiration time spinner ---------- */
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactoryExpTime =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1, 1);
        spinnerExpTimeCount.setValueFactory(valueFactoryExpTime);

        UnaryOperator<TextFormatter.Change> integerFilter = ch -> {
            return ch.getControlNewText().matches("\\d*") ? ch : null;
        };

        TextFormatter<Integer> formatterExp = new TextFormatter<>(
                new IntegerStringConverter(),
                valueFactoryExpTime.getValue(),
                integerFilter
        );
        spinnerExpTimeCount.getEditor().setTextFormatter(formatterExp);

        spinnerExpTimeCount.focusedProperty().addListener((obs, ov, nv) -> {
            if (!nv) spinnerExpTimeCount.increment(0);        // commit on focus lost
        });
        formatterExp.valueProperty().addListener((obs, ov, nv) -> {
            if (nv == null) formatterExp.setValue(valueFactoryExpTime.getMin());
        });

        /* -------------- mark spinner (editable) -------------- */
        Locale locale = Locale.getDefault();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        char sep = symbols.getDecimalSeparator();

        SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(-999, 999, 1.0, 0.5);
        spinnerMark.setValueFactory(valueFactory);
        spinnerMark.setEditable(true);

        // фильтр ввода с учётом локали и допустимыми только 0 или 5 после запятой/точки
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String t = change.getControlNewText();
            String sepStr = Pattern.quote(String.valueOf(sep));
            return t.matches("-?\\d{0,3}(" + sepStr + "[05]?)?") || t.isEmpty() ? change : null;
        };

        // Конвертер строки в Double и обратно, с учетом текущей локали
        StringConverter<Double> converter = new StringConverter<>() {
            @Override
            public String toString(Double value) {
                if (value == null) return "";
                return String.format(locale, "%.1f", value);
            }

            @Override
            public Double fromString(String s) {
                if (s == null || s.isBlank() || "-".equals(s) || String.valueOf(sep).equals(s))
                    return null;
                try {
                    return Double.parseDouble(s.replace(sep, '.')); // преобразуем в формат parseDouble
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        };

        TextFormatter<Double> formatter = new TextFormatter<>(converter, valueFactory.getValue(), filter);
        spinnerMark.getEditor().setTextFormatter(formatter);

        // Синхронизация
        formatter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(valueFactory.getValue())) {
                valueFactory.setValue(newVal);
            }
        });

        valueFactory.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(formatter.getValue())) {
                formatter.setValue(newVal);
            }
        });

        // Если ушли с фокуса и значение пустое — ставим 1.0
        spinnerMark.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (formatter.getValue() == null) formatter.setValue(1.0);
                spinnerMark.increment(0);
            }
        });
    }

    /* init table of events */
    private void initTable() {

        Actions.getInstance().uiActions().addCopyPasteEventTableAction();

        /* add placeholder label */
        Label emptyLabel = new Label("You have not added any events yet...");
        emptyLabel.setStyle("-fx-text-fill: rgba(195,195,195,0.5); -fx-font-size: 32px;");
        eventsTable.setPlaceholder(emptyLabel);
        /* --------------------- */

        eventsTable.getColumns().forEach(col -> col.setReorderable(false));     /* remove drag of all columns */

        /* ----- column NUMBER ----- */
        numberColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(eventsTable.getItems().indexOf(cellData.getValue()) + 1));
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
        /* ------------------------- */



        /* ----- column CREATION DATE ----- */
        /* sort by date */
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        crtDateColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(
                        LocalDate.of(
                                cellData.getValue().getCrtDate().getYear(),
                                cellData.getValue().getCrtDate().getMonth(),
                                cellData.getValue().getCrtDate().getDay()
                        )
                )
        );

        crtDateColumn.setCellFactory(column -> new TableCell<StudentEvent, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.format(dateFormatter));
            }
        });

        /* -------------------------------- */



        /* ----- column CATEGORY ----- */
        categoryColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(
                        cellData.getValue()
                                .getCategory()
                                .getDisplayName())
        );

        /* sort by ukrainian alphabet */
        //categoryColumn.setComparator(AppConstants.UA_COLLATOR::compare);

        /* sort by name in MODs (MOD_1, MOD_2 ...) */
        categoryColumn.setComparator((a, b) -> {
            EventCategories ec1 = Actions.getInstance().uiActions().returnCategoryByName(a);
            EventCategories ec2 = Actions.getInstance().uiActions().returnCategoryByName(b);
            return Integer.compare(ec1.getCode(), ec2.getCode());
        });
        /* ------------------------------ */

        /* ----- style ----- */
        categoryColumn.setCellFactory(column -> new TableCell<StudentEvent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                StudentEvent event = getTableRow() != null ? getTableRow().getItem() : null;
                if (event == null) {
                    setText(item);
                    setStyle("");
                    return;
                }

                EventCategory category = event.getCategory().getEventCategory();
                Color fxColor = category.color();

                /* to css rgba */
                String rgba = String.format(
                        "rgba(%d,%d,%d,%.3f)",
                        (int) (fxColor.getRed() * 255),
                        (int) (fxColor.getGreen() * 255),
                        (int) (fxColor.getBlue() * 255),
                        fxColor.getOpacity()
                );

                setText(event.getCategory().getDisplayName());
                setStyle("-fx-background-color: " + rgba + ";");
            }
        });

        /* --------------------------- */

        /* ----- column DESCRIPTION ----- */
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(column -> new TableCell<StudentEvent, String>() {
            private final Button btnEdit = createEditButton();
            private final VBox vbox = new VBox(4);
            private final StackPane stackPane = new StackPane(vbox, btnEdit);

            /* instance initialization (once for each line-event) */
            {
                StackPane.setAlignment(btnEdit, Pos.TOP_RIGHT);
                Actions.getInstance().uiActions().editingEventProperty().addListener((obs, oldVal, newVal) -> refreshStyle());
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    TextFlow textFlow = applyTextStyleForTextFlow(item);
                    textFlow.maxWidthProperty().bind(getTableColumn().widthProperty().subtract(6));
                    vbox.getChildren().setAll(new javafx.scene.Group(textFlow));
                    setGraphic(stackPane);
                    refreshStyle();
                }
            }

            private Button createEditButton() {
                Button b = new Button();
                b.setPrefSize(32, 32);
                b.getStyleClass().add("edit-button");
                b.setOnAction(e -> {
                    StudentEvent event = getTableRow().getItem();
                    if (event == null || Actions.getInstance().formAnims().isAnimPlaysFlag()) return;

                    if (Actions.getInstance().uiActions().getEditingEvent() == event)
                        Actions.getInstance().uiActions().stopEditing();
                    else
                        Actions.getInstance().uiActions().startEditing(event);
                });
                return b;
            }

            private void refreshStyle() {
                StudentEvent current = getTableRow().getItem();
                boolean isEditing = current != null && current == Actions.getInstance().uiActions().getEditingEvent();

                ObservableList<String> style = btnEdit.getStyleClass();
                style.removeAll("edit-button", "edit-button-selected");
                style.add(isEditing ? "edit-button-selected" : "edit-button");
            }

            private TextFlow applyTextStyleForTextFlow(String textStr) {
                final TextFlow flow = new TextFlow();

                String regex = String.format("(%1$s[^%1$s]+%1$s)|(%2$s[^%2$s]+%2$s)|(%3$s[^%3$s]+%3$s)|([^%1$s%2$s%3$s]+)",
                        Pattern.quote(BOLD_MARKER),
                        Pattern.quote(ITALIC_MARKER),
                        Pattern.quote(UNDERLINE_MARKER)
                );

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(textStr);

                while (matcher.find()) {
                    String group = matcher.group();
                    Text text = new Text();
                    text.setFill(Color.WHITE);
                    text.setStyle("-fx-font-size: 14px;");

                    if (group.startsWith(BOLD_MARKER) && group.endsWith(BOLD_MARKER)) {
                        text.setText(group.substring(BOLD_MARKER.length(), group.length() - BOLD_MARKER.length()));
                        text.setStyle(text.getStyle() + "; -fx-font-weight: bold;");
                    } else if (group.startsWith(ITALIC_MARKER) && group.endsWith(ITALIC_MARKER)) {
                        text.setText(group.substring(ITALIC_MARKER.length(), group.length() - ITALIC_MARKER.length()));
                        text.setStyle(text.getStyle() + "; -fx-font-style: italic;");
                    } else if (group.startsWith(UNDERLINE_MARKER) && group.endsWith(UNDERLINE_MARKER)) {
                        text.setText(group.substring(UNDERLINE_MARKER.length(), group.length() - UNDERLINE_MARKER.length()));
                        text.setUnderline(true);
                    } else {
                        text.setText(group);
                    }

                    flow.getChildren().add(text);
                }

                return flow;
            }
        });

        /* sort by ukrainian alphabet */
        descriptionColumn.setComparator(AppConstants.UA_COLLATOR::compare);
        /* ------------------------------ */



        /* ----- column MARK ----- */
        marksColumn.setCellValueFactory(new PropertyValueFactory<>("mark"));
        marksColumn.setCellFactory(column -> new TableCell<StudentEvent, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    double value = item.doubleValue();
                    if (value % 1 == 0) setText(String.valueOf((int) value));       // do this for "25.0"  = "25"
                    else setText(String.valueOf(value));                            // do this for "27.78" = "27.78"
                }
            }
        });
        /* ----------------------- */



        /* ----- column EXPIRATION DATE ----- */
        /* style "dd.MM.yyyy (xx days)" */
        expDateColumn.setCellValueFactory(cellData -> {
            StudentEvent event = cellData.getValue();
            String formatted = event.getExpDate().toFormattedWithDaysFrom(event.getCrtDate());
            return new SimpleStringProperty(formatted);
        });

        /* sort by date */
        /*expDateColumn.setComparator(Comparator.comparing(s -> {
            String[] parts = s.split(" ")[0].split("\\.");
            return LocalDate.of(
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[0])
            );
        }));*/

        /* sort by expire day value */
        expDateColumn.setComparator((s1, s2) -> {
            int d1 = extractDays(s1);
            int d2 = extractDays(s2);
            return Integer.compare(d1, d2);
        });
        /* ---------------------------------- */



        /* ----- column STATUS ----- */
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<StudentEvent, EventStatus>() {
            @Override
            protected void updateItem(EventStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(""); // reset
                } else {
                    setText(item.name());

                    if (item == EventStatus.ACTIVE) {
                        setStyle("-fx-background-color: rgba(0,255,0,0.15);" +
                                "-fx-text-fill: #00b100;" +
                                "-fx-font-weight: bold");
                    } else if (item == EventStatus.COMPLETED) {
                        setStyle("-fx-background-color: rgba(255,0,0,0.15);" +
                                "-fx-text-fill: #b10000;" +
                                "-fx-font-weight: bold");
                    } else {
                        setStyle(""); // fallback
                    }
                }
            }
        });
    }

    private int extractDays(String s) {
        if (s == null) return -1;
        int start = s.indexOf('(');
        int end = s.indexOf(" days");
        if (start != -1 && end != -1 && start < end) {
            try { return Integer.parseInt(s.substring(start + 1, end).trim()); }
            catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    /* init autosave action */
    private void initAutoSaveAction() {
        if (ConfigManager.isAutoSaveEnabled()) {
            int interval = ConfigManager.getAutoSaveSecInterval();

            Timeline autoSaveTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(interval), event -> {
                        Actions.getInstance().menuActions().saveAction();
                    })
            );
            autoSaveTimeline.setCycleCount(Animation.INDEFINITE);
            autoSaveTimeline.play();
        }
    }

    /* init data picker custom style */
    private Callback<DatePicker, DateCell> addDatePickerCSS() {
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
                    if (item.getMonthValue() % 2 == 1)   getStyleClass().add("odd-month");
                    else                                 getStyleClass().add("even-month");
                }
            }
        };
    }

    /* init data picker custom format */
    private void applyDateFormat(DatePicker datePicker, String format) {
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
        int caretPos;

        if (hasBefore && hasAfter) {
            /* removing markers */
            newText = text.substring(0, before) + selected + text.substring(after);
            caretPos = before + selected.length();
        } else {
            /* adding markers */
            newText = text.substring(0, start) + marker + selected + marker + text.substring(end);
            caretPos = end + 2 * marker.length();
        }

        txtAreaEventDescrp.requestFocus();
        txtAreaEventDescrp.setText(newText);
        txtAreaEventDescrp.positionCaret(caretPos);
    }
    /* ==================================== */
}
