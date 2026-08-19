package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockTaintFeature extends DirectionalBlock implements ITaintBlock {
    public static final MapCodec<BlockTaintFeature> CODEC = simpleCodec(BlockTaintFeature::new);

    private static final VoxelShape SHAPE_DOWN = Shapes.box(0.125, 0.625, 0.125, 0.875, 1.0, 0.875);
    private static final VoxelShape SHAPE_UP = Shapes.box(0.125, 0.0, 0.125, 0.875, 0.375, 0.875);
    private static final VoxelShape SHAPE_NORTH = Shapes.box(0.125, 0.125, 0.625, 0.875, 0.875, 1.0);
    private static final VoxelShape SHAPE_SOUTH = Shapes.box(0.125, 0.125, 0.0, 0.875, 0.875, 0.375);
    private static final VoxelShape SHAPE_WEST = Shapes.box(0.625, 0.125, 0.125, 1.0, 0.875, 0.875);
    private static final VoxelShape SHAPE_EAST = Shapes.box(0.0, 0.125, 0.125, 0.375, 0.875, 0.875);

    private static final int DIE_CHANCE = 10;
    private static final int GEYSER_CHANCE = 100;
    private static final float CRAWLER_ON_BREAK_CHANCE = 0.333F;
    private static final float BREAK_POLLUTE_AMOUNT = 1.0F;

    public BlockTaintFeature(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    public MapCodec<BlockTaintFeature> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!TaintHelper.isNearTaintSeed(level, pos) && random.nextInt(DIE_CHANCE) == 0) {
            die(level, pos, state);
            return;
        }
        TaintHelper.spreadFibres(level, pos, false);
        BlockState below = level.getBlockState(pos.below());
        if (below.is(TCBlocks.TAINT_LOG.get())) {
            Direction.Axis axis = below.getValue(RotatedPillarBlock.AXIS);
            if (axis == Direction.Axis.Y && random.nextInt(GEYSER_CHANCE) == 0) {
                level.setBlock(pos, TCBlocks.TAINT_GEYSER.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        if (level.getRandom().nextFloat() < CRAWLER_ON_BREAK_CHANCE) {
            EntityTaintCrawler crawler = TCEntities.TAINT_CRAWLER.get().create(level, EntitySpawnReason.NATURAL);
            if (crawler != null) {
                crawler.snapTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, level.getRandom().nextInt(360), 0.0F);
                level.addFreshEntity(crawler);
            }
        } else {
            AuraHelper.polluteAura(level, pos, BREAK_POLLUTE_AMOUNT, true);
        }
    }

    @Override
    public void die(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, TCBlocks.FLUX_GOO.get().defaultBlockState(), Block.UPDATE_ALL);
    }
}
