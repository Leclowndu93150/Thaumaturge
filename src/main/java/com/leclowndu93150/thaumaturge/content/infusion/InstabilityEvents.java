package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.content.infusion.instability.InstabilityContext;
import com.leclowndu93150.thaumaturge.content.infusion.instability.InstabilityOutcome;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public final class InstabilityEvents {
    private static final double RESEARCH_RANGE = 10.0;
    private static final ResourceLocation INSTABILITY_RESEARCH = TTIds.rl("instability");

    private InstabilityEvents() {}

    public static void trigger(ServerLevel level, BlockPos matrixPos, List<BlockPos> pedestals) {
        grantInstabilityResearch(level, matrixPos);
        InstabilityOutcome.roll(level.registryAccess(), level.getRandom())
                .ifPresent(outcome -> outcome.effect().apply(new InstabilityContext(level, matrixPos, pedestals)));
    }

    private static void grantInstabilityResearch(ServerLevel level, BlockPos matrixPos) {
        for (ServerPlayer player :
                level.getEntitiesOfClass(ServerPlayer.class, new AABB(matrixPos).inflate(RESEARCH_RANGE))) {
            if (!KnowledgeAccess.of(player).isResearchKnown(INSTABILITY_RESEARCH)
                    && ResearchManager.complete(player, INSTABILITY_RESEARCH)) {
                player.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.translatable("message.thaumaturge.discovery.instability")
                                .withStyle(ChatFormatting.DARK_PURPLE)));
            }
        }
    }
}
