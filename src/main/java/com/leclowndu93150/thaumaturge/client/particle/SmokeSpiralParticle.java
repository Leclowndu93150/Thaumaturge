package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SmokeSpiralParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SmokeSpiralParticle extends TTParticle {
    private static final int FRAME_COUNT = 5;
    private static final int BASE_LIFETIME = 20;
    private static final float BASE_ALPHA = 0.66F;
    private static final float SIZE = 0.15F;
    private static final float TURNS = 2.0F;

    private final float radius;
    private final float startAngle;
    private final int floorY;

    private SmokeSpiralParticle(
            ClientLevel level, double x, double y, double z, SmokeSpiralParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.lifetime = BASE_LIFETIME + this.random.nextInt(10);
        this.gravity = -0.01F;
        this.radius = options.radius();
        this.startAngle = (float) Math.toRadians(options.start());
        this.floorY = options.minY();
        setColor(options.r(), options.g(), options.b());
        this.quadSize = SIZE;
        this.setSize(0.01F, 0.01F);
        frame(0);
    }

    @Override
    protected void update() {
        this.alpha = BASE_ALPHA * (1.0F - progress());
        frame((int) (progress() * (FRAME_COUNT - 1)));
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        float t = (this.age + partialTick) / this.lifetime;
        float azimuth = this.startAngle + (float) (Math.PI * 2.0) * TURNS * t;
        float elevation = (float) (Math.PI / 2.0) * (1.0F - 2.0F * t);
        float ring = Mth.cos(elevation) * this.radius;
        Vec3 cam = camera.getPosition();
        double centerX = Mth.lerp(partialTick, this.xo, this.x);
        double centerY = Mth.lerp(partialTick, this.yo, this.y) - Mth.sin(elevation) * this.radius;
        double centerZ = Mth.lerp(partialTick, this.zo, this.z);
        centerY = Math.max(centerY, this.floorY + 0.1);
        Quaternionf rotation = new Quaternionf();
        getFacingCameraMode().setRotation(rotation, camera, partialTick);
        float px = (float) (centerX - Mth.sin(azimuth) * ring - cam.x());
        float py = (float) (centerY - cam.y());
        float pz = (float) (centerZ + Mth.cos(azimuth) * ring - cam.z());
        int color = ARGB32.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol);
        int light = getLightColor(partialTick);
        float size = getQuadSize(partialTick);
        emitVertex(buffer, rotation, px, py, pz, 1.0F, -1.0F, size, getU1(), getV1(), color, light);
        emitVertex(buffer, rotation, px, py, pz, 1.0F, 1.0F, size, getU1(), getV0(), color, light);
        emitVertex(buffer, rotation, px, py, pz, -1.0F, 1.0F, size, getU0(), getV0(), color, light);
        emitVertex(buffer, rotation, px, py, pz, -1.0F, -1.0F, size, getU0(), getV1(), color, light);
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

    public static final class Provider implements ParticleProvider<SmokeSpiralParticleOptions> {
        private static final ParticleSheet SHEET = TTParticleSheets.sheet("smoke_spiral");

        @Override
        public Particle createParticle(
                SmokeSpiralParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            return new SmokeSpiralParticle(level, x, y, z, options, SHEET);
        }
    }
}
