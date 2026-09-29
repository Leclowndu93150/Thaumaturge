package com.leclowndu93150.thaumaturge.content.warding;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class BlockArcaneDoor extends DoorBlock {
    public static final MapCodec<BlockArcaneDoor> CODEC = simpleCodec(BlockArcaneDoor::new);
    private static final BlockSetType ARCANE = new BlockSetType(
            "thaumaturge_arcane",
            false,
            false,
            true,
            BlockSetType.PressurePlateSensitivity.EVERYTHING,
            SoundType.WOOD,
            SoundEvents.WOODEN_DOOR_CLOSE,
            SoundEvents.WOODEN_DOOR_OPEN,
            SoundEvents.WOODEN_TRAPDOOR_CLOSE,
            SoundEvents.WOODEN_TRAPDOOR_OPEN,
            SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_OFF,
            SoundEvents.WOODEN_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.WOODEN_BUTTON_CLICK_OFF,
            SoundEvents.WOODEN_BUTTON_CLICK_ON);

    public BlockArcaneDoor(BlockBehaviour.Properties properties) {
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel server && placer instanceof Player player) {
            WardHandler.ward(server, pos, player.getUUID());
            WardHandler.ward(server, pos.above(), player.getUUID());
            ArcaneAccess.lock(server, pos, player.getUUID());
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        if (!ArcaneAccess.canAccess(server, lockOrigin(state, pos), player)) {
            return InteractionResult.FAIL;
        }
        setOpen(null, level, state, pos, !isOpen(state));
        return InteractionResult.CONSUME;
    }

    @Override
    protected void neighborChanged(
            BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!(level instanceof ServerLevel server) || defaultBlockState().is(block)) {
            return;
        }
        BlockPos origin = lockOrigin(state, pos);
        boolean signal = (level.hasNeighborSignal(origin) || level.hasNeighborSignal(origin.above()))
                && hasLinkedPlate(server, origin);
        if (signal != state.getValue(POWERED)) {
            BlockState powered = state.setValue(POWERED, signal);
            level.setBlock(pos, powered, Block.UPDATE_CLIENTS);
            setOpen(null, level, powered, pos, signal);
        }
    }

    private static BlockPos lockOrigin(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    private static boolean hasLinkedPlate(ServerLevel level, BlockPos origin) {
        for (int height = 0; height <= 1; height++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos platePos = origin.above(height).relative(direction);
                BlockState plate = level.getBlockState(platePos);
                if (plate.is(TCBlocks.ARCANE_PRESSURE_PLATE)
                        && plate.getValue(POWERED)
                        && ArcaneAccess.sharesAccess(level, platePos, origin)) {
                    return true;
                }
            }
        }
        return false;
    }
}
