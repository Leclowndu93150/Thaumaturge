package com.leclowndu93150.thaumaturge.client.network;

import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityInfusionMatrix;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundInfusionSourcePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class InfusionSourceClientHandler {
    private static final int PEDESTAL_FX_TICKS = 60;
    private static final int DEFAULT_FX_TICKS = 15;

    private InfusionSourceClientHandler() {}

    public static void handle(ClientboundInfusionSourcePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> dispatch(payload));
    }

    private static void dispatch(ClientboundInfusionSourcePayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (!(level.getBlockEntity(payload.matrixPos()) instanceof BlockEntityInfusionMatrix matrix)) {
            return;
        }
        int ticks = level.getBlockEntity(payload.sourcePos()) instanceof BlockEntityPedestal ? PEDESTAL_FX_TICKS : DEFAULT_FX_TICKS;
        matrix.addClientSourceFX(payload.sourcePos(), ticks);
    }
}
