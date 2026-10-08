package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFxStyle;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public record ClientboundSpellFxPayload(Vec3 anchor, List<Event> events) implements CustomPacketPayload {
    public static final Type<ClientboundSpellFxPayload> TYPE = new Type<>(TTIds.rl("spell_fx"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSpellFxPayload> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.composite(
                            ByteBufCodecs.DOUBLE,
                            Vec3::x,
                            ByteBufCodecs.DOUBLE,
                            Vec3::y,
                            ByteBufCodecs.DOUBLE,
                            Vec3::z,
                            Vec3::new),
                    ClientboundSpellFxPayload::anchor,
                    Event.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    ClientboundSpellFxPayload::events,
                    ClientboundSpellFxPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Event(
            Holder<SpellFxStyle> style,
            float x,
            float y,
            float z,
            float dx,
            float dy,
            float dz,
            int color,
            boolean directed) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Event> STREAM_CODEC = new StreamCodec<>() {
            private final StreamCodec<RegistryFriendlyByteBuf, Holder<SpellFxStyle>> styleCodec =
                    ByteBufCodecs.holderRegistry(SpellFxStyle.REGISTRY_KEY);

            @Override
            public Event decode(RegistryFriendlyByteBuf buffer) {
                return new Event(
                        styleCodec.decode(buffer),
                        buffer.readFloat(),
                        buffer.readFloat(),
                        buffer.readFloat(),
                        buffer.readFloat(),
                        buffer.readFloat(),
                        buffer.readFloat(),
                        buffer.readInt(),
                        buffer.readBoolean());
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, Event event) {
                styleCodec.encode(buffer, event.style());
                buffer.writeFloat(event.x());
                buffer.writeFloat(event.y());
                buffer.writeFloat(event.z());
                buffer.writeFloat(event.dx());
                buffer.writeFloat(event.dy());
                buffer.writeFloat(event.dz());
                buffer.writeInt(event.color());
                buffer.writeBoolean(event.directed());
            }
        };
    }
}
