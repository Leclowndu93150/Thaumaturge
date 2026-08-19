package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TCSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockRedstoneRelay extends DiodeBlock implements EntityBlock {
    public static final MapCodec<BlockRedstoneRelay> CODEC = simpleCodec(BlockRedstoneRelay::new);

    private static final int DELAY_TICKS = 2;

    public BlockRedstoneRelay(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    public MapCodec<BlockRedstoneRelay> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    protected int getDelay(BlockState state) {
        return DELAY_TICKS;
    }

    @Override
    protected boolean shouldTurnOn(Level level, BlockPos pos, BlockState state) {
        int threshold = level.getBlockEntity(pos) instanceof BlockEntityRedstoneRelay relay ? relay.getIn() : 1;
        return this.getInputSignal(level, pos, state) >= threshold;
    }

    @Override
    protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) {
        return level.getBlockEntity(pos) instanceof BlockEntityRedstoneRelay relay ? relay.getOut() : 15;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.mayBuild()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityRedstoneRelay relay) {
            Vec3 local = hitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
            Direction facing = state.getValue(FACING);
            double along = switch (facing) {
                case NORTH -> 1.0 - local.z;
                case SOUTH -> local.z;
                case WEST -> 1.0 - local.x;
                default -> local.x;
            };
            if (along < 0.5) {
                relay.increaseOut();
            } else {
                relay.increaseIn();
            }
            level.playSound(null, pos, TCSounds.KEY.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            this.checkTickOnNeighbor(level, pos, state);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityRedstoneRelay(pos, state);
    }
}
