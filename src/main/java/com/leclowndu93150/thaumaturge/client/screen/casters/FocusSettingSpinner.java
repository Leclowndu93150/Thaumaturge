package com.leclowndu93150.thaumaturge.client.screen.casters;

import com.leclowndu93150.thaumaturge.api.casters.SettingDefinition;
import com.leclowndu93150.thaumaturge.client.screen.TTScreenTextures;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class FocusSettingSpinner extends AbstractWidget {
    private static final int ARROW_SIZE = 10;
    private static final int U_MINUS = 20;
    private static final int U_PLUS = 30;
    private static final int V_ARROWS = 0;
    private static final int ATLAS = 256;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private final SettingDefinition definition;
    private final Map<String, Integer> values;
    private final Runnable onChange;
    private int index;

    public FocusSettingSpinner(
            int x, int y, int width, SettingDefinition definition, Map<String, Integer> values, Runnable onChange) {
        super(x, y, width + ARROW_SIZE, ARROW_SIZE, Component.empty());
        this.definition = definition;
        this.values = values;
        this.onChange = onChange;
        this.index = definition.values().indexOf(values.getOrDefault(definition.key(), definition.defaultValue()));
        setTooltip(Tooltip.create(Component.translatable(definition.nameKey())));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int bodyWidth = this.width - ARROW_SIZE;
        graphics.blit(
                TTScreenTextures.GUI_BASE, getX(), getY(), U_MINUS, V_ARROWS, ARROW_SIZE, ARROW_SIZE, ATLAS, ATLAS);
        graphics.blit(
                TTScreenTextures.GUI_BASE,
                getX() + bodyWidth,
                getY(),
                U_PLUS,
                V_ARROWS,
                ARROW_SIZE,
                ARROW_SIZE,
                ATLAS,
                ATLAS);
        Component value = definition.values().labelAt(index);
        var font = Minecraft.getInstance().font;
        graphics.drawString(
                font,
                value,
                getX() + (bodyWidth + ARROW_SIZE) / 2 - font.width(value) / 2,
                getY() + 1,
                TEXT_COLOR,
                true);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        int bodyWidth = this.width - ARROW_SIZE;
        if (mouseX < getX() + ARROW_SIZE) {
            step(-1);
        } else if (mouseX >= getX() + bodyWidth) {
            step(1);
        }
    }

    private void step(int delta) {
        index = Mth.clamp(index + delta, 0, definition.values().count() - 1);
        values.put(definition.key(), definition.values().valueAt(index));
        onChange.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
