package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.phys.Vec3;

public final class ElementalSpearItem extends Item {
    private static final double MIN_LAUNCH_SPEED = 4.6;
    private static final double LAUNCH_PER_SPEED = 0.05;
    private static final double MAX_LAUNCH = 0.7;
    private static final int GUST_COLOR = 0xDDDDDD;
    private static final int GUST_PUFFS = 6;
    private static final float GUST_SCALE = 1.5F;
    private static final double GUST_SPREAD = 0.15;
    private static final float GUST_VOLUME = 0.5F;
    private static final float GUST_PITCH = 1.2F;
    private static final float GUST_PITCH_SPREAD = 0.2F;

    public ElementalSpearItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity mob, LivingEntity attacker) {
        if (attacker.isShiftKeyDown() || !(attacker.level() instanceof ServerLevel level)) {
            return;
        }
        double speed = attacker.getLookAngle().dot(KineticWeapon.getMotion(attacker));
        if (speed < MIN_LAUNCH_SPEED) {
            return;
        }
        double launch = Math.min(speed * LAUNCH_PER_SPEED, MAX_LAUNCH);
        mob.push(0.0, launch, 0.0);
        mob.hurtMarked = true;
        Vec3 center = mob.position().add(0.0, mob.getBbHeight() / 2.0, 0.0);
        for (int i = 0; i < GUST_PUFFS; i++) {
            Effects.vent(level, center).motion((level.getRandom().nextDouble() - 0.5) * GUST_SPREAD, launch * GUST_SPREAD, (level.getRandom().nextDouble() - 0.5) * GUST_SPREAD).color(GUST_COLOR)
                    .scale(GUST_SCALE).send();
        }
        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), TCSounds.WIND.get(), SoundSource.PLAYERS, GUST_VOLUME, GUST_PITCH + level.getRandom().nextFloat() * GUST_PITCH_SPREAD);
    }
}
