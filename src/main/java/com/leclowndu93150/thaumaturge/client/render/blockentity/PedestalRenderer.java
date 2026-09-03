package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class PedestalRenderer<T extends BlockEntityPedestal> implements BlockEntityRenderer<T> {
    private static final float ITEM_SCALE = 1.25F;
    private static final float FULL_HEIGHT = 1.0F;
    private static final float ANCIENT_AND_ELDRITCH_HEIGHT = 0.75F;
    private static final float SPIN_DEGREES_PER_TICK = 1.0F;
    private static final float VOXEL = 1.0F / 16.0F;

    private final float itemScale;

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        this(context, ITEM_SCALE);
    }

    public PedestalRenderer(BlockEntityRendererProvider.Context context, float itemScale) {
        this.itemScale = itemScale;
    }

    @Override
    public void render(
            T pedestal, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = pedestal.getItem();
        if (stack.isEmpty()) {
            return;
        }
        float groundLift = LegacyItemLift.bottomLift(stack, ItemDisplayContext.GROUND) + VOXEL;
        var viewEntity = Minecraft.getInstance().getCameraEntity();
        float ticks = viewEntity == null ? partialTick : viewEntity.tickCount + partialTick;
        float spin = ticks % 360.0F * SPIN_DEGREES_PER_TICK;
        poseStack.pushPose();
        poseStack.translate(0.5F, pedestalHeight(pedestal), 0.5F);
        poseStack.scale(itemScale, itemScale, itemScale);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.translate(0.0F, groundLift, 0.0F);
        int seed = HashCommon.long2int(pedestal.getBlockPos().asLong());
        ItemRenderHelper.render(stack, ItemDisplayContext.GROUND, poseStack, buffers, light, overlay, seed);
        poseStack.popPose();
    }

    private static float pedestalHeight(BlockEntityPedestal pedestal) {
        var state = pedestal.getBlockState();
        return state.is(TCBlocks.PEDESTAL_ANCIENT.get()) || state.is(TCBlocks.PEDESTAL_ELDRITCH.get())
                ? ANCIENT_AND_ELDRITCH_HEIGHT
                : FULL_HEIGHT;
    }
}
