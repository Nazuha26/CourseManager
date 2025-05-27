package com.coursemanagerfx.logic;

import com.coursemanagerfx.CM_HELPER;
import com.coursemanagerfx.Launcher;
import com.coursemanagerfx.animations.WindowOutAnimation;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX;
import com.coursemanagerfx.controllers.dialogs.alert.AlertFX_type;
import com.coursemanagerfx.controllers.dialogs.exceptions.SaveException;
import com.coursemanagerfx.controllers.main.Main_controller;
import com.coursemanagerfx.controllers.main.StudentPanel_controller;
import com.coursemanagerfx.custom_ui.ProgressSpinner;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.EventTypes;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.date.EventDate;
import com.coursemanagerfx.logic.basic.event.date.ExpDateStrings;
import com.coursemanagerfx.logic.commands.Command;
import com.coursemanagerfx.logic.commands.event_comms.AddEventCommand;
import com.coursemanagerfx.logic.commands.event_comms.DeleteEventCommand;
import com.coursemanagerfx.logic.commands.event_comms.EditEventCommand;
import com.coursemanagerfx.logic.security.CmanSecurityUtility;
import com.coursemanagerfx.logic.utilities.ExcelExportUtility;
import com.coursemanagerfx.logic.utilities.HistoryUtility;
import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.coursemanagerfx.CM_HELPER.*;

public final class Actions {

    /* ========================= SINGLETON ========================= */
    private static final Actions INSTANCE = new Actions();
    private Actions() {}
    public static Actions getInstance() { return INSTANCE; }

    /* ========================= CONTROLLER ========================= */
    private Main_controller ctrl;
    public void setController(Main_controller controller) { this.ctrl = controller; }

    /* ========================= SUB-ACTIONS ========================= */
    private final Repaint      repaint     = new Repaint();
    private final Select       select      = new Select();
    private final uiActions    uiActions   = new uiActions();
    private final MenuActions  menuActions = new MenuActions();
    private final IdGenerator  idGenerator = new IdGenerator();
    private final FormAnims    formAnims   = new FormAnims();
    private final UndoRedo     undoRedo    = new UndoRedo();
    private final TaskLoader   taskLoader  = new TaskLoader();

    public Repaint     repaint()         { return repaint; }
    public Select      select()          { return select; }
    public uiActions   uiActions()       { return uiActions; }
    public MenuActions menuActions()     { return menuActions; }
    public IdGenerator getIdGenerator()  { return idGenerator; }
    public FormAnims   formAnims()       { return formAnims; }
    public UndoRedo    undoRedo()        { return undoRedo; }
    public TaskLoader  taskLoader()      { return taskLoader; }

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

            /* show everyone */
            if (searchField.isDisable() || searchText.isEmpty()) {
                list = new ArrayList<>(original);
                searchField.setDisable(original.isEmpty());
            }
            /* show student by filter by searched text */
            else {
                list = original.stream()
                        .filter(s -> s.getName().toLowerCase().contains(searchText))
                        .collect(Collectors.toList());
            }

