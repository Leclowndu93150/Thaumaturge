package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.DeconTableModel;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import com.leclowndu93150.thaumaturge.client.render.aspect.StripUv;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public final class DeconstructionTableRenderer implements BlockEntityRenderer<BlockEntityDeconstructionTable> {
    private static final ResourceLocation TABLE_TEXTURE = TTIds.rl("textures/entity/decontable.png");

    private static final float TABLE_Y = 1.0F;
    private static final float BOOK_Y = TABLE_Y + 0.068F;
    private static final float BOOK_SCALE = 0.85F;
    private static final float BOOK_YAW = 20.0F;
    private static final float INPUT_Y = 1.42F;
    private static final float ASPECT_Y = TABLE_Y + 0.03F;
    private static final float ASPECT_SCALE = 0.44F;
    private static final float ASPECT_ALPHA = 0.82F;
    private static final float IN_FRAME_SCALE = 0.5128205F;
    private static final float ITEM_SPIN_PER_TICK = 1.0F;
    private static final float GLOW_SIZE = 0.62F;
    private static final float GLOW_HALF = 0.5F;
    private static final int GLOW_RGB = 0xE4D6FF;
    private static final int GLOW_FRAME = 0;

    private final DeconTableModel model;
    private ItemStack cachedLiftItem = ItemStack.EMPTY;
    private float cachedInputLift = LegacyItemLift.LEGACY_CENTER_Y;

    public DeconstructionTableRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new DeconTableModel(context.bakeLayer(TTModelLayers.DECONSTRUCTION_TABLE));
    }

    @Override
    public void render(
            BlockEntityDeconstructionTable table,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        if (table.getLevel() == null) {
            return;
        }
        float ticks = (table.getLevel().getGameTime() % 360L) + partialTick;
        ItemStack book = new ItemStack(TTItems.THAUMOMETER.get());
        ItemStack input = table.items().getStackInSlot(BlockEntityDeconstructionTable.SLOT_INPUT);
        Holder<IAspect> aspect = resolveAspect(table);

        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        model.root.render(poseStack, buffers.getBuffer(RenderType.entityCutout(TABLE_TEXTURE)), light, overlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5F, BOOK_Y, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(BOOK_YAW));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(BOOK_SCALE, BOOK_SCALE, BOOK_SCALE);
        ItemRenderHelper.render(book, ItemDisplayContext.FIXED, poseStack, buffers, light, overlay, 0);
        poseStack.popPose();

        if (!input.isEmpty()) {
            submitInput(input, ticks, poseStack, buffers, light, overlay);
        }

        if (aspect != null) {
            poseStack.pushPose();
            poseStack.translate(0.5F, ASPECT_Y, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(DeconstructionTableRenderMath.aspectSpin(ticks)));
            poseStack.scale(ASPECT_SCALE, ASPECT_SCALE, ASPECT_SCALE);
            AspectTagWorldRenderer.renderQuad(
                    poseStack.last(),
                    buffers.getBuffer(
                            RenderType.entityTranslucent(aspect.value().texture())),
                    aspect,
                    ASPECT_ALPHA,
                    false,
                    light);
            poseStack.popPose();
        }
    }

    private void submitInput(
            ItemStack input, float ticks, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        float bob = DeconstructionTableRenderMath.itemBob(ticks);
        float centreY = INPUT_Y + bob;
        poseStack.pushPose();
        poseStack.translate(0.5F, centreY, 0.5F);
        poseStack.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        poseStack.scale(GLOW_SIZE, GLOW_SIZE, GLOW_SIZE);
        float alpha = DeconstructionTableRenderMath.glowAlpha(ticks);
        int tint = ARGB32.color(Math.round(alpha * 255.0F), GLOW_RGB);
        writeGlow(
                buffers.getBuffer(TTRenderTypes.fxAlphaAdditiveBlurred(ParticleTextures.ORB_GLOW)),
                poseStack.last().pose(),
                tint);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5F, centreY - LegacyItemLift.LEGACY_CENTER_Y * IN_FRAME_SCALE, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(ticks * ITEM_SPIN_PER_TICK));
        poseStack.scale(IN_FRAME_SCALE, IN_FRAME_SCALE, IN_FRAME_SCALE);
        poseStack.translate(0.0F, inputCenterLift(input), 0.0F);
        ItemRenderHelper.render(
                input, ItemDisplayContext.GROUND, poseStack, buffers, LightTexture.FULL_BRIGHT, overlay, 0);
        poseStack.popPose();
    }

    private float inputCenterLift(ItemStack input) {
        if (cachedLiftItem.isEmpty() || !ItemStack.isSameItemSameComponents(cachedLiftItem, input)) {
            cachedLiftItem = input.copy();
            cachedInputLift = LegacyItemLift.centerLift(input, ItemDisplayContext.GROUND);
        }
        return cachedInputLift;
    }

    private static void writeGlow(VertexConsumer buffer, Matrix4f matrix, int tint) {
        float u0 = StripUv.u0(GLOW_FRAME, ParticleTextures.ORB_GLOW_FRAMES);
        float u1 = StripUv.u1(GLOW_FRAME, ParticleTextures.ORB_GLOW_FRAMES);
        buffer.addVertex(matrix, -GLOW_HALF, -GLOW_HALF, 0.0F)
                .setUv(u0, StripUv.V1)
                .setColor(tint)
                .setLight(LightTexture.FULL_BRIGHT);
        buffer.addVertex(matrix, GLOW_HALF, -GLOW_HALF, 0.0F)
                .setUv(u1, StripUv.V1)
                .setColor(tint)
                .setLight(LightTexture.FULL_BRIGHT);
        buffer.addVertex(matrix, GLOW_HALF, GLOW_HALF, 0.0F)
                .setUv(u1, StripUv.V0)
                .setColor(tint)
                .setLight(LightTexture.FULL_BRIGHT);
        buffer.addVertex(matrix, -GLOW_HALF, GLOW_HALF, 0.0F)
                .setUv(u0, StripUv.V0)
                .setColor(tint)
                .setLight(LightTexture.FULL_BRIGHT);
    }

    private static @Nullable Holder<IAspect> resolveAspect(BlockEntityDeconstructionTable table) {
        ResourceLocation id = table.resultAspect();
        if (id == null || table.getLevel() == null) {
            return null;
        }
        return table.getLevel()
                .registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .get(ResourceKey.create(IAspect.REGISTRY_KEY, id))
                .map(holder -> (Holder<IAspect>) holder)
                .orElse(null);
    }
}
