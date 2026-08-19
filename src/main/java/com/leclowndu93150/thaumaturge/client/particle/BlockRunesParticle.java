package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class BlockRunesParticle extends TCParticle {
    private static final int RUNE_COUNT = 16;
    private static final float QUAD_RADIUS = 0.3F;
    private static final float FACE_OFFSET = -0.51F;
    private static final int EMISSIVE_LIGHT = 0x00F000F0;
    private static final float FADE_IN_PORTION = 0.2F;

    private final Quaternionf faceRotation;
    private final Vector3f faceOffset;

    private BlockRunesParticle(ClientLevel level, double x, double y, double z, BlockRunesParticleOptions options, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, sheet);
        this.rCol = options.r() == 0.0F ? 1.0F : options.r();
        this.gCol = options.g();
        this.bCol = options.b();
        this.gravity = options.gravity();
        this.lifetime = 3 * Math.max(1, options.duration());
        this.alpha = 0.0F;
        this.quadSize = QUAD_RADIUS * (options.variant() ? (float) (0.5 + this.random.nextGaussian() * 0.1) : (float) (1.0 + this.random.nextGaussian() * 0.1));
        this.setSize(0.01F, 0.01F);
        frame(this.random.nextInt(RUNE_COUNT));
        this.faceRotation = new Quaternionf().rotateY((float) Math.toRadians(this.random.nextInt(4) * 90.0F));
        float slideX = options.variant() ? 0.0F : this.random.nextFloat() * 0.2F;
        float slideY = -0.3F + this.random.nextFloat() * 0.6F;
        this.faceOffset = this.faceRotation.transform(new Vector3f(-slideY, slideX, FACE_OFFSET));
    }

    @Override
    protected void update() {
        float t = progress();
        this.alpha = 0.5F * (t < FADE_IN_PORTION ? t / FADE_IN_PORTION : 1.0F - t);
    }

    @Override
    public void extract(QuadParticleRenderState renderState, Camera camera, float partialTickTime) {
        extractRotatedQuad(renderState, camera, new Quaternionf(this.faceRotation), partialTickTime);
    }

    @Override
    protected void extractRotatedQuad(QuadParticleRenderState renderState, Quaternionf rotation, float x, float y, float z, float partialTickTime) {
        super.extractRotatedQuad(renderState, rotation, x + this.faceOffset.x(), y + this.faceOffset.y(), z + this.faceOffset.z(), partialTickTime);
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return EMISSIVE_LIGHT;
    }

    public static final class Provider implements ParticleProvider<BlockRunesParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("block_runes");

        @Override
        public Particle createParticle(BlockRunesParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            return new BlockRunesParticle(level, x, y, z, options, SHEET);
        }
    }
}
