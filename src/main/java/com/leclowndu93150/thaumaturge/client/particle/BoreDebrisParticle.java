package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.BoreDebrisParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jspecify.annotations.Nullable;

public final class BoreDebrisParticle extends SeekerParticle {
    private static final float WINDOW_STEPS = 4.0F;
    private static final float OFFSET_RANGE = 3.0F;
    private static final float DRIFT_STRENGTH = 0.004F;
    private static final float TINT = 0.82F;
    private static final float SIZE_UNIT = 1.0F / 32.0F;
    private static final float SIZE_BASE = 0.4F;
    private static final float SIZE_RANGE = 1.6F;

    private final float patchU;
    private final float patchV;

    private BoreDebrisParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            BoreDebrisParticleOptions options,
            TextureAtlasSprite sprite) {
        super(
                level,
                x,
                y,
                z,
                sprite,
                options.targetEntityId(),
                new Vec3(options.tx(), options.ty(), options.tz()),
                new Vec3(options.sx(), options.sy(), options.sz()),
                DRIFT_STRENGTH);
        setColor(TINT, TINT, TINT);
        this.quadSize = (SIZE_BASE + this.random.nextFloat() * SIZE_RANGE) * SIZE_UNIT;
        this.patchU = this.random.nextFloat() * OFFSET_RANGE;
        this.patchV = this.random.nextFloat() * OFFSET_RANGE;
    }

    @Override
    protected void update() {}

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.patchU + 1.0F) / WINDOW_STEPS);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.patchU / WINDOW_STEPS);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.patchV / WINDOW_STEPS);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.patchV + 1.0F) / WINDOW_STEPS);
    }

    public static final class Provider implements ParticleProvider<BoreDebrisParticleOptions> {
        @Override
        public @Nullable Particle createParticle(
                BoreDebrisParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            if (options.state().isAir()) {
                return null;
            }
            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getBlockRenderer()
                    .getBlockModelShaper()
                    .getBlockModel(options.state())
                    .getParticleIcon(ModelData.EMPTY);
            return new BoreDebrisParticle(level, x, y, z, options, sprite);
        }
    }
}
