package com.leclowndu93150.thaumaturge.compat.curio.client;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public final class GoggleCurioRenderer implements ICurioRenderer {

    private static final ResourceLocation GOGGLES_TEXTURE =
            TTIds.rl("textures/models/armor/goggles_revealing_layer_1.png");

    private HumanoidModel<LivingEntity> armorModel;

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> renderLayerParent,
            MultiBufferSource buffers,
            int light,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?>)) {
            return;
        }
        LivingEntity wearer = slotContext.entity();
        if (wearer == null) {
            return;
        }
        List<ResourceLocation> textures =
                stack.getItem() instanceof ArmorItem armorItem ? armorTextures(armorItem) : List.of(GOGGLES_TEXTURE);
        HumanoidModel<LivingEntity> model = armorModel();
        ICurioRenderer.followBodyRotations(wearer, model);
        model.setAllVisible(false);
        model.head.visible = true;
        model.hat.visible = true;
        for (ResourceLocation texture : textures) {
            VertexConsumer buffer = buffers.getBuffer(RenderType.armorCutoutNoCull(texture));
            model.renderToBuffer(poseStack, buffer, light, OverlayTexture.NO_OVERLAY, -1);
        }
    }

    private static List<ResourceLocation> armorTextures(ArmorItem armorItem) {
        ArmorMaterial material = armorItem.getMaterial().value();
        return material.layers().stream().map(layer -> layer.texture(false)).toList();
    }

    private HumanoidModel<LivingEntity> armorModel() {
        if (armorModel == null) {
            ModelPart part = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR);
            armorModel = new HumanoidModel<>(part);
        }
        return armorModel;
    }
}
