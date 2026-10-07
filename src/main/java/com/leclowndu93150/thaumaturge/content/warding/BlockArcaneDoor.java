package com.leclowndu93150.thaumaturge.content.warding;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class BlockArcaneDoor extends DoorBlock {
    public static final MapCodec<BlockArcaneDoor> CODEC = simpleCodec(BlockArcaneDoor::new);
    private static final BlockSetType ARCANE = new BlockSetType("thaumaturge_arcane", false, false, true, BlockSetType.PressurePlateSensitivity.EVERYTHING, SoundType.WOOD,
            SoundEvents.WOODEN_DOOR_CLOSE, SoundEvents.WOODEN_DOOR_OPEN, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF,
            SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON, SoundEvents.WOODEN_BUTTON_CLICK_OFF, SoundEvents.WOODEN_BUTTON_CLICK_ON);

    public BlockArcaneDoor(Properties properties) {
        super(ARCANE, properties);
    }

    @Override
    public MapCodec<BlockArcaneDoor> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(POWERED, false).setValue(OPEN, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel serverLevel && placer instanceof Player player) {
            WardHandler.ward(serverLevel, pos, player.getUUID());
            WardHandler.ward(serverLevel, pos.above(), player.getUUID());
            ArcaneAccess.lock(serverLevel, pos, player.getUUID());
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (!ArcaneAccess.canAccess(serverLevel, ArcaneAccess.lockOrigin(state, pos), player)) {
            return InteractionResult.FAIL;
        }
        setOpen(null, level, state, pos, !isOpen(state));
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!(level instanceof ServerLevel serverLevel) || defaultBlockState().is(block)) {
            return;
        }
        BlockPos origin = ArcaneAccess.lockOrigin(state, pos);
        boolean signal = (level.hasNeighborSignal(origin) || level.hasNeighborSignal(origin.above())) && hasLinkedPlate(serverLevel, origin);
        if (signal != state.getValue(POWERED)) {
            BlockState powered = state.setValue(POWERED, signal);
            level.setBlock(pos, powered, Block.UPDATE_CLIENTS);
            setOpen(null, level, powered, pos, signal);
        }
    }

    private static boolean hasLinkedPlate(ServerLevel level, BlockPos origin) {
        for (int height = 0; height <= 1; height++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos platePos = origin.above(height).relative(direction);
                BlockState plate = level.getBlockState(platePos);
                if (plate.is(TTBlocks.ARCANE_PRESSURE_PLATE) && plate.getValue(POWERED) && ArcaneAccess.sharesAccess(level, platePos, origin)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        WardHandler.clear(level, pos);
        ArcaneAccess.removeLock(level, ArcaneAccess.lockOrigin(state, pos));
    }
}
