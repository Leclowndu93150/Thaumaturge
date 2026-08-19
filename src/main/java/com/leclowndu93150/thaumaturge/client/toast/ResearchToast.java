package com.leclowndu93150.thaumaturge.client.toast;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.render.research.EntryIconRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ResearchToast implements Toast {
    private static final long DURATION_MS = 5000L;
    private static final Identifier HUD = TCIds.rl("textures/gui/hud.png");
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
    private final Identifier id;
    private Toast.Visibility wantedVisibility = Visibility.SHOW;

    public ResearchToast(Identifier researchId, Component title, Component subtitle, Object icon) {
        this.id = researchId;
        this.title = title;
        this.subtitle = subtitle;
        this.icon = icon;
    }

    @Override
    public Visibility getWantedVisibility() {
        return wantedVisibility;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        wantedVisibility = fullyVisibleForMs > DURATION_MS ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, HUD, 0, 0, (float) BACKGROUND_U, (float) BACKGROUND_V, BACKGROUND_W, BACKGROUND_H, BACKGROUND_W, BACKGROUND_H, TEX_SIZE, TEX_SIZE);
        EntryIconRenderer.drawResearchIcon(graphics, ICON_X, ICON_Y, icon, false);
        graphics.text(font, title, TEXT_X, TITLE_Y, TITLE_COLOR, false);
        float nameWidth = font.width(subtitle);
        if (nameWidth > NAME_MAX_WIDTH) {
            float scale = NAME_MAX_WIDTH / nameWidth;
            graphics.pose().pushMatrix();
            graphics.pose().translate(TEXT_X, NAME_Y);
            graphics.pose().scale(scale, scale);
            graphics.text(font, subtitle, 0, 0, NAME_COLOR, false);
            graphics.pose().popMatrix();
        } else {
            graphics.text(font, subtitle, TEXT_X, NAME_Y, NAME_COLOR, false);
        }
    }

    @Override
    public Object getToken() {
        return id;
    }
}
