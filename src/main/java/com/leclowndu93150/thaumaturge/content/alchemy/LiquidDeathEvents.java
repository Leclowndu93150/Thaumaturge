package com.leclowndu93150.thaumaturge.content.alchemy;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.damagesource.TCDamageTypes;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class LiquidDeathEvents {
    private LiquidDeathEvents() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || !event.getSource().is(TCDamageTypes.DISSOLVE)) {
            return;
        }

        AspectList aspects = EntityAspects.of(entity);
        if (aspects.isEmpty()) {
            return;
        }

        double b = ThaumaturgeServerConfig.LD_DROP_RATE_BOUND_1.get();
        double b2 = ThaumaturgeServerConfig.LD_DROP_RATE_BOUND_2.get();
        double min = Math.min(b, b2);
        double max = Math.max(b, b2);
        RandomSource random = entity.getRandom();
        for (AspectInstance instance : aspects.entries()) {
            int count = Mth.floor(Mth.nextDouble(random, min, max) * instance.amount());
            if (count > 0) {
				// Drop at ~chest height
                event.getDrops()
                        .add(new ItemEntity(
                                level,
                                entity.getX(),
                                entity.getY() + entity.getBbHeight() / 2.0,
                                entity.getZ(),
                                EssentiaCrystalFactory.of(instance.aspect(), count)));
            }
        }
    }
}
