package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.InfusionCrumbsParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jspecify.annotations.Nullable;

public final class InfusionCrumbsParticle extends SeekerParticle {
    private static final float MIN_SCALE = 0.4F;
    private static final float SCALE_RANGE = 0.3F;
    private static final float DRIFT = 0.005F;
    private static final float TINT = 0.6F;

    private final float patchU;
    private final float patchV;

    private InfusionCrumbsParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            InfusionCrumbsParticleOptions options,
            TextureAtlasSprite sprite) {
        super(
                level,
                x,
                y,
                z,
                sprite,
                NO_ENTITY,
                new Vec3(options.tx(), options.ty(), options.tz()),
                new Vec3(options.sx(), options.sy(), options.sz()),
                DRIFT);
        setColor(TINT, TINT, TINT);
        this.alpha = 0.3F;
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

    public static final class Provider implements ParticleProvider<InfusionCrumbsParticleOptions> {
        @Override
        public @Nullable Particle createParticle(
                InfusionCrumbsParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double vx,
                double vy,
                double vz) {
            TextureAtlasSprite sprite = resolveSprite(options, level);
            if (sprite == null) {
                return null;
            }
            return new InfusionCrumbsParticle(level, x, y, z, options, sprite);
        }

        private @Nullable TextureAtlasSprite resolveSprite(InfusionCrumbsParticleOptions options, ClientLevel level) {
            if (options.stack().getItem() instanceof BlockItem blockItem) {
                BlockState state = blockItem.getBlock().defaultBlockState();
                if (!state.isAir()) {
                    return Minecraft.getInstance()
                            .getBlockRenderer()
                            .getBlockModelShaper()
                            .getBlockModel(state)
                            .getParticleIcon(ModelData.EMPTY);
                }
            }
            return Minecraft.getInstance()
                    .getItemRenderer()
                    .getModel(options.stack(), level, null, 0)
                    .getParticleIcon(ModelData.EMPTY);
        }
    }
}
