package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.registry.TTPlacementModifiers;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class SurfaceBiomeSearch extends PlacementModifier {
    public static final SurfaceBiomeSearch INSTANCE = new SurfaceBiomeSearch();
    public static final MapCodec<SurfaceBiomeSearch> CODEC = MapCodec.unit(INSTANCE);
    private static final int CELL_CENTER = QuartPos.SIZE / 2;

    private SurfaceBiomeSearch() {}

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos origin) {
        Optional<PlacedFeature> feature = context.topFeature();
        if (feature.isEmpty()) {
            return Stream.of(origin);
        }
        int minX = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getX()));
        int minZ = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getZ()));
        for (int dx = CELL_CENTER; dx < SectionPos.SECTION_SIZE; dx += QuartPos.SIZE) {
            for (int dz = CELL_CENTER; dz < SectionPos.SECTION_SIZE; dz += QuartPos.SIZE) {
                int x = minX + dx;
                int z = minZ + dz;
                BlockPos surface = new BlockPos(x, context.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), z);
                if (context.generator().getBiomeGenerationSettings(context.getLevel().getBiome(surface)).hasFeature(feature.get())) {
                    return Stream.of(surface);
                }
            }
        }
        return Stream.empty();
    }

    @Override
    public PlacementModifierType<?> type() {
        return TTPlacementModifiers.SURFACE_BIOME_SEARCH.get();
    }
}
