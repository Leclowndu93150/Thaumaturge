package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.TaintSwarmParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.world.entity.Entity;

/**
 * A short-lived member of a taint swarm. Taint Swarms have no model in the legacy versions: their
 * visible body is a persistent cloud of particles which steer back toward the entity.
 */
public final class TaintSwarmParticle extends TCParticle {
    private static final float SPEED = 0.22F;
    private static final float TURN_STRENGTH = 0.08F;

    private final int targetId;
    private final double orbitX;
    private final double orbitY;
    private final double orbitZ;

    private TaintSwarmParticle(ClientLevel level, double x, double y, double z, TaintSwarmParticleOptions options) {
        super(level, x, y, z, 0.0, 0.0, 0.0, ParticleSheetHolder.SHEET);
        this.targetId = options.entityId();
        this.orbitX = (this.random.nextDouble() - this.random.nextDouble()) * 0.85;
        this.orbitY = (this.random.nextDouble() - this.random.nextDouble()) * 0.85;
        this.orbitZ = (this.random.nextDouble() - this.random.nextDouble()) * 0.85;
        this.xd = (this.random.nextDouble() - this.random.nextDouble()) * SPEED;
        this.yd = (this.random.nextDouble() - this.random.nextDouble()) * SPEED;
        this.zd = (this.random.nextDouble() - this.random.nextDouble()) * SPEED;
        this.lifetime = 28 + this.random.nextInt(8);
        this.friction = 0.985F;
        this.quadSize = 0.095F + this.random.nextFloat() * 0.045F;
        this.alpha = 0.75F;
        this.rCol = 0.7F;
        this.gCol = 0.0F;
        this.bCol = 1.0F;
        frame(this.random.nextInt(3));
    }

    @Override
    public void tick() {
        Entity target = this.level.getEntity(this.targetId);
        if (target == null || target.isRemoved()) {
            this.remove();
            return;
        }

        double targetX = target.getX() + this.orbitX;
        double targetY = target.getY() + target.getBbHeight() * 0.5 + this.orbitY;
        double targetZ = target.getZ() + this.orbitZ;
        double dx = targetX - this.x;
        double dy = targetY - this.y;
        double dz = targetZ - this.z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance > 0.001) {
            double pull = Math.min(TURN_STRENGTH, distance * TURN_STRENGTH);
            this.xd += dx / distance * pull;
            this.yd += dy / distance * pull;
            this.zd += dz / distance * pull;
        }
        super.tick();
    }

    @Override
    protected void update() {
        float progress = progress();
        this.alpha = 0.75F * Math.min(1.0F, progress * 6.0F) * (1.0F - progress * 0.35F);
        frame((this.age / 3) % 3);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return TCParticleLayers.translucent(this.sheet);
    }

    public static final class Provider implements ParticleProvider<TaintSwarmParticleOptions> {
        @Override
        public Particle createParticle(
                TaintSwarmParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            return new TaintSwarmParticle(level, x, y, z, options);
        }
    }

    private static final class ParticleSheetHolder {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("taint_fume");
    }
}
