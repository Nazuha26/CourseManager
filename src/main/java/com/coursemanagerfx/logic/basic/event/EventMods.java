package com.coursemanagerfx.logic.basic.event;

import com.coursemanagerfx.logic.basic.event.date.DateType;
import com.coursemanagerfx.logic.basic.event.date.ExpirationRule;

public enum EventMods {
    FIRST (
            new EventPreset("1 months; mark 5. Mod", 5, new ExpirationRule(1, DateType.MONTH))
    ),
    SECOND(
            new EventPreset("5 weeks; mark 2. Mod", 2, new ExpirationRule(5, DateType.WEEK))
    ),
    THIRD (
            new EventPreset("3 days; mark 4. Mod", 4, new ExpirationRule(3, DateType.DAY))
    ),
    FOURTH (
            new EventPreset("2 months; mark 1. Mod", 1, new ExpirationRule(2, DateType.MONTH))
    ),
    OTHER (
            new EventPreset("Custom mod", 5, new ExpirationRule(1, DateType.WEEK))
    );

    private final EventPreset preset;
    EventMods(EventPreset preset) { this.preset = preset; }
    public EventPreset getPreset() { return preset; }
}
