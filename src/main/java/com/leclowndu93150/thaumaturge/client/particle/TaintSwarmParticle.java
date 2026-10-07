package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.TaintSwarmParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Entity;

public final class TaintSwarmParticle extends TTParticle {
    private static final int GLOW_FRAMES = 16;
    private static final int SWARM_FRAMES = 8;
    private static final int SWARM_FRAME_STEP = 2;

    private static final float SPEED = 0.22F;
    private static final float TURN_STRENGTH = 0.08F;

    private final int targetId;
    private final double orbitX;
    private final double orbitY;
    private final double orbitZ;

    private TaintSwarmParticle(ClientLevel level, double x, double y, double z, TaintSwarmParticleOptions options) {
        super(level, x, y, z, 0.0, 0.0, 0.0, (TextureAtlasSprite) null);
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
        this.rCol = 0.8F + this.random.nextFloat() * 0.2F;
        this.gCol = this.random.nextFloat() * 0.4F;
        this.bCol = 1.0F - this.random.nextFloat() * 0.2F;
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
    }

    @Override
    public Layer getLayer() {
        return TTParticleLayers.ORB_GLOW_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<TaintSwarmParticleOptions> {
        @Override
        public Particle createParticle(TaintSwarmParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new TaintSwarmParticle(level, x, y, z, options);
        }
    }

    @Override
    protected float getU0() {
        return (this.age % SWARM_FRAMES) * SWARM_FRAME_STEP / (float) GLOW_FRAMES;
    }

    @Override
    protected float getU1() {
        return getU0() + 1.0F / GLOW_FRAMES;
    }

    @Override
    protected float getV0() {
        return 0.0F;
    }

    @Override
    protected float getV1() {
        return 1.0F;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }
}
