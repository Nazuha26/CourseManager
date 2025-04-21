package com.coursemanagerfx.logic;

public enum EventMods {
    FIRST(new EventPreset("Some first mode", 5, "1 %m")),
    SECOND(new EventPreset("Some second mode", 2, "5 %w")),
    THIRD(new EventPreset("Some third mode", 4, "3 %d")),

    ///... some others mods
    OTHER(new EventPreset("Other mode", 5, "1 %w"));   /// the last one


    private final EventPreset preset;

    EventMods(EventPreset preset) {
        this.preset = preset;
    }

    public EventPreset getPreset() {
        return preset;
    }
}