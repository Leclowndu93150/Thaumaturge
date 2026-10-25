package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;

final class PechMoods {
    static final byte EVENT_IDLE = 70;
    static final byte EVENT_TRADE = 71;
    static final byte EVENT_HAPPY = 72;
    static final byte EVENT_ANGRY = 73;

    private static final float MUMBLE_IDLE = (float) Math.PI;
    private static final float MUMBLE_EXCITED = (float) (Math.PI * 2.0);
    private static final float MUMBLE_DECAY = 0.75F;
    private static final int BURST_PARTICLES = 5;
    private static final int ANGRY_PARTICLE_ODDS = 15;
    private static final int HAPPY_PARTICLE_ODDS = 25;
    private static final double PARTICLE_LIFT = 0.5;
    private static final double PARTICLE_VELOCITY = 0.02;

    private PechMoods() {}

    static void decay(EntityPech pech) {
        if (pech.chatterLevel > 0.0F) {
            pech.chatterLevel *= MUMBLE_DECAY;
        }
    }

    static void tickClient(EntityPech pech) {
        RandomSource random = pech.getRandom();
        if (pech.rageTicks() > 0 && random.nextInt(ANGRY_PARTICLE_ODDS) == 0) {
            particle(pech, true);
        }
        if (pech.isDomesticated() && random.nextInt(HAPPY_PARTICLE_ODDS) == 0) {
            particle(pech, false);
        }
    }

    static boolean handleEvent(EntityPech pech, byte id) {
        if (id == EVENT_IDLE) {
            pech.chatterLevel = MUMBLE_IDLE;
        } else if (id == EVENT_TRADE) {
            pech.chatterLevel = MUMBLE_EXCITED;
        } else if (id == EVENT_HAPPY) {
            burst(pech, false);
        } else if (id == EVENT_ANGRY) {
            burst(pech, true);
            pech.chatterLevel = MUMBLE_EXCITED;
        } else {
            return false;
        }
        return true;
    }

    private static void burst(EntityPech pech, boolean angry) {
        for (int i = 0; i < BURST_PARTICLES; i++) {
            particle(pech, angry);
        }
    }

    private static void particle(EntityPech pech, boolean angry) {
        RandomSource random = pech.getRandom();
        double x = pech.getX() + (random.nextFloat() * 2.0F - 1.0F) * pech.getBbWidth();
        double y = pech.getY() + PARTICLE_LIFT + random.nextFloat() * pech.getBbHeight();
        double z = pech.getZ() + (random.nextFloat() * 2.0F - 1.0F) * pech.getBbWidth();
        pech.level()
                .addParticle(
                        angry ? ParticleTypes.ANGRY_VILLAGER : ParticleTypes.HAPPY_VILLAGER,
                        x,
                        y,
                        z,
                        random.nextGaussian() * PARTICLE_VELOCITY,
                        random.nextGaussian() * PARTICLE_VELOCITY,
                        random.nextGaussian() * PARTICLE_VELOCITY);
    }
}
