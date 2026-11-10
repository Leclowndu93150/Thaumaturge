package com.leclowndu93150.thaumaturge.content.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.capability.ResearchStatus;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanBindings;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public final class ResearchDiscoveryBackendTest {
    private static final ResourceLocation NON_ENTRY_SCAN = TTIds.rl("scan/non_entry_discovery");

    public interface TestResearchLookup extends HolderLookup.RegistryLookup<IResearchEntry> {}

    @Test
    void nonEntryScanLearnsAndCompletesTheSyntheticResearchOnce() {
        ServerPlayer player = mock(ServerPlayer.class);
        RegistryAccess registries = mock(RegistryAccess.class);
        TestResearchLookup entries = mock(TestResearchLookup.class);
        when(player.registryAccess()).thenReturn(registries);
        when(registries.lookupOrThrow(IResearchEntry.REGISTRY_KEY)).thenReturn(entries);
        when(entries.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, NON_ENTRY_SCAN)))
                .thenReturn(Optional.empty());

        PlayerKnowledge backend = new PlayerKnowledge();
        try (MockedStatic<PlayerResearchAccess> access = mockStatic(PlayerResearchAccess.class)) {
            access.when(() -> PlayerResearchAccess.of(player)).thenReturn(backend);
            ScanBindings bindings = new ScanBindings();

            assertTrue(bindings.progressResearch(player, NON_ENTRY_SCAN));
            assertEquals(ResearchStatus.COMPLETE, backend.status(NON_ENTRY_SCAN));
            assertTrue(backend.takeSyncPending());

            assertFalse(bindings.progressResearch(player, NON_ENTRY_SCAN));
            assertFalse(backend.takeSyncPending());
        }
    }

    @Test
    void craftReferenceDiscoveryLearnsTheItemKeyAndIgnoresIneligibleItems() {
        ServerPlayer player = mock(ServerPlayer.class);
        RegistryAccess registries = mock(RegistryAccess.class);
        when(player.registryAccess()).thenReturn(registries);
        PlayerKnowledge backend = new PlayerKnowledge();
        ResourceLocation craftedKey = ResearchManager.craftedKey(BuiltInRegistries.ITEM.getKey(Items.DIAMOND));

        try (MockedStatic<PlayerResearchAccess> access = mockStatic(PlayerResearchAccess.class);
                MockedStatic<CraftReferenceHolder> references = mockStatic(CraftReferenceHolder.class)) {
            access.when(() -> PlayerResearchAccess.of(player)).thenReturn(backend);
            references
                    .when(() -> CraftReferenceHolder.isReference(registries, Items.DIAMOND))
                    .thenReturn(false);
            ResearchProgressionEvents.recordCrafted(player, new ItemStack(Items.DIAMOND));
            assertFalse(backend.knows(craftedKey));
            assertFalse(backend.takeSyncPending());

            references
                    .when(() -> CraftReferenceHolder.isReference(registries, Items.DIAMOND))
                    .thenReturn(true);
            ResearchProgressionEvents.recordCrafted(player, new ItemStack(Items.DIAMOND));
            assertTrue(backend.knows(craftedKey));
            assertEquals(ResearchStatus.IN_PROGRESS, backend.status(craftedKey));
            assertTrue(backend.takeSyncPending());

            ResearchProgressionEvents.recordCrafted(player, new ItemStack(Items.DIAMOND));
            assertFalse(backend.takeSyncPending());
        }
    }
}
