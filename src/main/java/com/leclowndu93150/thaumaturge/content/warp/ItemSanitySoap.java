package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.api.warp.IPlayerWarp;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

public class ItemSanitySoap extends Item {
    private static final int USE_DURATION_TICKS = 100;
    private static final int FINISH_THRESHOLD_TICKS = 95;
    private static final int SCRUB_BUBBLES = 10;
    private static final int FINISH_BUBBLES = 40;
    private static final float SCRUB_SOUND_CHANCE = 0.2F;

    public ItemSanitySoap(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (getUseDuration(stack, entity) - remainingTicks > FINISH_THRESHOLD_TICKS) {
            entity.stopUsingItem();
        }
        if (level.isClientSide()) {
            if (level.getRandom().nextFloat() < SCRUB_SOUND_CHANCE) {
                level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CHORUS_FLOWER_DEATH, SoundSource.PLAYERS, 0.1F, 1.5F + level.getRandom().nextFloat() * 0.2F, false);
            }
            spawnBubbles(level, entity, SCRUB_BUBBLES, 1.0F);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int used = getUseDuration(stack, entity) - timeLeft;
        if (used <= FINISH_THRESHOLD_TICKS || !(entity instanceof Player)) {
            return false;
        }
        stack.shrink(1);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            IPlayerWarp warp = WarpHelper.getWarp(player);
            int amount = 1;
            if (player.hasEffect(TCMobEffects.WARP_WARD)) {
                amount++;
            }
            if (level.getBlockState(player.blockPosition()).is(TCBlocks.PURIFYING_FLUID.get())) {
                amount++;
            }
            if (warp.get(WarpType.NORMAL) > 0) {
                WarpHelper.addWarp(player, -amount, WarpType.NORMAL);
            }
            if (warp.get(WarpType.TEMPORARY) > 0) {
                WarpHelper.addWarp(player, -warp.get(WarpType.TEMPORARY), WarpType.TEMPORARY);
            }
        } else if (level.isClientSide()) {
            level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), TCSounds.CRAFTSTART.get(), SoundSource.PLAYERS, 0.25F, 1.0F, false);
            spawnBubbles(level, entity, FINISH_BUBBLES, 1.5F);
        }
        return true;
    }

    private static void spawnBubbles(Level level, LivingEntity entity, int count, float spread) {
        for (int a = 0; a < count; a++) {
            level.addParticle(ParticleTypes.BUBBLE_POP, entity.getX() - 0.5 + level.getRandom().nextFloat() * spread,
                    entity.getBoundingBox().minY + level.getRandom().nextFloat() * entity.getBbHeight(), entity.getZ() - 0.5 + level.getRandom().nextFloat() * spread, 0.0, 0.02, 0.0);
        }
    }
}
