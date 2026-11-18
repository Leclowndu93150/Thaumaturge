package com.leclowndu93150.thaumaturge.network.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.junit.jupiter.api.Test;

class ClientboundWandSpellFxPayloadTest {
    @Test
    void preservesBothTheServerPositionAndTheWandEventMask() {
        var effects = new ClientboundSpellFxPayload(new Vec3(1, 2, 3), List.of());
        int wandEvents = 1 | (1 << 23);
        var payload = new ClientboundWandSpellFxPayload(effects, true, wandEvents);
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY, ConnectionType.OTHER);
        try {
            ClientboundWandSpellFxPayload.STREAM_CODEC.encode(buffer, payload);
            var decoded = ClientboundWandSpellFxPayload.STREAM_CODEC.decode(buffer);

            assertEquals(effects, decoded.effects());
            assertEquals(InteractionHand.OFF_HAND, decoded.hand());
            assertEquals(wandEvents, decoded.wandEvents());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }
}
