package com.leclowndu93150.thaumaturge.content.research;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanEntry;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScannedStack;
import com.leclowndu93150.thaumaturge.content.research.link.ResearchLinkData;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanBindings;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

final class ResearchProgressionDataParityTest {
    private static final ResourceLocation AUROMANCY = TTIds.rl("unlock_auromancy");
    private static final ResourceLocation ORE = TTIds.rl("ore");
    private MockedStatic<ResearchLinkData> links;

    @BeforeEach
    void setUpLinkStorage() {
        ResearchLinkData data = new ResearchLinkData();
        links = mockStatic(ResearchLinkData.class);
        links.when(() -> ResearchLinkData.get(org.mockito.ArgumentMatchers.any()))
                .thenReturn(data);
    }

    @AfterEach
    void resetCraftReferences() {
        links.close();
        CraftReferenceHolder.resetSession();
    }

    @Test
    void wandCraftRequirementAndOreScanUseLoadedResearchData() throws Exception {
        JsonObject auromancyJson = loadResearch("unlock_auromancy");
        JsonArray auromancyStages = auromancyJson.getAsJsonArray("stages");
        RegistryOps<com.google.gson.JsonElement> ops = RegistryOps.create(
                JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        List<IResearchStage> auromancy = new ArrayList<>();
        for (int index = 0; index < auromancyStages.size(); index++) {
            auromancy.add(
                    ResearchStage.CODEC.parse(ops, auromancyStages.get(index)).getOrThrow());
        }
        List<ResearchEntryMeta> auromancyMeta = ResearchEntryMeta.CODEC
                .listOf()
                .parse(JsonOps.INSTANCE, auromancyJson.getAsJsonArray("meta"))
                .getOrThrow();
        assertTrue(auromancy.get(1).craft().stream()
                .anyMatch(requirement -> requirement.items().contains(TTItems.WAND.getDelegate())));

        JsonObject oreJson = loadResearch("ore");
        List<ResearchEntryMeta> oreMeta = ResearchEntryMeta.CODEC
                .listOf()
                .parse(JsonOps.INSTANCE, oreJson.getAsJsonArray("meta"))
                .getOrThrow();
        assertFalse(oreMeta.contains(ResearchEntryMeta.AUTOUNLOCK));
        IResearchStage oreStage = ResearchStage.CODEC
                .parse(ops, oreJson.getAsJsonArray("stages").get(0))
                .getOrThrow();

        IResearchEntry auromancyEntry = mock(IResearchEntry.class);
        when(auromancyEntry.stages()).thenReturn(List.copyOf(auromancy));
        when(auromancyEntry.meta()).thenReturn(Set.copyOf(auromancyMeta));
        when(auromancyEntry.noteAspects()).thenReturn(com.leclowndu93150.thaumaturge.api.aspect.AspectList.EMPTY);
        IResearchEntry oreEntry = mock(IResearchEntry.class);
        when(oreEntry.stages()).thenReturn(List.of(oreStage));
        when(oreEntry.meta()).thenReturn(Set.copyOf(oreMeta));
        when(oreEntry.parents()).thenReturn(Set.of());
        when(oreEntry.noteAspects()).thenReturn(com.leclowndu93150.thaumaturge.api.aspect.AspectList.EMPTY);

        MappedRegistry<IResearchEntry> entries = new MappedRegistry<>(IResearchEntry.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(entries, AUROMANCY, auromancyEntry);
        Registry.register(entries, ORE, oreEntry);
        MappedRegistry<ScanEntry> scans = new MappedRegistry<>(ScanEntry.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(
                scans,
                ORE,
                new ScanEntry(
                        ORE,
                        Optional.of(HolderSet.direct(TTBlocks.ORE_AMBER.getDelegate())),
                        Optional.empty(),
                        Optional.empty()));
        RegistryAccess registries = new RegistryAccess.ImmutableRegistryAccess(List.of(entries, scans));

        PlayerKnowledge knowledge = new PlayerKnowledge();
        ServerPlayer player = mock(ServerPlayer.class);
        Inventory inventory = mock(Inventory.class);
        Level level = mock(Level.class);
        when(player.getUUID()).thenReturn(new UUID(1L, 2L));
        when(player.registryAccess()).thenReturn(registries);
        when(player.getData(TTAttachments.KNOWLEDGE)).thenReturn(knowledge);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getContainerSize()).thenReturn(1);
        when(inventory.getItem(0)).thenReturn(ItemStack.EMPTY);
        when(player.level()).thenReturn(level);
        when(level.registryAccess()).thenReturn(registries);

        CraftReferenceHolder.rebuild(registries);
        assertFalse(ResearchManager.stageRequirementsMet(player, knowledge, auromancyEntry, AUROMANCY, 1));

        Item wand = TTItems.WAND.get();
        when(inventory.getItem(0)).thenReturn(new ItemStack(wand));
        assertTrue(ResearchManager.stageRequirementsMet(player, knowledge, auromancyEntry, AUROMANCY, 1));
        when(inventory.getItem(0)).thenReturn(ItemStack.EMPTY);
        try (MockedStatic<PlayerResearchAccess> access = mockStatic(PlayerResearchAccess.class)) {
            access.when(() -> PlayerResearchAccess.of(player)).thenReturn(knowledge);
            ResearchProgressionEvents.recordCrafted(player, new ItemStack(wand));
        }
        assertTrue(CraftReferenceHolder.isReference(registries, wand));
        assertTrue(knowledge.knows(ResearchManager.craftedKey(TTIds.rl("wand"))));
        assertTrue(ResearchManager.stageRequirementsMet(player, knowledge, auromancyEntry, AUROMANCY, 1));

        ResearchManager.applyAutoUnlock(player);
        assertFalse(knowledge.knows(ORE));
        ScanEntry oreScan = scans.get(ORE);
        assertNotNull(oreScan);
        ScanTarget target = new ScannedStack(new ItemStack(TTBlocks.ORE_AMBER.get()));
        assertTrue(oreScan.matches(player, target));
        try (MockedStatic<PlayerResearchAccess> access = mockStatic(PlayerResearchAccess.class)) {
            access.when(() -> PlayerResearchAccess.of(player)).thenReturn(knowledge);
            assertTrue(new ScanBindings().progressResearch(player, oreScan.key()));
        }
        assertTrue(knowledge.knows(ORE));
    }

    private static JsonObject loadResearch(String name) throws Exception {
        String path = "data/thaumaturge/thaumaturge/research_entry/" + name + ".json";
        InputStream stream =
                ResearchProgressionDataParityTest.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Missing loaded research resource " + path);
        }
        try (InputStream input = stream;
                InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
