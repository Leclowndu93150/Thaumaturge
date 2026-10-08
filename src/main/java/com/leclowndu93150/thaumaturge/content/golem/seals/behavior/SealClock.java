package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

final class SealClock {
    private int ticks;

    SealClock(int stagger) {
        this.ticks = System.identityHashCode(this) % stagger;
    }

    int now() {
        return ticks;
    }

    int advance() {
        return ticks++;
    }

    boolean at(int period) {
        return ticks % period == 0;
    }
}
