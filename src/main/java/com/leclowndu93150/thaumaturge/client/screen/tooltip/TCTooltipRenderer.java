package com.leclowndu93150.thaumaturge.client.screen.tooltip;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public final class TCTooltipRenderer {
    private static final int PLAIN_LINE_MAX_WIDTH = 200;
    private static final int HALF_LINE_MAX_WIDTH = 400;

    private TCTooltipRenderer() {}

    public static void render(GuiGraphicsExtractor graphics, Font font, List<Component> lines, int x, int y) {
        if (lines.isEmpty())
            return;
        List<ClientTooltipComponent> built = new ArrayList<>();
        for (Component line : lines) {
            String raw = line.getString();
            if (raw.startsWith(HalfScaleTooltipLine.PREFIX)) {
                Component stripped = stripPrefix(line);
                int wrapWidth = HALF_LINE_MAX_WIDTH;
                List<FormattedText> wrapped = font.getSplitter().splitLines(stripped, wrapWidth, Style.EMPTY);
                for (FormattedText part : wrapped) {
                    built.add(new HalfScaleTooltipLine(Component.literal(part.getString()).getVisualOrderText()));
                }
            } else {
                List<FormattedText> wrapped = font.getSplitter().splitLines(line, PLAIN_LINE_MAX_WIDTH, Style.EMPTY);
                for (FormattedText part : wrapped) {
                    built.add(new ClientTextTooltip(Component.literal(part.getString()).getVisualOrderText()));
                }
            }
        }
        graphics.tooltip(font, built, x, y, DefaultTooltipPositioner.INSTANCE, null);
    }

    private static Component stripPrefix(Component line) {
        String s = line.getString();
        if (s.startsWith(HalfScaleTooltipLine.PREFIX)) {
            s = s.substring(HalfScaleTooltipLine.PREFIX.length());
        }
        return Component.literal(s);
    }
}
