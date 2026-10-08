package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.SpellProblem;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import com.leclowndu93150.thaumaturge.client.screen.casters.SpellPartIcons;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public final class SpellCanvas {
    private static final float[] ZOOMS = {0.5F, 0.75F, 1.0F, 1.25F, 1.5F, 2.0F};
    private static final int DEFAULT_ZOOM = 2;
    private static final int PERCENT = 100;
    private static final int HIT_RADIUS = 12;
    private static final int LINK_OFFSET_X = 6;
    private static final int LINK_OFFSET_Y = 22;
    private static final int SPLIT_LEFT_OFFSET = 4;
    private static final int SPLIT_RIGHT_OFFSET = 12;
    private static final int SPLIT_OFFSET_Y = 36;
    private static final int MID_OFFSET = 12;

    private final DraftHost host;
    private int zoom = DEFAULT_ZOOM;
    private double panX;
    private double panY;
    private boolean panning;
    private List<SpellTreeLayout.Placed> placed = List.of();

    public SpellCanvas(DraftHost host) {
        this.host = host;
    }

    private float scale() {
        return ZOOMS[zoom];
    }

    private double originX(FocalLayout layout) {
        return layout.canvasX0() + layout.canvasWidth() / 2.0 + panX;
    }

    private double originY(FocalLayout layout) {
        return layout.bodyY0() + FocalLayout.ROOT_Y + panY;
    }

    public void relayout() {
        SpellNode root = host.draft().root();
        Selection selection = host.selection();
        placed = SpellTreeLayout.layout(root, path -> {
            SpellNode node = SpellPaths.at(root, path);
            int max = host.maxChildren(node);
            boolean selected = selection.path().equals(path);
            boolean starving = host.part(node.part())
                    .map(part -> node.children().size() < part.behavior().minChildren())
                    .orElse(false);
            return node.children().size() < max
                    && (selected || starving || node.children().isEmpty() && path.isEmpty());
        });
    }

    public void render(GuiGraphics graphics, FocalLayout layout, int mouseX, int mouseY) {
        int x0 = layout.canvasX0();
        int y0 = layout.bodyY0();
        FocalDraw.sprite(graphics, FocalSprites.WELL, x0, y0, layout.canvasWidth(), layout.canvasHeight());
        FocalDraw.sprite(
                graphics,
                FocalSprites.SIGIL,
                x0 + layout.canvasWidth() / 2 - FocalSprites.SIGIL_SIZE / 2,
                y0 + layout.canvasHeight() / 2 - FocalSprites.SIGIL_SIZE / 2,
                FocalSprites.SIGIL_SIZE,
                FocalSprites.SIGIL_SIZE);
        graphics.enableScissor(
                x0 + FocalLayout.CANVAS_CLIP,
                y0 + FocalLayout.CANVAS_CLIP,
                layout.canvasX1() - FocalLayout.CANVAS_CLIP,
                layout.bodyY1() - FocalLayout.CANVAS_CLIP);
        graphics.pose().pushPose();
        graphics.pose().translate((float) originX(layout), (float) originY(layout), 0.0F);
        graphics.pose().scale(scale(), scale(), 1.0F);
        for (SpellTreeLayout.Placed node : placed) {
            if (!node.path().isEmpty()) {
                links(graphics, node);
            }
        }
        SpellTreeLayout.Placed hovered = layout.inCanvas(mouseX, mouseY) ? hit(layout, mouseX, mouseY) : null;
        for (SpellTreeLayout.Placed node : placed) {
            int cx = node.column() * FocalLayout.COLUMN_PITCH;
            int cy = node.row() * FocalLayout.ROW_PITCH;
            if (node.ghost()) {
                int half = FocalSprites.CELL_SIZE / 2;
                FocalDraw.sprite(
                        graphics,
                        FocalSprites.CELL,
                        cx - half,
                        cy - half,
                        FocalSprites.CELL_SIZE,
                        FocalSprites.CELL_SIZE,
                        node == hovered ? FocalColors.WHITE : FocalColors.GHOST_ALPHA);
            } else {
                SpellPartIcons.draw(
                        graphics,
                        host.registries(),
                        node.node(),
                        cx,
                        cy,
                        FocalLayout.NODE_BACK,
                        FocalLayout.NODE_GLYPH,
                        FocalLayout.FLOW_GLYPH,
                        1.0F);
            }
            if (selected(node)) {
                int half = FocalSprites.SELECTION_SIZE / 2;
                FocalDraw.sprite(
                        graphics,
                        FocalSprites.SELECTION,
                        cx - half,
                        cy - half,
                        FocalSprites.SELECTION_SIZE,
                        FocalSprites.SELECTION_SIZE);
            }
        }
        graphics.pose().popPose();
        graphics.disableScissor();
        controls(graphics, layout, mouseX, mouseY);
        if (hovered != null) {
            host.tooltip(tooltip(hovered), mouseX, mouseY);
        }
    }

    private void links(GuiGraphics graphics, SpellTreeLayout.Placed node) {
        int x = node.column() * FocalLayout.COLUMN_PITCH;
        int y = node.row() * FocalLayout.ROW_PITCH;
        int tint = node.ghost() ? FocalColors.GHOST_ALPHA : FocalColors.WHITE;
        strip(
                graphics,
                x - LINK_OFFSET_X,
                y - LINK_OFFSET_Y,
                FocalSprites.LINK_U,
                FocalSprites.LINK_V,
                FocalSprites.LINK_SIZE,
                FocalSprites.LINK_SIZE,
                tint);
        if (!node.branched() || node.column() == node.parentColumn()) {
            return;
        }
        int splitY = y - SPLIT_OFFSET_Y;
        if (node.column() < node.parentColumn()) {
            strip(
                    graphics,
                    x - SPLIT_LEFT_OFFSET,
                    splitY,
                    FocalSprites.SPLIT_LEFT_U,
                    FocalSprites.SPLIT_V,
                    FocalSprites.SPLIT_END_W,
                    FocalSprites.SPLIT_H,
                    tint);
            for (int column = node.column() + 1; column < node.parentColumn(); column++) {
                strip(
                        graphics,
                        column * FocalLayout.COLUMN_PITCH - MID_OFFSET,
                        splitY,
                        FocalSprites.SPLIT_MID_U,
                        FocalSprites.SPLIT_V,
                        FocalSprites.SPLIT_MID_W,
                        FocalSprites.SPLIT_H,
                        tint);
            }
        } else {
            strip(
                    graphics,
                    x - SPLIT_RIGHT_OFFSET,
                    splitY,
                    FocalSprites.SPLIT_RIGHT_U,
                    FocalSprites.SPLIT_V,
                    FocalSprites.SPLIT_END_W,
                    FocalSprites.SPLIT_H,
                    tint);
            for (int column = node.parentColumn() + 1; column < node.column(); column++) {
                strip(
                        graphics,
                        column * FocalLayout.COLUMN_PITCH - MID_OFFSET,
                        splitY,
                        FocalSprites.SPLIT_MID_U,
                        FocalSprites.SPLIT_V,
                        FocalSprites.SPLIT_MID_W,
                        FocalSprites.SPLIT_H,
                        tint);
            }
        }
    }

    private static void strip(GuiGraphics graphics, int x, int y, int u, int v, int width, int height, int tint) {
        GuiBlend.blitTinted(
                graphics,
                FocalSprites.STRIP,
                x,
                y,
                width,
                height,
                u,
                v,
                width,
                height,
                FocalSprites.STRIP_TEXTURE,
                FocalSprites.STRIP_TEXTURE,
                tint);
    }

    private void controls(GuiGraphics graphics, FocalLayout layout, int mouseX, int mouseY) {
        int y = layout.bodyY0() + FocalLayout.ZOOM_Y;
        int minus = layout.canvasX1() - FocalLayout.ZOOM_MINUS_RIGHT;
        int plus = layout.canvasX1() - FocalLayout.ZOOM_PLUS_RIGHT;
        FocalDraw.button(
                graphics,
                minus,
                y,
                FocalLayout.ZOOM_BUTTON,
                FocalLayout.ZOOM_BUTTON,
                FocalSprites.GLYPH_MINUS,
                FocalSprites.GLYPH_SIZE,
                FocalDraw.over(mouseX, mouseY, minus, y, FocalLayout.ZOOM_BUTTON, FocalLayout.ZOOM_BUTTON),
                false);
        FocalDraw.button(
                graphics,
                plus,
                y,
                FocalLayout.ZOOM_BUTTON,
                FocalLayout.ZOOM_BUTTON,
                FocalSprites.GLYPH_PLUS,
                FocalSprites.GLYPH_SIZE,
                FocalDraw.over(mouseX, mouseY, plus, y, FocalLayout.ZOOM_BUTTON, FocalLayout.ZOOM_BUTTON),
                false);
        FocalDraw.textRight(
                graphics,
                host.font(),
                Component.translatable("gui.thaumaturge.percent", Math.round(scale() * PERCENT)),
                layout.canvasX1() - FocalLayout.ZOOM_TEXT_RIGHT,
                layout.bodyY0() + FocalLayout.ZOOM_TEXT_Y,
                FocalColors.GREY);
    }

    private boolean selected(SpellTreeLayout.Placed node) {
        Selection selection = host.selection();
        if (node.ghost()) {
            return selection.ghost() && selection.path().equals(SpellPaths.parent(node.path()));
        }
        return !selection.ghost() && selection.path().equals(node.path());
    }

    private List<Component> tooltip(SpellTreeLayout.Placed node) {
        List<Component> lines = new ArrayList<>();
        if (node.ghost()) {
            lines.add(SpellText.emptySlot());
            return lines;
        }
        lines.add(SpellText.partName(node.node().part()).withStyle(ChatFormatting.WHITE));
        Optional<SpellPart> part = host.part(node.node().part());
        part.ifPresent(found -> lines.add(SpellText.kind(found.kind()).withStyle(ChatFormatting.GRAY)));
        for (SpellProblem problem : host.summary().problems()) {
            if (problem.node().isPresent() && problem.node().get().equals(node.node())) {
                lines.add(
                        problem.message().copy().withStyle(problem.fatal() ? ChatFormatting.RED : ChatFormatting.GOLD));
            }
        }
        lines.add(SpellText.removeHint());
        return lines;
    }

    private SpellTreeLayout.@Nullable Placed hit(FocalLayout layout, double mouseX, double mouseY) {
        double localX = (mouseX - originX(layout)) / scale();
        double localY = (mouseY - originY(layout)) / scale();
        for (SpellTreeLayout.Placed node : placed) {
            double dx = localX - node.column() * FocalLayout.COLUMN_PITCH;
            double dy = localY - node.row() * FocalLayout.ROW_PITCH;
            if (Math.abs(dx) <= HIT_RADIUS && Math.abs(dy) <= HIT_RADIUS) {
                return node;
            }
        }
        return null;
    }

    public boolean mouseClicked(FocalLayout layout, double mouseX, double mouseY, int button) {
        int y = layout.bodyY0() + FocalLayout.ZOOM_Y;
        if (FocalDraw.over(
                mouseX,
                mouseY,
                layout.canvasX1() - FocalLayout.ZOOM_MINUS_RIGHT,
                y,
                FocalLayout.ZOOM_BUTTON,
                FocalLayout.ZOOM_BUTTON)) {
            zoomBy(-1);
            return true;
        }
        if (FocalDraw.over(
                mouseX,
                mouseY,
                layout.canvasX1() - FocalLayout.ZOOM_PLUS_RIGHT,
                y,
                FocalLayout.ZOOM_BUTTON,
                FocalLayout.ZOOM_BUTTON)) {
            zoomBy(1);
            return true;
        }
        if (!layout.inCanvas(mouseX, mouseY)) {
            return false;
        }
        SpellTreeLayout.Placed node = hit(layout, mouseX, mouseY);
        if (node == null) {
            panning = button == 0;
            return true;
        }
        if (button == 1 && !node.ghost()) {
            host.edit(spell -> spell.withRoot(SpellPaths.remove(spell.root(), node.path(), host::maxChildren)));
            host.select(Selection.node(SpellPaths.parent(node.path())));
            host.playClick();
            return true;
        }
        host.select(node.ghost() ? Selection.below(SpellPaths.parent(node.path())) : Selection.node(node.path()));
        host.playClick();
        return true;
    }

    public boolean mouseDragged(double dragX, double dragY) {
        if (!panning) {
            return false;
        }
        panX += dragX;
        panY += dragY;
        return true;
    }

    public void mouseReleased() {
        panning = false;
    }

    public boolean mouseScrolled(FocalLayout layout, double mouseX, double mouseY, double amount) {
        if (!layout.inCanvas(mouseX, mouseY)) {
            return false;
        }
        zoomBy((int) Math.signum(amount));
        return true;
    }

    private void zoomBy(int steps) {
        int next = Math.clamp(zoom + steps, 0, ZOOMS.length - 1);
        if (next != zoom) {
            panX = panX * ZOOMS[next] / ZOOMS[zoom];
            panY = panY * ZOOMS[next] / ZOOMS[zoom];
            zoom = next;
            host.playClick();
        }
    }
}
