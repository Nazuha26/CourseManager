/*
========================================
THIS FILE CREATED FOR "CourseManagerFX"
            Author: Nazuha26
========================================
*/

package com.coursemanagerfx.logic.basic.event.category;

import javafx.scene.paint.Color;

public record EventCategory(CategoryTag tag, String name, Color color) {
    @Override
    public String toString() {
        return "EventCategory [\n" +
                "  tag  = " + tag + ",\n" +
                "  name = \"" + name + "\"\n" +
                "  color = " + color.toString() + "\n" +
                "]";
    }
}
