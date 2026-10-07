package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityOwnedConstruct;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitEngine;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSplosion;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TaintCreatureEvents {
    private static final float BLAST_STRENGTH = 1.5F;
    private static final double POISON_RANGE = 6.0;
    private static final int FLUX_TAINT_TICKS = 100;
    private static final float SPLOSION_SPREAD = 5.0F;
    private static final float INFECTION_HEALTH = 2.0F;
    private static final int TAINTED_ATTACK_FLUX_TAINT_TICKS = 200;
    private static final int BROOD_EXPERIENCE = 2;

    private TaintCreatureEvents() {}

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getExplosion().getDirectSourceEntity() instanceof Creeper creeper)
                || !MobTraits.has(creeper, TTMobTraits.TAINT_BLAST.getKey())) {
            return;
        }
        event.setCanceled(true);
        level.explode(null, level.damageSources().explosion(creeper, creeper), null, creeper.getX(), creeper.getY() + creeper.getBbHeight() / 2.0F, creeper.getZ(), BLAST_STRENGTH, false,
                Level.ExplosionInteraction.NONE);
        AABB area = new AABB(creeper.position(), creeper.position()).inflate(POISON_RANGE);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!MobTraits.isTainted(living) && !living.is(EntityTypeTags.UNDEAD)) {
                living.addEffect(new MobEffectInstance(TTMobEffects.FLUX_TAINT, FLUX_TAINT_TICKS, 0, false, true, false));
            }
        }
        if (!ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            TaintSplosion.burstAtHeight(level, creeper.blockPosition(), level.getRandom(), SPLOSION_SPREAD);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        if (victim.getHealth() < INFECTION_HEALTH && !victim.isInvertedHealAndHarm() && victim.isAlive() && !(victim instanceof EntityOwnedConstruct) && victim.hasEffect(TTMobEffects.FLUX_TAINT)
                && victim.getRandom().nextBoolean()) {
            TaintInfection.tryInfect(level, victim);
            return;
        }
        if (event.getAmount() > 0.0F && event.getSource().getEntity() instanceof LivingEntity attacker && MobTraits.isTainted(attacker)) {
            victim.addEffect(new MobEffectInstance(TTMobEffects.FLUX_TAINT, TAINTED_ATTACK_FLUX_TAINT_TICKS, 0, true, false, false));
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof LivingEntity target && MobTraitEngine.suppressesNativeAi(target) && MobTraits.has(target, MobTraits.TAINTED)
                && !event.getItemStack().is(Tags.Items.TOOLS_SHEAR)) {
            event.setCancellationResult(InteractionResult.PASS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (MobTraits.has(entity, TTMobTraits.TAINT_BROOD.getKey())) {
            event.setCanceled(true);
            return;
        }
        if (!(entity.level() instanceof ServerLevel level) || !MobTraits.has(entity, MobTraits.TAINTED)) {
            return;
        }
        TaintedProfile profile = TaintedProfile.of(entity.getType());
        if (profile == null || profile.lootTable().isEmpty()) {
            return;
        }
        event.getDrops().clear();
        entity.dropFromLootTable(level, event.getSource(), event.isRecentlyHit(), profile.lootTable().get(),
                stack -> event.getDrops().add(new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack)));
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (MobTraits.has(event.getEntity(), TTMobTraits.TAINT_BROOD.getKey())) {
            event.setDroppedExperience(BROOD_EXPERIENCE);
        }
    }
}
