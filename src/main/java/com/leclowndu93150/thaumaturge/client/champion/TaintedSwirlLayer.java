package com.leclowndu93150.thaumaturge.client.champion;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionModifier;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class TaintedSwirlLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TAINT_FIBRES =
            ResourceLocation.fromNamespaceAndPath(TCIds.MODID, "textures/models/taint_fibres.png");
    private static final float ALPHA = 0.66F;
    private static final int COLOR = ARGB32.colorFromFloat(ALPHA, 1.0F, 1.0F, 1.0F);
    private static final float U_FREQUENCY = 2.5E-4F;
    private static final float V_FACTOR = 0.001F;

    private static final Function<Key, RenderType> SWIRL = Util.memoize(TaintedSwirlLayer::create);

    public TaintedSwirlLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        if (!(entity instanceof ITaintedMob) && ChampionHelper.championType(entity) != ChampionModifier.TAINTED) {
            return;
        }
        int id = entity.getId();
        RenderType type = SWIRL.apply(new Key(swirlU(id), swirlV(id), entity.isInvisible()));
        VertexConsumer buffer = buffers.getBuffer(type);
        this.getParentModel().renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, COLOR);
    }

    private record Key(float u, float v, boolean invisible) {}

    private static RenderType create(Key key) {
        return RenderType.create(
                "thaumaturge_tainted_swirl",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                1536,
                false,
                true,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_ENERGY_SWIRL_SHADER)
                        .setTextureState(new RenderStateShard.TextureStateShard(TAINT_FIBRES, false, false))
                        .setTexturingState(new RenderStateShard.OffsetTexturingStateShard(key.u, key.v))
                        .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                        .setCullState(RenderStateShard.NO_CULL)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(
                                key.invisible ? RenderStateShard.COLOR_WRITE : RenderStateShard.COLOR_DEPTH_WRITE)
                        .createCompositeState(false));
    }

    static float swirlU(int entityId) {
        return Mth.cos(entityId * U_FREQUENCY);
    }

    static float swirlV(int entityId) {
        return entityId * V_FACTOR;
    }
}
