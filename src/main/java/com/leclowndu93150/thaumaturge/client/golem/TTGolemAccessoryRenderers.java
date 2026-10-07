package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryAnchor;
import com.leclowndu93150.thaumaturge.api.client.golems.RegisterGolemAccessoryRenderersEvent;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.registry.TTGolemAccessories;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTGolemAccessoryRenderers {
    private TTGolemAccessoryRenderers() {}

    @SubscribeEvent
    public static void onRegisterRenderers(RegisterGolemAccessoryRenderersEvent event) {
        register(event, TTGolemAccessories.FEZ, GolemAccessoryAnchor.HEAD);
        register(event, TTGolemAccessories.TOP_HAT, GolemAccessoryAnchor.HEAD);
        register(event, TTGolemAccessories.GLASSES, GolemAccessoryAnchor.HEAD);
        register(event, TTGolemAccessories.VISOR, GolemAccessoryAnchor.HEAD);
        register(event, TTGolemAccessories.BOWTIE, GolemAccessoryAnchor.BODY);
    }

    private static void register(
            RegisterGolemAccessoryRenderersEvent event, GolemAccessory accessory, GolemAccessoryAnchor anchor) {
        ResourceLocation mesh =
                TTIds.rl("models/mesh/golem_accessory_" + accessory.id().getPath() + ".ttmesh");
        event.register(
                accessory,
                anchor,
                (pose, collector, context) -> GolemEquipmentRenderer.render(mesh, pose, collector, context));
    }
}
