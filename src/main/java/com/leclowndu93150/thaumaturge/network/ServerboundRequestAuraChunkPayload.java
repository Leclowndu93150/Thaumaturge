package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.aura.AuraData;
import com.leclowndu93150.thaumaturge.content.aura.AuraManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundRequestAuraChunkPayload(int chunkX, int chunkZ) implements CustomPacketPayload {
    public static final Type<ServerboundRequestAuraChunkPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "request_aura_chunk"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRequestAuraChunkPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ServerboundRequestAuraChunkPayload::chunkX,
            ByteBufCodecs.VAR_INT, ServerboundRequestAuraChunkPayload::chunkZ, ServerboundRequestAuraChunkPayload::new);

    public static void handle(ServerboundRequestAuraChunkPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel level = player.level();
        ChunkPos pos = new ChunkPos(payload.chunkX(), payload.chunkZ());
        AuraData data = AuraManager.getAuraChunk(level, pos);
        short base = data != null ? data.getBase() : 0;
        float vis = data != null ? data.getVis() : 0.0F;
        float flux = data != null ? data.getFlux() : 0.0F;
        PacketDistributor.sendToPlayer(player, new ClientboundAuraSnapshotPayload(pos.x(), pos.z(), base, vis, flux));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
