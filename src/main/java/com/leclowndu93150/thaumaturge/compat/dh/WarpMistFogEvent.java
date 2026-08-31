package com.leclowndu93150.thaumaturge.compat.dh;

import com.leclowndu93150.thaumaturge.client.warp.WarpFogState;
import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeFogRenderEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiFogRenderParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiMutableFogRenderParam;
import net.minecraft.util.Mth;

public final class WarpMistFogEvent extends DhApiBeforeFogRenderEvent {
    private static final float MIST_FOG_PERCENT = 0.0F;
    private static final float MIST_THICKNESS = 1.0F;

    public static void register() {
        DhApiEventRegister.on(DhApiBeforeFogRenderEvent.class, new WarpMistFogEvent());
    }

    @Override
    public void beforeRender(DhApiCancelableEventParam<EventParam> event) {
        if (!WarpFogState.active()) {
            return;
        }
        float intensity = WarpFogState.intensity();
        DhApiFogRenderParam original = event.value.getOriginalFogRenderParam();
        DhApiMutableFogRenderParam fog = event.value.getFogRenderParam();
        fog.setFarFogStartPercent(Mth.lerp(intensity, original.getFarFogStartPercent(), MIST_FOG_PERCENT));
        fog.setFarFogEndPercent(Mth.lerp(intensity, original.getFarFogEndPercent(), MIST_FOG_PERCENT));
        fog.setFarFogMinThickness(Mth.lerp(intensity, original.getFarFogMinThickness(), MIST_THICKNESS));
        fog.setFarFogMaxThickness(Mth.lerp(intensity, original.getFarFogMaxThickness(), MIST_THICKNESS));
    }
}
