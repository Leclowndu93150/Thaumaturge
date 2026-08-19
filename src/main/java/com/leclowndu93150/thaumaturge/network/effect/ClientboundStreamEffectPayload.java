package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundStreamEffectPayload(StreamEffectKind kind, double sx, double sy, double sz, double tx, double ty, double tz, int color, int extraInt, int extraInt2, float extraFloat,
        float extraFloat2, int entityId, byte flags) implements CustomPacketPayload {

    public static final byte FLAG_REVERSE = 1;
    public static final byte FLAG_WITH_SOURCE = 2;

    public static final Type<ClientboundStreamEffectPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "fx_stream"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundStreamEffectPayload> STREAM_CODEC = StreamCodec.of((buf, data) -> {
        buf.writeByte(data.kind.ordinal());
        buf.writeDouble(data.sx);
        buf.writeDouble(data.sy);
        buf.writeDouble(data.sz);
        buf.writeDouble(data.tx);
        buf.writeDouble(data.ty);
        buf.writeDouble(data.tz);
        buf.writeInt(data.color);
        buf.writeVarInt(data.extraInt);
        buf.writeVarInt(data.extraInt2);
        buf.writeFloat(data.extraFloat);
        buf.writeFloat(data.extraFloat2);
        buf.writeVarInt(data.entityId);
        buf.writeByte(data.flags);
    }, buf -> new ClientboundStreamEffectPayload(StreamEffectKind.byOrdinal(buf.readByte()), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
            buf.readInt(), buf.readVarInt(), buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readByte()));

    public boolean hasFlag(byte mask) {
        return (this.flags & mask) != 0;
    }

    public static ClientboundStreamEffectPayload arc(double sx, double sy, double sz, double tx, double ty, double tz, int color, float gravity) {
        return new ClientboundStreamEffectPayload(StreamEffectKind.ARC, sx, sy, sz, tx, ty, tz, color, 0, 0, gravity, 0F, -1, (byte) 0);
    }

    public static ClientboundStreamEffectPayload bolt(double sx, double sy, double sz, double tx, double ty, double tz, int color, float width) {
        return new ClientboundStreamEffectPayload(StreamEffectKind.BOLT, sx, sy, sz, tx, ty, tz, color, 0, 0, width, 0F, -1, (byte) 0);
    }

    public static ClientboundStreamEffectPayload beam(double sx, double sy, double sz, double tx, double ty, double tz, int color, int age, int beamType, float endMod, boolean reverse, int sourceEntityId, boolean withSource) {
        byte flags = 0;
        if (reverse)
            flags |= FLAG_REVERSE;
        if (withSource)
            flags |= FLAG_WITH_SOURCE;
        return new ClientboundStreamEffectPayload(StreamEffectKind.BEAM, sx, sy, sz, tx, ty, tz, color, age, beamType, endMod, 0F, sourceEntityId, flags);
    }

    public static ClientboundStreamEffectPayload essentia(double sx, double sy, double sz, double tx, double ty, double tz, int color, int count, float scale, int extend, double my) {
        return new ClientboundStreamEffectPayload(StreamEffectKind.ESSENTIA, sx, sy, sz, tx, ty, tz, color, count, extend, scale, (float) my, -1, (byte) 0);
    }

    public static ClientboundStreamEffectPayload bore(double sx, double sy, double sz, int targetEntityId, int color, int count, float scale, int extend, double my) {
        return new ClientboundStreamEffectPayload(StreamEffectKind.BORE, sx, sy, sz, 0, 0, 0, color, count, extend, scale, (float) my, targetEntityId, (byte) 0);
    }

    public static ClientboundStreamEffectPayload voidStream(double sx, double sy, double sz, double tx, double ty, double tz, int seed, float scale) {
        return new ClientboundStreamEffectPayload(StreamEffectKind.VOID, sx, sy, sz, tx, ty, tz, 0, seed, 0, scale, 0F, -1, (byte) 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
