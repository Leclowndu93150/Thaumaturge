package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFxStyle;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record ClientboundSpellFxPayload(Vec3 anchor, List<Event> events) implements CustomPacketPayload {
    public static final Type<ClientboundSpellFxPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TTIds.MODID, "spell_fx"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSpellFxPayload> STREAM_CODEC = StreamCodec.composite(Vec3.STREAM_CODEC, ClientboundSpellFxPayload::anchor,
            Event.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundSpellFxPayload::events, ClientboundSpellFxPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Event(Holder<SpellFxStyle> style, float x, float y, float z, float dx, float dy, float dz, int color, boolean directed) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Event> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderRegistry(SpellFxStyle.REGISTRY_KEY), Event::style, ByteBufCodecs.FLOAT,
                Event::x, ByteBufCodecs.FLOAT, Event::y, ByteBufCodecs.FLOAT, Event::z, ByteBufCodecs.FLOAT, Event::dx, ByteBufCodecs.FLOAT, Event::dy, ByteBufCodecs.FLOAT, Event::dz,
                ByteBufCodecs.INT, Event::color, ByteBufCodecs.BOOL, Event::directed, Event::new);
    }
}
