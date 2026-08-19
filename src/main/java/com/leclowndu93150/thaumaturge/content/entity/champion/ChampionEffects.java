package com.leclowndu93150.thaumaturge.content.entity.champion;

import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jspecify.annotations.Nullable;

final class ChampionEffects {
    private static final float PROC_CHANCE = 0.4F;
    private static final float WARP_CHANCE = 0.33F;
    private static final int WITHER_TICKS = 200;
    private static final int HUNGER_TICKS = 500;
    private static final int POISON_TICKS = 100;
    private static final int FIRE_SECONDS = 4;
    private static final float ARMORED_FACTOR = 19.0F / 25.0F;
    private static final int WARDED_INTERVAL_TICKS = 25;
    private static final int UNDYING_INTERVAL_TICKS = 20;

    private ChampionEffects() {}

    static float none(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        return amount;
    }

    static float spined(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        if (target != null && source != null && !source.is(DamageTypes.THORNS) && mob.level() instanceof ServerLevel server) {
            target.hurtServer(server, mob.damageSources().thorns(mob), 1 + mob.getRandom().nextInt(3));
            server.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.THORNS_HIT, SoundSource.HOSTILE, 0.5F, 1.0F);
        }
        return amount;
    }

    static float armored(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        if (source != null && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return amount * ARMORED_FACTOR;
        }
        return amount;
    }

    static float grim(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        if (target != null && mob.getRandom().nextFloat() < PROC_CHANCE) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_TICKS));
        }
        return amount;
    }

    static float warded(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        if (mob.invulnerableTime <= 0 && mob.tickCount % WARDED_INTERVAL_TICKS == 0) {
            int cap = (int) mob.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2;
            if (mob.getAbsorptionAmount() < cap) {
                mob.setAbsorptionAmount(mob.getAbsorptionAmount() + 1.0F);
            }
        }
        return amount;
    }

    static float warp(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        if (target instanceof ServerPlayer player && mob.getRandom().nextFloat() < WARP_CHANCE) {
            WarpHelper.addWarp(player, 1 + mob.getRandom().nextInt(3), WarpType.TEMPORARY);
        }
        return amount;
    }

    static float undying(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        if (mob.tickCount % UNDYING_INTERVAL_TICKS == 0) {
            mob.heal(1.0F);
        }
        return amount;
    }

    static float fiery(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        if (target != null && mob.getRandom().nextFloat() < PROC_CHANCE) {
            target.igniteForSeconds(FIRE_SECONDS);
        }
        return amount;
    }

    static float sickly(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        if (target != null && mob.getRandom().nextFloat() < PROC_CHANCE) {
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, HUNGER_TICKS));
        }
        return amount;
    }

    static float venomous(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        if (target != null && mob.getRandom().nextFloat() < PROC_CHANCE) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_TICKS));
        }
        return amount;
    }

    static float vampiric(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        mob.heal(Math.max(2.0F, amount / 2.0F));
        return amount;
    }

    static float infested(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        if (mob.getRandom().nextFloat() < PROC_CHANCE && mob.level() instanceof ServerLevel server) {
            EntityTaintCrawler crawler = TCEntities.TAINT_CRAWLER.get().create(server, EntitySpawnReason.REINFORCEMENT);
            if (crawler != null) {
                crawler.snapTo(mob.getX(), mob.getY() + mob.getBbHeight() / 2.0F, mob.getZ(), mob.getRandom().nextFloat() * 360.0F, 0.0F);
                server.addFreshEntity(crawler);
                mob.playSound(TCSounds.GORE.get(), 0.5F, 1.0F);
            }
        }
        return amount;
    }

    static float tainted(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount) {
        ChampionHelper.resetTaintedAI(mob);
        return amount;
    }
}
