package com.leclowndu93150.thaumaturge.client.screen.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ThaumonomiconBrowserSearchComponentsTest {
    @Test
    void searchMatchesCustomNameOnCraftRequirementStack() throws ReflectiveOperationException {
        Component customName = Component.literal("Custom research relic");
        DataComponentPatch components = DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_NAME, customName)
                .build();
        ResearchRequirement requirement = requirement(components);

        assertEquals(List.of("Custom research relic"), recipeSearchResults("custom research", requirement));
    }

    @Test
    void searchStillFindsCraftRequirementByItsDefaultItemName() throws ReflectiveOperationException {
        ResearchRequirement requirement = requirement(DataComponentPatch.EMPTY);

        assertEquals(List.of("Stone"), recipeSearchResults("stone", requirement));
    }

    private static ResearchRequirement requirement(DataComponentPatch components) {
        return new ResearchRequirement(
                HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.STONE)), components, 1);
    }

    private static List<String> recipeSearchResults(String query, ResearchRequirement requirement)
            throws ReflectiveOperationException {
        ThaumonomiconBrowserScreen screen = new ThaumonomiconBrowserScreen();
        LocalPlayer player = mock(LocalPlayer.class);
        Minecraft minecraft = mock(Minecraft.class);
        minecraft.player = player;
        Field minecraftField = Screen.class.getDeclaredField("minecraft");
        minecraftField.setAccessible(true);
        minecraftField.set(screen, minecraft);

        IResearchStage stage = mock(IResearchStage.class);
        when(stage.craft()).thenReturn(List.of(requirement));
        IResearchEntry entry = mock(IResearchEntry.class);
        when(entry.nameKey()).thenReturn("research.test.entry.title");
        when(entry.stages()).thenReturn(List.of(stage));
        ResourceLocation entryId = ResourceLocation.fromNamespaceAndPath("test", "entry");
        Holder<IResearchEntry> entryHolder = Holder.direct(entry);
        Class<?> entryNodeClass = Class.forName(
                "com.leclowndu93150.thaumaturge.client.screen.research.ThaumonomiconBrowserScreen$EntryNode");
        Constructor<?> entryNodeConstructor = entryNodeClass.getDeclaredConstructor(
                ResourceLocation.class, IResearchEntry.class, Holder.class, Holder.Reference.class);
        entryNodeConstructor.setAccessible(true);
        Object node = entryNodeConstructor.newInstance(entryId, entry, entryHolder, null);
        Field allEntriesField = ThaumonomiconBrowserScreen.class.getDeclaredField("allEntries");
        allEntriesField.setAccessible(true);
        Method listAdd = List.class.getMethod("add", Object.class);
        listAdd.invoke(allEntriesField.get(screen), node);

        PlayerResearch knowledge = mock(PlayerResearch.class);
        when(knowledge.knownEntries()).thenReturn(Set.of(entryId));
        when(knowledge.stageOf(entryId)).thenReturn(0);
        try (MockedStatic<PlayerResearchAccess> researchAccess = mockStatic(PlayerResearchAccess.class)) {
            researchAccess.when(() -> PlayerResearchAccess.of(player)).thenReturn(knowledge);
            Method search = ThaumonomiconBrowserScreen.class.getDeclaredMethod("onSearchChanged", String.class);
            search.setAccessible(true);
            search.invoke(screen, query);
        }

        Field searchResultsField = ThaumonomiconBrowserScreen.class.getDeclaredField("searchResults");
        searchResultsField.setAccessible(true);
        List<?> searchResults = (List<?>) searchResultsField.get(screen);
        List<String> names = new ArrayList<>();
        for (Object result : searchResults) {
            Field displayNameField = result.getClass().getDeclaredField("displayName");
            displayNameField.setAccessible(true);
            names.add((String) displayNameField.get(result));
        }
        return names;
    }
}
