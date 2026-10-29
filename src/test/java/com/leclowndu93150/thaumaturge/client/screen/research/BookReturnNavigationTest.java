package com.leclowndu93150.thaumaturge.client.screen.research;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.client.screen.widget.TTButton;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeClientConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class BookReturnNavigationTest {
    private static final ResourceLocation RECIPE = ResourceLocation.withDefaultNamespace("test_recipe");

    @Test
    void rightClickClosesTheInsertBeforeReturningToTheSameMap() throws Exception {
        Minecraft game = mock(Minecraft.class);
        Screen map = mock(Screen.class);
        EntryDetailScreen detail = detail(game, map);
        field(EntryDetailScreen.class, "shownRecipe").set(detail, RECIPE);

        try (MockedStatic<ThaumaturgeClientConfig> config = mockStatic(ThaumaturgeClientConfig.class)) {
            config.when(ThaumaturgeClientConfig::useBookReturnButton).thenReturn(false);

            assertTrue(detail.mouseClicked(100, 100, 1));
            assertNull(field(EntryDetailScreen.class, "shownRecipe").get(detail));
            verifyNoInteractions(game);

            assertTrue(detail.mouseClicked(100, 100, 1));
            verify(game).setScreen(map);
        }
    }

    @Test
    void buttonModeLeavesRightClickInactive() throws Exception {
        Minecraft game = mock(Minecraft.class);
        EntryDetailScreen detail = detail(game, mock(Screen.class));
        field(EntryDetailScreen.class, "shownRecipe").set(detail, RECIPE);

        try (MockedStatic<ThaumaturgeClientConfig> config = mockStatic(ThaumaturgeClientConfig.class)) {
            config.when(ThaumaturgeClientConfig::useBookReturnButton).thenReturn(true);

            assertFalse(detail.mouseClicked(100, 100, 1));
            assertSame(RECIPE, field(EntryDetailScreen.class, "shownRecipe").get(detail));
            verifyNoInteractions(game);
        }
    }

    @Test
    void returnControlClosesEveryInsertBeforeReturningToTheSameMap() throws Exception {
        try (MockedStatic<ThaumaturgeClientConfig> config = mockStatic(ThaumaturgeClientConfig.class);
                MockedStatic<Minecraft> minecraft = mockStatic(Minecraft.class)) {
            config.when(ThaumaturgeClientConfig::useBookReturnButton).thenReturn(true);

            for (String insert : List.of("shownRecipe", "showingAspects", "showingKnowledge", "showingConstruct")) {
                Minecraft game = mock(Minecraft.class);
                when(game.getSoundManager()).thenReturn(mock(SoundManager.class));
                minecraft.when(Minecraft::getInstance).thenReturn(game);
                Screen map = mock(Screen.class);
                EntryDetailScreen detail = detail(game, map);
                detail.width = 320;
                detail.height = 240;
                Field state = field(EntryDetailScreen.class, insert);
                state.set(detail, insert.equals("shownRecipe") ? RECIPE : true);

                Method create = EntryDetailScreen.class.getDeclaredMethod("createReturnControl");
                create.setAccessible(true);
                TTButton control = (TTButton) create.invoke(detail);
                field(EntryDetailScreen.class, "returnControl").set(detail, control);
                Method attach = Screen.class.getDeclaredMethod("addRenderableWidget", GuiEventListener.class);
                attach.setAccessible(true);
                attach.invoke(detail, control);

                assertTrue(control.getX() >= 0 && control.getX() + control.getWidth() <= detail.width);
                assertTrue(control.getY() >= 0 && control.getY() + control.getHeight() <= detail.height);
                assertTrue(detail.mouseClicked(control.getX() + 2, control.getY() + 2, 0));
                assertTrue(insert.equals("shownRecipe") ? state.get(detail) == null : !state.getBoolean(detail));
                verify(game, org.mockito.Mockito.never()).setScreen(map);

                assertTrue(detail.mouseClicked(control.getX() + 2, control.getY() + 2, 0));
                verify(game).setScreen(map);
            }
        }
    }

    private static EntryDetailScreen detail(Minecraft game, Screen map) throws Exception {
        IResearchEntry research = mock(IResearchEntry.class);
        when(research.nameKey()).thenReturn("test.research.title");
        EntryDetailScreen detail = new EntryDetailScreen(Holder.direct(research), RECIPE, map);
        field(Screen.class, "minecraft").set(detail, game);
        return detail;
    }

    private static Field field(Class<?> owner, String name) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
