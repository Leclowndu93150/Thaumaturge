package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockEldritchObelisk extends BlockEldritchStructure implements EntityBlock {
    public static final MapCodec<BlockEldritchObelisk> CODEC = simpleCodec(BlockEldritchObelisk::new);
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 5);
    public static final int PART_COUNT = 6;

    static final VoxelShape BASE_SHAPE = Shapes.or(
            Block.box(6, 0, 6, 10, 4, 10),
            Block.box(4, 4, 4, 12, 8, 12),
            Block.box(2, 8, 2, 14, 12, 14),
            Block.box(0, 12, 0, 16, 16, 16));
    static final VoxelShape SHAFT_SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 1),
            Block.box(0, 0, 15, 16, 16, 16),
            Block.box(0, 0, 1, 1, 16, 15),
            Block.box(15, 0, 1, 16, 16, 15));
    static final VoxelShape TOP_CAP_SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(2, 4, 2, 14, 8, 14),
            Block.box(4, 8, 4, 12, 12, 12),
            Block.box(6, 12, 6, 10, 16, 10));
    static final VoxelShape TOP_TIP_SHAPE = Block.box(6, 0, 6, 10, 4, 10);
    static final VoxelShape COLLISION_SHAPE = Shapes.block();

    public BlockEldritchObelisk(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PART, 0));
    }

    @Override
    protected MapCodec<BlockEldritchObelisk> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PART);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() + PART_COUNT - 1 >= level.getMaxBuildHeight()) {
            return null;
        }
        for (int part = 1; part < PART_COUNT; part++) {
            if (!level.isEmptyBlock(pos.above(part))) {
                return null;
            }
        }
        return defaultBlockState();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !oldState.is(this) && state.getValue(PART) == 0) {
            ensureInteractionParts(level, pos, state);
        }
    }

    static void ensureInteractionParts(Level level, BlockPos basePos, BlockState baseState) {
        BlockEldritchObelisk obelisk = TTBlocks.ELDRITCH_OBELISK.get();
        if (level.isClientSide() || !baseState.is(obelisk) || baseState.getValue(PART) != 0) {
            return;
        }
        for (int part = 1; part < PART_COUNT; part++) {
            BlockPos partPos = basePos.above(part);
            if (!LoadedChunks.contains(level, partPos)) {
                continue;
            }
            BlockState current = level.getBlockState(partPos);
            if (current.is(obelisk)) {
                continue;
            } else if (!current.isAir()) {
                continue;
            }
            level.setBlock(
                    partPos,
                    obelisk.defaultBlockState().setValue(PART, part).setValue(SEALED, baseState.getValue(SEALED)),
                    Block.UPDATE_ALL);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return interactionShape(state.getValue(PART));
    }

    static VoxelShape interactionShape(int part) {
        return switch (part) {
            case 0 -> BASE_SHAPE;
            case 1, 2, 3 -> SHAFT_SHAPE;
            case 4 -> TOP_CAP_SHAPE;
            case 5 -> TOP_TIP_SHAPE;
            default -> Shapes.empty();
        };
    }

    static VoxelShape collisionShape(int part) {
        return part >= 0 && part < PART_COUNT ? COLLISION_SHAPE : Shapes.empty();
    }

    public static BlockPos basePos(BlockState state, BlockPos pos) {
        return pos.below(state.getValue(PART));
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return collisionShape(state.getValue(PART));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == 0 ? new BlockEntityEldritchObelisk(pos, state) : null;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            int part = state.getValue(PART);
            if (part == 0) {
                for (int childPart = 1; childPart < PART_COUNT; childPart++) {
                    BlockPos childPos = pos.above(childPart);
                    if (!LoadedChunks.contains(level, childPos)) {
                        continue;
                    }
                    BlockState childState = level.getBlockState(childPos);
                    if (childState.is(this) && childState.getValue(PART) == childPart) {
                        level.setBlock(childPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            } else {
                BlockPos basePos = pos.below(part);
                if (LoadedChunks.contains(level, basePos)) {
                    BlockState baseState = level.getBlockState(basePos);
                    if (baseState.is(this) && baseState.getValue(PART) == 0) {
                        level.setBlock(basePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return state.getValue(PART) == 0 && type == TTBlockEntities.ELDRITCH_OBELISK.get()
                ? (tickLevel, pos, tickState, obelisk) ->
                        ((BlockEntityEldritchObelisk) obelisk).serverTick(tickLevel, pos)
                : null;
    }
}
