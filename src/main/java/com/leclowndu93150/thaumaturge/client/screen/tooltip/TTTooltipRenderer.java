package com.leclowndu93150.thaumaturge.client.screen.tooltip;

import com.leclowndu93150.thaumaturge.mixin.client.gui.GuiGraphicsAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public final class TTTooltipRenderer {
    private static final int PLAIN_LINE_MAX_WIDTH = 200;
    private static final int HALF_LINE_MAX_WIDTH = 400;

    private TTTooltipRenderer() {}

    public static void render(GuiGraphics graphics, Font font, List<Component> lines, int x, int y) {
        if (lines.isEmpty())
            return;
        List<ClientTooltipComponent> built = new ArrayList<>();
        for (Component line : lines) {
            if (line.getString().startsWith(HalfScaleTooltipLine.PREFIX)) {
                for (FormattedCharSequence part : font.split(stripPrefix(line), HALF_LINE_MAX_WIDTH)) {
                    built.add(new HalfScaleTooltipLine(part));
                }
            } else {
                for (FormattedCharSequence part : font.split(line, PLAIN_LINE_MAX_WIDTH)) {
                    built.add(new ClientTextTooltip(part));
                }
            }
        }
        ((GuiGraphicsAccessor) graphics).thaumaturge$renderTooltipInternal(font, built, x, y, DefaultTooltipPositioner.INSTANCE);
    }

    private static Component stripPrefix(Component line) {
        MutableComponent stripped = Component.empty();
        int[] toSkip = {HalfScaleTooltipLine.PREFIX.length()};
        line.visit((style, text) -> {
            int skip = Math.min(toSkip[0], text.length());
            toSkip[0] -= skip;
            if (skip < text.length()) {
                stripped.append(Component.literal(text.substring(skip)).withStyle(style));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return stripped;
    }
}
