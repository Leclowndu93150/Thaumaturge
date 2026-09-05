package com.leclowndu93150.thaumaturge.content.warding;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** A warded transparent block. TransparentBlock supplies seam-free same-block face culling. */
public final class BlockWardedGlass extends TransparentBlock {
    public static final MapCodec<BlockWardedGlass> CODEC = simpleCodec(BlockWardedGlass::new);

    public BlockWardedGlass(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<BlockWardedGlass> codec() {
        return CODEC;
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            net.minecraft.world.level.block.state.BlockState state,
            LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel server && placer instanceof Player player) {
            WardHandler.ward(server, pos, player.getUUID());
        }
    }
}
