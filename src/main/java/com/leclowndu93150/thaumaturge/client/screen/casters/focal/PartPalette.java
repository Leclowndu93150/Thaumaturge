package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import com.leclowndu93150.thaumaturge.client.screen.casters.SpellPartIcons;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;

public final class PartPalette {
    private static final SpellPartKind[] TABS = SpellPartKind.values();

    private final DraftHost host;
    private SpellPartKind tab = SpellPartKind.DELIVERY;
    private String search = "";
    private int scroll;
    private boolean draggingThumb;
    private List<ResourceKey<SpellPart>> entries = List.of();

    public PartPalette(DraftHost host) {
        this.host = host;
    }

    public void setSearch(String search) {
        this.search = search.toLowerCase(Locale.ROOT);
        refresh();
    }

    public void refresh() {
        List<Holder.Reference<SpellPart>> found = new ArrayList<>();
        host.registries().lookupOrThrow(SpellPart.REGISTRY_KEY).listElements().forEach(holder -> {
            SpellPart part = holder.value();
            if (!part.hidden()
                    && (part.kind() == tab || !search.isEmpty())
                    && unlocked(part)
                    && matches(holder.key())) {
                found.add(holder);
            }
        });
        found.sort(Comparator.<Holder.Reference<SpellPart>>comparingInt(
                        holder -> holder.value().complexity())
                .thenComparing(holder -> SpellText.partName(holder.key()).getString()));
        entries = found.stream().map(Holder.Reference::key).toList();
        scroll = Mth.clamp(scroll, 0, maxScroll(null));
    }

    private boolean unlocked(SpellPart part) {
        Optional<ResearchGate> gate = part.research();
        return gate.isEmpty() || ResearchGate.passes(host.player(), gate.get());
    }

    private boolean matches(ResourceKey<SpellPart> key) {
        return search.isEmpty()
                || SpellText.partName(key).getString().toLowerCase(Locale.ROOT).contains(search);
    }

    private int maxScroll(FocalLayout layout) {
        int rows = (entries.size() + FocalLayout.GRID_COLUMNS - 1) / FocalLayout.GRID_COLUMNS;
        return layout == null ? Math.max(0, rows) : Math.max(0, rows - layout.gridRows());
    }

    public void render(GuiGraphics graphics, FocalLayout layout, int mouseX, int mouseY) {
        int y = layout.bodyY0();
        for (int index = 0; index < TABS.length; index++) {
            int x = layout.leftX0() + index * FocalLayout.TAB_PITCH;
            boolean selected = TABS[index] == tab;
            boolean hovered = FocalDraw.over(mouseX, mouseY, x, y, FocalSprites.TAB_W, FocalSprites.TAB_H);
            FocalDraw.sprite(
                    graphics,
                    selected ? FocalSprites.TAB_SELECTED : hovered ? FocalSprites.TAB_HOVER : FocalSprites.TAB_NORMAL,
                    x,
                    y,
                    FocalSprites.TAB_W,
                    FocalSprites.TAB_H);
            FocalDraw.sprite(
                    graphics,
                    FocalSprites.tabIcon(TABS[index], selected),
                    x + FocalLayout.TAB_ICON_X,
                    y + FocalLayout.TAB_ICON_Y,
                    FocalSprites.TAB_ICON,
                    FocalSprites.TAB_ICON);
            if (hovered) {
                host.tooltip(SpellText.kind(TABS[index]), mouseX, mouseY);
            }
        }
        FocalDraw.field(graphics, layout.leftX0(), y + FocalLayout.SEARCH_Y, FocalLayout.LEFT_W, FocalLayout.SEARCH_H);
        int gridY = layout.gridY0();
        FocalDraw.sprite(graphics, FocalSprites.WELL, layout.leftX0(), gridY, FocalLayout.GRID_W, layout.gridHeight());
        FocalDraw.field(
                graphics, layout.leftX0() + FocalLayout.SCROLL_X, gridY, FocalLayout.SCROLL_W, layout.gridHeight());
        FocalDraw.sprite(
                graphics,
                FocalSprites.SCROLL_THUMB,
                layout.leftX0() + FocalLayout.SCROLL_X,
                thumbY(layout),
                FocalSprites.SCROLL_THUMB_W,
                FocalSprites.SCROLL_THUMB_H);
        ResourceKey<SpellPart> highlighted = highlighted();
        int rows = layout.gridRows();
        for (int cell = 0; cell < rows * FocalLayout.GRID_COLUMNS; cell++) {
            int px = layout.leftX0() + FocalLayout.GRID_PAD + cell % FocalLayout.GRID_COLUMNS * FocalLayout.GRID_PITCH;
            int py = gridY + FocalLayout.GRID_PAD + cell / FocalLayout.GRID_COLUMNS * FocalLayout.GRID_PITCH;
            FocalDraw.sprite(
                    graphics, FocalSprites.CELL, px + 1, py + 1, FocalSprites.CELL_SIZE, FocalSprites.CELL_SIZE);
            int index = cell + scroll * FocalLayout.GRID_COLUMNS;
            if (index >= entries.size()) {
                continue;
            }
            ResourceKey<SpellPart> key = entries.get(index);
            int centre = FocalLayout.GRID_PITCH / 2;
            SpellPartIcons.draw(
                    graphics,
                    host.registries(),
                    key,
                    px + centre,
                    py + centre,
                    FocalLayout.PALETTE_BACK,
                    FocalLayout.PALETTE_GLYPH,
                    FocalLayout.PALETTE_BACK,
                    1.0F);
            if (key.equals(highlighted)) {
                FocalDraw.sprite(
                        graphics,
                        FocalSprites.SELECTION,
                        px,
                        py,
                        FocalSprites.SELECTION_SIZE,
                        FocalSprites.SELECTION_SIZE);
            }
            if (FocalDraw.over(mouseX, mouseY, px, py, FocalLayout.GRID_PITCH, FocalLayout.GRID_PITCH)) {
                host.tooltip(tooltip(key), mouseX, mouseY);
            }
        }
    }

