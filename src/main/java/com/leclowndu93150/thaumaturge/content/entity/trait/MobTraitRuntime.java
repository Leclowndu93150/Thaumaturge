package com.leclowndu93150.thaumaturge.content.entity.trait;

import java.util.ArrayList;
import java.util.List;

public final class MobTraitRuntime {
    private final List<TraitGoal> injected = new ArrayList<>();
    private final List<TraitGoal> stashed = new ArrayList<>();
    private boolean nativeAiSuppressed;

    public List<TraitGoal> injected() {
        return injected;
    }

    public List<TraitGoal> stashed() {
        return stashed;
    }

    public boolean nativeAiSuppressed() {
        return nativeAiSuppressed;
    }

    public void setNativeAiSuppressed(boolean suppressed) {
        this.nativeAiSuppressed = suppressed;
    }
}
