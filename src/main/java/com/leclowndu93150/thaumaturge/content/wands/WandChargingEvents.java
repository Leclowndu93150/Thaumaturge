package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class WandChargingEvents {
    private static final int PLANT_ORB_MAX_BONUS = 2;
    private static final int MAX_COMPONENT_DEPTH = 16;
    private static final double HALF = 0.5;

    private WandChargingEvents() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity dead = event.getEntity();
        if (!event.isRecentlyHit() || !(dead.level() instanceof ServerLevel level)) {
            return;
        }
        AspectList aspects = EntityAspects.of(dead);
        if (aspects.isEmpty()) {
            return;
        }
        RandomSource random = level.getRandom();
        double y = dead.getY() + dead.getBbHeight() * HALF;
        for (Map.Entry<ResourceKey<IAspect>, Integer> primal : reduceToPrimals(aspects).entrySet()) {
            int total = primal.getValue();
            if (total > 0 && random.nextBoolean()) {
                level.addFreshEntity(new EntityAspectOrb(level, dead.getX(), y, dead.getZ(), primal.getKey(), 1 + random.nextInt(total)));
            }
        }
    }

    public static Map<ResourceKey<IAspect>, Integer> reduceToPrimals(AspectList aspects) {
        Map<ResourceKey<IAspect>, Integer> totals = new LinkedHashMap<>();
        for (AspectInstance entry : aspects.entries()) {
            splitInto(totals, entry.aspect(), entry.amount(), 0);
        }
        return totals;
    }

    private static void splitInto(Map<ResourceKey<IAspect>, Integer> totals, Holder<IAspect> aspect, int amount, int depth) {
        if (aspect.value().isPrimal()) {
            aspect.unwrapKey().ifPresent(key -> totals.merge(key, amount, Integer::sum));
            return;
        }
        if (depth >= MAX_COMPONENT_DEPTH) {
            return;
        }
        for (Holder<IAspect> part : aspect.value().components()) {
            splitInto(totals, part, amount, depth + 1);
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player) || player.isCreative()) {
            return;
        }
        ServerLevel level = event.getLevel();
        if (!event.getState().is(TTBlockTags.MAGICAL_PLANTS)) {
            return;
        }
        ResourceKey<IAspect> aspect = plantAspect(event.getState().getBlock());
        if (aspect == null) {
            return;
        }
        BlockPos pos = event.getPos();
        RandomSource random = level.getRandom();
        level.addFreshEntity(new EntityAspectOrb(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, aspect, 1 + random.nextInt(PLANT_ORB_MAX_BONUS)));
    }

    private static ResourceKey<IAspect> plantAspect(Block block) {
        if (block == TTBlocks.PLANT_CINDERPEARL.get()) {
            return TTAspects.IGNIS;
        }
        if (block == TTBlocks.PLANT_SHIMMERLEAF.get()) {
            return TTAspects.ORDO;
        }
        if (block == TTBlocks.PLANT_VISHROOM.get()) {
            return TTAspects.PERDITIO;
        }
        return null;
    }

}
