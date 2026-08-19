package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundRequestSyncAspectPoolPayload implements CustomPacketPayload {
    public static final Type<ServerboundRequestSyncAspectPoolPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "request_aspect_pool"));

    public static final ServerboundRequestSyncAspectPoolPayload INSTANCE = new ServerboundRequestSyncAspectPoolPayload();

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRequestSyncAspectPoolPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private ServerboundRequestSyncAspectPoolPayload() {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundRequestSyncAspectPoolPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        AspectPools.sync(player);
    }
}
