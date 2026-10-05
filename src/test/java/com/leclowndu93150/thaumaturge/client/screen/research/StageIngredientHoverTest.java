package com.leclowndu93150.thaumaturge.client.screen.research;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.KnowledgeReward;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.client.render.research.EntryIconRenderer;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StageIngredientHoverTest {
    private EntryDetailScreen screen;
    private Minecraft minecraft;
    private IResearchEntry chapter;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        chapter = mock(IResearchEntry.class);
        when(chapter.nameKey()).thenReturn("test.chapter");
        screen = new EntryDetailScreen(Holder.direct(chapter), ResourceLocation.withDefaultNamespace("test"), null);
        minecraft = mock(Minecraft.class);
        minecraft.player = mock(LocalPlayer.class);
        minecraft.level = mock(ClientLevel.class);
        Field client = Screen.class.getDeclaredField("minecraft");
        client.setAccessible(true);
        client.set(screen, minecraft);
    }

    @Test
    @SuppressWarnings("unchecked")
    void researchPrerequisiteItemIconIsDecorativeAndExcludedFromLookup() throws ReflectiveOperationException {
        ResourceLocation prerequisite = ResourceLocation.withDefaultNamespace("prerequisite");
        RegistryAccess registries = mock(RegistryAccess.class);
        HolderLookup.RegistryLookup<IResearchEntry> entries = mock(HolderLookup.RegistryLookup.class);
        Holder.Reference<IResearchEntry> entry = mock(Holder.Reference.class);
        IResearchEntry research = mock(IResearchEntry.class);
        when(minecraft.player.registryAccess()).thenReturn(registries);
        when(registries.lookup(IResearchEntry.REGISTRY_KEY)).thenReturn(Optional.of(entries));
        when(entries.get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, prerequisite)))
                .thenReturn(Optional.of(entry));
        when(entry.value()).thenReturn(research);
        ItemStack stack = new ItemStack(Items.STONE);

        try (var icons = mockStatic(EntryIconRenderer.class)) {
            icons.when(() -> EntryIconRenderer.resolveIcon(research, 0)).thenReturn(stack);
            Method draw = EntryDetailScreen.class.getDeclaredMethod(
                    "drawPrereqIcon", GuiGraphics.class, int.class, int.class, ResourceLocation.class);
            draw.setAccessible(true);
            draw.invoke(screen, mock(GuiGraphics.class), 40, 60, prerequisite);
        }

        assertNull(screen.itemUnderMouse(45, 65));
        assertNull(screen.itemUnderMouse(56, 65));
    }

    @Test
    void renderedAspectPrerequisiteSupportsLookupWithoutRevealingUnknownAspects() throws ReflectiveOperationException {
        Holder<IAspect> aspect = Holder.direct(mock(IAspect.class));
        ResourceLocation prerequisite =
                ResourceLocation.fromNamespaceAndPath("thaumaturge", "scanned/aspect/thaumaturge/aer");
        ResourceKey<IAspect> aspectKey =
                ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("thaumaturge", "aer"));
        Method render = EntryDetailScreen.class.getDeclaredMethod(
                "renderResearchPrereqs",
                GuiGraphics.class,
                List.class,
                IPlayerKnowledge.class,
                int.class,
                int.class,
                int.class,
                int.class,
                boolean[].class,
                boolean.class);
        render.setAccessible(true);

        try (var aspects = mockStatic(Aspects.class);
                var tags = mockStatic(AspectTagRenderer.class);
                var knowledge = mockStatic(AspectKnowledgeAccess.class)) {
            aspects.when(() -> Aspects.resolve(minecraft.level, aspectKey)).thenReturn(aspect);
            render.invoke(
                    screen,
                    mock(GuiGraphics.class),
                    List.of(prerequisite),
                    mock(IPlayerKnowledge.class),
                    100,
                    120,
                    Integer.MIN_VALUE,
                    Integer.MIN_VALUE,
                    new boolean[1],
                    false);
            knowledge.when(() -> AspectKnowledgeAccess.isKnown(aspect)).thenReturn(true);

            assertSame(aspect, screen.aspectUnderMouse(110, 125).aspect().aspect());
            assertEquals(1, screen.aspectUnderMouse(110, 125).aspect().amount());
            assertNull(screen.aspectUnderMouse(125, 125));

            knowledge.when(() -> AspectKnowledgeAccess.isKnown(aspect)).thenReturn(false);
            assertNull(screen.aspectUnderMouse(110, 125));
            knowledge.when(() -> AspectKnowledgeAccess.isKnown(aspect)).thenReturn(true);
            Field overlay = EntryDetailScreen.class.getDeclaredField("showingKnowledge");
            overlay.setAccessible(true);
            overlay.setBoolean(screen, true);
            assertNull(screen.aspectUnderMouse(110, 125));
        }
    }

    @Test
    void observationCostUsesTheRenderedChipAndRequiredAmount() throws ReflectiveOperationException {
        IResearchStage stage = mock(IResearchStage.class);
        when(chapter.stages()).thenReturn(List.of(stage));
        Holder<IAspect> known = Holder.direct(mock(IAspect.class));
        Holder<IAspect> unknown = Holder.direct(mock(IAspect.class));
        AspectInstance required = new AspectInstance(known, 12);
        AspectList cost = AspectList.ofEntries(List.of(required, new AspectInstance(unknown, 8)));
        KnowledgeReward reward =
                new KnowledgeReward(KnowledgeType.OBSERVATION, Holder.direct(mock(IResearchCategory.class)), 1);
        IPlayerKnowledge knowledge = mock(IPlayerKnowledge.class);
        Method render = EntryDetailScreen.class.getDeclaredMethod(
                "renderKnowledgeRow",
                GuiGraphics.class,
                List.class,
                IPlayerKnowledge.class,
                int.class,
                int.class,
                int.class,
                int.class,
                boolean[].class,
                boolean.class);
        render.setAccessible(true);

        try (var notes = mockStatic(ResearchNotes.class);
                var access = mockStatic(KnowledgeAccess.class);
                var pools = mockStatic(AspectPools.class);
                var tags = mockStatic(AspectTagRenderer.class);
                var blend = mockStatic(GuiBlend.class);
                var discovery = mockStatic(AspectKnowledgeAccess.class)) {
            notes.when(() -> ResearchNotes.stageObservationCost(chapter, stage)).thenReturn(cost);
            access.when(() -> KnowledgeAccess.of(minecraft.player)).thenReturn(knowledge);
            pools.when(() -> AspectPools.isDiscovered(minecraft.player, known)).thenReturn(true);
            discovery.when(() -> AspectKnowledgeAccess.isKnown(known)).thenReturn(true);

            render.invoke(
                    screen,
                    mock(GuiGraphics.class),
                    List.of(reward),
                    knowledge,
                    100,
                    120,
                    Integer.MIN_VALUE,
                    Integer.MIN_VALUE,
                    new boolean[1],
                    false);

            assertEquals(required, screen.aspectUnderMouse(110, 125).aspect());
            assertNull(screen.aspectUnderMouse(128, 125));
        }
    }

    @Test
    void gatherRequirementLookupKeepsTheDisplayedVariantAndAmount() throws ReflectiveOperationException {
        Component variant = Component.literal("Required variant");
        ResearchRequirement requirement = new ResearchRequirement(
                HolderSet.direct(Items.STONE.builtInRegistryHolder()),
                DataComponentPatch.builder()
                        .set(DataComponents.CUSTOM_NAME, variant)
                        .build(),
                5);
        Method render = EntryDetailScreen.class.getDeclaredMethod(
                "renderItemRow",
                GuiGraphics.class,
                List.class,
                int.class,
                int.class,
                long.class,
                int.class,
                int.class,
                boolean.class,
                boolean[].class,
                boolean.class);
        render.setAccessible(true);

        try (var blend = mockStatic(GuiBlend.class)) {
            render.invoke(
                    screen,
                    mock(GuiGraphics.class, RETURNS_DEEP_STUBS),
                    List.of(requirement),
                    100,
                    120,
                    0L,
                    Integer.MIN_VALUE,
                    Integer.MIN_VALUE,
                    true,
                    new boolean[1],
                    true);
        }

        ItemStack hit = screen.itemUnderMouse(110, 125).stack();
        assertEquals(5, hit.getCount());
        assertEquals(variant, hit.get(DataComponents.CUSTOM_NAME));
        assertTrue(requirement.matches(hit));
    }
}
