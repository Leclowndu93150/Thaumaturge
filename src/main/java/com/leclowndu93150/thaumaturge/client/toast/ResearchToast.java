package com.leclowndu93150.thaumaturge.client.toast;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.render.research.EntryIconRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class ResearchToast implements Toast {
    private static final long DURATION_MS = 5000L;
    private static final ResourceLocation HUD = TTIds.rl("textures/gui/hud.png");
    private static final int TEX_SIZE = 256;
    private static final int BACKGROUND_U = 0;
    private static final int BACKGROUND_V = 224;
    private static final int BACKGROUND_W = 160;
    private static final int BACKGROUND_H = 32;
    private static final int ICON_X = 6;
    private static final int ICON_Y = 8;
    private static final int TEXT_X = 30;
    private static final int TITLE_Y = 7;
    private static final int NAME_Y = 18;
    private static final int TITLE_COLOR = 0xFFA23BF1;
    private static final int NAME_COLOR = 0xFFFFAB09;
    private static final float NAME_MAX_WIDTH = 124.0F;

    private final Component title;
    private final Component subtitle;
    private final Object icon;
    private final ResourceLocation id;

    public ResearchToast(ResourceLocation researchId, Component title, Component subtitle, Object icon) {
        this.id = researchId;
        this.title = title;
        this.subtitle = subtitle;
        this.icon = icon;
    }

    @Override
    public Toast.Visibility render(GuiGraphics graphics, ToastComponent manager, long timeVisible) {
        Font font = manager.getMinecraft().font;
        graphics.blit(
                HUD, 0, 0, (float) BACKGROUND_U, (float) BACKGROUND_V, BACKGROUND_W, BACKGROUND_H, TEX_SIZE, TEX_SIZE);
        EntryIconRenderer.drawResearchIcon(graphics, ICON_X, ICON_Y, icon, false);
        graphics.drawString(font, title, TEXT_X, TITLE_Y, TITLE_COLOR, false);
        float nameWidth = font.width(subtitle);
        if (nameWidth > NAME_MAX_WIDTH) {
            float scale = NAME_MAX_WIDTH / nameWidth;
            graphics.pose().pushPose();
            graphics.pose().translate(TEXT_X, NAME_Y, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(font, subtitle, 0, 0, NAME_COLOR, false);
            graphics.pose().popPose();
        } else {
            graphics.drawString(font, subtitle, TEXT_X, NAME_Y, NAME_COLOR, false);
        }
        return timeVisible >= DURATION_MS * manager.getNotificationDisplayTimeMultiplier()
                ? Toast.Visibility.HIDE
                : Toast.Visibility.SHOW;
    }

    @Override
    public Object getToken() {
        return id;
    }
}
