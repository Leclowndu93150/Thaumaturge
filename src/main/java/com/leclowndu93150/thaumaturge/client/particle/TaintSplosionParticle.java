package com.leclowndu93150.thaumaturge.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public final class TaintSplosionParticle extends SingleQuadParticle {
    private static final float BOOM_MOTION_SCALE = 0.964F;
    private static final float BOOM_LIFT = 0.1F;
    private static final int BASE_LIFETIME = 66;

    private final TextureAtlasSprite sprite;
    private final SingleQuadParticle.Layer layer;
    private final float uo;
    private final float vo;
    private final float baseAlpha;

    private TaintSplosionParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
        super(level, x, y, z, xd, yd, zd, sprite);
        this.sprite = sprite;
        this.layer = SingleQuadParticle.Layer.bySprite(sprite);
        this.uo = this.random.nextFloat() * 3.0F;
        this.vo = this.random.nextFloat() * 3.0F;
        this.gravity = 1.0F;
        this.quadSize = (this.random.nextFloat() * 0.5F + 0.5F) * 0.5F;
        if (this.random.nextBoolean()) {
            this.rCol = 0.6F;
            this.gCol = 0.0F;
            this.bCol = 0.3F;
            this.baseAlpha = 0.4F;
        } else {
            this.rCol = 0.3F;
            this.gCol = 0.0F;
            this.bCol = 0.3F;
            this.baseAlpha = 0.6F;
        }
        this.alpha = this.baseAlpha;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        boom();
        this.lifetime = (int) (BASE_LIFETIME / (this.random.nextFloat() * 0.9F + 0.1F));
    }

    private void boom() {
        float strength = (this.random.nextFloat() + this.random.nextFloat() + 1.0F) * 0.15F;
        float length = Mth.sqrt((float) (this.xd * this.xd + this.yd * this.yd + this.zd * this.zd));
        if (length < 1.0E-4F) {
            length = 1.0F;
        }
        this.xd = this.xd / length * strength * BOOM_MOTION_SCALE;
        this.yd = this.yd / length * strength * BOOM_MOTION_SCALE + BOOM_LIFT;
        this.zd = this.zd / length * strength * BOOM_MOTION_SCALE;
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = this.baseAlpha * (1.0F - (float) this.age / this.lifetime);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return this.layer;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.uo + 1.0F) / 4.0F);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.uo / 4.0F);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vo / 4.0F);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vo + 1.0F) / 4.0F);
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return this.quadSize * 0.2F;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final ItemStackRenderState scratchRenderState = new ItemStackRenderState();

        @Override
        public @Nullable Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random) {
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(scratchRenderState, new ItemStack(Items.SLIME_BALL), ItemDisplayContext.GROUND, level, null, 0);
            Material.Baked material = scratchRenderState.pickParticleMaterial(random);
            TextureAtlasSprite sprite = material != null ? material.sprite() : Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).missingSprite();
            return new TaintSplosionParticle(level, x, y, z, xd, yd, zd, sprite);
        }
    }
}
