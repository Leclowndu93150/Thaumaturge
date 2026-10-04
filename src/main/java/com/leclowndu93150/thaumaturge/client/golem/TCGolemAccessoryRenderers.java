package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemAccessoryAnchor;
import com.leclowndu93150.thaumaturge.api.client.golems.RegisterGolemAccessoryRenderersEvent;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.registry.TCGolemAccessories;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCGolemAccessoryRenderers {
    private TCGolemAccessoryRenderers() {}

    @SubscribeEvent
    public static void onRegisterRenderers(RegisterGolemAccessoryRenderersEvent event) {
        register(event, TCGolemAccessories.FEZ, GolemAccessoryAnchor.HEAD);
        register(event, TCGolemAccessories.TOP_HAT, GolemAccessoryAnchor.HEAD);
        register(event, TCGolemAccessories.GLASSES, GolemAccessoryAnchor.HEAD);
        register(event, TCGolemAccessories.VISOR, GolemAccessoryAnchor.HEAD);
        register(event, TCGolemAccessories.BOWTIE, GolemAccessoryAnchor.BODY);
    }

    private static void register(RegisterGolemAccessoryRenderersEvent event, GolemAccessory accessory, GolemAccessoryAnchor anchor) {
        Identifier mesh = TCIds.rl("models/mesh/golem_accessory_" + accessory.id().getPath() + ".tcmesh");
        event.register(accessory, anchor, (pose, collector, context) -> GolemEquipmentRenderer.submit(mesh, pose, collector, context));
    }
}
