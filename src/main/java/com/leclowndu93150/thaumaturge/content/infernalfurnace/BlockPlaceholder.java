package com.leclowndu93150.thaumaturge.content.infernalfurnace;

import com.leclowndu93150.thaumaturge.content.golem.press.BlockGolemBuilder;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockPlaceholder extends Block {
    private final boolean visible;

    public BlockPlaceholder(Properties properties) {
        this(properties, false);
    }

    public BlockPlaceholder(Properties properties, boolean visible) {
        super(properties);
        this.visible = visible;
    }

    protected boolean propagatesSkylightDown(BlockState state) {
        return state.getFluidState().isEmpty();
    }

    protected RenderShape getRenderShape(BlockState state) {
        return visible ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if ((state.is(TCBlocks.NETHER_BRICKS_PLACEHOLDER) || state.is(TCBlocks.OBSIDIAN_PLACEHOLDER)) && !level.isClientSide()) {
            destroyFor : for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos offsetPos = pos.offset(x, y, z);
                        BlockState offsetState = level.getBlockState(offsetPos);
                        if (offsetState.is(TCBlocks.INFERNAL_FURNACE)) {
                            BlockInfernalFurnace.destroyFurnace(level, offsetPos, offsetState, pos);
                            break destroyFor;
                        }
                    }
                }
            }
        }
        if (!level.isClientSide()
                && (state.is(TCBlocks.PLACEHOLDER_IRON_BARS) || state.is(TCBlocks.PLACEHOLDER_ANVIL) || state.is(TCBlocks.PLACEHOLDER_CAULDRON) || state.is(TCBlocks.PLACEHOLDER_TABLE))) {
            restoreGolemPress : for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos offsetPos = pos.offset(x, y, z);
                        if (level.getBlockState(offsetPos).is(TCBlocks.GOLEM_BUILDER)) {
                            BlockGolemBuilder.restoreStructure(level, offsetPos, pos);
                            break restoreGolemPress;
                        }
                    }
                }
            }
        }
        super.destroy(level, pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.is(TCBlocks.PLACEHOLDER_IRON_BARS) || state.is(TCBlocks.PLACEHOLDER_ANVIL) || state.is(TCBlocks.PLACEHOLDER_CAULDRON) || state.is(TCBlocks.PLACEHOLDER_TABLE)) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos offsetPos = pos.offset(x, y, z);
                        if (level.getBlockState(offsetPos).is(TCBlocks.GOLEM_BUILDER)) {
                            return BlockGolemBuilder.openBuilderGui(level, offsetPos, player);
                        }
                    }
                }
            }
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState neighborState, Direction direction) {
        return true;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    @Override
    protected int getLightDampening(BlockState state) {
        return 0;
    }
}
