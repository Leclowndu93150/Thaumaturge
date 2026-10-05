package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class HungryNodeWardTest {
    @Test
    void wardedRayHitBlocksConsumptionWhileUnwardedBlockCanBeEaten() throws Exception {
        BlockEntityNode node =
                new BlockEntityNode(BlockPos.ZERO, TCBlocks.NODE.get().defaultBlockState());
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        BlockPos target = new BlockPos(1, 0, 0);
        when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
        when(level.getHeight(eq(Heightmap.Types.MOTION_BLOCKING), anyInt(), anyInt()))
                .thenReturn(10);
        when(level.clip(any(ClipContext.class)))
                .thenReturn(new BlockHitResult(Vec3.atCenterOf(target), Direction.WEST, target, false));
        when(level.getBlockState(target)).thenReturn(Blocks.STONE.defaultBlockState());
        Method hungryTarget = BlockEntityNode.class.getDeclaredMethod(
                "hungryTarget", Level.class, BlockPos.class, RandomSource.class);
        hungryTarget.setAccessible(true);
        Method eatBlock = BlockEntityNode.class.getDeclaredMethod(
                "eatBlock", ServerLevel.class, BlockPos.class, RandomSource.class);
        eatBlock.setAccessible(true);
        try (var wards = mockStatic(WardHandler.class)) {
            wards.when(() -> WardHandler.isWarded(level, target)).thenReturn(true);
            assertNull(hungryTarget.invoke(node, level, BlockPos.ZERO, random));
            eatBlock.invoke(node, level, BlockPos.ZERO, random);
            verify(level, never()).destroyBlock(any(), anyBoolean());
            wards.when(() -> WardHandler.isWarded(level, target)).thenReturn(false);
            assertEquals(target, hungryTarget.invoke(node, level, BlockPos.ZERO, random));
            eatBlock.invoke(node, level, BlockPos.ZERO, random);
            verify(level).destroyBlock(target, true);
        }
    }
}
