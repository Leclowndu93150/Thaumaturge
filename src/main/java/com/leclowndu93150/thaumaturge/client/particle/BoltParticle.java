package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.BoltParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class BoltParticle extends TTParticle {
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

    private BoltParticle(
            ClientLevel level, double x, double y, double z, BoltParticleOptions options, ParticleSheet sheet) {
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
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 cam = camera.getPosition();
        float baseX = (float) (this.x - cam.x());
        float baseY = (float) (this.y - cam.y());
        float baseZ = (float) (this.z - cam.z());
        float fade = Mth.clamp(1.0F - (this.age + partialTick) / this.lifetime, MIN_ALPHA, 1.0F);
        int color = ARGB32.colorFromFloat(fade, this.rCol, this.gCol, this.bCol);
        float amplitude = (this.age + partialTick) / WAVE_AMPLITUDE_RATE;
        RandomSource jitter = RandomSource.create(this.seed);
        Quaternionf rotation = camera.rotation();
        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();
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
                px = (float) (this.delta.x * step / this.steps)
                        + this.waveX[step] * amplitude
                        + (jitter.nextFloat() - jitter.nextFloat()) * JITTER;
                py = (float) (this.delta.y * step / this.steps)
                        + this.waveY[step] * amplitude
                        + (jitter.nextFloat() - jitter.nextFloat()) * JITTER;
                pz = (float) (this.delta.z * step / this.steps)
                        + this.waveZ[step] * amplitude
                        + (jitter.nextFloat() - jitter.nextFloat()) * JITTER;
            }
            for (int sub = 0; sub < SUBDIVISIONS; sub++) {
                float t = (sub + 1.0F) / SUBDIVISIONS;
                emitQuad(
                        buffer,
                        rotation,
                        baseX + Mth.lerp(t, prevX, px),
                        baseY + Mth.lerp(t, prevY, py),
                        baseZ + Mth.lerp(t, prevZ, pz),
                        this.beadSize,
                        u0,
                        u1,
                        v0,
                        v1,
                        color,
                        EMISSIVE_LIGHT);
            }
            prevX = px;
            prevY = py;
            prevZ = pz;
        }
    }

    private static void emitQuad(
            VertexConsumer buffer,
            Quaternionf rotation,
            float x,
            float y,
            float z,
            float size,
            float u0,
            float u1,
            float v0,
            float v1,
            int color,
            int light) {
        emitVertex(buffer, rotation, x, y, z, 1.0F, -1.0F, size, u1, v1, color, light);
        emitVertex(buffer, rotation, x, y, z, 1.0F, 1.0F, size, u1, v0, color, light);
        emitVertex(buffer, rotation, x, y, z, -1.0F, 1.0F, size, u0, v0, color, light);
        emitVertex(buffer, rotation, x, y, z, -1.0F, -1.0F, size, u0, v1, color, light);
    }

    private static void emitVertex(
            VertexConsumer buffer,
            Quaternionf rotation,
            float x,
            float y,
            float z,
            float cornerX,
            float cornerY,
            float size,
            float u,
            float v,
            int color,
            int light) {
        Vector3f pos =
                new Vector3f(cornerX, cornerY, 0.0F).rotate(rotation).mul(size).add(x, y, z);
        buffer.addVertex(pos.x(), pos.y(), pos.z()).setUv(u, v).setColor(color).setLight(light);
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.beadSize;
    }

    public static final class Provider implements ParticleProvider<BoltParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("bolt");

        @Override
        public Particle createParticle(
                BoltParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            return new BoltParticle(level, x, y, z, options, SHEET);
        }
    }
}
