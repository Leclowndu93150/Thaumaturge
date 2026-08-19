package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.VisSparkleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class VisSparkleParticle extends TCParticle {
    private static final int FRAME_COUNT = 16;
    private static final int MAX_LIFETIME = 1000;
    private static final int GROW_TICKS = 10;
    private static final float FRICTION = 0.985F;
    private static final double ACCELERATION = 0.1;
    private static final double SPEED_CAP = 0.1;
    private static final double ARRIVE_DISTANCE = 0.2;
    private static final double SHRINK_DISTANCE = 2.0;
    private static final float SHRINK_FACTOR = 0.95F;
    private static final float SPAWN_SPREAD = 0.01F;

    private final Vec3 target;
    private final float growDivisor;

    private VisSparkleParticle(ClientLevel level, double x, double y, double z, VisSparkleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        if (options.color() >= 0) {
            setColor(options.color());
        } else {
            setColor(0.2F, 0.6F + this.random.nextFloat() * 0.3F, 0.2F);
        }
        this.target = new Vec3(options.tx(), options.ty(), options.tz());
        this.alpha = 0.5F;
        this.lifetime = MAX_LIFETIME;
        this.friction = FRICTION;
        this.growDivisor = 45 + this.random.nextInt(15);
        drift(SPAWN_SPREAD, SPAWN_SPREAD, SPAWN_SPREAD);
        this.quadSize = 0.0F;
    }

    @Override
    protected void update() {
        frame(this.age % FRAME_COUNT);
        if (this.age < GROW_TICKS) {
            this.quadSize = this.age / this.growDivisor;
        }
        Vec3 toTarget = this.target.subtract(this.x, this.y, this.z);
        double distance = toTarget.length();
        if (distance < ARRIVE_DISTANCE) {
            remove();
            return;
        }
        if (distance < SHRINK_DISTANCE) {
            this.quadSize *= SHRINK_FACTOR;
        }
        Vec3 steer = toTarget.scale(ACCELERATION / Math.max(distance, 1.0E-6));
        this.xd = Mth.clamp(this.xd + steer.x, -SPEED_CAP, SPEED_CAP);
        this.yd = Mth.clamp(this.yd + steer.y, -SPEED_CAP, SPEED_CAP);
        this.zd = Mth.clamp(this.zd + steer.z, -SPEED_CAP, SPEED_CAP);
    }

    @Override
    public float getQuadSize(float partialTick) {
        float pulse = Mth.sin(this.age / 3.0F) * 0.3F + 6.0F;
        return 0.1F * this.quadSize * pulse;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    public static final class Provider implements ParticleProvider<VisSparkleParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("vis_sparkle");

        @Override
        public Particle createParticle(VisSparkleParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new VisSparkleParticle(level, x, y, z, options, SHEET);
        }
    }
}
