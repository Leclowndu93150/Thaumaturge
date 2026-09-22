package com.leclowndu93150.thaumaturge.client.taint;

import com.leclowndu93150.thaumaturge.network.ClientboundTaintEnvironmentPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class TaintEnvironmentClientHandler {
    private TaintEnvironmentClientHandler() {}

    public static void handle(ClientboundTaintEnvironmentPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> TaintEnvironmentClientState.accept(payload.pressure()));
    }
}
