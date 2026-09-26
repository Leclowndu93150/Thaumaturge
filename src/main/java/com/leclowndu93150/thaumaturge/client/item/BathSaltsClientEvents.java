package com.leclowndu93150.thaumaturge.client.item;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class BathSaltsClientEvents {
    private static final int BUBBLE_INTERVAL = 2;

    private BathSaltsClientEvents() {}

    @SubscribeEvent
    public static void onItemTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)
                || !itemEntity.getItem().is(TCItems.BATH_SALTS.get())
                || !itemEntity.isInWater()
                || itemEntity.tickCount % BUBBLE_INTERVAL != 0) {
            return;
        }

        RandomSource random = itemEntity.level().getRandom();
        for (int i = 0, count = 2 + random.nextInt(3); i < count; i++) {
            itemEntity
                    .level()
                    .addParticle(
                            new BubbleParticleOptions(
                                    0xFFE8E8FF,
                                    1.0F,
                                    1.5F + random.nextFloat() * 0.5F,
                                    18 + random.nextInt(10),
                                    -0.025F,
                                    false),
                            itemEntity.getX() + (random.nextDouble() - 0.5) * 0.3,
                            itemEntity.getY() + random.nextDouble() * itemEntity.getBbHeight(),
                            itemEntity.getZ() + (random.nextDouble() - 0.5) * 0.3,
                            0.0,
                            0.0,
                            0.0);
        }
    }
}
