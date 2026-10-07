package com.leclowndu93150.thaumaturge.content.eldritch;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public final class OuterLands {
    public static final ResourceKey<Level> DIMENSION =
            ResourceKey.create(Registries.DIMENSION, TTIds.rl("outer_lands"));
    public static final ResourceLocation EFFECTS = TTIds.rl("outer_lands");
    public static final ResourceKey<DimensionType> DIMENSION_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, TTIds.rl("outer_lands"));
    public static final ResourceKey<LevelStem> STEM =
            ResourceKey.create(Registries.LEVEL_STEM, TTIds.rl("outer_lands"));

    public static final int MAZE_Y = 50;

    private OuterLands() {}
}
