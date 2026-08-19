package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.BoltParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class BoltParticle extends TCParticle {
    private static final int FRAME_COUNT = 16;
    private static final int BOLT_LIFETIME = 3;
    private static final float WAVE_AMPLITUDE_RATE = 10.0F;
    private static final float JITTER = 0.1F;
    private static final float MIN_ALPHA = 0.1F;
    private static final int SUBDIVISIONS = 4;
    private static final float BEAD_SIZE_FACTOR = 1.0F / 6.0F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;
    private static final int SEED_BOUND = 1000;

    private final Vec3 delta;
    private final float beadSize;
    private final int steps;
    private final long seed;
    private final float[] waveX;
    private final float[] waveY;
    private final float[] waveZ;

    private BoltParticle(ClientLevel level, double x, double y, double z, BoltParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        setColor(options.r(), options.g(), options.b());
        this.setSize(0.02F, 0.02F);
        this.lifetime = BOLT_LIFETIME;
        this.delta = new Vec3(options.targetX() - x, options.targetY() - y, options.targetZ() - z);
        this.beadSize = options.width() * BEAD_SIZE_FACTOR;
        float boltLength = (float) (this.delta.length() * Math.PI);
        this.steps = Math.max(2, (int) boltLength);
        this.seed = this.random.nextInt(SEED_BOUND);
        float phase = (float) (this.random.nextInt(50) * Math.PI);
        this.waveX = new float[this.steps + 1];
        this.waveY = new float[this.steps + 1];
        this.waveZ = new float[this.steps + 1];
        for (int step = 1; step < this.steps; step++) {
            float along = step * (boltLength / this.steps) + phase;
            this.waveX[step] = Mth.sin(along / 4.0F);
            this.waveY[step] = Mth.sin(along / 3.0F);
            this.waveZ[step] = Mth.sin(along / 2.0F);
        }
    }

    @Override
    protected void update() {
        frame((this.age + (int) this.seed) % FRAME_COUNT);
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera camera, float partialTickTime) {
        Vec3 cam = camera.position();
        float baseX = (float) (this.x - cam.x());
        float baseY = (float) (this.y - cam.y());
        float baseZ = (float) (this.z - cam.z());
        float fade = Mth.clamp(1.0F - (this.age + partialTickTime) / this.lifetime, MIN_ALPHA, 1.0F);
        int color = ARGB.colorFromFloat(fade, this.rCol, this.gCol, this.bCol);
        float amplitude = (this.age + partialTickTime) / WAVE_AMPLITUDE_RATE;
        RandomSource jitter = RandomSource.create(this.seed);
        float prevX = 0.0F;
        float prevY = 0.0F;
        float prevZ = 0.0F;
        for (int step = 1; step <= this.steps; step++) {
            float px;
            float py;
            float pz;
            if (step == this.steps) {
                px = (float) this.delta.x;
                py = (float) this.delta.y;
                pz = (float) this.delta.z;
            } else {
                px = (float) (this.delta.x * step / this.steps) + this.waveX[step] * amplitude + (jitter.nextFloat() - jitter.nextFloat()) * JITTER;
                py = (float) (this.delta.y * step / this.steps) + this.waveY[step] * amplitude + (jitter.nextFloat() - jitter.nextFloat()) * JITTER;
                pz = (float) (this.delta.z * step / this.steps) + this.waveZ[step] * amplitude + (jitter.nextFloat() - jitter.nextFloat()) * JITTER;
            }
            for (int sub = 0; sub < SUBDIVISIONS; sub++) {
                float t = (sub + 1.0F) / SUBDIVISIONS;
                renderState.add(getLayer(), baseX + Mth.lerp(t, prevX, px), baseY + Mth.lerp(t, prevY, py), baseZ + Mth.lerp(t, prevZ, pz), camera.rotation().x, camera.rotation().y,
                        camera.rotation().z, camera.rotation().w, this.beadSize, getU0(), getU1(), getV0(), getV1(), color, EMISSIVE_LIGHT);
            }
            prevX = px;
            prevY = py;
            prevZ = pz;
        }
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.beadSize;
    }

    public static final class Provider implements ParticleProvider<BoltParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("bolt");

        @Override
        public Particle createParticle(BoltParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new BoltParticle(level, x, y, z, options, SHEET);
        }
    }
}
