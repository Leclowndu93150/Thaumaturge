package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityOwnedConstruct;
import com.leclowndu93150.thaumaturge.content.taint.effect.FluxTaintExposure;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintSplosion;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TaintCreatureEvents {
    private static final float BLAST_STRENGTH = 1.5F;
    private static final double POISON_RANGE = 6.0;
    private static final int BLAST_DURATION = 100;
    private static final float SPLOSION_SPREAD = 5.0F;
    private static final float INFECTION_HEALTH = 2.0F;
    private static final int BROOD_EXPERIENCE = 2;
    private static final double BODY_MIDDLE = 0.5;

    private TaintCreatureEvents() {}

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Creeper creeper = findBlastingCreeper(event.getExplosion());
        if (creeper == null) {
            return;
        }
        event.setCanceled(true);
        level.explode(
                creeper,
                creeper.getX(),
                creeper.getY(BODY_MIDDLE),
                creeper.getZ(),
                BLAST_STRENGTH,
                false,
                Level.ExplosionInteraction.NONE);
        AABB area = creeper.getBoundingBox().inflate(POISON_RANGE);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (living.distanceTo(creeper) <= POISON_RANGE && !FluxTaintExposure.isImmune(living)) {
                FluxTaintExposure.expose(living, BLAST_DURATION, false, true);
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
        if (victim.getHealth() < INFECTION_HEALTH
                && !victim.getType().is(EntityTypeTags.UNDEAD)
                && victim.isAlive()
                && !(victim instanceof EntityOwnedConstruct)
                && victim.hasEffect(TTMobEffects.FLUX_TAINT)
                && victim.getRandom().nextBoolean()) {
            TaintInfection.tryInfect(level, victim);
            return;
        }
        if (event.getAmount() > 0.0F
                && event.getSource().getEntity() instanceof LivingEntity attacker
                && MobTraits.isTainted(attacker)) {
            FluxTaintExposure.exposeQuietly(victim);
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
        LootTable table = level.getServer()
                .reloadableRegistries()
                .getLootTable(profile.lootTable().get());
        DamageSource source = event.getSource();
        LootParams.Builder params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity());
        if (event.isRecentlyHit() && entity.getKillCredit() instanceof Player player) {
            params = params.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                    .withLuck(player.getLuck());
        }
        table.getRandomItems(
                params.create(LootContextParamSets.ENTITY),
                entity.getLootTableSeed(),
                stack -> event.getDrops().add(dropOf(level, entity, stack)));
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getEntity().getData(TTAttachments.LEGACY_TAINT_SPIDERLING)) {
            event.setDroppedExperience(BROOD_EXPERIENCE);
            return;
        }
        if (MobTraits.has(event.getEntity(), TTMobTraits.TAINT_BROOD.getKey())) {
            event.setDroppedExperience(BROOD_EXPERIENCE);
        }
    }

    private static @Nullable Creeper findBlastingCreeper(Explosion explosion) {
        if (explosion.radius() == BLAST_STRENGTH || !(explosion.getDirectSourceEntity() instanceof Creeper creeper)) {
            return null;
        }
        return MobTraits.has(creeper, TTMobTraits.TAINT_BLAST.getKey()) ? creeper : null;
    }

    private static ItemEntity dropOf(ServerLevel level, LivingEntity source, ItemStack stack) {
        ItemEntity drop = new ItemEntity(level, source.getX(), source.getY(), source.getZ(), stack);
        drop.setDefaultPickUpDelay();
        return drop;
    }
}
