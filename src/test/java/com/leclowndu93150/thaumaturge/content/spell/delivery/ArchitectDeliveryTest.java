package com.leclowndu93150.thaumaturge.content.spell.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.world.space.WorldFrame;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class ArchitectDeliveryTest {
    @Test
    void volumeScanIncludesEverySolidBlockInTheForwardShiftedBounds() {
        Level level = mock(Level.class);
        BlockPos origin = new BlockPos(8, 64, 8);
        BlockPos nearCorner = new BlockPos(6, 63, 7);
        BlockPos farCorner = new BlockPos(8, 65, 9);
        BlockPos outside = new BlockPos(9, 64, 8);
        Map<BlockPos, BlockState> blocks = Map.of(
                nearCorner, Blocks.STONE.defaultBlockState(),
                farCorner, Blocks.DIRT.defaultBlockState(),
                outside, Blocks.OBSIDIAN.defaultBlockState());
        when(level.getBlockState(any(BlockPos.class)))
                .thenAnswer(call -> blocks.getOrDefault(call.getArgument(0), Blocks.AIR.defaultBlockState()));

        Set<BlockPos> affected =
                Set.copyOf(ArchitectDelivery.volume(level, origin, Direction.EAST, 1, 1, 1, WorldFrame.INSTANCE));

        assertEquals(Set.of(nearCorner, farCorner), affected);
    }
}
