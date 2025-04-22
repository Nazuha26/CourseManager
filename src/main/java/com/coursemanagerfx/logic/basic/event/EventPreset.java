package com.coursemanagerfx.logic.basic.event;

import com.coursemanagerfx.logic.basic.event.date.ExpirationRule;

public class EventPreset {
    private final String name;
    private final int mark;
    private final ExpirationRule rule;

    public EventPreset(String name, int mark, ExpirationRule rule) {
        this.name = name;
        this.mark = mark;
        this.rule = rule;
    }

    public String getName() {
        return name;
    }
    public int getMark() { return mark; }
    public ExpirationRule getRule() { return rule; }
}