package com.leclowndu93150.thaumaturge.client.screen.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.research.table.MenuResearchTable;
import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Field;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

class ResearchTableSearchInputTest {
    private final LocalPlayer player = mock(LocalPlayer.class);
    private final Minecraft game = mock(Minecraft.class);
    private final ResearchTableScreen screen =
            new ResearchTableScreen(mock(MenuResearchTable.class), new Inventory(player), Component.empty());
    private final EditBox search = new EditBox(font(), 0, 0, 80, 14, Component.empty());

    @BeforeEach
    void setup() throws ReflectiveOperationException {
        Options options = mock(Options.class);
        KeyMapping inventory = mock(KeyMapping.class);
        when(inventory.matches(GLFW.GLFW_KEY_E, 0)).thenReturn(true);
        when(inventory.isActiveAndMatches(InputConstants.getKey(GLFW.GLFW_KEY_E, 0)))
                .thenReturn(true);
        setField(Options.class, options, "keyInventory", inventory);
        setField(Options.class, options, "keyHotbarSlots", new KeyMapping[0]);
        when(screen.getMenu().getCarried()).thenReturn(ItemStack.EMPTY);
        setField(Minecraft.class, game, "options", options);
        setField(Minecraft.class, game, "player", player);
        setField(Screen.class, screen, "minecraft", game);
        screen.setFocused(search);
        search.setFocused(true);
    }

    @Test
    void inventoryLetterTypesIntoSearchWithoutClosingContainer() {
        assertTrue(screen.keyPressed(GLFW.GLFW_KEY_E, 0, 0));
        assertTrue(screen.charTyped('e', 0));
        assertEquals("e", search.getValue());
        verify(player, never()).closeContainer();
    }

    @Test
    void editingKeysAreHandledOnce() {
        try (var keys = mockStatic(Screen.class)) {
            search.setValue("terra");
            assertTrue(screen.keyPressed(GLFW.GLFW_KEY_BACKSPACE, 0, 0));
            assertEquals("terr", search.getValue());
        }
    }

    @Test
    void escapeStillClosesWhileSearching() {
        assertTrue(screen.keyPressed(GLFW.GLFW_KEY_ESCAPE, 0, 0));
        verify(player).closeContainer();
    }

    @Test
    void inventoryKeyStillClosesWhenSearchIsNotFocused() {
        search.setFocused(false);
        assertTrue(screen.keyPressed(GLFW.GLFW_KEY_E, 0, 0));
        verify(player).closeContainer();
    }

    private static void setField(Class<?> owner, Object target, String name, Object value)
            throws ReflectiveOperationException {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Font font() {
        Font font = mock(Font.class);
        when(font.plainSubstrByWidth(anyString(), anyInt())).thenAnswer(call -> call.getArgument(0));
        when(font.plainSubstrByWidth(anyString(), anyInt(), anyBoolean())).thenAnswer(call -> call.getArgument(0));
        return font;
    }
}
