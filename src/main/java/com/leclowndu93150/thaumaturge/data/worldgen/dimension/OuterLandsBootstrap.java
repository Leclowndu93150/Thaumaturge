package com.leclowndu93150.thaumaturge.data.worldgen.dimension;

import com.leclowndu93150.thaumaturge.content.eldritch.ChunkGeneratorOuter;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TCBiomes;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public final class OuterLandsBootstrap {
    private static final int FOG_COLOR = 0xFF181305;
    private static final int SKY_COLOR = 0xFF000000;
    private static final float FOG_START = 10.0F;
    private static final float FOG_END = 96.0F;
    private static final float AMBIENT_LIGHT = 0.05F;
    private static final int HEIGHT = 128;

    private OuterLandsBootstrap() {}

    public static void bootstrapTypes(BootstrapContext<DimensionType> context) {
        EnvironmentAttributeMap attributes = EnvironmentAttributeMap.builder().set(EnvironmentAttributes.FOG_COLOR, FOG_COLOR).set(EnvironmentAttributes.SKY_COLOR, SKY_COLOR)
                .set(EnvironmentAttributes.FOG_START_DISTANCE, FOG_START).set(EnvironmentAttributes.FOG_END_DISTANCE, FOG_END).set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.0F)
                .set(EnvironmentAttributes.BED_RULE, BedRule.EXPLODES).set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false).set(EnvironmentAttributes.CAN_START_RAID, false).build();
        context.register(OuterLands.DIMENSION_TYPE, new DimensionType(true, false, false, false, 1.0, 0, HEIGHT, HEIGHT, BlockTags.INFINIBURN_OVERWORLD, AMBIENT_LIGHT,
                new DimensionType.MonsterSettings(ConstantInt.of(7), 15), DimensionType.Skybox.NONE, CardinalLighting.Type.DEFAULT, attributes, HolderSet.direct(), Optional.empty()));
    }

    public static void bootstrapStems(BootstrapContext<LevelStem> context) {
        context.register(OuterLands.STEM, new LevelStem(context.lookup(Registries.DIMENSION_TYPE).getOrThrow(OuterLands.DIMENSION_TYPE),
                new ChunkGeneratorOuter(context.lookup(Registries.BIOME).getOrThrow(TCBiomes.ELDRITCH))));
    }
}
