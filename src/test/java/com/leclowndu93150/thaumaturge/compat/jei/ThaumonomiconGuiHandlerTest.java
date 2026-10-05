package com.leclowndu93150.thaumaturge.compat.jei;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayWidget.ItemHit;
import com.leclowndu93150.thaumaturge.client.screen.research.EntryDetailScreen;
import com.leclowndu93150.thaumaturge.client.screen.research.EntryDetailScreen.AspectHit;
import com.leclowndu93150.thaumaturge.compat.jei.ingredient.AspectIngredientType;
import java.util.Optional;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IScreenHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ThaumonomiconGuiHandlerTest {
    private IScreenHandler<EntryDetailScreen> handler;
    private final EntryDetailScreen screen = mock(EntryDetailScreen.class);
    private final IClickableIngredientFactory factory = mock(IClickableIngredientFactory.class);

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        IGuiHandlerRegistration registration = mock(IGuiHandlerRegistration.class);
        new ThaumaturgeJEIPlugin().registerGuiHandlers(registration);
        ArgumentCaptor<IScreenHandler<EntryDetailScreen>> capture =
                ArgumentCaptor.forClass((Class) IScreenHandler.class);
        verify(registration).addGuiScreenHandler(eq(EntryDetailScreen.class), capture.capture());
        handler = capture.getValue();
    }

    @Test
    void guiPropertiesEnableJeiInputAndReserveTheEntireBookViewport() {
        screen.width = 640;
        screen.height = 360;

        var properties = handler.apply(screen);

        assertNotNull(properties); // JEI skips all keyboard lookup when these are null.
        assertEquals(EntryDetailScreen.class, properties.screenClass());
        assertEquals(0, properties.guiLeft());
        assertEquals(0, properties.guiTop());
        assertEquals(properties.screenWidth(), properties.guiRight());
        assertEquals(properties.screenHeight(), properties.guiBottom());
        assertEquals(640, properties.screenWidth());
        assertEquals(360, properties.screenHeight());
    }

    @Test
    @SuppressWarnings("unchecked")
    void itemLookupUsesTheDisplayedStackAndItsActualBounds() {
        ItemStack stack = new ItemStack(Items.STONE, 5);
        when(screen.itemUnderMouse(110, 125)).thenReturn(new ItemHit(stack, 109, 120));
        IClickableIngredientFactory.IBuilder<ItemStack> builder = mock(IClickableIngredientFactory.IBuilder.class);
        IClickableIngredient<ItemStack> ingredient = mock(IClickableIngredient.class);
        when(factory.createBuilder(stack)).thenReturn(builder);
        when(builder.buildWithArea(109, 120, 16, 16)).thenReturn(Optional.of(ingredient));

        assertSame(
                ingredient,
                handler.getClickableIngredientUnderMouse(factory, screen, 110, 125)
                        .orElseThrow());
        verify(screen, never()).aspectUnderMouse(anyDouble(), anyDouble());
    }

    @Test
    @SuppressWarnings("unchecked")
    void aspectLookupUsesJeisAspectTypeAndTheRenderedChipBounds() {
        AspectInstance aspect = new AspectInstance(Holder.direct(mock(IAspect.class)), 12);
        when(screen.aspectUnderMouse(110, 125)).thenReturn(new AspectHit(aspect, 109, 120));
        IClickableIngredientFactory.IBuilder<AspectInstance> builder = mock(IClickableIngredientFactory.IBuilder.class);
        IClickableIngredient<AspectInstance> ingredient = mock(IClickableIngredient.class);
        when(factory.createBuilder(AspectIngredientType.INSTANCE, aspect)).thenReturn(builder);
        when(builder.buildWithArea(109, 120, 16, 16)).thenReturn(Optional.of(ingredient));

        assertSame(
                ingredient,
                handler.getClickableIngredientUnderMouse(factory, screen, 110, 125)
                        .orElseThrow());
    }

    @Test
    void emptyHoverLeavesNormalBookInputAlone() {
        assertTrue(handler.getClickableIngredientUnderMouse(factory, screen, 110, 125)
                .isEmpty());
        verifyNoInteractions(factory);
    }
}
