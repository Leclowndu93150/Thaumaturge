package com.leclowndu93150.thaumaturge.content.equipment.bauble;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.mixin.world.entity.ExperienceOrbAccessor;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class BaubleEvents {
    private static final byte TOTEM_EVENT = 35;
    private static final int UNDYING_REGEN_TICKS = 900;
    private static final int UNDYING_ABSORPTION_TICKS = 100;
    private static final int UNDYING_EFFECT_AMPLIFIER = 1;
    private static final double THEORY_CHANCE_PER_XP = 0.05;
    private static final double OBSERVATION_CHANCE_PER_XP = 0.2;

    private static final long CLOUD_JUMP_GRACE_WINDOW_TICKS = 100L;
    private static final double CLOUD_JUMP_GRACE_DISTANCE = 16.0;

    private BaubleEvents() {}

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !ModList.get().isLoaded(TCIds.CURIOS) || !ThaumaturgeCuriosCompat.isCurioEquipped(player, TCItems.CLOUD_RING.get())) {
            return;
        }
        long lastJump = player.getData(TCAttachments.CLOUD_JUMP_TIME);
        if (lastJump == 0L || player.level().getGameTime() - lastJump > CLOUD_JUMP_GRACE_WINDOW_TICKS) {
            return;
        }
        player.setData(TCAttachments.CLOUD_JUMP_TIME, 0L);
        event.setDistance(Math.max(0.0, event.getDistance() - CLOUD_JUMP_GRACE_DISTANCE));
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !ModList.get().isLoaded(TCIds.CURIOS)) {
            return;
        }
        ItemStack charm = ThaumaturgeCuriosCompat.extractCurio(player, TCItems.CHARM_UNDYING.get());
        if (charm.isEmpty()) {
            return;
        }
        player.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
        CriteriaTriggers.USED_TOTEM.trigger(player, charm);
        player.setHealth(1.0F);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, UNDYING_REGEN_TICKS, UNDYING_EFFECT_AMPLIFIER));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, UNDYING_ABSORPTION_TICKS, UNDYING_EFFECT_AMPLIFIER));
        player.level().broadcastEntityEvent(player, TOTEM_EVENT);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPickupXp(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer) || event.getOrb().getValue() <= 1 || !ModList.get().isLoaded(TCIds.CURIOS)
                || !ThaumaturgeCuriosCompat.isCurioEquipped(player, TCItems.CURIOSITY_BAND.get())) {
            return;
        }
        int drained = event.getOrb().getValue() / 2;
        ((ExperienceOrbAccessor) event.getOrb()).thaumaturge$setValue(event.getOrb().getValue() - drained);
        float roll = player.getRandom().nextFloat();
        if (roll < THEORY_CHANCE_PER_XP * drained) {
            ResearchGrants.grantConvertedKnowledge(serverPlayer, KnowledgeType.THEORY, 1);
        } else if (roll < OBSERVATION_CHANCE_PER_XP * drained) {
            ResearchGrants.grantConvertedKnowledge(serverPlayer, KnowledgeType.OBSERVATION, 1);
        }
    }
}
