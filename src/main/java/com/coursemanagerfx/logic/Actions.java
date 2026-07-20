/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic;

import com.coursemanagerfx.AppConstants;
import com.coursemanagerfx.Launcher;
import com.coursemanagerfx.animations.WindowBlindsOutAnimation;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertMessageType;
import com.coursemanagerfx.controllers.dialogs.exceptions.SaveException;
import com.coursemanagerfx.controllers.main.Main_controller;
import com.coursemanagerfx.controllers.main.StudentPanel_controller;
import com.coursemanagerfx.custom_ui.ProgressSpinner;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.category.EventCategories;
import com.coursemanagerfx.logic.basic.event.EventStatus;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.date.EventDate;
import com.coursemanagerfx.logic.basic.event.date.ExpDateStrings;
import com.coursemanagerfx.logic.commands.Command;
import com.coursemanagerfx.logic.commands.event_comms.AddEventCommand;
import com.coursemanagerfx.logic.commands.event_comms.DeleteEventCommand;
import com.coursemanagerfx.logic.commands.event_comms.EditEventCommand;
import com.coursemanagerfx.logic.utilities.security.CmanSecurityUtility;
import com.coursemanagerfx.logic.utilities.ExcelExportUtility;
import com.coursemanagerfx.logic.utilities.update.UpdateUtility;
import com.coursemanagerfx.logic.config_api.ConfigManager;
import com.coursemanagerfx.logic.utilities.update.exceptions.NoInternetConnection;
import com.coursemanagerfx.logic.utilities.view.ShowWindowUtility;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Window;
import javafx.util.Duration;
import org.fxmisc.richtext.InlineCssTextArea;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Actions {

    /* ========================= SINGLETON ========================= */
    private static final Actions INSTANCE = new Actions();
    private Actions() {}
    public static Actions getInstance() { return INSTANCE; }

    /* ========================= CONTROLLER ========================= */
    private Main_controller ctrl;
    public void setController(Main_controller controller) { this.ctrl = controller; }

    /* ========================= SUB-ACTIONS ========================= */
    private final Repaint        repaint        = new Repaint();
    private final Select         select         = new Select();
    private final UiActions      uiActions      = new UiActions();
    private final MenuActions    menuActions    = new MenuActions();
    private final IdGenerator    idGenerator    = new IdGenerator();
    private final FormAnims      formAnims      = new FormAnims();
    private final UndoRedo       undoRedo       = new UndoRedo();
    private final HistoryActions historyActions = new HistoryActions();
    private final UiFlowActions  uiFlowActions  = new UiFlowActions();

    public Repaint        repaint()         { return repaint; }
    public Select         select()          { return select; }
    public UiActions      uiActions()       { return uiActions; }
    public MenuActions    menuActions()     { return menuActions; }
    public IdGenerator    idGenerator()     { return idGenerator; }
    public FormAnims      formAnims()       { return formAnims; }
    public UndoRedo       undoRedo()        { return undoRedo; }
    public HistoryActions historyActions()  { return historyActions; }
    public UiFlowActions  uiFlowActions()   { return uiFlowActions; }

    /* ******************************************************************
     *                          CLASS  Repaint
     * *****************************************************************/
    public class Repaint {

        /* call once in main initialization */
        public void initGroupTabs() {
            if (ctrl == null) return;

            HBox tabBox = ctrl.getTabHBox();
            tabBox.getChildren().clear();

            Group[] course = Launcher.getCourseInfo().getCourse();
            for (int i = 0; i < course.length; i++) {
                Group group = course[i];

                Button tabBtn = new Button("Group " + (i + 1));
                tabBtn.setMaxWidth(Double.MAX_VALUE);
                tabBtn.getStyleClass().add("tab-button");
                HBox.setHgrow(tabBtn, Priority.ALWAYS);

                tabBtn.setOnAction(e -> select.selectGroupTab(group));

                tabBox.getChildren().add(tabBtn);
            }

            select.selectGroupTab(course[0]);   // select first group by default
        }

        /* repaint student panels of group */
        public List<Student> repaintStudentPanels(Group group) {
            if (ctrl == null || group == null) return Collections.emptyList();

            VBox studentsBox = ctrl.getStudentVBox();
            studentsBox.getChildren().clear();

            TextField searchField = ctrl.getTxtFieldSearch();
            String searchText = searchField.getText().trim().toLowerCase();

            List<Student> original = group.getStudents();
            List<Student> list;

            /* view everyone */
            if (searchField.isDisable() || searchText.isEmpty()) {
                list = new ArrayList<>(original);
                searchField.setDisable(original.isEmpty());
            }
            /* view student by filter by searched text */
            else {
                list = original.stream()
                        .filter(s -> s.getName().toLowerCase().contains(searchText))
                        .collect(Collectors.toList());
            }

            //list.sort(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));   // always sort by alphabet

            /* --- sort by ukrainian alphabet --- */
            list.sort(Comparator.comparing(Student::getName, AppConstants.UA_COLLATOR));
            /* ---------------------------------- */

            if (list.isEmpty()) {
                showEmptyLabel(studentsBox,
                        original.isEmpty() ? "There are no students in the group yet"
                                : "No matching students found");
                return Collections.emptyList();
            }

            int num = 1;
            for (Student s : list) {
                try {
                    FXMLLoader ldr = new FXMLLoader(
                            getClass().getResource("/com/coursemanagerfx/ui/forms/student_panel.fxml"));
                    BorderPane pane = ldr.load();

                    boolean searched = !searchField.isDisable() && !searchText.isEmpty();
                    ldr.<StudentPanel_controller>getController().setCtx(ctrl, s, num++, searched);

                    studentsBox.getChildren().add(pane);
                } catch (IOException ex) {
                    throw new RuntimeException("student_panel.fxml load error", ex);
                }
            }

            return list;
        }

        /* repaint event table of student */
        public void repaintEventTable(Student student, Group group) {
            if (ctrl == null || group == null) return;

            TableView<StudentEvent> table = ctrl.getEventsTable();
            if (student != null && !student.getEvents().isEmpty()) {
                table.getItems().setAll(student.getEvents());

                ctrl.getCategoryColumn().setSortType(TableColumn.SortType.ASCENDING);
                ctrl.getCrtDateColumn().setSortType(TableColumn.SortType.ASCENDING);
                ctrl.getMarksColumn().setSortType(TableColumn.SortType.DESCENDING);

                table.getSortOrder().setAll(
                        ctrl.getCategoryColumn(),
                        ctrl.getCrtDateColumn(),
                        ctrl.getMarksColumn()
                );

                table.sort();
            } else {
                table.getItems().clear();
                table.getSortOrder().clear();       // remove sort from CATEGORY column
                table.refresh();
            }
            repaintStudentInfoLbl();
        }

        /* repaint group and select student */
        public void refreshStudentView(Group group, Student student) {
            repaintStudentPanels(group);
            select().selectStudentPanel(student, group);
        }

        /* only for AddEventCommand & DeleteEventCommand & EditEventCommand */
        public void smartRefresh(Group group, Student student) {
            Select select = Actions.getInstance().select();
            Repaint repaint = Actions.getInstance().repaint();

            if (group == select.getSelectedGroup() && student == select.getSelectedStudent()) {
                repaint.repaintEventTable(student, group);
            } else {
                repaint.refreshStudentView(group, student);
            }
        }



        /* ========== CORE ========== */

        private void repaintStudentInfoLbl() {
            if (ctrl == null) return;

            int activeEventsCount = (int) ctrl.getEventsTable().getItems().stream()
                    .filter(event -> event.getStatus() == EventStatus.ACTIVE)
                    .count();

            double totalMark = ctrl.getEventsTable().getItems().stream()
                    .filter(event -> event.getStatus() == EventStatus.ACTIVE)
                    .mapToDouble(StudentEvent::getMark)
                    .sum();

            ctrl.getLblActiveEvents().setText("Active events: " + activeEventsCount);

            ctrl.getLblTotalMark().setText("Total mark: " + ((totalMark % 1 == 0) ?
                    String.valueOf((int) totalMark) : String.valueOf(totalMark)));
        }

        /* ========== helpers ========== */
        private void showEmptyLabel(VBox box, String text) { box.getChildren().addAll(spacer(), emptyLabel(text), spacer()); }
        private Pane spacer() { Pane p = new Pane(); p.setPrefHeight(9999); return p; }
        private Label emptyLabel(String text) {
            Label l = new Label(text);
            l.getStyleClass().add("empty-label");
            l.setAlignment(Pos.CENTER);
            l.setMaxWidth(Double.MAX_VALUE);
            return l;
        }
    }

    /* ******************************************************************
     *                          CLASS  Select
     * *****************************************************************/
    public class Select {

        private Group   selectedGroup;
        private Student selectedStudent;

        /* select group tab, repaint students and select first if it possible */
        public void selectGroupTab(Group group) {
            if (ctrl == null || group == null || group == selectedGroup) return;

            int groupIdx = indexOfGroup(group);

            int idx = 0;
            for (Node n : ctrl.getTabHBox().getChildren()) {
                if (idx == groupIdx)
                    n.getStyleClass().add("selected-tab");
                else
                    n.getStyleClass().remove("selected-tab");
                idx++;
            }

            selectedGroup = group;
            ctrl.getLblGroupNumber().setText("GROUP " + (groupIdx + 1));
            ctrl.getTxtFieldSearch().setText("");

            /* repaint students and select first if it possible */
            /*List<Student> sorted = repaint.repaintStudentPanels(group);
            if (!sorted.isEmpty()) {
                selectStudentPanel(sorted.getFirst(), group);
            } else {
                selectedStudent = null;
                repaint.repaintEventTable(null, group);
            }*/

            selectFirstOrClear(group);
        }

        /* select student in selected group */
        public void selectStudentPanel(Student student) {
            if (ctrl == null || student == null || selectedGroup == null) return;

            for (Node n : ctrl.getStudentVBox().getChildren()) {
                if (n instanceof BorderPane bp &&
                        bp.getCenter() instanceof Button btn &&
                        btn.getUserData() instanceof Student st) {
                            btn.getStyleClass().remove("selected-student-panel");
                            if (st.equals(student))
                                btn.getStyleClass().add("selected-student-panel");
                }
            }

            repaint.repaintEventTable(student, selectedGroup);
            selectedStudent = student;
        }
        /* select group tab then select student panel and load his events in event table */
        public void selectStudentPanel(Student student, Group group) {
            selectGroupTab(group);
            selectStudentPanel(student);
        }

        /* util method only for AddStudentCommand & DeleteStudentCommand */
        public void selectFirstOrClear(Group group) {
            if (group == null) return;

            List<Student> sorted = repaint.repaintStudentPanels(group);
            if (!sorted.isEmpty()) {
                selectStudentPanel(sorted.getFirst(), group);
            } else {
                selectedStudent = null;
                repaint.repaintEventTable(null, group);
            }
        }

        /* ========== getters ========== */
        public Group   getSelectedGroup()   { return selectedGroup; }
        public Student getSelectedStudent() { return selectedStudent; }



        /* ========== CORE ========== */

        /* ========== helper ========== */
        private int indexOfGroup(Group g) {
            Group[] arr = Launcher.getCourseInfo().getCourse();
            for (int i = 0; i < arr.length; i++) if (arr[i] == g) return i;
            return -1;
        }
    }

    /* ******************************************************************
     *                          CLASS  UiActions
     * *****************************************************************/
    public class UiActions {
        /*private StudentEvent editingEvent;
        public StudentEvent getEditingEvent() {
            return editingEvent;
        }*/

        /* ======  PUBLIC API  ====== */

        private final ObjectProperty<StudentEvent> editingEvent = new SimpleObjectProperty<>(null);
        public ObjectProperty<StudentEvent> editingEventProperty() { return editingEvent; }

        public StudentEvent getEditingEvent() { return editingEvent.get(); }
        public void setEditingEvent(StudentEvent event) { editingEvent.set(event); }


        public void startEditing(StudentEvent event) {
            if (ctrl == null) return;

            if (!ctrl.getInfoTopPane().isVisible()) {
                formAnims.loadEventInfoPane();
                formAnims.mainTopPanelInOut(FormAnims.State.SHOW);
            }

            setEditingEvent(event);
            //editingEvent = event;

            for (Node node : ctrl.getTabHBox().getChildren()) node.setDisable(true);      // disable group tab buttons
            for (Node node : ctrl.getStudentVBox().getChildren()) node.setDisable(true);  // disable student panels

            undoRedo.setUndoRedoDisabled(true);       // block undo/redo actions
            ctrl.getMenuFile().setDisable(true);    // block menu "File"
            ctrl.getBtnExport().setDisable(true);   // block button "Export"

            ctrl.getTxtFieldSearch().setDisable(true);  // disable search field
            ctrl.getBtnAddStudent().setDisable(true);

            ctrl.getBtnCancelEvent().setVisible(false);
            ctrl.getBtnCancelEvent().setManaged(false);

            ctrl.getLblEditing().setVisible(true);
            ctrl.getLblEditing().setManaged(true);

            ctrl.getEventsTable().getColumns().forEach(col -> col.setSortable(false));       // turn off sortable of event table

            /* === fill in the panel === */
            ctrl.getComBoxEventCategory().getSelectionModel().select(event.getCategory().getEventCategory().name()); // type
            ctrl.getTxtAreaEventDescrp().setText(event.getDescription());                                // description
            /* creation date */
            EventDate cd = event.getCrtDate();
            LocalDate creation = LocalDate.of(cd.getYear(), cd.getMonth(), cd.getDay());
            ctrl.getDtpkCreationDate().setValue(creation);
            /* ------------- */
            ctrl.getSpinnerMark().getValueFactory().setValue(event.getMark());                           // mark
            /* expiration date datepicker */
            EventDate ed = event.getExpDate();
            LocalDate expiration = LocalDate.of(ed.getYear(), ed.getMonth(), ed.getDay());
            ctrl.getDtpkExpirationDate().setValue(expiration);
            /* --------------- */

            /* expiration date spinner & combobox */
            int days = (int) ChronoUnit.DAYS.between(creation, expiration);
            ctrl.getSpinnerExpTimeCount().getValueFactory().setValue(days);
            ctrl.getComBoxExpTimeType().getSelectionModel().select(ExpDateStrings.DAYS);
            /* ---------------------------------- */

            /* replace "Create event" button to "Save event" button */
            ctrl.getBtnCreateEvent().setText("Save");
            ctrl.getBtnCreateEvent().setOnAction(ae -> saveEventAction());

            /* view "Delete event" button */
            ctrl.getBtnDeleteEvent().setVisible(true);
            ctrl.getBtnDeleteEvent().setManaged(true);
            ctrl.getBtnDeleteEvent().setOnAction(ae -> deleteEventAction());
        }

        public void stopEditing() {
            if (ctrl == null) return;

            formAnims.mainTopPanelInOut(FormAnims.State.HIDE);

            setEditingEvent(null);
            //editingEvent = null;

            for (Node node : ctrl.getTabHBox().getChildren()) node.setDisable(false);       // enable group tab buttons
            for (Node node : ctrl.getStudentVBox().getChildren()) node.setDisable(false);   // enable student panels

            undoRedo.setUndoRedoDisabled(false);       // unblock undo/redo actions
            ctrl.getMenuFile().setDisable(false);        // unblock menu "File"
            ctrl.getBtnExport().setDisable(false);       // unblock button "Export"

            ctrl.getTxtFieldSearch().setDisable(false);     // enable search field
            ctrl.getBtnAddStudent().setDisable(false);

            ctrl.getBtnCancelEvent().setVisible(true);
            ctrl.getBtnCancelEvent().setManaged(true);

            ctrl.getLblEditing().setVisible(false);
            ctrl.getLblEditing().setManaged(false);

            ctrl.getEventsTable().getColumns().forEach(col -> col.setSortable(true));       // turn on sortable of event table

            ctrl.getComBoxEventCategory().getSelectionModel().select(EventCategories.MOD_1.getEventCategory().name());

            ctrl.getBtnDeleteEvent().setVisible(false);
            ctrl.getBtnDeleteEvent().setManaged(false);

            ctrl.getBtnCreateEvent().setText("Create");
            ctrl.getBtnCreateEvent().setOnAction(evt -> createEventAction());

            clearAllEventInfo();
        }

        public void createEventAction() {
            if (ctrl == null) return;
            if (isInvalidEventForm()) return;

            EventCategories selectedType = returnCategoryByName(ctrl.getComBoxEventCategory().getSelectionModel().getSelectedItem());    // get event type

            /* get creation date */
            LocalDate creationDateRaw = ctrl.getDtpkCreationDate().getValue();
            EventDate creationDate = new EventDate(
                    creationDateRaw.getDayOfMonth(),
                    creationDateRaw.getMonthValue(),
                    creationDateRaw.getYear()
            );
            /* ----------------- */

            String description = ctrl.getTxtAreaEventDescrp().getText().trim();    // get description date
            double mark = ctrl.getSpinnerMark().getValue();                        // get mark

            /* expiration date */
            LocalDate expirationDateRaw = calculateExpirationDate(creationDateRaw);
            if (expirationDateRaw == null) {
                AlertFX.showNotification(
                        AlertMessageType.ERROR,
                        "Expiration date error",
                        "Failed to calculate expiration date. Please check the form."
                );
                return;
            }
            if (isInvalidExpirationDate(creationDateRaw, expirationDateRaw)) {
                AlertFX.showNotification(
                        AlertMessageType.ERROR,
                        "Expiration date error",
                        "Expiration date cannot be less than or equal to creation date"
                );
                return;
            }

            EventDate expirationDate = new EventDate(
                    expirationDateRaw.getDayOfMonth(),
                    expirationDateRaw.getMonthValue(),
                    expirationDateRaw.getYear()
            );
            /* --------------- */

            int eventID = idGenerator.genGlobalEventId();
            StudentEvent newEvent = new StudentEvent(
                    eventID,
                    creationDate,
                    description,
                    mark,
                    expirationDate,
                    selectedType
            );

            Command cmd = new AddEventCommand(select.getSelectedGroup(), select.getSelectedStudent(), newEvent);
            cmd.execute();
            undoRedo.addCommandToStack(cmd);

            historyActions.setHistory(
                    HistoryActions.HistoryType.SUCCESS,
                    "Successfully created event with description: \"" + description + "\""
            );
            clearAllEventInfo();
        }

        public void toggleExpInputAction() {
            if (ctrl == null) return;

            Window owner = ctrl.getStage().getScene().getWindow();

            /* get create date */
            LocalDate creationDateRaw = ctrl.getDtpkCreationDate().getValue();
            if (creationDateRaw == null)
                    { show(owner, "Creation date not set", "Set creation date please."); return; }
            /* --------------- */

            LocalDate expirationDateRaw;
            if (!formAnims.showingDatePicker) {
                expirationDateRaw = calculateExpirationDate(creationDateRaw);
                if (expirationDateRaw == null)
                    { show(owner, "Unexpected error", "An unknown error occurred with the expiration date while switching toggle, please try again."); return; }

                ctrl.getDtpkExpirationDate().setValue(expirationDateRaw);
            } else {
                /* get expiration date */
                expirationDateRaw = ctrl.getDtpkExpirationDate().getValue();
                if (expirationDateRaw == null)
                    { show(owner, "Expiration date not set", "Set expiration date please."); return; }
                /* --------------- */
                int days = (int) ChronoUnit.DAYS.between(creationDateRaw, expirationDateRaw);
                ctrl.getSpinnerExpTimeCount().getValueFactory().setValue(days);
                ctrl.getComBoxExpTimeType().getSelectionModel().select(ExpDateStrings.DAYS);
                ctrl.getDtpkExpirationDate().setValue(null);
            }
            formAnims.expDateInOut();
            isShowingDataPickerFlag = !formAnims.showingDatePicker;
        }

        public void clearAllEventInfo() {
            if (ctrl == null) return;

            ctrl.getTxtAreaEventDescrp().setText("");
            ctrl.getComBoxEventCategory().getSelectionModel().selectFirst();
            ctrl.getSpinnerMark().getValueFactory().setValue(1.0);

            if (isShowingDataPickerFlag) toggleExpInputAction();

            ctrl.getSpinnerExpTimeCount().getValueFactory().setValue(1);
            ctrl.getComBoxExpTimeType().getSelectionModel().selectFirst();

            ctrl.getDtpkCreationDate().setValue(null);
        }

        public void mainExitAction() {
            if (ctrl == null) return;

            if (undoRedo.undoStack.isEmpty()) {
                WindowBlindsOutAnimation.play(
                        ctrl,
                        ctrl.getStage().getWidth(),
                        ctrl.getStage().getHeight(),
                        Main_controller.MW_ANIM_STRIPE_COUNT,
                        Duration.seconds(1),
                        ctrl.getStage()::close);
                return;
            }

            boolean confirmed = AlertFX.showQuestion(
                    "Unsaved Changes",
                    "You have unsaved changes. Are you sure you want to exit without saving?",
                    "Exit Without Saving"
            );

            if (confirmed) {
                WindowBlindsOutAnimation.play(
                        ctrl,
                        ctrl.getStage().getWidth(),
                        ctrl.getStage().getHeight(),
                        Main_controller.MW_ANIM_STRIPE_COUNT,
                        Duration.seconds(1),
                        ctrl.getStage()::close);
            }
        }


        /* add function of event copy/paste */
        public void addCopyPasteEventTableAction() {
            if (ctrl == null) return;

            TableView<StudentEvent> eventsTable = ctrl.getEventsTable();

            /* ---------- context-menu for empty area ---------- */
            MenuItem pasteEmptyItem = new MenuItem("Paste event");

            ContextMenu emptyMenu = new ContextMenu(pasteEmptyItem);
            emptyMenu.setAutoHide(true);

            pasteEmptyItem.setOnAction(ev -> pasteIntoSelectedStudent());

            /* ---------- show empty-area menu on right click ---------- */
            eventsTable.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
                if (e.getButton() == MouseButton.PRIMARY) emptyMenu.hide();     // hide on left click

                if (eventsTable.getItems().isEmpty()) {
                    if (e.getButton() == MouseButton.SECONDARY) {
                        pasteEmptyItem.setDisable(copiedEvent == null);
                        emptyMenu.show(eventsTable, e.getScreenX(), e.getScreenY());
                        e.consume();
                    }
                }
            });

            /* ---------- row factory with per-row menu ---------- */
            eventsTable.setRowFactory(tv -> {
                TableRow<StudentEvent> row = new TableRow<>();

                /* --- TOOLTIP --- */
                Tooltip tooltip = new Tooltip("Right-click on an event to COPY\nRight-click anywhere to PASTE");
                tooltip.setStyle("""
                    -fx-font-size: 14px;
                    -fx-padding: 6px 10px;
                """);
                /* --------------- */

                row.itemProperty().addListener((obs, oldItem, newItem) -> {
                    row.setTooltip(newItem == null ? null : tooltip);
                });

                /* menu items */
                MenuItem copyItem  = new MenuItem("Copy event");
                MenuItem pasteItem = new MenuItem("Paste event");
                ContextMenu rowMenu = new ContextMenu(copyItem, pasteItem);
                rowMenu.setAutoHide(true);

                /* menu-state updater */
                rowMenu.setOnShowing(ev -> {
                    copyItem.setDisable(row.isEmpty());
                    pasteItem.setDisable(copiedEvent == null);
                });

                /* COPY */
                copyItem.setOnAction(ev -> {
                    if (row.isEmpty()) return;
                    copiedEvent = deepCopyEvent(row.getItem(), false);
                    studentFrom = select.getSelectedStudent();
                });

                /* PASTE */
                pasteItem.setOnAction(ev -> pasteIntoSelectedStudent());

                row.setContextMenu(rowMenu);
                return row;
            });

        }
        /* ========================== */



        /* ======  CORE  ====== */

        /* --- copy/paste core --- */
        private StudentEvent copiedEvent = null;
        private Student      studentFrom = null;

        private void pasteIntoSelectedStudent() {
            if (copiedEvent == null) return;
            Student targetStud = select.getSelectedStudent();
            if (targetStud == null) return;

            StudentEvent newEvent = deepCopyEvent(copiedEvent, true);

            Command cmd = new AddEventCommand(select.getSelectedGroup(), targetStud, newEvent);
            cmd.execute();
            undoRedo.addCommandToStack(cmd);

            historyActions.setHistory(
                    HistoryActions.HistoryType.SUCCESS,
                    "Copied event from \"" +
                            (studentFrom != null ? studentFrom.getName() : "Unknown") +
                            "\" to \"" +
                            targetStud.getName() +
                            "\""
            );
        }

        /* --- save/delete editing event core --- */
        private boolean isShowingDataPickerFlag = false;

        private void saveEventAction() {
            if (ctrl == null || getEditingEvent() == null) return;
            if (isInvalidEventForm()) return;

            /* === collect all new data === */

            /* create date */
            LocalDate creationDateRaw = ctrl.getDtpkCreationDate().getValue();
            EventDate newCrt = new EventDate(
                    creationDateRaw.getDayOfMonth(),
                    creationDateRaw.getMonthValue(),
                    creationDateRaw.getYear()
            );
            /* ----------- */

            String newDesc = ctrl.getTxtAreaEventDescrp().getText().trim();      // description

            EventCategories newType = returnCategoryByName(ctrl.getComBoxEventCategory()
                                        .getSelectionModel().getSelectedItem()); // type

            double newMark = ctrl.getSpinnerMark().getValue();                      // mark

            /* expiration date */
            LocalDate newExpirationDateRaw = calculateExpirationDate(creationDateRaw);
            if (newExpirationDateRaw == null) {
                AlertFX.showNotification(
                        AlertMessageType.ERROR,
                        "Expiration date error",
                        "Failed to calculate expiration date. Please check the form."
                );
                return;
            }
            if (isInvalidExpirationDate(creationDateRaw, newExpirationDateRaw)) {
                AlertFX.showNotification(
                        AlertMessageType.ERROR,
                        "Expiration date error",
                        "Expiration date cannot be less than or equal to creation date"
                );
                return;
            }
            EventDate newExp = new EventDate(
                    newExpirationDateRaw.getDayOfMonth(),
                    newExpirationDateRaw.getMonthValue(),
                    newExpirationDateRaw.getYear()
            );
            /* --------------- */

            StudentEvent editedCopy = new StudentEvent(
                    getEditingEvent().getID(),
                    newCrt,
                    newDesc,
                    newMark,
                    newExp,
                    newType
            );

            // === Check if anything has changed ===
            if (editedCopy.equals(getEditingEvent())) {
                System.out.println("No changes detected - skipping save.");
                stopEditing();
                return;
            }

            // === Save ===
            EditEventCommand cmd = new EditEventCommand(
                    select.getSelectedGroup(),
                    select.getSelectedStudent(),
                    getEditingEvent(),
                    editedCopy
            );
            cmd.execute();
            undoRedo.addCommandToStack(cmd);

            historyActions.setHistory(
                    HistoryActions.HistoryType.INFO,
                    "Edited event with description: \"" + newDesc + "\""
            );

            stopEditing();
        }

        private void deleteEventAction() {
            if (ctrl == null || getEditingEvent() == null) return;

            String shortDesc = getEditingEvent().getDescription().length() > 10
                    ? getEditingEvent().getDescription().substring(0, 10) + "..."
                    : getEditingEvent().getDescription();
            boolean isDelete = AlertFX.showQuestion(
                    "Event deleting",
                    "Do you want to delete \"" + shortDesc + "\" event?");

            if (isDelete) {
                StudentEvent currentEvent = getEditingEvent();
                DeleteEventCommand cmd = new DeleteEventCommand(
                        select.getSelectedGroup(),
                        select.getSelectedStudent(),
                        currentEvent
                );
                cmd.execute();
                undoRedo.addCommandToStack(cmd);

                historyActions.setHistory(
                        HistoryActions.HistoryType.INFO,
                        "Deleted event with description: \"" + currentEvent.getDescription() + "\""
                );

                stopEditing();
            }
        }



        /* ======  HELPERS  ====== */
        /* get event category by name in combobox */
        public EventCategories returnCategoryByName(String name) {
            for (EventCategories mod : EventCategories.values()) {
                if (mod.getEventCategory().name().equals(name)) {
                    return mod;
                }
            }
            return EventCategories.CUSTOM;
        }

        private StudentEvent deepCopyEvent(StudentEvent original, boolean genId) {
            int id = !genId ? original.getID() : idGenerator.genGlobalEventId();
            return new StudentEvent(
                    id,
                    original.getCrtDate(),
                    original.getDescription(),
                    original.getMark(),
                    original.getExpDate(),
                    original.getCategory()
            );
        }

        /* check all fields in event form during creation event or saving edited event */
        private boolean isInvalidEventForm() {
            Window owner = ctrl.getStage().getScene().getWindow();

            if (select.getSelectedStudent() == null)
                    { show(owner, "Student not selected", "Choose the student please."); return true; }

            if (ctrl.getDtpkCreationDate().getValue() == null)
                    { show(owner, "Creation date not set", "Set creation date please."); return true; }

            if (ctrl.getSpinnerMark().getValue() == null)
                    { show(owner, "Mark spinner is empty", "Set mark please."); return true; }

            TextArea area = ctrl.getTxtAreaEventDescrp();
            if (area.getText() == null || area.getText().trim().isEmpty())
                    { show(owner, "Empty description", "Please enter a description of the event.");  return true; }

            if (ctrl.getDtpkExpirationDate().isVisible() && ctrl.getDtpkExpirationDate().getValue() == null)
                    { show(owner, "Expiration date not set", "Set expiration date please."); return true; }

            if (ctrl.getSpinnerExpTimeCount().isVisible() && ctrl.getSpinnerExpTimeCount().getValue() == null)
                    { show(owner, "Expiration spinner is empty", "Set expiration date please."); return true; }

            return false;
        }

        /* check that expiration date is not equal or less than creation date */
        private boolean isInvalidExpirationDate(LocalDate creationDate, LocalDate expDate)
            { return expDate == null || creationDate == null || !expDate.isAfter(creationDate); }

        /* private method for view notification */
        private static void show(Window owner, String header, String msg)
            { AlertFX.showNotification( AlertMessageType.WARNING, header, msg ); }

        /* calculate expiration date depending on creation date */
        private LocalDate calculateExpirationDate(LocalDate creationDateRaw) {
            if (ctrl.getDtpkExpirationDate().isVisible()) {
                return ctrl.getDtpkExpirationDate().getValue();
            } else if (ctrl.getSpinnerExpTimeCount().isVisible() && ctrl.getComBoxExpTimeType().isVisible()) {
                int count = ctrl.getSpinnerExpTimeCount().getValue();
                String unit = ctrl.getComBoxExpTimeType().getSelectionModel().getSelectedItem();

                return switch (unit) {
                    case ExpDateStrings.DAYS   -> creationDateRaw.plusDays(count);
                    case ExpDateStrings.WEEKS  -> creationDateRaw.plusWeeks(count);
                    case ExpDateStrings.MONTHS -> creationDateRaw.plusMonths(count);
                    default                    -> null;
                };
            }
            return null;
        }
    }

    /* ******************************************************************
     *                          CLASS  MenuActions
     * *****************************************************************/
    public class MenuActions {
        /* ======  PUBLIC API  ====== */

        /* === FILE === */
        public void toHomeAction() {
            boolean yes = AlertFX.showQuestion(
                    "Return to Home",
                    "Are you sure you want to leave the current course and return to the home screen?");
            if (!yes) return;

            ConfigManager.setOpenCourse("none");        // set opened course as none for backing home
            Launcher.clearCourseInfo();

            WindowBlindsOutAnimation.play(
                    ctrl,
                    ctrl.getStage().getWidth(),
                    ctrl.getStage().getHeight(),
                    Main_controller.MW_ANIM_STRIPE_COUNT,
                    Duration.seconds(1),
                    () -> {
                        ctrl.getStage().close();
                        ShowWindowUtility.showStartWindow();
                    }
            );
        }
        /* done */
        public void saveAction() {
            Window owner = ctrl.getStage().getScene().getWindow();
            try {
                File file = Launcher.getCourseInfo().getCourseFile();

                char[] seedPhrase = Launcher.getCourseInfo().copySeedPhrase();
                try {
                    CmanSecurityUtility.updateSecureFile(
                            Launcher.getCourseInfo().getCourse(), file, seedPhrase);
                } finally {
                    if (seedPhrase != null) Arrays.fill(seedPhrase, '\0');
                }

                undoRedo.getUndoStack().clear();    // clear undo stack
                undoRedo.getRedoStack().clear();    // clear redo stack
                undoRedo.updateState();

                /*AlertFX.showNotification(
                        owner,
                        AlertFX_type.INFO,
                        "Saved successfully",
                        "",
                        true);*/

                historyActions.setHistory(
                        HistoryActions.HistoryType.SUCCESS,
                        "Saved successfully"
                );
            } catch (Exception e) {
                AlertFX.showNotification(
                        AlertMessageType.ERROR,
                        "Saving Error",
                        "Message: " + e.getMessage()
                );
                throw new SaveException("=== FATAL SAVING ERROR ===", e);
            }
        }

        /** Saves an open, changed course before the updater can close the app. */
        public boolean savePendingChangesBeforeUpdate() {
            if (ctrl == null
                    || Launcher.getCourseInfo() == null
                    || !undoRedo.hasUnsavedChanges()) {
                return true;
            }

            try {
                saveAction();
                return true;
            } catch (SaveException exception) {
                return false;
            }
        }
        /* done */
        public void exportAction() {
            Window owner = ctrl.getStage().getScene().getWindow();

            if (Launcher.getCourseInfo().isEmpty()) {
                historyActions.setHistory(
                        HistoryActions.HistoryType.WARNING,
                        "There is nothing to export"
                );
                AlertFX.showNotification(
                        AlertMessageType.WARNING,
                        "There is nothing to export",
                        "The whole course is empty, there is nothing to export to Excel."
                );
                return;
            }

            try {
                File exportedPath = ConfigManager.getExportPath();
                boolean successExport = ExcelExportUtility.exportToExcel(
                        Launcher.getCourseInfo().getCourse(),
                        Launcher.getCourseInfo().getCourseName(),
                        exportedPath);
                if (successExport) {
                    historyActions.setHistory(
                            HistoryActions.HistoryType.SUCCESS,
                            "Export completed successfully. The Excel file has been saved to: \"" + exportedPath + "\""
                    );
                    AlertFX.showNotification(
                            AlertMessageType.INFO,
                            "Export completed successfully",
                            "The Excel file has been saved to:\n\"" + exportedPath + "\""
                    );
                }
            } catch (RuntimeException e) {
                Throwable cause = e.getCause();

                if (cause instanceof IOException) {
                    AlertFX.showNotification(
                            AlertMessageType.ERROR,
                            "Excel Export Error",
                            "It looks like the Excel file is open. Please close it and try again."
                    );
                } else {
                    AlertFX.showNotification(
                            AlertMessageType.ERROR,
                            "Unexpected Error",
                            "An unexpected exporting error occurred: " + e.getMessage()
                    );
                }
            }
        }

        /* === EDIT === */
        // TODO options
        public void optionsAction() {
            // TODO OPTIONS
            Window owner = ctrl.getStage().getScene().getWindow();
            AlertFX.showNotification(
                    AlertMessageType.INFO,
                    "Options soon...",
                    "Soon..."
            );
        }

        /* === HELP === */
        // ...

        /* ======  HELPERS  ====== */
    }

    /* ******************************************************************
     *                          CLASS  IdGenerator
     * *****************************************************************/
    public static class IdGenerator {
        public int genGlobalStudentId() {
            Set<Integer> existingIds = new HashSet<>();

            for (Group group : Launcher.getCourseInfo().getCourse())
                for (Student student : group.getStudents())
                    existingIds.add(student.getStudentID());

            int newId = 1_000_000;
            while (existingIds.contains(newId)) newId++;
            return newId;
        }
        public int genGlobalEventId() {
            Set<Integer> existingIds = new HashSet<>();

            for (Group group : Launcher.getCourseInfo().getCourse())
                for (Student student : group.getStudents())
                    for (StudentEvent event : student.getEvents())
                        existingIds.add(event.getID());

            int newId = 10_000;
            while (existingIds.contains(newId)) newId++;
            return newId;
        }
    }

    /* ******************************************************************
     *                          CLASS  FormAnims
     * *****************************************************************/
    public class FormAnims {

        public enum State { SHOW, HIDE }
        private boolean isAnimPlaysFlag = false;
        public boolean isAnimPlaysFlag() { return isAnimPlaysFlag; }

        private boolean isToggleAnimPlaysFlag = false;
        public boolean isToggleAnimPlaysFlag() {
            return isToggleAnimPlaysFlag;
        }
        private boolean showingDatePicker = false;

        /* ======  PUBLIC API  ====== */
        public void mainTopPanelInOut(State state) { mainTopPanelInOut(state, 400); }
        public void mainTopPanelInOut(State state, double ms) {
            if (ctrl == null || isAnimPlaysFlag) return;
            isAnimPlaysFlag = true;
            if (state == State.SHOW)  playIn(ms);
            else                      playOut(ms);
        }

        /* to expiration data picker /or/ to expiration hbox */
        public void expDateInOut() { expDateInOut(300); }
        public void expDateInOut(double ms) {
            if (ctrl == null || isToggleAnimPlaysFlag) return;
            isToggleAnimPlaysFlag = true;
            if (!showingDatePicker) playExpIn(ms);
            else                    playExpOut(ms);
        }

        /* load necessary view in main top panel */
        public void loadEventInfoPane() {
            if (ctrl == null) return;
            ctrl.getHistoryInfoMaskPane().setManaged(false);
            ctrl.getHistoryInfoMaskPane().setVisible(false);
            ctrl.getEventInfoMaskPane().setManaged(true);
            ctrl.getEventInfoMaskPane().setVisible(true);
        }
        public void loadHistoryPane() {
            if (ctrl == null) return;
            ctrl.getEventInfoMaskPane().setManaged(false);
            ctrl.getEventInfoMaskPane().setVisible(false);
            ctrl.getHistoryInfoMaskPane().setManaged(true);
            ctrl.getHistoryInfoMaskPane().setVisible(true);
        }
        /* ========================== */

        /* ======  CORE  ====== */

        /** plays “bottom-out / top-in” sequence */
        private void playIn(double ms) {
            Duration half = Duration.millis(ms / 2);

            /* --- hide bottom info panel --- */
            ParallelTransition hideBottom = buildSlideFadeY(
                    ctrl.getInfoBotPane(),
                    0, 30,
                    1, 0,
                    half
            );

            /* --- view top info panel (starts after bottom hidden) --- */
            ParallelTransition showTop = buildSlideFadeX(
                    ctrl.getInfoTopPane(),
                    50, 0,
                    0, 1,
                    half
            );

            hideBottom.setOnFinished(ev -> {
                toggle(ctrl.getInfoBotPane(), false);
                toggle(ctrl.getInfoTopPane(),        true);
                showTop.play();
            });

            showTop.setOnFinished(ev -> isAnimPlaysFlag = false);

            new ParallelTransition(hideBottom, blinkingAnim(ctrl.getTableStackPane(), ms)).play();
        }

        /** plays “top-out / bottom-in” sequence */
        private void playOut(double ms) {
            Duration half = Duration.millis(ms / 2);

            /* --- hide top info panel --- */
            ParallelTransition hideTop = buildSlideFadeX(
                    ctrl.getInfoTopPane(),
                    0,  50,
                    1, 0,
                    half
            );

            /* --- view bottom info panel (starts after top hidden) --- */
            ParallelTransition showBottom = buildSlideFadeY(
                    ctrl.getInfoBotPane(),
                    30, 0,
                    0, 1,
                    half
            );

            hideTop.setOnFinished(ev -> {
                toggle(ctrl.getInfoTopPane(),        false);
                toggle(ctrl.getInfoBotPane(), true);
                showBottom.play();
            });

            showBottom.setOnFinished(ev -> isAnimPlaysFlag = false);

            new ParallelTransition(hideTop, blinkingAnim(ctrl.getTableStackPane(), ms)).play();
        }

        private void playExpIn(double ms) {
            Duration half = Duration.millis(ms / 2);

            /* ----- hide expiration hbox ----- */
            ParallelTransition hideExpHBox = buildSlideFadeX(
                    ctrl.getHboxExpiredTime(),
                    0, -30,
                    1, 0,
                    half
                    );

            /* ----- view expiration data picker ----- */
            ParallelTransition showExpDtpk = buildSlideFadeX(
                    ctrl.getDtpkExpirationDate(),
                    30, 0,
                    0, 1,
                    half
            );

            hideExpHBox.setOnFinished(e -> {
                toggle(ctrl.getHboxExpiredTime(), false);
                toggle(ctrl.getDtpkExpirationDate(), true);
                showExpDtpk.play();
            });

            showExpDtpk.setOnFinished(e -> {
                showingDatePicker = true;
                isToggleAnimPlaysFlag = false;
            });
            hideExpHBox.play();
        }

        private void playExpOut(double ms) {
            Duration half = Duration.millis(ms / 2);

            /* ----- hide expiration data picker ----- */
            ParallelTransition hideExpDtpk = buildSlideFadeX(
                    ctrl.getDtpkExpirationDate(),
                    0, 30,
                    1, 0,
                    half
            );

            /* ----- view expiration hbox ----- */
            ParallelTransition showExpHBox = buildSlideFadeX(
                    ctrl.getHboxExpiredTime(),
                    -30, 0,
                    0, 1,
                    half
            );

            hideExpDtpk.setOnFinished(e -> {
                toggle(ctrl.getDtpkExpirationDate(), false);
                toggle(ctrl.getHboxExpiredTime(), true);
                showExpHBox.play();
            });

            showExpHBox.setOnFinished(e -> {
                showingDatePicker = false;
                isToggleAnimPlaysFlag = false;
            });
            hideExpDtpk.play();
        }

        /* ======  HELPERS  ====== */

        /** builder for parallel anim of Y transition and fade */
        private ParallelTransition buildSlideFadeY(Node node,
                                                   double fromY, double toY,
                                                   double fromOp, double toOp,
                                                   Duration d) {
            TranslateTransition slide = new TranslateTransition(d, node);
            slide.setFromY(fromY);
            slide.setToY(toY);
            slide.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fade   = buildFade(node, fromOp, toOp, d);
            return new ParallelTransition(slide, fade);
        }

        /** builder for parallel anim of X transition and fade */
        private ParallelTransition buildSlideFadeX(Node node,
                                                   double fromX, double toX,
                                                   double fromOp, double toOp,
                                                   Duration d) {
            TranslateTransition slide = new TranslateTransition(d, node);
            slide.setFromX(fromX);
            slide.setToX(toX);
            slide.setInterpolator(Interpolator.EASE_IN);

            FadeTransition fade   = buildFade(node, fromOp, toOp, d);
            return new ParallelTransition(slide, fade);
        }

        /** fade builder */
        private FadeTransition buildFade(Node node, double from, double to, Duration d) {
            FadeTransition ft = new FadeTransition(d, node);
            ft.setFromValue(from);
            ft.setToValue(to);
            ft.setInterpolator(Interpolator.EASE_IN);
            return ft;
        }

        private static void toggle(Node n, boolean visible) {
            n.setVisible(visible);
            n.setManaged(visible);
        }

        /** blink effect on any node (opacity 1 → 0 → 1 over full duration) */
        private Timeline blinkingAnim(Node n, double msTotal) {
            return new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(n.opacityProperty(), 1, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(msTotal / 2),
                            new KeyValue(n.opacityProperty(), 0, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(msTotal),
                            new KeyValue(n.opacityProperty(), 1, Interpolator.EASE_BOTH))
            );
        }
    }

    /* ******************************************************************
     *                          CLASS  UndoRedo
     * *****************************************************************/
    public class UndoRedo {

        /* ===== PUBLIC API ===== */

        public Deque<Command> getUndoStack() {
            return undoStack;
        }
        public Deque<Command> getRedoStack() {
            return redoStack;
        }

        public BooleanProperty canUndoProperty() { return canUndo; }
        public BooleanProperty canRedoProperty() { return canRedo; }

        public boolean hasUnsavedChanges() { return !undoStack.isEmpty(); }

        public UndoRedo()
            { updateState(); }

        public void setUndoRedoDisabled(boolean value) {
            this.undoredoDisabled = value;
            updateState();
        }

        public void addCommandToStack(Command cmd) {
            if (ctrl == null || cmd == null) return;
            undoStack.addLast(cmd);
            redoStack.clear();
            updateState();
        }

        public void undo() {
            if (ctrl == null || undoStack.isEmpty() || undoredoDisabled) return;

            Command cmd = undoStack.removeLast();
            cmd.undo();
            historyActions.setHistory(
                    HistoryActions.HistoryType.INFO,
                    "Undo: " + cmd.getHistoryDescription()
            );
            redoStack.addLast(cmd);
            updateState();
        }

        public void redo() {
            if (ctrl == null || redoStack.isEmpty() || undoredoDisabled) return;

            Command cmd = redoStack.removeLast();
            cmd.execute();
            historyActions.setHistory(
                    HistoryActions.HistoryType.INFO,
                    "Redo: " + cmd.getHistoryDescription()
            );
            undoStack.addLast(cmd);
            updateState();
        }



        /* ===== CORE ===== */

        private boolean undoredoDisabled = false;     // flag

        private final Deque<Command> undoStack = new ArrayDeque<>();
        private final Deque<Command> redoStack = new ArrayDeque<>();

        private final BooleanProperty canUndo = new SimpleBooleanProperty(false);
        private final BooleanProperty canRedo = new SimpleBooleanProperty(false);

        private void updateState() {
            canUndo.set(!undoredoDisabled && !undoStack.isEmpty());
            canRedo.set(!undoredoDisabled && !redoStack.isEmpty());
        }
    }

    /* ******************************************************************
     *                          CLASS  HistoryActions
     * *****************************************************************/
    public class HistoryActions {

        /* ======  PUBLIC API  ====== */

        public enum HistoryType { SUCCESS, WARNING, ERROR, INFO }

        public void setHistory(HistoryType type, String historyText) {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));      // get cur time

            InlineCssTextArea area = ctrl.getHistoryTxtArea();
            Label curHistoryLbl = ctrl.getLblCurHistory();

            /*  create string like: [TYPE] message [HH:mm:ss]\n  */
            String prefix = "[" + type.name() + "] ";
            String suffix = " [" + time + "]\n";
            String fullMessage = prefix + historyText + suffix;

            /* position */
            int start = area.getLength();
            area.appendText(fullMessage);
            int end = area.getLength();

            /* set color */
            Color color = switch (type) {
                case SUCCESS -> Color.rgb(7, 213, 0);
                case WARNING -> Color.rgb(213, 202, 0);
                case ERROR   -> Color.rgb(213, 0, 0);
                case INFO    -> Color.rgb(0, 149, 213);
            };

            /* css style for each segment */
            String prefixStyle = String.format("-fx-fill: %s; -fx-font-weight: bold;", toWebColor(color));
            String mainStyle = "-fx-fill: #dddddd;";
            String suffixStyle = "-fx-fill: gray;";

            area.setStyle(start, start + prefix.length(), prefixStyle);
            area.setStyle(start + prefix.length(), start + prefix.length() + historyText.length(), mainStyle);
            area.setStyle(start + prefix.length() + historyText.length(), end, suffixStyle);

            /*  in Label print only: [TYPE] short history text  */
            byte mnocihl = 40;     // max_number_of_chars_in_history_label
            String shortHistoryText = historyText.length() > mnocihl
                    ? historyText.substring(0, mnocihl) + "..."
                    : historyText;

            curHistoryLbl.setText("[" + type.name() + "] " + shortHistoryText);

            curHistoryLbl.setTextFill(color);
        }
        /* ========================== */



        /* ======  CORE  ====== */

        // convert Color to string like: "#RRGGBB"
        private static String toWebColor(Color color) {
            return String.format("#%02X%02X%02X",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255));
        }
    }

    /* ******************************************************************
     *                          CLASS  UiFlowActions
     * *****************************************************************/
    public class UiFlowActions {
        public static final Logger LOGGER = Logger.getLogger(UiFlowActions.class.getName());

        public void runUpdateFlow(boolean showNotAvailableUpdatesNotify) {
            if (!showNotAvailableUpdatesNotify && !UpdateUtility.beginAutomaticCheck()) return;

            final Task<String> checkTask = new Task<>() {
                @Override protected String call() throws Exception {
                    updateProgress(0.15, 1); Thread.sleep(400);
                    updateProgress(0.50, 1);
                    String latest = UpdateUtility.getLatestGitVersion();
                    updateProgress(0.80, 1); Thread.sleep(400);
                    return latest;
                }
            };

            ProgressSpinner spinner = new ProgressSpinner(
                    12, 30, 5, 20,
                    Color.rgb(37, 171, 228),
                    Font.font("Roboto", 18),
                    500,
                    "Checking for updates");
            spinner.showLoadingWindow(
                    null,
                    false,
                    checkTask,
                    latest -> {
                        try {
                            boolean updateAvailable = UpdateUtility.compareVersions(latest, AppConstants.APP_VERSION) > 0;

                            if (updateAvailable) {
                                boolean courseIsOpen = ctrl != null && Launcher.getCourseInfo() != null;
                                String prompt = "Do you want to download and install it now?";
                                if (courseIsOpen) {
                                    prompt += "\n\nUnsaved course changes will be saved before installation.";
                                }

                                boolean downloadConfirmed = AlertFX.showQuestion(
                                        "New v" + latest + " update is available",
                                        prompt,
                                        "Download update");

                                if (downloadConfirmed) {
                                    if (!menuActions.savePendingChangesBeforeUpdate()) return;

                                    /* install task */
                                    final Task<Void> installTask = new Task<>() {
                                        @Override protected Void call() throws Exception {
                                            updateProgress(0.25, 1); Thread.sleep(300);
                                            updateProgress(0.70, 1);
                                            UpdateUtility.installUpdate(latest, p -> updateProgress(p, 1));
                                            updateProgress(0.80, 1); Thread.sleep(200);
                                            updateProgress(1.0, 1);
                                            return null;
                                        }
                                    };

                                    ProgressSpinner installSpinner = new ProgressSpinner(
                                            12, 30, 5, 20,
                                            Color.rgb(45, 215, 75),
                                            Font.font("Roboto", 16),
                                            500,
                                            "Downloading update v" + latest
                                    );

                                    installSpinner.showLoadingWindow(
                                            courseIsOpen ? ctrl.getStage().getScene().getWindow() : null,
                                            true,
                                            installTask,
                                            e -> {
                                                Platform.exit();
                                            },
                                            e -> {
                                                Platform.runLater(() -> {
                                                    AlertFX.showNotification(
                                                            AlertMessageType.ERROR,
                                                            "Installation failed",
                                                            "Message: " + installTask.getException().getMessage());
                                                    UiFlowActions.LOGGER.log(
                                                            Level.SEVERE, "=== ERROR DURING INSTALLING UPDATE ===", installTask.getException());
                                                });
                                            },
                                            "update-install-thread",
                                            0.0,
                                            0.3,
                                            new Image(Objects.requireNonNull(Actions.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_i_icon_256x256.png")))
                                            );
                                }

                            } else if (showNotAvailableUpdatesNotify) {
                                AlertFX.showNotification(
                                        AlertMessageType.INFO,
                                        "There are no new updates",
                                        "You have all the latest updates installed."
                                );
                            }
                        } catch (NoInternetConnection ex) {
                            AlertFX.showNotification(
                                    AlertMessageType.ERROR,
                                    "No internet connection",
                                    "Could not check for updates. Please connect to the Internet and try again."
                            );
                        }
                    },
                    ex -> {
                        if (ex instanceof NoInternetConnection) {
                            AlertFX.showNotification(
                                    AlertMessageType.ERROR,
                                    "No internet connection",
                                    "Could not check for updates. Please connect to the Internet "
                                            + "and try again."
                            );
                            return;
                        }
                        AlertFX.showNotification(
                                AlertMessageType.ERROR,
                                "Update check failed",
                                "Message: " + ex.getMessage()
                        );
                        UiFlowActions.LOGGER.log(
                                Level.SEVERE, "=== UPDATE CHECK FAILED ===", ex);
                    },
                    "update-check-thread",
                    0.0,
                    0.3,
                    new Image(Objects.requireNonNull(Actions.class.getResourceAsStream("/com/coursemanagerfx/ui/icons/app/cmfx_u_icon_256x256.png")))
            );
        }

        public void runCourseDataLoadingFlow() {
            if (ctrl == null) return;

            final Task<Void> loadingTask = new Task<>() {
                @Override protected Void call() throws Exception {
                    updateProgress(0.14, 1); Thread.sleep(500);
                    updateProgress(0.61, 1); Thread.sleep(400);

                    Platform.runLater(repaint::initGroupTabs);

                    updateProgress(0.91, 1); Thread.sleep(500);
                    return null;
                }
            };

            ProgressSpinner spinner = new ProgressSpinner(
                    12, 40, 8, 30,
                    Color.rgb(140, 140, 140),
                    Font.font("Roboto", 18),
                    800,
                    "Course data loading");
            spinner.showLoadingWindow(
                    ctrl.getStage().getScene().getWindow(),
                    true,
                    loadingTask,
                    unused -> {},
                    ex -> {                               // onFailure
                        AlertFX.showNotification(
                                AlertMessageType.ERROR,
                                "Data loading failed",
                                "Something went wrong during data loading"
                        );
                        LOGGER.log(Level.SEVERE, "=== DATA LOADING FAILED ===", ex);
                    },
                    "course-data-loading-thread");
        }
    }
}
