package com.leclowndu93150.thaumaturge.client.screen.casters;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.casters.FocusEffect;
import com.leclowndu93150.thaumaturge.api.casters.FocusElement;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusMedium;
import com.leclowndu93150.thaumaturge.api.casters.FocusMod;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.FocusSplit;
import com.leclowndu93150.thaumaturge.api.casters.SettingDefinition;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTCContainerScreen;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCImageButton;
import com.leclowndu93150.thaumaturge.content.casters.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.casters.FocusElementNode;
import com.leclowndu93150.thaumaturge.content.casters.ItemFocus;
import com.leclowndu93150.thaumaturge.content.casters.MenuFocalManipulator;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.network.ServerboundFocusDataPayload;
import com.leclowndu93150.thaumaturge.registry.TCFocusElements;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class FocalManipulatorScreen extends AbstractTCContainerScreen<MenuFocalManipulator> {
    private static final Identifier TEX = TCIds.rl("textures/gui/gui_wandtable.png");
    private static final Identifier TEX2 = TCIds.rl("textures/gui/gui_wandtable2.png");
    private static final Identifier TEX3 = TCIds.rl("textures/gui/gui_wandtable3.png");
    private static final Identifier TEX_BASE = TCIds.rl("textures/gui/gui_base.png");
    private static final Identifier TEX_COMPLEXITY = TCIds.rl("textures/gui/complex.png");
    private static final Identifier TEX_COST_XP = TCIds.rl("textures/gui/costxp.png");
    private static final Identifier TEX_COST_VIS = TCIds.rl("textures/gui/costvis.png");
    private static final Identifier ICON_MEDIUM = TCIds.rl("textures/foci/_medium.png");
    private static final Identifier ICON_EFFECT = TCIds.rl("textures/foci/_effect.png");
    private static final Identifier ROOT_KEY = Identifier.fromNamespaceAndPath(TCIds.MODID, "root");

    private static final int GUI_SIZE = 231;
    private static final int ATLAS = 256;
    private static final int SIDE_PANEL_W = 71;
    private static final int SIDE_PANEL_H = 239;
    private static final int SIDE_PANEL_X = -71;
    private static final int SIDE_PANEL_Y = -3;
    private static final int NAME_X = 30;
    private static final int NAME_Y = 11;
    private static final int NAME_W = 170;
    private static final int NAME_H = 12;
    private static final int NAME_MAX = 50;
    private static final int CONFIRM_X = 234;
    private static final int CONFIRM_Y = 18;
    private static final int CONFIRM_W = 24;
    private static final int CONFIRM_H = 16;
    private static final int CONFIRM_U = 232;
    private static final int CONFIRM_V = 240;
    private static final int INFO_X = 233;
    private static final int INFO_W = 16;
    private static final int INFO_H = 16;
    private static final int INFO_COMPLEXITY_Y = 39;
    private static final int INFO_XP_Y = 53;
    private static final int INFO_VIS_Y = 67;
    private static final int STAT_TEXT_X = 252;
    private static final int STAT_TEXT_Y_NUDGE = 4;
    private static final int STAT_COMPLEXITY_Y = 36;
    private static final int STAT_XP_Y = 50;
    private static final int STAT_VIS_Y = 64;
    private static final int CAST_COST_X = 233;
    private static final int CAST_COST_Y = 88;
    private static final int COMPONENTS_LABEL_Y = 100;
    private static final int COMPONENT_X = 233;
    private static final int COMPONENT_Y = 114;
    private static final int CANVAS_X = 63;
    private static final int CANVAS_Y = 31;
    private static final int CANVAS_W = 136;
    private static final int CANVAS_H = 160;
    private static final int NODE_ORIGIN_X = 132;
    private static final int NODE_ORIGIN_Y = 48;
    private static final int NODE_STEP_X = 24;
    private static final int NODE_STEP_Y = 32;
    private static final int NODE_CLIP_X = 48;
    private static final int NODE_CLIP_Y = 16;
    private static final int NODE_CLIP_W = 154;
    private static final int NODE_CLIP_H = 192;
    private static final int CLIP_CENTER_X = 48;
    private static final int CLIP_CENTER_Y = 26;
    private static final int CLIP_CENTER_W = 166;
    private static final int CLIP_CENTER_H = 174;
    private static final int PART_LIST_X = 38;
    private static final int PART_LIST_Y = 43;
    private static final int PART_DRAW_SPACING = 25;
    private static final int PART_HOVER_SPACING = 24;
    private static final int PART_HOVER_X = 28;
    private static final int PART_HOVER_Y = 32;
    private static final int PART_HOVER_SIZE = 20;
    private static final int PARTS_SHOWN = 6;
    private static final int U_SELECTION = 96;
    private static final int U_BLANK = 120;
    private static final int V_NODE_CHROME = 232;
    private static final int NODE_CHROME_SIZE = 24;
    private static final int U_LINK = 54;
    private static final int LINK_SIZE = 12;
    private static final int V_SPLIT = 240;
    private static final int U_SPLIT_LEFT = 8;
    private static final int U_SPLIT_RIGHT = 24;
    private static final int U_SPLIT_MID = 72;
    private static final int SPLIT_END_W = 16;
    private static final int SPLIT_MID_W = 24;
    private static final int SPLIT_H = 16;
    private static final int U_MARK_TARGET = 152;
    private static final int U_MARK_TRAJECTORY = 168;
    private static final int MARK_SIZE = 16;
    private static final int NAME_PLATE_Y = 8;
    private static final int NAME_PLATE_X = 24;
    private static final int NAME_PLATE_U_LEFT = 192;
    private static final int NAME_PLATE_U_MID = 200;
    private static final int NAME_PLATE_U_RIGHT = 208;
    private static final int NAME_PLATE_V = 224;
    private static final int NAME_PLATE_SEG_W = 8;
    private static final int NAME_PLATE_H = 14;
    private static final int NAME_PLATE_SEGMENTS = 22;
    private static final int SLIDER_PARTS_X = 51;
    private static final int SLIDER_PARTS_Y = 30;
    private static final int SLIDER_PARTS_H = 149;
    private static final int SLIDER_SIDE_X = 203;
    private static final int SLIDER_SIDE_Y = 32;
    private static final int SLIDER_SIDE_H = 156;
    private static final int SLIDER_BOTTOM_X = 64;
    private static final int SLIDER_BOTTOM_Y = 195;
    private static final int SLIDER_BOTTOM_W = 132;
    private static final int SLIDER_W = 8;
    private static final int SIDE_SCROLL_THRESHOLD = 130;
    private static final int BOTTOM_SCROLL_RANGE = 70;
    private static final int JEI_RIGHT_PANEL_W = 90;
    private static final int JEI_BOTTOM_EXTRA = 40;
    private static final int COLOR_STAT_BAD = 0xFFF67578;
    private static final int COLOR_STAT_GOOD = 0xFFFFC13F;
    private static final int COLOR_STAT_NEUTRAL = 0xFF99FF8D;

    private final DecimalFormat format = new DecimalFormat("#######.##");

    private @Nullable BlockEntityFocalManipulator table;
    private @Nullable EditBox nameField;
    private TCImageButton buttonConfirm;
    private final List<Identifier> shownParts = new ArrayList<>();
    private final List<FocusSettingSpinner> spinners = new ArrayList<>();
    private @Nullable FocusSlider sliderParts;
    private @Nullable FocusSlider sliderSide;
    private @Nullable FocusSlider sliderBottom;
    private @Nullable List<ItemStack> components;
    private int partsStart;
    private int totalComplexity;
    private int maxComplexity;
    private int lastNodeHover = -1;
    private int selectedNode = -1;
    private int nodeId;
    private float costCast;
    private int costXp;
    private int costVis;
    private int scrollX;
    private int scrollY;
    private int sMinX;
    private int sMinY;
    private int sMaxX;
    private int sMaxY;
    private boolean valid;
    private boolean draggingCanvas;
    private double lastDragX;
    private double lastDragY;
    private int lastDataStamp = -1;
    private ItemStack lastFocusStack = ItemStack.EMPTY;

    public FocalManipulatorScreen(MenuFocalManipulator menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEX2, GUI_SIZE, GUI_SIZE);
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft != null && minecraft.level != null && minecraft.level.getBlockEntity(menu.pos()) instanceof BlockEntityFocalManipulator be) {
            table = be;
            lastDataStamp = be.clientDataStamp;
        }
        buttonConfirm = new TCImageButton(leftPos + CONFIRM_X, topPos + CONFIRM_Y, CONFIRM_W, CONFIRM_H, TEX_BASE, CONFIRM_U, CONFIRM_V, CONFIRM_W, CONFIRM_H, ATLAS, ATLAS,
                Component.translatable("gui.thaumaturge.wandtable.craft"), this::confirmCraft);
        nameField = new EditBox(font, leftPos + NAME_X, topPos + NAME_Y, NAME_W, NAME_H, Component.empty());
        nameField.setTextColor(-1);
        nameField.setBordered(false);
        nameField.setMaxLength(NAME_MAX);
        nameField.setResponder(this::onNameChanged);
        if (table != null) {
            if (table.focusName.isEmpty() && !menu.getSlot(0).getItem().isEmpty()) {
                table.focusName = menu.getSlot(0).getItem().getHoverName().getString();
            }
            nameField.setValue(table.focusName);
            lastFocusStack = table.focusStack();
            if (!lastFocusStack.isEmpty() && table.data.isEmpty() && table.vis <= 0.0F) {
                resetNodes();
            }
        }
        gatherInfo(false);
    }

    private void confirmCraft() {
        if (minecraft != null && minecraft.gameMode != null && valid && table != null && table.vis <= 0.0F) {
            gatherInfo(true);
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MenuFocalManipulator.BUTTON_START_CRAFT);
        }
    }

    private void onNameChanged(String value) {
        if (table != null && !value.equals(table.focusName)) {
            table.focusName = value;
            sendData();
        }
    }

    private void sendData() {
        if (table != null) {
            ClientPacketDistributor.sendToServer(new ServerboundFocusDataPayload(menu.pos(), table.focusName, List.copyOf(table.data.values())));
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (table == null) {
            return;
        }
        ItemStack current = table.focusStack();
        if (!ItemStack.matches(current, lastFocusStack)) {
            lastFocusStack = current.copy();
            table.data.clear();
            table.focusName = current.isEmpty() ? "" : current.getHoverName().getString();
            if (nameField != null) {
                nameField.setValue(table.focusName);
            }
            if (!current.isEmpty()) {
                resetNodes();
            } else {
                gatherInfo(false);
            }
        }
        if (table.clientDataStamp != lastDataStamp) {
            lastDataStamp = table.clientDataStamp;
            gatherInfo(false);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {}

    public List<Rect2i> jeiExtraAreas() {
        List<Rect2i> areas = new ArrayList<>();
        areas.add(new Rect2i(leftPos + imageWidth, topPos, JEI_RIGHT_PANEL_W, imageHeight + JEI_BOTTOM_EXTRA));
        areas.add(new Rect2i(leftPos + SIDE_PANEL_X, topPos + SIDE_PANEL_Y, SIDE_PANEL_W, SIDE_PANEL_H));
        return areas;
    }

    @Override
    protected void extractBackgroundOverlay(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX3, leftPos + SIDE_PANEL_X, topPos + SIDE_PANEL_Y, 0, 0, SIDE_PANEL_W, SIDE_PANEL_H, ATLAS, ATLAS);
        drawNodes(graphics, leftPos + NODE_ORIGIN_X - scrollX, topPos + NODE_ORIGIN_Y - scrollY, mouseX, mouseY);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, leftPos, topPos, 0, 0, GUI_SIZE, GUI_SIZE, ATLAS, ATLAS);
        drawStats(graphics, mouseX, mouseY);
        drawPartsList(graphics, mouseX, mouseY);
        drawNamePlate(graphics);
        if (buttonConfirm != null) {
            buttonConfirm.active = table != null && table.vis <= 0.0F && valid;
        }
    }

    private void drawStats(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        drawStatIcon(graphics, TEX_COMPLEXITY, INFO_COMPLEXITY_Y, "gui.thaumaturge.wandtable.complexity", mouseX, mouseY);
        drawStatIcon(graphics, TEX_COST_XP, INFO_XP_Y, "gui.thaumaturge.wandtable.xp_cost", mouseX, mouseY);
        drawStatIcon(graphics, TEX_COST_VIS, INFO_VIS_Y, "gui.thaumaturge.wandtable.vis_cost", mouseX, mouseY);
        if (maxComplexity > 0) {
            graphics.text(font, Component.literal(totalComplexity + "/" + maxComplexity), leftPos + STAT_TEXT_X, topPos + INFO_COMPLEXITY_Y + STAT_TEXT_Y_NUDGE,
                    totalComplexity > maxComplexity ? COLOR_STAT_BAD : COLOR_STAT_GOOD, true);
        }
        boolean creative = minecraft != null && minecraft.player != null && minecraft.player.getAbilities().instabuild;
        int playerLevel = minecraft != null && minecraft.player != null ? minecraft.player.experienceLevel : 0;
        graphics.text(font, Component.literal(Integer.toString(costXp)), leftPos + STAT_TEXT_X, topPos + INFO_XP_Y + STAT_TEXT_Y_NUDGE,
                costXp > playerLevel && !creative ? COLOR_STAT_BAD : COLOR_STAT_NEUTRAL, true);
        int visShown = table != null && table.vis > 0.0F ? (int) table.vis : costVis;
        graphics.text(font, Component.literal(Integer.toString(visShown)).withStyle(ChatFormatting.AQUA), leftPos + STAT_TEXT_X, topPos + INFO_VIS_Y + STAT_TEXT_Y_NUDGE, COLOR_STAT_NEUTRAL, true);
        if (costCast > 0.0F) {
            graphics.text(font, Component.translatable("gui.thaumaturge.wandtable.cast_cost", format.format(costCast)).withStyle(ChatFormatting.AQUA), leftPos + CAST_COST_X, topPos + CAST_COST_Y,
                    COLOR_STAT_NEUTRAL, true);
        }
        if (components != null && !components.isEmpty()) {
            graphics.text(font, Component.translatable("gui.thaumaturge.wandtable.components").withStyle(ChatFormatting.GOLD), leftPos + CAST_COST_X, topPos + COMPONENTS_LABEL_Y, COLOR_STAT_NEUTRAL,
                    true);
            int i = 0;
            int q = 0;
            for (ItemStack stack : components) {
                int ix = leftPos + COMPONENT_X + i * 16;
                int iy = topPos + COMPONENT_Y + q * 16;
                graphics.item(stack, ix, iy);
                if (mouseX >= ix && mouseX < ix + 16 && mouseY >= iy && mouseY < iy + 16) {
                    graphics.setComponentTooltipForNextFrame(font, List.of(stack.getHoverName(), Component.literal("x" + stack.getCount()).withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
                }
                if (++i > 4) {
                    i = 0;
                    q++;
                }
            }
        }
    }

    private void drawStatIcon(GuiGraphicsExtractor graphics, Identifier texture, int y, String tooltipKey, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + INFO_X, topPos + y, 0, 0, INFO_W, INFO_H, INFO_W, INFO_H);
        if (mouseX >= leftPos + INFO_X && mouseX < leftPos + INFO_X + INFO_W && mouseY >= topPos + y && mouseY < topPos + y + INFO_H) {
            graphics.setComponentTooltipForNextFrame(font, List.of(Component.translatable(tooltipKey)), mouseX, mouseY);
        }
    }

    private void drawNamePlate(GuiGraphicsExtractor graphics) {
        if (table == null || table.data.isEmpty()) {
            return;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BASE, leftPos + NAME_PLATE_X, topPos + NAME_PLATE_Y, NAME_PLATE_U_LEFT, NAME_PLATE_V, NAME_PLATE_SEG_W, NAME_PLATE_H, ATLAS, ATLAS);
        int a = 1;
        for (a = 1; a < NAME_PLATE_SEGMENTS; a++) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BASE, leftPos + NAME_PLATE_X + a * NAME_PLATE_SEG_W, topPos + NAME_PLATE_Y, NAME_PLATE_U_MID, NAME_PLATE_V, NAME_PLATE_SEG_W, NAME_PLATE_H,
                    ATLAS, ATLAS);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BASE, leftPos + NAME_PLATE_X + a * NAME_PLATE_SEG_W, topPos + NAME_PLATE_Y, NAME_PLATE_U_RIGHT, NAME_PLATE_V, NAME_PLATE_SEG_W, NAME_PLATE_H,
                ATLAS, ATLAS);
    }

    private void drawPartsList(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int count = 0;
        int index = 0;
        for (Identifier key : shownParts) {
            if (++count - 1 < partsStart) {
                continue;
            }
            FocusElement element = FocusEngine.element(key);
            if (element != null) {
                boolean hover = isHovering(PART_HOVER_X, PART_HOVER_Y + PART_HOVER_SPACING * index, PART_HOVER_SIZE, PART_HOVER_SIZE, mouseX, mouseY);
                float scale = element instanceof FocusMod || element instanceof FocusSplit ? 24.0F : 32.0F;
                drawPart(graphics, key, element, leftPos + PART_LIST_X, topPos + PART_LIST_Y + PART_DRAW_SPACING * index, scale, hover);
                if (hover) {
                    graphics.setComponentTooltipForNextFrame(font, genPartText(key, element, -1), mouseX, mouseY);
                }
            }
            if (++index >= PARTS_SHOWN) {
                break;
            }
        }
    }

    private void drawNodes(GuiGraphicsExtractor graphics, int originX, int originY, int mouseX, int mouseY) {
        if (table == null || table.data.isEmpty()) {
            return;
        }
        int hover = -1;
        for (FocusElementNode fn : table.data.values()) {
            int xx = originX + fn.x * NODE_STEP_X;
            int yy = originY + fn.y * NODE_STEP_Y;
            boolean mouseover = isHovering(CANVAS_X, CANVAS_Y, CANVAS_W, CANVAS_H, mouseX, mouseY) && mouseX >= xx - 10 && mouseX < xx + 10 && mouseY >= yy - 10 && mouseY < yy + 10;
            if (mouseover && fn.parent >= 0) {
                hover = fn.id;
            }
            FocusElement fnElement = fn.resolve();
            if (fn.element != null && fnElement != null) {
                if (inClipRegion(xx - leftPos - 8, yy - topPos - 8, NODE_CLIP_X, NODE_CLIP_Y, NODE_CLIP_W, NODE_CLIP_H)) {
                    drawPart(graphics, fn.element, fnElement, xx, yy, 32.0F, mouseover);
                }
            } else {
                drawClippedRect(graphics, TEX, xx - 12, yy - 12, U_BLANK, V_NODE_CHROME, NODE_CHROME_SIZE, NODE_CHROME_SIZE);
            }
            if (selectedNode == fn.id || (mouseover && fn.parent >= 0)) {
                drawClippedRect(graphics, TEX, xx - 12, yy - 12, U_SELECTION, V_NODE_CHROME, NODE_CHROME_SIZE, NODE_CHROME_SIZE);
            }
            FocusElementNode parent = table.data.get(fn.parent);
            if (parent != null) {
                drawClippedRect(graphics, TEX, xx - 6, yy - 22, U_LINK, V_NODE_CHROME, LINK_SIZE, LINK_SIZE);
                if (parent.resolve() instanceof FocusSplit) {
                    int q = Math.abs(fn.x - parent.x);
                    for (int a = 0; a < q; a++) {
                        if (fn.x < parent.x) {
                            if (a == 0) {
                                drawClippedRect(graphics, TEX, xx - 4, yy - 36, U_SPLIT_LEFT, V_SPLIT, SPLIT_END_W, SPLIT_H);
                            } else {
                                drawClippedRect(graphics, TEX, xx - 12 + a * NODE_STEP_X, yy - 36, U_SPLIT_MID, V_SPLIT, SPLIT_MID_W, SPLIT_H);
                            }
                        } else if (a == 0) {
                            drawClippedRect(graphics, TEX, xx - 12, yy - 36, U_SPLIT_RIGHT, V_SPLIT, SPLIT_END_W, SPLIT_H);
                        } else {
                            drawClippedRect(graphics, TEX, xx - 12 - a * NODE_STEP_X, yy - 36, U_SPLIT_MID, V_SPLIT, SPLIT_MID_W, SPLIT_H);
                        }
                    }
                }
                if (fn.element == null) {
                    int s = parent.target && parent.trajectory ? 4 : 0;
                    if (inClipRegion(xx - leftPos - 4, yy - topPos - 4, NODE_CLIP_X, NODE_CLIP_Y, 168, NODE_CLIP_H)) {
                        if (parent.target) {
                            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, xx - s - 4, yy - 4, U_MARK_TARGET, V_SPLIT, 8, 8, MARK_SIZE, MARK_SIZE, ATLAS, ATLAS);
                        }
                        if (parent.trajectory) {
                            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, xx + s - 4, yy - 4, U_MARK_TRAJECTORY, V_SPLIT, 8, 8, MARK_SIZE, MARK_SIZE, ATLAS, ATLAS);
                        }
                    }
                }
            }
        }
        if (hover >= 0 && lastNodeHover != hover) {
            playRollover();
        }
        lastNodeHover = hover;
        if (hover >= 0) {
            FocusElementNode fn = table.data.get(hover);
            if (fn != null && fn.element != null && fn.resolve() != null) {
                graphics.setComponentTooltipForNextFrame(font, genPartText(fn.element, fn.resolve(), hover), mouseX, mouseY);
            }
        }
    }

    private void drawClippedRect(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int u, int v, int w, int h) {
        if (inClipRegion(x - leftPos + w / 2, y - topPos + h / 2, CLIP_CENTER_X, CLIP_CENTER_Y, CLIP_CENTER_W, CLIP_CENTER_H)) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, ATLAS, ATLAS);
        }
    }

    private static boolean inClipRegion(int px, int py, int x, int y, int w, int h) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    private void drawPart(GuiGraphicsExtractor graphics, Identifier key, FocusElement element, int x, int y, float scale, boolean mouseover) {
        boolean big = element instanceof FocusMod || element instanceof FocusSplit || key.equals(ROOT_KEY);
        if (big) {
            scale *= 2.0F;
        }
        int color = 0xFF000000 | FocusEngine.color(key);
        float backSize = scale * 0.9F + (mouseover ? 2 : 0);
        if (element instanceof FocusEffect) {
            blitCentered(graphics, ICON_EFFECT, x, y, backSize, color);
        } else if (element instanceof FocusMedium && !big) {
            blitCentered(graphics, ICON_MEDIUM, x, y, backSize, color);
        }
        float iconSize = scale / 2.0F + (mouseover ? 2 : 0);
        blitCentered(graphics, FocusEngine.icon(key), x, y, iconSize, 0xFFFFFFFF);
    }

    private static void blitCentered(GuiGraphicsExtractor graphics, Identifier texture, int cx, int cy, float size, int color) {
        int s = Math.round(size);
        int half = s / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, cx - half, cy - half, 0.0F, 0.0F, s, s, 32, 32, 32, 32, color);
    }

    private List<Component> genPartText(Identifier key, FocusElement element, int idx) {
        List<Component> list = new ArrayList<>();
        FocusElementNode placed = idx >= 0 && table != null ? table.data.get(idx) : null;
        FocusSettings settings = placed != null ? placed.resolvedSettings() : FocusSettings.defaults(element);
        list.add(Component.translatable(element.nameKey()));
        list.add(Component.translatable(element.descriptionKey()).withStyle(ChatFormatting.DARK_PURPLE));
        int c = element.complexity(settings);
        if (placed != null) {
            c = (int) (c * placed.complexityMultiplier);
        }
        Component complexity = Component.translatable("gui.thaumaturge.wandtable.part_complexity")
                .append(Component.literal(" " + c).withStyle(placed != null && placed.complexityMultiplier > 1.0F ? ChatFormatting.RED : ChatFormatting.GOLD)).withStyle(ChatFormatting.GOLD);
        list.add(complexity);
        float p = element.powerMultiplier(settings);
        if (placed != null && table != null) {
            p = placed.getPower(table.data);
        }
        if (p != 1.0F) {
            list.add(Component.translatable("gui.thaumaturge.wandtable.part_efficiency")
                    .append(Component.literal(" x" + format.format(p)).withStyle(p < 1.0F ? ChatFormatting.RED : ChatFormatting.GREEN)).withStyle(ChatFormatting.GOLD));
        }
        if (element instanceof FocusEffect effect) {
            float d = effect.damageForDisplay(settings, placed == null || table == null ? 1.0F : placed.getPower(table.data));
            if (d > 0.0F) {
                list.add(Component.translatable("attribute.modifier.equals.0", format.format(d), Component.translatable("attribute.name.attack_damage")).withStyle(ChatFormatting.DARK_RED));
            } else if (d < 0.0F) {
                list.add(Component.translatable("attribute.modifier.equals.0", format.format(-d), Component.translatable("gui.thaumaturge.wandtable.heal_power")).withStyle(ChatFormatting.DARK_GREEN));
            }
        }
        return list;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.closeContainer();
            }
            return true;
        }
        if (nameField != null && (nameField.keyPressed(event) || nameField.canConsumeInput())) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (nameField != null && nameField.mouseClicked(event, doubleClick)) {
            setFocused(nameField);
            return true;
        }
        if (table != null && table.vis <= 0.0F && !table.data.isEmpty()) {
            if (lastNodeHover >= 0) {
                selectedNode = lastNodeHover;
                FocusElementNode fn = table.data.get(selectedNode);
                if (button == 1 && fn != null && fn.element != null) {
                    FocusElementNode parent = table.data.get(fn.parent);
                    if (parent != null && parent.element != null) {
                        addNodeAt(parent.element, fn.parent, true);
                    }
                }
                gatherInfo(false);
                playButtonClick();
                return true;
            }
            if (selectedNode >= 0) {
                int count = 0;
                int index = 0;
                for (Identifier key : shownParts) {
                    if (++count - 1 < partsStart) {
                        continue;
                    }
                    if (isHovering(PART_HOVER_X, PART_HOVER_Y + PART_HOVER_SPACING * index, PART_HOVER_SIZE, PART_HOVER_SIZE, (int) mouseX, (int) mouseY)) {
                        addNodeAt(key, selectedNode, true);
                        playButtonClick();
                        return true;
                    }
                    if (++index >= PARTS_SHOWN) {
                        break;
                    }
                }
            }
        }
        if (button == 0 && isHovering(CANVAS_X, CANVAS_Y, CANVAS_W, CANVAS_H, (int) mouseX, (int) mouseY)) {
            draggingCanvas = true;
            lastDragX = mouseX;
            lastDragY = mouseY;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (draggingCanvas && event.button() == 0) {
            scrollX -= (int) (mouseX - lastDragX);
            scrollY -= (int) (mouseY - lastDragY);
            lastDragX = mouseX;
            lastDragY = mouseY;
            clampScroll();
            syncSliders();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0) {
            draggingCanvas = false;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (shownParts.size() > PARTS_SHOWN && isHovering(PART_HOVER_X - 4, PART_HOVER_Y - 8, 32, 157, (int) mouseX, (int) mouseY)) {
            if (scrollY > 0 && partsStart > 0) {
                partsStart--;
            } else if (scrollY < 0 && partsStart < shownParts.size() - PARTS_SHOWN) {
                partsStart++;
            }
            syncSliders();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean isHovering(int x, int y, int w, int h, int mouseX, int mouseY) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + w && mouseY >= topPos + y && mouseY < topPos + y + h;
    }

    private void clampScroll() {
        if (scrollY > (sMaxY - 3) * NODE_STEP_Y) {
            scrollY = (sMaxY - 3) * NODE_STEP_Y;
        }
        if (scrollY < 0) {
            scrollY = 0;
        }
        if (scrollX > sMaxX * NODE_STEP_X) {
            scrollX = sMaxX * NODE_STEP_X;
        }
        if (scrollX < sMinX * NODE_STEP_X) {
            scrollX = sMinX * NODE_STEP_X;
        }
    }

    private void playButtonClick() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(TCSounds.CLACK.get(), 1.0F, 0.4F));
        }
    }

    private void playRollover() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(TCSounds.CLACK.get(), 2.0F, 0.4F));
        }
    }

    private int getNextId() {
        while (table != null && table.data.containsKey(nodeId)) {
            nodeId++;
        }
        return nodeId;
    }

    private void cullChildren(int idx) {
        if (table != null && table.data.containsKey(idx)) {
            for (int i : table.data.get(idx).children) {
                cullChildren(i);
                table.data.remove(i);
            }
        }
    }

    private void addNodeAt(Identifier elementKey, int idx, boolean gather) {
        if (table == null) {
            return;
        }
        boolean same = false;
        FocusElementNode previous = null;
        if (table.data.containsKey(idx)) {
            cullChildren(idx);
            FocusElementNode existing = table.data.get(idx);
            if (existing.element != null && existing.element.equals(elementKey)) {
                same = true;
            } else {
                previous = table.data.remove(idx);
            }
        }
        FocusElementNode fn;
        FocusElement element;
        if (!same) {
            FocusElement created = FocusEngine.element(elementKey);
            if (created == null) {
                return;
            }
            fn = new FocusElementNode();
            element = created;
            fn.element = elementKey;
            for (SettingDefinition definition : created.settings()) {
                fn.settings.put(definition.key(), definition.defaultValue());
            }
            if (previous != null) {
                fn.x = previous.x;
                fn.y = previous.y;
            }
            fn.id = getNextId();
            selectedNode = fn.id;
            if (previous != null && table.data.containsKey(previous.parent)) {
                fn.parent = previous.parent;
                int[] c = table.data.get(previous.parent).children;
                for (int a = 0; a < c.length; a++) {
                    if (c[a] == previous.id) {
                        c[a] = fn.id;
                        break;
                    }
                }
            }
            fn.target = element.supplies().contains(FocusElement.SupplyType.TARGET);
            fn.trajectory = element.supplies().contains(FocusElement.SupplyType.TRAJECTORY);
            table.data.put(fn.id, fn);
        } else {
            fn = table.data.get(idx);
            element = fn.resolve();
        }
        if (fn.target || fn.trajectory) {
            if (element instanceof FocusSplit) {
                FocusElementNode blank1 = new FocusElementNode();
                blank1.parent = fn.id;
                blank1.id = getNextId();
                blank1.x = fn.x - 1;
                blank1.y = fn.y + 1;
                table.data.put(blank1.id, blank1);
                selectedNode = blank1.id;
                FocusElementNode blank2 = new FocusElementNode();
                blank2.parent = fn.id;
                blank2.x = fn.x + 1;
                blank2.y = fn.y + 1;
                blank2.id = getNextId();
                table.data.put(blank2.id, blank2);
                fn.children = new int[]{blank1.id, blank2.id};
            } else {
                FocusElementNode blank = new FocusElementNode();
                blank.parent = fn.id;
                blank.x = fn.x;
                blank.y = fn.y + 1;
                blank.id = getNextId();
                table.data.put(blank.id, blank);
                fn.children = new int[]{blank.id};
                selectedNode = blank.id;
            }
        }
        if (gather) {
            calcNodeTreeLayout();
            gatherInfo(true);
        }
    }

    private void resetNodes() {
        if (table == null) {
            return;
        }
        nodeId = 0;
        table.data.clear();
        addNodeAt(ROOT_KEY, 0, false);
        FocusElementNode root = table.data.get(0);
        if (root != null && root.children.length > 0) {
            selectedNode = root.children[0];
        }
        calcNodeTreeLayout();
        gatherInfo(true);
    }

    private void processLeftNodes(FocusElementNode start, int[] bounds) {
        if (table == null) {
            return;
        }
        if (start.children.length > 0) {
            processLeftNodes(table.data.get(start.children[0]), bounds);
        }
        int ox = 0;
        if (start.children.length == 1) {
            ox = bounds[0] - 1;
            bounds[0] = table.data.get(start.children[0]).x;
        }
        start.x = bounds[0];
        if (start.children.length == 1) {
            bounds[0] = ox;
        }
        bounds[0]++;
        if (start.children.length > 1) {
            processLeftNodes(table.data.get(start.children[1]), bounds);
        }
    }

    private void moveNodes(FocusElementNode start, int amt) {
        if (table == null) {
            return;
        }
        for (int ci : start.children) {
            moveNodes(table.data.get(ci), amt);
        }
        start.x -= amt;
    }

    private void calcNodeTreeLayout() {
        if (table == null) {
            return;
        }
        int fsi = -1;
        for (FocusElementNode node : table.data.values()) {
            if (fsi < 0 && node.resolve() instanceof FocusSplit) {
                fsi = node.id;
            }
        }
        if (fsi >= 0) {
            int[] bounds = {0};
            processLeftNodes(table.data.get(fsi), bounds);
            moveNodes(table.data.get(fsi), bounds[0] / 2);
        }
        for (FocusElementNode node : table.data.values()) {
            if (node.resolve() instanceof FocusSplit) {
                FocusElementNode parent = table.data.get(node.parent);
                if (parent != null && parent.element != null && !(parent.resolve() instanceof FocusSplit)) {
                    node.x = parent.x;
                } else if (node.children.length > 0) {
                    int xx = 0;
                    for (int a : node.children) {
                        xx += table.data.get(a).x;
                    }
                    node.x = xx / node.children.length;
                }
            }
        }
        if (selectedNode >= 0 && !table.data.containsKey(selectedNode)) {
            selectedNode = -1;
        }
    }

    private void calcScrollBounds() {
        sMinX = 0;
        sMinY = 0;
        sMaxX = 0;
        sMaxY = 0;
        if (table == null) {
            return;
        }
        for (FocusElementNode fn : table.data.values()) {
            sMinX = Math.min(sMinX, fn.x);
            sMinY = Math.min(sMinY, fn.y);
            sMaxX = Math.max(sMaxX, fn.x);
            sMaxY = Math.max(sMaxY, fn.y);
        }
    }

    private void gatherPartsList() {
        List<Identifier> previousParts = new ArrayList<>(shownParts);
        shownParts.clear();
        if (table == null || minecraft == null || minecraft.player == null || selectedNode < 0 || !table.data.containsKey(selectedNode)) {
            return;
        }
        List<Identifier> pMed = new ArrayList<>();
        List<Identifier> pEff = new ArrayList<>();
        List<Identifier> pMod = new ArrayList<>();
        List<Identifier> excluded = new ArrayList<>();
        boolean hasExclusive = false;
        boolean hasMedium = false;
        for (FocusElementNode fn : table.data.values()) {
            FocusElement placed = fn.resolve();
            if (placed instanceof FocusMedium) {
                hasMedium = !ROOT_KEY.equals(fn.element);
                if (placed.exclusive()) {
                    hasExclusive = true;
                    break;
                }
            }
            if (placed != null && placed.exclusive()) {
                excluded.add(fn.element);
            }
        }
        FocusElementNode node = table.data.get(selectedNode);
        FocusElementNode parent = table.data.get(node.parent);
        if (parent == null || parent.resolve() == null) {
            return;
        }
        FocusElement parentElement = parent.resolve();
        for (Identifier key : TCFocusElements.registry().keySet()) {
            if (key.equals(ROOT_KEY)) {
                continue;
            }
            FocusElement element = FocusEngine.element(key);
            if (element == null) {
                continue;
            }
            if (!ResearchManager.doesPassGate(minecraft.player, element.research())) {
                continue;
            }
            if (excluded.contains(key) || element.requires().isEmpty()) {
                continue;
            }
            boolean supplied = false;
            for (FocusElement.SupplyType type : element.requires()) {
                if (parentElement.supplies().contains(type)) {
                    supplied = true;
                    break;
                }
            }
            if (!supplied) {
                continue;
            }
            if (element instanceof FocusEffect) {
                pEff.add(key);
            } else if (element instanceof FocusMedium) {
                if (!hasExclusive && (!element.exclusive() || !hasMedium)) {
                    pMed.add(key);
                }
            } else {
                pMod.add(key);
            }
        }
        Collections.sort(pMed);
        Collections.sort(pEff);
        Collections.sort(pMod);
        shownParts.addAll(pMed);
        shownParts.addAll(pEff);
        shownParts.addAll(pMod);
        if (!shownParts.equals(previousParts)) {
            partsStart = 0;
        }
        partsStart = Mth.clamp(partsStart, 0, Math.max(0, shownParts.size() - PARTS_SHOWN));
    }

    private void gatherInfo(boolean sync) {
        spinners.forEach(this::removeWidget);
        spinners.clear();
        if (buttonConfirm != null) {
            removeWidget(buttonConfirm);
            addRenderableWidget(buttonConfirm);
        }
        if (nameField != null) {
            removeWidget(nameField);
            if (table != null && !table.data.isEmpty()) {
                addRenderableWidget(nameField);
            }
        }
        if (table == null) {
            return;
        }
        FocusElementNode selected = table.data.get(selectedNode);
        FocusElement selectedElement = selected != null ? selected.resolve() : null;
        if (selected != null && selectedElement != null && !selectedElement.settings().isEmpty()) {
            int a = 0;
            List<SettingDefinition> definitions = selectedElement.settings();
            int settingCount = definitions.size();
            for (SettingDefinition definition : definitions) {
                int w = definition.values() instanceof SettingDefinition.IntList ? 72 : 32;
                FocusSettingSpinner spinner = new FocusSettingSpinner(leftPos + imageWidth, topPos + imageHeight + 3 - settingCount * 26 + a * 26, w, definition, selected.settings,
                        () -> gatherInfo(true));
                spinners.add(spinner);
                addRenderableWidget(spinner);
                a++;
            }
        }
        shownParts.clear();
        components = null;
        totalComplexity = 0;
        maxComplexity = 0;
        ItemStack focus = table.focusStack();
        if (focus.isEmpty()) {
            valid = false;
            return;
        }
        if (focus.getItem() instanceof ItemFocus focusItem) {
            maxComplexity = focusItem.getMaxComplexity();
        }
        boolean emptyNodes = false;
        Map<String, Integer> compCount = new HashMap<>();
        Map<ResourceKey<IAspect>, Integer> crystalAspects = new LinkedHashMap<>();
        for (FocusElementNode fn : table.data.values()) {
            FocusElement placed = fn.resolve();
            if (placed != null) {
                int a = compCount.getOrDefault(fn.element.toString(), 0);
                fn.complexityMultiplier = 0.5F * (++a + 1);
                compCount.put(fn.element.toString(), a);
                totalComplexity = (int) (totalComplexity + placed.complexity(fn.resolvedSettings()) * fn.complexityMultiplier);
                if (placed.aspect() != null) {
                    crystalAspects.merge(placed.aspect(), 1, Integer::sum);
                }
            } else {
                emptyNodes = true;
            }
        }
        costCast = totalComplexity / 5.0F;
        costVis = totalComplexity * 10 + maxComplexity / 5;
        costXp = (int) Math.max(1L, Math.round(Math.sqrt(totalComplexity)));
        boolean validCrystals = false;
        if (!crystalAspects.isEmpty() && minecraft != null && minecraft.player != null && minecraft.level != null) {
            validCrystals = true;
            List<ItemStack> stacks = new ArrayList<>();
            var registry = minecraft.level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
            for (var entry : crystalAspects.entrySet()) {
                ItemStack crystal = EssentiaCrystalFactory.of(registry.getOrThrow(entry.getKey()), entry.getValue());
                stacks.add(crystal);
                if (!carrying(crystal)) {
                    validCrystals = false;
                }
            }
            components = stacks;
        }
        gatherPartsList();
        boolean creative = minecraft != null && minecraft.player != null && minecraft.player.getAbilities().instabuild;
        int playerLevel = minecraft != null && minecraft.player != null ? minecraft.player.experienceLevel : 0;
        valid = totalComplexity <= maxComplexity && !emptyNodes && validCrystals && (creative || costXp <= playerLevel);
        updateConfirmTooltip(emptyNodes, validCrystals, playerLevel);
        calcScrollBounds();
        clampScroll();
        rebuildSliders();
        if (table.focusName.isEmpty() && !focus.isEmpty()) {
            table.focusName = focus.getHoverName().getString();
            if (nameField != null) {
                nameField.setValue(table.focusName);
            }
        }
        if (sync) {
            sendData();
        }
    }

    private void rebuildSliders() {
        if (sliderParts != null) {
            removeWidget(sliderParts);
            sliderParts = null;
        }
        if (sliderSide != null) {
            removeWidget(sliderSide);
            sliderSide = null;
        }
        if (sliderBottom != null) {
            removeWidget(sliderBottom);
            sliderBottom = null;
        }
        if (shownParts.size() > PARTS_SHOWN) {
            sliderParts = new FocusSlider(leftPos + SLIDER_PARTS_X, topPos + SLIDER_PARTS_Y, SLIDER_W, SLIDER_PARTS_H, 0.0F, shownParts.size() - PARTS_SHOWN, partsStart, true,
                    v -> partsStart = Math.round(v));
            addRenderableWidget(sliderParts);
        }
        if (sMaxY * NODE_STEP_Y > SIDE_SCROLL_THRESHOLD) {
            sliderSide = new FocusSlider(leftPos + SLIDER_SIDE_X, topPos + SLIDER_SIDE_Y, SLIDER_W, SLIDER_SIDE_H, 0.0F, (sMaxY - 3) * NODE_STEP_Y, scrollY, true, v -> scrollY = Math.round(v));
            addRenderableWidget(sliderSide);
        } else {
            scrollY = Math.min(scrollY, Math.max(0, (sMaxY - 3) * NODE_STEP_Y));
        }
        if (sMinX * NODE_STEP_X >= -BOTTOM_SCROLL_RANGE && sMaxX * NODE_STEP_X <= BOTTOM_SCROLL_RANGE) {
            scrollX = Mth.clamp(scrollX, Math.min(0, sMinX * NODE_STEP_X), Math.max(0, sMaxX * NODE_STEP_X));
        } else {
            sliderBottom = new FocusSlider(leftPos + SLIDER_BOTTOM_X, topPos + SLIDER_BOTTOM_Y, SLIDER_BOTTOM_W, SLIDER_W, sMinX * NODE_STEP_X, sMaxX * NODE_STEP_X, scrollX, false,
                    v -> scrollX = Math.round(v));
            addRenderableWidget(sliderBottom);
        }
    }

    private void syncSliders() {
        if (sliderParts != null) {
            sliderParts.setValue(partsStart);
        }
        if (sliderSide != null) {
            sliderSide.setValue(scrollY);
        }
        if (sliderBottom != null) {
            sliderBottom.setValue(scrollX);
        }
    }

    private void updateConfirmTooltip(boolean emptyNodes, boolean validCrystals, int playerLevel) {
        if (buttonConfirm == null) {
            return;
        }
        MutableComponent text = Component.translatable("gui.thaumaturge.wandtable.craft").copy();
        if (table != null && table.vis > 0.0F) {
            text.append(newline("gui.thaumaturge.wandtable.problem.in_progress"));
        } else {
            if (totalComplexity > maxComplexity) {
                text.append(newline("gui.thaumaturge.wandtable.problem.complexity", totalComplexity, maxComplexity));
            }
            if (emptyNodes) {
                text.append(newline("gui.thaumaturge.wandtable.problem.empty_nodes"));
            }
            if (!validCrystals && components != null) {
                for (ItemStack stack : components) {
                    if (!carrying(stack)) {
                        text.append(Component.literal("\n")
                                .append(Component.translatable("gui.thaumaturge.wandtable.problem.crystal", stack.getCount(), stack.getHoverName()).withStyle(ChatFormatting.RED)));
                    }
                }
            }
            if (components == null || components.isEmpty()) {
                text.append(newline("gui.thaumaturge.wandtable.problem.no_effects"));
            }
            if (costXp > playerLevel && !(minecraft != null && minecraft.player != null && minecraft.player.getAbilities().instabuild)) {
                text.append(newline("gui.thaumaturge.wandtable.problem.xp", costXp));
            }
            if (valid) {
                text.append(Component.literal("\n").append(Component.translatable("gui.thaumaturge.wandtable.problem.ready").withStyle(ChatFormatting.GREEN)));
            }
        }
        buttonConfirm.setTooltip(Tooltip.create(text));
    }

    private static Component newline(String key, Object... args) {
        return Component.literal("\n").append(Component.translatable(key, args).withStyle(ChatFormatting.RED));
    }

    private boolean carrying(ItemStack required) {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }
        int found = 0;
        var inv = minecraft.player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, required)) {
                found += stack.getCount();
                if (found >= required.getCount()) {
                    return true;
                }
            }
        }
        return false;
    }
}
