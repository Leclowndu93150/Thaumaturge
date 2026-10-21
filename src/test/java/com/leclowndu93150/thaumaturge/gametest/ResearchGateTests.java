package com.leclowndu93150.thaumaturge.gametest;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class ResearchGateTests {
    private ResearchGateTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("research/gate_conjunction", 20, helper -> {
            ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "gate_test"));
            PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
            ResearchGate first = new ResearchGate(TTIds.rl("gametest/gate_first"), Optional.empty(), false);
            ResearchGate second = new ResearchGate(TTIds.rl("gametest/gate_second"), Optional.of(2), false);
            ResearchGate forbidden = new ResearchGate(TTIds.rl("gametest/gate_forbidden"), Optional.empty(), true);
            ResearchGate gate = ResearchGate.allOf(List.of(first, ResearchGate.allOf(List.of(second, forbidden))));
            knowledge.addResearch(first.entry());
            knowledge.markComplete(first.entry());
            if (ResearchGate.passes(player, gate) || ResearchManager.doesPassGate(player, gate) || ResearchManager.doesPassGate(knowledge, gate)) {
                helper.fail("A conjunction passed with an unmet staged requirement");
                return;
            }
            knowledge.addResearch(second.entry());
            knowledge.setResearchStage(second.entry(), 2);
            if (!ResearchGate.passes(player, gate) || !ResearchManager.doesPassGate(player, gate) || !ResearchManager.doesPassGate(knowledge, gate) || !ResearchManager.doesPassGate(knowledge, null)) {
                helper.fail("A conjunction rejected satisfied requirements");
                return;
            }
            knowledge.addResearch(forbidden.entry());
            knowledge.markComplete(forbidden.entry());
            if (ResearchGate.passes(player, gate) || ResearchManager.doesPassGate(knowledge, gate)) {
                helper.fail("A conjunction discarded a negated requirement");
                return;
            }
            helper.succeed();
        });

        registrar.add("research/gate_serialization", 20, helper -> {
            ResearchGate leaf = new ResearchGate(TTIds.rl("gametest/gate_first"), Optional.of(2), true);
            ResearchGate gate = ResearchGate.allOf(List.of(leaf, new ResearchGate(TTIds.rl("gametest/gate_second"), Optional.empty(), false)));
            for (ResearchGate value : List.of(leaf, gate)) {
                var json = ResearchGate.CODEC.encodeStart(JsonOps.INSTANCE, value).getOrThrow();
                if (!ResearchGate.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow().equals(value)) {
                    helper.fail("Research gate JSON lost requirements");
                    return;
                }
                RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
                try {
                    ResearchGate.STREAM_CODEC.encode(buffer, value);
                    if (!ResearchGate.STREAM_CODEC.decode(buffer).equals(value) || buffer.isReadable()) {
                        helper.fail("Research gate stream lost requirements or left unread bytes");
                        return;
                    }
                } finally {
                    buffer.release();
                }
            }
            RegistryFriendlyByteBuf legacy = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            try {
                ResearchGate.STREAM_CODEC.encode(legacy, leaf);
                if (!Identifier.STREAM_CODEC.decode(legacy).equals(leaf.entry()) || !ByteBufCodecs.optional(ByteBufCodecs.VAR_INT).decode(legacy).equals(leaf.stage())
                        || ByteBufCodecs.BOOL.decode(legacy) != leaf.negate() || legacy.isReadable()) {
                    helper.fail("Leaf stream encoding changed");
                    return;
                }
            } finally {
                legacy.release();
            }
            if (ResearchGate.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"all_of\":[]}")).result().isPresent()) {
                helper.fail("An empty conjunction was accepted");
                return;
            }
            helper.succeed();
        });
    }
}
