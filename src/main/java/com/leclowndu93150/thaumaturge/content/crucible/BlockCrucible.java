package com.leclowndu93150.thaumaturge.content.crucible;

import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jspecify.annotations.Nullable;

public class BlockCrucible extends BaseEntityBlock {
    private static final double[][] SHAPE_BOXES = {{0.0, 0.1875, 0.0, 0.125, 1.0, 1.0}, {0.125, 0.1875, 0.125, 0.875, 0.19, 0.875}, {0.875, 0.1875, 0.0, 1.0, 1.0, 1.0},
            {0.125, 0.1875, 0.0, 0.875, 1.0, 0.125}, {0.125, 0.1875, 0.875, 0.875, 1.0, 1.0}, {0.0, 0.0, 0.0, 0.1875, 0.1875, 0.125}, {0.0, 0.0, 0.125, 0.125, 0.1875, 0.1875},
            {0.8125, 0.0, 0.0, 1.0, 0.1875, 0.125}, {0.875, 0.0, 0.125, 1.0, 0.1875, 0.1875}, {0.0, 0.0, 0.875, 0.1875, 0.1875, 1.0}, {0.0, 0.0, 0.8125, 0.125, 0.1875, 0.875},
            {0.8125, 0.0, 0.875, 1.0, 0.1875, 1.0}, {0.875, 0.0, 0.8125, 1.0, 0.1875, 0.875}};

    public static final MapCodec<BlockCrucible> CODEC = simpleCodec(BlockCrucible::new);
    public static final VoxelShape SHAPE = buildShape();

    private static final List<InsideBlockEffectType> CONTACT_EFFECTS = List.of(InsideBlockEffectType.EXTINGUISH, InsideBlockEffectType.CLEAR_FREEZE);
    private static final int TANK_SLOT = 0;
    private static final int SCALD_INTERVAL = 10;
    private static final float SCALD_DAMAGE = 1.0F;
    private static final float HISS_VOLUME = 0.4F;
    private static final float HISS_PITCH = 1.7F;
    private static final float HISS_PITCH_JITTER = 0.3F;
    private static final int SIGNAL_STEPS = 14;
    private static final int MAX_SIGNAL = 15;
    private static final int AMBIENT_POP_ODDS = 8;
    private static final double AMBIENT_POP_HEIGHT = 0.7;
    private static final float AMBIENT_POP_VOLUME = 0.15F;
    private static final float AMBIENT_POP_PITCH = 0.8F;
    private static final float AMBIENT_POP_PITCH_JITTER = 0.3F;
    private static final double BLOCK_CENTER = 0.5;

    public BlockCrucible(Properties properties) {
        super(properties);
    }

    private static VoxelShape buildShape() {
        VoxelShape shape = Shapes.empty();
        for (double[] box : SHAPE_BOXES) {
            shape = Shapes.or(shape, Shapes.box(box[0], box[1], box[2], box[3], box[4], box[5]));
        }
        return shape;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityCrucible(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return level.isClientSide() ? InteractionResult.SUCCESS : serverUseItem(stack, level, pos, player, hand, hit);
    }

    private static InteractionResult serverUseItem(ItemStack stack, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntityCrucible crucible = crucibleAt(level, pos);
        if (crucible == null) {
            return InteractionResult.PASS;
        }
        boolean handled = isWaterFill(stack) ? tryFill(pos, crucible, hand, player) : tryDropIn(crucible, stack, player, hit);
        return handled ? InteractionResult.SUCCESS_SERVER : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    private static boolean tryDropIn(BlockEntityCrucible crucible, ItemStack stack, Player player, BlockHitResult hit) {
        boolean dropAllowed = hit.getDirection() == Direction.UP && !player.isShiftKeyDown() && crucible.isBoiling();
        if (!dropAllowed || crucible.dropIn(stack.copyWithCount(1), player) != null) {
            return false;
        }
        stack.shrink(1);
        return true;
    }

    private static boolean tryFill(BlockPos pos, BlockEntityCrucible crucible, InteractionHand hand, Player player) {
        FluidStacksResourceHandler tank = crucible.getTank();
        boolean hasRoom = tank.getAmountAsInt(TANK_SLOT) < BlockEntityCrucible.TANK_CAPACITY;
        return hasRoom && FluidUtil.interactWithFluidHandler(player, hand, pos, tank);
    }

    private static boolean isWaterFill(ItemStack stack) {
        FluidStack contained = FluidUtil.getFirstStackContained(stack);
        boolean fullBucket = contained.getAmount() >= FluidType.BUCKET_VOLUME;
        return fullBucket && contained.getFluid() == Fluids.WATER;
    }

    private static @Nullable BlockEntityCrucible crucibleAt(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos, TTBlockEntities.CRUCIBLE.get()).orElse(null);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            return player.isShiftKeyDown() ? emptyCrucible(level, pos) : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult emptyCrucible(Level level, BlockPos pos) {
        Optional.ofNullable(crucibleAt(level, pos)).ifPresent(BlockEntityCrucible::emptyOut);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean intersects) {
        if (level instanceof ServerLevel serverLevel) {
            Optional.ofNullable(crucibleAt(serverLevel, pos)).filter(BlockEntityCrucible::isBoiling).ifPresent(crucible -> applyContact(serverLevel, pos, entity, effectApplier, crucible));
        }
        super.entityInside(state, level, pos, entity, effectApplier, intersects);
    }

    private static void applyContact(ServerLevel level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, BlockEntityCrucible crucible) {
        if (entity instanceof ItemEntity item) {
            if (!(item instanceof EntitySpecialItem) && item.isAlive()) {
                crucible.absorbThrown(item);
            }
            return;
        }
        if (entity instanceof LivingEntity creature) {
            CONTACT_EFFECTS.forEach(effectApplier::apply);
            if (creature.tickCount % SCALD_INTERVAL == 0) {
                scaldCreature(level, pos, creature);
            }
        }
    }

    private static void scaldCreature(ServerLevel level, BlockPos pos, LivingEntity creature) {
        if (creature.hurtServer(level, level.damageSources().inFire(), SCALD_DAMAGE)) {
            float pitch = HISS_PITCH + level.getRandom().nextFloat() * HISS_PITCH_JITTER;
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, HISS_VOLUME, pitch);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(AMBIENT_POP_ODDS) != 0) {
            return;
        }
        BlockEntityCrucible crucible = crucibleAt(level, pos);
        if (crucible != null && crucible.isBoiling()) {
            float pitch = AMBIENT_POP_PITCH + random.nextFloat() * AMBIENT_POP_PITCH_JITTER;
            level.playLocalSound(pos.getX() + BLOCK_CENTER, pos.getY() + AMBIENT_POP_HEIGHT, pos.getZ() + BLOCK_CENTER, SoundEvents.LAVA_POP, SoundSource.BLOCKS, AMBIENT_POP_VOLUME, pitch, false);
        }
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TTBlockEntities.CRUCIBLE.get(), BlockEntityCrucible::staticTick);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        BlockEntityCrucible crucible = crucibleAt(level, pos);
        int total = crucible == null ? 0 : crucible.getAspects().totalAmount();
        if (total <= 0) {
            return 0;
        }
        int filled = Mth.floor((float) total / BlockEntityCrucible.MAX_ASPECT * SIGNAL_STEPS);
        return Math.min(MAX_SIGNAL, filled + 1);
    }

}
