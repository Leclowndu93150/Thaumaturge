package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.content.particle.VisSparkleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class VisSparkleParticle extends TTParticle {
    private static final int SAFETY_LIFETIME = 1000;
    private static final int GROW_TICKS = 9;
    private static final int GROWTH_DIVISOR_MIN = 45;
    private static final int GROWTH_DIVISOR_RANGE = 15;
    private static final double ARRIVAL_DISTANCE = 0.2;
    private static final double NEAR_DISTANCE = 2.0;
    private static final double HOMING_ACCELERATION = 0.1;
    private static final double MAX_SPEED = 0.1;
    private static final float NEAR_SHRINK = 0.95F;
    private static final float FRICTION = 0.985F;
    private static final float SIZE_BASE = 0.6F;
    private static final float PULSE_AMPLITUDE = 0.03F;
    private static final float PULSE_RATE = 1.0F / 3.0F;
    private static final float ALPHA = 0.5F;
    private static final double SPAWN_KICK = 0.01;
    private static final float DEFAULT_RED = 0.2F;
    private static final float DEFAULT_GREEN = 0.6F;
    private static final float DEFAULT_GREEN_VARIANCE = 0.3F;
    private static final float DEFAULT_BLUE = 0.2F;

    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final int growthDivisor;
    private float proximityScale = 1.0F;

    private VisSparkleParticle(
            ClientLevel level, double x, double y, double z, VisSparkleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        if (options.color() >= 0) {
            setColor(options.color());
        } else {
            setColor(DEFAULT_RED, DEFAULT_GREEN + this.random.nextFloat() * DEFAULT_GREEN_VARIANCE, DEFAULT_BLUE);
        }
        Vec3 target = ClientSpaces.renderPosition(level, new Vec3(options.tx(), options.ty(), options.tz()));
        this.targetX = target.x;
        this.targetY = target.y;
        this.targetZ = target.z;
        this.alpha = ALPHA;
        this.lifetime = SAFETY_LIFETIME;
        this.friction = FRICTION;
        this.growthDivisor = GROWTH_DIVISOR_MIN + this.random.nextInt(GROWTH_DIVISOR_RANGE);
        drift((float) SPAWN_KICK, (float) SPAWN_KICK, (float) SPAWN_KICK);
        this.quadSize = 0.0F;
    }

    @Override
    protected void update() {
        frame(this.age % sheetFrames());
        double dx = this.targetX - this.x;
        double dy = this.targetY - this.y;
        double dz = this.targetZ - this.z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance <= ARRIVAL_DISTANCE) {
            remove();
            return;
        }

        boolean near = distance < NEAR_DISTANCE;
        double acceleration = HOMING_ACCELERATION / distance;
        this.xd = Mth.clamp(this.xd + dx * acceleration, -MAX_SPEED, MAX_SPEED);
        this.yd = Mth.clamp(this.yd + dy * acceleration, -MAX_SPEED, MAX_SPEED);
        this.zd = Mth.clamp(this.zd + dz * acceleration, -MAX_SPEED, MAX_SPEED);

        if (this.age <= GROW_TICKS) {
            this.proximityScale = near ? NEAR_SHRINK : 1.0F;
        } else if (near) {
            this.proximityScale *= NEAR_SHRINK;
        }

        float growth = (float) Math.min(this.age, GROW_TICKS) / this.growthDivisor;
        float pulse = SIZE_BASE + PULSE_AMPLITUDE * Mth.sin(this.age * PULSE_RATE);
        this.quadSize = growth * pulse * this.proximityScale;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    public static final class Provider implements ParticleProvider<VisSparkleParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("vis_sparkle");

        @Override
        public Particle createParticle(
                VisSparkleParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            RandomSource random = level.getRandom();
            return new VisSparkleParticle(level, x, y, z, options, SHEET);
        }
    }
}
