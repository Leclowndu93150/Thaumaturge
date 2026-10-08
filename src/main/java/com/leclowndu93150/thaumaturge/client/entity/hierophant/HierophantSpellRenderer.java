package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.AbstractHierophantSpell;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantCrescent;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantNova;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityHierophantSigil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class HierophantSpellRenderer<T extends AbstractHierophantSpell> extends EntityRenderer<T> {
    public enum Shape {
        CRESCENT,
        SIGIL,
        NOVA
    }

    public static final class State {
        public float age;
        public float yaw;
        public float pitch;
        public float fade;
        public boolean left;
        public Vec3 direction = Vec3.ZERO;
    }

    private static final float FADE_TICKS = 5;
    private static final float SPRITE_RADIUS_SCALE = 1.18F;
    private static final double CULLING_MARGIN = 24;
    private final Shape shape;

    public HierophantSpellRenderer(EntityRendererProvider.Context context, Shape shape) {
        super(context);
        this.shape = shape;
    }

    private void extractRenderState(T entity, State state, float partialTick) {
        state.age = entity.age(partialTick);
        state.yaw = entity.getYRot();
        state.direction = entity.forward();
        state.pitch = (float) -Math.asin(state.direction.y);
        state.left = entity.isLeft();
        state.fade = Mth.clamp((entity.lifetime() - state.age) / FADE_TICKS, 0, 1);
    }

    @Override
    public void render(T entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource collector, int light) {
        super.render(entity, yaw, partialTick, pose, collector, light);
        State state = new State();
        extractRenderState(entity, state, partialTick);
        pose.pushPose();
        if (shape == Shape.CRESCENT) {
            final Vec3 displacement = state.direction.scale(state.age * EntityHierophantCrescent.SPEED);
            pose.translate(displacement.x, displacement.y, displacement.z);
            pose.mulPose(Axis.YP.rotationDegrees(-state.yaw));
            pose.mulPose(Axis.XP.rotation(state.pitch));
            pose.mulPose(Axis.ZP.rotationDegrees(state.left ? -12 : 12));
        }
        VertexConsumer buffer = collector.getBuffer(HierophantRenderTypes.SPELL);
        {
            switch (shape) {
                case CRESCENT ->
                    HierophantSprites.floor(
                            buffer,
                            pose.last().pose(),
                            EntityHierophantCrescent.radius(state.age) * SPRITE_RADIUS_SCALE,
                            0,
                            0,
                            state.fade,
                            state.left);
                case NOVA ->
                    HierophantSprites.floor(
                            buffer,
                            pose.last().pose(),
                            EntityHierophantNova.radius(state.age) * SPRITE_RADIUS_SCALE,
                            EntityHierophantNova.HEIGHT / 2,
                            2,
                            state.fade,
                            false);
                case SIGIL -> {
                    final boolean active = state.age >= EntityHierophantSigil.WARNING_TICKS;
                    final float intensity = active
                            ? state.fade
                            : 0.45F + 0.55F * Math.min(1, state.age / EntityHierophantSigil.WARNING_TICKS);
                    HierophantSprites.floor(
                            buffer,
                            pose.last().pose(),
                            EntityHierophantSigil.RADIUS * SPRITE_RADIUS_SCALE,
                            0,
                            1,
                            intensity,
                            false);
                    if (active) {
                        HierophantSprites.flame(
                                buffer,
                                pose.last().pose(),
                                EntityHierophantSigil.RADIUS,
                                EntityHierophantSigil.HEIGHT,
                                state.fade);
                    }
                }
            }
        }
        pose.popPose();
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        return entity.shouldRender(x, y, z)
                && frustum.isVisible(entity.getBoundingBox().inflate(CULLING_MARGIN));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return HierophantTextures.SPELLS;
    }
}
