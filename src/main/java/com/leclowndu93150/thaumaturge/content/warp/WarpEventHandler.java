package com.leclowndu93150.thaumaturge.content.warp;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.warp.ItemWarp;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class WarpEventHandler {
    private static final int WARP_CHECK_INTERVAL = 2000;
    private static final int DEATH_GAZE_INTERVAL = 20;
    private static final int HUNGER_CURE_DURATION_STEP = 600;

    private static final Set<Identifier> MILK_PROOF_EFFECTS = Set.of(TTIds.rl("vis_exhaust"), TTIds.rl("infectious_vis_exhaust"), TTIds.rl("thaumarhia"), TTIds.rl("unnatural_hunger"),
            TTIds.rl("sun_scorned"), TTIds.rl("death_gaze"), TTIds.rl("flux_taint"));

    private WarpEventHandler() {}

    @SubscribeEvent
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        if (!entity.isUsingItem() || !entity.getUseItem().is(Items.MILK_BUCKET)) {
            return;
        }
        Identifier id = event.getEffect().unwrapKey().map(ResourceKey::identifier).orElse(null);
        if (id != null && MILK_PROOF_EFFECTS.contains(id)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!ThaumaturgeCommonConfig.WUSS_MODE.get() && player.tickCount > 0 && player.tickCount % WARP_CHECK_INTERVAL == 0 && !player.hasEffect(TTMobEffects.WARP_WARD)) {
            WarpEvents.checkWarpEvent(player);
        }
        if (player.tickCount % DEATH_GAZE_INTERVAL == 0 && player.hasEffect(TTMobEffects.DEATH_GAZE)) {
            WarpEvents.checkDeathGaze(player);
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemWarp warp = event.getCrafting().getItem().builtInRegistryHolder().getData(TTDataMaps.ITEM_WARP);
        if (warp != null) {
            WarpManager.addWarp(player, warp.amount(), WarpType.NORMAL);
        }
    }

    @SubscribeEvent
    public static void onFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        ItemStack used = event.getItem();
        if (used.is(TTItems.BRAIN.get()) && !ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            if (player.getRandom().nextFloat() < 0.1F) {
                WarpManager.addWarp(player, 1, WarpType.NORMAL);
            } else {
                WarpManager.addWarp(player, 1 + player.getRandom().nextInt(3), WarpType.TEMPORARY);
            }
        }
        MobEffectInstance hunger = player.getEffect(TTMobEffects.UNNATURAL_HUNGER);
        if (hunger == null || used.get(DataComponents.CONSUMABLE) == null) {
            return;
        }
        if (used.is(Items.ROTTEN_FLESH) || used.is(TTItems.BRAIN.get())) {
            player.removeEffect(TTMobEffects.UNNATURAL_HUNGER);
            int amplifier = hunger.getAmplifier() - 1;
            int duration = hunger.getDuration() - HUNGER_CURE_DURATION_STEP;
            if (duration > 0 && amplifier >= 0) {
                player.addEffect(new MobEffectInstance(TTMobEffects.UNNATURAL_HUNGER, duration, amplifier, true, true));
            }
            WarpManager.sendActionBar(player, "warp.thaumaturge.text.hunger.2");
        } else {
            WarpManager.sendActionBar(player, "warp.thaumaturge.text.hunger.1");
        }
    }
}
