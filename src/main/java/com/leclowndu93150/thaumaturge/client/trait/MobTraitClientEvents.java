package com.leclowndu93150.thaumaturge.client.trait;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class MobTraitClientEvents {
    private MobTraitClientEvents() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof LivingEntity mob)
                || !mob.isAlive()) {
            return;
        }
        for (Holder<MobTrait> trait : MobTraits.traits(mob)) {
            MobTraitParticles particles = MobTraitVisuals.particles(trait);
            if (particles != null) {
                Level level = mob.level();
                RandomSource random = level.getRandom();
                AABB box = mob.getBoundingBox();
                particles.spawn(
                        mob,
                        level,
                        random,
                        box.minX + random.nextFloat() * mob.getBbWidth(),
                        box.minY + random.nextFloat() * mob.getBbHeight(),
                        box.minZ + random.nextFloat() * mob.getBbWidth());
            }
        }
    }
}
