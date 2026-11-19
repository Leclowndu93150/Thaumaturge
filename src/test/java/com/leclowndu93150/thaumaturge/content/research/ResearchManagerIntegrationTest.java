package com.leclowndu93150.thaumaturge.content.research;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.KnowledgeReward;
import com.leclowndu93150.thaumaturge.api.research.ResearchEvent;
import com.leclowndu93150.thaumaturge.api.research.ResearchParent;
import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
import com.leclowndu93150.thaumaturge.content.research.link.ResearchLinkData;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

final class ResearchManagerIntegrationTest {
    private static final ResourceLocation PARENT = TTIds.rl("integration_parent");
    private static final ResourceLocation CHILD = TTIds.rl("integration_child");
    private MockedStatic<ResearchLinkData> links;

    @BeforeEach
    void setUpLinkStorage() {
        ResearchLinkData data = new ResearchLinkData();
        links = mockStatic(ResearchLinkData.class);
        links.when(() -> ResearchLinkData.get(any())).thenReturn(data);
    }

    @AfterEach
    void closeLinkStorage() {
        links.close();
    }

    @Test
    void managerUnlocksChildOnlyAfterItsParentCompletes() {
        IResearchEntry parent = mock(IResearchEntry.class);
        IResearchEntry child = mock(IResearchEntry.class);
        when(child.parents()).thenReturn(Set.of(ResearchParent.of(PARENT)));
        PlayerKnowledge knowledge = new PlayerKnowledge();
        ServerPlayer player = playerWithEntries(knowledge, parent, child);

        assertTrue(ResearchManager.unlock(player, PARENT));
        assertTrue(knowledge.takeSyncPending());
        assertFalse(ResearchManager.unlock(player, CHILD));
        assertFalse(knowledge.knows(CHILD));
        assertFalse(knowledge.takeSyncPending());

        assertTrue(ResearchManager.complete(player, PARENT));
        assertTrue(ResearchManager.unlock(player, CHILD));
        assertTrue(knowledge.knows(CHILD));
        assertFalse(knowledge.hasCompleted(CHILD));
        assertTrue(knowledge.takeSyncPending());
    }

    @Test
    void managerEnforcesStageResearchAndGrantsEveryStageRewardExactlyOnce() {
        Holder<IResearchCategory> category = Holder.direct(mock(IResearchCategory.class));
        IResearchStage first = stage(category, 16, List.of(PARENT));
        IResearchStage middle = stage(category, 32, List.of(PARENT));
        IResearchStage last = stage(category, 64, List.of());
        IResearchEntry parent = mock(IResearchEntry.class);
        IResearchEntry child = mock(IResearchEntry.class);
        when(child.stages()).thenReturn(List.of(first, middle, last));
        PlayerKnowledge knowledge = new PlayerKnowledge();
        ServerPlayer player = playerWithEntries(knowledge, parent, child);
        knowledge.learn(CHILD);

        assertFalse(ResearchManager.advanceStage(player, CHILD));
        assertEquals(0, knowledge.stageOf(CHILD));
        assertEquals(0, knowledge.knowledgePoints(KnowledgeType.OBSERVATION, null));
        verify(player, never()).giveExperiencePoints(anyInt());

        assertTrue(ResearchManager.complete(player, PARENT));
        knowledge.takeSyncPending();
        assertTrue(ResearchManager.advanceStage(player, CHILD));
        assertEquals(1, knowledge.stageOf(CHILD));
        assertFalse(knowledge.hasCompleted(CHILD));
        assertEquals(16, knowledge.knowledgePoints(KnowledgeType.OBSERVATION, null));
        assertTrue(knowledge.takeSyncPending());

        assertTrue(ResearchManager.advanceStage(player, CHILD));
        assertTrue(knowledge.hasCompleted(CHILD));
        assertEquals(3, knowledge.stageOf(CHILD));
        assertEquals(112, knowledge.knowledgePoints(KnowledgeType.OBSERVATION, null));
        assertTrue(knowledge.hasFlag(CHILD, ResearchFlag.POPUP));
        assertTrue(knowledge.hasFlag(CHILD, ResearchFlag.RESEARCH));
        assertTrue(knowledge.takeSyncPending());
        verify(player, times(2)).giveExperiencePoints(5);

        assertFalse(ResearchManager.advanceStage(player, CHILD));
        assertEquals(112, knowledge.knowledgePoints(KnowledgeType.OBSERVATION, null));
        assertFalse(knowledge.takeSyncPending());
        verify(player, times(2)).giveExperiencePoints(5);
    }

