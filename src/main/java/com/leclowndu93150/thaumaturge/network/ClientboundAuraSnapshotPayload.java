package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundAuraSnapshotPayload(int chunkX, int chunkZ, short base, float vis, float flux) implements CustomPacketPayload {
    public static final Type<ClientboundAuraSnapshotPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "aura_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAuraSnapshotPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ClientboundAuraSnapshotPayload::chunkX,
            ByteBufCodecs.VAR_INT, ClientboundAuraSnapshotPayload::chunkZ, ByteBufCodecs.SHORT, ClientboundAuraSnapshotPayload::base, ByteBufCodecs.FLOAT, ClientboundAuraSnapshotPayload::vis,
            ByteBufCodecs.FLOAT, ClientboundAuraSnapshotPayload::flux, ClientboundAuraSnapshotPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
