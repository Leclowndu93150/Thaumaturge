package com.leclowndu93150.thaumaturge.compat.curio.client;

import com.leclowndu93150.thaumaturge.mixin.client.renderer.entity.layers.HumanoidArmorLayerAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class GoggleCurioRenderer implements ICurioRenderer {

    public GoggleCurioRenderer() {}

    @Override
    public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, S renderState, RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context, float yRotation, float xRotation) {
        if (renderLayerParent instanceof AvatarRenderer<?> avatarRenderer) {
            HumanoidArmorLayer<AvatarRenderState, ?, ?> layer = new HumanoidArmorLayer<>(avatarRenderer,
                    ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), (part) -> new PlayerModel(part, false)), context.getEquipmentRenderer());

            ((HumanoidArmorLayerAccessor) layer).invokeRenderArmorPiece(poseStack, submitNodeCollector, stack, EquipmentSlot.HEAD, packedLight, (AvatarRenderState) renderState);
        }
    }

    private record LayerTextureKey(EquipmentClientInfo.LayerType layerType, EquipmentClientInfo.Layer layer) {
    }
}
