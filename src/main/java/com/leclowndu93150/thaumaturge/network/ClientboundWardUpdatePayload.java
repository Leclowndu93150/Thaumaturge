package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundWardUpdatePayload(BlockPos pos, Optional<UUID> owner) implements CustomPacketPayload {
    public static final Type<ClientboundWardUpdatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TCIds.MODID, "ward_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWardUpdatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ClientboundWardUpdatePayload::pos,
                    ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
                    ClientboundWardUpdatePayload::owner,
                    ClientboundWardUpdatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
