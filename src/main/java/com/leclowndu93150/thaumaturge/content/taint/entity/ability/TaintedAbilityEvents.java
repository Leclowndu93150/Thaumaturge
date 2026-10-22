package com.leclowndu93150.thaumaturge.content.taint.entity.ability;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSwarm;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitEngine;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintedTrait;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TaintedAbilityEvents {
    private static final float ARROW_FLUX = 1.0F;
    private static final double BODY_MIDDLE = 0.5;

    private TaintedAbilityEvents() {}

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof LivingEntity living) || !MobTraits.has(living, MobTraits.TAINTED)) {
            return;
        }
        InteractionResult harvest =
                TaintedHarvest.interact(event.getEntity(), event.getHand(), event.getItemStack(), living);
        if (harvest != null) {
            event.setCanceled(true);
            event.setCancellationResult(harvest);
            return;
        }
        if (MobTraitEngine.suppressesNativeAi(living) && !event.getItemStack().is(Tags.Items.TOOLS_SHEAR)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity living = event.getEntity();
        if (living.level() instanceof ServerLevel level
                && level.getDifficulty() != Difficulty.PEACEFUL
                && MobTraits.has(living, TTMobTraits.SWARM_BURST.getKey())) {
            EntityTaintSwarm.release(level, living.getX(), living.getY(BODY_MIDDLE), living.getZ());
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK
                || !(event.getProjectile() instanceof AbstractArrow arrow)
                || !(arrow.level() instanceof ServerLevel level)
                || !(arrow.getOwner() instanceof LivingEntity shooter)
                || !MobTraits.has(shooter, TTMobTraits.TAINT_ARROWS.getKey())) {
            return;
        }
        TaintHelper.attemptFibreGrowth(level, hit.getBlockPos().relative(hit.getDirection()), true);
        AuraHelper.polluteAura(level, hit.getBlockPos(), ARROW_FLUX, false);
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Entity entity = event.getEntity();
        if (event.loadedFromDisk()) {
            if (entity instanceof LivingEntity living && MobTraits.has(living, MobTraits.TAINTED)) {
                TaintedTrait.grantProfileTraits(living);
            }
            return;
        }
        if (entity instanceof ThrownPotion potion
                && potion.getOwner() instanceof LivingEntity thrower
                && MobTraits.has(thrower, TTMobTraits.TAINT_BREWING.getKey())
                && TaintBrewing.replacesThrow(level, potion, thrower)) {
            event.setCanceled(true);
        }
    }
}
