package com.leclowndu93150.thaumaturge.content.crucible;

import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
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
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jspecify.annotations.Nullable;

public class BlockCrucible extends BaseEntityBlock {
    private int delay = 0;

    public static final MapCodec<BlockCrucible> CODEC = simpleCodec(BlockCrucible::new);

    public static final VoxelShape SHAPE = Shapes.or(Shapes.box(0, 0.1875, 0, 0.125, 1, 1), Shapes.box(0.125, 0.1875, 0.125, 0.875, 0.19, 0.875), Shapes.box(0.875, 0.1875, 0, 1, 1, 1),
            Shapes.box(0.125, 0.1875, 0, 0.875, 1, 0.125), Shapes.box(0.125, 0.1875, 0.875, 0.875, 1, 1), Shapes.box(0, 0, 0, 0.1875, 0.1875, 0.125), Shapes.box(0, 0, 0.125, 0.125, 0.1875, 0.1875),
            Shapes.box(0.8125, 0, 0, 1, 0.1875, 0.125), Shapes.box(0.875, 0, 0.125, 1, 0.1875, 0.1875), Shapes.box(0, 0, 0.875, 0.1875, 0.1875, 1), Shapes.box(0, 0, 0.8125, 0.125, 0.1875, 0.875),
            Shapes.box(0.8125, 0, 0.875, 1, 0.1875, 1), Shapes.box(0.875, 0, 0.8125, 1, 0.1875, 0.875));

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
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityCrucible crucible))
            return InteractionResult.PASS;
        FluidStack fs = FluidUtil.getFirstStackContained(itemStack);
        if (fs.is(Fluids.WATER) && fs.amount() >= BlockEntityCrucible.TANK_CAPACITY) {
            if (crucible.getTank().getAmountAsInt(0) < BlockEntityCrucible.TANK_CAPACITY) {
                if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hitResult.getDirection(), null)) {
                    return InteractionResult.SUCCESS;
                }
            }
        } else if (!player.isCrouching() /*&& (!(player.getItemInHand(hand).getItem() instanceof ICaster))*/
                && hitResult.getDirection() == Direction.UP) {
            ItemStack input = itemStack.copyWithCount(1);
            if (crucible.getHeat() > 150 && crucible.getTank().getAmountAsInt(0) > 0 && crucible.attemptSmelt(input, player) == null) {
                itemStack.shrink(1);
            }
        }
        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, TCBlockEntities.CRUCIBLE.get(), BlockEntityCrucible::staticTick);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BlockEntityCrucible crucible) {
            crucible.spillRemnants();
        }
        super.destroy(level, pos, state);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityCrucible crucible))
            return 0;
        float ratio = (float) crucible.getAspects().totalAmount() / BlockEntityCrucible.MAX_ASPECT;

        return Mth.floor(ratio * 14.0F) + (crucible.getAspects().totalAmount() > 0 ? 1 : 0);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityCrucible crucible))
            return InteractionResult.PASS;
        if (!player.isCrouching())
            return InteractionResult.PASS;
        crucible.spillRemnants();
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level.isClientSide())
            return;
        if (!(level.getBlockEntity(pos) instanceof BlockEntityCrucible crucible))
            return;
        if (crucible.getHeat() <= 150 || crucible.getTank().getAmountAsInt(0) <= 0)
            return;
        if (entity instanceof ItemEntity it && !(it instanceof EntitySpecialItem)) {
            crucible.attemptSmelt(it);
        } else {
            this.delay++;
            if (this.delay < 10)
                return;
            delay = 0;
            if (entity instanceof LivingEntity e && !e.isInvulnerableTo((ServerLevel) level, level.damageSources().lava())) {
                entity.lavaHurt();
                effectApplier.apply(InsideBlockEffectType.EXTINGUISH);
                effectApplier.apply(InsideBlockEffectType.CLEAR_FREEZE);
                level.playSound(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 2.0F + level.getRandom().nextFloat() * 0.4F);
            }
        }

        super.entityInside(state, level, pos, entity, effectApplier, isPrecise);
    }
}
