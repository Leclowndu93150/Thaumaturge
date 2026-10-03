package com.leclowndu93150.thaumaturge.client.extensions;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.entity.TCModelLayers;
import com.leclowndu93150.thaumaturge.client.model.gear.ThaumostaticHarnessModel;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ThaumostaticHarnessClientExtensions {
    private ThaumostaticHarnessClientExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new HarnessExtension(), TCItems.THAUMOSTATIC_HARNESS.get());
    }

    private static final class HarnessExtension implements IClientItemExtensions {
        private ThaumostaticHarnessModel model;

        @Override
        public Model getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
            if (model == null) {
                model = new ThaumostaticHarnessModel(Minecraft.getInstance().getEntityModels().bakeLayer(TCModelLayers.THAUMOSTATIC_HARNESS));
            }
            return model;
        }
    }
}
