package com.leclowndu93150.thaumaturge.content.infernalfurnace;

import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.AdvancedAlchemicalFurnaceStructure;
import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.AdvancedFurnaceShapes;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockGolemBuilder;
import com.leclowndu93150.thaumaturge.content.golem.press.GolemPressShapes;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
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
import net.minecraft.world.phys.shapes.CollisionContext;
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

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (AdvancedAlchemicalFurnaceStructure.isPart(state)) {
            VoxelShape furnace = AdvancedFurnaceShapes.find(level, pos);
            if (furnace != null) {
                return furnace;
            }
        }
        if (state.is(TTBlocks.PLACEHOLDER_IRON_BARS) || state.is(TTBlocks.PLACEHOLDER_ANVIL) || state.is(TTBlocks.PLACEHOLDER_CAULDRON) || state.is(TTBlocks.PLACEHOLDER_TABLE)) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 0; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos corePos = pos.offset(x, y, z);
                        BlockState core = level.getBlockState(corePos);
                        if (core.is(TTBlocks.GOLEM_BUILDER)) {
                            Direction facing = core.getValue(BlockGolemBuilder.FACING);
                            BlockPos offset = pos.subtract(corePos);
                            Direction right = facing.getClockWise();
                            Direction back = facing.getOpposite();
                            BlockPos expected;
                            if (state.is(TTBlocks.PLACEHOLDER_IRON_BARS)) {
                                expected = BlockPos.ZERO.above();
                            } else if (state.is(TTBlocks.PLACEHOLDER_TABLE)) {
                                expected = BlockPos.ZERO.relative(right);
                            } else if (state.is(TTBlocks.PLACEHOLDER_CAULDRON)) {
                                expected = BlockPos.ZERO.relative(back);
                            } else {
                                expected = BlockPos.ZERO.relative(right).relative(back);
                            }
                            if (offset.equals(expected)) {
                                return GolemPressShapes.at(facing, offset);
                            }
                        }
                    }
                }
            }
        }
        return super.getShape(state, level, pos, context);
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
        if ((state.is(TTBlocks.NETHER_BRICKS_PLACEHOLDER) || state.is(TTBlocks.OBSIDIAN_PLACEHOLDER)) && !level.isClientSide()) {
            destroyFor : for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos offsetPos = pos.offset(x, y, z);
                        BlockState offsetState = level.getBlockState(offsetPos);
                        if (offsetState.is(TTBlocks.INFERNAL_FURNACE)) {
                            BlockInfernalFurnace.destroyFurnace(level, offsetPos, offsetState, pos);
                            break destroyFor;
                        }
                    }
                }
            }
        }
        if (!level.isClientSide()
                && (state.is(TTBlocks.PLACEHOLDER_IRON_BARS) || state.is(TTBlocks.PLACEHOLDER_ANVIL) || state.is(TTBlocks.PLACEHOLDER_CAULDRON) || state.is(TTBlocks.PLACEHOLDER_TABLE))) {
            restoreGolemPress : for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos offsetPos = pos.offset(x, y, z);
                        if (level.getBlockState(offsetPos).is(TTBlocks.GOLEM_BUILDER)) {
                            BlockGolemBuilder.restoreStructure(level, offsetPos, pos);
                            break restoreGolemPress;
                        }
                    }
                }
            }
        }
        if (!level.isClientSide() && AdvancedAlchemicalFurnaceStructure.isPart(state)) {
            AdvancedAlchemicalFurnaceStructure.disassembleAround(level, pos);
        }
        super.destroy(level, pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.is(TTBlocks.PLACEHOLDER_IRON_BARS) || state.is(TTBlocks.PLACEHOLDER_ANVIL) || state.is(TTBlocks.PLACEHOLDER_CAULDRON) || state.is(TTBlocks.PLACEHOLDER_TABLE)) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos offsetPos = pos.offset(x, y, z);
                        if (level.getBlockState(offsetPos).is(TTBlocks.GOLEM_BUILDER)) {
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
