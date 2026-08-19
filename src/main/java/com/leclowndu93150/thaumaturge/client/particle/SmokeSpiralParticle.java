package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.SmokeSpiralParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class SmokeSpiralParticle extends TCParticle {
    private static final int FRAME_COUNT = 5;
    private static final int BASE_LIFETIME = 20;
    private static final float BASE_ALPHA = 0.66F;
    private static final float SIZE = 0.15F;
    private static final float TURNS = 2.0F;

    private final float radius;
    private final float startAngle;
    private final int floorY;

    private SmokeSpiralParticle(ClientLevel level, double x, double y, double z, SmokeSpiralParticleOptions options, ParticleSheet sheet) {
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
    public void extract(QuadParticleRenderState renderState, Camera camera, float partialTickTime) {
        float t = (this.age + partialTickTime) / this.lifetime;
        float azimuth = this.startAngle + (float) (Math.PI * 2.0) * TURNS * t;
        float elevation = (float) (Math.PI / 2.0) * (1.0F - 2.0F * t);
        float ring = Mth.cos(elevation) * this.radius;
        Vec3 cam = camera.position();
        double centerX = Mth.lerp(partialTickTime, this.xo, this.x);
        double centerY = Mth.lerp(partialTickTime, this.yo, this.y) - Mth.sin(elevation) * this.radius;
        double centerZ = Mth.lerp(partialTickTime, this.zo, this.z);
        centerY = Math.max(centerY, this.floorY + 0.1);
        Quaternionf rotation = new Quaternionf();
        getFacingCameraMode().setRotation(rotation, camera, partialTickTime);
        extractRotatedQuad(renderState, rotation, (float) (centerX - Mth.sin(azimuth) * ring - cam.x()), (float) (centerY - cam.y()), (float) (centerZ + Mth.cos(azimuth) * ring - cam.z()),
                partialTickTime);
    }

    public static final class Provider implements ParticleProvider<SmokeSpiralParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("smoke_spiral");

        @Override
        public Particle createParticle(SmokeSpiralParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new SmokeSpiralParticle(level, x, y, z, options, SHEET);
        }
    }
}
