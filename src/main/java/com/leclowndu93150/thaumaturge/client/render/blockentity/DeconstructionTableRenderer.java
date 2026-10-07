package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.entity.DeconTableModel;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagWorldRenderer;
import com.leclowndu93150.thaumaturge.content.research.decon.BlockEntityDeconstructionTable;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class DeconstructionTableRenderer implements BlockEntityRenderer<BlockEntityDeconstructionTable> {
    private static final ResourceLocation TABLE_TEXTURE = TTIds.rl("textures/entity/decontable.png");

    private static final float BOOK_Y = 1.02F;
    private static final float BOOK_SCALE = 0.8F;
    private static final float INPUT_Y = 1.15F;
    private static final float ASPECT_Y = 1.081F;
    private static final float ASPECT_SCALE = 0.384F;
    private static final float ASPECT_ALPHA = 0.8F;
    private static final float IN_FRAME_SCALE = 0.5128205F;
    private static final float IN_FRAME_DROP = -0.05F;
    private static final float FLAT_ITEM_LIFT = 0.125F;

    private final DeconTableModel model;

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
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        poseStack.scale(BOOK_SCALE, BOOK_SCALE, BOOK_SCALE);
        poseStack.scale(IN_FRAME_SCALE, IN_FRAME_SCALE, IN_FRAME_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        ItemRenderHelper.render(book, ItemDisplayContext.FIXED, poseStack, buffers, light, overlay, 0);
        poseStack.popPose();

        if (!input.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.5F, INPUT_Y, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(ticks % 360.0F));
            poseStack.scale(IN_FRAME_SCALE, IN_FRAME_SCALE, IN_FRAME_SCALE);
            poseStack.translate(0.0F, IN_FRAME_DROP + FLAT_ITEM_LIFT, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            ItemRenderHelper.render(input, ItemDisplayContext.FIXED, poseStack, buffers, light, overlay, 0);
            poseStack.popPose();
        }

        if (aspect != null) {
            poseStack.pushPose();
            poseStack.translate(0.5F, ASPECT_Y, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(ticks % 360.0F));
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
