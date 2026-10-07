package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public final class ResearchTests {
    private ResearchTests() {}

    private static ServerPlayer testPlayer(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name));
    }

    public static void register(TTTestRegistrar r) {
        r.add("research/knowledge_roundtrip", 40, helper -> {
            ServerPlayer player = testPlayer(helper, "tc_knowledge_test");
            PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
            Identifier key = TTIds.rl("gametest/knowledge_roundtrip");
            if (knowledge.isResearchKnown(key)) {
                helper.fail("Fresh mock player already knows the test research");
                return;
            }
            knowledge.addResearch(key);
            if (!knowledge.isResearchKnown(key)) {
                helper.fail("addResearch did not record the key");
                return;
            }
            knowledge.markComplete(key);
            if (!knowledge.isResearchComplete(key)) {
                helper.fail("markComplete did not complete the key");
                return;
            }
            helper.succeed();
        });

        r.add("research/aspect_pool_grant_and_spend", 40, helper -> {
            ServerPlayer player = testPlayer(helper, "tc_pool_test");
            Holder<IAspect> ignis = resolve(helper, TTAspects.IGNIS);
            Identifier id = AspectPools.idOf(ignis);
            AspectPools.grant(player, ignis, 5);
            int afterGrant = AspectPools.data(player).amount(id);
            if (afterGrant < 5) {
                helper.fail("Grant of 5 left only " + afterGrant + " points");
                return;
            }
            if (!AspectPools.data(player).isDiscovered(id)) {
                helper.fail("Grant did not discover the aspect");
                return;
            }
            if (AspectPools.spend(player, ignis, afterGrant + 100)) {
                helper.fail("Spend succeeded beyond the pool balance");
                return;
            }
            if (!AspectPools.spend(player, ignis, 3)) {
                helper.fail("Spend of 3 failed with " + afterGrant + " banked");
                return;
            }
            int afterSpend = AspectPools.data(player).amount(id);
            if (afterSpend != afterGrant - 3) {
                helper.fail("Expected " + (afterGrant - 3) + " after spending 3, found " + afterSpend);
                return;
            }
            helper.succeed();
        });
    }

    private static Holder<IAspect> resolve(GameTestHelper helper, net.minecraft.resources.ResourceKey<IAspect> key) {
        Holder<IAspect> holder = Aspects.resolve(helper.getLevel().registryAccess(), key);
        if (holder == null) {
            throw new IllegalStateException("Aspect " + key + " missing from the registry");
        }
        return holder;
    }
}
