package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.SpellProblem;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class SpellSummaryPanel {
    private static final int STYLE_LABEL_W = 28;
    private static final int STYLE_H = 14;
    private static final int STYLE_TEXT_X = 5;
    private static final int STYLE_TEXT_Y = 3;
    private static final int STYLE_GLYPH_RIGHT = 11;
    private static final int STYLE_STEP = 18;
    private static final int OPTION_H = 12;
    private static final int OPTION_PAD = 2;
    private static final int ICON = 12;
    private static final int BAR_X = 15;
    private static final int BAR_Y = 1;
    private static final int BAR_H = 9;
    private static final int BAR_RESERVE = 16 + 32;
    private static final int FILL_INSET = 2;
    private static final int BAR_STEP = 15;
    private static final int COST_TEXT_X = 15;
    private static final int COST_TEXT_Y = 1;
    private static final int XP_ORB_X = 64;
    private static final int XP_TEXT_GAP = 3;
    private static final int COST_STEP = 15;
    private static final int PROBLEM_TEXT_X = 12;
    private static final int PROBLEM_STEP = 11;
    private static final int PERCENT = 100;

    private final DraftHost host;
    private boolean styleOpen;
    private int styleX;
    private int styleY;
    private int styleW;

    public SpellSummaryPanel(DraftHost host) {
        this.host = host;
    }

    public void render(GuiGraphics graphics, FocalLayout layout, int top, float inscription, int mouseX, int mouseY) {
        int x = layout.rightX0();
        int right = layout.rightX1();
        int y = top;
        SpellSummary summary = host.summary();
        FocalDraw.text(graphics, host.font(), SpellText.inspectorLabel("spell"), x, y, FocalColors.LABEL);
        y += NodeInspector.LABEL_STEP;
        FocalDraw.text(graphics, host.font(), SpellText.inspectorLabel("cast"), x, y + STYLE_TEXT_Y, FocalColors.GREY);
        styleX = x + STYLE_LABEL_W;
        styleY = y;
        styleW = FocalLayout.RIGHT_W - STYLE_LABEL_W;
        FocalDraw.field(graphics, styleX, styleY, styleW, STYLE_H);
        FocalDraw.text(
                graphics,
                host.font(),
                SpellText.style(host.draft().style()),
                styleX + STYLE_TEXT_X,
                y + STYLE_TEXT_Y,
                FocalColors.WHITE);
        FocalDraw.sprite(
                graphics,
                FocalSprites.GLYPH_DOWN,
                right - STYLE_GLYPH_RIGHT,
                y + STYLE_TEXT_Y,
                FocalSprites.GLYPH_SIZE,
                FocalSprites.GLYPH_SIZE);
        y += STYLE_STEP;
        bar(graphics, x, right, y, summary, mouseX, mouseY);
        y += BAR_STEP;
        costs(graphics, x, y, summary, mouseX, mouseY);
        y += COST_STEP;
        if (inscription > 0.0F) {
            FocalDraw.text(
                    graphics,
                    host.font(),
                    SpellText.inscribing(Math.round(inscription * PERCENT)),
                    x,
                    y + COST_TEXT_Y,
                    FocalColors.AQUA);
            y += PROBLEM_STEP;
        }
        for (SpellProblem problem : summary.problems()) {
            List<FormattedCharSequence> lines =
                    host.font().split(problem.message(), FocalLayout.RIGHT_W - PROBLEM_TEXT_X);
            if (y + PROBLEM_STEP * lines.size() > layout.bodyY1()) {
                break;
            }
            FocalDraw.sprite(graphics, FocalSprites.WARN, x, y, FocalSprites.WARN_SIZE, FocalSprites.WARN_SIZE);
            int colour = problem.fatal() ? FocalColors.ERROR : FocalColors.LABEL;
            for (FormattedCharSequence line : lines) {
                graphics.drawString(host.font(), line, x + PROBLEM_TEXT_X, y + COST_TEXT_Y, colour, true);
                y += PROBLEM_STEP;
            }
        }
    }

    public void renderOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!styleOpen) {
            return;
        }
        CastStyle[] styles = CastStyle.values();
        int top = styleY + STYLE_H;
        FocalDraw.field(graphics, styleX, top, styleW, styles.length * OPTION_H + OPTION_PAD * 2);
        for (int index = 0; index < styles.length; index++) {
            int y = top + OPTION_PAD + index * OPTION_H;
            if (FocalDraw.over(mouseX, mouseY, styleX, y, styleW, OPTION_H)) {
                graphics.fill(
                        styleX + OPTION_PAD, y, styleX + styleW - OPTION_PAD, y + OPTION_H, FocalColors.HIGHLIGHT);
                host.tooltip(SpellText.styleHelp(styles[index]), mouseX, mouseY);
            }
            FocalDraw.text(
                    graphics,
                    host.font(),
                    SpellText.style(styles[index]),
                    styleX + STYLE_TEXT_X,
                    y + OPTION_PAD,
                    styles[index] == host.draft().style() ? FocalColors.LABEL : FocalColors.WHITE);
        }
    }

    private void bar(GuiGraphics graphics, int x, int right, int y, SpellSummary summary, int mouseX, int mouseY) {
        GuiBlend.blitTinted(
                graphics,
                FocalSprites.COMPLEXITY,
                x,
                y - 1,
                ICON,
                ICON,
                0.0F,
                0.0F,
                FocalSprites.COMPLEXITY_TEXTURE,
                FocalSprites.COMPLEXITY_TEXTURE,
                FocalSprites.COMPLEXITY_TEXTURE,
                FocalSprites.COMPLEXITY_TEXTURE,
                0xFFFFFFFF);
        int barW = FocalLayout.RIGHT_W - BAR_RESERVE;
        FocalDraw.field(graphics, x + BAR_X, y + BAR_Y, barW, BAR_H);
        int budget = Math.max(1, summary.budget());
        boolean over = summary.budget() > 0 && summary.complexity() > summary.budget();
        int fill = Math.min(barW - FILL_INSET * 2, (barW - FILL_INSET * 2) * summary.complexity() / budget);
        if (fill > 0) {
            FocalDraw.sprite(
                    graphics,
                    over ? FocalSprites.BAR_FILL_OVER : FocalSprites.BAR_FILL,
                    x + BAR_X + FILL_INSET,
                    y + BAR_Y + FILL_INSET,
                    fill,
                    FocalSprites.BAR_FILL_H);
        }
        Component text = summary.budget() > 0
                ? Component.translatable("gui.thaumaturge.fraction", summary.complexity(), summary.budget())
                : Component.literal(Integer.toString(summary.complexity()));
        FocalDraw.textRight(
                graphics, host.font(), text, right, y + BAR_Y, over ? FocalColors.ERROR : FocalColors.WHITE);
        if (FocalDraw.over(mouseX, mouseY, x, y - 1, FocalLayout.RIGHT_W, ICON)) {
            host.tooltip(SpellText.complexityLine(summary.complexity(), summary.budget()), mouseX, mouseY);
        }
    }

    private void costs(GuiGraphics graphics, int x, int y, SpellSummary summary, int mouseX, int mouseY) {
        GuiBlend.blitTinted(
                graphics,
                FocalSprites.CRYSTAL,
                x,
                y - 1,
                ICON,
                ICON,
                0.0F,
                0.0F,
                FocalSprites.CRYSTAL_TEXTURE,
                FocalSprites.CRYSTAL_TEXTURE,
                FocalSprites.CRYSTAL_TEXTURE,
                FocalSprites.CRYSTAL_TEXTURE,
                FocalColors.AQUA);
        Component vis = FocusItems.formatVis(summary.vis());
        FocalDraw.text(graphics, host.font(), vis, x + COST_TEXT_X, y + COST_TEXT_Y, FocalColors.AQUA);
        int perCastX = x + COST_TEXT_X + host.font().width(vis) + XP_TEXT_GAP;
        FocalDraw.flatText(
                graphics,
                host.font(),
                SpellText.inspectorLabel("per_cast"),
                perCastX,
                y + COST_TEXT_Y,
                FocalColors.DIM);
        int orbX = x + XP_ORB_X + host.font().width(vis);
        GuiBlend.blitTinted(
                graphics,
                FocalSprites.XP_ORB,
                orbX,
                y,
                FocalSprites.XP_ORB_SIZE,
                FocalSprites.XP_ORB_SIZE,
                FocalSprites.XP_ORB_U,
                FocalSprites.XP_ORB_V,
                FocalSprites.XP_ORB_SIZE,
                FocalSprites.XP_ORB_SIZE,
                FocalSprites.XP_ORB_TEXTURE,
                FocalSprites.XP_ORB_TEXTURE,
                0xFFFFFFFF);
        FocalDraw.text(
                graphics,
                host.font(),
                Component.literal(Integer.toString(summary.xp())),
                orbX + FocalSprites.XP_ORB_SIZE + XP_TEXT_GAP,
                y + COST_TEXT_Y,
                FocalColors.XP);
        if (FocalDraw.over(mouseX, mouseY, x, y - 1, orbX - x, ICON)) {
            host.tooltip(SpellText.visHelp(vis), mouseX, mouseY);
        } else if (FocalDraw.over(mouseX, mouseY, orbX, y - 1, FocalLayout.RIGHT_W - (orbX - x), ICON)) {
            host.tooltip(SpellText.inscribeTooltip(summary, host.inscribeCheck()), mouseX, mouseY);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (styleOpen) {
            CastStyle[] styles = CastStyle.values();
            int top = styleY + STYLE_H + OPTION_PAD;
            for (int index = 0; index < styles.length; index++) {
                if (FocalDraw.over(mouseX, mouseY, styleX, top + index * OPTION_H, styleW, OPTION_H)) {
                    CastStyle chosen = styles[index];
                    host.edit(spell -> spell.withStyle(chosen));
                    host.playClick();
                }
            }
            styleOpen = false;
            return true;
        }
        if (FocalDraw.over(mouseX, mouseY, styleX, styleY, styleW, STYLE_H)) {
            styleOpen = true;
            host.playClick();
            return true;
        }
        return false;
    }

    public boolean open() {
        return styleOpen;
    }
}
