package com.leclowndu93150.thaumaturge.compat.iris;

import com.leclowndu93150.thaumaturge.TCIds;
import net.irisshaders.iris.api.v0.IrisApi;
import net.neoforged.fml.ModList;

public final class IrisCompat {
    private IrisCompat() {}

    public static boolean shadersActive() {
        return ModList.get().isLoaded(TCIds.IRIS) && IrisApi.getInstance().isShaderPackInUse();
    }
}
