package com.leclowndu93150.thaumaturge.compat.iris;

import com.leclowndu93150.thaumaturge.TCIds;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.layer.BlockEntityRenderStateShard;
import net.irisshaders.iris.layer.BufferSourceWrapper;
import net.irisshaders.iris.layer.EntityRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.fml.ModList;

public final class IrisCompat {
    private IrisCompat() {}

    public static boolean shadersActive() {
        return ModList.get().isLoaded(TCIds.IRIS) && IrisApi.getInstance().isShaderPackInUse();
    }

    /**
     * Adds Iris's entity render context to deferred visual effects. Effects submitted after the normal entity pass do
     * not pass through Iris's {@code EntityRenderDispatcher} hook, so they need the same wrapper explicitly.
     */
    public static MultiBufferSource entityEffectBuffers(MultiBufferSource buffers) {
        return shadersActive()
                ? new BufferSourceWrapper(
                        buffers,
                        renderType -> OuterWrappedRenderType.wrapExactlyOnce(
                                "iris:thaumaturge_entity_effect", renderType, EntityRenderStateShard.INSTANCE))
                : buffers;
    }

    /**
     * Adds Iris's block-entity render context to deferred visual effects. Effects submitted after the normal block
     * entity pass do not pass through Iris's {@code BlockEntityRenderDispatcher} hook.
     */
    public static MultiBufferSource blockEntityEffectBuffers(MultiBufferSource buffers) {
        return shadersActive()
                ? new BufferSourceWrapper(
                        buffers,
                        renderType -> OuterWrappedRenderType.wrapExactlyOnce(
                                "iris:thaumaturge_block_entity_effect",
                                renderType,
                                BlockEntityRenderStateShard.INSTANCE))
                : buffers;
    }
}
