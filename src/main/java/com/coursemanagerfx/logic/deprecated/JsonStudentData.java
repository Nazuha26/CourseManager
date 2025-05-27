package com.coursemanagerfx.logic.deprecated;

public class JsonStudentData {
    private String name;                // Имя студента
    private final int ID;

    public JsonStudentData(String name, int ID) {
        this.name = name;
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
