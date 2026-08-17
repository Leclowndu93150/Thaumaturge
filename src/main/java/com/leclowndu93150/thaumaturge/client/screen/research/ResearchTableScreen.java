package com.leclowndu93150.thaumaturge.client.screen.research;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.client.effect.pipeline.TCRenderPipelines;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTCContainerScreen;
import com.leclowndu93150.thaumaturge.content.research.note.HexGrid;
import com.leclowndu93150.thaumaturge.content.research.note.NoteRules;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPoolData;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.MenuResearchTable;
import com.leclowndu93150.thaumaturge.network.ServerboundTableCombinePayload;
import com.leclowndu93150.thaumaturge.network.ServerboundTableDuplicatePayload;
import com.leclowndu93150.thaumaturge.network.ServerboundTablePlaceAspectPayload;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class ResearchTableScreen extends AbstractTCContainerScreen<MenuResearchTable> {
    private static final Identifier TEXTURE = TCIds.rl("textures/gui/guiresearchtable2.png");
    private static final Identifier PARCHMENT = TCIds.rl("textures/misc/parchment3.png");
    private static final Identifier HEX_IDLE = TCIds.rl("textures/gui/hex1.png");
    private static final Identifier HEX_HOVER = TCIds.rl("textures/gui/hex2.png");
    private static final Identifier LINE_TEXTURE = TCIds.rl("textures/misc/white.png");
    private static final int LINE_HALF_WIDTH = 1;
    private static final float LINE_ALPHA = 0.6F;
    private static final Identifier UNKNOWN_ASPECT = TCIds.rl("textures/aspects/_unknown.png");

    private static final int GUI_SIZE = 255;
    private static final int MAIN_PANE_H = 167;
    private static final int LOWER_PANEL_X = 40;
    private static final int LOWER_PANEL_V = 166;
    private static final int LOWER_PANEL_W = 184;
    private static final int LOWER_PANEL_H = 88;

    private static final int PALETTE_X = 10;
    private static final int PALETTE_Y = 40;
    private static final int PALETTE_CELL = 16;
    private static final int PALETTE_ROWS = 5;
    private static final int PALETTE_SLOTS = 25;
    private static final int PALETTE_W = 80;
    private static final int PALETTE_H = 80;
    private static final int PAGE_STEP = 5;

    private static final int ARROW_PREV_X = 27;
    private static final int ARROW_NEXT_X = 51;
    private static final int ARROW_Y = 119;
    private static final int ARROW_W = 24;
    private static final int ARROW_H = 8;
    private static final int ARROW_PREV_U = 184;
    private static final int ARROW_NEXT_U = 208;
    private static final int ARROW_V = 208;

    private static final int SELECT1_CENTER_X = 21;
    private static final int SELECT2_CENTER_X = 79;
    private static final int SELECT_CENTER_Y = 147;
    private static final float SELECT_SCALE = 1.5F;
    private static final int SELECT1_HIT_X = 11;
    private static final int SELECT2_HIT_X = 71;
    private static final int SELECT_HIT_Y = 137;
    private static final int SELECT_SIZE = 16;

    private static final int COMBINE_X = 35;
    private static final int COMBINE_Y = 139;
    private static final int COMBINE_W = 32;
    private static final int COMBINE_H = 16;
    private static final int COMBINE_U = 184;
    private static final int COMBINE_V = 184;
    private static final int COMBINE_PRESSED_V = 168;
    private static final long COMBINE_COOLDOWN_MS = 200L;

    private static final int DUPE_X = 37;
    private static final int DUPE_Y = 5;
    private static final int DUPE_SIZE = 24;
    private static final int DUPE_U = 232;
    private static final int DUPE_V = 200;

    private static final int SHEET_X = 94;
    private static final int SHEET_Y = 8;
    private static final int SHEET_SIZE = 150;
    private static final int HEX_ORIGIN_X = 169;
    private static final int HEX_ORIGIN_Y = 83;
    private static final float HEX_SIZE = 9.0F;
    private static final int HEX_TILE_HALF = 8;
    private static final int ORB_OFFSET = -8;

    private static final int INK_WARN_X = 157;
    private static final int INK_WARN_Y = 84;

    private static final int HELPER_X = 41;
    private static final int HELPER_Y = 9;
    private static final int HELPER_SIZE = 16;
    private static final int HELPER_ROWS = 7;
    private static final int HELPER_ROW_START_Y = 10;
    private static final int HELPER_ROW_STRIDE = 17;
    private static final int HELPER_ARROW_Y = 144;
    private static final int HELPER_PAGE_HALF_GAP = 18;
    private static final int HELPER_ROW_WIDTH = 72;

    private boolean helperOpen;
    private int helperPage;

    private @Nullable Holder<IAspect> draggedAspect;
    private @Nullable Holder<IAspect> select1;
    private @Nullable Holder<IAspect> select2;
    private int page;
    private long combineCooldownUntil;

    public ResearchTableScreen(MenuResearchTable menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, GUI_SIZE, GUI_SIZE);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    @Override
    protected void extractBackgroundTexture(GuiGraphicsExtractor graphics) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F, GUI_SIZE, MAIN_PANE_H, 256, 256);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                leftPos + LOWER_PANEL_X,
                topPos + MAIN_PANE_H,
                0.0F,
                (float) LOWER_PANEL_V,
                LOWER_PANEL_W,
                LOWER_PANEL_H,
                256,
                256);
    }

    private @Nullable BlockEntityResearchTable table() {
        return menu.blockEntity();
    }

    private @Nullable ResearchNoteData noteData() {
        return ResearchNotes.dataOf(
                menu.slots.get(BlockEntityResearchTable.SLOT_NOTE).getItem());
    }

    private boolean hasInkReady() {
        var tools = menu.slots.get(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS).getItem();
        return !tools.isEmpty() && tools.isDamageableItem() && tools.getDamageValue() < tools.getMaxDamage();
    }

    private AspectPoolData pool() {
        return AspectPools.data(minecraft.player);
    }

    private List<Holder<IAspect>> discoveredAspects() {
        List<Holder<IAspect>> result = new ArrayList<>();
        if (minecraft == null || minecraft.level == null) {
            return result;
        }
        HolderLookup.RegistryLookup<IAspect> lookup =
                minecraft.level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        List<Identifier> ids = new ArrayList<>(pool().pool().keySet());
        ids.sort(Identifier::compareTo);
        for (Identifier id : ids) {
            lookup.get(ResourceKey.create(IAspect.REGISTRY_KEY, id)).ifPresent(result::add);
        }
        return result;
    }

    private int availableOf(Holder<IAspect> aspect) {
        int amount = pool().amount(AspectPools.idOf(aspect));
        BlockEntityResearchTable table = table();
        if (table != null) {
            amount += table.bonusAspects().amountOf(aspect);
        }
        return amount;
    }

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        drawPalette(graphics, mouseX, mouseY);
        drawCombineTray(graphics, mouseX, mouseY);
        drawSheet(graphics, mouseX, mouseY);
        drawDuplicateButton(graphics, mouseX, mouseY);
        drawHelperButton(graphics, mouseX, mouseY);
        drawSlotHints(graphics, mouseX, mouseY);
        drawDragged(graphics, mouseX, mouseY);
    }

    private void drawPalette(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<Holder<IAspect>> aspects = discoveredAspects();
        int start = page * PAGE_STEP;
        int drawn = 0;
        Holder<IAspect> hovered = null;
        for (int i = start; i < aspects.size() && drawn < PALETTE_SLOTS; i++, drawn++) {
            Holder<IAspect> aspect = aspects.get(i);
            int x = leftPos + PALETTE_X + (drawn / PALETTE_ROWS) * PALETTE_CELL;
            int y = topPos + PALETTE_Y + (drawn % PALETTE_ROWS) * PALETTE_CELL;
            float alpha = availableOf(aspect) > 0 ? 1.0F : 0.33F;
            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            graphics.pose().scale(0.8F);
            AspectTagRenderer.render(
                    graphics,
                    font,
                    1,
                    1,
                    aspect,
                    pool().amount(AspectPools.idOf(aspect)),
                    0,
                    0.0,
                    AspectTagRenderer.BlendMode.ALPHA,
                    alpha,
                    false);
            graphics.pose().popMatrix();
            if (mouseX >= x && mouseX < x + PALETTE_CELL && mouseY >= y && mouseY < y + PALETTE_CELL) {
                hovered = aspect;
            }
        }
        int lastPage = lastPage(aspects.size());
        if (page > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    leftPos + ARROW_PREV_X,
                    topPos + ARROW_Y,
                    (float) ARROW_PREV_U,
                    (float) ARROW_V,
                    ARROW_W,
                    ARROW_H,
                    256,
                    256);
            if (inRect(mouseX, mouseY, leftPos + ARROW_PREV_X, topPos + ARROW_Y, ARROW_W, ARROW_H)) {
                graphics.setTooltipForNextFrame(font, Component.translatable("tc.table.page.prev"), mouseX, mouseY);
            }
        }
        if (page < lastPage) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    leftPos + ARROW_NEXT_X,
                    topPos + ARROW_Y,
                    (float) ARROW_NEXT_U,
                    (float) ARROW_V,
                    ARROW_W,
                    ARROW_H,
                    256,
                    256);
            if (inRect(mouseX, mouseY, leftPos + ARROW_NEXT_X, topPos + ARROW_Y, ARROW_W, ARROW_H)) {
                graphics.setTooltipForNextFrame(font, Component.translatable("tc.table.page.next"), mouseX, mouseY);
            }
        }
        if (hovered != null && draggedAspect == null) {
            graphics.setTooltipForNextFrame(font, aspectTooltip(hovered), Optional.empty(), mouseX, mouseY);
        }
    }

    private static int lastPage(int count) {
        return Math.max(0, (count - (PALETTE_SLOTS - PAGE_STEP)) / PAGE_STEP);
    }

    private List<Component> aspectTooltip(Holder<IAspect> aspect) {
        List<Component> lines = new ArrayList<>();
        lines.add(AspectComponents.name(aspect));
        return lines;
    }

    private boolean isDepleted(@Nullable BlockEntityResearchTable table, @Nullable Holder<IAspect> aspect) {
        return aspect != null
                && pool().amount(AspectPools.idOf(aspect)) <= 0
                && (table == null || table.bonusAspects().amountOf(aspect) <= 0);
    }

    private void clearDepletedSelections() {
        BlockEntityResearchTable table = table();
        if (isDepleted(table, select1)) {
            select1 = null;
        }
        if (isDepleted(table, select2)) {
            select2 = null;
        }
    }

    private void drawCombineTray(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        clearDepletedSelections();
        drawSelectTag(graphics, select1, leftPos + SELECT1_CENTER_X, topPos + SELECT_CENTER_Y);
        drawSelectTag(graphics, select2, leftPos + SELECT2_CENTER_X, topPos + SELECT_CENTER_Y);
        if (select1 != null && select2 != null) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    leftPos + COMBINE_X,
                    topPos + COMBINE_Y,
                    (float) COMBINE_U,
                    (float) COMBINE_V,
                    COMBINE_W,
                    COMBINE_H,
                    256,
                    256);
            if (System.currentTimeMillis() < combineCooldownUntil) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        TEXTURE,
                        leftPos + COMBINE_X,
                        topPos + COMBINE_Y,
                        (float) COMBINE_U,
                        (float) COMBINE_PRESSED_V,
                        COMBINE_W,
                        COMBINE_H,
                        256,
                        256);
            }
            if (inRect(mouseX, mouseY, leftPos + COMBINE_X, topPos + COMBINE_Y, COMBINE_W, COMBINE_H)) {
                graphics.setTooltipForNextFrame(font, Component.translatable("tc.table.combine"), mouseX, mouseY);
            }
        }
        if (inRect(mouseX, mouseY, leftPos + SELECT1_HIT_X, topPos + SELECT_HIT_Y, SELECT_SIZE, SELECT_SIZE)) {
            graphics.setTooltipForNextFrame(
                    font,
                    select1 != null ? AspectComponents.name(select1) : Component.translatable("tc.table.select"),
                    mouseX,
                    mouseY);
        } else if (inRect(mouseX, mouseY, leftPos + SELECT2_HIT_X, topPos + SELECT_HIT_Y, SELECT_SIZE, SELECT_SIZE)) {
            graphics.setTooltipForNextFrame(
                    font,
                    select2 != null ? AspectComponents.name(select2) : Component.translatable("tc.table.select"),
                    mouseX,
                    mouseY);
        }
    }

    private void drawSelectTag(
            GuiGraphicsExtractor graphics, @Nullable Holder<IAspect> aspect, int centerX, int centerY) {
        if (aspect == null) {
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().scale(SELECT_SCALE, SELECT_SCALE);
        AspectTagRenderer.render(
                graphics, font, -8.0, -8.0, aspect, 0, 0, 0.0, AspectTagRenderer.BlendMode.ALPHA, 1.0F, false);
        graphics.pose().popMatrix();
    }

    private void drawDuplicateButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        BlockEntityResearchTable table = table();
        if (table == null || minecraft.player == null) {
            return;
        }
        ResearchNoteData data = noteData();
        if (data == null
                || !data.complete()
                || !KnowledgeAccess.of(minecraft.player)
                        .isResearchComplete(BlockEntityResearchTable.RESEARCH_DUPLICATION)) {
            return;
        }
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                leftPos + DUPE_X,
                topPos + DUPE_Y,
                (float) DUPE_U,
                (float) DUPE_V,
                DUPE_SIZE,
                DUPE_SIZE,
                256,
                256);
        if (inRect(mouseX, mouseY, leftPos + DUPE_X, topPos + DUPE_Y, DUPE_SIZE, DUPE_SIZE)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("tc.research.copy"));
            AspectList cost = table.duplicationCost(minecraft.player, data);
            if (cost != null) {
                for (AspectInstance instance : cost.entries()) {
                    lines.add(AspectComponents.name(instance.aspect())
                            .copy()
                            .append(Component.literal(" x" + instance.amount())));
                }
            }
            graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
        }
    }

    private void drawHelperButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        BlockEntityResearchTable table = table();
        if (table != null && minecraft.player != null) {
            ResearchNoteData data = noteData();
            if (data != null
                    && data.complete()
                    && KnowledgeAccess.of(minecraft.player)
                            .isResearchComplete(BlockEntityResearchTable.RESEARCH_DUPLICATION)) {
                return;
            }
        }
        graphics.item(new ItemStack(TCItems.THAUMONOMICON.get()), leftPos + HELPER_X, topPos + HELPER_Y);
        if (inRect(mouseX, mouseY, leftPos + HELPER_X, topPos + HELPER_Y, HELPER_SIZE, HELPER_SIZE)) {
            graphics.setTooltipForNextFrame(font, Component.translatable("tc.table.helper"), mouseX, mouseY);
        }
    }

    private List<Holder<IAspect>> discoveredCompounds() {
        List<Holder<IAspect>> result = new ArrayList<>();
        for (Holder<IAspect> aspect : discoveredAspects()) {
            if (aspect.value().components().size() == 2) {
                result.add(aspect);
            }
        }
        return result;
    }

    private void drawHelper(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        List<Holder<IAspect>> compounds = discoveredCompounds();
        int start = helperPage * HELPER_ROWS;
        for (int i = start, row = 0; i < compounds.size() && row < HELPER_ROWS; i++, row++) {
            Holder<IAspect> compound = compounds.get(i);
            List<Holder<IAspect>> parts = compound.value().components();
            int y = topPos + SHEET_Y + HELPER_ROW_START_Y + row * HELPER_ROW_STRIDE;
            int x = leftPos + SHEET_X + (SHEET_SIZE - HELPER_ROW_WIDTH) / 2;
            drawHelperTag(graphics, parts.get(0), x, y, mouseX, mouseY);
            graphics.text(font, "+", x + 20, y + 4, 0xFF3A2A1A, false);
            drawHelperTag(graphics, parts.get(1), x + 28, y, mouseX, mouseY);
            graphics.text(font, "=", x + 48, y + 4, 0xFF3A2A1A, false);
            drawHelperTag(graphics, compound, x + 56, y, mouseX, mouseY);
        }
        int lastPage = Math.max(0, (compounds.size() - 1) / HELPER_ROWS);
        int center = leftPos + SHEET_X + SHEET_SIZE / 2;
        if (lastPage > 0) {
            String label = (helperPage + 1) + "/" + (lastPage + 1);
            graphics.text(font, label, center - font.width(label) / 2, topPos + HELPER_ARROW_Y, 0xFF3A2A1A, false);
        }
        if (helperPage > 0) {
            int x = center - HELPER_PAGE_HALF_GAP - ARROW_W;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    x,
                    topPos + HELPER_ARROW_Y,
                    (float) ARROW_PREV_U,
                    (float) ARROW_V,
                    ARROW_W,
                    ARROW_H,
                    256,
                    256);
            if (inRect(mouseX, mouseY, x, topPos + HELPER_ARROW_Y, ARROW_W, ARROW_H)) {
                graphics.setTooltipForNextFrame(font, Component.translatable("tc.table.page.prev"), mouseX, mouseY);
            }
        }
        if (helperPage < lastPage) {
            int x = center + HELPER_PAGE_HALF_GAP;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    x,
                    topPos + HELPER_ARROW_Y,
                    (float) ARROW_NEXT_U,
                    (float) ARROW_V,
                    ARROW_W,
                    ARROW_H,
                    256,
                    256);
            if (inRect(mouseX, mouseY, x, topPos + HELPER_ARROW_Y, ARROW_W, ARROW_H)) {
                graphics.setTooltipForNextFrame(font, Component.translatable("tc.table.page.next"), mouseX, mouseY);
            }
        }
    }

    private void drawHelperTag(
            GuiGraphicsExtractor graphics, Holder<IAspect> aspect, int x, int y, int mouseX, int mouseY) {
        if (AspectPools.isDiscovered(minecraft.player, aspect)) {
            AspectTagRenderer.render(
                    graphics,
                    font,
                    (double) x,
                    (double) y,
                    aspect,
                    0,
                    0,
                    0.0,
                    AspectTagRenderer.BlendMode.ALPHA,
                    1.0F,
                    false);
            if (inRect(mouseX, mouseY, x, y, 16, 16)) {
                graphics.setTooltipForNextFrame(font, AspectComponents.name(aspect), mouseX, mouseY);
            }
        } else {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    UNKNOWN_ASPECT,
                    x,
                    y,
                    0.0F,
                    0.0F,
                    16,
                    16,
                    32,
                    32,
                    32,
                    32,
                    ARGB.color(128, 0x000000));
        }
    }

    private void drawSheet(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                PARCHMENT,
                leftPos + SHEET_X,
                topPos + SHEET_Y,
                0.0F,
                0.0F,
                SHEET_SIZE,
                SHEET_SIZE,
                SHEET_SIZE,
                SHEET_SIZE,
                256,
                256);
        if (helperOpen) {
            drawHelper(graphics, mouseX, mouseY);
            return;
        }
        ResearchNoteData data = noteData();
        if (data == null) {
            if (!hasInkReady()) {
                drawInkWarning(graphics);
            }
            return;
        }
        HexGrid.Hex hoveredHex = hexAt(mouseX, mouseY);
        Map<HexGrid.Hex, ResearchNoteData.Cell> cells = new HashMap<>(data.cellMap());
        drawConnections(graphics, data, cells);
        for (ResearchNoteData.Cell cell : data.cells()) {
            int cx = leftPos + HEX_ORIGIN_X + Math.round(cell.hex().pixelX(HEX_SIZE));
            int cy = topPos + HEX_ORIGIN_Y + Math.round(cell.hex().pixelY(HEX_SIZE));
            if (!data.complete() && cell.type() != ResearchNoteData.TYPE_ROOT) {
                boolean hover = cell.hex().equals(hoveredHex);
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        hover ? HEX_HOVER : HEX_IDLE,
                        cx - HEX_TILE_HALF,
                        cy - HEX_TILE_HALF,
                        0.0F,
                        0.0F,
                        16,
                        16,
                        32,
                        32,
                        32,
                        32,
                        ARGB.color(hover ? 255 : 64, 0xFFFFFF));
            }
            Holder<IAspect> aspect = cell.aspectOrNull();
            if (aspect != null) {
                if (!AspectPools.isDiscovered(minecraft.player, aspect)) {
                    graphics.blit(
                            RenderPipelines.GUI_TEXTURED,
                            UNKNOWN_ASPECT,
                            cx + ORB_OFFSET,
                            cy + ORB_OFFSET,
                            0.0F,
                            0.0F,
                            16,
                            16,
                            32,
                            32,
                            32,
                            32,
                            ARGB.color(128, 0x000000));
                    if (cell.hex().equals(hoveredHex)) {
                        graphics.setTooltipForNextFrame(
                                font, Component.translatable("tc.aspect.unknown"), mouseX, mouseY);
                    }
                } else {
                    float alpha = 1.0F;
                    AspectTagRenderer.render(
                            graphics,
                            font,
                            (double) (cx + ORB_OFFSET),
                            (double) (cy + ORB_OFFSET),
                            aspect,
                            0,
                            0,
                            0.0,
                            AspectTagRenderer.BlendMode.ALPHA,
                            alpha,
                            false);
                    if (cell.hex().equals(hoveredHex) && draggedAspect == null) {
                        graphics.setTooltipForNextFrame(font, aspectTooltip(aspect), Optional.empty(), mouseX, mouseY);
                    }
                }
            }
        }
        if (!hasInkReady()) {
            drawInkWarning(graphics);
        }
    }

    private void drawConnections(
            GuiGraphicsExtractor graphics, ResearchNoteData data, Map<HexGrid.Hex, ResearchNoteData.Cell> cells) {
        for (ResearchNoteData.Cell cell : data.cells()) {
            if (!cell.active()) {
                continue;
            }
            for (int dir = 0; dir < 3; dir++) {
                HexGrid.Hex neighbour = cell.hex().neighbour(dir);
                ResearchNoteData.Cell other = cells.get(neighbour);
                if (other == null || !other.active()) {
                    continue;
                }
                if (NoteRules.connects(
                        cell.aspectOrNull(),
                        other.aspectOrNull(),
                        a -> AspectPools.isDiscovered(minecraft.player, a))) {
                    int x1 = leftPos + HEX_ORIGIN_X + Math.round(cell.hex().pixelX(HEX_SIZE));
                    int y1 = topPos + HEX_ORIGIN_Y + Math.round(cell.hex().pixelY(HEX_SIZE));
                    int x2 = leftPos + HEX_ORIGIN_X + Math.round(neighbour.pixelX(HEX_SIZE));
                    int y2 = topPos + HEX_ORIGIN_Y + Math.round(neighbour.pixelY(HEX_SIZE));
                    drawLine(graphics, x1, y1, x2, y2);
                }
            }
        }
    }

    private void drawLine(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2) {
        int color = ARGB.color((int) (LINE_ALPHA * 255.0F), 0, 153, 204);
        float dx = x2 - x1;
        float dy = y2 - y1;
        int length = Math.round(Mth.sqrt(dx * dx + dy * dy));
        graphics.pose().pushMatrix();
        graphics.pose().translate(x1, y1);
        graphics.pose().rotate((float) Math.atan2(dy, dx));
        graphics.blit(
                TCRenderPipelines.GUI_TEXTURED_ADDITIVE,
                LINE_TEXTURE,
                0,
                -LINE_HALF_WIDTH,
                0.0F,
                0.0F,
                length,
                LINE_HALF_WIDTH * 2,
                4,
                4,
                4,
                4,
                color);
        graphics.pose().popMatrix();
    }

    private void drawInkWarning(GuiGraphicsExtractor graphics) {
        Component line0 = Component.translatable("tile.researchtable.noink.0");
        Component line1 = Component.translatable("tile.researchtable.noink.1");
        int x = leftPos + SHEET_X + SHEET_SIZE / 2;
        int y = topPos + INK_WARN_Y;
        graphics.text(font, line0, x - font.width(line0) / 2, y - font.lineHeight, 0xFFFF5555, true);
        graphics.text(font, line1, x - font.width(line1) / 2, y, 0xFFFF5555, true);
    }

    private void drawSlotHints(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && draggedAspect == null) {
            if (menu.slots.get(0).getItem().isEmpty()
                    && inRect(
                            mouseX,
                            mouseY,
                            leftPos + MenuResearchTable.SCRIBE_TOOLS_X,
                            topPos + MenuResearchTable.SCRIBE_TOOLS_Y,
                            16,
                            16)) {
                graphics.setTooltipForNextFrame(font, Component.translatable("tc.table.slot.tools"), mouseX, mouseY);
            } else if (menu.slots.get(1).getItem().isEmpty()
                    && inRect(
                            mouseX,
                            mouseY,
                            leftPos + MenuResearchTable.NOTE_X,
                            topPos + MenuResearchTable.NOTE_Y,
                            16,
                            16)) {
                graphics.setTooltipForNextFrame(font, Component.translatable("tc.table.slot.note"), mouseX, mouseY);
            }
        }
    }

    private void drawDragged(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (draggedAspect != null) {
            AspectTagRenderer.render(
                    graphics,
                    font,
                    (double) (mouseX - 8),
                    (double) (mouseY - 8),
                    draggedAspect,
                    0,
                    0,
                    0.0,
                    AspectTagRenderer.BlendMode.ALPHA,
                    1.0F,
                    false);
        }
    }

    private HexGrid.@Nullable Hex hexAt(double mouseX, double mouseY) {
        if (mouseX < leftPos + SHEET_X
                || mouseX >= leftPos + SHEET_X + SHEET_SIZE
                || mouseY < topPos + SHEET_Y
                || mouseY >= topPos + SHEET_Y + SHEET_SIZE) {
            return null;
        }
        float relX = (float) (mouseX - leftPos - HEX_ORIGIN_X);
        float relY = (float) (mouseY - topPos - HEX_ORIGIN_Y);
        return HexGrid.pixelToHex(relX, relY, HEX_SIZE);
    }

    private @Nullable Holder<IAspect> paletteAspectAt(double mouseX, double mouseY) {
        if (mouseX < leftPos + PALETTE_X
                || mouseX >= leftPos + PALETTE_X + PALETTE_W
                || mouseY < topPos + PALETTE_Y
                || mouseY >= topPos + PALETTE_Y + PALETTE_H) {
            return null;
        }
        int col = (int) ((mouseX - leftPos - PALETTE_X) / PALETTE_CELL);
        int row = (int) ((mouseY - topPos - PALETTE_Y) / PALETTE_CELL);
        int index = page * PAGE_STEP + col * PALETTE_ROWS + row;
        List<Holder<IAspect>> aspects = discoveredAspects();
        return index >= 0 && index < aspects.size() ? aspects.get(index) : null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        if (event.button() == 0) {
            BlockEntityResearchTable table = table();
            if (table != null && minecraft.player != null) {
                ResearchNoteData data = noteData();
                if (data == null
                        || !data.complete()
                        || !KnowledgeAccess.of(minecraft.player)
                                .isResearchComplete(BlockEntityResearchTable.RESEARCH_DUPLICATION)) {
                    if (inRect(mx, my, leftPos + HELPER_X, topPos + HELPER_Y, HELPER_SIZE, HELPER_SIZE)) {
                        helperOpen = !helperOpen;
                        playSound(TCSounds.KEY.get(), 0.3F, 1.0F);
                        return true;
                    }
                    if (helperOpen && handleHelperArrows(mx, my)) {
                        return true;
                    }
                }
            }
            if (handleArrows(mx, my)
                    || handleCombineButton(mx, my)
                    || handleSelectRemove(mx, my)
                    || handleDuplicate(mx, my)) {
                return true;
            }
            Holder<IAspect> palette = paletteAspectAt(mx, my);
            if (palette != null) {
                if (Minecraft.getInstance().hasShiftDown()
                        && !palette.value().isPrimal()
                        && minecraft.player != null
                        && KnowledgeAccess.of(minecraft.player)
                                .isResearchComplete(BlockEntityResearchTable.RESEARCH_MASTERY)) {
                    List<Holder<IAspect>> components = palette.value().components();
                    if (components.size() == 2) {
                        select1 = components.get(0);
                        select2 = components.get(1);
                        playSound(TCSounds.HHON.get(), 0.2F, 1.0F);
                        return true;
                    }
                }
                if (availableOf(palette) > 0) {
                    draggedAspect = palette;
                    playSound(TCSounds.HHOFF.get(), 0.2F, 1.0F);
                }
                return true;
            }
            HexGrid.Hex hex = hexAt(mx, my);
            if (hex != null && draggedAspect == null) {
                ResearchNoteData data = noteData();
                if (data != null && !data.complete()) {
                    ResearchNoteData.Cell cell = data.cellAt(hex);
                    if (cell != null && cell.type() == ResearchNoteData.TYPE_PLACED) {
                        ClientPacketDistributor.sendToServer(
                                new ServerboundTablePlaceAspectPayload(menu.pos(), hex.q(), hex.r(), Optional.empty()));
                        playSound(TCSounds.ERASE.get(), 0.2F, 1.0F);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && draggedAspect != null) {
            double mx = event.x();
            double my = event.y();
            HexGrid.Hex hex = hexAt(mx, my);
            ResearchNoteData data = noteData();
            if (hex != null && data != null && !data.complete()) {
                ResearchNoteData.Cell cell = data.cellAt(hex);
                if (cell != null && cell.type() == ResearchNoteData.TYPE_BLANK) {
                    ClientPacketDistributor.sendToServer(new ServerboundTablePlaceAspectPayload(
                            menu.pos(), hex.q(), hex.r(), Optional.of(AspectPools.idOf(draggedAspect))));
                    playSound(TCSounds.WRITE.get(), 0.2F, 1.0F);
                }
            } else if (inRect(
                    mx, my, leftPos + SELECT1_HIT_X - 8, topPos + SELECT_HIT_Y - 8, SELECT_SIZE * 2, SELECT_SIZE * 2)) {
                select1 = draggedAspect;
            } else if (inRect(
                    mx, my, leftPos + SELECT2_HIT_X - 8, topPos + SELECT_HIT_Y - 8, SELECT_SIZE * 2, SELECT_SIZE * 2)) {
                select2 = draggedAspect;
            }
            draggedAspect = null;
            return true;
        }
        return super.mouseReleased(event);
    }

    private boolean handleHelperArrows(double mx, double my) {
        int lastPage = Math.max(0, (discoveredCompounds().size() - 1) / HELPER_ROWS);
        int center = leftPos + SHEET_X + SHEET_SIZE / 2;
        if (helperPage > 0
                && inRect(mx, my, center - HELPER_PAGE_HALF_GAP - ARROW_W, topPos + HELPER_ARROW_Y, ARROW_W, ARROW_H)) {
            helperPage--;
            playSound(TCSounds.KEY.get(), 0.3F, 1.0F);
            return true;
        }
        if (helperPage < lastPage
                && inRect(mx, my, center + HELPER_PAGE_HALF_GAP, topPos + HELPER_ARROW_Y, ARROW_W, ARROW_H)) {
            helperPage++;
            playSound(TCSounds.KEY.get(), 0.3F, 1.0F);
            return true;
        }
        return false;
    }

    private boolean handleArrows(double mx, double my) {
        int lastPage = lastPage(discoveredAspects().size());
        if (page > 0 && inRect(mx, my, leftPos + ARROW_PREV_X, topPos + ARROW_Y, ARROW_W, ARROW_H)) {
            page--;
            playSound(TCSounds.KEY.get(), 0.3F, 1.0F);
            return true;
        }
        if (page < lastPage && inRect(mx, my, leftPos + ARROW_NEXT_X, topPos + ARROW_Y, ARROW_W, ARROW_H)) {
            page++;
            playSound(TCSounds.KEY.get(), 0.3F, 1.0F);
            return true;
        }
        return false;
    }

    private boolean handleCombineButton(double mx, double my) {
        if (select1 == null
                || select2 == null
                || !inRect(mx, my, leftPos + COMBINE_X, topPos + COMBINE_Y, COMBINE_W, COMBINE_H)) {
            return false;
        }
        if (System.currentTimeMillis() < combineCooldownUntil) {
            return true;
        }
        combineCooldownUntil = System.currentTimeMillis() + COMBINE_COOLDOWN_MS;
        BlockEntityResearchTable table = table();
        boolean bonus1 = table != null
                && pool().amount(AspectPools.idOf(select1)) <= 0
                && table.bonusAspects().amountOf(select1) > 0;
        boolean bonus2 = table != null
                && pool().amount(AspectPools.idOf(select2)) <= 0
                && table.bonusAspects().amountOf(select2) > 0;
        ClientPacketDistributor.sendToServer(new ServerboundTableCombinePayload(
                menu.pos(), AspectPools.idOf(select1), AspectPools.idOf(select2), bonus1, bonus2));
        playSound(TCSounds.HHON.get(), 0.3F, 1.0F);
        return true;
    }

    private boolean handleSelectRemove(double mx, double my) {
        if (select1 != null
                && inRect(mx, my, leftPos + SELECT1_HIT_X, topPos + SELECT_HIT_Y, SELECT_SIZE, SELECT_SIZE)) {
            select1 = null;
            playSound(TCSounds.HHOFF.get(), 0.2F, 1.0F);
            return true;
        }
        if (select2 != null
                && inRect(mx, my, leftPos + SELECT2_HIT_X, topPos + SELECT_HIT_Y, SELECT_SIZE, SELECT_SIZE)) {
            select2 = null;
            playSound(TCSounds.HHOFF.get(), 0.2F, 1.0F);
            return true;
        }
        return false;
    }

    private boolean handleDuplicate(double mx, double my) {
        BlockEntityResearchTable table = table();
        if (table == null
                || minecraft.player == null
                || !inRect(mx, my, leftPos + DUPE_X, topPos + DUPE_Y, DUPE_SIZE, DUPE_SIZE)) {
            return false;
        }
        ResearchNoteData data = noteData();
        if (data == null
                || !data.complete()
                || !KnowledgeAccess.of(minecraft.player)
                        .isResearchComplete(BlockEntityResearchTable.RESEARCH_DUPLICATION)) {
            return false;
        }
        ClientPacketDistributor.sendToServer(new ServerboundTableDuplicatePayload(menu.pos()));
        playSound(TCSounds.CLACK.get(), 0.4F, 1.0F);
        return true;
    }

    private static boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void playSound(SoundEvent sound, float volume, float pitch) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(sound, volume, pitch);
        }
    }
}
