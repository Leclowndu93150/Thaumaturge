package com.leclowndu93150.thaumaturge.content.device.levitator;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum LevitatorReach implements StringRepresentable {
    SHORT("short", 4),
    MEDIUM("medium", 8),
    LONG("long", 16),
    FAR("far", 32);

    public static final Codec<LevitatorReach> CODEC = StringRepresentable.fromEnum(LevitatorReach::values);

    private static final int VIS_COST_PER_BLOCK = 2;

    private final String name;
    private final int blocks;

    LevitatorReach(String name, int blocks) {
        this.name = name;
        this.blocks = blocks;
    }

    public int blocks() {
        return blocks;
    }

    public int visCost() {
        return blocks * VIS_COST_PER_BLOCK;
    }

    public LevitatorReach next() {
        LevitatorReach[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
