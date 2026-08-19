package com.leclowndu93150.thaumaturge.client.champion;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionModifier;
import com.leclowndu93150.thaumaturge.content.particle.CrackShardParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FlameFanParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.FluxSwirlParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispFlameParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ChampionClientFX {

    private ChampionClientFX() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide() || !(event.getEntity() instanceof PathfinderMob mob) || !mob.isAlive()) {
            return;
        }
        int type = ChampionHelper.championType(mob);
        if (type < 0 || type >= ChampionModifier.MODS.size()) {
            return;
        }
        showFX(mob, type);
    }

    private static void showFX(LivingEntity mob, int type) {
        Level level = mob.level();
        RandomSource rand = level.getRandom();
        double x = mob.getBoundingBox().minX + rand.nextFloat() * mob.getBbWidth();
        double y = mob.getBoundingBox().minY + rand.nextFloat() * mob.getBbHeight();
        double z = mob.getBoundingBox().minZ + rand.nextFloat() * mob.getBbWidth();
        switch (type) {
            case ChampionModifier.BOLD -> {
                if (rand.nextBoolean()) {
                    double sy = mob.getBoundingBox().minY + rand.nextFloat() * mob.getBbHeight() / 3.0F;
                    addParticle(level, x, sy, z, new SparkParticleOptions(ARGB.colorFromFloat(1.0F, 0.3F - rand.nextFloat() * 0.1F, 0.0F, 0.8F + rand.nextFloat() * 0.2F), 1.0F, 0.2F));
                }
            }
            case ChampionModifier.SPINED -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, new CrackShardParticleOptions(ARGB.colorFromFloat(1.0F, 0.5F + rand.nextFloat() * 0.2F, 0.1F + rand.nextFloat() * 0.2F, 0.1F + rand.nextFloat() * 0.2F),
                            rand.nextInt(4), 1.2F + rand.nextFloat() * 0.3F, 3));
                }
            }
            case ChampionModifier.ARMORED -> {
                if (rand.nextInt(4) == 0) {
                    addParticle(level, x, y, z,
                            new ShieldSparkParticleOptions(ARGB.colorFromFloat(1.0F, 0.9F, 0.9F, 0.9F + rand.nextFloat() * 0.1F), 0.7F, 0.6F + rand.nextFloat() * 0.2F, 5 + rand.nextInt(4), 0, true));
                }
            }
            case ChampionModifier.MIGHTY -> {
                if (rand.nextFloat() <= 0.3F) {
                    addParticle(level, x, y, z, new CrackShardParticleOptions(ARGB.colorFromFloat(1.0F, 0.8F + rand.nextFloat() * 0.2F, 0.8F + rand.nextFloat() * 0.2F, 0.8F + rand.nextFloat() * 0.2F),
                            rand.nextInt(4), 1.0F + rand.nextFloat() * 0.3F, 4 + rand.nextInt(3)));
                }
            }
            case ChampionModifier.GRIM -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, 0.0, -0.02, 0.0, new FlameFanParticleOptions(0.6F + rand.nextFloat() * 0.4F, 0.0F, 0.8F));
                }
            }
            case ChampionModifier.WARDED -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, TCParticles.colorOf(TCParticles.LEAF_MOTE, 0.5F + rand.nextFloat() * 0.1F, 0.5F + rand.nextFloat() * 0.1F, 0.5F + rand.nextFloat() * 0.1F));
                }
            }
            case ChampionModifier.WARP -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, new WispFlameParticleOptions(ARGB.colorFromFloat(1.0F, 0.8F + rand.nextFloat() * 0.2F, 0.0F, 0.9F + rand.nextFloat() * 0.1F), 0.7F,
                            0.6F + rand.nextFloat() * 0.4F, 0.6F + rand.nextFloat() * 0.4F, 0));
                }
            }
            case ChampionModifier.UNDYING -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, 0.0, 0.03, 0.0,
                            new WispFlameParticleOptions(ARGB.colorFromFloat(1.0F, 0.1F + rand.nextFloat() * 0.1F, 0.8F + rand.nextFloat() * 0.2F, 0.1F + rand.nextFloat() * 0.1F), 0.9F,
                                    0.5F + rand.nextFloat() * 0.2F, 0.5F + rand.nextFloat() * 0.2F, 0));
                }
            }
            case ChampionModifier.FIERY -> addParticle(level, x, y, z, 0.0, 0.03, 0.0, new FlameFanParticleOptions(0.7F + rand.nextFloat() * 0.2F, 0.0F, 0.7F));
            case ChampionModifier.SICKLY -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, 0.0, -0.02, 0.0,
                            new FluxSwirlParticleOptions(ARGB.colorFromFloat(1.0F, 0.2F, 0.6F + rand.nextFloat() * 0.1F, 0.2F + rand.nextFloat() * 0.1F), 0.9F + rand.nextFloat() * 0.3F, 0.9F));
                }
            }
            case ChampionModifier.VENOMOUS -> {
                if (rand.nextBoolean()) {
                    addParticle(level, x, y, z, 0.0, 0.02, 0.0, TCParticles.colorOf(TCParticles.GOO_DRIP, 0.2F, 0.6F + rand.nextFloat() * 0.1F, 0.2F + rand.nextFloat() * 0.1F));
                }
            }
            case ChampionModifier.VAMPIRIC -> {
                if (rand.nextFloat() <= 0.2F) {
                    addParticle(level, x, y, z, TCParticles.colorOf(TCParticles.GOO_DRIP, 0.9F + rand.nextFloat() * 0.1F, 0.0F, 0.0F));
                }
            }
            case ChampionModifier.INFESTED -> {
                if (rand.nextBoolean()) {
                    level.addParticle(TCParticles.TAINT_SPLOSION.get(), mob.getX() + (rand.nextFloat() - 0.5) * mob.getBbWidth(), (mob.getBoundingBox().minY + mob.getBoundingBox().maxY) / 2.0,
                            mob.getZ() + (rand.nextFloat() - 0.5) * mob.getBbWidth(), 0.0, 0.0, 0.0);
                }
            }
            case ChampionModifier.TAINTED -> addParticle(level, x, y, z, 0.0, -0.01, 0.0,
                    new FluxSwirlParticleOptions(ARGB.colorFromFloat(0.25F, 0.1F + rand.nextFloat() * 0.2F, 0.0F, 0.1F + rand.nextFloat() * 0.1F), 2.0F + rand.nextFloat(), 2.0F));
            default -> {
            }
        }
    }

    private static void addParticle(Level level, double x, double y, double z, ParticleOptions options) {
        level.addParticle(options, x, y, z, 0.0, 0.0, 0.0);
    }

    private static void addParticle(Level level, double x, double y, double z, double vx, double vy, double vz, ParticleOptions options) {
        level.addParticle(options, x, y, z, vx, vy, vz);
    }
}
