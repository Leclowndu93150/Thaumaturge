package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.registry.TCPlacementModifiers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class ConfigNodeSpawnFilter extends PlacementFilter {
    public static final MapCodec<ConfigNodeSpawnFilter> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Kind.CODEC.fieldOf("kind").forGetter(filter -> filter.kind))
                    .apply(instance, ConfigNodeSpawnFilter::new));

    public static final ConfigNodeSpawnFilter WILD = new ConfigNodeSpawnFilter(Kind.WILD);
    public static final ConfigNodeSpawnFilter MAGICAL = new ConfigNodeSpawnFilter(Kind.MAGICAL);
    public static final ConfigNodeSpawnFilter EERIE = new ConfigNodeSpawnFilter(Kind.EERIE);
    public static final ConfigNodeSpawnFilter NETHER = new ConfigNodeSpawnFilter(Kind.NETHER);

    private final Kind kind;

    private ConfigNodeSpawnFilter(Kind kind) {
        this.kind = kind;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        double chance =
                switch (kind) {
                    case WILD -> ThaumaturgeCommonConfig.WILD_NODE_CHANCE.get();
                    case MAGICAL -> ThaumaturgeCommonConfig.MAGICAL_NODE_CHANCE.get();
                    case EERIE -> ThaumaturgeCommonConfig.EERIE_NODE_CHANCE.get();
                    case NETHER -> ThaumaturgeCommonConfig.NETHER_NODE_CHANCE.get();
                };
        return chance > 0.0 && random.nextDouble() * 100.0 < chance;
    }

    @Override
    public PlacementModifierType<?> type() {
        return TCPlacementModifiers.NODE_SPAWN_CHANCE.get();
    }

    public enum Kind implements StringRepresentable {
        WILD("wild"),
        MAGICAL("magical"),
        EERIE("eerie"),
        NETHER("nether");

        private static final com.mojang.serialization.Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);
        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