    @Test
    void discoveredAspectSatisfiesItsResearchRequirementWithoutScan() {
        ResourceLocation id = TTIds.rl("vitium");
        ResourceLocation requirement = TTIds.rl("scanned/aspect/thaumaturge/vitium");
        MappedRegistry<IAspect> aspects = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Holder.Reference<IAspect> aspect = Registry.registerForHolder(aspects, id, mock(IAspect.class));
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.registryAccess()).thenReturn(new RegistryAccess.ImmutableRegistryAccess(List.of(aspects)));
        PlayerKnowledge knowledge = new PlayerKnowledge();
        try (var pools = mockStatic(AspectPoolAccess.class)) {
            assertFalse(ResearchManager.requirementSatisfied(player, knowledge, requirement));
            pools.when(() -> AspectPoolAccess.isDiscovered(player, aspect)).thenReturn(true);
            assertTrue(ResearchManager.requirementSatisfied(player, knowledge, requirement));
            IResearchStage stage = mock(IResearchStage.class);
            IResearchEntry entry = mock(IResearchEntry.class);
            when(stage.requiredResearch()).thenReturn(List.of(requirement));
            when(stage.requiredKnowledge()).thenReturn(List.of());
            when(stage.obtain()).thenReturn(List.of());
            when(stage.craft()).thenReturn(List.of());
            when(entry.stages()).thenReturn(List.of(stage));
            assertTrue(ResearchManager.stageRequirementsMet(player, knowledge, entry, CHILD, 0));
            assertFalse(ResearchManager.requirementSatisfied(
                    player, knowledge, TTIds.rl("scanned/item/thaumaturge/vitium")));
            assertFalse(knowledge.hasCompleted(requirement));
        }
    }

    @Test
    void invalidStageIndicesAreRejectedWithoutReadingRequirements() {
        IResearchEntry entry = mock(IResearchEntry.class);
        IResearchStage stage = mock(IResearchStage.class);
        when(entry.stages()).thenReturn(List.of(stage));
        PlayerKnowledge knowledge = new PlayerKnowledge();
        ServerPlayer player = mock(ServerPlayer.class);

        assertFalse(ResearchManager.stageRequirementsMet(player, knowledge, entry, CHILD, -1));
        assertFalse(ResearchManager.stageRequirementsMet(player, knowledge, entry, CHILD, 1));
        verifyNoInteractions(stage, player);
    }

    @Test
    void canceledCompletionStillSynchronizesTheNewlyLearnedEntry() {
        PlayerKnowledge knowledge = new PlayerKnowledge();
        ServerPlayer player = playerWithEntries(knowledge, mock(IResearchEntry.class), mock(IResearchEntry.class));
        Consumer<ResearchEvent.Completed> veto = event -> {
            if (event.player() == player && event.research().equals(CHILD)) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(ResearchEvent.Completed.class, veto);
        try {
            assertFalse(ResearchManager.complete(player, CHILD));
            assertTrue(knowledge.knows(CHILD));
            assertFalse(knowledge.hasCompleted(CHILD));
            assertTrue(knowledge.takeSyncPending());
            assertFalse(ResearchManager.complete(player, CHILD));
            assertFalse(knowledge.takeSyncPending());
        } finally {
            NeoForge.EVENT_BUS.unregister(veto);
        }
    }

    private static IResearchStage stage(
            Holder<IResearchCategory> category, int reward, List<ResourceLocation> prerequisites) {
        IResearchStage stage = mock(IResearchStage.class);
        when(stage.requiredResearch()).thenReturn(prerequisites);
        when(stage.knowledge()).thenReturn(List.of(new KnowledgeReward(KnowledgeType.OBSERVATION, category, reward)));
        return stage;
    }

    private static ServerPlayer playerWithEntries(
            PlayerKnowledge knowledge, IResearchEntry parent, IResearchEntry child) {
        MappedRegistry<IResearchEntry> entries = new MappedRegistry<>(IResearchEntry.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(entries, PARENT, parent);
        Registry.register(entries, CHILD, child);
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getUUID()).thenReturn(new UUID(1L, 2L));
        when(player.registryAccess()).thenReturn(new RegistryAccess.ImmutableRegistryAccess(List.of(entries)));
        when(player.getData(TTAttachments.KNOWLEDGE)).thenReturn(knowledge);
        return player;
    }
}
