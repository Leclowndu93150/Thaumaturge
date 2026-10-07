package com.leclowndu93150.thaumaturge.data.worldgen.dimension;

import com.leclowndu93150.thaumaturge.content.eldritch.ChunkGeneratorOuter;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import java.util.OptionalLong;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public final class OuterLandsBootstrap {
    private static final float AMBIENT_LIGHT = 0.05F;
    private static final int HEIGHT = 128;
    private static final int MONSTER_SPAWN_LIGHT = 7;
    private static final int MONSTER_SPAWN_BLOCK_LIGHT_LIMIT = 15;

    private OuterLandsBootstrap() {}

    public static void bootstrapTypes(BootstrapContext<DimensionType> context) {
        context.register(
                OuterLands.DIMENSION_TYPE,
                new DimensionType(
                        OptionalLong.empty(),
                        false,
                        false,
                        false,
                        false,
                        1.0,
                        false,
                        false,
                        0,
                        HEIGHT,
                        HEIGHT,
                        BlockTags.INFINIBURN_OVERWORLD,
                        OuterLands.EFFECTS,
                        AMBIENT_LIGHT,
                        new DimensionType.MonsterSettings(
                                false, false, ConstantInt.of(MONSTER_SPAWN_LIGHT), MONSTER_SPAWN_BLOCK_LIGHT_LIMIT)));
    }

    public static void bootstrapStems(BootstrapContext<LevelStem> context) {
        context.register(
                OuterLands.STEM,
                new LevelStem(
                        context.lookup(Registries.DIMENSION_TYPE).getOrThrow(OuterLands.DIMENSION_TYPE),
                        new ChunkGeneratorOuter(context.lookup(Registries.BIOME).getOrThrow(TTBiomes.ELDRITCH))));
    }
}
