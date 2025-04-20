package com.coursemanagerfx.logic;

public class EventPreset {
    private String presetName;
    private int mark;
    private String expiredTime;

    public EventPreset(String presetName, int mark, String expiredTime) {
        this.presetName = presetName;
        this.mark = mark;
        this.expiredTime = expiredTime;
    }

    public String getPresetName() {
        return presetName;
    }

    public void setPresetName(String presetName) {
        this.presetName = presetName;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public String getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(String expiredTime) {
        this.expiredTime = expiredTime;
    }
}
