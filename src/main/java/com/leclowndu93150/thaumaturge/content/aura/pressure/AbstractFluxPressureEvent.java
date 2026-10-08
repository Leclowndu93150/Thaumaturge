package com.leclowndu93150.thaumaturge.content.aura.pressure;

public abstract class AbstractFluxPressureEvent implements FluxPressureEvent {
    private final String name;
    private final int weight;
    private final float cost;
    private final boolean allowedNearTaint;

    protected AbstractFluxPressureEvent(String name, int weight, float cost, boolean allowedNearTaint) {
        this.name = name;
        this.weight = weight;
        this.cost = cost;
        this.allowedNearTaint = allowedNearTaint;
    }

    @Override
    public final String name() {
        return name;
    }

    @Override
    public final int weight() {
        return weight;
    }

    @Override
    public final float cost() {
        return cost;
    }

    @Override
    public final boolean allowedNearTaint() {
        return allowedNearTaint;
    }
}
