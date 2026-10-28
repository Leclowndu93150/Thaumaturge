package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class NodeDevicePortTest {
    @Test
    void jarredNodeAcceptsStabilizerLocksLikeOtherNodes() {
        BlockEntityJarNode node =
                new BlockEntityJarNode(BlockPos.ZERO, TTBlocks.JAR_NODE.get().defaultBlockState());
        assertTrue(node.allowLock());
    }

    @Test
    void catastrophicTransducerFailurePollutesAndRemovesTheNodeWithoutDestroyingNearbyBlocks() throws Exception {
        BlockPos nodePos = new BlockPos(4, 70, 9);
        ServerLevel level = mock(ServerLevel.class);
        BlockState hostState = TTBlocks.SILVERWOOD_NODE_LOG.get().defaultBlockState();
        BlockEntityNode node = new BlockEntityNode(nodePos, hostState);
        BlockState depletedState = ((NodeHostBlock) hostState.getBlock()).depletedState(hostState);
        when(level.getBlockEntity(nodePos)).thenReturn(node);
        when(level.getBlockState(nodePos)).thenReturn(hostState);
        RandomSource random = mock(RandomSource.class);
        when(level.getRandom()).thenReturn(random);
        when(level.hasChunkAt(any(BlockPos.class))).thenReturn(false);

        Method catastrophe = BlockEntityNodeTransducer.class.getDeclaredMethod(
                "catastrophicFailure", ServerLevel.class, BlockPos.class);
        catastrophe.setAccessible(true);
        try (MockedStatic<AuraHelper> aura = org.mockito.Mockito.mockStatic(AuraHelper.class)) {
            catastrophe.invoke(null, level, nodePos);
            aura.verify(() -> AuraHelper.polluteAura(level, nodePos, 32.0F, true));
        }

        verify(level).removeBlock(nodePos, false);
        verify(level, never()).setBlock(nodePos, depletedState, Block.UPDATE_ALL);
        verify(level)
                .explode(
                        null,
                        nodePos.getX() + 0.5,
                        nodePos.getY() + 0.5,
                        nodePos.getZ() + 0.5,
                        3.0F,
                        Level.ExplosionInteraction.NONE);
    }
}
