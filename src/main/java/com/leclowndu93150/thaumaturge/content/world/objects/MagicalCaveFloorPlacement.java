package com.leclowndu93150.thaumaturge.content.world.objects;

import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTPlacementModifiers;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public final class MagicalCaveFloorPlacement extends PlacementModifier {
    public static final MagicalCaveFloorPlacement INSTANCE = new MagicalCaveFloorPlacement();
    public static final MapCodec<MagicalCaveFloorPlacement> CODEC = MapCodec.unit(INSTANCE);

    private MagicalCaveFloorPlacement() {}

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos origin) {
        ChunkAccess chunk = context.getLevel().getChunk(origin);
        int top = context.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        int bottom = Math.max(chunk.getMinY(), top - chunk.getHeight());
        if (top <= bottom)
            return Stream.empty();

        int minGroundY = Math.max(chunk.getMinY(), bottom - 1);
        int localX = origin.getX() & 15;
        int localZ = origin.getZ() & 15;
        boolean airAbove = context.getBlockState(origin.atY(top - 1)).isAir();
        List<BlockPos> floors = new ArrayList<>();

        for (int y = top - 2; y >= minGroundY;) {
            LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(y));
            int sectionBottom = Math.max(minGroundY, y & ~15);
            if (section.hasOnlyAir()) {
                airAbove = true;
            } else if (!section.maybeHas(BlockState::isAir)) {
                if (airAbove && section.getBlockState(localX, y & 15, localZ).is(TTBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE)) {
                    floors.add(new BlockPos(origin.getX(), y + 1, origin.getZ()));
                }
                airAbove = false;
            } else {
                for (; y >= sectionBottom; y--) {
                    BlockState state = section.getBlockState(localX, y & 15, localZ);
                    if (airAbove && state.is(TTBlockTags.MAGICAL_CAVE_GROUND_REPLACEABLE)) {
                        floors.add(new BlockPos(origin.getX(), y + 1, origin.getZ()));
                    }
                    airAbove = state.isAir();
                }
            }
            y = sectionBottom - 1;
        }

        return floors.stream();
    }

    @Override
    public PlacementModifierType<?> type() {
        return TTPlacementModifiers.MAGICAL_CAVE_FLOOR.get();
    }
}
