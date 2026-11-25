package com.leclowndu93150.thaumaturge.content.warding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class WardBlockEntityTest {
    @Test
    void blockEntitiesAreAllowedByDefaultAndCanBeDisabled() {
        ModConfigSpec.BooleanValue option = ThaumaturgeCommonConfig.ALLOW_WARDING_BLOCK_ENTITIES;
        boolean original = option.get();
        BlockPos chestPos = BlockPos.ZERO;
        BlockPos stabilizerPos = BlockPos.ZERO.above();
        BlockPos stonePos = BlockPos.ZERO.below();
        BlockPos nodePos = BlockPos.ZERO.east();
        BlockPos jarNodePos = BlockPos.ZERO.west();
        BlockPos leavesPos = BlockPos.ZERO.north();
        BlockPos gravelPos = BlockPos.ZERO.south();
        BlockState chestState = Blocks.CHEST.defaultBlockState();
        BlockState stabilizerState = TTBlocks.NODE_STABILIZER.get().defaultBlockState();
        BlockState stoneState = Blocks.STONE.defaultBlockState();
        BlockState nodeState = TTBlocks.NODE.get().defaultBlockState();
        BlockState jarNodeState = TTBlocks.JAR_NODE.get().defaultBlockState();
        BlockState leavesState = Blocks.OAK_LEAVES.defaultBlockState();
        BlockState gravelState = Blocks.GRAVEL.defaultBlockState();
        BlockGetter level = mock(BlockGetter.class);
        when(level.getBlockState(chestPos)).thenReturn(chestState);
        when(level.getBlockState(stabilizerPos)).thenReturn(stabilizerState);
        when(level.getBlockState(stonePos)).thenReturn(stoneState);
        when(level.getBlockState(nodePos)).thenReturn(nodeState);
        when(level.getBlockState(jarNodePos)).thenReturn(jarNodeState);
        when(level.getBlockState(leavesPos)).thenReturn(leavesState);
        when(level.getBlockState(gravelPos)).thenReturn(gravelState);

        try {
            assertTrue(option.getDefault());
            option.set(true);
            assertTrue(chestState.hasBlockEntity());
            assertTrue(stabilizerState.hasBlockEntity());
            assertTrue(WardHandler.canWard(level, chestPos));
            assertTrue(WardHandler.canWard(level, stabilizerPos));
            assertFalse(WardHandler.canWard(level, nodePos));
            assertFalse(WardHandler.canWard(level, jarNodePos));
            assertFalse(WardHandler.canWard(level, leavesPos));
            assertFalse(WardHandler.canWard(level, gravelPos));

            option.set(false);
            assertFalse(WardHandler.canWard(level, chestPos));
            assertFalse(WardHandler.canWard(level, stabilizerPos));
            assertTrue(WardHandler.canWard(level, stonePos));
        } finally {
            option.set(original);
        }
    }

    @Test
    void wardingAndUnwardingKeepTheContainerBlockEntityAndItsContents() {
        ModConfigSpec.BooleanValue option = ThaumaturgeCommonConfig.ALLOW_WARDING_BLOCK_ENTITIES;
        boolean original = option.get();
        option.set(true);

        BlockPos pos = new BlockPos(3, 64, 5);
        BlockState chestState = Blocks.CHEST.defaultBlockState();
        ChestBlockEntity chest = new ChestBlockEntity(pos, chestState);
        ItemStack stored = new ItemStack(Items.DIAMOND, 7);
        chest.setItem(0, stored.copy());

        ServerLevel level = mock(ServerLevel.class);
        LevelChunk chunk = mock(LevelChunk.class);
        WardChunkData wards = new WardChunkData();
        when(level.getBlockState(pos)).thenReturn(chestState);
        when(level.getBlockEntity(pos)).thenReturn(chest);
        when(chunk.hasData(TTAttachments.WARDS.get())).thenReturn(true);
        when(chunk.getData(TTAttachments.WARDS.get())).thenReturn(wards);

        UUID owner = UUID.randomUUID();
        try (MockedStatic<LevelSpaces> spaces = mockStatic(LevelSpaces.class);
                MockedStatic<PacketDistributor> packets = mockStatic(PacketDistributor.class)) {
            spaces.when(() -> LevelSpaces.loadedChunk(level, pos)).thenReturn(chunk);

            assertTrue(WardHandler.ward(level, pos, owner));
            assertTrue(WardHandler.isWarded(level, pos));
            assertTrue(WardHandler.unward(level, pos, owner));

            assertFalse(WardHandler.isWarded(level, pos));
            assertSame(chestState, level.getBlockState(pos));
            assertSame(chest, level.getBlockEntity(pos));
            assertEquals(Items.DIAMOND, chest.getItem(0).getItem());
            assertEquals(stored.getCount(), chest.getItem(0).getCount());
        } finally {
            option.set(original);
        }
    }

    @Test
    void placingIntoAFormerlyWardedPositionClearsItsStaleWard() {
        ModConfigSpec.BooleanValue option = ThaumaturgeCommonConfig.ALLOW_WARDING_BLOCK_ENTITIES;
        boolean original = option.get();
        option.set(true);

        BlockPos pos = new BlockPos(3, 64, 5);
        ServerLevel level = mock(ServerLevel.class);
        LevelChunk chunk = mock(LevelChunk.class);
        WardChunkData wards = new WardChunkData();
        BlockEvent.EntityPlaceEvent event = mock(BlockEvent.EntityPlaceEvent.class);
        when(level.getBlockState(pos)).thenReturn(Blocks.CHEST.defaultBlockState());
        when(chunk.hasData(TTAttachments.WARDS.get())).thenReturn(true);
        when(chunk.getData(TTAttachments.WARDS.get())).thenReturn(wards);
        when(event.getLevel()).thenReturn(level);
        when(event.getPos()).thenReturn(pos);
        when(event.isCanceled()).thenReturn(true, false);

        UUID owner = UUID.randomUUID();
        try (MockedStatic<LevelSpaces> spaces = mockStatic(LevelSpaces.class);
                MockedStatic<PacketDistributor> packets = mockStatic(PacketDistributor.class)) {
            spaces.when(() -> LevelSpaces.loadedChunk(level, pos)).thenReturn(chunk);

            assertTrue(WardHandler.ward(level, pos, owner));
            WardEvents.onBlockPlaced(event);
            assertTrue(WardHandler.isWarded(level, pos));

            WardEvents.onBlockPlaced(event);
            assertFalse(WardHandler.isWarded(level, pos));
        } finally {
            option.set(original);
        }
    }
}
