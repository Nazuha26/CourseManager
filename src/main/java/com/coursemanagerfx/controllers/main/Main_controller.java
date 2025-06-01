package com.coursemanagerfx.controllers.main;

import com.coursemanagerfx.controllers.StageAttachable;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.coursemanagerfx.logic.Actions;
import com.coursemanagerfx.logic.basic.*;
import com.coursemanagerfx.custom_ui.GradientBackground;
import com.coursemanagerfx.logic.basic.event.EventCategories;
import com.coursemanagerfx.logic.basic.event.EventStatus;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.date.ExpDateStrings;
import com.coursemanagerfx.logic.commands.*;
import com.coursemanagerfx.logic.commands.student_comms.AddStudentCommand;
import com.coursemanagerfx.logic.utilities.show.ShowDialogUtility;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.fxmisc.richtext.InlineCssTextArea;

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
    @FXML private ComboBox<String> comBoxEventType;
    @FXML private Spinner<Integer> spinnerMark;
    @FXML private DatePicker dtpkCreationDate;
    @FXML private DatePicker dtpkExpirationDate;

    @FXML private Spinner<Integer> spinnerExpTimeCount;
    @FXML private ComboBox<String> comBoxExpTimeType;
    @FXML private HBox hboxExpiredTime;

    @FXML private Button btnToSchedule;
    @FXML private Label lblEditing;
    @FXML private Label placeholderLbl;

    @FXML private Button btnCreateEvent;
    @FXML private Button btnCancelEvent;
    @FXML private Button btnDeleteEvent;

    @FXML private InlineCssTextArea richTxtPaneHistory;
    @FXML private Button btnClearHistory;
    @FXML private Button btnBackFromHistory;

    /* --- info bottom panel --- */
    @FXML private HBox infoBotPane;

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
    @FXML private TableColumn<StudentEvent, String> crtDateColumn;
    @FXML private TableColumn<StudentEvent, String> descriptionColumn;
    @FXML private TableColumn<StudentEvent, Number> marksColumn;
    @FXML private TableColumn<StudentEvent, String> expDateColumn;
    @FXML private TableColumn<StudentEvent, EventStatus> statusColumn;
    /* --- TABLE --- */
    /* ============================================== */

    /* ==================== FXML GETTERS/SETTERS ==================== */
    /* --- GETTERS --- */
    public BorderPane getTitleBar() {
        return titleBar;
    }
    public BorderPane getRootPane() {
        return rootPane;
    }

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
    public StackPane getInfoTopPane() {
        return infoTopPane;
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
    /* --- GETTERS --- */
    /* ============================================================== */



    /* ==================== FIELDS ==================== */
    private ImageView historyIcon_To;
    private ImageView historyIcon_Back;
    private Stage stage;
    private Button activeEditButton = null;
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

        /* --- LISTENERS --- */
        /* listener for visibility of placeholder on the text area description */
        txtAreaEventDescrp.textProperty().addListener((obs, oldText, newText) -> placeholderLbl.setVisible(newText.isEmpty()) );

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

        /* init types of event combobox */
        for (EventCategories type : EventCategories.values())
            comBoxEventType.getItems().add(type.getEventCategory().name());
        comBoxEventType.getSelectionModel().selectFirst();      // and select first type by default

        initSpinners();     /* init mark and expiration time spinners */

        initTable();        /* init event table */

        /* sort by status column in ascending type by default */
        eventsTable.getSortOrder().add(statusColumn);
        statusColumn.setSortType(TableColumn.SortType.ASCENDING);
        eventsTable.sort();
    }

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
            AlertFX.showNotification(
                    owner,
                    AlertFX_type.WARNING,
                    "Invalid input",
                    "Enter full name in format:\nFirstname Lastname Patronymic",
                    true
            );
            return;
        }

        int studentID = Actions.getInstance().getIdGenerator().genUniqueStudentId();
        Student newStudent = new Student(studentName, studentID);

        Command cmd = new AddStudentCommand(selectedGroup, newStudent);
        cmd.execute();
        Actions.getInstance().undoRedo().addCommand(cmd);
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
        richTxtPaneHistory.clear();
        lblCurHistory.setText("");
    }

    @FXML private void toggleExpirationInput() {
        if (Actions.getInstance().formAnims().isToggleAnimPlaysFlag()) return;
        Actions.getInstance().uiActions().toggleExpInputAction();
    }

    // === SET STYLE FOR TEXT ===
    @FXML
    private void setBoldText()
        { toggleStyleAroundSelection(BOLD_MARKER); }

    @FXML
    private void setItalicText()
        { toggleStyleAroundSelection(ITALIC_MARKER); }

    @FXML
    private void setUnderlineText()
        { toggleStyleAroundSelection(UNDERLINE_MARKER); }

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
    public void initSpinners() {
        /* expiration time spinner */
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactoryExpTime =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1, 1);
        spinnerExpTimeCount.setValueFactory(valueFactoryExpTime);

        /* mark spinner */
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactoryMark =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1, 1);
        spinnerMark.setValueFactory(valueFactoryMark);

        /* --- set input to digits only --- */
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*"))
                return change;
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
        /* -------------------------------- */

        spinnerExpTimeCount.focusedProperty().addListener((obs, oldVal, newVal) ->
            { if (!newVal) spinnerExpTimeCount.increment(0); });
        spinnerMark.focusedProperty().addListener((obs, oldVal, newVal) ->
            { if (!newVal) spinnerMark.increment(0); });
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
    }

    /* init table of events */
    public void initTable() {
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



        /* ----- column CREATION DATA ----- */
        /* sort by date */
        crtDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCrtDate().toString()));
        /* -------------------------------- */



        /* ----- column DESCRIPTION ----- */
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionColumn.setCellFactory(column -> new TableCell<StudentEvent, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(4);

                    TextFlow textFlow = applyTextStyleForTextFlow(item);
                    textFlow.maxWidthProperty().bind(getTableColumn().widthProperty().subtract(6));
                    vbox.getChildren().add(new javafx.scene.Group(textFlow));

                    /* edit event button */
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

            private TextFlow applyTextStyleForTextFlow(String textStr) {
                final TextFlow flow = new TextFlow();

                // Используем маркеры из констант
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
        /* ------------------------------ */



        /* ----- column MARK ----- */
        marksColumn.setCellValueFactory(new PropertyValueFactory<>("mark"));
        /* ----------------------- */



        /* ----- column EXPIRATION DATE ----- */
        /* style "dd.MM.yyyy (xx days)" */
        expDateColumn.setCellValueFactory(cellData -> {
            StudentEvent event = cellData.getValue();
            String formatted = event.getExpDate().toFormattedWithDaysFrom(event.getCrtDate());
            return new SimpleStringProperty(formatted);
        });

        /* sort by date */
        expDateColumn.setComparator(Comparator.comparing(s -> {
            String[] parts = s.split(" ")[0].split("\\.");
            return LocalDate.of(
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[0])
            );
        }));

        /* sort by value parenthesis */
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
                return -1;
            }
            return -1;
        }));*/
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
    }

    /* init data picker custom style */
    public Callback<DatePicker, DateCell> addDatePickerCSS() {
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

    /* init data picker custom format */
    public void applyDateFormat(DatePicker datePicker, String format) {
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