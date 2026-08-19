package com.leclowndu93150.thaumaturge.compat.curio.client;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class CuriosityBandCurioRenderer implements ICurioRenderer {
    private static final Identifier TEXTURE = TCIds.rl("textures/item/curiosity_band_worn.png");
    private static final float HALF_WIDTH = 0.25F;
    private static final float TOP_Y = -0.5F;
    private static final float HEIGHT = 0.8125F;
    private static final float FACE_Z = -0.26F;
    private static final float HELMET_LIFT = 0.06F;

    @Override
    public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, S renderState, RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context, float yRotation, float xRotation) {
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> humanoid)) {
            return;
        }
        LivingEntity wearer = slotContext.entity();
        boolean helmeted = wearer != null && !wearer.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
        poseStack.pushPose();
        humanoid.head.translateAndRotate(poseStack);
        float z = FACE_Z - (helmeted ? HELMET_LIFT : 0.0F);
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE), (pose, buffer) -> {
            vertex(buffer, pose, -HALF_WIDTH, TOP_Y, z, 0.0F, 0.0F, packedLight);
            vertex(buffer, pose, -HALF_WIDTH, TOP_Y + HEIGHT, z, 0.0F, 1.0F, packedLight);
            vertex(buffer, pose, HALF_WIDTH, TOP_Y + HEIGHT, z, 1.0F, 1.0F, packedLight);
            vertex(buffer, pose, HALF_WIDTH, TOP_Y, z, 1.0F, 0.0F, packedLight);
        });
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light) {
        buffer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}
