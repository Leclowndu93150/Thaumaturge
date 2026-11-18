package com.leclowndu93150.thaumaturge.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class DelayedSparkleAttachmentTest {
    @Test
    void delayedShellKeepsItsAttachmentUpdatedWithoutStartingTheEffect() {
        CountingAttachedParticle particle = new CountingAttachedParticle(mock(ClientLevel.class));
        particle.setParticleSpeed(0.4, 0.0, 0.0);
        particle.setStartDelay(2);

        particle.tick();
        particle.tick();

        assertEquals(2, particle.attachmentUpdates);
        assertEquals(0, particle.ageValue());
        assertEquals(0, particle.visualUpdates);
        assertEquals(Vec3.ZERO, particle.getPos());
        assertEquals(0.0F, particle.alphaValue());
    }

    private static final class CountingAttachedParticle extends TTParticle {
        private int attachmentUpdates;
        private int visualUpdates;

        private CountingAttachedParticle(ClientLevel level) {
            super(level, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, (ParticleSheet) null);
            this.lifetime = 20;
        }

        private void setStartDelay(int ticks) {
            setDelay(ticks);
        }

        private int ageValue() {
            return this.age;
        }

        private float alphaValue() {
            return this.alpha;
        }

        @Override
        protected void tickWhileDelayed() {
            move(0.0, 0.0, 0.0);
        }

        @Override
        public void move(double dx, double dy, double dz) {
            attachmentUpdates++;
            super.move(dx, dy, dz);
        }

        @Override
        protected void update() {
            visualUpdates++;
        }
    }
}