            list.sort(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));   // always sort by alphabet

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
            if (student != null) {
                ctrl.getEventsTable().getItems().setAll(student.getEvents());
                ctrl.getStatusColumn().setSortType(TableColumn.SortType.ASCENDING);
                ctrl.getEventsTable().sort();
            } else {
                ctrl.getEventsTable().getItems().clear();
                ctrl.getEventsTable().refresh();
            }
        }

        /* repaint group and select student */
        public void refreshStudentView(Group group, Student student) {
            repaintStudentPanels(group);
            select().selectStudentPanel(student, group);
        }

        /* only for AddEventCommand & DeleteEventCommand & EditEventCommand */
        public void smartRefresh(Group group, Student student) {
            Actions.Select select = Actions.getInstance().select();
            Actions.Repaint repaint = Actions.getInstance().repaint();

            if (group == select.getSelectedGroup() && student == select.getSelectedStudent()) {
                repaint.repaintEventTable(student, group);
            } else {
                repaint.refreshStudentView(group, student);
            }
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
            List<Student> sorted = repaint.repaintStudentPanels(group);
            if (!sorted.isEmpty()) {
                selectStudentPanel(sorted.getFirst(), group);
            } else {
                selectedStudent = null;
                repaint.repaintEventTable(null, group);
            }
        }

        /* select student in selected group */
        public void selectStudentPanel(Student student) {
            if (ctrl == null || student == null || selectedGroup == null) return;

            for (Node n : ctrl.getStudentVBox().getChildren()) {
                if (n instanceof BorderPane bp && bp.getCenter() instanceof StackPane sp) {
                    for (Node ch : sp.getChildren()) {
                        if (ch instanceof Button btn && btn.getUserData() instanceof Student st) {
                            btn.getStyleClass().remove("selected-student-panel");
                            if (st.equals(student))
                                btn.getStyleClass().add("selected-student-panel");
                        }
                    }
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
        public void selectAnyOrClear(Group group) {
            if (group == null) return;

            if (!group.getStudents().isEmpty()) {
                Student firstStudent = group.getStudents().getFirst();
                select().selectStudentPanel(firstStudent, group);
            } else {
                repaint().repaintEventTable(null, group);
            }
        }

        /* ========== getters ========== */
        public Group   getSelectedGroup()   { return selectedGroup; }
        public Student getSelectedStudent() { return selectedStudent; }

        /* ========== helper ========== */
        private int indexOfGroup(Group g) {
            Group[] arr = Launcher.getCourseInfo().getCourse();
            for (int i = 0; i < arr.length; i++) if (arr[i] == g) return i;
            return -1;
        }
    }

    /* ******************************************************************
     *                          CLASS  uiActions
     * *****************************************************************/
    public class uiActions {
        private StudentEvent editingEvent;
        public StudentEvent getEditingEvent() {
            return editingEvent;
        }

        /* ======  PUBLIC API  ====== */
        public void startEditing(StudentEvent event) {
            if (ctrl == null) return;

            if (!ctrl.getMainTopPane().isVisible()) {
                formAnims.loadEventInfoPane();
                formAnims.mainTopPanelInOut(FormAnims.State.SHOW);
            }

            editingEvent = event;

            for (Node node : ctrl.getTabHBox().getChildren()) node.setDisable(true);      // disable group tab buttons
            for (Node node : ctrl.getStudentVBox().getChildren()) node.setDisable(true);  // disable student panels

            ctrl.getTxtFieldSearch().setDisable(true);  // disable search field
            ctrl.getBtnAddStudent().setDisable(true);

            ctrl.getBtnCancelEvent().setVisible(false);
            ctrl.getBtnCancelEvent().setManaged(false);

            ctrl.getLblEditing().setVisible(true);
            ctrl.getLblEditing().setManaged(true);

            ctrl.getEventsTable().getColumns().forEach(col -> col.setSortable(false));       // turn off sortable of event table

            /* === fill in the panel === */
            ctrl.getComBoxEventType().getSelectionModel().select(event.getType().getEventType().name()); // type
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
            ctrl.getComBoxExpiredTime().getSelectionModel().select(ExpDateStrings.DAYS);
            /* ---------------------------------- */

            /* replace "Create event" button to "Save event" button */
            ctrl.getBtnCreateEvent().setText("Save");
            ctrl.getBtnCreateEvent().setOnAction(ae -> saveEventAction());

            /* show "Delete event" button */
            ctrl.getBtnDeleteEvent().setVisible(true);
            ctrl.getBtnDeleteEvent().setManaged(true);
            ctrl.getBtnDeleteEvent().setOnAction(ae -> deleteEventAction());
        }

        public void stopEditing() {
            if (ctrl == null) return;

            formAnims.mainTopPanelInOut(FormAnims.State.HIDE);

            editingEvent = null;

            for (Node node : ctrl.getTabHBox().getChildren()) node.setDisable(false);       // enable group tab buttons
            for (Node node : ctrl.getStudentVBox().getChildren()) node.setDisable(false);   // enable student panels
            ctrl.getTxtFieldSearch().setDisable(false);     // enable search field

            ctrl.getBtnAddStudent().setDisable(false);

            ctrl.getBtnCancelEvent().setVisible(true);
            ctrl.getBtnCancelEvent().setManaged(true);

            ctrl.getLblEditing().setVisible(false);
            ctrl.getLblEditing().setManaged(false);

            ctrl.getEventsTable().getColumns().forEach(col -> col.setSortable(true));       // turn on sortable of event table

            ctrl.getComBoxEventType().getSelectionModel().select(EventTypes.MOD_1.getEventType().name());
            ctrl.getTxtAreaEventDescrp().setText("");

            ctrl.getBtnDeleteEvent().setVisible(false);
            ctrl.getBtnDeleteEvent().setManaged(false);

            ctrl.getBtnCreateEvent().setText("Create");
            ctrl.getBtnCreateEvent().setOnAction(evt -> createEventAction());
        }

        public void createEventAction() {
            if (ctrl == null) return;
            if (isInvalidEventForm()) return;

            EventTypes selectedType = returnTypeByName(ctrl.getComBoxEventType().getSelectionModel().getSelectedItem());    // get event type

            /* get creation date */
            LocalDate creationDateRaw = ctrl.getDtpkCreationDate().getValue();
            EventDate creationDate = new EventDate(
                    creationDateRaw.getDayOfMonth(),
                    creationDateRaw.getMonthValue(),
                    creationDateRaw.getYear()
            );
            /* ----------------- */

            String description = ctrl.getTxtAreaEventDescrp().getText().trim();    // get description date
            int mark = ctrl.getSpinnerMark().getValue();                           // get mark

            /* expiration date */
            LocalDate expirationDateRaw = calculateExpirationDate(creationDateRaw);
            if (expirationDateRaw == null) {
                AlertFX.showNotification(ctrl.getStage().getScene().getWindow(),
                        AlertFX_type.ERROR,
                        "Expiration date error",
                        "Failed to calculate expiration date. Please check the form.",
                        true);
                return;
            }
            if (isInvalidExpirationDate(creationDateRaw, expirationDateRaw)) {
                AlertFX.showNotification(ctrl.getStage().getScene().getWindow(),
                        AlertFX_type.ERROR,
                        "Expiration date error",
                        "Expiration date cannot be less than or equal to creation date",
                        true
                );
                return;
            }

            EventDate expirationDate = new EventDate(
                    expirationDateRaw.getDayOfMonth(),
                    expirationDateRaw.getMonthValue(),
                    expirationDateRaw.getYear()
            );
            /* --------------- */

            int eventID = idGenerator.genUniqueEventId();
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
            undoRedo.addCommand(cmd);
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
                ctrl.getComBoxExpiredTime().getSelectionModel().select(ExpDateStrings.DAYS);
                ctrl.getDtpkExpirationDate().setValue(null);
            }
            formAnims.expDateInOut();
        }

        public void mainExitAction() {
            if (ctrl == null) return;

            if (undoRedo.undoStack.isEmpty()) {
                WindowOutAnimation.play(
                        ctrl,
                        ctrl.getStage().getWidth(),
                        ctrl.getStage().getHeight(),
                        Main_controller.MW_ANIM_STRIPE_COUNT,
                        Duration.seconds(1),
                        ctrl.getStage()::close);
                return;
            }

            boolean confirmed = AlertFX.showConfirm(ctrl.getStage().getScene().getWindow(),
                    AlertFX_type.WARNING,
                    "You have unsaved changes.",
                    "Do you want to exit without saving?"
            );

            if (confirmed) {
                WindowOutAnimation.play(
                        ctrl,
                        ctrl.getStage().getWidth(),
                        ctrl.getStage().getHeight(),
                        Main_controller.MW_ANIM_STRIPE_COUNT,
                        Duration.seconds(1),
                        ctrl.getStage()::close);
            }
        }

        /* ========================== */

        /* ======  CORE  ====== */
        private void saveEventAction() {
            if (ctrl == null || editingEvent == null) return;
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

            EventTypes newType = returnTypeByName(ctrl.getComBoxEventType()
                                        .getSelectionModel().getSelectedItem()); // type

            int newMark = ctrl.getSpinnerMark().getValue();                      // mark

            /* expiration date */
            LocalDate newExpirationDateRaw = calculateExpirationDate(creationDateRaw);
            if (newExpirationDateRaw == null) {
                AlertFX.showNotification(ctrl.getStage().getScene().getWindow(),
                        AlertFX_type.ERROR,
                        "Expiration date error",
                        "Failed to calculate expiration date. Please check the form.",
                        true);
                return;
            }
            if (isInvalidExpirationDate(creationDateRaw, newExpirationDateRaw)) {
                AlertFX.showNotification(ctrl.getStage().getScene().getWindow(),
                        AlertFX_type.ERROR,
                        "Expiration date error",
                        "Expiration date cannot be less than or equal to creation date",
                        true
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
                    editingEvent.getID(),
                    newCrt,
                    newDesc,
                    newMark,
                    newExp,
                    newType
            );

            EditEventCommand cmd = new EditEventCommand(
                    select.getSelectedGroup(),
                    select.getSelectedStudent(),
                    editingEvent,
                    editedCopy
            );
            cmd.execute();
            undoRedo.addCommand(cmd);

            stopEditing();
            formAnims.mainTopPanelInOut(FormAnims.State.HIDE);
        }

        private void deleteEventAction() {
            if (ctrl == null || editingEvent == null) return;

            Window owner = ctrl.getStage().getScene().getWindow();

            String shortDesc = editingEvent.getDescription().length() > 10
                    ? editingEvent.getDescription().substring(0, 10) + "..."
                    : editingEvent.getDescription();
            boolean isDelete = AlertFX.showConfirm(owner,
                    AlertFX_type.INFO,
                    "Event deleting",
                    "Do you want to delete \"" + shortDesc + "\" event?");

            if (isDelete) {
                StudentEvent currentEvent = editingEvent;
                DeleteEventCommand cmd = new DeleteEventCommand(
                        select.getSelectedGroup(),
                        select.getSelectedStudent(),
                        currentEvent
                );
                cmd.execute();
                undoRedo.addCommand(cmd);

                stopEditing();
                formAnims.mainTopPanelInOut(FormAnims.State.HIDE);
            }
        }

        /* ======  HELPERS  ====== */
        /* get event type by name in combobox */
        private EventTypes returnTypeByName(String name) {
            for (EventTypes mod : EventTypes.values()) {
                if (mod.getEventType().name().equals(name)) {
                    return mod;
                }
            }
            return EventTypes.CUSTOM;
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

        /* private method for show notification */
        private static void show(Window owner, String header, String msg)
            { AlertFX.showNotification(owner, AlertFX_type.WARNING, header, msg, true); }

        /* calculate expiration date depending on creation date */
        private LocalDate calculateExpirationDate(LocalDate creationDateRaw) {
            if (ctrl.getDtpkExpirationDate().isVisible()) {
                return ctrl.getDtpkExpirationDate().getValue();
            } else if (ctrl.getSpinnerExpTimeCount().isVisible() && ctrl.getComBoxExpiredTime().isVisible()) {
                int count = ctrl.getSpinnerExpTimeCount().getValue();
                String unit = ctrl.getComBoxExpiredTime().getSelectionModel().getSelectedItem();

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
        /* done */
        public void toHomeAction() {
            Window owner = ctrl.getStage().getScene().getWindow();

            boolean yes = AlertFX.showConfirm(owner,
                    AlertFX_type.INFO,
                    "Return to Home",
                    "Are you sure you want to leave the current course and return to the home screen?");
            if (!yes) return;

            try {
                if (!LAST_RUN_FILE.delete()) {
                    System.err.println("Failed to delete file: " + LAST_RUN_FILE.getAbsolutePath());
                    AlertFX.showNotification(
                            null,
                            AlertFX_type.ERROR,
                            "Startup Error",
                            "Failed to delete cache file:\n" + LAST_RUN_FILE.getAbsolutePath() +
                                    "\n\nPlease try restarting the app or delete it manually.",
                            true
                    );
                    return;
                }
            } catch (Exception ex) {
                System.err.println("Error deleting file: " + LAST_RUN_FILE.getAbsolutePath());
                AlertFX.showNotification(
                        null,
                        AlertFX_type.ERROR,
                        "Startup Error",
                        "An unexpected error occurred while deleting file:\n" + LAST_RUN_FILE.getAbsolutePath() +
                                "\n\nDetails: " + ex.getMessage(),
                        true
                );
                return;
            }

            AlertFX.showNotification(owner,
                    AlertFX_type.INFO,
                    "Restart Required",
                    "To apply the changes, please restart the application.",
                    true);

            WindowOutAnimation.play(
                    ctrl,
                    ctrl.getStage().getWidth(),
                    ctrl.getStage().getHeight(),
                    Main_controller.MW_ANIM_STRIPE_COUNT,
                    Duration.seconds(1),
                    ctrl.getStage()::close
            );
        }
        /* done */
        public void saveAction() {
            Window owner = ctrl.getStage().getScene().getWindow();
            try {
                File file = new File(CM_HELPER.COURSES_DIR.getAbsolutePath() +
                        File.separator + Launcher.getCourseInfo().getCourseName() + ".cman");    // full path with file

                CmanSecurityUtility.updateSecureFile(
                        Launcher.getCourseInfo().getCourse(), file, Launcher.getCourseInfo().getPassword());
                Actions.getInstance().undoRedo().getUndoStack().clear();    // clear undo stack
                Actions.getInstance().undoRedo().getRedoStack().clear();    // clear redo stack

                /*AlertFX.showNotification(
                        owner,
                        AlertFX_type.INFO,
                        "Saved successfully",
                        "",
                        true);*/

                HistoryUtility.setHistory(
                        ctrl.getRichTxtPaneHistory(),
                        ctrl.getLblCurHistory(),
                        HistoryUtility.Types.SUCCESS,
                        "Saved successfully"
                );
            } catch (Exception e) {
                AlertFX.showNotification(owner,
                        AlertFX_type.ERROR,
                        "SAVING ERROR",
                        "Message: " + e.getMessage(),
                        true);
                throw new SaveException("=== FATAL SAVING ERROR ===", e);
            }
        }
        /* done */
        public void exportAction() {
            ExcelExportUtility.exportToExcel(
                    Launcher.getCourseInfo().getCourse(),
                    Launcher.getCourseInfo().getCourseName(),
                    new File(System.getProperty("user.home") + File.separator + "Desktop"));
        }

        /* === EDIT === */
        // TODO options
        public void optionsAction() {
            // TODO OPTIONS
            Window owner = ctrl.getStage().getScene().getWindow();
            AlertFX.showNotification(owner,
                    AlertFX_type.INFO,
                    "Options soon...",
                    "Soon...",
                    true
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
        public int genUniqueStudentId() {
            Set<Integer> existingIds = new HashSet<>();

            for (Group group : Launcher.getCourseInfo().getCourse())
                for (Student student : group.getStudents())
                    existingIds.add(student.getStudentID());

            int newId = 1_000_000;
            while (existingIds.contains(newId)) newId++;
            return newId;
        }
        public int genUniqueEventId() {
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
            ctrl.getHistoryTopPane().setManaged(false);
            ctrl.getHistoryTopPane().setVisible(false);
            ctrl.getEventInfoTopPane().setManaged(true);
            ctrl.getEventInfoTopPane().setVisible(true);
        }
        public void loadHistoryPane() {
            if (ctrl == null) return;
            ctrl.getEventInfoTopPane().setManaged(false);
            ctrl.getEventInfoTopPane().setVisible(false);
            ctrl.getHistoryTopPane().setManaged(true);
            ctrl.getHistoryTopPane().setVisible(true);
        }
        /* ========================== */

        /* ======  CORE  ====== */

        /** plays “bottom-out / top-in” sequence */
        private void playIn(double ms) {
            Duration half = Duration.millis(ms / 2);

            /* --- hide bottom info panel --- */
            ParallelTransition hideBottom = buildSlideFadeY(
                    ctrl.getAddEventBottomPane(),
                    0, 30,
                    1, 0,
                    half
            );

            /* --- show top info panel (starts after bottom hidden) --- */
            ParallelTransition showTop = buildSlideFadeX(
                    ctrl.getMainTopPane(),
                    50, 0,
                    0, 1,
                    half
            );

            hideBottom.setOnFinished(ev -> {
                toggle(ctrl.getAddEventBottomPane(), false);
                toggle(ctrl.getMainTopPane(),        true);
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
                    ctrl.getMainTopPane(),
                    0,  50,
                    1, 0,
                    half
            );

            /* --- show bottom info panel (starts after top hidden) --- */
            ParallelTransition showBottom = buildSlideFadeY(
                    ctrl.getAddEventBottomPane(),
                    30, 0,
                    0, 1,
                    half
            );

            hideTop.setOnFinished(ev -> {
                toggle(ctrl.getMainTopPane(),        false);
                toggle(ctrl.getAddEventBottomPane(), true);
                showBottom.play();
            });

            showBottom.setOnFinished(ev -> isAnimPlaysFlag = false);

            new ParallelTransition(hideTop, blinkingAnim(ctrl.getTableStackPane(), ms)).play();
        }

        private void playExpIn(double ms) {
            Duration half = Duration.millis(ms / 2);

            /* ----- hide expiration hbox ----- */
            ParallelTransition hideExpHBox = buildSlideFadeX(
                    ctrl.getHboxExpiredDate(),
                    0, -30,
                    1, 0,
                    half
                    );

            /* ----- show expiration data picker ----- */
            ParallelTransition showExpDtpk = buildSlideFadeX(
                    ctrl.getDtpkExpirationDate(),
                    30, 0,
                    0, 1,
                    half
            );

            hideExpHBox.setOnFinished(e -> {
                toggle(ctrl.getHboxExpiredDate(), false);
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

            /* ----- show expiration hbox ----- */
            ParallelTransition showExpHBox = buildSlideFadeX(
                    ctrl.getHboxExpiredDate(),
                    -30, 0,
                    0, 1,
                    half
            );

            hideExpDtpk.setOnFinished(e -> {
                toggle(ctrl.getDtpkExpirationDate(), false);
                toggle(ctrl.getHboxExpiredDate(), true);
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

        private final Deque<Command> undoStack = new ArrayDeque<>();
        private final Deque<Command> redoStack = new ArrayDeque<>();

        public Deque<Command> getUndoStack() {
            return undoStack;
        }
        public Deque<Command> getRedoStack() {
            return redoStack;
        }

        public void addCommand(Command cmd) {
            if (ctrl == null || cmd == null) return;
            undoStack.addLast(cmd);
            redoStack.clear();
        }

        public void undo() {
            if (ctrl == null || undoStack.isEmpty()) return;

            Command cmd = undoStack.removeLast();
            cmd.undo();
            HistoryUtility.setHistory(ctrl.getRichTxtPaneHistory(), ctrl.getLblCurHistory(),
                    HistoryUtility.Types.INFO, "Undo: " + cmd.getHistoryDescription());
            redoStack.addLast(cmd);
        }

        public void redo() {
            if (ctrl == null || redoStack.isEmpty()) return;

            Command cmd = redoStack.removeLast();
            cmd.execute();
            HistoryUtility.setHistory(ctrl.getRichTxtPaneHistory(), ctrl.getLblCurHistory(),
                    HistoryUtility.Types.INFO, "Redo: " + cmd.getHistoryDescription());
            undoStack.addLast(cmd);
        }
    }

    /* ******************************************************************
     *                          CLASS  TaskLoader
     * *****************************************************************/
    public class TaskLoader {
        public static final Logger LOGGER = Logger.getLogger(TaskLoader.class.getName());

        public void loadTask(int ms, Runnable onSuccess, Consumer<Throwable> onFailure) {
            if (ctrl == null) return;

            int totalSteps = 10;
            long baseDelay = (long) ms / totalSteps;

            Task<Void> loadTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    for (int i = 0; i < totalSteps; i++) {
                        /* noise for the wait time (±25%) */
                        long noise = (long) (Math.random() * baseDelay * 0.5 - baseDelay * 0.25);
                        Thread.sleep(baseDelay + noise);

                        /* noise for the progress (±2%) */
                        double progress = (i + 1) / (double) totalSteps;
                        double noisyProgress = Math.min(1.0, Math.max(0.0, progress + (Math.random() - 0.5) * 0.04));

                        updateProgress(noisyProgress, 1.0);
                    }
                    return null;
                }
            };

            /* === creating progress spinner === */
            ProgressSpinner spinner = new ProgressSpinner();
            spinner.setFont(Font.font("Roboto", 32));
            spinner.progressProperty().bind(loadTask.progressProperty());

            StackPane root = new StackPane(spinner);
            root.setAlignment(Pos.CENTER);
            root.setStyle("-fx-background-color: transparent");
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            Stage dialog = new Stage();
            dialog.initStyle(StageStyle.TRANSPARENT);
            //dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(ctrl.getStage());
            dialog.setScene(scene);
            dialog.setAlwaysOnTop(true);
            /* ================================= */

            /* === centering its === */
            dialog.setOnShown(ev -> {
                Window owner = dialog.getOwner();
                if (owner != null) {
                    dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
                    dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
                }
            });
            /* ===================== */

            loadTask.setOnSucceeded(e -> {
                dialog.close();
                if (onSuccess != null) onSuccess.run();
            });

            loadTask.setOnFailed(e -> {
                dialog.close();
                if (onFailure != null) onFailure.accept(e.getSource().getException());
            });

            new Thread(loadTask).start();
            dialog.show();
        }
    }
}