package com.leclowndu93150.thaumaturge.client.extensions;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.entity.TTModelLayers;
import com.leclowndu93150.thaumaturge.client.model.gear.ThaumostaticHarnessModel;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class ThaumostaticHarnessClientExtensions {
    private ThaumostaticHarnessClientExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new HarnessExtension(), TTItems.THAUMOSTATIC_HARNESS.get());
    }

    private static final class HarnessExtension implements IClientItemExtensions {
        private ThaumostaticHarnessModel model;

        @Override
        public HumanoidModel<?> getHumanoidArmorModel(
                LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
            if (model == null) {
                model = new ThaumostaticHarnessModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(TTModelLayers.THAUMOSTATIC_HARNESS));
            }
            return model;
        }
    }
}
