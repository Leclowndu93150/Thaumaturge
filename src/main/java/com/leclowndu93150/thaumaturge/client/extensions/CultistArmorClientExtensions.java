package com.leclowndu93150.thaumaturge.client.extensions;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.gear.ClericArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.CrimsonBootsModel;
import com.leclowndu93150.thaumaturge.client.model.gear.FortressArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.KnightArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.PraetorArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.RangerArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.RobeArmorModel;
import com.leclowndu93150.thaumaturge.registry.TTItems;
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

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class CultistArmorClientExtensions {
    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private CultistArmorClientExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(
                new CustomArmorExtension(
                        PraetorArmorModel::new,
                        TTModelLayers.PRAETOR_ARMOR_HEAD,
                        TTModelLayers.PRAETOR_ARMOR_CHEST,
                        TTModelLayers.PRAETOR_ARMOR_LEGS),
                TTItems.CRIMSON_PRAETOR_HELM.get(),
                TTItems.CRIMSON_PRAETOR_CHEST.get(),
                TTItems.CRIMSON_PRAETOR_LEGS.get());
        event.registerItem(
                new CustomArmorExtension(
                        KnightArmorModel::new,
                        TTModelLayers.KNIGHT_ARMOR_HEAD,
                        TTModelLayers.KNIGHT_ARMOR_CHEST,
                        TTModelLayers.KNIGHT_ARMOR_LEGS),
                TTItems.CRIMSON_PLATE_HELM.get(),
                TTItems.CRIMSON_PLATE_CHEST.get(),
                TTItems.CRIMSON_PLATE_LEGS.get());
        event.registerItem(
                new CustomArmorExtension(
                        RangerArmorModel::new,
                        TTModelLayers.RANGER_ARMOR_HEAD,
                        TTModelLayers.RANGER_ARMOR_CHEST,
                        TTModelLayers.RANGER_ARMOR_LEGS),
                TTItems.CRIMSON_RANGER_HELM.get(),
                TTItems.CRIMSON_RANGER_CHEST.get(),
                TTItems.CRIMSON_RANGER_LEGS.get());
        event.registerItem(
                new CustomArmorExtension(
                        RangerArmorModel::new,
                        TTModelLayers.CAPTAIN_ARMOR_HEAD,
                        TTModelLayers.CAPTAIN_ARMOR_CHEST,
                        TTModelLayers.RANGER_ARMOR_LEGS),
                TTItems.CRIMSON_CAPTAIN_HELM.get(),
                TTItems.CRIMSON_CAPTAIN_CHEST.get());
        event.registerItem(
                new CustomArmorExtension(
                        ClericArmorModel::new,
                        TTModelLayers.CLERIC_ARMOR_HEAD,
                        TTModelLayers.CLERIC_ARMOR_CHEST,
                        TTModelLayers.CLERIC_ARMOR_LEGS),
                TTItems.CRIMSON_ROBE_HELM.get(),
                TTItems.CRIMSON_ROBE_CHEST.get(),
                TTItems.CRIMSON_ROBE_LEGS.get());
        event.registerItem(
                new SingleLayerArmorExtension(CrimsonBootsModel::new, TTModelLayers.CRIMSON_BOOTS),
                TTItems.CRIMSON_BOOTS.get());
        event.registerItem(
                new CustomArmorExtension(
                        RobeArmorModel::new,
                        TTModelLayers.ROBE_ARMOR_HEAD,
                        TTModelLayers.ROBE_ARMOR_CHEST,
                        TTModelLayers.ROBE_ARMOR_LEGS,
                        true),
                TTItems.VOID_ROBE_HELM.get(),
                TTItems.VOID_ROBE_CHEST.get(),
                TTItems.VOID_ROBE_LEGS.get());
        event.registerItem(
                new CustomArmorExtension(
                        FortressArmorModel::new,
                        TTModelLayers.FORTRESS_ARMOR_HEAD,
                        TTModelLayers.FORTRESS_ARMOR_CHEST,
                        TTModelLayers.FORTRESS_ARMOR_LEGS),
                TTItems.FORTRESS_HELM.get(),
                TTItems.FORTRESS_CHEST.get(),
                TTItems.FORTRESS_LEGS.get());
        event.registerItem(
                new RobeArmorClientExtension(),
                TTItems.CLOTH_CHEST.get(),
                TTItems.CLOTH_LEGS.get(),
                TTItems.CLOTH_BOOTS.get());
    }

    private static final class CustomArmorExtension implements IClientItemExtensions {
        private final Function<ModelPart, HumanoidModel<LivingEntity>> factory;
        private final ModelLayerLocation headLayer;
        private final ModelLayerLocation chestLayer;
        private final ModelLayerLocation legsLayer;
        private final boolean robeDyeable;
        private HumanoidModel<LivingEntity> head;
        private HumanoidModel<LivingEntity> chest;
        private HumanoidModel<LivingEntity> legs;

        private CustomArmorExtension(
                Function<ModelPart, HumanoidModel<LivingEntity>> factory,
                ModelLayerLocation headLayer,
                ModelLayerLocation chestLayer,
                ModelLayerLocation legsLayer,
                boolean robeDyeable) {
            this.factory = factory;
            this.headLayer = headLayer;
            this.chestLayer = chestLayer;
            this.legsLayer = legsLayer;
            this.robeDyeable = robeDyeable;
        }

        private CustomArmorExtension(
                Function<ModelPart, HumanoidModel<LivingEntity>> factory,
                ModelLayerLocation headLayer,
                ModelLayerLocation chestLayer,
                ModelLayerLocation legsLayer) {
            this(factory, headLayer, chestLayer, legsLayer, false);
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
        public int getDefaultDyeColor(ItemStack stack) {
            return robeDyeable
                    ? RobeArmorClientExtension.DEFAULT_DYE_COLOR
                    : IClientItemExtensions.super.getDefaultDyeColor(stack);
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
            if (model instanceof com.leclowndu93150.thaumaturge.client.model.gear.AbstractTTArmorModel armor) {
                armor.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            }
            applyArmorStandHeadPose(entity, slot, model);
        }

        private HumanoidModel<LivingEntity> bake(ModelLayerLocation layer) {
            return factory.apply(Minecraft.getInstance().getEntityModels().bakeLayer(layer));
        }
    }

    private static final class SingleLayerArmorExtension implements IClientItemExtensions {
        private final Function<ModelPart, HumanoidModel<LivingEntity>> factory;
        private final ModelLayerLocation layer;
        private HumanoidModel<LivingEntity> model;

        private SingleLayerArmorExtension(
                Function<ModelPart, HumanoidModel<LivingEntity>> factory, ModelLayerLocation layer) {
            this.factory = factory;
            this.layer = layer;
        }

        @Override
        public HumanoidModel<?> getHumanoidArmorModel(
                LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            if (slot != EquipmentSlot.FEET) {
                return original;
            }
            if (model == null) {
                model = factory.apply(Minecraft.getInstance().getEntityModels().bakeLayer(layer));
            }
            return model;
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
            if (model instanceof com.leclowndu93150.thaumaturge.client.model.gear.AbstractTTArmorModel armor) {
                armor.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            }
            applyArmorStandHeadPose(entity, slot, model);
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
