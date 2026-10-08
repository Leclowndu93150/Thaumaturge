package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.research.pool.AspectPoolAccess;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.part.SettingSpec;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import com.leclowndu93150.thaumaturge.client.screen.casters.SpellPartIcons;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import com.leclowndu93150.thaumaturge.content.spell.engine.SpellAnalyzer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FastColor.ARGB32;

public final class NodeInspector {
    public static final int LABEL_STEP = 11;
    private static final int CARD_H = 26;
    private static final int CARD_STEP = 31;
    private static final int CARD_ICON = 13;
    private static final int CARD_BACK = 22;
    private static final int CARD_GLYPH = 14;
    private static final int CARD_TEXT_X = 27;
    private static final int CARD_NAME_Y = 4;
    private static final int CARD_KIND_Y = 15;
    private static final int ASPECT_COLUMNS = 6;
    private static final int ASPECT_PITCH_X = 21;
    private static final int ASPECT_PITCH_Y = 20;
    private static final int ASPECT_ICON = 16;
    private static final int ASPECT_TEXTURE = 32;
    private static final int ASPECT_SELECT_OFFSET = 3;
    private static final int ASPECT_TAIL = 2;
    private static final int ROW_STEP = 15;
    private static final int ROW_TEXT_Y = 2;
    private static final int SPIN = 12;
    private static final int SPIN_GAP = 1;
    private static final int VALUE_MIN_W = 24;
    private static final int VALUE_PAD = 6;
    private static final int COST_STEP = 13;
    private static final int COST_TEXT_Y = 1;
    private static final int DIVIDER_STEP = 6;

    private final DraftHost host;
    private final List<Spinner> spinners = new ArrayList<>();
    private final List<AspectCell> aspects = new ArrayList<>();

    public NodeInspector(DraftHost host) {
        this.host = host;
    }

    public int render(GuiGraphics graphics, FocalLayout layout, int mouseX, int mouseY) {
        spinners.clear();
        aspects.clear();
        int x = layout.rightX0();
        int right = layout.rightX1();
        int y = layout.bodyY0();
        FocalDraw.text(graphics, host.font(), SpellText.inspectorLabel("selected"), x, y, FocalColors.LABEL);
        y += LABEL_STEP;
        FocalDraw.field(graphics, x, y, FocalLayout.RIGHT_W, CARD_H);
        Selection selection = host.selection();
        SpellNode node = SpellPaths.at(host.draft().root(), selection.path());
        Optional<SpellPart> part = selection.ghost() ? Optional.empty() : host.part(node.part());
        if (part.isPresent()) {
            SpellPartIcons.draw(
                    graphics,
                    host.registries(),
                    node,
                    x + CARD_ICON,
                    y + CARD_ICON,
                    CARD_BACK,
                    CARD_GLYPH,
                    CARD_BACK,
                    1.0F);
            FocalDraw.text(
                    graphics,
                    host.font(),
                    SpellText.partName(node.part()),
                    x + CARD_TEXT_X,
                    y + CARD_NAME_Y,
                    FocalColors.WHITE);
            Optional<ResourceKey<IAspect>> aspect = part.get().aspect().resolve(node.aspect(), host.registries());
            Component kind = aspect.isPresent() && part.get().aspect().selectable()
                    ? SpellText.kindWithAspect(part.get().kind(), aspect.get())
                    : SpellText.kind(part.get().kind());
            FocalDraw.text(graphics, host.font(), kind, x + CARD_TEXT_X, y + CARD_KIND_Y, FocalColors.GREY);
        } else {
            FocalDraw.text(
                    graphics, host.font(), SpellText.emptySlot(), x + CARD_TEXT_X, y + CARD_NAME_Y, FocalColors.GREY);
        }
        y += CARD_STEP;
        if (part.isPresent() && part.get().aspect().selectable()) {
            y = aspectGrid(graphics, x, y, node, part.get(), mouseX, mouseY);
        }
        if (part.isPresent()) {
            for (SettingSpec spec : part.get().settings()) {
                spinner(graphics, x, right, y, node, spec, mouseX, mouseY);
                y += ROW_STEP;
            }
            FocalDraw.text(
                    graphics, host.font(), SpellText.inspectorLabel("node_cost"), x, y + COST_TEXT_Y, FocalColors.GREY);
            int cost = Math.round(SpellAnalyzer.nodeComplexity(node, part.get(), host.registries()));
            FocalDraw.textRight(
                    graphics,
                    host.font(),
                    Component.literal(Integer.toString(cost)),
                    right,
                    y + COST_TEXT_Y,
                    FocalColors.WHITE);
            y += COST_STEP;
        }
        FocalDraw.sprite(graphics, FocalSprites.DIVIDER, x, y, FocalLayout.RIGHT_W, FocalSprites.DIVIDER_H);
        return y + DIVIDER_STEP;
    }

