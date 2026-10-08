package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchStructure;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockEldritchAltar extends BlockEldritchStructure implements EntityBlock, IInteractWithCaster {
    public static final MapCodec<BlockEldritchAltar> CODEC = simpleCodec(BlockEldritchAltar::new);
    private static final float EYE_VOLUME = 0.4F;
    private static final VoxelShape SHAPE = Shapes.or(
            box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0),
            box(0.0, 1.0, 1.0, 16.0, 2.0, 15.0),
            box(1.0, 1.0, 0.0, 15.0, 2.0, 1.0),
            box(1.0, 1.0, 15.0, 15.0, 2.0, 16.0),
            box(1.0, 2.0, 1.0, 15.0, 5.0, 15.0),
            box(1.0, 5.0, 2.0, 15.0, 6.0, 14.0),
            box(2.0, 5.0, 1.0, 14.0, 6.0, 2.0),
            box(2.0, 5.0, 14.0, 14.0, 6.0, 15.0),
            box(2.0, 6.0, 2.0, 14.0, 9.0, 14.0),
            box(2.0, 9.0, 3.0, 14.0, 10.0, 13.0),
            box(3.0, 9.0, 2.0, 13.0, 10.0, 3.0),
            box(3.0, 9.0, 13.0, 13.0, 10.0, 14.0),
            box(3.0, 10.0, 3.0, 13.0, 13.0, 13.0),
            box(3.0, 13.0, 4.0, 13.0, 14.0, 12.0),
            box(4.0, 13.0, 3.0, 12.0, 14.0, 4.0),
            box(4.0, 13.0, 12.0, 12.0, 14.0, 13.0));

    public BlockEldritchAltar(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEldritchAltar> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEldritchAltar(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != TTBlockEntities.ELDRITCH_ALTAR.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, altar) -> ((BlockEntityEldritchAltar) altar).serverTick(tickLevel, pos);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (!stack.is(TTItems.ELDRITCH_EYE.get())
                || !(level.getBlockEntity(pos) instanceof BlockEntityEldritchAltar altar)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (altar.getEyes() >= BlockEntityEldritchAltar.MAX_EYES
                || altar.ritual().isPresent()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            altar.setEyes(altar.getEyes() + 1);
            stack.consume(1, player);
            level.playSound(null, pos, TTSounds.CRYSTAL.get(), SoundSource.BLOCKS, EYE_VOLUME, 1.0F);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown()
                || !ThaumaturgeServerConfig.LABYRINTH.allowEyeRemoval.get()
                || !(level.getBlockEntity(pos) instanceof BlockEntityEldritchAltar altar)
                || altar.getEyes() == 0
                || altar.ritual().isPresent()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            altar.setEyes(altar.getEyes() - 1);
            player.getInventory().placeItemBackInInventory(new ItemStack(TTItems.ELDRITCH_EYE.get()));
            level.playSound(null, pos, TTSounds.CRYSTAL.get(), SoundSource.BLOCKS, EYE_VOLUME, 0.6F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onCasterRightClick(
            Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof BlockEntityEldritchAltar altar)) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            AltarRituals.start(serverLevel, altar, serverPlayer);
        }
        return true;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof BlockEntityEldritchAltar altar)
            altar.removePortal();
        super.onRemove(state, level, pos, next, moving);
    }
}
