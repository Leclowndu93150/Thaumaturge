package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSpore;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSporeSwarmer;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBloomRegistry;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockTaintSporeStalk extends Block implements ITaintBlock {
    public static final MapCodec<BlockTaintSporeStalk> CODEC = simpleCodec(BlockTaintSporeStalk::new);
    public static final BooleanProperty MATURE = BooleanProperty.create("mature");
    private static final VoxelShape SHAPE = Shapes.box(0.25, 0.0, 0.25, 0.75, 0.875, 0.75);

    public BlockTaintSporeStalk(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(MATURE, false));
    }

    @Override
    public MapCodec<BlockTaintSporeStalk> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MATURE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        float saturation = TaintEcology.getSaturation(level, pos);
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()
                || level.getDifficulty() == Difficulty.PEACEFUL
                || TaintBloomRegistry.isProtected(level, pos)
                || !TaintEcology.isTainted(level, pos)) {
            return;
        }
        AABB occupancy = new AABB(pos.above()).inflate(0.25);
        boolean occupied = !level.getEntitiesOfClass(
                        Monster.class,
                        occupancy,
                        entity -> entity instanceof EntityTaintSpore || entity instanceof EntityTaintSporeSwarmer)
                .isEmpty();
        if (state.getValue(MATURE)) {
            if (!occupied) {
                level.setBlock(pos, state.setValue(MATURE, false), Block.UPDATE_CLIENTS);
            }
            return;
        }
        if (occupied
                || random.nextInt(10) != 0
                || !level.getBlockState(pos.above()).isAir()
                || level.getEntitiesOfClass(EntityTaintSpore.class, new AABB(pos).inflate(24.0))
                                .size()
                        >= 6) {
            return;
        }
        boolean swarmer = saturation >= 0.85F
                && random.nextInt(20) == 0
                && level.getEntitiesOfClass(EntityTaintSporeSwarmer.class, new AABB(pos).inflate(16.0))
                        .isEmpty();
        Monster spore = swarmer
                ? TCEntities.TAINT_SPORE_SWARMER.get().create(level)
                : TCEntities.TAINT_SPORE.get().create(level);
        if (spore == null) {
            return;
        }
        spore.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(spore);
        level.setBlock(pos, state.setValue(MATURE, true), Block.UPDATE_CLIENTS);
        TaintEcology.addPressure(level, pos, 0.03F);
    }

    @Override
    public void die(net.minecraft.world.level.Level level, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
    }
}
