package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.taint.ITaintBlock;
import com.leclowndu93150.thaumaturge.content.device.DeviceShapes;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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

    private static final VoxelShape ORB_0 = Shapes.or(
            Block.box(3.0, 0.0, 3.0, 11.0, 1.0, 11.0),
            Block.box(2.0, 1.0, 3.0, 12.0, 9.0, 11.0),
            Block.box(3.0, 1.0, 2.0, 11.0, 9.0, 3.0),
            Block.box(3.0, 1.0, 11.0, 11.0, 9.0, 12.0),
            Block.box(3.0, 9.0, 3.0, 11.0, 10.0, 11.0),
            Block.box(5.0, 10.0, 6.0, 8.0, 11.0, 9.0));

    private static final VoxelShape ORB_1 = Shapes.or(
            Block.box(3.0, 0.0, 4.0, 9.0, 1.0, 10.0),
            Block.box(2.0, 1.0, 4.0, 10.0, 7.0, 10.0),
            Block.box(3.0, 1.0, 3.0, 9.0, 7.0, 4.0),
            Block.box(3.0, 1.0, 10.0, 9.0, 7.0, 11.0),
            Block.box(3.0, 7.0, 4.0, 9.0, 8.0, 10.0),
            Block.box(5.0, 8.0, 6.0, 7.0, 9.0, 8.0),
            Block.box(9.0, 0.0, 8.0, 13.0, 1.0, 12.0),
            Block.box(10.0, 1.0, 7.0, 13.0, 5.0, 10.0),
            Block.box(13.0, 1.0, 8.0, 14.0, 5.0, 11.0),
            Block.box(9.0, 1.0, 10.0, 13.0, 6.0, 11.0),
            Block.box(8.0, 1.0, 11.0, 14.0, 5.0, 12.0),
            Block.box(9.0, 1.0, 12.0, 13.0, 5.0, 13.0),
            Block.box(10.0, 5.0, 8.0, 13.0, 6.0, 10.0),
            Block.box(9.0, 5.0, 11.0, 13.0, 6.0, 12.0));

    private static final VoxelShape ORB_2 = Shapes.or(
            Block.box(2.0, 0.0, 8.0, 6.0, 1.0, 12.0),
            Block.box(1.0, 1.0, 8.0, 7.0, 5.0, 12.0),
            Block.box(2.0, 1.0, 7.0, 6.0, 5.0, 8.0),
            Block.box(2.0, 1.0, 12.0, 6.0, 5.0, 13.0),
            Block.box(2.0, 5.0, 8.0, 6.0, 6.0, 12.0),
            Block.box(9.0, 0.0, 2.0, 13.0, 1.0, 6.0),
            Block.box(8.0, 1.0, 2.0, 14.0, 4.0, 6.0),
            Block.box(9.0, 1.0, 1.0, 13.0, 4.0, 2.0),
            Block.box(9.0, 1.0, 6.0, 13.0, 4.0, 7.0),
            Block.box(9.0, 4.0, 2.0, 13.0, 5.0, 6.0),
            Block.box(10.0, 0.0, 10.0, 14.0, 1.0, 14.0),
            Block.box(9.0, 1.0, 10.0, 15.0, 5.0, 14.0),
            Block.box(10.0, 1.0, 9.0, 14.0, 5.0, 10.0),
            Block.box(10.0, 1.0, 14.0, 14.0, 5.0, 15.0),
            Block.box(10.0, 5.0, 10.0, 14.0, 6.0, 14.0),
            Block.box(11.0, 6.0, 11.0, 13.0, 7.0, 13.0));

    private static final List<Map<Direction, VoxelShape>> ORB_SHAPES = List.of(
            DeviceShapes.facingShapesFromUp(ORB_0),
            DeviceShapes.facingShapesFromUp(ORB_1),
            DeviceShapes.facingShapesFromUp(ORB_2));

    private static final int DIE_CHANCE = 10;
    private static final int GEYSER_CHANCE = 100;
    private static final float CRAWLER_ON_BREAK_CHANCE = 0.333F;
    private static final float BREAK_POLLUTE_AMOUNT = 1.0F;
    private static final int PASSIVE_POLLUTE_CHANCE = 200;
    private static final float PASSIVE_POLLUTE_AMOUNT = 1.0F;
    private static final float PASSIVE_POLLUTE_MAX_RATIO = 0.2F;

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
        return ORB_SHAPES
                .get(RandomSource.create(state.getSeed(pos)).nextInt(ORB_SHAPES.size()))
                .get(state.getValue(FACING));
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TaintHelper.trySpreadTaintedBiome(level, pos, random);
        if (!TaintHelper.isEcologicallySustained(level, pos) && random.nextInt(DIE_CHANCE) == 0) {
            decay(level, pos, state);
            return;
        }
        int auraBase = AuraHelper.getAuraBase(level, pos);
        if (TaintHelper.isEcologicallySustained(level, pos)
                && auraBase > 0
                && AuraHelper.getFlux(level, pos) <= auraBase * PASSIVE_POLLUTE_MAX_RATIO
                && random.nextInt(PASSIVE_POLLUTE_CHANCE) == 0) {
            AuraHelper.polluteAura(level, pos, PASSIVE_POLLUTE_AMOUNT, true);
            return;
        }
        TaintHelper.spreadFibres(level, pos, false);
        BlockState below = level.getBlockState(pos.below());
        if (below.is(TTBlocks.TAINT_LOG.get())) {
            Direction.Axis axis = below.getValue(RotatedPillarBlock.AXIS);
            if (axis == Direction.Axis.Y && random.nextInt(GEYSER_CHANCE) == 0) {
                level.setBlock(pos, TTBlocks.TAINT_GEYSER.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {

            if (level.getRandom().nextFloat() < CRAWLER_ON_BREAK_CHANCE) {
                EntityTaintCrawler crawler = TTEntities.TAINT_CRAWLER.get().create(level);
                if (crawler != null) {
                    crawler.moveTo(
                            pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            level.getRandom().nextInt(360),
                            0.0F);
                    level.addFreshEntity(crawler);
                }
            } else {
                AuraHelper.polluteAura(level, pos, BREAK_POLLUTE_AMOUNT, true);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void decay(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, TTBlocks.FLUX_GOO.get().defaultBlockState(), Block.UPDATE_ALL);
    }
}
