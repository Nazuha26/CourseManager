package com.coursemanagerfx.logic.commands.event_comms;

import com.coursemanagerfx.logic.Actions;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.commands.Command;

public class EditEventCommand implements Command {
    private final Group group;
    private final Student student;
    private final StudentEvent event;
    private final StudentEvent before;
    private final StudentEvent after;

    public EditEventCommand(Group group, Student student,
                            StudentEvent original,
                            StudentEvent newCopy) {
        this.group   = group;
        this.student = student;
        this.event   = original;
        this.before  = new StudentEvent(original);
        this.after   = newCopy;
    }

    @Override
    public void execute() {
        apply(after);
        Actions.getInstance().repaint().smartRefresh(group, student);
    }

    @Override
    public void undo() {
        apply(before);
        Actions.getInstance().repaint().smartRefresh(group, student);
    }

    private void apply(StudentEvent src) {
        event.setCategory(src.getCategory());
        event.setDescription(src.getDescription());
        event.setCrtDate(src.getCrtDate());
        event.setMark(src.getMark());
        event.setExpDate(src.getExpDate());
    }

    @Override
    public String getHistoryDescription() {
        String shortDesc = event.getDescription().length() > 10
                ? event.getDescription().substring(0, 10) + "..."
                : event.getDescription();
        return "Edited event \"" + event.getDescription() + "\"";
    }
}