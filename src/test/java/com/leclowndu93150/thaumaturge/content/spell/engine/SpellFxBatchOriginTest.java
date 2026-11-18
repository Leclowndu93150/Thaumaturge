package com.leclowndu93150.thaumaturge.content.spell.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.network.effect.ClientboundStreamEffectPayload;
import com.leclowndu93150.thaumaturge.network.effect.StreamEffectKind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

class SpellFxBatchOriginTest {
    @ParameterizedTest
    @EnumSource(
            value = StreamEffectKind.class,
            names = {"BEAM", "BOLT"})
    void onlyTheRootStreamUsesTheVisualWandTipAfterWorldSpaceConversion(StreamEffectKind kind) {
        ServerLevel level = mock(ServerLevel.class);
        Player caster = mock(Player.class);
        when(level.getEntity(17)).thenReturn(caster);
        Vec3 offset = new Vec3(100, 0, 100);
        Vec3 root = new Vec3(10, 20, 30);
        Vec3 tip = new Vec3(10.3, 19.55, 30.35);
        Vec3 target = new Vec3(10, 20, 40);
        Vec3 rootWorld = root.add(offset);
        Vec3 tipWorld = tip.add(offset);
        Vec3 targetWorld = target.add(offset);
        try (var spaces = mockStatic(LevelSpaces.class);
                var targeting = mockStatic(SpellTarget.class);
                var packets = mockStatic(PacketDistributor.class)) {
            spaces.when(() -> LevelSpaces.toWorld(eq(level), any(Vec3.class)))
                    .thenAnswer(call -> ((Vec3) call.getArgument(1)).add(offset));
            targeting
                    .when(() -> SpellTarget.visualWandOrigin(caster, InteractionHand.OFF_HAND))
                    .thenReturn(tip);
            SpellFxBatch effects = new SpellFxBatch(level);
            effects.setWandOrigin(17, InteractionHand.OFF_HAND, rootWorld);
            if (kind == StreamEffectKind.BEAM) {
                effects.beam(root, target, 0x8000FF, 4, true);
            } else {
                effects.arc(root, target, 0x8000FF, 0.5F);
            }
            ArgumentCaptor<ClientboundStreamEffectPayload> rootPayload =
                    ArgumentCaptor.forClass(ClientboundStreamEffectPayload.class);
            packets.verify(() -> PacketDistributor.sendToPlayersNear(
                    eq(level),
                    isNull(),
                    eq(rootWorld.x),
                    eq(rootWorld.y),
                    eq(rootWorld.z),
                    eq(64.0),
                    rootPayload.capture()));
            ClientboundStreamEffectPayload visual = rootPayload.getValue();
            assertEquals(kind, visual.kind());
            assertEquals(tipWorld, new Vec3(visual.sx(), visual.sy(), visual.sz()));
            assertEquals(targetWorld, new Vec3(visual.tx(), visual.ty(), visual.tz()));
            assertEquals(17, visual.entityId());
            assertEquals(InteractionHand.OFF_HAND, visual.wandHand());
            if (kind == StreamEffectKind.BEAM) {
                assertTrue(visual.hasFlag(ClientboundStreamEffectPayload.FLAG_HIT));
            }

            Vec3 secondary = root.add(1, 0, 0);
            Vec3 secondaryWorld = secondary.add(offset);
            if (kind == StreamEffectKind.BEAM) {
                effects.beam(secondary, target, 0x8000FF, 4, true);
            } else {
                effects.arc(secondary, target, 0x8000FF, 0.5F);
            }
            ArgumentCaptor<ClientboundStreamEffectPayload> secondaryPayload =
                    ArgumentCaptor.forClass(ClientboundStreamEffectPayload.class);
            packets.verify(() -> PacketDistributor.sendToPlayersNear(
                    eq(level),
                    isNull(),
                    eq(secondaryWorld.x),
                    eq(secondaryWorld.y),
                    eq(secondaryWorld.z),
                    eq(64.0),
                    secondaryPayload.capture()));
            ClientboundStreamEffectPayload unchanged = secondaryPayload.getValue();
            assertEquals(secondaryWorld, new Vec3(unchanged.sx(), unchanged.sy(), unchanged.sz()));
            assertEquals(targetWorld, new Vec3(unchanged.tx(), unchanged.ty(), unchanged.tz()));
            assertNull(unchanged.wandHand());
            assertEquals(0, unchanged.channelBranch());
        }
    }
}