    private ResourceKey<SpellPart> highlighted() {
        Selection selection = host.selection();
        if (selection.ghost()) {
            return null;
        }
        return SpellPaths.at(host.draft().root(), selection.path()).part();
    }

    private int thumbY(FocalLayout layout) {
        int travel = layout.gridHeight() - FocalSprites.SCROLL_THUMB_H;
        int max = maxScroll(layout);
        return layout.gridY0() + (max == 0 ? 0 : travel * scroll / max);
    }

    private List<Component> tooltip(ResourceKey<SpellPart> key) {
        List<Component> lines = new ArrayList<>();
        lines.add(SpellText.partName(key).withStyle(ChatFormatting.WHITE));
        Optional<SpellPart> part = host.part(key);
        part.ifPresent(found -> lines.add(SpellText.kind(found.kind()).withStyle(ChatFormatting.GRAY)));
        lines.add(SpellText.partDescription(key).withStyle(ChatFormatting.DARK_PURPLE));
        part.ifPresent(found -> lines.add(SpellText.complexityOnly(found.complexity())));
        return lines;
    }

    public boolean mouseClicked(FocalLayout layout, double mouseX, double mouseY) {
        int y = layout.bodyY0();
        for (int index = 0; index < TABS.length; index++) {
            if (FocalDraw.over(
                    mouseX,
                    mouseY,
                    layout.leftX0() + index * FocalLayout.TAB_PITCH,
                    y,
                    FocalSprites.TAB_W,
                    FocalSprites.TAB_H)) {
                if (tab != TABS[index]) {
                    tab = TABS[index];
                    scroll = 0;
                    refresh();
                    host.playClick();
                }
                return true;
            }
        }
        if (FocalDraw.over(
                mouseX,
                mouseY,
                layout.leftX0() + FocalLayout.SCROLL_X,
                layout.gridY0(),
                FocalLayout.SCROLL_W,
                layout.gridHeight())) {
            draggingThumb = true;
            dragThumb(layout, mouseY);
            return true;
        }
        int gridY = layout.gridY0();
        if (!FocalDraw.over(mouseX, mouseY, layout.leftX0(), gridY, FocalLayout.GRID_W, layout.gridHeight())) {
            return false;
        }
        int column = (int) (mouseX - layout.leftX0() - FocalLayout.GRID_PAD) / FocalLayout.GRID_PITCH;
        int row = (int) (mouseY - gridY - FocalLayout.GRID_PAD) / FocalLayout.GRID_PITCH;
        int index = (row + scroll) * FocalLayout.GRID_COLUMNS + column;
        if (column >= 0 && column < FocalLayout.GRID_COLUMNS && row >= 0 && index < entries.size()) {
            host.place(entries.get(index));
        }
        return true;
    }

    public boolean mouseDragged(FocalLayout layout, double mouseY) {
        if (!draggingThumb) {
            return false;
        }
        dragThumb(layout, mouseY);
        return true;
    }

    public void mouseReleased() {
        draggingThumb = false;
    }

    private void dragThumb(FocalLayout layout, double mouseY) {
        int max = maxScroll(layout);
        int travel = layout.gridHeight() - FocalSprites.SCROLL_THUMB_H;
        if (max > 0 && travel > 0) {
            scroll = Mth.clamp(
                    (int) Math.round((mouseY - layout.gridY0() - FocalSprites.SCROLL_THUMB_H / 2.0) * max / travel),
                    0,
                    max);
        }
    }

    public boolean mouseScrolled(FocalLayout layout, double mouseX, double mouseY, double amount) {
        if (!FocalDraw.over(
                mouseX, mouseY, layout.leftX0(), layout.gridY0(), FocalLayout.LEFT_W, layout.gridHeight())) {
            return false;
        }
        scroll = Mth.clamp(scroll - (int) Math.signum(amount), 0, maxScroll(layout));
        return true;
    }
}
