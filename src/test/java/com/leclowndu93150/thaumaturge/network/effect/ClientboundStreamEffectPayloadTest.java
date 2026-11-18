package com.leclowndu93150.thaumaturge.network.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.junit.jupiter.api.Test;

class ClientboundStreamEffectPayloadTest {
    @Test
    void channelMetadataAndHandSurviveTheExistingStreamCodec() {
        for (InteractionHand hand : InteractionHand.values()) {
            ClientboundStreamEffectPayload payload =
                    beam(3).withChannel(8, 16.0F).withWandSource(17, hand);
            ClientboundStreamEffectPayload decoded = roundTrip(payload);

            assertEquals(payload, decoded);
            assertEquals(8, decoded.channelBranch());
            assertEquals(3, decoded.beamType());
            assertEquals((8 << 2) | 3, decoded.extraInt2());
            assertEquals(16.0F, decoded.extraFloat2());
            assertEquals(17, decoded.entityId());
            assertEquals(hand, decoded.wandHand());
            assertTrue(decoded.hasFlag(ClientboundStreamEffectPayload.FLAG_HIT));
            assertTrue(decoded.hasFlag(ClientboundStreamEffectPayload.FLAG_WITH_SOURCE));
            assertTrue(decoded.hasFlag(ClientboundStreamEffectPayload.FLAG_REVERSE));
            assertEquals(payload, beam(3).withWandSource(17, hand).withChannel(8, 16.0F));
        }
    }

    @Test
    void ordinaryBeamsKeepTheirUnpackedTypeAndZeroChannelRange() {
        ClientboundStreamEffectPayload payload = beam(7);
        ClientboundStreamEffectPayload decoded = roundTrip(payload);
        assertEquals(payload, decoded);
        assertEquals(7, decoded.extraInt2());
        assertEquals(7, decoded.beamType());
        assertEquals(0, decoded.channelBranch());
        assertEquals(0.0F, decoded.extraFloat2());
        assertNull(decoded.wandHand());
    }

    @Test
    void replacingChannelMetadataDoesNotTreatThePackedBranchAsTheBeamType() {
        ClientboundStreamEffectPayload decoded =
                roundTrip(beam(2).withChannel(1, 8.0F).withChannel(4, 12.0F));
        assertEquals(2, decoded.beamType());
        assertEquals(4, decoded.channelBranch());
        assertEquals(12.0F, decoded.extraFloat2());
    }

    @Test
    void channelBuilderRejectsInvalidBranchRangeAndEffectKind() {
        for (int branch : new int[] {0, -1, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> beam(0).withChannel(branch, 8.0F));
        }
        for (float range : new float[] {0, -1, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> beam(0).withChannel(1, range));
        }
        assertThrows(
                IllegalArgumentException.class,
                () -> ClientboundStreamEffectPayload.bolt(0, 0, 0, 1, 0, 0, 0xFFFFFF, 1.0F)
                        .withChannel(1, 8.0F));
    }

    private static ClientboundStreamEffectPayload beam(int type) {
        return ClientboundStreamEffectPayload.beam(1, 2, 3, 4, 5, 6, 0x8000FF, 10, type, 0.75F, true, -1, true, true);
    }

    private static ClientboundStreamEffectPayload roundTrip(ClientboundStreamEffectPayload payload) {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY, ConnectionType.OTHER);
        try {
            ClientboundStreamEffectPayload.STREAM_CODEC.encode(buffer, payload);
            ClientboundStreamEffectPayload decoded = ClientboundStreamEffectPayload.STREAM_CODEC.decode(buffer);
            assertEquals(0, buffer.readableBytes());
            return decoded;
        } finally {
            buffer.release();
        }
    }
}
