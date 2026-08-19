package com.leclowndu93150.thaumaturge.client.screen.widget;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public sealed interface TCButtonIcon {
    void draw(GuiGraphicsExtractor graphics, int x, int y, int size, int tintColor);

    record AspectIcon(Holder<IAspect> aspect) implements TCButtonIcon {
        @Override
        public void draw(GuiGraphicsExtractor graphics, int x, int y, int size, int tintColor) {
            IAspect value = aspect.value();
            int color = (tintColor & 0xFF000000) | (value.color() & 0x00FFFFFF);
            graphics.blit(RenderPipelines.GUI_TEXTURED, value.texture(), x, y, 0.0F, 0.0F, size, size, size, size, size, size, color);
        }
    }

    record TextureIcon(Identifier texture, int textureWidth, int textureHeight) implements TCButtonIcon {
        public TextureIcon(Identifier texture) {
            this(texture, 16, 16);
        }

        @Override
        public void draw(GuiGraphicsExtractor graphics, int x, int y, int size, int tintColor) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, size, size, size, size, textureWidth, textureHeight, tintColor);
        }
    }

    record StackIcon(ItemStack stack) implements TCButtonIcon {
        @Override
        public void draw(GuiGraphicsExtractor graphics, int x, int y, int size, int tintColor) {
            graphics.item(stack, x, y);
            graphics.itemDecorations(Minecraft.getInstance().font, stack, x, y);
        }
    }
}
