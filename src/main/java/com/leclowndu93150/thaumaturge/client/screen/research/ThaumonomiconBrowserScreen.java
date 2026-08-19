package com.leclowndu93150.thaumaturge.client.screen.research;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumaturge.api.research.ResearchParent;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.client.render.research.ConnectorRenderer;
import com.leclowndu93150.thaumaturge.client.render.research.EntryIconRenderer;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTCScreen;
import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.tooltip.TCTooltipRenderer;
import com.leclowndu93150.thaumaturge.network.ServerboundClearResearchFlagsPayload;
import com.leclowndu93150.thaumaturge.network.ServerboundUnlockResearchPayload;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public final class ThaumonomiconBrowserScreen extends AbstractTCScreen {
    private static final int CELL_SIZE = 24;
    private static final int START_X = 16;
    private static final int START_Y = 16;
    private static final int CORNER_SIZE = 22;
    private static final int CORNER_UV = 13;
    private static final int EDGE_TILE = 64;
    private static final int EDGE_UV = 48;
    private static final int BORDER_OFFSET = -2;
    private static final int FRAME_INSET = 48;
    private static final int FRAME_MARGIN = 16;
    private static final int FRAME_EDGE_FROM_RIGHT = 20;
    private static final int FRAME_EDGE_FROM_BOTTOM = 20;

    private static final int CATEGORY_BUTTON_X_LEFT = 1;
    private static final int CATEGORY_BUTTON_X_RIGHT_MARGIN = 17;
    private static final int CATEGORY_BUTTON_FIRST_Y_OFFSET = 10;
    private static final int CATEGORY_BUTTON_STRIDE_Y = 24;
    private static final int CATEGORY_FRAME_OFFSET = 3;
    private static final int CATEGORY_FRAME_SIZE = 22;
    private static final int CATEGORY_ICON_SIZE = 16;
    private static final int CATEGORY_LABEL_GAP_X = 22;
    private static final int CATEGORY_LABEL_FROM_RIGHT_OFFSET = 9;
    private static final int CATEGORY_LABEL_LINE_HEIGHT = 9;
    private static final int CATEGORY_LABEL_Y_OFFSET = 4;
    private static final int CATEGORY_AVAILABLE_HEIGHT_PADDING = 28;
    private static final int CATEGORY_BADGE_SIZE = 32;
    private static final float CATEGORY_BADGE_SCALE = 0.25F;
    private static final int CATEGORY_BADGE_OFFSET_X = -2;
    private static final int CATEGORY_BADGE_RESEARCH_OFFSET_Y = -2;
    private static final int CATEGORY_BADGE_PAGE_OFFSET_Y = 9;
    private static final int CATEGORY_BADGE_RESEARCH_U = 176;
    private static final int CATEGORY_BADGE_PAGE_U = 208;
    private static final int CATEGORY_BADGE_V = 16;
    private static final int CATEGORY_BADGE_TINT = 0xB3FFFFFF;
    private static final int CATEGORY_TAB_ACTIVE_TINT = 0xFF99FFFF;
    private static final int CATEGORY_TAB_INACTIVE_TINT = 0xFFFFFFFF;
    private static final int CATEGORY_TAB_ACTIVE_ICON_TINT = 0xFFFFFFFF;
    private static final int CATEGORY_TAB_INACTIVE_ICON_TINT = 0xCCA8A8A8;

    private static final int SCROLL_BUTTON_X_FROM_RIGHT = 14;
    private static final int SCROLL_BUTTON_UP_Y = 20;
    private static final int SCROLL_BUTTON_WIDTH = 10;
    private static final int SCROLL_BUTTON_HEIGHT = 11;
    private static final int SCROLL_BUTTON_UV_U = 51;
    private static final int SCROLL_BUTTON_UV_UP_V = 55;
    private static final int SCROLL_BUTTON_UV_DOWN_V = 71;
    private static final int SCROLL_BUTTON_TINT_HOVER = 0xFFFFFFFF;
    private static final int SCROLL_BUTTON_TINT_IDLE = 0xFFB3B3B3;

    private static final int SEARCH_BUTTON_X = 1;
    private static final int SEARCH_BUTTON_Y_OFFSET_FROM_BOTTOM = 17;
    private static final int SEARCH_BUTTON_SIZE = 16;
    private static final int SEARCH_BUTTON_U = 160;
    private static final int SEARCH_BUTTON_V = 16;
    private static final int SEARCH_BUTTON_LABEL_X_OFFSET = 19;
    private static final int SEARCH_BUTTON_LABEL_Y_OFFSET = 4;
    private static final int SEARCH_BUTTON_TINT_HOVER = 0xFFFFFFFF;
    private static final int SEARCH_BUTTON_TINT_IDLE = 0xFFCCCCCC;

    private static final int SEARCH_BOX_X = 20;
    private static final int SEARCH_BOX_Y = 20;
    private static final int SEARCH_BOX_WIDTH = 89;
    private static final int SEARCH_BOX_HEIGHT = 12;
    private static final int SEARCH_RESULT_TEXT_X = 32;
    private static final int SEARCH_RESULT_TEXT_Y_START = 33;
    private static final int SEARCH_RESULT_ROW_HEIGHT = 10;
    private static final int SEARCH_RESULT_OVERFLOW_Y_OFFSET = 2;
    private static final int SEARCH_RESULT_HIT_LEFT_X = 22;
    private static final int SEARCH_RESULT_HIT_RIGHT_OFFSET = 18;
    private static final int SEARCH_RESULT_ICON_X = 22;
    private static final float SEARCH_RESULT_ICON_SCALE = 0.5F;
    private static final int SEARCH_RESULT_ICON_U = 224;
    private static final int SEARCH_RESULT_ICON_V = 48;
    private static final int SEARCH_RESULT_ICON_W = 16;
    private static final int SEARCH_RESULT_COLOR_CATEGORY = 0xFFDDAAAA;
    private static final int SEARCH_RESULT_COLOR_ENTRY = 0xFFDDDDDD;
    private static final int SEARCH_RESULT_COLOR_RECIPE = 0xFFAAAADD;
    private static final int SEARCH_RESULT_COLOR_CATEGORY_HOVER = 0xFFFFCCCC;
    private static final int SEARCH_RESULT_COLOR_ENTRY_HOVER = 0xFFFFFFFF;
    private static final int SEARCH_RESULT_COLOR_RECIPE_HOVER = 0xFFCCCCFF;
    private static final int SEARCH_OVERFLOW_COLOR = 0xFFAAAAAA;

    private static final int POPUP_X = 10;
    private static final int POPUP_Y = 34;
    private static final long POPUP_DURATION_MS = 3000L;

    private static final int PAN_SNAP_DISTANCE_SQUARED = 4;
    private static final float PAN_INERTIA_FACTOR = 0.85F;
    private static final float MAX_ZOOM = 2.0F;
    private static final float MIN_ZOOM = 1.0F;
    private static final float ZOOM_STEP = 0.25F;

    private static final int HOVER_LABEL_COLOR = 0xFFFFFFFF;
    private static final int TOOLTIP_OFFSET_X = 3;
    private static final int TOOLTIP_OFFSET_Y = -3;

    private static final int CONNECTOR_PARENT_KNOWN = 0xFF999999;
    private static final int CONNECTOR_PARENT_UNKNOWN = 0xFF333333;
    private static final int CONNECTOR_SIBLING_KNOWN = 0xFF4C4C66;
    private static final int CONNECTOR_SIBLING_UNKNOWN = 0xFF2F2F3F;
    private static final float CONNECTOR_Z_PARENT_KNOWN = 3.0F;
    private static final float CONNECTOR_Z_PARENT_UNKNOWN = 2.0F;
    private static final float CONNECTOR_Z_SIBLING_KNOWN = 1.0F;
    private static final float CONNECTOR_Z_SIBLING_UNKNOWN = 0.0F;
    private static final double UNSET_PERSIST = -9999.0;
    private static final int ENTRY_CULL_BORDER = 24;
    private static final int ENTRY_HIT_NEG_PADDING = 2;
    private static final int ENTRY_HIT_POS_PADDING = 18;

    private static double persistedX = UNSET_PERSIST;
    private static double persistedY = UNSET_PERSIST;
    private static @Nullable Identifier persistedCategoryId = null;
    private static int persistedCatScrollPos = 0;
    private static boolean persistedSearching = false;

    private final List<Holder.Reference<IResearchCategory>> categoriesTC = new ArrayList<>();
    private final List<Holder.Reference<IResearchCategory>> categoriesOther = new ArrayList<>();
    private final List<Holder.Reference<IResearchCategory>> categoriesOtherVisible = new ArrayList<>();
    private final Map<Holder.Reference<IResearchCategory>, List<EntryNode>> nodesByCategory = new HashMap<>();
    private final List<EntryNode> allEntries = new ArrayList<>();
    private final List<SearchResult> searchResults = new ArrayList<>();
    private final Set<Identifier> invisibleEntries = new HashSet<>();

    private Holder.@Nullable Reference<IResearchCategory> activeCategory;
    private double curMouseX;
    private double curMouseY;
    private double guiMapX;
    private double guiMapY;
    private double tempMapX;
    private double tempMapY;
    private int prevDragMouseX;
    private int prevDragMouseY;
    private int isMouseButtonDown;
    private float screenZoom = MIN_ZOOM;
    private int screenX;
    private int screenY;
    private int guiBoundsLeft = 99999;
    private int guiBoundsTop = 99999;
    private int guiBoundsRight = -99999;
    private int guiBoundsBottom = -99999;
    private int catScrollPos;
    private int catScrollMax;
    private int addonShift;
    private @Nullable EditBox searchField;
    private boolean searching;
    private @Nullable EntryNode currentHighlight;
    private int tickCount;

    public ThaumonomiconBrowserScreen() {
        super(Component.empty());
        this.curMouseX = this.guiMapX = this.tempMapX = persistedX;
        this.curMouseY = this.guiMapY = this.tempMapY = persistedY;
    }

    @Override
    protected void init() {
        super.init();
        if (minecraft == null || minecraft.player == null)
            return;
        loadRegistryData();
        searchField = new EditBox(font, SEARCH_BOX_X, SEARCH_BOX_Y, SEARCH_BOX_WIDTH, SEARCH_BOX_HEIGHT, Component.translatable("tc.search"));
        searchField.setBordered(true);
        searchField.setMaxLength(15);
        searchField.setTextColor(0xFFFFFFFF);
        searchField.setResponder(this::onSearchChanged);
        searchField.setVisible(false);
        addRenderableWidget(searchField);
        if (persistedCategoryId != null) {
            for (Holder.Reference<IResearchCategory> ref : categoriesTC) {
                ref.unwrapKey().ifPresent(key -> {
                    if (key.identifier().equals(persistedCategoryId))
                        activeCategory = ref;
                });
            }
            if (activeCategory == null) {
                for (Holder.Reference<IResearchCategory> ref : categoriesOther) {
                    ref.unwrapKey().ifPresent(key -> {
                        if (key.identifier().equals(persistedCategoryId))
                            activeCategory = ref;
                    });
                }
            }
        }
        if (activeCategory == null && !categoriesTC.isEmpty()) {
            activeCategory = categoriesTC.get(0);
        } else if (activeCategory == null && !categoriesOther.isEmpty()) {
            activeCategory = categoriesOther.get(0);
        }
        catScrollPos = persistedCatScrollPos;
        searching = persistedSearching;
        if (searching) {
            searchField.setVisible(true);
            searchField.setFocused(true);
            setFocused(searchField);
        }
        updateResearch();
        if (persistedX == UNSET_PERSIST || guiMapX > guiBoundsRight || guiMapX < guiBoundsLeft) {
            guiMapX = tempMapX = curMouseX = (double) (guiBoundsLeft + guiBoundsRight) / 2.0;
        }
        if (persistedY == UNSET_PERSIST || guiMapY > guiBoundsBottom || guiMapY < guiBoundsTop) {
            guiMapY = tempMapY = curMouseY = (double) (guiBoundsTop + guiBoundsBottom) / 2.0;
        }
    }

    @Override
    public void onClose() {
        persistState();
        super.onClose();
    }

    private void persistState() {
        persistedX = guiMapX;
        persistedY = guiMapY;
        persistedCatScrollPos = catScrollPos;
        persistedSearching = searching;
        if (activeCategory != null) {
            activeCategory.unwrapKey().ifPresent(key -> persistedCategoryId = key.identifier());
        }
    }

    private void loadRegistryData() {
        if (minecraft == null || minecraft.player == null)
            return;
        categoriesTC.clear();
        categoriesOther.clear();
        nodesByCategory.clear();
        allEntries.clear();
        minecraft.player.registryAccess().lookup(IResearchCategory.REGISTRY_KEY).ifPresent(lookup -> lookup.listElements().forEach(ref -> ref.unwrapKey().ifPresent(key -> {
            if (key.identifier().getNamespace().equals(TCIds.MODID)) {
                categoriesTC.add(ref);
            } else {
                categoriesOther.add(ref);
            }
        })));
        categoriesTC.sort(Comparator.comparing(ref -> ref.value().index()));
        minecraft.player.registryAccess().lookup(IResearchEntry.REGISTRY_KEY).ifPresent(lookup -> lookup.listElements().forEach(holder -> holder.unwrapKey().ifPresent(key -> {
            IResearchEntry entry = holder.value();
            @SuppressWarnings("unchecked")
            Holder.Reference<IResearchCategory> categoryRef = (Holder.Reference<IResearchCategory>) entry.category();
            EntryNode node = new EntryNode(key.identifier(), entry, holder, categoryRef);
            nodesByCategory.computeIfAbsent(categoryRef, k -> new ArrayList<>()).add(node);
            allEntries.add(node);
        })));
    }

    private void updateResearch() {
        if (minecraft == null || minecraft.player == null)
            return;
        screenX = width - 32;
        screenY = height - 32;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        categoriesOtherVisible.clear();
        addonShift = 0;
        int limit = (int) Math.floor((screenY - CATEGORY_AVAILABLE_HEIGHT_PADDING) / (double) CATEGORY_BUTTON_STRIDE_Y);
        int count = 0;
        for (Holder.Reference<IResearchCategory> ref : categoriesOther) {
            if (!isCategoryUnlocked(knowledge, ref))
                continue;
            count++;
            if (count <= limit + catScrollPos && count - 1 >= catScrollPos) {
                categoriesOtherVisible.add(ref);
            }
        }
        if (count > limit || count < catScrollPos) {
            addonShift = (screenY - CATEGORY_AVAILABLE_HEIGHT_PADDING) % CATEGORY_BUTTON_STRIDE_Y / 2;
        }
        catScrollMax = count - limit;
        if (catScrollPos > catScrollMax)
            catScrollPos = Math.max(0, catScrollMax);
        invisibleEntries.clear();
        guiBoundsLeft = 99999;
        guiBoundsTop = 99999;
        guiBoundsRight = -99999;
        guiBoundsBottom = -99999;
        if (activeCategory != null) {
            for (EntryNode node : nodesByCategory.getOrDefault(activeCategory, List.of())) {
                if (!isVisible(knowledge, node))
                    continue;
                int c = node.entry.column();
                int r = node.entry.row();
                if (c * CELL_SIZE - screenX + FRAME_INSET < guiBoundsLeft) {
                    guiBoundsLeft = c * CELL_SIZE - screenX + FRAME_INSET;
                }
                if (c * CELL_SIZE - CELL_SIZE > guiBoundsRight) {
                    guiBoundsRight = c * CELL_SIZE - CELL_SIZE;
                }
                if (r * CELL_SIZE - screenY + FRAME_INSET < guiBoundsTop) {
                    guiBoundsTop = r * CELL_SIZE - screenY + FRAME_INSET;
                }
                if (r * CELL_SIZE - CELL_SIZE > guiBoundsBottom) {
                    guiBoundsBottom = r * CELL_SIZE - CELL_SIZE;
                }
            }
        }
        if (guiBoundsLeft == 99999)
            guiBoundsLeft = 0;
        if (guiBoundsTop == 99999)
            guiBoundsTop = 0;
        if (guiBoundsRight == -99999)
            guiBoundsRight = 0;
        if (guiBoundsBottom == -99999)
            guiBoundsBottom = 0;
    }

    private boolean isCategoryUnlocked(IPlayerKnowledge knowledge, Holder.Reference<IResearchCategory> ref) {
        Optional<Identifier> gate = ref.value().requiredResearch();
        return gate.isEmpty() || knowledge.isResearchComplete(gate.get());
    }

    private boolean isVisible(IPlayerKnowledge knowledge, EntryNode node) {
        if (knowledge.isResearchKnown(node.id))
            return true;
        if (invisibleEntries.contains(node.id))
            return false;
        boolean hidden = node.entry.hasMeta(ResearchEntryMeta.HIDDEN);
        if (hidden && !canUnlockResearch(knowledge, node))
            return false;
        if (node.entry.parents().isEmpty() && hidden)
            return false;
        for (ResearchParent parent : node.entry.parents()) {
            EntryNode parentNode = findGlobalNode(parent.id());
            if (parentNode != null && !isVisible(knowledge, parentNode)) {
                invisibleEntries.add(parent.id());
                return false;
            }
        }
        return true;
    }

    private boolean canUnlockResearch(IPlayerKnowledge knowledge, EntryNode node) {
        for (ResearchParent parent : node.entry.parents()) {
            if (!parent.isSatisfiedBy(knowledge))
                return false;
        }
        return true;
    }

    private void onSearchChanged(String query) {
        searchResults.clear();
        invisibleEntries.clear();
        if (query == null)
            return;
        if (minecraft == null || minecraft.player == null)
            return;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        String needle = query.toLowerCase(Locale.ROOT);
        for (Holder.Reference<IResearchCategory> ref : categoriesTC) {
            if (!isCategoryUnlocked(knowledge, ref))
                continue;
            String name = categoryDisplayName(ref).getString();
            if (name.toLowerCase(Locale.ROOT).contains(needle)) {
                searchResults.add(SearchResult.category(name, ref));
            }
        }
        for (Holder.Reference<IResearchCategory> ref : categoriesOther) {
            if (!isCategoryUnlocked(knowledge, ref))
                continue;
            String name = categoryDisplayName(ref).getString();
            if (name.toLowerCase(Locale.ROOT).contains(needle)) {
                searchResults.add(SearchResult.category(name, ref));
            }
        }
        for (Identifier known : knowledge.researchList()) {
            EntryNode node = findGlobalNode(known);
            if (node == null)
                continue;
            String entryName = Component.translatable(node.entry.nameKey()).getString();
            if (entryName.toLowerCase(Locale.ROOT).contains(needle)) {
                searchResults.add(SearchResult.entry(entryName, node));
            }
            int stage = knowledge.researchStage(known);
            int sIdx = node.entry.stages().size() - 1 < stage + 2 ? node.entry.stages().size() - 1 : stage + 2;
            if (sIdx >= 0 && sIdx < node.entry.stages().size()) {
                IResearchStage st = node.entry.stages().get(sIdx);
                for (ResearchRequirement req : st.craft()) {
                    Optional<Holder<Item>> first = req.items().stream().findFirst();
                    if (first.isEmpty())
                        continue;
                    ItemStack stack = new ItemStack(first.get());
                    if (stack.isEmpty())
                        continue;
                    String dn = stack.getHoverName().getString();
                    if (dn.toLowerCase(Locale.ROOT).contains(needle)) {
                        searchResults.add(SearchResult.recipe(dn, node));
                    }
                }
            }
        }
        searchResults.sort(SearchResult::compareTo);
    }

    private Component categoryDisplayName(Holder.Reference<IResearchCategory> ref) {
        return ref.unwrapKey().<Component>map(key -> Component.translatable("research_category." + key.identifier().getNamespace() + "." + key.identifier().getPath())).orElse(Component.empty());
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (!searching) {
            if (scrollY < 0.0) {
                screenZoom += ZOOM_STEP;
            } else if (scrollY > 0.0) {
                screenZoom -= ZOOM_STEP;
            }
            screenZoom = Mth.clamp(screenZoom, MIN_ZOOM, MAX_ZOOM);
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        if (!searching) {
            updateDragging(mouseX, mouseY);
            clampTempMap();
        }
        int locX = Mth.floor(curMouseX + (guiMapX - curMouseX) * partialTick);
        int locY = Mth.floor(curMouseY + (guiMapY - curMouseY) * partialTick);
        locX = clampInt(locX, (int) (guiBoundsLeft * screenZoom), (int) (guiBoundsRight * screenZoom - 1.0F));
        locY = clampInt(locY, (int) (guiBoundsTop * screenZoom), (int) (guiBoundsBottom * screenZoom - 1.0F));
        currentHighlight = null;
        if (!searching && activeCategory != null) {
            graphics.pose().pushMatrix();
            graphics.pose().scale(1.0F / screenZoom, 1.0F / screenZoom);
            renderBackgroundLayers(graphics, locX, locY);
            renderConnectors(graphics, locX, locY);
            renderEntries(graphics, mouseX, mouseY, locX, locY);
            graphics.pose().popMatrix();
        } else if (searching) {
            renderSearchResults(graphics, mouseX, mouseY);
        }
        drawBrassFrame(graphics);
        renderCategoryButtons(graphics, mouseX, mouseY);
        renderSearchButton(graphics, mouseX, mouseY);
        renderScrollButtons(graphics, mouseX, mouseY);
        if (currentHighlight != null && minecraft != null && minecraft.player != null) {
            IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
            renderEntryTooltip(graphics, knowledge, currentHighlight, mouseX + TOOLTIP_OFFSET_X, mouseY + TOOLTIP_OFFSET_Y);
        }
    }

    private void updateDragging(int mx, int my) {
        if (!isMouseLeftButtonDown()) {
            isMouseButtonDown = 0;
            return;
        }
        if ((isMouseButtonDown == 0 || isMouseButtonDown == 1) && mx >= START_X && mx < START_X + screenX && my >= START_Y && my < START_Y + screenY) {
            if (isMouseButtonDown == 0) {
                isMouseButtonDown = 1;
            } else {
                guiMapX = guiMapX - (double) (mx - prevDragMouseX) * screenZoom;
                guiMapY = guiMapY - (double) (my - prevDragMouseY) * screenZoom;
                tempMapX = curMouseX = guiMapX;
                tempMapY = curMouseY = guiMapY;
            }
            prevDragMouseX = mx;
            prevDragMouseY = my;
        }
    }

    private boolean isMouseLeftButtonDown() {
        if (minecraft == null)
            return false;
        return GLFW.glfwGetMouseButton(minecraft.getWindow().handle(), 0) == 1;
    }

    private void clampTempMap() {
        if (tempMapX < guiBoundsLeft * screenZoom)
            tempMapX = guiBoundsLeft * screenZoom;
        if (tempMapY < guiBoundsTop * screenZoom)
            tempMapY = guiBoundsTop * screenZoom;
        if (tempMapX >= guiBoundsRight * screenZoom)
            tempMapX = guiBoundsRight * screenZoom - 1.0F;
        if (tempMapY >= guiBoundsBottom * screenZoom)
            tempMapY = guiBoundsBottom * screenZoom - 1.0F;
    }

    private int clampInt(int v, int min, int max) {
        if (min > max)
            return v;
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public void tick() {
        super.tick();
        tickCount++;
        curMouseX = guiMapX;
        curMouseY = guiMapY;
        double dx = tempMapX - guiMapX;
        double dy = tempMapY - guiMapY;
        if (dx * dx + dy * dy < PAN_SNAP_DISTANCE_SQUARED) {
            guiMapX += dx;
            guiMapY += dy;
        } else {
            guiMapX += dx * PAN_INERTIA_FACTOR;
            guiMapY += dy * PAN_INERTIA_FACTOR;
        }
    }

    private void renderBackgroundLayers(GuiGraphicsExtractor graphics, int locX, int locY) {
        if (activeCategory == null)
            return;
        IResearchCategory cat = activeCategory.value();
        int x = (int) ((START_X - 2) * screenZoom);
        int y = (int) ((START_Y - 2) * screenZoom);
        int w = (int) ((screenX + 4) * screenZoom);
        int h = (int) ((screenY + 4) * screenZoom);
        graphics.blit(RenderPipelines.GUI_TEXTURED, cat.background(), x, y, (float) (locX / 2.0), (float) (locY / 2.0), w, h, w, h, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        cat.overlayBackground().ifPresent(
                overlay -> graphics.blit(RenderPipelines.GUI_TEXTURED, overlay, x, y, (float) (locX / 1.5), (float) (locY / 1.5), w, h, w, h, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE));
    }

    private void renderConnectors(GuiGraphicsExtractor graphics, int locX, int locY) {
        if (minecraft == null || minecraft.player == null || activeCategory == null)
            return;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        List<EntryNode> nodes = nodesByCategory.getOrDefault(activeCategory, List.of());
        for (EntryNode source : nodes) {
            for (ResearchParent parent : source.entry.parents()) {
                EntryNode parentNode = findGlobalNode(parent.id());
                if (parentNode == null)
                    continue;
                if (!activeCategory.equals(parentNode.category))
                    continue;
                if (parentNode.entry.siblings().contains(source.id))
                    continue;
                boolean b = isVisible(knowledge, source) && !parent.inherit();
                if (!b)
                    continue;
                boolean knowsParent = knowledge.isResearchComplete(parent.id());
                int originX = START_X - locX;
                int originY = START_Y - locY;
                if (knowsParent) {
                    ConnectorRenderer.draw(graphics, source.entry.column(), source.entry.row(), parentNode.entry.column(), parentNode.entry.row(), originX, originY, CONNECTOR_PARENT_KNOWN,
                            CONNECTOR_Z_PARENT_KNOWN, true, source.entry.hasMeta(ResearchEntryMeta.REVERSE));
                } else if (isVisible(knowledge, parentNode)) {
                    ConnectorRenderer.draw(graphics, source.entry.column(), source.entry.row(), parentNode.entry.column(), parentNode.entry.row(), originX, originY, CONNECTOR_PARENT_UNKNOWN,
                            CONNECTOR_Z_PARENT_UNKNOWN, true, source.entry.hasMeta(ResearchEntryMeta.REVERSE));
                }
            }
            for (Identifier siblingRaw : source.entry.siblings()) {
                EntryNode siblingNode = findGlobalNode(siblingRaw);
                if (siblingNode == null)
                    continue;
                if (!activeCategory.equals(siblingNode.category))
                    continue;
                if (!isVisible(knowledge, source))
                    continue;
                boolean knowsSibling = knowledge.isResearchComplete(siblingRaw);
                int originX = START_X - locX;
                int originY = START_Y - locY;
                if (knowsSibling) {
                    ConnectorRenderer.draw(graphics, siblingNode.entry.column(), siblingNode.entry.row(), source.entry.column(), source.entry.row(), originX, originY, CONNECTOR_SIBLING_KNOWN,
                            CONNECTOR_Z_SIBLING_KNOWN, false, source.entry.hasMeta(ResearchEntryMeta.REVERSE));
                } else if (isVisible(knowledge, siblingNode)) {
                    ConnectorRenderer.draw(graphics, siblingNode.entry.column(), siblingNode.entry.row(), source.entry.column(), source.entry.row(), originX, originY, CONNECTOR_SIBLING_UNKNOWN,
                            CONNECTOR_Z_SIBLING_UNKNOWN, false, source.entry.hasMeta(ResearchEntryMeta.REVERSE));
                }
            }
        }
        ConnectorRenderer.flush(graphics);
    }

    private void renderEntries(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int locX, int locY) {
        if (minecraft == null || minecraft.player == null || activeCategory == null)
            return;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        List<EntryNode> nodes = nodesByCategory.getOrDefault(activeCategory, List.of());
        for (EntryNode node : nodes) {
            int relCol = node.entry.column() * CELL_SIZE - locX;
            int relRow = node.entry.row() * CELL_SIZE - locY;
            if (relCol < -ENTRY_CULL_BORDER || relRow < -ENTRY_CULL_BORDER || relCol > screenX * screenZoom || relRow > screenY * screenZoom)
                continue;
            if (!isVisible(knowledge, node))
                continue;
            int iconX = START_X + relCol;
            int iconY = START_Y + relRow;
            boolean canUnlock = canUnlockResearch(knowledge, node);
            EntryIconRenderer.Status iconStatus;
            if (knowledge.isResearchComplete(node.id)) {
                iconStatus = EntryIconRenderer.Status.COMPLETE;
            } else if (canUnlock) {
                iconStatus = EntryIconRenderer.Status.IN_PROGRESS;
            } else {
                iconStatus = EntryIconRenderer.Status.UNKNOWN;
            }
            boolean hasWarp = entryHasWarp(node.entry);
            boolean newResearch = knowledge.hasResearchFlag(node.id, ResearchFlag.RESEARCH);
            boolean newPage = knowledge.hasResearchFlag(node.id, ResearchFlag.PAGE);
            Object icon = resolveDisplayIcon(node);
            EntryIconRenderer.render(graphics, iconX, iconY, node.entry, iconStatus, icon, hasWarp, newResearch, newPage);
            if (mouseX >= START_X && mouseY >= START_Y && mouseX < START_X + screenX && mouseY < START_Y + screenY && mouseX >= (iconX - ENTRY_HIT_NEG_PADDING) / screenZoom
                    && mouseX <= (iconX + ENTRY_HIT_POS_PADDING) / screenZoom && mouseY >= (iconY - ENTRY_HIT_NEG_PADDING) / screenZoom && mouseY <= (iconY + ENTRY_HIT_POS_PADDING) / screenZoom) {
                currentHighlight = node;
            }
        }
    }

    private Object resolveDisplayIcon(EntryNode node) {
        return EntryIconRenderer.resolveIcon(node.entry, tickCount);
    }

    private boolean entryHasWarp(IResearchEntry entry) {
        for (IResearchStage stage : entry.stages()) {
            if (stage.warp() > 0)
                return true;
        }
        return false;
    }

    private void renderSearchResults(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (searchField != null)
            searchField.setVisible(true);
        int q = 0;
        for (SearchResult sr : searchResults) {
            int textY = SEARCH_RESULT_TEXT_Y_START + q * SEARCH_RESULT_ROW_HEIGHT;
            int textRightBound = SEARCH_RESULT_HIT_RIGHT_OFFSET + screenX;
            boolean hover = mouseX > SEARCH_RESULT_HIT_LEFT_X && mouseX < textRightBound && mouseY >= textY && mouseY < textY + 8;
            int color = sr.color(hover);
            graphics.pose().pushMatrix();
            graphics.pose().scale(SEARCH_RESULT_ICON_SCALE);
            int iconScreenX = (SEARCH_RESULT_ICON_X * 2);
            int iconScreenY = textY * 2;
            if (sr.recipeIcon()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, iconScreenX, iconScreenY, (float) SEARCH_RESULT_ICON_U, (float) SEARCH_RESULT_ICON_V,
                        SEARCH_RESULT_ICON_W, SEARCH_RESULT_ICON_W, SEARCH_RESULT_ICON_W, SEARCH_RESULT_ICON_W, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
            } else if (sr.entryNode != null) {
                Object icon = resolveDisplayIcon(sr.entryNode);
                EntryIconRenderer.drawResearchIcon(graphics, iconScreenX, iconScreenY, icon, false);
            } else if (sr.categoryRef != null && sr.categoryRef.isBound()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, sr.categoryRef.value().icon(), iconScreenX, iconScreenY, 0.0F, 0.0F, CATEGORY_ICON_SIZE, CATEGORY_ICON_SIZE, CATEGORY_ICON_SIZE,
                        CATEGORY_ICON_SIZE, CATEGORY_ICON_SIZE, CATEGORY_ICON_SIZE, CATEGORY_TAB_INACTIVE_ICON_TINT);
            }
            graphics.pose().popMatrix();
            graphics.text(font, sr.displayName(), SEARCH_RESULT_TEXT_X, textY, color, false);
            q++;
            if (SEARCH_RESULT_TEXT_Y_START + (q + 1) * SEARCH_RESULT_ROW_HEIGHT > screenY) {
                graphics.text(font, Component.translatable("tc.search.more"), SEARCH_RESULT_HIT_LEFT_X, SEARCH_RESULT_TEXT_Y_START + q * SEARCH_RESULT_ROW_HEIGHT + SEARCH_RESULT_OVERFLOW_Y_OFFSET,
                        SEARCH_OVERFLOW_COLOR, false);
                break;
            }
        }
    }

    private void drawBrassFrame(GuiGraphicsExtractor graphics) {
        for (int x = FRAME_MARGIN; x < width - FRAME_MARGIN; x += EDGE_TILE) {
            int len = Math.min(EDGE_TILE, width - FRAME_MARGIN - x);
            if (len <= 0)
                continue;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, x, BORDER_OFFSET, EDGE_UV, CORNER_UV, len, CORNER_SIZE, len, CORNER_SIZE, TCScreenTextures.TEX_SIZE,
                    TCScreenTextures.TEX_SIZE);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, x, height - FRAME_EDGE_FROM_BOTTOM, EDGE_UV, CORNER_UV, len, CORNER_SIZE, len, CORNER_SIZE,
                    TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        }
        for (int y = FRAME_MARGIN; y < height - FRAME_MARGIN; y += EDGE_TILE) {
            int len = Math.min(EDGE_TILE, height - FRAME_MARGIN - y);
            if (len <= 0)
                continue;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, BORDER_OFFSET, y, CORNER_UV, EDGE_UV, CORNER_SIZE, len, CORNER_SIZE, len, TCScreenTextures.TEX_SIZE,
                    TCScreenTextures.TEX_SIZE);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, width - FRAME_EDGE_FROM_RIGHT, y, CORNER_UV, EDGE_UV, CORNER_SIZE, len, CORNER_SIZE, len,
                    TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        }
        drawCorner(graphics, BORDER_OFFSET, BORDER_OFFSET);
        drawCorner(graphics, BORDER_OFFSET, height - FRAME_EDGE_FROM_BOTTOM);
        drawCorner(graphics, width - FRAME_EDGE_FROM_RIGHT, BORDER_OFFSET);
        drawCorner(graphics, width - FRAME_EDGE_FROM_RIGHT, height - FRAME_EDGE_FROM_BOTTOM);
    }

    private void drawCorner(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, x, y, CORNER_UV, CORNER_UV, CORNER_SIZE, CORNER_SIZE, CORNER_SIZE, CORNER_SIZE, TCScreenTextures.TEX_SIZE,
                TCScreenTextures.TEX_SIZE);
    }

    private void renderCategoryButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null)
            return;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        int tcYIndex = 0;
        for (Holder.Reference<IResearchCategory> ref : categoriesTC) {
            if (!isCategoryUnlocked(knowledge, ref))
                continue;
            int x = CATEGORY_BUTTON_X_LEFT;
            int y = CATEGORY_BUTTON_FIRST_Y_OFFSET + (tcYIndex + 1) * CATEGORY_BUTTON_STRIDE_Y;
            drawCategoryButton(graphics, knowledge, ref, x, y, mouseX, mouseY, false, 0);
            tcYIndex++;
        }
        for (int i = 0; i < categoriesOtherVisible.size(); i++) {
            Holder.Reference<IResearchCategory> ref = categoriesOtherVisible.get(i);
            int x = width - CATEGORY_BUTTON_X_RIGHT_MARGIN;
            int y = CATEGORY_BUTTON_FIRST_Y_OFFSET + (i + 1) * CATEGORY_BUTTON_STRIDE_Y;
            drawCategoryButton(graphics, knowledge, ref, x, y, mouseX, mouseY, true, addonShift);
        }
    }

    private void drawCategoryButton(GuiGraphicsExtractor graphics, IPlayerKnowledge knowledge, Holder.Reference<IResearchCategory> ref, int x, int y, int mouseX, int mouseY, boolean flip, int yShift) {
        boolean active = ref.equals(activeCategory);
        boolean hover = mouseX >= x && mouseY >= y + yShift && mouseX < x + CATEGORY_ICON_SIZE && mouseY < y + CATEGORY_ICON_SIZE + yShift;
        int frameTint = active ? CATEGORY_TAB_ACTIVE_TINT : CATEGORY_TAB_INACTIVE_TINT;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, x - CATEGORY_FRAME_OFFSET, y - CATEGORY_FRAME_OFFSET + yShift, CORNER_UV, CORNER_UV, CATEGORY_FRAME_SIZE,
                CATEGORY_FRAME_SIZE, CATEGORY_FRAME_SIZE, CATEGORY_FRAME_SIZE, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE, frameTint);
        int iconTint = (active || hover) ? CATEGORY_TAB_ACTIVE_ICON_TINT : CATEGORY_TAB_INACTIVE_ICON_TINT;
        graphics.blit(RenderPipelines.GUI_TEXTURED, ref.value().icon(), x, y + yShift, 0.0F, 0.0F, CATEGORY_ICON_SIZE, CATEGORY_ICON_SIZE, CATEGORY_ICON_SIZE, CATEGORY_ICON_SIZE, CATEGORY_ICON_SIZE,
                CATEGORY_ICON_SIZE, iconTint);
        boolean newResearch = false;
        boolean newPage = false;
        for (EntryNode node : nodesByCategory.getOrDefault(ref, List.of())) {
            if (!knowledge.isResearchKnown(node.id))
                continue;
            if (!newResearch && knowledge.hasResearchFlag(node.id, ResearchFlag.RESEARCH))
                newResearch = true;
            if (!newPage && knowledge.hasResearchFlag(node.id, ResearchFlag.PAGE))
                newPage = true;
            if (newResearch && newPage)
                break;
        }
        if (newResearch) {
            drawCategoryBadge(graphics, x + CATEGORY_BADGE_OFFSET_X, y + CATEGORY_BADGE_RESEARCH_OFFSET_Y + yShift, CATEGORY_BADGE_RESEARCH_U);
        }
        if (newPage) {
            drawCategoryBadge(graphics, x + CATEGORY_BADGE_OFFSET_X, y + CATEGORY_BADGE_PAGE_OFFSET_Y + yShift, CATEGORY_BADGE_PAGE_U);
        }
        if (hover) {
            final boolean hasNewResearch = newResearch;
            final boolean hasNewPage = newPage;
            int completion = categoryCompletionPercent(knowledge, ref);
            String name = categoryDisplayName(ref).getString();
            String full = name + " (" + completion + "%)";
            int labelX = !flip ? x + CATEGORY_LABEL_GAP_X : screenX + CATEGORY_LABEL_FROM_RIGHT_OFFSET - font.width(full);
            int labelY = y + CATEGORY_LABEL_Y_OFFSET + yShift;
            graphics.text(font, full, labelX, labelY, HOVER_LABEL_COLOR, false);
            int t = CATEGORY_LABEL_LINE_HEIGHT;
            if (hasNewResearch) {
                String s = Component.translatable("tc.research.newresearch").getString();
                int sx = !flip ? x + CATEGORY_LABEL_GAP_X : screenX + CATEGORY_LABEL_FROM_RIGHT_OFFSET - font.width(s);
                graphics.text(font, s, sx, labelY + t, HOVER_LABEL_COLOR, false);
                t += CATEGORY_LABEL_LINE_HEIGHT;
            }
            if (hasNewPage) {
                String s = Component.translatable("tc.research.newpage").getString();
                int sx = !flip ? x + CATEGORY_LABEL_GAP_X : screenX + CATEGORY_LABEL_FROM_RIGHT_OFFSET - font.width(s);
                graphics.text(font, s, sx, labelY + t, HOVER_LABEL_COLOR, false);
            }
        }
    }

    private void drawCategoryBadge(GuiGraphicsExtractor graphics, int x, int y, int u) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(CATEGORY_BADGE_SCALE, CATEGORY_BADGE_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, 0, 0, (float) u, (float) CATEGORY_BADGE_V, CATEGORY_BADGE_SIZE, CATEGORY_BADGE_SIZE, CATEGORY_BADGE_SIZE,
                CATEGORY_BADGE_SIZE, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE, CATEGORY_BADGE_TINT);
        graphics.pose().popMatrix();
    }

    private int categoryCompletionPercent(IPlayerKnowledge knowledge, Holder.Reference<IResearchCategory> ref) {
        List<EntryNode> nodes = nodesByCategory.getOrDefault(ref, List.of());
        int rt = 0;
        int rco = 0;
        for (EntryNode node : nodes) {
            if (node.entry.hasMeta(ResearchEntryMeta.AUTOUNLOCK))
                continue;
            rt++;
            if (knowledge.isResearchKnown(node.id))
                rco++;
        }
        if (rt == 0)
            return 0;
        return (int) ((float) rco / rt * 100.0F);
    }

    private void renderSearchButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = SEARCH_BUTTON_X;
        int y = height - SEARCH_BUTTON_Y_OFFSET_FROM_BOTTOM;
        boolean hover = mouseX >= x && mouseX < x + SEARCH_BUTTON_SIZE && mouseY >= y && mouseY < y + SEARCH_BUTTON_SIZE;
        int tint = hover ? SEARCH_BUTTON_TINT_HOVER : SEARCH_BUTTON_TINT_IDLE;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, x, y, (float) SEARCH_BUTTON_U, (float) SEARCH_BUTTON_V, SEARCH_BUTTON_SIZE, SEARCH_BUTTON_SIZE,
                SEARCH_BUTTON_SIZE, SEARCH_BUTTON_SIZE, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE, tint);
        if (hover) {
            graphics.text(font, Component.translatable("tc.search").getString(), x + SEARCH_BUTTON_LABEL_X_OFFSET, y + SEARCH_BUTTON_LABEL_Y_OFFSET, HOVER_LABEL_COLOR, false);
        }
    }

    private void renderScrollButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (catScrollMax <= 0 && catScrollPos == 0)
            return;
        int upX = width - SCROLL_BUTTON_X_FROM_RIGHT;
        int upY = SCROLL_BUTTON_UP_Y;
        int downX = upX;
        int downY = screenY + 1;
        boolean upHover = mouseX >= upX && mouseX < upX + SCROLL_BUTTON_WIDTH && mouseY >= upY && mouseY < upY + SCROLL_BUTTON_HEIGHT;
        boolean downHover = mouseX >= downX && mouseX < downX + SCROLL_BUTTON_WIDTH && mouseY >= downY && mouseY < downY + SCROLL_BUTTON_HEIGHT;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, upX, upY, (float) SCROLL_BUTTON_UV_U, (float) SCROLL_BUTTON_UV_UP_V, SCROLL_BUTTON_WIDTH, SCROLL_BUTTON_HEIGHT,
                SCROLL_BUTTON_WIDTH, SCROLL_BUTTON_HEIGHT, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE, upHover ? SCROLL_BUTTON_TINT_HOVER : SCROLL_BUTTON_TINT_IDLE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BROWSER, downX, downY, (float) SCROLL_BUTTON_UV_U, (float) SCROLL_BUTTON_UV_DOWN_V, SCROLL_BUTTON_WIDTH,
                SCROLL_BUTTON_HEIGHT, SCROLL_BUTTON_WIDTH, SCROLL_BUTTON_HEIGHT, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE, downHover ? SCROLL_BUTTON_TINT_HOVER : SCROLL_BUTTON_TINT_IDLE);
    }

    private void renderEntryTooltip(GuiGraphicsExtractor graphics, IPlayerKnowledge knowledge, EntryNode node, int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        Component name = Component.translatable(node.entry.nameKey()).withStyle(ChatFormatting.GOLD);
        lines.add(name);
        boolean canUnlock = canUnlockResearch(knowledge, node);
        if (canUnlock) {
            if (!knowledge.isResearchComplete(node.id) && !node.entry.stages().isEmpty()) {
                int stage = knowledge.researchStage(node.id);
                if (stage >= 0) {
                    MutableComponent stageLine = Component.literal("@@").append(Component
                            .literal(ChatFormatting.AQUA + Component.translatable("tc.research.stage").getString() + " " + (stage + 1) + "/" + node.entry.stages().size() + ChatFormatting.RESET));
                    lines.add(stageLine);
                } else {
                    MutableComponent begin = Component.literal("@@").append(Component.literal(ChatFormatting.GREEN + Component.translatable("tc.research.begin").getString() + ChatFormatting.RESET));
                    lines.add(begin);
                }
            }
        } else {
            lines.add(Component.literal("@@" + ChatFormatting.RED + Component.translatable("tc.researchmissing").getString()));
            for (ResearchParent parent : node.entry.parents()) {
                if (parent.isSatisfiedBy(knowledge))
                    continue;
                String s = "?";
                EntryNode parentNode = findGlobalNode(parent.id());
                if (parentNode != null) {
                    s = Component.translatable(parentNode.entry.nameKey()).getString();
                }
                lines.add(Component.literal("@@" + ChatFormatting.YELLOW + " - " + s));
            }
        }
        if (knowledge.hasResearchFlag(node.id, ResearchFlag.RESEARCH)) {
            lines.add(Component.literal("@@").append(Component.translatable("tc.research.newresearch")));
        }
        if (knowledge.hasResearchFlag(node.id, ResearchFlag.PAGE)) {
            lines.add(Component.literal("@@").append(Component.translatable("tc.research.newpage")));
        }
        if (minecraft.options.advancedItemTooltips) {
            lines.add(Component.literal(ChatFormatting.DARK_GRAY + node.id().toString()));
        }
        TCTooltipRenderer.render(graphics, font, lines, mouseX, mouseY);
    }

    private @Nullable EntryNode findGlobalNode(Identifier id) {
        for (EntryNode node : allEntries) {
            if (node.id.equals(id))
                return node;
        }
        return null;
    }

    private static final float BUTTON_CLACK_VOLUME = 0.4F;
    private static final float PAGE_OPEN_VOLUME = 0.66F;

    private void playButtonClack() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(TCSounds.CLACK.get(), BUTTON_CLACK_VOLUME, 1.0F);
        }
    }

    private void playPageOpen() {
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(TCSounds.PAGE.get(), PAGE_OPEN_VOLUME, 1.0F);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0)
            return super.mouseClicked(event, doubleClick);
        int mx = (int) event.x();
        int my = (int) event.y();
        if (minecraft == null || minecraft.player == null)
            return super.mouseClicked(event, doubleClick);
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        if (hitSearchButton(mx, my)) {
            playButtonClack();
            toggleSearch();
            return true;
        }
        if (hitScrollUp(mx, my)) {
            if (catScrollPos > 0) {
                playButtonClack();
                catScrollPos--;
                updateResearch();
            }
            return true;
        }
        if (hitScrollDown(mx, my)) {
            if (catScrollPos < catScrollMax) {
                playButtonClack();
                catScrollPos++;
                updateResearch();
            }
            return true;
        }
        Holder.Reference<IResearchCategory> tabHit = hitCategoryButton(mx, my);
        if (tabHit != null && !tabHit.equals(activeCategory)) {
            playButtonClack();
            searching = false;
            if (searchField != null) {
                searchField.setVisible(false);
                searchField.setFocused(false);
            }
            activeCategory = tabHit;
            updateResearch();
            guiMapX = tempMapX = (double) (guiBoundsLeft + guiBoundsRight) / 2.0;
            guiMapY = tempMapY = (double) (guiBoundsTop + guiBoundsBottom) / 2.0;
            return true;
        }
        if (!searching && currentHighlight != null && !knowledge.isResearchKnown(currentHighlight.id) && canUnlockResearch(knowledge, currentHighlight)) {
            EntryNode hl = currentHighlight;
            updateResearch();
            ClientPacketDistributor.sendToServer(new ServerboundUnlockResearchPayload(hl.id));
            persistState();
            playPageOpen();
            minecraft.setScreen(new EntryDetailScreen(hl.holder, hl.id, this));
            return true;
        } else if (currentHighlight != null && knowledge.isResearchKnown(currentHighlight.id)) {
            EntryNode hl = currentHighlight;
            knowledge.clearResearchFlag(hl.id, ResearchFlag.RESEARCH);
            knowledge.clearResearchFlag(hl.id, ResearchFlag.PAGE);
            ClientPacketDistributor.sendToServer(new ServerboundClearResearchFlagsPayload(hl.id, List.of(ResearchFlag.RESEARCH, ResearchFlag.PAGE)));
            int stage = knowledge.researchStage(hl.id);
            if (stage > 0 && stage >= hl.entry.stages().size() - 1) {
                ClientPacketDistributor.sendToServer(new ServerboundUnlockResearchPayload(hl.id));
            }
            persistState();
            playPageOpen();
            minecraft.setScreen(new EntryDetailScreen(hl.holder, hl.id, this));
            return true;
        } else if (searching) {
            SearchResult sr = hitSearchResult(mx, my);
            if (sr != null) {
                if (sr.kind == SearchResult.Kind.CATEGORY) {
                    searching = false;
                    if (searchField != null) {
                        searchField.setVisible(false);
                        searchField.setFocused(false);
                    }
                    activeCategory = sr.categoryRef;
                    updateResearch();
                    guiMapX = tempMapX = (double) (guiBoundsLeft + guiBoundsRight) / 2.0;
                    guiMapY = tempMapY = (double) (guiBoundsTop + guiBoundsBottom) / 2.0;
                    return true;
                }
                if ((sr.kind == SearchResult.Kind.ENTRY || sr.kind == SearchResult.Kind.RECIPE) && sr.entryNode != null) {
                    persistState();
                    playPageOpen();
                    minecraft.setScreen(new EntryDetailScreen(sr.entryNode.holder, sr.entryNode.id, this));
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void toggleSearch() {
        searching = !searching;
        if (searchField != null) {
            searchField.setVisible(searching);
            searchField.setValue("");
            if (searching) {
                searchField.setFocused(true);
                setFocused(searchField);
            } else {
                searchField.setFocused(false);
            }
        }
        searchResults.clear();
    }

    private boolean hitSearchButton(int mx, int my) {
        int x = SEARCH_BUTTON_X;
        int y = height - SEARCH_BUTTON_Y_OFFSET_FROM_BOTTOM;
        return mx >= x && mx < x + SEARCH_BUTTON_SIZE && my >= y && my < y + SEARCH_BUTTON_SIZE;
    }

    private boolean hitScrollUp(int mx, int my) {
        if (catScrollMax <= 0 && catScrollPos == 0)
            return false;
        int x = width - SCROLL_BUTTON_X_FROM_RIGHT;
        int y = SCROLL_BUTTON_UP_Y;
        return mx >= x && mx < x + SCROLL_BUTTON_WIDTH && my >= y && my < y + SCROLL_BUTTON_HEIGHT;
    }

    private boolean hitScrollDown(int mx, int my) {
        if (catScrollMax <= 0 && catScrollPos == 0)
            return false;
        int x = width - SCROLL_BUTTON_X_FROM_RIGHT;
        int y = screenY + 1;
        return mx >= x && mx < x + SCROLL_BUTTON_WIDTH && my >= y && my < y + SCROLL_BUTTON_HEIGHT;
    }

    private Holder.@Nullable Reference<IResearchCategory> hitCategoryButton(int mx, int my) {
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        if (knowledge == null)
            return null;
        int tcYIndex = 0;
        for (Holder.Reference<IResearchCategory> ref : categoriesTC) {
            if (!isCategoryUnlocked(knowledge, ref))
                continue;
            int x = CATEGORY_BUTTON_X_LEFT;
            int y = CATEGORY_BUTTON_FIRST_Y_OFFSET + (tcYIndex + 1) * CATEGORY_BUTTON_STRIDE_Y;
            if (mx >= x && mx < x + CATEGORY_ICON_SIZE && my >= y && my < y + CATEGORY_ICON_SIZE) {
                return ref;
            }
            tcYIndex++;
        }
        for (int i = 0; i < categoriesOtherVisible.size(); i++) {
            int x = width - CATEGORY_BUTTON_X_RIGHT_MARGIN;
            int y = CATEGORY_BUTTON_FIRST_Y_OFFSET + (i + 1) * CATEGORY_BUTTON_STRIDE_Y;
            if (mx >= x && mx < x + CATEGORY_ICON_SIZE && my >= y + addonShift && my < y + CATEGORY_ICON_SIZE + addonShift) {
                return categoriesOtherVisible.get(i);
            }
        }
        return null;
    }

    private @Nullable SearchResult hitSearchResult(int mx, int my) {
        int q = 0;
        for (SearchResult sr : searchResults) {
            int textY = SEARCH_RESULT_TEXT_Y_START + q * SEARCH_RESULT_ROW_HEIGHT;
            int textRightBound = SEARCH_RESULT_HIT_RIGHT_OFFSET + screenX;
            if (mx > SEARCH_RESULT_HIT_LEFT_X && mx < textRightBound && my >= textY && my < textY + 8) {
                return sr;
            }
            q++;
            if (SEARCH_RESULT_TEXT_Y_START + (q + 1) * SEARCH_RESULT_ROW_HEIGHT > screenY)
                break;
        }
        return null;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searching && searchField != null && searchField.keyPressed(event)) {
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_ESCAPE && searching) {
            toggleSearch();
            return true;
        }
        if (minecraft != null && minecraft.options.keyInventory.matches(event) && (searchField == null || !searchField.isFocused())) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0)
            isMouseButtonDown = 0;
        return super.mouseReleased(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record EntryNode(Identifier id, IResearchEntry entry, Holder<IResearchEntry> holder, Holder.Reference<IResearchCategory> category) {
    }

    private static final class SearchResult implements Comparable<SearchResult> {
        enum Kind {
            CATEGORY, ENTRY, RECIPE
        }

        private final String displayName;
        private final Kind kind;
        private final @Nullable EntryNode entryNode;
        private final Holder.@Nullable Reference<IResearchCategory> categoryRef;

        private SearchResult(String displayName, Kind kind, @Nullable EntryNode entryNode, Holder.@Nullable Reference<IResearchCategory> categoryRef) {
            this.displayName = displayName;
            this.kind = kind;
            this.entryNode = entryNode;
            this.categoryRef = categoryRef;
        }

        static SearchResult category(String name, Holder.Reference<IResearchCategory> ref) {
            return new SearchResult(name, Kind.CATEGORY, null, ref);
        }

        static SearchResult entry(String name, EntryNode node) {
            return new SearchResult(name, Kind.ENTRY, node, null);
        }

        static SearchResult recipe(String name, EntryNode node) {
            return new SearchResult(name, Kind.RECIPE, node, null);
        }

        String displayName() {
            return displayName;
        }

        boolean recipeIcon() {
            return kind == Kind.RECIPE;
        }

        int color(boolean hover) {
            if (hover) {
                return switch (kind) {
                    case CATEGORY -> SEARCH_RESULT_COLOR_CATEGORY_HOVER;
                    case RECIPE -> SEARCH_RESULT_COLOR_RECIPE_HOVER;
                    case ENTRY -> SEARCH_RESULT_COLOR_ENTRY_HOVER;
                };
            }
            return switch (kind) {
                case CATEGORY -> SEARCH_RESULT_COLOR_CATEGORY;
                case RECIPE -> SEARCH_RESULT_COLOR_RECIPE;
                case ENTRY -> SEARCH_RESULT_COLOR_ENTRY;
            };
        }

        @Override
        public int compareTo(SearchResult other) {
            return this.displayName.compareTo(other.displayName);
        }
    }
}
