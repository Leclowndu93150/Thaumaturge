package com.leclowndu93150.thaumaturge.compat.jade;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.content.crucible.BlockEntityCrucible;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchObelisk;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEntityEldritchObelisk;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatorium;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.BlockEntityThaumatoriumTop;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockEntityInfernalFurnace;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.callback.JadeRayTraceCallback;

class JadeMultiblockIconTest {
    private static final BlockPos CONTROLLER = new BlockPos(15, 64, 15);

    @Test
    void everyGolemPressPartUsesTheControllerItemPreview() {
        Fixture fixture = new Fixture(mock(BlockEntityGolemBuilder.class), TTBlocks.GOLEM_BUILDER.get());
        fixture.check(CONTROLLER, TTBlocks.GOLEM_BUILDER.get(), fixture.controller);
        for (Block part : List.of(
                TTBlocks.PLACEHOLDER_IRON_BARS.get(),
                TTBlocks.PLACEHOLDER_ANVIL.get(),
                TTBlocks.PLACEHOLDER_CAULDRON.get(),
                TTBlocks.PLACEHOLDER_TABLE.get())) {
            fixture.check(CONTROLLER.offset(1, 1, 1), part, null);
        }
    }

    @Test
    void everyThaumatoriumPartUsesTheControllerItemPreview() {
        Fixture fixture = new Fixture(mock(BlockEntityThaumatorium.class), TTBlocks.THAUMATORIUM.get());
        fixture.check(CONTROLLER, TTBlocks.THAUMATORIUM.get(), fixture.controller);
        fixture.check(CONTROLLER.above(), TTBlocks.THAUMATORIUM_TOP.get(), mock(BlockEntityThaumatoriumTop.class));
        fixture.check(CONTROLLER.below(), TTBlocks.CRUCIBLE.get(), mock(BlockEntityCrucible.class));
    }

    @Test
    void infernalFurnacePlaceholderUsesTheControllerItemPreview() {
        Fixture fixture = new Fixture(mock(BlockEntityInfernalFurnace.class), TTBlocks.INFERNAL_FURNACE.get());
        fixture.check(CONTROLLER.offset(1, 0, 0), TTBlocks.NETHER_BRICKS_PLACEHOLDER.get(), null);
        fixture.check(CONTROLLER.offset(0, 0, 1), TTBlocks.OBSIDIAN_PLACEHOLDER.get(), null);
    }

    @Test
    void everyObeliskPartUsesTheBaseEntityPreview() {
        Fixture fixture = new Fixture(mock(BlockEntityEldritchObelisk.class), TTBlocks.ELDRITCH_OBELISK.get());
        for (int part = 0; part < BlockEldritchObelisk.PART_COUNT; part++) {
            BlockPos pos = CONTROLLER.above(part);
            BlockState state =
                    TTBlocks.ELDRITCH_OBELISK.get().defaultBlockState().setValue(BlockEldritchObelisk.PART, part);
            fixture.check(pos, TTBlocks.ELDRITCH_OBELISK.get(), part == 0 ? fixture.controller : null, state);
        }
    }

    @Test
    void unrelatedBlocksKeepTheirOriginalAccessorAndIcon() {
        Fixture fixture = new Fixture(mock(BlockEntityGolemBuilder.class), TTBlocks.GOLEM_BUILDER.get());
        BlockAccessor original = fixture.accessor(CONTROLLER.east(), Blocks.STONE, null);

        assertSame(original, fixture.callback.onRayTrace(original.getHitResult(), original, original));
        verifyNoInteractions(fixture.builder);
    }

    private static final class Fixture {
        private final Level level = mock(Level.class);
        private final BlockEntity controller;
        private final Block block;
        private final BlockAccessor.Builder builder = mock(BlockAccessor.Builder.class, RETURNS_SELF);
        private final BlockAccessor remapped = mock(BlockAccessor.class);
        private final JadeRayTraceCallback callback;

        private Fixture(BlockEntity controller, Block block) {
            this.controller = controller;
            this.block = block;
            when(controller.getBlockPos()).thenReturn(CONTROLLER);
            when(controller.getBlockState()).thenReturn(block.defaultBlockState());
            when(level.hasChunk(anyInt(), anyInt())).thenReturn(true);
            when(level.getBlockEntity(any()))
                    .thenAnswer(call -> CONTROLLER.equals(call.getArgument(0)) ? controller : null);
            when(builder.build()).thenReturn(remapped);
            IWailaClientRegistration registration = mock(IWailaClientRegistration.class);
            when(registration.blockAccessor()).thenReturn(builder);
            new ThaumaturgeJadePlugin().registerClient(registration);
            ArgumentCaptor<JadeRayTraceCallback> capture = ArgumentCaptor.forClass(JadeRayTraceCallback.class);
            verify(registration).addRayTraceCallback(capture.capture());
            callback = capture.getValue();
        }

        private BlockAccessor accessor(BlockPos pos, Block part, BlockEntity entity) {
            return accessor(pos, part, entity, part.defaultBlockState());
        }

        private BlockAccessor accessor(BlockPos pos, Block part, BlockEntity entity, BlockState state) {
            BlockAccessor accessor = mock(BlockAccessor.class);
            when(accessor.getLevel()).thenReturn(level);
            when(accessor.getPosition()).thenReturn(pos);
            when(accessor.getBlockState()).thenReturn(state);
            when(accessor.getBlockEntity()).thenReturn(entity);
            when(accessor.getHitResult())
                    .thenReturn(new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
            return accessor;
        }

        private void check(BlockPos pos, Block part, BlockEntity entity) {
            check(pos, part, entity, part.defaultBlockState());
        }

        private void check(BlockPos pos, Block part, BlockEntity entity, BlockState state) {
            clearInvocations(builder);
            BlockAccessor original = accessor(pos, part, entity, state);

            assertSame(remapped, callback.onRayTrace(original.getHitResult(), original, original));
            verify(builder).from(original);
            verify(builder).blockEntity(controller);
            verify(builder).blockState(controller.getBlockState());
            ArgumentCaptor<BlockHitResult> hit = ArgumentCaptor.forClass(BlockHitResult.class);
            verify(builder).hit(hit.capture());
            assertSame(CONTROLLER, hit.getValue().getBlockPos());
            ArgumentCaptor<ItemStack> icon = ArgumentCaptor.forClass(ItemStack.class);
            verify(builder).fakeBlock(icon.capture());
            assertTrue(icon.getValue().is(block.asItem()));
        }
    }
}
