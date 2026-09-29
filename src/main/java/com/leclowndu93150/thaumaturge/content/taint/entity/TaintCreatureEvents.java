package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCreeper;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSplosion;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TaintCreatureEvents {
    private static final float BLAST_STRENGTH = 1.5F;
    private static final double POISON_RANGE = 6.0;
    private static final int FLUX_TAINT_TICKS = 100;
    private static final float SPLOSION_SPREAD = 5.0F;

    private TaintCreatureEvents() {}

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getExplosion().getDirectSourceEntity() instanceof EntityTaintCreeper creeper)) {
            return;
        }
        event.setCanceled(true);
        level.explode(
                null,
                level.damageSources().explosion(creeper, creeper),
                null,
                creeper.getX(),
                creeper.getY() + creeper.getBbHeight() / 2.0F,
                creeper.getZ(),
                BLAST_STRENGTH,
                false,
                Level.ExplosionInteraction.NONE);
        AABB area = new AABB(creeper.position(), creeper.position()).inflate(POISON_RANGE);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!(living instanceof ITaintedMob) && !living.getType().is(EntityTypeTags.UNDEAD)) {
                living.addEffect(
                        new MobEffectInstance(TCMobEffects.FLUX_TAINT, FLUX_TAINT_TICKS, 0, false, true, false));
            }
        }
        if (!ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            TaintSplosion.burstAtHeight(level, creeper.blockPosition(), level.getRandom(), SPLOSION_SPREAD);
        }
    }
}
