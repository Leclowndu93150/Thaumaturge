package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class AdvancedFurnaceShapes {
    private static final int RADIUS = 1;
    private static final Map<BlockPos, VoxelShape> CELLS = cells();

    private AdvancedFurnaceShapes() {}

    public static @Nullable VoxelShape find(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int y = -1; y <= 0; y++) {
                for (int z = -RADIUS; z <= RADIUS; z++) {
                    cursor.setWithOffset(pos, x, y, z);
                    if (level.getBlockState(cursor).is(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE)) {
                        return CELLS.get(new BlockPos(-x, -y, -z));
                    }
                }
            }
        }
        return null;
    }

    private static Map<BlockPos, VoxelShape> cells() {
        Map<BlockPos, VoxelShape> cells = new HashMap<>();
        cells.put(
                new BlockPos(-1, 0, -1),
                Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0), Block.box(1.0, 3.0, 1.0, 16.0, 16.0, 16.0)));
        cells.put(
                new BlockPos(-1, 0, 0),
                Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0), Block.box(1.0, 3.0, 0.0, 16.0, 16.0, 16.0)));
        cells.put(
                new BlockPos(-1, 0, 1),
                Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0), Block.box(1.0, 3.0, 0.0, 16.0, 16.0, 15.0)));
        cells.put(
                new BlockPos(-1, 1, -1),
                Shapes.or(
                        Block.box(1.0, 1.0, 1.0, 16.0, 2.0, 15.0),
                        Block.box(1.0, 12.0, 1.0, 15.0, 13.0, 15.0),
                        Block.box(2.0, 0.0, 2.0, 14.0, 1.0, 13.0),
                        Block.box(2.0, 2.0, 2.0, 14.0, 12.0, 14.0),
                        Block.box(2.0, 13.0, 2.0, 14.0, 14.0, 14.0),
                        Block.box(3.0, 0.0, 13.0, 16.0, 1.0, 14.0),
                        Block.box(5.0, 14.0, 5.0, 11.0, 16.0, 11.0),
                        Block.box(12.0, 0.0, 14.0, 16.0, 6.0, 16.0),
                        Block.box(14.0, 0.0, 12.0, 16.0, 6.0, 14.0),
                        Block.box(15.0, 6.0, 15.0, 16.0, 12.0, 16.0)));
        cells.put(
                new BlockPos(-1, 1, 0),
                Shapes.or(Block.box(12.0, 0.0, 0.0, 16.0, 6.0, 16.0), Block.box(14.0, 6.0, 0.0, 16.0, 12.0, 16.0)));
        cells.put(
                new BlockPos(-1, 1, 1),
                Shapes.or(
                        Block.box(1.0, 1.0, 1.0, 16.0, 2.0, 15.0),
                        Block.box(1.0, 12.0, 1.0, 15.0, 13.0, 15.0),
                        Block.box(2.0, 0.0, 3.0, 14.0, 1.0, 14.0),
                        Block.box(2.0, 2.0, 2.0, 14.0, 12.0, 14.0),
                        Block.box(2.0, 13.0, 2.0, 14.0, 14.0, 14.0),
                        Block.box(3.0, 0.0, 2.0, 16.0, 1.0, 3.0),
                        Block.box(5.0, 14.0, 5.0, 11.0, 16.0, 11.0),
                        Block.box(12.0, 0.0, 0.0, 16.0, 6.0, 2.0),
                        Block.box(14.0, 0.0, 2.0, 16.0, 6.0, 4.0),
                        Block.box(15.0, 6.0, 0.0, 16.0, 12.0, 1.0)));
        cells.put(
                new BlockPos(0, 0, -1),
                Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0), Block.box(0.0, 3.0, 1.0, 16.0, 16.0, 16.0)));
        cells.put(
                new BlockPos(0, 0, 1),
                Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0), Block.box(0.0, 3.0, 0.0, 16.0, 16.0, 15.0)));
        cells.put(
                new BlockPos(0, 1, -1),
                Shapes.or(Block.box(0.0, 0.0, 12.0, 16.0, 6.0, 16.0), Block.box(0.0, 6.0, 14.0, 16.0, 12.0, 16.0)));
        cells.put(
                new BlockPos(0, 1, 0),
                Shapes.or(
                        Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
                        Block.box(0.0, 12.0, 2.0, 16.0, 16.0, 14.0),
                        Block.box(1.0, 12.0, 1.0, 15.0, 16.0, 2.0),
                        Block.box(1.0, 12.0, 14.0, 15.0, 16.0, 15.0),
                        Block.box(2.0, 12.0, 0.0, 14.0, 16.0, 1.0),
                        Block.box(2.0, 12.0, 15.0, 14.0, 16.0, 16.0)));
        cells.put(
                new BlockPos(0, 1, 1),
                Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 4.0), Block.box(0.0, 6.0, 0.0, 16.0, 12.0, 2.0)));
        cells.put(
                new BlockPos(1, 0, -1),
                Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0), Block.box(0.0, 3.0, 1.0, 15.0, 16.0, 16.0)));
        cells.put(
                new BlockPos(1, 0, 0),
                Shapes.or(Block.box(0.0, 0.0, 0.0, 15.0, 16.0, 16.0), Block.box(15.0, 0.0, 0.0, 16.0, 3.0, 16.0)));
        cells.put(
                new BlockPos(1, 0, 1),
                Shapes.or(Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0), Block.box(0.0, 3.0, 0.0, 15.0, 16.0, 15.0)));
        cells.put(
                new BlockPos(1, 1, -1),
                Shapes.or(
                        Block.box(0.0, 0.0, 12.0, 2.0, 6.0, 16.0),
                        Block.box(0.0, 6.0, 15.0, 1.0, 12.0, 16.0),
                        Block.box(1.0, 1.0, 1.0, 15.0, 2.0, 15.0),
                        Block.box(1.0, 12.0, 1.0, 15.0, 13.0, 15.0),
                        Block.box(2.0, 0.0, 2.0, 14.0, 1.0, 14.0),
                        Block.box(2.0, 0.0, 14.0, 4.0, 6.0, 16.0),
                        Block.box(2.0, 2.0, 2.0, 14.0, 12.0, 13.0),
                        Block.box(2.0, 13.0, 2.0, 14.0, 14.0, 14.0),
                        Block.box(3.0, 2.0, 13.0, 13.0, 12.0, 14.0),
                        Block.box(5.0, 14.0, 5.0, 11.0, 16.0, 11.0)));
        cells.put(
                new BlockPos(1, 1, 0),
                Shapes.or(Block.box(0.0, 0.0, 0.0, 2.0, 12.0, 16.0), Block.box(2.0, 0.0, 0.0, 4.0, 6.0, 16.0)));
        cells.put(
                new BlockPos(1, 1, 1),
                Shapes.or(
                        Block.box(0.0, 0.0, 0.0, 2.0, 6.0, 4.0),
                        Block.box(0.0, 6.0, 0.0, 1.0, 12.0, 1.0),
                        Block.box(1.0, 1.0, 1.0, 15.0, 2.0, 15.0),
                        Block.box(1.0, 12.0, 1.0, 15.0, 13.0, 15.0),
                        Block.box(2.0, 0.0, 0.0, 4.0, 6.0, 2.0),
                        Block.box(2.0, 0.0, 2.0, 14.0, 1.0, 14.0),
                        Block.box(2.0, 2.0, 2.0, 14.0, 12.0, 13.0),
                        Block.box(2.0, 13.0, 2.0, 14.0, 14.0, 14.0),
                        Block.box(3.0, 2.0, 13.0, 13.0, 12.0, 14.0),
                        Block.box(5.0, 14.0, 5.0, 11.0, 16.0, 11.0)));
        return Map.copyOf(cells);
    }
}
