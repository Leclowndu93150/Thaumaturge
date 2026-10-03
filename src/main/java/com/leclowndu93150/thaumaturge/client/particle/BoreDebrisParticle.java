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
    private static final float MIN_SCALE = 0.4F;
    private static final float SCALE_RANGE = 0.3F;
    private static final float DRIFT = 0.005F;
    private static final float TINT = 0.6F;

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
                DRIFT);
        setColor(TINT, TINT, TINT);
        this.quadSize = (MIN_SCALE + this.random.nextFloat() * SCALE_RANGE) * 0.1F;
        this.patchU = this.random.nextFloat() * 3.0F;
        this.patchV = this.random.nextFloat() * 3.0F;
    }

    @Override
    protected void update() {}

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.patchU + 1.0F) / 4.0F);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.patchU / 4.0F);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.patchV / 4.0F);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.patchV + 1.0F) / 4.0F);
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
