package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundFocusImpactPayload(
        double x,
        double y,
        double z,
        float mx,
        float my,
        float mz,
        boolean burst,
        int casterId,
        List<ResourceLocation> parts)
        implements CustomPacketPayload {
    public static final int NO_CASTER = -1;

    public static final Type<ClientboundFocusImpactPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "fx_focus_impact"));

    private static final StreamCodec<ByteBuf, List<ResourceLocation>> PARTS_CODEC =
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundFocusImpactPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, data) -> {
                        buf.writeDouble(data.x);
                        buf.writeDouble(data.y);
                        buf.writeDouble(data.z);
                        buf.writeFloat(data.mx);
                        buf.writeFloat(data.my);
                        buf.writeFloat(data.mz);
                        buf.writeBoolean(data.burst);
                        VarInt.write(buf, data.casterId);
                        PARTS_CODEC.encode(buf, data.parts);
                    },
                    buf -> new ClientboundFocusImpactPayload(
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readFloat(),
                            buf.readBoolean(),
                            VarInt.read(buf),
                            PARTS_CODEC.decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
