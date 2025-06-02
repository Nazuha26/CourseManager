package com.coursemanagerfx.logic;

import com.coursemanagerfx.AppConstants;
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
import com.coursemanagerfx.logic.basic.event.EventCategories;
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
import com.coursemanagerfx.logic.utilities.UpdateUtility;
import com.coursemanagerfx.logic.utilities.config_api.ConfigManager;
import com.coursemanagerfx.logic.utilities.exceptions.NoInternetConnection;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Consumer;
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
    private final Repaint       repaint       = new Repaint();
    private final Select        select        = new Select();
    private final uiActions     uiActions     = new uiActions();
    private final MenuActions   menuActions   = new MenuActions();
    private final IdGenerator   idGenerator   = new IdGenerator();
    private final FormAnims     formAnims     = new FormAnims();
    private final UndoRedo      undoRedo      = new UndoRedo();
    private final TaskLoader    taskLoader    = new TaskLoader();
    private final UpdateActions updateActions = new UpdateActions();

    public Repaint       repaint()         { return repaint; }
    public Select        select()          { return select; }
    public uiActions     uiActions()       { return uiActions; }
    public MenuActions   menuActions()     { return menuActions; }
    public IdGenerator   idGenerator()     { return idGenerator; }
    public FormAnims     formAnims()       { return formAnims; }
    public UndoRedo      undoRedo()        { return undoRedo; }
    public TaskLoader    taskLoader()      { return taskLoader; }
    public UpdateActions updateActions()   { return updateActions; }

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

            if (!ctrl.getInfoTopPane().isVisible()) {
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

            ctrl.getComBoxEventCategory().getSelectionModel().select(EventCategories.MOD_1.getEventCategory().name());

            ctrl.getBtnDeleteEvent().setVisible(false);
            ctrl.getBtnDeleteEvent().setManaged(false);

            ctrl.getBtnCreateEvent().setText("Create");
            ctrl.getBtnCreateEvent().setOnAction(evt -> createEventAction());

            clearAllInfoData();
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
                ctrl.getComBoxExpTimeType().getSelectionModel().select(ExpDateStrings.DAYS);
                ctrl.getDtpkExpirationDate().setValue(null);
            }
            formAnims.expDateInOut();
            isShowingDataPickerFlag = !formAnims.showingDatePicker;
        }

        public void clearAllInfoData() {
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
        private boolean isShowingDataPickerFlag = false;

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

            EventCategories newType = returnCategoryByName(ctrl.getComBoxEventCategory()
                                        .getSelectionModel().getSelectedItem()); // type

            double newMark = ctrl.getSpinnerMark().getValue();                      // mark

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
        /* get event category by name in combobox */
        private EventCategories returnCategoryByName(String name) {
            for (EventCategories mod : EventCategories.values()) {
                if (mod.getEventCategory().name().equals(name)) {
                    return mod;
                }
            }
            return EventCategories.CUSTOM;
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
            Window owner = ctrl.getStage().getScene().getWindow();

            boolean yes = AlertFX.showConfirm(owner,
                    AlertFX_type.INFO,
                    "Return to Home",
                    "Are you sure you want to leave the current course and return to the home screen?");
            if (!yes) return;

            /*try {
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
            }*/

            ConfigManager.setOpenCourse("none");        // set opened course as none for backing home

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
                File file = AppConstants.COURSES_PATH
                        .resolve(Launcher.getCourseInfo().getCourseName() + ".cman")
                        .toFile();

                CmanSecurityUtility.updateSecureFile(
                        Launcher.getCourseInfo().getCourse(), file, Launcher.getCourseInfo().getPassword());

                undoRedo.getUndoStack().clear();    // clear undo stack
                undoRedo.getRedoStack().clear();    // clear redo stack
                undoRedo.updateState();

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
            Window owner = ctrl.getStage().getScene().getWindow();

            if (Launcher.getCourseInfo().isEmpty()) {
                AlertFX.showNotification(
                        owner,
                        AlertFX_type.WARNING,
                        "There is nothing to export",
                        "The whole course is empty, there is nothing to export to Excel.",
                        true
                );
                return;
            }

            boolean successExport = ExcelExportUtility.exportToExcel(
                    Launcher.getCourseInfo().getCourse(),
                    Launcher.getCourseInfo().getCourseName(),
                    new File(System.getProperty("user.home") + File.separator + "Desktop"));
            if (successExport) {
                AlertFX.showNotification(
                        owner,
                        AlertFX_type.INFO,
                        "Export completed successfully",
                        "The Excel file has been saved to your Desktop.",
                        true
                );
            }
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

            /* --- show top info panel (starts after bottom hidden) --- */
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

            /* --- show bottom info panel (starts after top hidden) --- */
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

            /* ----- show expiration data picker ----- */
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

            /* ----- show expiration hbox ----- */
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

        private final Deque<Command> undoStack = new ArrayDeque<>();
        private final Deque<Command> redoStack = new ArrayDeque<>();

        public Deque<Command> getUndoStack() {
            return undoStack;
        }
        public Deque<Command> getRedoStack() {
            return redoStack;
        }

        private final BooleanProperty canUndo = new SimpleBooleanProperty(false);
        private final BooleanProperty canRedo = new SimpleBooleanProperty(false);

        public BooleanProperty canUndoProperty() { return canUndo; }
        public BooleanProperty canRedoProperty() { return canRedo; }

        private void updateState() {
            canUndo.set(!undoStack.isEmpty());
            canRedo.set(!redoStack.isEmpty());
        }

        public UndoRedo()
            { updateState(); }

        public void addCommand(Command cmd) {
            if (ctrl == null || cmd == null) return;
            undoStack.addLast(cmd);
            redoStack.clear();
            updateState();
        }

        public void undo() {
            if (ctrl == null || undoStack.isEmpty()) return;

            Command cmd = undoStack.removeLast();
            cmd.undo();
            HistoryUtility.setHistory(ctrl.getRichTxtPaneHistory(), ctrl.getLblCurHistory(),
                    HistoryUtility.Types.INFO, "Undo: " + cmd.getHistoryDescription());
            redoStack.addLast(cmd);
            updateState();
        }

        public void redo() {
            if (ctrl == null || redoStack.isEmpty()) return;

            Command cmd = redoStack.removeLast();
            cmd.execute();
            HistoryUtility.setHistory(ctrl.getRichTxtPaneHistory(), ctrl.getLblCurHistory(),
                    HistoryUtility.Types.INFO, "Redo: " + cmd.getHistoryDescription());
            undoStack.addLast(cmd);
            updateState();
        }
    }

    /* ******************************************************************
     *                          CLASS  TaskLoader
     * *****************************************************************/
    public class TaskLoader {
        public static final Logger LOGGER = Logger.getLogger(TaskLoader.class.getName());

        public void loadTask(int ms,
                             ProgressSpinner ps,
                             Runnable onSuccess,
                             Consumer<Throwable> onFailure) {
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
            ps.progressProperty().bind(loadTask.progressProperty());
            Platform.runLater(ps::show);

            loadTask.setOnSucceeded(e -> {
                Platform.runLater(() -> {
                    if (onSuccess != null) onSuccess.run();
                });
            });

            loadTask.setOnFailed(e -> {
                Platform.runLater(() -> {
                    if (onFailure != null) onFailure.accept(loadTask.getException());
                });
            });

            new Thread(loadTask).start();
        }

        public <T> void loadRealTask(ProgressSpinner ps,
                                     Task<T> realTask,
                                     Consumer<T> onSuccess,
                                     Consumer<Throwable> onFailure) {
            /* if (ctrl == null) return; we do not need to check it here cause this is common method */

            // === создаём спиннер ===
            ps.progressProperty().bind(realTask.progressProperty());
            Platform.runLater(ps::show);

            realTask.setOnSucceeded(e -> {
                Platform.runLater(() -> {
                    if (onSuccess != null) onSuccess.accept(realTask.getValue());
                });
            });

            realTask.setOnFailed(e -> {
                Platform.runLater(() -> {
                    if (onFailure != null) onFailure.accept(realTask.getException());
                });
            });

            new Thread(realTask).start();
        }
    }

    /* ******************************************************************
     *                          CLASS  UpdateActions
     * *****************************************************************/
    public static class UpdateActions {
        /* ===== PUBLIC API ===== */

        public void checkAndInstallUpdate(Window owner, boolean showNotAvailableUpdatesNotify) {
            checkForUpdates(
                    owner,
                    latest -> {

                        boolean yes = AlertFX.showConfirm(
                                owner, AlertFX_type.INFO,
                                "New v" + latest + " update is available",
                                "Do you want to install it now?");
                        if (yes) installUpdate(latest, owner);
                    },

                    () -> {
                        if (showNotAvailableUpdatesNotify)
                            AlertFX.showNotification(owner,
                                    AlertFX_type.INFO,
                                    "There are no new updates",
                                    "You have all the latest updates installed.",
                                    true);
                    }
            );
        }



        /* ===== CORE ===== */
        private void checkForUpdates(Window owner,
                                     Consumer<String> onUpdateAvailable,
                                     Runnable onUpToDate) {

            ProgressSpinner ps = new ProgressSpinner(
                    owner,
                    ProgressSpinner.Style.SMALL,
                    Color.rgb(40, 75, 220),
                    ProgressSpinner.Position.BOTTOM,
                    Modality.NONE,
                    "Checking for updates");

            Task<String> task = new Task<>() {
                @Override protected String call() throws Exception {
                    updateProgress(0.15, 1); Thread.sleep(400);
                    updateProgress(0.50, 1);
                    String latest = UpdateUtility.getLatestGitVersion();
                    updateProgress(0.80, 1); Thread.sleep(400);
                    return latest;
                }
            };

            Actions.getInstance().taskLoader().loadRealTask(
                    ps, task,
                    latest -> {
                        ps.close();

                        try {
                            if (!"-1".equals(latest)
                                    && UpdateUtility.compareVersions(latest, AppConstants.CUR_VERSION) > 0) {
                                onUpdateAvailable.accept(latest);
                            } else {
                                onUpToDate.run();
                            }
                        } catch (NoInternetConnection e) {
                            AlertFX.showNotification(
                                    owner, AlertFX_type.ERROR,
                                    "No internet connection",
                                    "Could not check for updates. Please connect to the Internet.",
                                    true);
                        }
                    },
                    ex -> {
                        ps.close();
                        AlertFX.showNotification(
                                owner, AlertFX_type.ERROR,
                                "Update check failed",
                                ex.getMessage(), true);
                        Actions.TaskLoader.LOGGER.log(
                                Level.SEVERE, "=== UPDATE CHECK FAILED ===", ex);
                    });
        }

        private void installUpdate(String version, Window owner) {
            ProgressSpinner psd = new ProgressSpinner(
                    owner,
                    ProgressSpinner.Style.SMALL,
                    Color.rgb(45, 215, 75),
                    ProgressSpinner.Position.BOTTOM,
                    Modality.NONE,
                    "Downloading update v" + version);

            Task<Void> task = new Task<>() {
                @Override protected Void call() throws Exception {
                    updateProgress(0.25, 1); Thread.sleep(300);
                    updateProgress(0.70, 1);
                    UpdateUtility.installUpdate(version, p -> updateProgress(p, 1));
                    updateProgress(0.80, 1); Thread.sleep(200);
                    updateProgress(1.0, 1);
                    return null;
                }
            };

            Actions.getInstance().taskLoader().loadRealTask(
                    psd, task,
                    unused -> {
                        psd.close();
                        ConfigManager.setOpenCourse("none");    // when installed new update reset the opened course
                        try { UpdateUtility.restartApp(); }
                        catch (Exception e) { throw new RuntimeException(e); }
                    },
                    ex -> {
                        psd.close();
                        AlertFX.showNotification(
                                owner, AlertFX_type.ERROR,
                                "Installation failed",
                                ex.getMessage(), true);
                        Actions.TaskLoader.LOGGER.log(
                                Level.SEVERE, "=== ERROR DURING INSTALLING UPDATE ===", ex);
                    });
        }
    }
}