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

    private WandChargingEvents() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity living = event.getEntity();
        if (!(living.level() instanceof ServerLevel level) || !event.isRecentlyHit()) {
            return;
        }
        AspectList aspects = EntityAspects.of(living);
        if (aspects.isEmpty()) {
            return;
        }
        Map<ResourceKey<IAspect>, Integer> primals = reduceToPrimals(aspects);
        RandomSource random = level.getRandom();
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : primals.entrySet()) {
            if (random.nextBoolean()) {
                level.addFreshEntity(new EntityAspectOrb(level, living.getX(), living.getY(), living.getZ(), entry.getKey(), 1 + random.nextInt(entry.getValue())));
            }
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

    public static Map<ResourceKey<IAspect>, Integer> reduceToPrimals(AspectList aspects) {
        Map<ResourceKey<IAspect>, Integer> out = new LinkedHashMap<>();
        for (AspectInstance instance : aspects.entries()) {
            reduce(instance.aspect(), instance.amount(), out);
        }
        return out;
    }

    private static void reduce(Holder<IAspect> aspect, int amount, Map<ResourceKey<IAspect>, Integer> out) {
        if (aspect.value().isPrimal()) {
            ResourceKey<IAspect> key = aspect.unwrapKey().orElseThrow();
            out.merge(key, amount, Integer::sum);
        } else {
            for (Holder<IAspect> component : aspect.value().components()) {
                reduce(component, amount, out);
            }
        }
    }
}
