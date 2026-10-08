package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.eldritch.ReliquaryViewHolder;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.BlockEldritchReliquary;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.BlockEntityEldritchReliquary;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryRole;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryView;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class EldritchReliquaryRenderer implements BlockEntityRenderer<BlockEntityEldritchReliquary> {
    private static final ResourceLocation GLOW_SPRITE = TTIds.rl("block/eldritch_crust_glowing");
    private static final ResourceLocation VEIL_SPRITE = TTIds.rl("block/eldritch_door");
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float ALCOVE_MIN_X = 4.0F * PIXEL;
    private static final float ALCOVE_MAX_X = 12.0F * PIXEL;
    private static final float ALCOVE_MIN_Y = 4.0F * PIXEL;
    private static final float ALCOVE_MAX_Y = 13.0F * PIXEL;
    private static final float SURFACE_OFFSET = 0.002F;
    private static final float BACK_Z = 8.0F * PIXEL - SURFACE_OFFSET;
    private static final float MOUTH_Z = 3.0F * PIXEL - SURFACE_OFFSET;
    private static final float REWARD_X = 0.5F;
    private static final float REWARD_Y = 6.5F * PIXEL;
    private static final float REWARD_Z = 5.5F * PIXEL;
    private static final float REWARD_SCALE = 0.75F;
    private static final float BOB_HEIGHT = 0.03F;
    private static final float BOB_PERIOD = 20.0F;
    private static final float SPIN_PER_TICK = 1.5F;
    private static final float FULL_TURN = 360.0F;
    private static final int WHITE = 0xFFFFFFFF;

    private final Map<ReliquaryRole, ItemStack> rewards = new EnumMap<>(ReliquaryRole.class);

    public EldritchReliquaryRenderer(BlockEntityRendererProvider.Context context) {}

    private void extractRenderState(
            BlockEntityEldritchReliquary reliquary, EldritchReliquaryRenderState state, float partialTicks) {
        state.facing = reliquary.getBlockState().getValue(BlockEldritchReliquary.FACING);
        state.view = ReliquaryViewHolder.get(reliquary.getBlockPos());
        var viewEntity = Minecraft.getInstance().getCameraEntity();
        state.ticks = viewEntity == null ? partialTicks : viewEntity.tickCount + partialTicks;
        if (!state.view.showsReward()) {
            state.reward = null;
            return;
        }
        state.reward = rewards.computeIfAbsent(reliquary.role(), EldritchReliquaryRenderer::rewardFor);
        state.rewardLift = LegacyItemLift.centerLift(state.reward, ItemDisplayContext.GROUND);
    }

    private static ItemStack rewardFor(ReliquaryRole role) {
        return switch (role) {
            case KEY_ROOM -> new ItemStack(TTItems.RUNED_TABLET.get());
            case BOSS -> new ItemStack(TTItems.LOOT_BAG_RARE.get());
            case CACHE -> new ItemStack(TTItems.LOOT_BAG_UNCOMMON.get());
        };
    }

    @Override
    public void render(
            BlockEntityEldritchReliquary reliquary,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource collector,
            int light,
            int overlay) {
        EldritchReliquaryRenderState state = new EldritchReliquaryRenderState();
        extractRenderState(reliquary, state, partialTicks);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.getOpposite().toYRot()));
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        if (state.view == ReliquaryView.CLAIMABLE) {
            submitPanel(
                    poseStack,
                    collector,
                    Minecraft.getInstance()
                            .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                            .apply(GLOW_SPRITE),
                    BACK_Z);
        }
        if (state.view == ReliquaryView.WARDED) {
            submitPanel(
                    poseStack,
                    collector,
                    Minecraft.getInstance()
                            .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                            .apply(VEIL_SPRITE),
                    MOUTH_Z);
        }
        if (state.reward != null) {
            poseStack.pushPose();
            poseStack.translate(REWARD_X, REWARD_Y + Mth.sin(state.ticks / BOB_PERIOD) * BOB_HEIGHT, REWARD_Z);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.ticks * SPIN_PER_TICK % FULL_TURN));
            poseStack.scale(REWARD_SCALE, REWARD_SCALE, REWARD_SCALE);
            poseStack.translate(0.0F, state.rewardLift, 0.0F);
            Minecraft.getInstance()
                    .getItemRenderer()
                    .renderStatic(
                            state.reward,
                            ItemDisplayContext.GROUND,
                            LightTexture.FULL_BRIGHT,
                            OverlayTexture.NO_OVERLAY,
                            poseStack,
                            collector,
                            reliquary.getLevel(),
                            0);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void submitPanel(
            PoseStack poseStack, MultiBufferSource collector, TextureAtlasSprite sprite, float z) {
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = collector.getBuffer(Sheets.cutoutBlockSheet());
        {
            VertexConsumer wrapped = sprite.wrap(buffer);
            float u0 = 16.0F * PIXEL - ALCOVE_MAX_X;
            float u1 = 16.0F * PIXEL - ALCOVE_MIN_X;
            float v0 = 16.0F * PIXEL - ALCOVE_MAX_Y;
            float v1 = 16.0F * PIXEL - ALCOVE_MIN_Y;
            vertex(wrapped, pose, ALCOVE_MIN_X, ALCOVE_MIN_Y, z, u1, v1);
            vertex(wrapped, pose, ALCOVE_MIN_X, ALCOVE_MAX_Y, z, u1, v0);
            vertex(wrapped, pose, ALCOVE_MAX_X, ALCOVE_MAX_Y, z, u0, v0);
            vertex(wrapped, pose, ALCOVE_MAX_X, ALCOVE_MIN_Y, z, u0, v1);
        }
    }

    private static void vertex(
            VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v) {
        buffer.addVertex(pose, x, y, z)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 0.0F, -1.0F)
                .setColor(WHITE);
    }
}
