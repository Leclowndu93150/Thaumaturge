package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.WispParticleOptions;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class WispParticle extends SingleQuadParticle {
    private static final float BASE_ALPHA = 0.2F;
    private static final float MAX_DIST_SQ = 1024.0F;
    private static final float SCALE_MULTIPLIER = 0.4F;
    private static final float COLOR_MAX = 0.05F;
    private static final int FRAME_COUNT = 4;

    private final ParticleSheet sheet;
    private int frame;
    private final int targetEntityId;
    private Entity target;

    private WispParticle(ClientLevel level, double x, double y, double z, WispParticleOptions data, ParticleSheet sheet) {
        super(level, x, y, z, 0.0, 0.0, 0.0, (net.minecraft.client.renderer.texture.TextureAtlasSprite) null);
        this.sheet = sheet;
        this.targetEntityId = data.entityId();
        this.xd = level.getRandom().nextGaussian() * 0.03;
        this.yd = -0.05;
        this.zd = level.getRandom().nextGaussian() * 0.03;
        this.quadSize *= SCALE_MULTIPLIER;
        this.lifetime = (int) (40.0 / (level.getRandom().nextDouble() * 0.3 + 0.7));
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.setSize(0.01F, 0.01F);
        this.rCol = level.getRandom().nextFloat() * COLOR_MAX;
        this.gCol = level.getRandom().nextFloat() * COLOR_MAX;
        this.bCol = level.getRandom().nextFloat() * COLOR_MAX;
        this.alpha = BASE_ALPHA;
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public void tick() {
        super.tick();
        this.frame = this.age % FRAME_COUNT;
        if (this.target == null && this.targetEntityId != WispParticleOptions.NO_ENTITY) {
            this.target = this.level.getEntity(this.targetEntityId);
        }
        if (this.target != null && !this.onGround) {
            Vec3 motion = this.target.getDeltaMovement();
            this.x += motion.x;
            this.z += motion.z;
        }
    }

    @Override
    public void extract(QuadParticleRenderState particleTypeRenderState, Camera camera, float partialTickTime) {
        float ageScale = 1.0F - (float) this.age / this.lifetime;
        Vec3 camPos = camera.position();
        double dx = camPos.x() - this.x;
        double dy = camPos.y() - this.y;
        double dz = camPos.z() - this.z;
        double distSq = dx * dx + dy * dy + dz * dz;
        float base = (float) (1.0 - Math.min((double) MAX_DIST_SQ, distSq) / MAX_DIST_SQ);
        float prev = this.alpha;
        this.alpha = Mth.clamp(BASE_ALPHA * ageScale * base, 0.0F, 1.0F);
        super.extract(particleTypeRenderState, camera, partialTickTime);
        this.alpha = prev;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCParticleLayers.translucent(this.sheet);
    }

    @Override
    protected float getU0() {
        return this.sheet.u0(this.frame);
    }

    @Override
    protected float getU1() {
        return this.sheet.u1(this.frame);
    }

    @Override
    protected float getV0() {
        return 0.0F;
    }

    @Override
    protected float getV1() {
        return 1.0F;
    }

    public static final class Provider implements ParticleProvider<WispParticleOptions> {
        private static final ParticleSheet SHEET = TCParticleSheets.sheet("wisp");

        @Override
        public Particle createParticle(WispParticleOptions options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new WispParticle(level, x, y, z, options, SHEET);
        }
    }
}
