package com.leclowndu93150.thaumaturge.content.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ThrowableItem extends Item {
    private static final float PITCH_CENTER = 0.5F;
    private static final float PITCH_SPREAD = 0.4F;

    private final Projectile.ProjectileFactory<?> projectile;
    private final ThrowProfile profile;

    public ThrowableItem(Item.Properties properties, Projectile.ProjectileFactory<?> projectile, ThrowProfile profile) {
        super(properties);
        this.projectile = projectile;
        this.profile = profile;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        RandomSource random = level.getRandom();
        float pitch = PITCH_CENTER + (random.nextFloat() - random.nextFloat()) * PITCH_SPREAD * PITCH_CENTER;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EGG_THROW, SoundSource.NEUTRAL, profile.volume(), pitch);
        if (level instanceof ServerLevel server) {
            Projectile.spawnProjectileFromRotation(projectile, server, held, player, profile.pitchOffset(), profile.velocity(), profile.inaccuracy());
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        held.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
