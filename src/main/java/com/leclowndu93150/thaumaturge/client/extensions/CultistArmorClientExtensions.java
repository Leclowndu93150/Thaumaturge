package com.leclowndu93150.thaumaturge.client.extensions;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.gear.FortressArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.KnightArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.RobeArmorModel;
import com.leclowndu93150.thaumaturge.content.equipment.FortressArmorItem;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class CultistArmorClientExtensions {
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private CultistArmorClientExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(
                new CustomArmorExtension(
                        KnightArmorModel::new,
                        TCModelLayers.KNIGHT_ARMOR_HEAD,
                        TCModelLayers.KNIGHT_ARMOR_CHEST,
                        TCModelLayers.KNIGHT_ARMOR_LEGS),
                TCItems.CRIMSON_PRAETOR_HELM.get(),
                TCItems.CRIMSON_PRAETOR_CHEST.get(),
                TCItems.CRIMSON_PRAETOR_LEGS.get());
        event.registerItem(
                new CustomArmorExtension(
                        KnightArmorModel::new,
                        TCModelLayers.KNIGHT_ARMOR_HEAD,
                        TCModelLayers.KNIGHT_ARMOR_CHEST,
                        TCModelLayers.KNIGHT_ARMOR_LEGS),
                TCItems.CRIMSON_PLATE_HELM.get(),
                TCItems.CRIMSON_PLATE_CHEST.get(),
                TCItems.CRIMSON_PLATE_LEGS.get());
        event.registerItem(
                new CustomArmorExtension(
                        RobeArmorModel::new,
                        TCModelLayers.ROBE_ARMOR_HEAD,
                        TCModelLayers.ROBE_ARMOR_CHEST,
                        TCModelLayers.ROBE_ARMOR_LEGS),
                TCItems.CRIMSON_ROBE_HELM.get(),
                TCItems.CRIMSON_ROBE_CHEST.get(),
                TCItems.CRIMSON_ROBE_LEGS.get());
        event.registerItem(
                new CustomArmorExtension(
                        RobeArmorModel::new,
                        TCModelLayers.ROBE_ARMOR_HEAD,
                        TCModelLayers.ROBE_ARMOR_CHEST,
                        TCModelLayers.ROBE_ARMOR_LEGS),
                TCItems.VOID_ROBE_HELM.get(),
                TCItems.VOID_ROBE_CHEST.get(),
                TCItems.VOID_ROBE_LEGS.get());
        event.registerItem(
                new FortressArmorExtension(),
                TCItems.FORTRESS_HELM.get(),
                TCItems.FORTRESS_CHEST.get(),
                TCItems.FORTRESS_LEGS.get());
    }

    private static final class FortressArmorExtension implements IClientItemExtensions {
        private FortressArmorModel head;
        private FortressArmorModel chest;
        private FortressArmorModel legs;

        @Override
        public HumanoidModel<?> getHumanoidArmorModel(
                LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            if (slot == EquipmentSlot.LEGS) {
                if (legs == null) {
                    legs = bake(TCModelLayers.FORTRESS_ARMOR_LEGS);
                }
                return legs;
            }
            if (slot == EquipmentSlot.HEAD) {
                if (head == null) {
                    head = bake(TCModelLayers.FORTRESS_ARMOR_HEAD);
                }
                head.setMask(FortressArmorItem.mask(stack));
                head.setGogglesVisible(FortressArmorItem.hasGoggles(stack));
                return head;
            }
            if (chest == null) {
                chest = bake(TCModelLayers.FORTRESS_ARMOR_CHEST);
            }
            return chest;
        }

        @Override
        public void setupModelAnimations(
                LivingEntity entity,
                ItemStack stack,
                EquipmentSlot slot,
                Model model,
                float limbSwing,
                float limbSwingAmount,
                float partialTick,
                float ageInTicks,
                float netHeadYaw,
                float headPitch) {
            applyArmorStandHeadPose(entity, slot, model);
        }

        private FortressArmorModel bake(ModelLayerLocation layer) {
            return new FortressArmorModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(layer));
        }
    }

    private static final class CustomArmorExtension implements IClientItemExtensions {
        private final Function<ModelPart, HumanoidModel<LivingEntity>> factory;
        private final ModelLayerLocation headLayer;
        private final ModelLayerLocation chestLayer;
        private final ModelLayerLocation legsLayer;
        private HumanoidModel<LivingEntity> head;
        private HumanoidModel<LivingEntity> chest;
        private HumanoidModel<LivingEntity> legs;

        private CustomArmorExtension(
                Function<ModelPart, HumanoidModel<LivingEntity>> factory,
                ModelLayerLocation headLayer,
                ModelLayerLocation chestLayer,
                ModelLayerLocation legsLayer) {
            this.factory = factory;
            this.headLayer = headLayer;
            this.chestLayer = chestLayer;
            this.legsLayer = legsLayer;
        }

        @Override
        public HumanoidModel<?> getHumanoidArmorModel(
                LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            if (slot == EquipmentSlot.LEGS) {
                if (legs == null) {
                    legs = bake(legsLayer);
                }
                return legs;
            }
            if (slot == EquipmentSlot.HEAD) {
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

        @Override
        public void setupModelAnimations(
                LivingEntity entity,
                ItemStack stack,
                EquipmentSlot slot,
                Model model,
                float limbSwing,
                float limbSwingAmount,
                float partialTick,
                float ageInTicks,
                float netHeadYaw,
                float headPitch) {
            applyArmorStandHeadPose(entity, slot, model);
        }

        private HumanoidModel<LivingEntity> bake(ModelLayerLocation layer) {
            return factory.apply(Minecraft.getInstance().getEntityModels().bakeLayer(layer));
        }
    }

    private static void applyArmorStandHeadPose(LivingEntity entity, EquipmentSlot slot, Model model) {
        if (!(entity instanceof ArmorStand stand)
                || slot != EquipmentSlot.HEAD
                || !(model instanceof HumanoidModel<?> humanoid)) {
            return;
        }
        humanoid.head.xRot = stand.getHeadPose().getX() * DEG_TO_RAD;
        humanoid.head.yRot = stand.getHeadPose().getY() * DEG_TO_RAD;
        humanoid.head.zRot = stand.getHeadPose().getZ() * DEG_TO_RAD;
        humanoid.hat.copyFrom(humanoid.head);
    }
}
