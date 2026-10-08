package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public record KnowledgeGrant(KnowledgeType type, int smallestShareDivisor, int largestShareDivisor) {
    public void award(ServerPlayer player) {
        int progression = type.progression();
        ResearchGrants.grantConvertedKnowledge(
                player,
                type,
                Mth.nextInt(player.getRandom(), progression / smallestShareDivisor, progression / largestShareDivisor));
    }
}
