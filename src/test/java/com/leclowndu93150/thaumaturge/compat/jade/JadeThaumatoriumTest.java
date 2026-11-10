package com.leclowndu93150.thaumaturge.compat.jade;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.content.crucible.BlockEntityCrucible;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatoriumTop;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.junit.jupiter.api.Test;
import snownee.jade.api.BlockAccessor;

class JadeThaumatoriumTest {
    private static final BlockPos CONTROLLER = new BlockPos(15, 64, 15);

    @Test
    void controllerTopAndCrucibleResolveToTheSameMachine() {
        Level level = mock(Level.class);
        BlockEntityThaumatorium machine = mock(BlockEntityThaumatorium.class);
        BlockEntityThaumatoriumTop top = mock(BlockEntityThaumatoriumTop.class);
        BlockEntityCrucible crucible = mock(BlockEntityCrucible.class);
        when(level.hasChunk(0, 0)).thenReturn(true);
        when(level.getBlockEntity(CONTROLLER)).thenReturn(machine);

        assertSame(machine, JadeBlockProviderHolder.resolve(accessor(level, CONTROLLER, machine)));
        assertSame(machine, JadeBlockProviderHolder.resolve(accessor(level, CONTROLLER.above(), top)));
        assertSame(machine, JadeBlockProviderHolder.resolve(accessor(level, CONTROLLER.below(), crucible)));
    }

    @Test
    void standaloneCrucibleAndOrphanTopKeepTheirOwnIdentity() {
        Level level = mock(Level.class);
        BlockEntityCrucible crucible = mock(BlockEntityCrucible.class);
        BlockEntityThaumatoriumTop top = mock(BlockEntityThaumatoriumTop.class);
        when(level.hasChunk(0, 0)).thenReturn(true);

        assertSame(crucible, JadeBlockProviderHolder.resolve(accessor(level, CONTROLLER.below(), crucible)));
        assertSame(top, JadeBlockProviderHolder.resolve(accessor(level, CONTROLLER.above(), top)));
        verify(level, never()).getBlockEntity(CONTROLLER.east());
        verify(level, never()).getBlockEntity(CONTROLLER.west());
    }

    @Test
    void unloadedControllerIsNotQueried() {
        Level level = mock(Level.class);
        BlockEntityThaumatoriumTop top = mock(BlockEntityThaumatoriumTop.class);

        assertSame(top, JadeBlockProviderHolder.resolve(accessor(level, CONTROLLER.above(), top)));
        verify(level, never()).getBlockEntity(any());
    }

    private static BlockAccessor accessor(Level level, BlockPos pos, BlockEntity entity) {
        BlockAccessor accessor = mock(BlockAccessor.class);
        when(accessor.getLevel()).thenReturn(level);
        when(accessor.getPosition()).thenReturn(pos);
        when(accessor.getBlockEntity()).thenReturn(entity);
        when(accessor.getBlockState())
                .thenReturn(
                        entity instanceof BlockEntityThaumatoriumTop
                                ? TTBlocks.THAUMATORIUM_TOP.get().defaultBlockState()
                                : entity instanceof BlockEntityCrucible
                                        ? TTBlocks.CRUCIBLE.get().defaultBlockState()
                                        : Blocks.AIR.defaultBlockState());
        return accessor;
    }
}
