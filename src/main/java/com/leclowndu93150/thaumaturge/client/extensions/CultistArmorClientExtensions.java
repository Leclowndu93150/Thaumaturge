package com.leclowndu93150.thaumaturge.client.extensions;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.gear.FortressArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.KnightArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.PraetorArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.RobeArmorModel;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class CultistArmorClientExtensions {
    private CultistArmorClientExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new CustomArmorExtension(KnightArmorModel::new, TTModelLayers.KNIGHT_ARMOR_HEAD, TTModelLayers.KNIGHT_ARMOR_CHEST, TTModelLayers.KNIGHT_ARMOR_LEGS),
                TTItems.CRIMSON_PLATE_HELM.get(), TTItems.CRIMSON_PLATE_CHEST.get(), TTItems.CRIMSON_PLATE_LEGS.get());
        event.registerItem(new CustomArmorExtension(RobeArmorModel::new, TTModelLayers.ROBE_ARMOR_HEAD, TTModelLayers.ROBE_ARMOR_CHEST, TTModelLayers.ROBE_ARMOR_LEGS), TTItems.CRIMSON_ROBE_HELM.get(),
                TTItems.CRIMSON_ROBE_CHEST.get(), TTItems.CRIMSON_ROBE_LEGS.get());
        event.registerItem(new CustomArmorExtension(RobeArmorModel::new, TTModelLayers.ROBE_ARMOR_HEAD, TTModelLayers.ROBE_ARMOR_CHEST, TTModelLayers.ROBE_ARMOR_LEGS), TTItems.VOID_ROBE_HELM.get(),
                TTItems.VOID_ROBE_CHEST.get(), TTItems.VOID_ROBE_LEGS.get());
        event.registerItem(new CustomArmorExtension(PraetorArmorModel::new, TTModelLayers.PRAETOR_ARMOR_HEAD, TTModelLayers.PRAETOR_ARMOR_CHEST, TTModelLayers.PRAETOR_ARMOR_LEGS),
                TTItems.CRIMSON_PRAETOR_HELM.get(), TTItems.CRIMSON_PRAETOR_CHEST.get(), TTItems.CRIMSON_PRAETOR_LEGS.get());
        event.registerItem(new CustomArmorExtension(FortressArmorModel::new, TTModelLayers.FORTRESS_ARMOR_HEAD, TTModelLayers.FORTRESS_ARMOR_CHEST, TTModelLayers.FORTRESS_ARMOR_LEGS),
                TTItems.FORTRESS_HELM.get(), TTItems.FORTRESS_CHEST.get(), TTItems.FORTRESS_LEGS.get());
    }

    private static final class CustomArmorExtension implements IClientItemExtensions {
        private final Function<ModelPart, HumanoidModel<HumanoidRenderState>> factory;
        private final ModelLayerLocation headLayer;
        private final ModelLayerLocation chestLayer;
        private final ModelLayerLocation legsLayer;
        private HumanoidModel<HumanoidRenderState> head;
        private HumanoidModel<HumanoidRenderState> chest;
        private HumanoidModel<HumanoidRenderState> legs;

        private CustomArmorExtension(Function<ModelPart, HumanoidModel<HumanoidRenderState>> factory, ModelLayerLocation headLayer, ModelLayerLocation chestLayer, ModelLayerLocation legsLayer) {
            this.factory = factory;
            this.headLayer = headLayer;
            this.chestLayer = chestLayer;
            this.legsLayer = legsLayer;
        }

        @Override
        public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
            if (layerType == EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS) {
                if (legs == null) {
                    legs = bake(legsLayer);
                }
                return legs;
            }
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
            if (equippable != null && equippable.slot() == EquipmentSlot.HEAD) {
                if (head == null) {
                    head = bake(headLayer);
                }
                return head;
            }
            if (chest == null) {
                chest = bake(chestLayer);
            }
            return chest;
        }

        private HumanoidModel<HumanoidRenderState> bake(ModelLayerLocation layer) {
            return factory.apply(Minecraft.getInstance().getEntityModels().bakeLayer(layer));
        }
    }
}
