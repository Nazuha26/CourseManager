package com.coursemanagerfx.logic.basic.event;

public record EventCategory(CategoryTag tag, String name) {
    @Override
    public String toString() {
        return "EventCategory [\n" +
                "  tag  = " + tag + ",\n" +
                "  name = \"" + name + "\"\n" +
                "]";
    }
}