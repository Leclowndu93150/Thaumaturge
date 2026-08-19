package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundSealPayload(SealPos pos, Optional<SealEntity> seal) implements CustomPacketPayload {
    public static final Type<ClientboundSealPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "seal"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSealPayload> STREAM_CODEC = StreamCodec.composite(SealPos.STREAM_CODEC, ClientboundSealPayload::pos,
            ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistries(SealEntity.CODEC)), ClientboundSealPayload::seal, ClientboundSealPayload::new);

    public static ClientboundSealPayload update(SealEntity seal) {
        return new ClientboundSealPayload(seal.getSealPos(), Optional.of(seal));
    }

    public static ClientboundSealPayload remove(SealPos pos) {
        return new ClientboundSealPayload(pos, Optional.empty());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
