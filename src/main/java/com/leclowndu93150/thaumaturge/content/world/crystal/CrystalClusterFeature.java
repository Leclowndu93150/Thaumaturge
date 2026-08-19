package com.leclowndu93150.thaumaturge.content.world.crystal;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAspects;
import com.leclowndu93150.thaumaturge.registry.TCDataMaps;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

public final class CrystalClusterFeature extends Feature<CrystalClusterConfig> {
    private static final int MIN_SIZE = 1;
    private static final int MAX_EXTRA_SIZE = 3;
    private static final int PLACE_FLAGS = 19;

    public CrystalClusterFeature(Codec<CrystalClusterConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<CrystalClusterConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        CrystalClusterConfig config = context.config();
        BlockPos origin = context.origin();
        int placedTotal = 0;
        boolean any = false;
        for (int attempt = 0; attempt < config.attempts(); attempt++) {
            int x = origin.getX() + 8 + Mth.nextInt(random, -6, 6);
            int z = origin.getZ() + 8 + Mth.nextInt(random, -6, 6);
            int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            int minY = level.getMinY();
            int y = minY + random.nextInt(Math.max(5, surface - 5 - minY));
            BlockPos center = new BlockPos(x, y, z);

            CrystalClusterConfig.Entry entry = config.crystals().get(random.nextInt(config.crystals().size()));
            if (random.nextInt(config.biomeAspectChance()) == 0) {
                CrystalClusterConfig.Entry biomeEntry = biomeEntry(level, center, random, config.crystals());
                if (biomeEntry != null) {
                    entry = biomeEntry;
                }
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (random.nextInt(3) != 0) {
                            BlockPos pos = center.offset(dx, dy, dz);
                            BlockState state = level.getBlockState(pos);
                            if (!state.liquid() && (state.isAir() || state.canBeReplaced()) && isTouchingRock(level, pos)) {
                                int size = MIN_SIZE + random.nextInt(MAX_EXTRA_SIZE);
                                BlockState crystal = entry.block().defaultBlockState();
                                if (crystal.hasProperty(BlockCrystal.SIZE)) {
                                    crystal = crystal.setValue(BlockCrystal.SIZE, size);
                                }
                                level.setBlock(pos, crystal, PLACE_FLAGS);
                                placedTotal += size;
                                any = true;
                            }
                        }
                    }
                }
            }
            if (placedTotal > config.maxTotal()) {
                break;
            }
        }
        return any;
    }

    private static CrystalClusterConfig.@Nullable Entry biomeEntry(WorldGenLevel level, BlockPos pos, RandomSource random, List<CrystalClusterConfig.Entry> entries) {
        Holder<Biome> biome = level.getBiome(pos);
        BiomeAspects aspects = biome.getData(TCDataMaps.BIOME_ASPECTS);
        if (aspects == null || aspects.aspects().isEmpty()) {
            return null;
        }
        ResourceKey<IAspect> aspect = aspects.aspects().get(random.nextInt(aspects.aspects().size()));
        for (CrystalClusterConfig.Entry entry : entries) {
            if (entry.aspect().equals(aspect)) {
                return entry;
            }
        }
        return null;
    }

    private static boolean isTouchingRock(WorldGenLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.is(BlockTags.BASE_STONE_OVERWORLD) || neighbor.is(Tags.Blocks.STONES) || neighbor.is(Tags.Blocks.ORES) || neighbor.is(BlockTags.BASE_STONE_NETHER)) {
                return true;
            }
        }
        return false;
    }
}
