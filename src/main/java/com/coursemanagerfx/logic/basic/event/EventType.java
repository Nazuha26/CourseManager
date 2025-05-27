package com.coursemanagerfx.logic.basic.event;

public record EventType(TypeTag tag, String name) {
    @Override
    public String toString() {
        return "EventType [\n" +
                "  tag  = " + tag + ",\n" +
                "  name = \"" + name + "\"\n" +
                "]";
    }
}