package com.leclowndu93150.thaumaturge.content.entity.boss;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

final class BossRage {
    private static final float THRESHOLD = 35.0F;
    private static final int RAGE_TICKS = 200;
    private static final float REGEN_DIVISOR = 15.0F;
    private static final float STRENGTH_DIVISOR = 10.0F;
    private static final float HASTE_DIVISOR = 40.0F;
    private static final int PARTICLE_CHANCE = 15;
    private static final double PARTICLE_RISE = 0.5;
    private static final double PARTICLE_DRIFT = 0.02;

    private final LivingEntity boss;
    private final EntityDataAccessor<Integer> data;

    BossRage(LivingEntity boss, EntityDataAccessor<Integer> data) {
        this.boss = boss;
        this.data = data;
    }

    int anger() {
        return boss.getEntityData().get(data);
    }

    void setAnger(int anger) {
        boss.getEntityData().set(data, anger);
    }

    void tick() {
        int anger = anger();
        if (anger <= 0) {
            return;
        }
        if (!boss.level().isClientSide()) {
            setAnger(anger - 1);
            return;
        }
        RandomSource random = boss.getRandom();
        if (random.nextInt(PARTICLE_CHANCE) == 0) {
            float width = boss.getBbWidth();
            boss.level()
                    .addParticle(
                            ParticleTypes.ANGRY_VILLAGER,
                            boss.getX() + random.nextFloat() * width - width / 2.0,
                            boss.getBoundingBox().minY + boss.getBbHeight() + random.nextFloat() * PARTICLE_RISE,
                            boss.getZ() + random.nextFloat() * width - width / 2.0,
                            random.nextGaussian() * PARTICLE_DRIFT,
                            random.nextGaussian() * PARTICLE_DRIFT,
                            random.nextGaussian() * PARTICLE_DRIFT);
        }
    }

    float absorb(DamageSource source, float damage) {
        if (damage <= THRESHOLD || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return damage;
        }
        if (anger() == 0) {
            boss.addEffect(new MobEffectInstance(MobEffects.REGENERATION, RAGE_TICKS, (int) (damage / REGEN_DIVISOR)));
            boss.addEffect(
                    new MobEffectInstance(MobEffects.DAMAGE_BOOST, RAGE_TICKS, (int) (damage / STRENGTH_DIVISOR)));
            boss.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, RAGE_TICKS, (int) (damage / HASTE_DIVISOR)));
            setAnger(RAGE_TICKS);
            if (source.getEntity() instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.translatable("message.thaumaturge.boss.enraged", boss.getDisplayName())));
            }
        }
        return THRESHOLD;
    }
}
