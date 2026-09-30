package com.leclowndu93150.thaumaturge.content.decor;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.MapColor;

public enum CandleHolderMaterial implements StringRepresentable {
    BRASS("brass", MapColor.GOLD, 1.0F, 0.1F, 0.05F),
    THAUMIUM("thaumium", MapColor.COLOR_PURPLE, 2.0F, 0.15F, 0.1F),
    VOID("void", MapColor.COLOR_BLACK, 3.0F, 0.2F, 0.15F);

    private final String name;
    private final MapColor mapColor;
    private final float strength;
    private final float stabilization;
    private final float researchSaveChance;

    CandleHolderMaterial(
            String name, MapColor mapColor, float strength, float stabilization, float researchSaveChance) {
        this.name = name;
        this.mapColor = mapColor;
        this.strength = strength;
        this.stabilization = stabilization;
        this.researchSaveChance = researchSaveChance;
    }

    public MapColor mapColor() {
        return mapColor;
    }

    public float strength() {
        return strength;
    }

    public float stabilization() {
        return stabilization;
    }

    public float researchSaveChance() {
        return researchSaveChance;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
