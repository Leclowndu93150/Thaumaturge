package com.leclowndu93150.thaumaturge.client.effect.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.network.effect.ClientboundStreamEffectPayload;
import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.Test;

class BeamImpactPortTest {
    @Test
    void wandSourceCarriesTheCastHandWithoutReplacingTheServerOrigin() {
        ClientboundStreamEffectPayload beam = beamPayload(true);
        assertNull(beam.wandHand());
        for (InteractionHand hand : InteractionHand.values()) {
            ClientboundStreamEffectPayload tagged = beam.withWandSource(17, hand);
            assertEquals(17, tagged.entityId());
            assertEquals(hand, tagged.wandHand());
            assertEquals(beam.sx(), tagged.sx());
            assertEquals(beam.sy(), tagged.sy());
            assertEquals(beam.sz(), tagged.sz());
            assertTrue(tagged.hasFlag(ClientboundStreamEffectPayload.FLAG_HIT));
        }
    }

    @Test
    void hitFlagIsEncodedWithoutChangingLegacyBeamPayloads() {
        ClientboundStreamEffectPayload hit = beamPayload(true);
        ClientboundStreamEffectPayload ordinary = beamPayload(false);

        assertTrue(hit.hasFlag(ClientboundStreamEffectPayload.FLAG_HIT));
        assertFalse(ordinary.hasFlag(ClientboundStreamEffectPayload.FLAG_HIT));
    }

    @Test
    void impactFlareKeepsShortBeamAliveUntilTheFlareExpires() {
        BeamInstance beam = beam(true);

        beam.tick();
        assertFalse(beam.isExpired());
        assertTrue(beam.hit());

        for (int i = 0; i < 4; i++) {
            beam.tick();
        }
        assertFalse(beam.isExpired());

        beam.tick();
        assertTrue(beam.isExpired());
        assertFalse(beam.hit());
    }

    @Test
    void ordinaryBeamLifetimeIsUnchanged() {
        BeamInstance beam = beam(false);

        beam.tick();
        assertFalse(beam.isExpired());
        beam.tick();
        assertTrue(beam.isExpired());
    }

    private static BeamInstance beam(boolean hit) {
        return new BeamInstance(0, 0, 0, 1, 0, 0, 0x80C0FF, 1, 0, 1.0F, false, -1, false, hit);
    }

    private static ClientboundStreamEffectPayload beamPayload(boolean hit) {
        return ClientboundStreamEffectPayload.beam(0, 0, 0, 1, 0, 0, 0x80C0FF, 1, 0, 1.0F, false, -1, false, hit);
    }
}
