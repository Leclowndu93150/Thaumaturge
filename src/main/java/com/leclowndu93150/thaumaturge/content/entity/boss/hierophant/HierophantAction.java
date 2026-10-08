package com.leclowndu93150.thaumaturge.content.entity.boss.hierophant;

public enum HierophantAction {
    IDLE(0, -1),
    SUMMON(60, -1),
    CAST(48, 26),
    SWIPE_RIGHT(24, 13),
    SWIPE_LEFT(24, 13),
    NOVA(56, 33),
    DEATH(72, -1),
    THROW(72, 34);

    private final int duration;
    private final int release;

    HierophantAction(int duration, int release) {
        this.duration = duration;
        this.release = release;
    }

    public int duration() {
        return duration;
    }

    public int release() {
        return release;
    }

    public static HierophantAction byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : IDLE;
    }
}
