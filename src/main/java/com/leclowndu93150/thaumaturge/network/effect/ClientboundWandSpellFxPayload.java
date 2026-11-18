package com.leclowndu93150.thaumaturge.network.effect;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

public record ClientboundWandSpellFxPayload(ClientboundSpellFxPayload effects, boolean offHand, int wandEvents)
        implements CustomPacketPayload {
    public static final Type<ClientboundWandSpellFxPayload> TYPE = new Type<>(TTIds.rl("wand_spell_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWandSpellFxPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ClientboundSpellFxPayload.STREAM_CODEC,
                    ClientboundWandSpellFxPayload::effects,
                    ByteBufCodecs.BOOL,
                    ClientboundWandSpellFxPayload::offHand,
                    ByteBufCodecs.VAR_INT,
                    ClientboundWandSpellFxPayload::wandEvents,
                    ClientboundWandSpellFxPayload::new);

    public InteractionHand hand() {
        return offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    @Override
    public Type<ClientboundWandSpellFxPayload> type() {
        return TYPE;
    }
}
