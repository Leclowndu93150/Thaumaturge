package com.leclowndu93150.thaumaturge.content.crucible;

import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jspecify.annotations.Nullable;

public class BlockCrucible extends BaseEntityBlock {
    private static final int LAVA_HURT_INTERVAL = 10;
    private static final int AMBIENT_POP_ODDS = 8;
    private static final double AMBIENT_POP_HEIGHT = 0.7;
    private static final float AMBIENT_POP_VOLUME = 0.15F;
    private static final float AMBIENT_POP_PITCH = 0.8F;
    private static final float AMBIENT_POP_PITCH_JITTER = 0.3F;
    private static final double BLOCK_CENTER = 0.5;
    private static final int SIGNAL_STEPS = 14;
    private static final int MAX_SIGNAL = 15;

    public static final MapCodec<BlockCrucible> CODEC = simpleCodec(BlockCrucible::new);

    public static final VoxelShape SHAPE = Shapes.or(
            Shapes.box(0, 0.1875, 0, 0.125, 1, 1),
            Shapes.box(0.125, 0.1875, 0.125, 0.875, 0.19, 0.875),
            Shapes.box(0.875, 0.1875, 0, 1, 1, 1),
            Shapes.box(0.125, 0.1875, 0, 0.875, 1, 0.125),
            Shapes.box(0.125, 0.1875, 0.875, 0.875, 1, 1),
            Shapes.box(0, 0, 0, 0.1875, 0.1875, 0.125),
            Shapes.box(0, 0, 0.125, 0.125, 0.1875, 0.1875),
            Shapes.box(0.8125, 0, 0, 1, 0.1875, 0.125),
            Shapes.box(0.875, 0, 0.125, 1, 0.1875, 0.1875),
            Shapes.box(0, 0, 0.875, 0.1875, 0.1875, 1),
            Shapes.box(0, 0, 0.8125, 0.125, 0.1875, 0.875),
            Shapes.box(0.8125, 0, 0.875, 1, 0.1875, 1),
            Shapes.box(0.875, 0, 0.8125, 1, 0.1875, 0.875));

    public BlockCrucible(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockEntityCrucible(blockPos, blockState);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack itemStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (level.isClientSide()) return ItemInteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityCrucible crucible))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        FluidStack fs = FluidUtil.getFluidContained(itemStack).orElse(FluidStack.EMPTY);
        if (fs.is(Fluids.WATER) && fs.getAmount() >= BlockEntityCrucible.TANK_CAPACITY) {
            if (crucible.getTank().getFluidAmount() < BlockEntityCrucible.TANK_CAPACITY) {
                if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hitResult.getDirection())) {
                    return ItemInteractionResult.SUCCESS;
                }
            }
        } else if (!player.isCrouching() && hitResult.getDirection() == Direction.UP) {
            ItemStack input = itemStack.copyWithCount(1);
            if (crucible.isBoiling() && crucible.attemptSmelt(input, player) == null) {
                itemStack.shrink(1);
                return ItemInteractionResult.CONSUME;
            }
        }
        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, TTBlockEntities.CRUCIBLE.get(), BlockEntityCrucible::staticTick);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof BlockEntityCrucible crucible) {
            crucible.spillRemnants();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityCrucible crucible)) return 0;
        return comparatorSignal(crucible.getAspects().totalAmount());
    }

    static int comparatorSignal(int aspectAmount) {
        if (aspectAmount <= 0) return 0;
        int filledSteps = Mth.floor((float) aspectAmount / BlockEntityCrucible.MAX_ASPECT * SIGNAL_STEPS);
        return Math.min(MAX_SIGNAL, filledSteps + 1);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityCrucible crucible)) return InteractionResult.PASS;
        if (!player.isCrouching()) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        crucible.spillRemnants();
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) return;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityCrucible crucible)) return;
        if (!crucible.isBoiling()) return;
        if (entity instanceof ItemEntity it && !(it instanceof EntitySpecialItem)) {
            crucible.attemptSmelt(it);
        } else {
            if (entity.tickCount % LAVA_HURT_INTERVAL != 0) return;
            if (entity instanceof LivingEntity e && e.hurt(level.damageSources().inFire(), 1.0F)) {
                entity.clearFire();
                entity.setTicksFrozen(0);
                level.playSound(
                        null,
                        pos.getX() + 0.5F,
                        pos.getY() + 0.5F,
                        pos.getZ() + 0.5F,
                        SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.BLOCKS,
                        0.4F,
                        2.0F + level.getRandom().nextFloat() * 0.4F);
            }
        }

        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(AMBIENT_POP_ODDS) != 0
                || !(level.getBlockEntity(pos) instanceof BlockEntityCrucible crucible)
                || !crucible.isBoiling()) {
            return;
        }
        float pitch = AMBIENT_POP_PITCH + random.nextFloat() * AMBIENT_POP_PITCH_JITTER;
        level.playLocalSound(
                pos.getX() + BLOCK_CENTER,
                pos.getY() + AMBIENT_POP_HEIGHT,
                pos.getZ() + BLOCK_CENTER,
                SoundEvents.LAVA_POP,
                SoundSource.BLOCKS,
                AMBIENT_POP_VOLUME,
                pitch,
                false);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
