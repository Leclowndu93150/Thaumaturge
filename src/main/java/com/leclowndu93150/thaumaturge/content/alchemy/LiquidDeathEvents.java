package com.leclowndu93150.thaumaturge.content.alchemy;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageTypes;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class LiquidDeathEvents {
    private static final int ASPECTS_PER_EXTRA_CRYSTAL = 10;

    private LiquidDeathEvents() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || !event.getSource().is(TTDamageTypes.DISSOLVE)) {
            return;
        }
        AspectList aspects = EntityAspects.of(entity);
        if (aspects.isEmpty()) {
            return;
        }
        List<AspectInstance> entries = aspects.entries();
        int count = 1 + entity.getRandom().nextInt(1 + aspects.totalAmount() / ASPECTS_PER_EXTRA_CRYSTAL);
        for (int i = 0; i < count; i++) {
            AspectInstance aspect = entries.get(entity.getRandom().nextInt(entries.size()));
            event.getDrops().add(new ItemEntity(level, entity.getX(), entity.getEyeY(), entity.getZ(), EssentiaCrystalFactory.of(aspect.aspect())));
        }
    }
}
