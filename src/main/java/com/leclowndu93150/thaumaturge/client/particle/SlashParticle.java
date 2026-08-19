package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SlashParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.RandomSource;

public final class SlashParticle extends TCParticle {
    private static final float START_SIZE = 0.05F;
    private static final float END_SIZE = 0.15F;
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private final FacingCameraMode facing;

    private SlashParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SlashParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, vx, vy, vz, sheet);
        this.lifetime = Math.max(1, options.duration());
        this.quadSize = START_SIZE;
        this.roll = options.roll();
        this.oRoll = options.roll();
        float yaw = options.yaw() * DEG_TO_RAD;
        float pitch = options.pitch() * DEG_TO_RAD;
        this.facing = (target, camera, partialTick) -> target.rotationYXZ(-yaw, pitch, 0.0F);
        frameByProgress();
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = Keyframes.sample(t, 0.0F, 0.5F, 0.3F, 0.2F, 0.1F, 0.0F);
        this.quadSize = Keyframes.sample(t, START_SIZE, END_SIZE);
        frameByProgress();
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return this.facing;
    }

    public static final class Provider implements ParticleProvider<SlashParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("slash");

        @Override
        public Particle createParticle(SlashParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new SlashParticle(level, x, y, z, vx, vy, vz, options, SHEET);
        }
    }
}
