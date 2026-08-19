package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class BoreSparkleParticle extends SeekerParticle {
    private static final int FRAME_COUNT = 4;
    private static final float MIN_SCALE = 0.5F;
    private static final float SCALE_RANGE = 0.5F;
    private static final float DRIFT = 0.01F;

    private BoreSparkleParticle(ClientLevel level, double x, double y, double z, BoreSparkleParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, sheet, options.targetEntityId(), new Vec3(options.tx(), options.ty(), options.tz()), Vec3.ZERO, DRIFT);
        setColor(options.r(), options.g(), options.b());
        this.quadSize = MIN_SCALE + this.random.nextFloat() * SCALE_RANGE;
    }

    @Override
    protected void update() {
        frame(this.age % FRAME_COUNT);
    }

    @Override
    public float getQuadSize(float partialTick) {
        float pulse = Mth.sin(this.age / 3.0F) * 0.5F + 1.0F;
        return this.quadSize * 0.1F * pulse;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return LightCoordsUtil.FULL_BRIGHT;
    }

    public static final class Provider implements ParticleProvider<BoreSparkleParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("bore_sparkle");

        @Override
        public Particle createParticle(BoreSparkleParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new BoreSparkleParticle(level, x, y, z, options, SHEET);
        }
    }
}
