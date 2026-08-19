package com.leclowndu93150.thaumaturge.client.particle;

import com.leclowndu93150.thaumaturge.content.particle.InfusionCrumbsParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class InfusionCrumbsParticle extends SeekerParticle {
    private static final float MIN_SCALE = 0.4F;
    private static final float SCALE_RANGE = 0.3F;
    private static final float DRIFT = 0.005F;
    private static final float TINT = 0.6F;

    private final float patchU;
    private final float patchV;

    private InfusionCrumbsParticle(ClientLevel level, double x, double y, double z, InfusionCrumbsParticleOptions options, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite, NO_ENTITY, new Vec3(options.tx(), options.ty(), options.tz()), new Vec3(options.sx(), options.sy(), options.sz()), DRIFT);
        setColor(TINT, TINT, TINT);
        this.alpha = 0.3F;
        this.quadSize = (MIN_SCALE + this.random.nextFloat() * SCALE_RANGE) * 0.1F;
        this.patchU = this.random.nextFloat() * 3.0F;
        this.patchV = this.random.nextFloat() * 3.0F;
    }

    @Override
    protected void update() {}

    @Override
    public Layer getLayer() {
        return Layer.bySprite(this.sprite);
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
        private final ItemStackRenderState scratchRenderState = new ItemStackRenderState();

        @Override
        public @Nullable Particle createParticle(InfusionCrumbsParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz, RandomSource random) {
            TextureAtlasSprite sprite = resolveSprite(options, level, random);
            if (sprite == null) {
                return null;
            }
            return new InfusionCrumbsParticle(level, x, y, z, options, sprite);
        }

        private @Nullable TextureAtlasSprite resolveSprite(InfusionCrumbsParticleOptions options, ClientLevel level, RandomSource random) {
            if (options.stack().item() instanceof BlockItem blockItem) {
                BlockState state = blockItem.getBlock().defaultBlockState();
                if (!state.isAir()) {
                    return Minecraft.getInstance().getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
                }
            }
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(scratchRenderState, options.stack().create(), ItemDisplayContext.GROUND, level, null, 0);
            Material.Baked material = scratchRenderState.pickParticleMaterial(random);
            return material != null ? material.sprite() : Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).missingSprite();
        }
    }
}
