package com.leclowndu93150.thaumaturge.client.champion;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.trait.MobTraitVisuals;
import com.leclowndu93150.thaumaturge.content.particle.CrackShardParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FlameFanParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FluxSwirlParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispFlameParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import com.leclowndu93150.thaumaturge.registry.TTParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ChampionTraitVisuals {
    private static final float GRIM_SHADE = 0.6F;

    private ChampionTraitVisuals() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ChampionTraitVisuals::register);
    }

    private static void register() {
        MobTraitVisuals.registerParticles(TTMobTraits.BOLD.getKey(), (mob, level, rand, x, y, z) -> {
            if (rand.nextBoolean()) {
                double sy = mob.getBoundingBox().minY + rand.nextFloat() * mob.getBbHeight() / 3.0F;
                addParticle(
                        level,
                        x,
                        sy,
                        z,
                        new SparkParticleOptions(
                                ARGB32.colorFromFloat(
                                        1.0F, 0.3F - rand.nextFloat() * 0.1F, 0.0F, 0.8F + rand.nextFloat() * 0.2F),
                                1.0F,
                                0.2F));
            }
        });
        MobTraitVisuals.registerParticles(TTMobTraits.SPINED.getKey(), (mob, level, rand, x, y, z) -> {
            if (rand.nextBoolean()) {
                addParticle(
                        level,
                        x,
                        y,
                        z,
                        new CrackShardParticleOptions(
                                ARGB32.colorFromFloat(
                                        1.0F,
                                        0.5F + rand.nextFloat() * 0.2F,
                                        0.1F + rand.nextFloat() * 0.2F,
                                        0.1F + rand.nextFloat() * 0.2F),
                                rand.nextInt(4),
                                1.2F + rand.nextFloat() * 0.3F,
                                3));
            }
        });
        MobTraitVisuals.registerParticles(TTMobTraits.ARMORED.getKey(), (mob, level, rand, x, y, z) -> {
            if (rand.nextInt(4) == 0) {
                addParticle(
                        level,
                        x,
                        y,
                        z,
                        new ShieldSparkParticleOptions(
                                ARGB32.colorFromFloat(1.0F, 0.9F, 0.9F, 0.9F + rand.nextFloat() * 0.1F),
                                0.7F,
                                0.6F + rand.nextFloat() * 0.2F,
                                5 + rand.nextInt(4),
                                0,
                                true));
            }
        });
        MobTraitVisuals.registerParticles(TTMobTraits.MIGHTY.getKey(), (mob, level, rand, x, y, z) -> {
            if (rand.nextFloat() <= 0.3F) {
                addParticle(
                        level,
                        x,
                        y,
                        z,
                        new CrackShardParticleOptions(
                                ARGB32.colorFromFloat(
                                        1.0F,
                                        0.8F + rand.nextFloat() * 0.2F,
                                        0.8F + rand.nextFloat() * 0.2F,
                                        0.8F + rand.nextFloat() * 0.2F),
                                rand.nextInt(4),
                                1.0F + rand.nextFloat() * 0.3F,
                                4 + rand.nextInt(3)));
            }
        });
        MobTraitVisuals.registerParticles(TTMobTraits.GRIM.getKey(), (mob, level, rand, x, y, z) -> {
            if (rand.nextBoolean()) {
                addParticle(
                        level,
                        x,
                        y,
                        z,
                        0.0,
                        -0.02,
                        0.0,
                        new FlameFanParticleOptions(0.6F + rand.nextFloat() * 0.4F, 0.0F, 0.8F));
            }
        });
        MobTraitVisuals.registerTint(
                TTMobTraits.GRIM.getKey(), ARGB32.colorFromFloat(1.0F, GRIM_SHADE, GRIM_SHADE, GRIM_SHADE));
        MobTraitVisuals.registerParticles(TTMobTraits.WARDED.getKey(), (mob, level, rand, x, y, z) -> {
            if (rand.nextBoolean()) {
                addParticle(
                        level,
                        x,
                        y,
                        z,
                        TTParticles.colorOf(
                                TTParticles.LEAF_MOTE,
                                0.5F + rand.nextFloat() * 0.1F,
                                0.5F + rand.nextFloat() * 0.1F,
                                0.5F + rand.nextFloat() * 0.1F));
            }
        });
        MobTraitVisuals.registerParticles(TTMobTraits.WARP.getKey(), (mob, level, rand, x, y, z) -> {
            if (rand.nextBoolean()) {
                addParticle(
                        level,
                        x,
                        y,
                        z,
                        new WispFlameParticleOptions(
                                ARGB32.colorFromFloat(
                                        1.0F, 0.8F + rand.nextFloat() * 0.2F, 0.0F, 0.9F + rand.nextFloat() * 0.1F),
                                0.7F,
                                0.6F + rand.nextFloat() * 0.4F,
                                0.6F + rand.nextFloat() * 0.4F,
                                0));
            }
        });
        MobTraitVisuals.registerParticles(TTMobTraits.UNDYING.getKey(), (mob, level, rand, x, y, z) -> {
            if (rand.nextBoolean()) {
                addParticle(
                        level,
                        x,
                        y,
                        z,
                        0.0,
                        0.03,
                        0.0,
                        new WispFlameParticleOptions(
                                ARGB32.colorFromFloat(
                                        1.0F,
                                        0.1F + rand.nextFloat() * 0.1F,
                                        0.8F + rand.nextFloat() * 0.2F,
                                        0.1F + rand.nextFloat() * 0.1F),
                                0.9F,
                                0.5F + rand.nextFloat() * 0.2F,
                                0.5F + rand.nextFloat() * 0.2F,
                                0));
            }
        });
        MobTraitVisuals.registerParticles(
                TTMobTraits.FIERY.getKey(),
                (mob, level, rand, x, y, z) -> addParticle(
                        level,
                        x,
                        y,
                        z,
                        0.0,
                        0.03,
                        0.0,
                        new FlameFanParticleOptions(0.7F + rand.nextFloat() * 0.2F, 0.0F, 0.7F)));
        MobTraitVisuals.registerParticles(TTMobTraits.SICKLY.getKey(), (mob, level, rand, x, y, z) -> {
            if (rand.nextBoolean()) {
                addParticle(
                        level,
                        x,
                        y,
                        z,
                        0.0,
                        -0.02,
                        0.0,
                        new FluxSwirlParticleOptions(
                                ARGB32.colorFromFloat(
                                        1.0F, 0.2F, 0.6F + rand.nextFloat() * 0.1F, 0.2F + rand.nextFloat() * 0.1F),
                                0.9F + rand.nextFloat() * 0.3F,
                                0.9F));
            }
        });
        MobTraitVisuals.registerParticles(TTMobTraits.VENOMOUS.getKey(), (mob, level, rand, x, y, z) -> {
            if (rand.nextBoolean()) {
                addParticle(
                        level,
                        x,
                        y,
                        z,
                        0.0,
                        0.02,
                        0.0,
                        TTParticles.colorOf(
                                TTParticles.GOO_DRIP,
                                0.2F,
                                0.6F + rand.nextFloat() * 0.1F,
                                0.2F + rand.nextFloat() * 0.1F));
            }
        });
        MobTraitVisuals.registerParticles(TTMobTraits.VAMPIRIC.getKey(), (mob, level, rand, x, y, z) -> {
            if (rand.nextFloat() <= 0.2F) {
                addParticle(
                        level,
                        x,
                        y,
                        z,
                        TTParticles.colorOf(TTParticles.GOO_DRIP, 0.9F + rand.nextFloat() * 0.1F, 0.0F, 0.0F));
            }
        });
        MobTraitVisuals.registerParticles(TTMobTraits.INFESTED.getKey(), (mob, level, rand, x, y, z) -> {
            if (rand.nextBoolean()) {
                level.addParticle(
                        TTParticles.TAINT_SPLOSION.get(),
                        mob.getX() + (rand.nextFloat() - 0.5) * mob.getBbWidth(),
                        (mob.getBoundingBox().minY + mob.getBoundingBox().maxY) / 2.0,
                        mob.getZ() + (rand.nextFloat() - 0.5) * mob.getBbWidth(),
                        0.0,
                        0.0,
                        0.0);
            }
        });
    }

    private static void addParticle(Level level, double x, double y, double z, ParticleOptions options) {
        level.addParticle(options, x, y, z, 0.0, 0.0, 0.0);
    }

    private static void addParticle(
            Level level, double x, double y, double z, double vx, double vy, double vz, ParticleOptions options) {
        level.addParticle(options, x, y, z, vx, vy, vz);
    }
}
