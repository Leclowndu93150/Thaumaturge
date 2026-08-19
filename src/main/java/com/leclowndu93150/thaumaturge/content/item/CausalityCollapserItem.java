package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.content.entity.EntityCausalityCollapser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CausalityCollapserItem extends Item {
    private static final float SHOOT_POWER = 0.8F;
    private static final float THROW_VOLUME = 0.3F;

    public CausalityCollapserItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EGG_THROW, SoundSource.NEUTRAL, THROW_VOLUME, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (level instanceof ServerLevel serverLevel) {
            Projectile.spawnProjectileFromRotation(EntityCausalityCollapser::new, serverLevel, stack, player, 0.0F, SHOOT_POWER, 2.0F);
        }
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
