package com.leclowndu93150.thaumaturge.content.warding;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

public final class BlockArcaneDoor extends DoorBlock {
    public static final MapCodec<BlockArcaneDoor> CODEC = simpleCodec(BlockArcaneDoor::new);

    public BlockArcaneDoor(BlockBehaviour.Properties properties) {
        super(BlockSetType.OAK, properties);
    }

    @Override
    public MapCodec<BlockArcaneDoor> codec() {
        return CODEC;
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
        BlockPos base = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        if (level instanceof ServerLevel server && !ArcaneAccess.canAccess(server, base, player)) {
            return InteractionResult.FAIL;
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }
}
