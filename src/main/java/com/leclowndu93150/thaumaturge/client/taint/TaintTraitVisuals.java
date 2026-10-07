package com.leclowndu93150.thaumaturge.client.taint;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.client.trait.MobTraitVisuals;
import com.leclowndu93150.thaumaturge.content.particle.FluxSwirlParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TaintTraitVisuals {
    private static final float ALPHA = 0.25F;
    private static final float RED_BASE = 0.1F;
    private static final float RED_SPREAD = 0.2F;
    private static final float BLUE_BASE = 0.1F;
    private static final float BLUE_SPREAD = 0.1F;
    private static final float SCALE_BASE = 2.0F;
    private static final float LIFE_SCALE = 2.0F;
    private static final double DRIFT = -0.01;

    private TaintTraitVisuals() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(TaintTraitVisuals::register);
    }

    private static void register() {
        MobTraitVisuals.registerParticles(MobTraits.TAINTED, TaintTraitVisuals::fluxSwirl);
        MobTraitVisuals.registerParticles(TTMobTraits.TAINT_BROOD.getKey(), TaintTraitVisuals::fluxSwirl);
    }

    private static void fluxSwirl(LivingEntity mob, Level level, RandomSource random, double x, double y, double z) {
        level.addParticle(new FluxSwirlParticleOptions(ARGB.colorFromFloat(ALPHA, RED_BASE + random.nextFloat() * RED_SPREAD, 0.0F, BLUE_BASE + random.nextFloat() * BLUE_SPREAD),
                SCALE_BASE + random.nextFloat(), LIFE_SCALE), x, y, z, 0.0, DRIFT, 0.0);
    }
}
