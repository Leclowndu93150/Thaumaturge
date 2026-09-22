package com.leclowndu93150.thaumaturge.content.warding;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class BlockArcanePressurePlate extends PressurePlateBlock {
    public static final MapCodec<BlockArcanePressurePlate> CODEC = simpleCodec(BlockArcanePressurePlate::new);
    public static final IntegerProperty MODE = IntegerProperty.create("mode", 0, 2);

    public BlockArcanePressurePlate(BlockBehaviour.Properties properties) {
        super(BlockSetType.OAK, properties);
        registerDefaultState(defaultBlockState().setValue(MODE, 0));
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<PressurePlateBlock> codec() {
        return (MapCodec<PressurePlateBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel server && placer instanceof Player player) {
            WardHandler.ward(server, pos, player.getUUID());
            ArcaneAccess.lock(server, pos, player.getUUID());
        }
    }

    @Override
    protected int getSignalStrength(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) return 0;
        int mode = level.getBlockState(pos).getValue(MODE);
        for (Entity entity : level.getEntities(null, TOUCH_AABB.move(pos))) {
            if (mode == 0
                    || (entity instanceof Player player && (mode == 1) == ArcaneAccess.canAccess(server, pos, player)))
                return 15;
        }
        return 0;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        if (!ArcaneAccess.canAccess(server, pos, player)) {
            return InteractionResult.FAIL;
        }
        int mode = (state.getValue(MODE) + 1) % 3;
        level.setBlock(pos, state.setValue(MODE, mode), UPDATE_CLIENTS);
        player.sendSystemMessage(Component.translatable("message.thaumaturge.arcane_pressure_plate_mode." + mode));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(MODE);
        super.createBlockStateDefinition(builder);
    }
}