    private int aspectGrid(GuiGraphics graphics, int x, int y, SpellNode node, SpellPart part, int mouseX, int mouseY) {
        FocalDraw.text(graphics, host.font(), SpellText.inspectorLabel("aspect"), x, y, FocalColors.LABEL);
        y += LABEL_STEP;
        List<Holder.Reference<IAspect>> known = new ArrayList<>();
        for (ResourceKey<IAspect> key : part.aspect().options(host.registries())) {
            host.registries()
                    .lookupOrThrow(IAspect.REGISTRY_KEY)
                    .get(key)
                    .filter(holder -> AspectPoolAccess.isDiscovered(host.player(), holder))
                    .ifPresent(known::add);
        }
        Optional<ResourceKey<IAspect>> chosen = part.aspect().resolve(node.aspect(), host.registries());
        for (int index = 0; index < known.size(); index++) {
            Holder.Reference<IAspect> aspect = known.get(index);
            int ax = x + index % ASPECT_COLUMNS * ASPECT_PITCH_X;
            int ay = y + index / ASPECT_COLUMNS * ASPECT_PITCH_Y;
            FocalDraw.sprite(graphics, FocalSprites.SLOT, ax, ay, FocalSprites.SLOT_SIZE, FocalSprites.SLOT_SIZE);
            GuiBlend.blitTinted(
                    graphics,
                    aspect.value().texture(),
                    ax + 1,
                    ay + 1,
                    ASPECT_ICON,
                    ASPECT_ICON,
                    0.0F,
                    0.0F,
                    ASPECT_TEXTURE,
                    ASPECT_TEXTURE,
                    ASPECT_TEXTURE,
                    ASPECT_TEXTURE,
                    ARGB32.color(255, aspect.value().color()));
            if (chosen.isPresent() && chosen.get().equals(aspect.key())) {
                FocalDraw.sprite(
                        graphics,
                        FocalSprites.SELECTION,
                        ax - ASPECT_SELECT_OFFSET,
                        ay - ASPECT_SELECT_OFFSET,
                        FocalSprites.SELECTION_SIZE,
                        FocalSprites.SELECTION_SIZE);
            }
            aspects.add(new AspectCell(aspect.key(), ax, ay));
            if (FocalDraw.over(mouseX, mouseY, ax, ay, FocalSprites.SLOT_SIZE, FocalSprites.SLOT_SIZE)) {
                host.tooltip(SpellText.aspectName(aspect.key()), mouseX, mouseY);
            }
        }
        int rows = Math.max(1, (known.size() + ASPECT_COLUMNS - 1) / ASPECT_COLUMNS);
        return y + rows * ASPECT_PITCH_Y + ASPECT_TAIL;
    }

    private void spinner(
            GuiGraphics graphics, int x, int right, int y, SpellNode node, SettingSpec spec, int mouseX, int mouseY) {
        FocalDraw.text(graphics, host.font(), SpellText.setting(spec), x, y + ROW_TEXT_Y, FocalColors.GREY);
        int value = spec.clamp(node.settings().getOrDefault(spec.key(), spec.defaultValue()));
        Component label = spec.label(value);
        int valueW = Math.max(VALUE_MIN_W, host.font().width(label) + VALUE_PAD);
        int plusX = right - SPIN;
        int fieldX = plusX - SPIN_GAP - valueW;
        int minusX = fieldX - SPIN_GAP - SPIN;
        FocalDraw.button(
                graphics,
                minusX,
                y,
                SPIN,
                SPIN,
                FocalSprites.GLYPH_LEFT,
                FocalSprites.GLYPH_SIZE,
                FocalDraw.over(mouseX, mouseY, minusX, y, SPIN, SPIN),
                false);
        FocalDraw.field(graphics, fieldX, y, valueW, SPIN);
        FocalDraw.text(
                graphics,
                host.font(),
                label,
                fieldX + (valueW - host.font().width(label)) / 2,
                y + ROW_TEXT_Y,
                FocalColors.WHITE);
        FocalDraw.button(
                graphics,
                plusX,
                y,
                SPIN,
                SPIN,
                FocalSprites.GLYPH_RIGHT,
                FocalSprites.GLYPH_SIZE,
                FocalDraw.over(mouseX, mouseY, plusX, y, SPIN, SPIN),
                false);
        spinners.add(new Spinner(spec, minusX, y, -1));
        spinners.add(new Spinner(spec, plusX, y, 1));
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        Selection selection = host.selection();
        if (selection.ghost()) {
            return false;
        }
        for (AspectCell cell : aspects) {
            if (FocalDraw.over(mouseX, mouseY, cell.x(), cell.y(), FocalSprites.SLOT_SIZE, FocalSprites.SLOT_SIZE)) {
                host.edit(spell -> spell.withRoot(SpellPaths.update(
                        spell.root(), selection.path(), node -> node.withAspect(Optional.of(cell.aspect())))));
                host.playClick();
                return true;
            }
        }
        for (Spinner spinner : spinners) {
            if (FocalDraw.over(mouseX, mouseY, spinner.x(), spinner.y(), SPIN, SPIN)) {
                host.edit(spell -> spell.withRoot(SpellPaths.update(spell.root(), selection.path(), node -> {
                    int current = node.settings()
                            .getOrDefault(spinner.spec().key(), spinner.spec().defaultValue());
                    return node.withSetting(spinner.spec().key(), spinner.spec().offset(current, spinner.delta()));
                })));
                host.playClick();
                return true;
            }
        }
        return false;
    }

    private record Spinner(SettingSpec spec, int x, int y, int delta) {}

    private record AspectCell(ResourceKey<IAspect> aspect, int x, int y) {}
}
