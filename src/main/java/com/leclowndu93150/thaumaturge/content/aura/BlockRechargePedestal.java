package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockRechargePedestal extends BaseEntityBlock {
    public static final MapCodec<BlockRechargePedestal> CODEC = simpleCodec(BlockRechargePedestal::new);

    private static final float POP_VOLUME = 0.25F;
    private static final float TAKE_PITCH = 1.45F;
    private static final float PLACE_PITCH = 1.7F;
    private static final float PITCH_WOBBLE = 0.12F;

    private static final VoxelShape SHAPE = Shapes.or(box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0), box(2.0, 2.0, 2.0, 14.0, 4.0, 14.0), box(5.0, 4.0, 5.0, 11.0, 12.0, 11.0),
            box(4.0, 12.0, 4.0, 12.0, 14.0, 12.0), box(4.0, 14.0, 4.0, 6.0, 16.0, 6.0), box(4.0, 14.0, 10.0, 6.0, 16.0, 12.0), box(10.0, 14.0, 4.0, 12.0, 16.0, 6.0),
            box(10.0, 14.0, 10.0, 12.0, 16.0, 12.0), box(6.0, 14.0, 6.0, 10.0, 15.0, 10.0));

    public BlockRechargePedestal(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockRechargePedestal> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityRechargePedestal(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TTBlockEntities.RECHARGE_PEDESTAL.get(), BlockEntityRechargePedestal::serverTick);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return handleClick(level, pos, player, InteractionHand.MAIN_HAND);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return handleClick(level, pos, player, hand);
    }

    private static InteractionResult handleClick(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityRechargePedestal pedestal)) {
            return InteractionResult.PASS;
        }
        ItemStack resting = pedestal.getItem();
        ItemStack inHand = player.getItemInHand(hand);
        boolean taking = !resting.isEmpty();
        if (!taking && !BlockEntityRechargePedestal.accepts(inHand)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (taking) {
            pedestal.setItem(ItemStack.EMPTY);
            returnToPlayer(player, resting);
        } else {
            pedestal.setItem(inHand.copyWithCount(1));
            inHand.consume(1, player);
        }
        playPop(level, pos, taking ? TAKE_PITCH : PLACE_PITCH);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void returnToPlayer(Player player, ItemStack stack) {
        boolean stored = player.getInventory().add(stack);
        if (!stored || !stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    private static void playPop(Level level, BlockPos pos, float pitch) {
        RandomSource random = level.getRandom();
        float wobble = (random.nextFloat() - random.nextFloat()) * PITCH_WOBBLE;
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, POP_VOLUME, pitch + wobble);
    }
}
