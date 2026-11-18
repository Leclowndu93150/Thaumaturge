package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

public record ClientboundSealChunkPayload(ChunkPos chunk, List<SealEntity> seals) implements CustomPacketPayload {
    public static final Type<ClientboundSealChunkPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "seal_chunk"));

    private static final StreamCodec<RegistryFriendlyByteBuf, ChunkPos> CHUNK_POS_CODEC =
            StreamCodec.of((buf, chunk) -> buf.writeLong(chunk.toLong()), buf -> new ChunkPos(buf.readLong()));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSealChunkPayload> STREAM_CODEC =
            StreamCodec.composite(
                    CHUNK_POS_CODEC,
                    ClientboundSealChunkPayload::chunk,
                    ByteBufCodecs.fromCodecWithRegistries(SealEntity.CODEC).apply(ByteBufCodecs.list()),
                    ClientboundSealChunkPayload::seals,
                    ClientboundSealChunkPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
