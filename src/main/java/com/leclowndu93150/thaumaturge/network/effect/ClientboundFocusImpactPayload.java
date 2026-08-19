package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundFocusImpactPayload(double x, double y, double z, float mx, float my, float mz, boolean burst, int casterId, List<Identifier> parts) implements CustomPacketPayload {
    public static final int NO_CASTER = -1;

    public static final Type<ClientboundFocusImpactPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "fx_focus_impact"));

    private static final StreamCodec<ByteBuf, List<Identifier>> PARTS_CODEC = Identifier.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundFocusImpactPayload> STREAM_CODEC = StreamCodec.of((buf, data) -> {
        buf.writeDouble(data.x);
        buf.writeDouble(data.y);
        buf.writeDouble(data.z);
        buf.writeFloat(data.mx);
        buf.writeFloat(data.my);
        buf.writeFloat(data.mz);
        buf.writeBoolean(data.burst);
        VarInt.write(buf, data.casterId);
        PARTS_CODEC.encode(buf, data.parts);
    }, buf -> new ClientboundFocusImpactPayload(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readBoolean(), VarInt.read(buf),
            PARTS_CODEC.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
