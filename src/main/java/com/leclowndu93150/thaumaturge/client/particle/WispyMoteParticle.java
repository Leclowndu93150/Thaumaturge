package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class WispyMoteParticle extends TCParticle {
    private static final int FRAME_COUNT = 16;
    private static final float PEAK_ALPHA = 0.6F;
    private static final float START_SIZE = 0.1F;
    private static final float END_SIZE = 0.05F;
    private static final float DRIFT = 0.0025F;
    private static final double WIND_SCALE = 0.001;
    private static final double SEEK_ACCEL = 0.3;
    private static final double SEEK_ACCEL_NEAR = 0.6;
    private static final double NEAR_DISTANCE = 4.0;
    private static final double ARRIVE_DISTANCE = 0.25;
    private static final double SPEED_LIMIT = 0.35;

    private final int targetEntityId;
    private Entity target;

    private WispyMoteParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, WispyMoteParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        setColor(options.color());
        this.lifetime = (int) (options.age() + options.age() / 2.0F * this.random.nextFloat());
        this.gravity = options.gravity();
        this.targetEntityId = options.targetEntityId();
        this.alpha = 0.0F;
        setMoonWind(WIND_SCALE);
    }

    @Override
    protected void update() {
        drift(DRIFT, 0.0F, DRIFT);
        frame(this.age % FRAME_COUNT);
        float t = progress();
        this.alpha = PEAK_ALPHA * Keyframes.sample(t, 0.0F, 1.0F, 1.0F, 0.0F);
        this.quadSize = Keyframes.sample(t, START_SIZE, END_SIZE);
        if (this.targetEntityId != WispyMoteParticleOptions.NO_ENTITY) {
            seekTarget();
        }
    }

    private void seekTarget() {
        if (this.target == null) {
            this.target = this.level.getEntity(this.targetEntityId);
            if (this.target == null) {
                return;
            }
        }
        if (!this.target.isAlive()) {
            remove();
            return;
        }
        Vec3 toTarget = this.target.position().subtract(this.x, this.y, this.z);
        double distance = toTarget.length();
        if (distance < ARRIVE_DISTANCE) {
            remove();
            return;
        }
        double accel = SEEK_ACCEL;
        if (distance < NEAR_DISTANCE) {
            accel = SEEK_ACCEL_NEAR;
            this.quadSize *= 0.9F;
        }
        Vec3 steer = toTarget.scale(accel / distance);
        this.xd = Mth.clamp(this.xd + steer.x, -SPEED_LIMIT, SPEED_LIMIT);
        this.yd = Mth.clamp(this.yd + steer.y, -SPEED_LIMIT, SPEED_LIMIT);
        this.zd = Mth.clamp(this.zd + steer.z, -SPEED_LIMIT, SPEED_LIMIT);
    }

    public static final class Provider implements ParticleProvider<WispyMoteParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("wispy_mote");

        @Override
        public Particle createParticle(WispyMoteParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new WispyMoteParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
