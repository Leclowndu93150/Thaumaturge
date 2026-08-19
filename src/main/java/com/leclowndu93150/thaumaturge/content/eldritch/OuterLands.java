package com.leclowndu93150.thaumaturge.content.eldritch;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public final class OuterLands {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, TCIds.rl("outer_lands"));
    public static final ResourceKey<DimensionType> DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, TCIds.rl("outer_lands"));
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, TCIds.rl("outer_lands"));

    public static final int MAZE_Y = 50;

    private OuterLands() {}
}
