package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundRequestSyncAspectPoolPayload() implements CustomPacketPayload {
    public static final Type<ServerboundRequestSyncAspectPoolPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "request_aspect_pool"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRequestSyncAspectPoolPayload> STREAM_CODEC =
            StreamCodec.unit(new ServerboundRequestSyncAspectPoolPayload());

    public static void handle(ServerboundRequestSyncAspectPoolPayload payload, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            AspectPools.sync(player);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
