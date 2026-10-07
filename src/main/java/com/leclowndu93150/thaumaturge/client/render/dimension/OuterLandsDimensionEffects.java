package com.leclowndu93150.thaumaturge.client.render.dimension;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class OuterLandsDimensionEffects extends DimensionSpecialEffects {
    private static final Vec3 FOG_COLOR = Vec3.fromRGB24(0x181305);

    private OuterLandsDimensionEffects() {
        super(Float.NaN, false, SkyType.NONE, false, true);
    }

    @SubscribeEvent
    public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(OuterLands.EFFECTS, new OuterLandsDimensionEffects());
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return FOG_COLOR;
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return true;
    }
}
