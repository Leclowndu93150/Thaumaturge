package com.leclowndu93150.thaumaturge.content.spell.fx;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class SpellFxStylesTest {
    @Test
    void directionalParticleStylesKeepTheSprayVelocity() {
        assertMotionIsForward(SpellFxStyles::flame);
        assertMotionIsForward(SpellFxStyles::frost);
        assertMotionIsForward(SpellFxStyles::gust);
        assertMotionIsForward(SpellFxStyles::pebble);
    }

    private static void assertMotionIsForward(StyleSpawner spawner) {
        Level level = mock(Level.class);
        RandomSource random = mock(RandomSource.class);
        when(random.nextGaussian()).thenReturn(0.0);
        when(random.nextFloat()).thenReturn(0.0F);
        when(random.nextInt(anyInt())).thenReturn(0);

        Vec3 position = new Vec3(1.0, 2.0, 3.0);
        Vec3 motion = new Vec3(0.25, 0.5, 0.75);
        spawner.spawn(level, position, motion, 0xFFFFFF, random);

        verify(level)
                .addParticle(
                        any(ParticleOptions.class),
                        eq(position.x),
                        eq(position.y),
                        eq(position.z),
                        eq(motion.x),
                        eq(motion.y),
                        eq(motion.z));
    }

    @FunctionalInterface
    private interface StyleSpawner {
        void spawn(Level level, Vec3 at, Vec3 motion, int color, RandomSource random);
    }
}
