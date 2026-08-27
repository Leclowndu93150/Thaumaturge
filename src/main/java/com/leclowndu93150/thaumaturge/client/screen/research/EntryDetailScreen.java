package com.leclowndu93150.thaumaturge.client.screen.research;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.KnowledgeReward;
import com.leclowndu93150.thaumaturge.api.research.ResearchAddendum;
import com.leclowndu93150.thaumaturge.api.research.ResearchConstruct;
import com.leclowndu93150.thaumaturge.api.research.ResearchIcon;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.client.render.research.PageParser;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayCache;
import com.leclowndu93150.thaumaturge.client.render.research.RecipeDisplayWidget;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTCScreen;
import com.leclowndu93150.thaumaturge.client.screen.TCScreenTextures;
import com.leclowndu93150.thaumaturge.client.screen.TCTooltips;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.network.ServerboundAdvanceStagePayload;
import com.leclowndu93150.thaumaturge.network.ServerboundClearResearchFlagsPayload;
import com.leclowndu93150.thaumaturge.network.ServerboundObtainNotePayload;
import com.leclowndu93150.thaumaturge.network.ServerboundRequestItemRecipePayload;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class EntryDetailScreen extends AbstractTCScreen {
    private static final int PANE_W = 256;
    private static final int PANE_H = 181;
    private static final float PANE_SCALE = 1.3F;

    private static final int PAGE_WIDTH = 140;
    private static final int PAGE_LEFT_OFFSET = -15;
    private static final int PAGE_SIDE_OFFSET = 152;
    private static final int CONTENT_Y_OFFSET = -10;
    private static final int TITLE_Y_ADVANCE = 28;

    private static final int LINE_HEIGHT = 9;
    private static final int TEXT_LINE_COLOR = 0xFF000000;
    private static final int TITLE_COLOR = 0xFF202020;

    private static final int DIVIDER_U = 24;
    private static final int DIVIDER_V = 184;
    private static final int DIVIDER_WIDTH = 96;
    private static final int DIVIDER_THICK = 4;

    private static final int REQUIREMENT_LABEL_U = 200;
    private static final int LABEL_RESEARCH_V = 232;
    private static final int LABEL_OBTAIN_V = 216;
    private static final int LABEL_CRAFT_V = 200;
    private static final int LABEL_KNOW_V = 184;
    private static final int LABEL_WIDTH = 56;
    private static final int LABEL_HEIGHT = 16;
    private static final int LABEL_OFFSET_X = -12;

    private static final int SLOT_BASE_SHIFT = 24;
    private static final int SLOT_DEFAULT_SPACING = 18;
    private static final int SLOT_BUDGET = 110;
    private static final int SLOT_INNER_OFFSET_X = -15;
    private static final int SLOT_HIT_SIZE = 16;
    private static final int CHECKMARK_U = 159;
    private static final int CHECKMARK_V = 207;
    private static final int CHECKMARK_SIZE = 10;
    private static final int CHECKMARK_OFFSET_X = 8;

    private static final int REQ_TOP_Y_OFFSET = 210 - 16;
    private static final int REQ_ROW_STEP = 18;

    private static final int COMPLETE_BUTTON_OFFSET_X = 20;
    private static final int COMPLETE_BUTTON_Y_OFFSET = -6;
    private static final int COMPLETE_BUTTON_W = 64;
    private static final int COMPLETE_BUTTON_H = 12;
    private static final int COMPLETE_BUTTON_U = 84;
    private static final int COMPLETE_BUTTON_V = 216;
    private static final int COMPLETE_BUTTON_TINT_NORMAL = 0xFFFFFFFF;
    private static final int COMPLETE_BUTTON_TINT_HOVER = 0xFFCCCCE6;
    private static final int COMPLETE_DIVIDER_U = 24;
    private static final int COMPLETE_DIVIDER_V = 184;
    private static final int COMPLETE_DIVIDER_W = 96;
    private static final int COMPLETE_DIVIDER_H = 8;
    private static final int COMPLETE_LABEL_COLOR = 0xFFFFFFFF;
    private static final int COMPLETE_LABEL_Y_OFFSET = 2;

    private static final int ARROW_W = 12;
    private static final int ARROW_H = 8;
    private static final int ARROW_Y_OFFSET = 190;
    private static final int ARROW_LEFT_OFFSET_X = -16;
    private static final int ARROW_LEFT_HIT_OFFSET_X = -17;
    private static final int ARROW_LEFT_HIT_Y_OFFSET = 189;
    private static final int ARROW_LEFT_HIT_W = 14;
    private static final int ARROW_LEFT_HIT_H = 10;
    private static final int ARROW_LEFT_U = 0;
    private static final int ARROW_RIGHT_OFFSET_X = 262;
    private static final int ARROW_RIGHT_HIT_OFFSET_X = 261;
    private static final int ARROW_RIGHT_U = 12;
    private static final int ARROW_V = 184;
    private static final int BACK_U = 38;
    private static final int BACK_V = 202;
    private static final int BACK_W = 20;
    private static final int BACK_H = 12;
    private static final int BACK_OFFSET_X = 118;

    private static final int RECIPE_NAV_LEFT_OFFSET_X = 40;
    private static final int RECIPE_NAV_RIGHT_OFFSET_X = 204;
    private static final int RECIPE_NAV_Y_OFFSET = 232;

    private static final int BOOKMARK_OFFSET_X = -48;
    private static final int BOOKMARK_W = 25;
    private static final int BOOKMARK_H = 16;
    private static final int BOOKMARK_ASPECT_U = 76;
    private static final int BOOKMARK_KNOWLEDGE_U = 44;
    private static final int BOOKMARK_V = 232;
    private static final int BOOKMARK_TIP_U = 100;
    private static final int BOOKMARK_TIP_W = 4;
    private static final int BOOKMARK_ASPECT_RENDER_Y = 9;
    private static final int BOOKMARK_ASPECT_CLICK_Y = 8;
    private static final int BOOKMARK_KNOWLEDGE_RENDER_Y = 32;
    private static final int BOOKMARK_KNOWLEDGE_CLICK_Y = 31;

    private static final int RECIPE_BOOKMARK_OFFSET_X = 280;
    private static final int RECIPE_BOOKMARK_BASE_Y_OFFSET = -8;
    private static final int RECIPE_BOOKMARK_MAX_STEP = 25;
    private static final int RECIPE_BOOKMARK_TOTAL_BUDGET = 200;
    private static final int RECIPE_BOOKMARK_U_BASE = 120;
    private static final int RECIPE_BOOKMARK_V = 232;
    private static final int RECIPE_BOOKMARK_W = 28;
    private static final int RECIPE_BOOKMARK_H = 16;
    private static final int RECIPE_BOOKMARK_HOVER_W = 30;
    private static final int RECIPE_BOOKMARK_TIP_U = 116;
    private static final int RECIPE_BOOKMARK_TIP_W = 4;
    private static final int RECIPE_BOOKMARK_ICON_OFFSET = 7;
    private static final int RECIPE_BOOKMARK_TINT_SELECTED = 0xFFFF8080;
    private static final int RECIPE_BOOKMARK_TINT_NORMAL = 0xFFFFFFFF;

    private static final int LABEL_TINT = 0x40FFFFFF;

    private static final Identifier FIRSTSTEPS_RESEARCH = Identifier.fromNamespaceAndPath(TCIds.MODID, "first_steps");
    private static final Identifier KNOWLEDGETYPES_RESEARCH = Identifier.fromNamespaceAndPath(TCIds.MODID, "knowledge_types");

    private static final int ASPECTS_INSERT_OFFSET_X = 60;
    private static final int ASPECTS_INSERT_OFFSET_Y = 24;
    private static final int ASPECT_PAGE_ROWS = 5;
    private static final int ASPECT_PROGRESS_X = 84;
    private static final int ASPECT_PROGRESS_Y = 152;
    private static final float ASPECT_COMBINE_YIELD = 1.0F;
    private static final Identifier UNKNOWN_ASPECT_TEXTURE = TCIds.rl("textures/aspects/_unknown.png");
    private static final int UNKNOWN_ASPECT_TINT = 0x80808080;
    private static final int ASPECT_ROW_STRIDE = 40;
    private static final int ASPECT_BACK_OFFSET_X = -2;
    private static final int ASPECT_BACK_OFFSET_Y = -2;
    private static final float ASPECT_BACK_SCALE = 2.0F;
    private static final float ASPECT_BACK_ALPHA = 0.5F;
    private static final int ASPECT_TAG_OFFSET_X = 2;
    private static final int ASPECT_TAG_OFFSET_Y = 2;
    private static final float ASPECT_TAG_SCALE = 1.5F;
    private static final int ASPECT_NAME_OFFSET_X = 16;
    private static final int ASPECT_NAME_OFFSET_Y = 29;
    private static final float ASPECT_NAME_SCALE = 0.5F;
    private static final int ASPECT_NAME_COLOR = 0xFF505050;
    private static final int ASPECT_COMPONENT_LEFT_X = 60;
    private static final int ASPECT_COMPONENT_RIGHT_X = 102;
    private static final int ASPECT_COMPONENT_Y_OFFSET = 4;
    private static final float ASPECT_COMPONENT_SCALE = 1.25F;
    private static final int ASPECT_COMPONENT_LEFT_NAME_X = 22 + 50;
    private static final int ASPECT_COMPONENT_RIGHT_NAME_X = 22 + 92;
    private static final int ASPECT_EQUALS_X = 9 + 32;
    private static final int ASPECT_PLUS_X = 10 + 79;
    private static final int ASPECT_SEPARATOR_Y_OFFSET = 12;
    private static final int ASPECT_SEPARATOR_COLOR = 0xFF999999;
    private static final int ASPECT_PRIMAL_X = 54;
    private static final int ASPECT_PRIMAL_COLOR = 0xFF777777;
    private static final int ASPECT_NAV_LEFT_X_OFFSET = -20;
    private static final int ASPECT_NAV_RIGHT_X_OFFSET = 144;
    private static final int ASPECT_NAV_Y_OFFSET = 208;
    private static final int ASPECT_BACK_TILE_SIZE = 32;

    private static final int FORBIDDEN_OFFSET_X = -57;
    private static final int FORBIDDEN_LABEL_OFFSET_X = -56;
    private static final int FORBIDDEN_LABEL_Y_OFFSET = -43;
    private static final int FORBIDDEN_Y_OFFSET = -40;
    private static final int FORBIDDEN_HOVER_OFFSET_X = -67;
    private static final int FORBIDDEN_HOVER_OFFSET_Y = -50;
    private static final int FORBIDDEN_HOVER_W = 20;
    private static final int FORBIDDEN_HOVER_H = 20;
    private static final int FORBIDDEN_COLOR = 0xFFAA8FFF;
    private static final int FORBIDDEN_NODE_CELL_PX = 64;
    private static final int FORBIDDEN_NODE_FRAME_COUNT = 32;
    private static final int FORBIDDEN_NODE_ROW = 5;
    private static final int FORBIDDEN_NODE_DRAW_SIZE = 90;
    private static final int FORBIDDEN_NODE_SHEET = 2048;
    private static final int FORBIDDEN_NODE_TINT = 0xE5540070;

    private static final int KNOW_ICON_TEX = 256;
    private static final float KNOW_ICON_SCALE_INPAGE = 0.0625F;
    private static final int KNOW_GRID_X_BASE_OFFSET = -10;
    private static final int KNOW_GRID_BAR_FILLED_V = 232;
    private static final int KNOW_GRID_BAR_EMPTY_V = 234;
    private static final int KNOW_GRID_BAR_BAR_HEIGHT = 2;
    private static final int KNOW_GRID_BAR_BAR_WIDTH = 16;
    private static final int KNOW_GRID_BAR_Y_OFFSET = 17;
    private static final int KNOW_GRID_INPAGE_COL_STRIDE = 18;
    private static final int KNOW_GRID_INPAGE_ROW_STRIDE = 20;
    private static final int KNOW_GRID_INSERT_COL_BASE = 164;
    private static final int KNOW_GRID_INSERT_ROW_STRIDE = 28;
    private static final int KNOW_GRID_AMT_X_OFFSET = 16;
    private static final int KNOW_GRID_AMT_Y_OFFSET = 8;
    private static final int KNOW_GRID_AMT_COLOR = 0xFFFFFFFF;
    private static final int KNOW_GRID_CATEGORY_OVERLAY_OFFSET = 66;
    private static final float KNOW_GRID_CATEGORY_OVERLAY_SCALE = 0.66F;
    private static final int KNOW_GRID_CATEGORY_OVERLAY_TINT = 0xBFFFFFFF;
    private static final int KNOW_INPAGE_INSERT_Y_OFFSET = 75;
    private static final int KNOW_INPAGE_INSERT_INPAGE_Y_OFFSET = 210;

    private static final int INSERT_PAPER_SIZE = 255;

    private static final int CONSTRUCT_PAGE_Y = 26;
    private static final int CONSTRUCT_TITLE_COLOR = 0xFF505050;
    private static final int CONSTRUCT_STRUCT_Y = 108;
    private static final int CONSTRUCT_LAYER_STRIDE = 50;
    private static final int CONSTRUCT_LAYER_HALF_STRIDE = 25;
    private static final float CONSTRUCT_SHRINK_PER_LAYER = 0.2F;
    private static final int CONSTRUCT_BACKDROP_Y_BASE = -119;
    private static final float CONSTRUCT_BACKDROP_U = 0.0F;
    private static final float CONSTRUCT_BACKDROP_V = 144.0F;
    private static final int CONSTRUCT_BACKDROP_W = 128;
    private static final int CONSTRUCT_BACKDROP_H = 88;
    private static final int CONSTRUCT_BACKDROP_DRAW_W = 64;
    private static final int CONSTRUCT_BACKDROP_DRAW_H = 44;
    private static final float CONSTRUCT_BACKDROP_ALPHA = 0.5F;
    private static final float CONSTRUCT_WAND_U = 136.0F;
    private static final float CONSTRUCT_WAND_V = 152.0F;
    private static final int CONSTRUCT_WAND_SIZE = 24;
    private static final int CONSTRUCT_WAND_Y = 174;
    private static final float CONSTRUCT_WAND_ALPHA = 0.4F;
    private static final int CONSTRUCT_COST_Y = 182;
    private static final int CONSTRUCT_COST_STRIDE = 18;
    private static final int CONSTRUCT_WAND_GAP = 2;
    private static final int OVERLAY_TEX_SIZE = 512;

    private final Holder<IResearchEntry> entry;
    private final Identifier entryId;
    private final @Nullable Screen parent;
    private int currentPage;
    private int sw;
    private int sh;
    private List<PageParser.Page> parsedPages = List.of();
    private boolean showingAspects;
    private boolean showingKnowledge;
    private @Nullable Identifier shownRecipe;
    private boolean showingConstruct;
    private int recipePage;
    private int aspectsPage;
    private boolean flagsCleared;
    private boolean hold;
    private int lastStage;
    private long holdSince;
    private static final long HOLD_TIMEOUT_TICKS = 60;
    private int rhash;
    private boolean isComplete;
    private int renderedStage = -1;
    private boolean renderedComplete;
    private int renderedAddenda = -1;
    private final Deque<Identifier> history = new ArrayDeque<>();

    public EntryDetailScreen(Holder<IResearchEntry> entry, Identifier entryId, @Nullable Screen parent) {
        super(Component.translatable(entry.value().nameKey()));
        this.entry = entry;
        this.entryId = entryId;
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        sw = (width - PANE_W) / 2;
        sh = (height - PANE_H) / 2;
        currentPage = clampPage(currentPage);
        for (IResearchStage stage : entry.value().stages()) {
            for (Identifier recipeId : stage.recipes()) {
                RecipeDisplayCache.ensureRequested(recipeId);
            }
        }
        for (ResearchAddendum addendum : entry.value().addenda()) {
            for (Identifier recipeId : addendum.recipes()) {
                RecipeDisplayCache.ensureRequested(recipeId);
            }
        }
        rebuildPages();
        if (!flagsCleared) {
            ClientPacketDistributor.sendToServer(new ServerboundClearResearchFlagsPayload(entryId, List.of(ResearchFlag.RESEARCH, ResearchFlag.PAGE)));
            flagsCleared = true;
        }
    }

    private void rebuildPages() {
        int currentStageIndex = currentStageIndex();
        IResearchStage stage = entry.value().stages().get(currentStageIndex);
        rhash = entryId.toString().hashCode() + currentStageIndex * 50;
        List<String> addendaKeys = new ArrayList<>();
        for (ResearchAddendum addendum : unlockedAddenda()) {
            addendaKeys.add(addendum.textKey());
        }
        parsedPages = PageParser.parse(font, entryId, stage.textKey(), addendaKeys, activeKnowledgeRowCount(), isComplete, !stage.requiredResearch().isEmpty(), !stage.obtain().isEmpty(),
                !stage.craft().isEmpty(), !stage.requiredKnowledge().isEmpty());
        renderedStage = currentStageIndex;
        renderedComplete = isComplete;
        renderedAddenda = addendaKeys.size();
    }

    @Override
    public void tick() {
        super.tick();
        int stageIndex = currentStageIndex();
        if (stageIndex != renderedStage || isComplete != renderedComplete || unlockedAddenda().size() != renderedAddenda) {
            rebuildPages();
            currentPage = clampPage(currentPage);
        }
    }

    private List<ResearchAddendum> unlockedAddenda() {
        if (!isComplete || minecraft == null || minecraft.player == null)
            return List.of();
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        List<ResearchAddendum> unlocked = new ArrayList<>();
        for (ResearchAddendum addendum : entry.value().addenda()) {
            boolean met = true;
            for (Identifier required : addendum.requiredResearch()) {
                if (!knowledge.isResearchComplete(required)) {
                    met = false;
                    break;
                }
            }
            if (met)
                unlocked.add(addendum);
        }
        return unlocked;
    }

    private List<Identifier> displayRecipes(IResearchStage stage) {
        List<ResearchAddendum> addenda = unlockedAddenda();
        if (addenda.isEmpty())
            return stage.recipes();
        List<Identifier> all = new ArrayList<>(stage.recipes());
        for (ResearchAddendum addendum : addenda) {
            for (Identifier rid : addendum.recipes()) {
                if (!all.contains(rid))
                    all.add(rid);
            }
        }
        return all;
    }

    private int activeKnowledgeRowCount() {
        if (minecraft == null || minecraft.player == null)
            return 0;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        HolderLookup.Provider registries = minecraft.player.registryAccess();
        Optional<? extends HolderLookup.RegistryLookup<IResearchCategory>> lookupOpt = registries.lookup(IResearchCategory.REGISTRY_KEY);
        if (lookupOpt.isEmpty())
            return 0;
        HolderLookup.RegistryLookup<IResearchCategory> lookup = lookupOpt.get();
        int tc = 0;
        for (KnowledgeType type : KnowledgeType.values()) {
            boolean row = false;
            for (Holder.Reference<IResearchCategory> ref : lookup.listElements().toList()) {
                Optional<ResourceKey<IResearchCategory>> keyOpt = ref.unwrapKey();
                if (keyOpt.isEmpty())
                    continue;
                int raw = knowledge.rawKnowledge(type, keyOpt.get());
                if (raw > 0) {
                    row = true;
                    break;
                }
            }
            if (row)
                tc++;
        }
        return tc;
    }

    private int currentStageIndex() {
        if (minecraft == null || minecraft.player == null)
            return 0;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        isComplete = knowledge.isResearchComplete(entryId);
        int total = entry.value().stages().size();
        int rawStage = knowledge.researchStage(entryId);
        if (rawStage < 0)
            rawStage = 0;
        if (isComplete) {
            rawStage = total - 1;
        } else if (rawStage >= total) {
            rawStage = total - 1;
        }
        if (hold && (isComplete || knowledge.researchStage(entryId) > lastStage || minecraft.player.level().getGameTime() - holdSince > HOLD_TIMEOUT_TICKS)) {
            hold = false;
        }
        return Math.min(Math.max(0, rawStage), total - 1);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        renderPaneBackground(graphics);
        IResearchStage stage = entry.value().stages().get(currentStageIndex());
        boolean insertOpen = insertOpen();
        int pageMouseX = insertOpen ? Integer.MIN_VALUE : mouseX;
        int pageMouseY = insertOpen ? Integer.MIN_VALUE : mouseY;
        int pageBaseY = sh + CONTENT_Y_OFFSET;
        renderTextPages(graphics, pageBaseY, pageMouseX, pageMouseY);
        renderRequirements(graphics, stage, sw, pageMouseX, pageMouseY);
        renderWarpIndicator(graphics, stage, sw, sh + CONTENT_Y_OFFSET + TITLE_Y_ADVANCE, pageMouseX, pageMouseY);
        if (knowsResearch(KNOWLEDGETYPES_RESEARCH) && entryId.equals(KNOWLEDGETYPES_RESEARCH)) {
            drawKnowledges(graphics, sw, sh + KNOW_INPAGE_INSERT_INPAGE_Y_OFFSET - 16, pageMouseX, pageMouseY, true);
        }
        if (showingAspects) {
            renderAspectsInsert(graphics, mouseX, mouseY);
        } else if (showingKnowledge) {
            renderKnowledgeInsert(graphics, mouseX, mouseY);
        } else if (showingConstruct) {
            renderConstructInsert(graphics, stage, mouseX, mouseY);
        } else if (shownRecipe != null) {
            renderRecipePage(graphics, mouseX, mouseY);
        }
        renderBookmarks(graphics, mouseX, mouseY);
        renderRecipeBookmarks(graphics, stage, mouseX, mouseY);
        drawNavigation(graphics, mouseX, mouseY);
    }

    private void renderPaneBackground(GuiGraphicsExtractor graphics) {
        float ox = (width - PANE_W * PANE_SCALE) / 2.0F;
        float oy = (height - PANE_H * PANE_SCALE) / 2.0F;
        graphics.pose().pushMatrix();
        graphics.pose().translate(ox, oy);
        graphics.pose().scale(PANE_SCALE, PANE_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, 0, 0, 0.0F, 0.0F, PANE_W, PANE_H, PANE_W, PANE_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        graphics.pose().popMatrix();
    }

    private void renderTextPages(GuiGraphicsExtractor graphics, int baseY, int mouseX, int mouseY) {
        if (parsedPages.isEmpty())
            return;
        int leftIndex = currentPage;
        int rightIndex = currentPage + 1;
        if (leftIndex < parsedPages.size()) {
            drawPage(graphics, parsedPages.get(leftIndex), 0, sw, baseY, leftIndex == 0);
        }
        if (rightIndex < parsedPages.size()) {
            drawPage(graphics, parsedPages.get(rightIndex), 1, sw, baseY, false);
        }
    }

    private void drawPage(GuiGraphicsExtractor graphics, PageParser.Page page, int side, int x, int y, boolean drawTitle) {
        int currentY = y;
        if (drawTitle) {
            drawDivider(graphics, x + 4, currentY - 7);
            renderTitle(graphics, x, currentY);
            drawDivider(graphics, x + 4, currentY + 10);
            currentY += TITLE_Y_ADVANCE;
        }
        int textX = x + PAGE_LEFT_OFFSET + side * PAGE_SIDE_OFFSET;
        for (PageParser.PageElement element : page.elements()) {
            if (element instanceof PageParser.PageElement.Text text) {
                FormattedCharSequence sequence = sink -> StringDecomposer.iterateFormatted(text.content(), text.style(), sink);
                graphics.text(font, sequence, textX, currentY - 6, TEXT_LINE_COLOR, false);
                currentY += LINE_HEIGHT;
                if (text.paragraphBreak()) {
                    currentY += (int) (LINE_HEIGHT * 0.66F);
                }
            } else if (element instanceof PageParser.PageElement.Image image) {
                PageParser.PageImage pi = image.image();
                int pad = (PAGE_WIDTH - pi.renderedWidth()) / 2;
                graphics.pose().pushMatrix();
                graphics.pose().translate(textX + pad, currentY - 5);
                graphics.pose().scale(pi.scale, pi.scale);
                graphics.blit(RenderPipelines.GUI_TEXTURED, pi.texture, 0, 0, (float) pi.u, (float) pi.v, pi.w, pi.h, pi.w, pi.h, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
                graphics.pose().popMatrix();
                currentY += pi.renderedHeight() + 2;
            }
        }
    }

    private void renderTitle(GuiGraphicsExtractor graphics, int x, int y) {
        Component title = getTitle();
        int titleWidth = font.width(title);
        if (titleWidth <= PAGE_WIDTH) {
            int titleX = x + PAGE_LEFT_OFFSET + PAGE_WIDTH / 2 - titleWidth / 2;
            graphics.text(font, title, titleX, y, TITLE_COLOR, false);
        } else {
            float scale = (float) PAGE_WIDTH / titleWidth;
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + PAGE_LEFT_OFFSET + PAGE_WIDTH / 2.0F - titleWidth / 2.0F * scale, y + scale);
            graphics.pose().scale(scale, scale);
            graphics.text(font, title, 0, 0, TITLE_COLOR, false);
            graphics.pose().popMatrix();
        }
    }

    private void drawDivider(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, x, y, (float) DIVIDER_U, (float) DIVIDER_V, DIVIDER_WIDTH, DIVIDER_THICK, DIVIDER_WIDTH, DIVIDER_THICK,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
    }

    private void renderRequirements(GuiGraphicsExtractor graphics, IResearchStage stage, int x, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null)
            return;
        if (currentPage > 0)
            return;
        if (isComplete)
            return;
        Player player = minecraft.player;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        int reqY = sh + REQ_TOP_Y_OFFSET;
        boolean hasAny = false;
        long gameTime = player.level().getGameTime();

        boolean[] researchSatisfied = new boolean[stage.requiredResearch().size()];
        boolean[] obtainSatisfied = new boolean[stage.obtain().size()];
        boolean[] craftSatisfied = new boolean[stage.craft().size()];
        boolean[] knowSatisfied = new boolean[stage.requiredKnowledge().size()];

        if (!stage.requiredResearch().isEmpty()) {
            reqY -= REQ_ROW_STEP;
            hasAny = true;
            renderRowLabel(graphics, x, reqY, LABEL_RESEARCH_V, mouseX, mouseY);
            renderResearchPrereqs(graphics, stage.requiredResearch(), knowledge, x, reqY, mouseX, mouseY, researchSatisfied);
        }
        if (!stage.obtain().isEmpty()) {
            reqY -= REQ_ROW_STEP;
            hasAny = true;
            renderRowLabel(graphics, x, reqY, LABEL_OBTAIN_V, mouseX, mouseY);
            renderItemRow(graphics, stage.obtain(), x, reqY, gameTime, mouseX, mouseY, true, obtainSatisfied);
        }
        if (!stage.craft().isEmpty()) {
            reqY -= REQ_ROW_STEP;
            hasAny = true;
            renderRowLabel(graphics, x, reqY, LABEL_CRAFT_V, mouseX, mouseY);
            renderItemRow(graphics, stage.craft(), x, reqY, gameTime, mouseX, mouseY, false, craftSatisfied);
        }
        if (!stage.requiredKnowledge().isEmpty()) {
            reqY -= REQ_ROW_STEP;
            hasAny = true;
            renderRowLabel(graphics, x, reqY, LABEL_KNOW_V, mouseX, mouseY);
            renderKnowledgeRow(graphics, stage.requiredKnowledge(), knowledge, x, reqY, mouseX, mouseY, knowSatisfied);
        }
        if (hasAny) {
            reqY -= 12;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, x + 4, reqY - 2, (float) COMPLETE_DIVIDER_U, (float) COMPLETE_DIVIDER_V, COMPLETE_DIVIDER_W, COMPLETE_DIVIDER_H,
                    COMPLETE_DIVIDER_W, COMPLETE_DIVIDER_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
            boolean allMet = allTrue(researchSatisfied) && allTrue(obtainSatisfied) && allTrue(craftSatisfied) && allTrue(knowSatisfied);
            if (allMet) {
                int hrx = x + COMPLETE_BUTTON_OFFSET_X;
                int hry = reqY + COMPLETE_BUTTON_Y_OFFSET;
                if (hold) {
                    Component holdLabel = Component.translatable("tc.stage.hold");
                    int lblWidth = font.width(holdLabel);
                    graphics.text(font, holdLabel, x + 52 - lblWidth / 2, reqY - 4, COMPLETE_LABEL_COLOR, true);
                } else {
                    boolean hover = mouseInside(hrx, hry, COMPLETE_BUTTON_W, COMPLETE_BUTTON_H, mouseX, mouseY);
                    int tint = hover ? COMPLETE_BUTTON_TINT_NORMAL : COMPLETE_BUTTON_TINT_HOVER;
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, hrx, hry, (float) COMPLETE_BUTTON_U, (float) COMPLETE_BUTTON_V, COMPLETE_BUTTON_W, COMPLETE_BUTTON_H,
                            COMPLETE_BUTTON_W, COMPLETE_BUTTON_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE, tint);
                    Component label = Component.translatable("tc.stage.complete");
                    int lblWidth = font.width(label);
                    graphics.text(font, label, x + 52 - lblWidth / 2, reqY - 4, COMPLETE_LABEL_COLOR, true);
                }
            }
        } else {
            ClientPacketDistributor.sendToServer(new ServerboundAdvanceStagePayload(entryId));
        }
    }

    private boolean allTrue(boolean[] arr) {
        for (boolean b : arr)
            if (!b)
                return false;
        return true;
    }

    private boolean knowsResearch(Identifier id) {
        if (minecraft == null || minecraft.player == null)
            return false;
        return KnowledgeAccess.of(minecraft.player).isResearchComplete(id);
    }

    private void renderWarpIndicator(GuiGraphicsExtractor graphics, IResearchStage stage, int x, int y, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.player == null)
            return;
        if (isComplete)
            return;
        int warp = stage.warp();
        if (warp <= 0)
            return;
        if (warp > 5)
            warp = 5;
        drawForbiddenNode(graphics, x + FORBIDDEN_OFFSET_X, y + FORBIDDEN_Y_OFFSET);
        Component label = Component.translatable("tc.forbidden.level." + warp);
        int labelW = font.width(label);
        graphics.text(font, label, x + FORBIDDEN_LABEL_OFFSET_X - labelW / 2, y + FORBIDDEN_LABEL_Y_OFFSET, FORBIDDEN_COLOR, false);
        int hx = x + FORBIDDEN_HOVER_OFFSET_X;
        int hy = y + FORBIDDEN_HOVER_OFFSET_Y;
        if (mouseInside(hx, hy, FORBIDDEN_HOVER_W, FORBIDDEN_HOVER_H, mouseX, mouseY)) {
            Component warn = Component.translatable("tc.warp.warn");
            String warnStr = warn.getString().replace("%n", label.getString());
            graphics.setTooltipForNextFrame(font, Component.literal(warnStr), mouseX, mouseY);
        }
    }

    private void drawForbiddenNode(GuiGraphicsExtractor graphics, int centerX, int centerY) {
        if (minecraft == null || minecraft.player == null)
            return;
        int ticksExisted = minecraft.player.tickCount;
        int frame = ticksExisted % FORBIDDEN_NODE_FRAME_COUNT;
        Identifier nodeTex = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/misc/auranodes.png");
        int u = frame * FORBIDDEN_NODE_CELL_PX;
        int v = FORBIDDEN_NODE_ROW * FORBIDDEN_NODE_CELL_PX;
        int half = FORBIDDEN_NODE_DRAW_SIZE / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, nodeTex, centerX - half, centerY - half, (float) u, (float) v, FORBIDDEN_NODE_DRAW_SIZE, FORBIDDEN_NODE_DRAW_SIZE, FORBIDDEN_NODE_CELL_PX,
                FORBIDDEN_NODE_CELL_PX, FORBIDDEN_NODE_SHEET, FORBIDDEN_NODE_SHEET, FORBIDDEN_NODE_TINT);
    }

    private void renderRowLabel(GuiGraphicsExtractor graphics, int x, int y, int v, int mouseX, int mouseY) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, x + LABEL_OFFSET_X, y - 1, (float) REQUIREMENT_LABEL_U, (float) v, LABEL_WIDTH, LABEL_HEIGHT, LABEL_WIDTH,
                LABEL_HEIGHT, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE, LABEL_TINT);
        if (mouseInside(x + LABEL_OFFSET_X, y, LABEL_WIDTH / 4, LABEL_HEIGHT, mouseX, mouseY)) {
            switch (v) {
                case LABEL_KNOW_V -> graphics.setTooltipForNextFrame(Component.translatable("tc.need.know"), mouseX, mouseY);
                case LABEL_CRAFT_V -> graphics.setTooltipForNextFrame(Component.translatable("tc.need.craft"), mouseX, mouseY);
                case LABEL_OBTAIN_V -> graphics.setTooltipForNextFrame(Component.translatable("tc.need.obtain"), mouseX, mouseY);
                case LABEL_RESEARCH_V -> graphics.setTooltipForNextFrame(Component.translatable("tc.need.research"), mouseX, mouseY);
            }
        }
    }

    private void renderItemRow(GuiGraphicsExtractor graphics, List<ResearchRequirement> reqs, int x, int y, long gameTime, int mouseX, int mouseY, boolean obtain, boolean[] satisfied) {
        int spacing = reqs.size() > 6 ? SLOT_BUDGET / reqs.size() : SLOT_DEFAULT_SPACING;
        int shift = SLOT_BASE_SHIFT;
        int innerX = x + SLOT_INNER_OFFSET_X;
        Player player = minecraft.player;
        for (int i = 0; i < reqs.size(); i++) {
            ResearchRequirement req = reqs.get(i);
            int slotX = innerX + shift;
            ItemStack stack = pickRotatingItem(req, i);
            if (!stack.isEmpty()) {
                graphics.item(stack, slotX, y);
            }
            boolean met = obtain ? countMatching(player, req) >= req.amount() : ResearchManager.isCraftSatisfied(player, KnowledgeAccess.of(player), req);
            satisfied[i] = met;
            if (met) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, slotX + CHECKMARK_OFFSET_X, y, (float) CHECKMARK_U, (float) CHECKMARK_V, CHECKMARK_SIZE, CHECKMARK_SIZE,
                        CHECKMARK_SIZE, CHECKMARK_SIZE, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
            }
            if (mouseInside(slotX, y, SLOT_HIT_SIZE, SLOT_HIT_SIZE, mouseX, mouseY)) {
                if (!stack.isEmpty()) {
                    graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
                } else {
                    graphics.setTooltipForNextFrame(font, TCTooltips.need(obtain ? "obtain" : "craft"), mouseX, mouseY);
                }
            }
            shift += spacing;
        }
    }

    private void renderResearchPrereqs(GuiGraphicsExtractor graphics, List<Identifier> prereqs, IPlayerKnowledge knowledge, int x, int y, int mouseX, int mouseY, boolean[] satisfied) {
        int spacing = prereqs.size() > 6 ? SLOT_BUDGET / prereqs.size() : SLOT_DEFAULT_SPACING;
        int shift = SLOT_BASE_SHIFT;
        int innerX = x + SLOT_INNER_OFFSET_X;
        for (int i = 0; i < prereqs.size(); i++) {
            Identifier prereq = prereqs.get(i);
            int slotX = innerX + shift;
            graphics.text(font, Component.literal("?").withStyle(ChatFormatting.GOLD), slotX + 5, y + 4, 0xFFFFFFFF, true);
            boolean met = knowledge.isResearchComplete(prereq);
            satisfied[i] = met;
            if (met) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, slotX + CHECKMARK_OFFSET_X, y, (float) CHECKMARK_U, (float) CHECKMARK_V, CHECKMARK_SIZE, CHECKMARK_SIZE,
                        CHECKMARK_SIZE, CHECKMARK_SIZE, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
            }
            if (mouseInside(slotX, y, SLOT_HIT_SIZE, SLOT_HIT_SIZE, mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(font, TCTooltips.prereqEntryName(prereq), mouseX, mouseY);
            }
            shift += spacing;
        }
    }

    private static int knowledgeSpacing(int rewardCount) {
        return rewardCount > 6 ? SLOT_BUDGET / rewardCount : SLOT_DEFAULT_SPACING;
    }

    private int[] knowledgeSlotXs(List<KnowledgeReward> rewards, int innerX, int spacing) {
        int[] slotXs = new int[rewards.size()];
        int observationChips = ResearchNotes.stageObservationCost(entry.value(), entry.value().stages().get(currentStageIndex())).entries().size();
        int shift = SLOT_BASE_SHIFT;
        boolean observationCounted = false;
        for (int i = 0; i < rewards.size(); i++) {
            slotXs[i] = innerX + shift;
            if (rewards.get(i).type() != KnowledgeType.THEORY) {
                if (observationCounted) {
                    continue;
                }
                observationCounted = true;
                if (observationChips > 1) {
                    shift += (observationChips - 1) * spacing;
                }
            }
            shift += spacing;
        }
        return slotXs;
    }

    private void renderKnowledgeRow(GuiGraphicsExtractor graphics, List<KnowledgeReward> rewards, IPlayerKnowledge knowledge, int x, int y, int mouseX, int mouseY, boolean[] satisfied) {
        int spacing = knowledgeSpacing(rewards.size());
        int innerX = x + SLOT_INNER_OFFSET_X;
        int[] slotXs = knowledgeSlotXs(rewards, innerX, spacing);
        int theoryOrdinal = ResearchNotes.theoryRowsBefore(entry.value(), currentStageIndex());
        AspectList observationCost = ResearchNotes.stageObservationCost(entry.value(), entry.value().stages().get(currentStageIndex()));
        boolean observationAfford = observationCost.isEmpty() || AspectPools.canAfford(minecraft.player, observationCost);
        boolean observationDrawn = false;
        for (int i = 0; i < rewards.size(); i++) {
            KnowledgeReward reward = rewards.get(i);
            int slotX = slotXs[i];
            boolean met;
            if (reward.type() == KnowledgeType.THEORY) {
                Identifier learnKey = ResearchNoteData.learnKey(entryId, theoryOrdinal);
                theoryOrdinal++;
                met = knowledge.isResearchKnown(learnKey);
                graphics.item(new ItemStack(TCItems.RESEARCH_NOTE.get()), slotX, y);
                if (mouseInside(slotX, y, SLOT_HIT_SIZE, SLOT_HIT_SIZE, mouseX, mouseY)) {
                    List<Component> lines = new ArrayList<>();
                    lines.add(Component.translatable("tc.researchtheory", Component.translatable(entry.value().nameKey())));
                    if (!met) {
                        lines.add(Component.translatable(ResearchNotes.hasNoteFor(minecraft.player, learnKey) ? "tc.researchnote.table" : "tc.researchnote.click").withStyle(ChatFormatting.GRAY));
                    }
                    graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
                }
                if (met) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, slotX + CHECKMARK_OFFSET_X, y, (float) CHECKMARK_U, (float) CHECKMARK_V, CHECKMARK_SIZE, CHECKMARK_SIZE,
                            CHECKMARK_SIZE, CHECKMARK_SIZE, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
                }
            } else {
                met = observationAfford;
                satisfied[i] = met;
                if (observationDrawn) {
                    continue;
                }
                observationDrawn = true;
                AspectList cost = observationCost;
                List<AspectInstance> entries = cost.entries();
                for (int a = 0; a < entries.size(); a++) {
                    AspectInstance instance = entries.get(a);
                    int chipX = slotX + a * spacing;
                    if (AspectPools.isDiscovered(minecraft.player, instance.aspect())) {
                        int have = AspectPools.amount(minecraft.player, instance.aspect());
                        float alpha = 1.0F;
                        if (have < instance.amount()) {
                            alpha = Mth.sin(System.currentTimeMillis() % 600L / 600.0F * Mth.TWO_PI) * 0.25F + 0.75F;
                        }
                        AspectTagRenderer.render(graphics, font, chipX, y, instance.aspect(), instance.amount(), 0, alpha, false);
                        if (mouseInside(chipX, y, SLOT_HIT_SIZE, SLOT_HIT_SIZE, mouseX, mouseY)) {
                            List<Component> lines = new ArrayList<>();
                            lines.add(Component.translatable("tc.aspectcost"));
                            lines.add(AspectComponents.name(instance.aspect()).copy().append(Component.literal(" " + have + "/" + instance.amount()))
                                    .withStyle(have >= instance.amount() ? ChatFormatting.GREEN : ChatFormatting.RED));
                            graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
                        }

                        if (have >= instance.amount()) {
                            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, chipX + CHECKMARK_OFFSET_X, y, (float) CHECKMARK_U, (float) CHECKMARK_V, CHECKMARK_SIZE,
                                    CHECKMARK_SIZE, CHECKMARK_SIZE, CHECKMARK_SIZE, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
                        }
                    } else {
                        graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN_ASPECT_TEXTURE, chipX, y, 0.0F, 0.0F, 16, 16, 32, 32, 32, 32, UNKNOWN_ASPECT_TINT);
                        if (mouseInside(chipX, y, SLOT_HIT_SIZE, SLOT_HIT_SIZE, mouseX, mouseY)) {
                            List<Component> lines = new ArrayList<>();
                            lines.add(Component.translatable("tc.aspect.unknown"));
                            lines.add(Component.translatable("tc.discoveryerror", AspectComponents.help(instance.aspect())).withStyle(ChatFormatting.GRAY));
                            graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
                        }
                    }
                }
            }
            satisfied[i] = met;

            if (reward.type() == KnowledgeType.THEORY && mouseInside(slotX, y, SLOT_HIT_SIZE, SLOT_HIT_SIZE, mouseX, mouseY)) {
                reward.category().unwrapKey().ifPresent(k -> graphics.setTooltipForNextFrame(font, TCTooltips.knowledgeLabel(reward.type(), k), mouseX, mouseY));
            }
        }
    }

    private void renderBookmarks(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (knowsResearch(FIRSTSTEPS_RESEARCH)) {
            int aspectX = sw + BOOKMARK_OFFSET_X;
            int aspectY = sh + BOOKMARK_ASPECT_RENDER_Y;
            boolean aspectHover = mouseInside(aspectX, aspectY, BOOKMARK_W, BOOKMARK_H, mouseX, mouseY);
            int aspectLeft = aspectHover ? 0 : 3;
            int aspectBodyWidth = 24 - aspectLeft;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, aspectX + aspectLeft, aspectY, (float) BOOKMARK_ASPECT_U, (float) BOOKMARK_V, aspectBodyWidth, BOOKMARK_H,
                    aspectBodyWidth, BOOKMARK_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, aspectX + 20, aspectY, (float) BOOKMARK_TIP_U, (float) BOOKMARK_V, BOOKMARK_TIP_W, BOOKMARK_H, BOOKMARK_TIP_W,
                    BOOKMARK_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
            if (aspectHover) {
                graphics.setTooltipForNextFrame(font, Component.translatable("tc.aspect.name"), mouseX, mouseY);
            }
        }

        if (knowsResearch(KNOWLEDGETYPES_RESEARCH) && !entryId.equals(KNOWLEDGETYPES_RESEARCH)) {
            int knowX = sw + BOOKMARK_OFFSET_X;
            int knowY = sh + BOOKMARK_KNOWLEDGE_RENDER_Y;
            boolean knowHover = mouseInside(knowX, knowY, BOOKMARK_W, BOOKMARK_H, mouseX, mouseY);
            int knowLeft = knowHover ? 0 : 3;
            int knowBodyWidth = 24 - knowLeft;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, knowX - 1 + knowLeft, knowY, (float) BOOKMARK_KNOWLEDGE_U, (float) BOOKMARK_V, knowBodyWidth, BOOKMARK_H,
                    knowBodyWidth, BOOKMARK_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, knowX + 19, knowY, (float) BOOKMARK_TIP_U, (float) BOOKMARK_V, BOOKMARK_TIP_W, BOOKMARK_H, BOOKMARK_TIP_W,
                    BOOKMARK_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
            if (knowHover) {
                graphics.setTooltipForNextFrame(font, Component.translatable("tc.knowledge.name"), mouseX, mouseY);
            }
        }
    }

    private void renderRecipeBookmarks(GuiGraphicsExtractor graphics, IResearchStage stage, int mouseX, int mouseY) {
        List<Identifier> recipes = displayRecipes(stage);
        boolean hasConstruct = stage.construct().isPresent();
        int totalBookmarks = recipes.size() + (hasConstruct ? 1 : 0);
        if (totalBookmarks == 0)
            return;
        int space = Math.min(RECIPE_BOOKMARK_MAX_STEP, RECIPE_BOOKMARK_TOTAL_BUDGET / totalBookmarks);
        int slotY = sh + RECIPE_BOOKMARK_BASE_Y_OFFSET;
        Random rng = new Random(rhash);
        for (Identifier rid : recipes) {
            List<RecipeDisplay> displays = RecipeDisplayCache.get(rid);
            if (displays == null || displays.isEmpty()) {
                slotY += space;
                continue;
            }
            ItemStack result = ItemStack.EMPTY;
            RecipeDisplay first = displays.get(0);
            SlotDisplay sd = first.result();
            try {
                result = sd.resolveForFirstStack(SlotDisplayContext.fromLevel(minecraft.level));
            } catch (Exception ignored) {
            }
            int x = sw + RECIPE_BOOKMARK_OFFSET_X;
            int shJitter = rng.nextInt(3);
            boolean hoverState = mouseInside(x, slotY - 1, RECIPE_BOOKMARK_HOVER_W, RECIPE_BOOKMARK_H, mouseX, mouseY);
            int le = rng.nextInt(3) + (hoverState ? 0 : 3);
            int tint = rid.equals(shownRecipe) ? RECIPE_BOOKMARK_TINT_SELECTED : RECIPE_BOOKMARK_TINT_NORMAL;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, x + shJitter, slotY - 1, (float) (RECIPE_BOOKMARK_U_BASE + le), (float) RECIPE_BOOKMARK_V, RECIPE_BOOKMARK_W,
                    RECIPE_BOOKMARK_H, RECIPE_BOOKMARK_W, RECIPE_BOOKMARK_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE, tint);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, x + shJitter, slotY - 1, (float) RECIPE_BOOKMARK_TIP_U, (float) RECIPE_BOOKMARK_V, RECIPE_BOOKMARK_TIP_W,
                    RECIPE_BOOKMARK_H, RECIPE_BOOKMARK_TIP_W, RECIPE_BOOKMARK_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
            if (!result.isEmpty()) {
                graphics.item(result, x + shJitter + RECIPE_BOOKMARK_ICON_OFFSET - le, slotY - 1);
                if (hoverState) {
                    graphics.setTooltipForNextFrame(font, result, mouseX, mouseY);
                }
            }
            slotY += space;
        }
        if (hasConstruct) {
            int x = sw + RECIPE_BOOKMARK_OFFSET_X;
            int shJitter = rng.nextInt(3);
            boolean hoverState = mouseInside(x, slotY - 1, RECIPE_BOOKMARK_HOVER_W, RECIPE_BOOKMARK_H, mouseX, mouseY);
            int le = rng.nextInt(3) + (hoverState ? 0 : 3);
            int tint = showingConstruct ? RECIPE_BOOKMARK_TINT_SELECTED : RECIPE_BOOKMARK_TINT_NORMAL;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, x + shJitter, slotY - 1, (float) (RECIPE_BOOKMARK_U_BASE + le), (float) RECIPE_BOOKMARK_V, RECIPE_BOOKMARK_W,
                    RECIPE_BOOKMARK_H, RECIPE_BOOKMARK_W, RECIPE_BOOKMARK_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE, tint);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, x + shJitter, slotY - 1, (float) RECIPE_BOOKMARK_TIP_U, (float) RECIPE_BOOKMARK_V, RECIPE_BOOKMARK_TIP_W,
                    RECIPE_BOOKMARK_H, RECIPE_BOOKMARK_TIP_W, RECIPE_BOOKMARK_H, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
            ItemStack icon = entryIconStack();
            if (!icon.isEmpty()) {
                graphics.item(icon, x + shJitter + RECIPE_BOOKMARK_ICON_OFFSET - le, slotY - 1);
            }
            if (hoverState) {
                graphics.setTooltipForNextFrame(font, Component.translatable("recipe.type.construct"), mouseX, mouseY);
            }
        }
    }

    private ItemStack entryIconStack() {
        List<ResearchIcon> icons = entry.value().icons();
        for (ResearchIcon icon : icons) {
            if (icon.kind() == ResearchIcon.Kind.ITEM) {
                return new ItemStack(BuiltInRegistries.ITEM.getValue(icon.id()));
            }
        }
        return ItemStack.EMPTY;
    }

    private void renderRecipePage(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (shownRecipe == null)
            return;
        int paperX = (width - 256) / 2;
        int paperY = (height - 256) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.PAPER, paperX, paperY, 0.0F, 0.0F, INSERT_PAPER_SIZE, INSERT_PAPER_SIZE, INSERT_PAPER_SIZE, INSERT_PAPER_SIZE,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        List<RecipeDisplay> displays = RecipeDisplayCache.get(shownRecipe);
        if (displays == null || displays.isEmpty())
            return;
        long gameTime = minecraft.player.level().getGameTime();
        if (recipePage >= displays.size())
            recipePage = displays.size() - 1;
        if (recipePage < 0)
            recipePage = 0;
        RecipeDisplay current = displays.get(recipePage);
        int cx = paperX + 128;
        int cy = paperY + 128;
        int gridW = RecipeDisplayWidget.width();
        int gridH = RecipeDisplayWidget.height();
        RecipeDisplayWidget.renderCrafting(graphics, cx - gridW / 2, cy - gridH / 2, current, gameTime);
        ItemStack hover = RecipeDisplayWidget.hoverStackForDisplay(cx - gridW / 2, cy - gridH / 2, current, gameTime, mouseX, mouseY);
        if (hover != null && !hover.isEmpty()) {
            graphics.setTooltipForNextFrame(font, hover, mouseX, mouseY);
        }
        if (displays.size() > 1) {
            float bob = bob();
            if (recipePage > 0) {
                drawTexturedRectScaled(graphics, paperX + RECIPE_NAV_LEFT_OFFSET_X, paperY + RECIPE_NAV_Y_OFFSET, ARROW_LEFT_U, ARROW_V, ARROW_W, ARROW_H, bob);
            }
            if (recipePage < displays.size() - 1) {
                drawTexturedRectScaled(graphics, paperX + RECIPE_NAV_RIGHT_OFFSET_X, paperY + RECIPE_NAV_Y_OFFSET, ARROW_RIGHT_U, ARROW_V, ARROW_W, ARROW_H, bob);
            }
        }
    }

    private void renderConstructInsert(GuiGraphicsExtractor graphics, IResearchStage stage, int mouseX, int mouseY) {
        ResearchConstruct construct = stage.construct().orElse(null);
        if (construct == null) {
            showingConstruct = false;
            return;
        }
        int paperX = (width - 256) / 2;
        int paperY = (height - 256) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.PAPER, paperX, paperY, 0.0F, 0.0F, INSERT_PAPER_SIZE, INSERT_PAPER_SIZE, INSERT_PAPER_SIZE, INSERT_PAPER_SIZE,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        long gameTime = minecraft.player.level().getGameTime();
        int centerX = paperX + INSERT_PAPER_SIZE / 2;
        int pageY = paperY + CONSTRUCT_PAGE_Y;
        Component title = Component.translatable("recipe.type.construct");
        graphics.text(font, title, centerX - font.width(title) / 2, pageY, CONSTRUCT_TITLE_COLOR, false);
        int dx = construct.xSize();
        int dy = construct.ySize();
        int dz = construct.zSize();
        int structWidth = (dx + dz - 1) * 16;
        int yoff = -dy * CONSTRUCT_LAYER_HALF_STRIDE;
        float shrink = dy > 3 ? (dy - 3) * CONSTRUCT_SHRINK_PER_LAYER : 0.0F;
        float structScale = 1.0F - shrink;
        float originX = centerX - structWidth * structScale / 2.0F;
        float originY = pageY + CONSTRUCT_STRUCT_Y + yoff * structScale;
        graphics.pose().pushMatrix();
        graphics.pose().translate(originX, originY);
        graphics.pose().scale(structScale, structScale);
        graphics.pose().pushMatrix();
        graphics.pose().translate(structWidth / 2.0F - CONSTRUCT_BACKDROP_DRAW_W, CONSTRUCT_BACKDROP_Y_BASE + Math.max(3 - dx, 3 - dz) * 8 + dx * 4 + dz * 4 + dy * CONSTRUCT_LAYER_STRIDE);
        graphics.pose().scale(2.0F, 2.0F);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK_OVERLAY, 0, 0, CONSTRUCT_BACKDROP_U, CONSTRUCT_BACKDROP_V, CONSTRUCT_BACKDROP_DRAW_W, CONSTRUCT_BACKDROP_DRAW_H,
                CONSTRUCT_BACKDROP_W, CONSTRUCT_BACKDROP_H, OVERLAY_TEX_SIZE, OVERLAY_TEX_SIZE, alphaTint(CONSTRUCT_BACKDROP_ALPHA));
        graphics.pose().popMatrix();
        ItemStack hover = ItemStack.EMPTY;
        List<ConstructCellDraw> layer = new ArrayList<>();
        int count = 0;
        for (int j = 0; j < dy; j++) {
            layer.clear();
            for (int k = dz - 1; k >= 0; k--) {
                for (int i = dx - 1; i >= 0; i--) {
                    int px = i * 16 + k * 16;
                    int py = -i * 8 + k * 8 + j * CONSTRUCT_LAYER_STRIDE;
                    ItemStack stack = resolveConstructCell(construct.cells().get(count), gameTime);
                    count++;
                    if (!stack.isEmpty()) {
                        layer.add(new ConstructCellDraw(px, py, stack));
                    }
                }
            }
            layer.sort(Comparator.comparingInt(ConstructCellDraw::py));
            for (ConstructCellDraw cell : layer) {
                graphics.item(cell.stack(), cell.px(), cell.py());
                if (mouseInside((int) (originX + cell.px() * structScale), (int) (originY + cell.py() * structScale), (int) (16 * structScale), (int) (16 * structScale), mouseX, mouseY)) {
                    hover = cell.stack();
                }
            }
        }
        graphics.pose().popMatrix();
        AspectList cost = construct.cost();
        if (!cost.isEmpty()) {
            List<AspectInstance> entries = cost.entries();
            int rowWidth = CONSTRUCT_COST_STRIDE * (entries.size() - 1) + 16;
            int rowX = centerX - rowWidth / 2;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK_OVERLAY, rowX - CONSTRUCT_WAND_SIZE - CONSTRUCT_WAND_GAP, pageY + CONSTRUCT_WAND_Y, CONSTRUCT_WAND_U,
                    CONSTRUCT_WAND_V, CONSTRUCT_WAND_SIZE, CONSTRUCT_WAND_SIZE, CONSTRUCT_WAND_SIZE, CONSTRUCT_WAND_SIZE, OVERLAY_TEX_SIZE, OVERLAY_TEX_SIZE, alphaTint(CONSTRUCT_WAND_ALPHA));
            int tagIndex = 0;
            for (AspectInstance costEntry : entries) {
                int tx = rowX + CONSTRUCT_COST_STRIDE * tagIndex;
                int ty = pageY + CONSTRUCT_COST_Y;
                AspectTagRenderer.render(graphics, font, tx, ty, costEntry.aspect(), costEntry.amount());
                if (mouseInside(tx, ty, 16, 16, mouseX, mouseY)) {
                    graphics.setTooltipForNextFrame(font, AspectComponents.name(costEntry.aspect()), mouseX, mouseY);
                }
                tagIndex++;
            }
        }
        if (!hover.isEmpty()) {
            graphics.setTooltipForNextFrame(font, hover, mouseX, mouseY);
        }
    }

    private record ConstructCellDraw(int px, int py, ItemStack stack) {
    }

    private static ItemStack resolveConstructCell(String spec, long gameTime) {
        if (spec.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (spec.startsWith("#")) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, Identifier.parse(spec.substring(1)));
            List<Holder<Item>> holders = new ArrayList<>();
            for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                holders.add(holder);
            }
            if (holders.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return new ItemStack(holders.get((int) (gameTime / 20L % holders.size())));
        }
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(spec)));
    }

    private static int alphaTint(float alpha) {
        return ((int) (alpha * 0xFF)) << 24 | 0x00FFFFFF;
    }

    private void renderAspectsInsert(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int paperX = (width - 256) / 2;
        int paperY = (height - 256) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.PAPER, paperX, paperY, 0.0F, 0.0F, INSERT_PAPER_SIZE, INSERT_PAPER_SIZE, INSERT_PAPER_SIZE, INSERT_PAPER_SIZE,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        drawAspectPage(graphics, paperX + ASPECTS_INSERT_OFFSET_X, paperY + ASPECTS_INSERT_OFFSET_Y, mouseX, mouseY);
    }

    private void drawAspectPage(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
        AspectList known = listedAspects();
        if (known.isEmpty())
            return;
        int count = -1;
        int start = aspectsPage * ASPECT_PAGE_ROWS;
        Identifier backTile = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/aspects/_back.png");
        Identifier unknownTile = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/aspects/_unknown.png");
        List<AspectInstance> sorted = known.sortedByTag();
        for (AspectInstance entry : sorted) {
            count++;
            if (count < start)
                continue;
            if (count >= start + ASPECT_PAGE_ROWS)
                break;
            int rowIndex = count % ASPECT_PAGE_ROWS;
            int rowY = y + rowIndex * ASPECT_ROW_STRIDE;
            IAspect aspect = entry.aspect().value();
            boolean rowHover = mouseInside(x, rowY, ASPECT_BACK_TILE_SIZE, ASPECT_BACK_TILE_SIZE, mouseX, mouseY);
            if (rowHover) {
                graphics.pose().pushMatrix();
                graphics.pose().translate(x + ASPECT_BACK_OFFSET_X, rowY + ASPECT_BACK_OFFSET_Y);
                graphics.pose().scale(ASPECT_BACK_SCALE, ASPECT_BACK_SCALE);
                int alpha = ((int) (ASPECT_BACK_ALPHA * 0xFF)) << 24;
                graphics.blit(RenderPipelines.GUI_TEXTURED, backTile, 0, 0, 0.0F, 0.0F, 16, 16, 32, 32, 32, 32, alpha | 0x00FFFFFF);
                graphics.pose().popMatrix();
            }
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + ASPECT_TAG_OFFSET_X, rowY + ASPECT_TAG_OFFSET_Y);
            graphics.pose().scale(ASPECT_TAG_SCALE, ASPECT_TAG_SCALE);
            if (AspectKnowledgeAccess.isKnown(entry.aspect())) {
                drawAspectTag(graphics, 0, 0, entry.aspect());
            } else {
                drawMaskedAspect(graphics, unknownTile, entry.aspect());
            }
            graphics.pose().popMatrix();
            graphics.pose().pushMatrix();
            graphics.pose().translate(x + ASPECT_NAME_OFFSET_X, rowY + ASPECT_NAME_OFFSET_Y);
            graphics.pose().scale(ASPECT_NAME_SCALE, ASPECT_NAME_SCALE);
            Component name = AspectComponents.name(entry.aspect());
            int nameW = font.width(name);
            graphics.text(font, name, -nameW / 2, 0, ASPECT_NAME_COLOR, false);
            graphics.pose().popMatrix();
            List<Holder<IAspect>> components = aspect.components();
            if (!components.isEmpty() && components.size() >= 2) {
                Holder<IAspect> left = components.get(0);
                Holder<IAspect> right = components.get(1);
                graphics.pose().pushMatrix();
                graphics.pose().translate(x + ASPECT_COMPONENT_LEFT_X, rowY + ASPECT_COMPONENT_Y_OFFSET);
                graphics.pose().scale(ASPECT_COMPONENT_SCALE, ASPECT_COMPONENT_SCALE);
                if (knowsAspect(left)) {
                    drawAspectTag(graphics, 0, 0, left);
                } else {
                    drawUnknownAspect(graphics, unknownTile);
                }
                graphics.pose().popMatrix();
                graphics.pose().pushMatrix();
                graphics.pose().translate(x + ASPECT_COMPONENT_RIGHT_X, rowY + ASPECT_COMPONENT_Y_OFFSET);
                graphics.pose().scale(ASPECT_COMPONENT_SCALE, ASPECT_COMPONENT_SCALE);
                if (knowsAspect(right)) {
                    drawAspectTag(graphics, 0, 0, right);
                } else {
                    drawUnknownAspect(graphics, unknownTile);
                }
                graphics.pose().popMatrix();
                if (knowsAspect(left)) {
                    graphics.pose().pushMatrix();
                    graphics.pose().translate(x + ASPECT_COMPONENT_LEFT_NAME_X, rowY + ASPECT_NAME_OFFSET_Y);
                    graphics.pose().scale(ASPECT_NAME_SCALE, ASPECT_NAME_SCALE);
                    Component leftName = AspectComponents.name(left);
                    int leftW = font.width(leftName);
                    graphics.text(font, leftName, -leftW / 2, 0, ASPECT_NAME_COLOR, false);
                    graphics.pose().popMatrix();
                }
                if (knowsAspect(right)) {
                    graphics.pose().pushMatrix();
                    graphics.pose().translate(x + ASPECT_COMPONENT_RIGHT_NAME_X, rowY + ASPECT_NAME_OFFSET_Y);
                    graphics.pose().scale(ASPECT_NAME_SCALE, ASPECT_NAME_SCALE);
                    Component rightName = AspectComponents.name(right);
                    int rightW = font.width(rightName);
                    graphics.text(font, rightName, -rightW / 2, 0, ASPECT_NAME_COLOR, false);
                    graphics.pose().popMatrix();
                }
                graphics.text(font, Component.literal("="), x + ASPECT_EQUALS_X, rowY + ASPECT_SEPARATOR_Y_OFFSET, ASPECT_SEPARATOR_COLOR, false);
                graphics.text(font, Component.literal("+"), x + ASPECT_PLUS_X, rowY + ASPECT_SEPARATOR_Y_OFFSET, ASPECT_SEPARATOR_COLOR, false);
            } else {
                graphics.text(font, Component.translatable("tc.aspect.primal"), x + ASPECT_PRIMAL_X, rowY + ASPECT_SEPARATOR_Y_OFFSET, ASPECT_PRIMAL_COLOR, false);
            }
        }
        Component progress = aspectProgress();
        graphics.text(font, progress, x + ASPECT_PROGRESS_X - font.width(progress) / 2, y + ASPECT_PROGRESS_Y, ASPECT_PRIMAL_COLOR, false);
        int totalKnown = known.size();
        int maxPages = totalKnown == 0 ? 0 : Mth.ceil(totalKnown / (float) ASPECT_PAGE_ROWS);
        float bob = bob();
        if (aspectsPage > 0) {
            drawTexturedRectScaled(graphics, x + ASPECT_NAV_LEFT_X_OFFSET, y + ASPECT_NAV_Y_OFFSET, ARROW_LEFT_U, ARROW_V, ARROW_W, ARROW_H, bob);
        }
        if (aspectsPage < maxPages - 1) {
            drawTexturedRectScaled(graphics, x + ASPECT_NAV_RIGHT_X_OFFSET, y + ASPECT_NAV_Y_OFFSET, ARROW_RIGHT_U, ARROW_V, ARROW_W, ARROW_H, bob);
        }
    }

    private void drawAspectTag(GuiGraphicsExtractor graphics, int x, int y, Holder<IAspect> aspect) {
        IAspect a = aspect.value();
        int color = 0xFF000000 | (a.color() & 0x00FFFFFF);
        graphics.blit(RenderPipelines.GUI_TEXTURED, a.texture(), x, y, 0.0F, 0.0F, 16, 16, 32, 32, 32, 32, color);
    }

    private void drawMaskedAspect(GuiGraphicsExtractor graphics, Identifier unknownTile, Holder<IAspect> aspect) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, unknownTile, 0, 0, 0.0F, 0.0F, 16, 16, 32, 32, 32, 32, 0xFF000000 | (aspect.value().color() & 0x00FFFFFF));
    }

    private void drawUnknownAspect(GuiGraphicsExtractor graphics, Identifier unknownTile) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, unknownTile, 0, 0, 0.0F, 0.0F, 16, 16, 32, 32, 32, 32, 0xFFCCCCCC);
    }

    private boolean knowsAspect(Holder<IAspect> aspect) {
        if (minecraft == null || minecraft.player == null)
            return false;
        return AspectPools.isDiscovered(minecraft.player, aspect);
    }

    private AspectList listedAspects() {
        if (minecraft == null || minecraft.player == null)
            return AspectList.EMPTY;
        HolderLookup.Provider registries = minecraft.player.registryAccess();
        Optional<? extends HolderLookup.RegistryLookup<IAspect>> lookupOpt = registries.lookup(IAspect.REGISTRY_KEY);
        if (lookupOpt.isEmpty())
            return AspectList.EMPTY;
        HolderLookup.RegistryLookup<IAspect> lookup = lookupOpt.get();
        AspectList list = AspectList.EMPTY;
        for (Holder.Reference<IAspect> ref : lookup.listElements().toList()) {
            if (AspectKnowledgeAccess.of(ref).isCompositionRevealed()) {
                list = list.add(ref, 1);
            }
        }
        return list;
    }

    private Component aspectProgress() {
        if (minecraft == null || minecraft.player == null)
            return Component.empty();
        HolderLookup.Provider registries = minecraft.player.registryAccess();
        Optional<? extends HolderLookup.RegistryLookup<IAspect>> lookupOpt = registries.lookup(IAspect.REGISTRY_KEY);
        if (lookupOpt.isEmpty())
            return Component.empty();
        List<Holder.Reference<IAspect>> all = lookupOpt.get().listElements().toList();
        int discovered = 0;
        for (Holder.Reference<IAspect> ref : all) {
            if (AspectKnowledgeAccess.isKnown(ref)) {
                discovered++;
            }
        }
        return Component.translatable("tc.aspect.progress", discovered, all.size());
    }

    private void renderKnowledgeInsert(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int paperX = (width - 256) / 2;
        int paperY = (height - 256) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.PAPER, paperX, paperY, 0.0F, 0.0F, INSERT_PAPER_SIZE, INSERT_PAPER_SIZE, INSERT_PAPER_SIZE, INSERT_PAPER_SIZE,
                TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        drawKnowledges(graphics, paperX + ASPECTS_INSERT_OFFSET_X, sh + KNOW_INPAGE_INSERT_Y_OFFSET, mouseX, mouseY, false);
        if (!hasAnyKnowledge()) {
            Component hint = Component.translatable("tc.knowledge.none");
            graphics.text(font, hint, (width - font.width(hint)) / 2, sh + KNOW_INPAGE_INSERT_Y_OFFSET, KNOW_GRID_AMT_COLOR, false);
        }
    }

    private boolean hasAnyKnowledge() {
        if (minecraft == null || minecraft.player == null)
            return false;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        Optional<? extends HolderLookup.RegistryLookup<IResearchCategory>> lookupOpt = minecraft.player.registryAccess().lookup(IResearchCategory.REGISTRY_KEY);
        if (lookupOpt.isEmpty())
            return false;
        for (Holder.Reference<IResearchCategory> ref : lookupOpt.get().listElements().toList()) {
            Optional<ResourceKey<IResearchCategory>> keyOpt = ref.unwrapKey();
            if (keyOpt.isEmpty())
                continue;
            for (KnowledgeType type : KnowledgeType.values()) {
                if (knowledge.rawKnowledge(type, keyOpt.get()) > 0)
                    return true;
            }
        }
        return false;
    }

    private void drawKnowledges(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, boolean inpage) {
        if (minecraft == null || minecraft.player == null)
            return;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        HolderLookup.Provider registries = minecraft.player.registryAccess();
        Optional<? extends HolderLookup.RegistryLookup<IResearchCategory>> lookupOpt = registries.lookup(IResearchCategory.REGISTRY_KEY);
        if (lookupOpt.isEmpty())
            return;
        HolderLookup.RegistryLookup<IResearchCategory> lookup = lookupOpt.get();
        int totalCategories = (int) lookup.listElements().count();
        if (totalCategories == 0)
            return;
        int hs = (int) (KNOW_GRID_INSERT_COL_BASE / (float) totalCategories);
        int yCursor = y - 18;
        int tc = 0;
        boolean drewSomething = false;
        for (KnowledgeType type : KnowledgeType.values()) {
            int fc = 0;
            boolean rowDrawn = false;
            for (Holder.Reference<IResearchCategory> ref : lookup.listElements().toList()) {
                Optional<ResourceKey<IResearchCategory>> keyOpt = ref.unwrapKey();
                if (keyOpt.isEmpty())
                    continue;
                ResourceKey<IResearchCategory> categoryKey = keyOpt.get();
                int amt = knowledge.knowledge(type, categoryKey);
                int raw = knowledge.rawKnowledge(type, categoryKey);
                int par = type.progression() > 0 ? raw % type.progression() : 0;
                if (amt > 0 || par > 0) {
                    drewSomething = true;
                    int cx = x + KNOW_GRID_X_BASE_OFFSET + (inpage ? KNOW_GRID_INPAGE_COL_STRIDE : hs) * fc;
                    int cy = yCursor - tc * (inpage ? KNOW_GRID_INPAGE_ROW_STRIDE : KNOW_GRID_INSERT_ROW_STRIDE);
                    drawKnowledgeIcon(graphics, cx, cy, type, ref);
                    String amtStr = Integer.toString(amt);
                    int amtWidth = font.width(amtStr);
                    graphics.text(font, Component.literal(amtStr), cx + KNOW_GRID_AMT_X_OFFSET - amtWidth, cy + KNOW_GRID_AMT_Y_OFFSET, KNOW_GRID_AMT_COLOR, true);
                    if (par > 0 && type.progression() > 0) {
                        int l = (int) ((float) par / type.progression() * KNOW_GRID_BAR_BAR_WIDTH);
                        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, cx, cy + KNOW_GRID_BAR_Y_OFFSET, 0.0F, (float) KNOW_GRID_BAR_FILLED_V, l, KNOW_GRID_BAR_BAR_HEIGHT,
                                l, KNOW_GRID_BAR_BAR_HEIGHT, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
                        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, cx + l, cy + KNOW_GRID_BAR_Y_OFFSET, (float) l, (float) KNOW_GRID_BAR_EMPTY_V,
                                KNOW_GRID_BAR_BAR_WIDTH - l, KNOW_GRID_BAR_BAR_HEIGHT, KNOW_GRID_BAR_BAR_WIDTH - l, KNOW_GRID_BAR_BAR_HEIGHT, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
                    }
                    if (mouseInside(cx, cy, 16, 16, mouseX, mouseY)) {
                        graphics.setTooltipForNextFrame(font, TCTooltips.knowledgeLabel(type, categoryKey), mouseX, mouseY);
                    }
                    fc++;
                    rowDrawn = true;
                }
            }
            if (rowDrawn)
                tc++;
        }
        if (inpage && drewSomething) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, x + 4, yCursor - tc * KNOW_GRID_INPAGE_ROW_STRIDE + 12, (float) DIVIDER_U, (float) DIVIDER_V, DIVIDER_WIDTH, 8,
                    DIVIDER_WIDTH, 8, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        }
    }

    private void drawKnowledgeIcon(GuiGraphicsExtractor graphics, int x, int y, KnowledgeType type, Holder.Reference<IResearchCategory> category) {
        Identifier typeIcon = Identifier.fromNamespaceAndPath(TCIds.MODID, "textures/research/knowledge_" + type.getSerializedName() + ".png");
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(KNOW_ICON_SCALE_INPAGE, KNOW_ICON_SCALE_INPAGE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, typeIcon, 0, 0, 0.0F, 0.0F, KNOW_ICON_TEX, KNOW_ICON_TEX, KNOW_ICON_TEX, KNOW_ICON_TEX, KNOW_ICON_TEX, KNOW_ICON_TEX);
        graphics.pose().translate((float) KNOW_GRID_CATEGORY_OVERLAY_OFFSET, (float) KNOW_GRID_CATEGORY_OVERLAY_OFFSET);
        graphics.pose().scale(KNOW_GRID_CATEGORY_OVERLAY_SCALE, KNOW_GRID_CATEGORY_OVERLAY_SCALE);
        graphics.blit(RenderPipelines.GUI_TEXTURED, category.value().icon(), 0, 0, 0.0F, 0.0F, KNOW_ICON_TEX, KNOW_ICON_TEX, KNOW_ICON_TEX, KNOW_ICON_TEX, KNOW_ICON_TEX, KNOW_ICON_TEX,
                KNOW_GRID_CATEGORY_OVERLAY_TINT);
        graphics.pose().popMatrix();
    }

    private void drawNavigation(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int arrowY = sh + ARROW_Y_OFFSET;
        float bob = bob();
        if (currentPage > 0 && !insertOpen()) {
            int leftX = sw + ARROW_LEFT_OFFSET_X;
            drawTexturedRectScaled(graphics, leftX, arrowY, ARROW_LEFT_U, ARROW_V, ARROW_W, ARROW_H, bob);
        }
        if (hasNextPage() && !insertOpen()) {
            int rightX = sw + ARROW_RIGHT_OFFSET_X;
            drawTexturedRectScaled(graphics, rightX, arrowY, ARROW_RIGHT_U, ARROW_V, ARROW_W, ARROW_H, bob);
        }
        if (!history.isEmpty()) {
            int backX = sw + BACK_OFFSET_X;
            drawTexturedRectScaled(graphics, backX, arrowY, BACK_U, BACK_V, BACK_W, BACK_H, bob);
            if (mouseInside(backX, arrowY, BACK_W, BACK_H, mouseX, mouseY)) {
                int textColor = 0xFFFFFFFF;
                graphics.text(font, Component.translatable("recipe.return"), mouseX, mouseY, textColor, true);
            }
        }
    }

    private boolean insertOpen() {
        return showingAspects || showingKnowledge || showingConstruct || shownRecipe != null;
    }

    private float bob() {
        if (minecraft == null || minecraft.player == null)
            return 0.0F;
        return Mth.sin(minecraft.player.tickCount / 3.0F) * 0.2F + 0.1F;
    }

    private void drawTexturedRectScaled(GuiGraphicsExtractor graphics, int x, int y, int u, int v, int w, int h, float scale) {
        float cx = x + w / 2.0F;
        float cy = y + h / 2.0F;
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(1.0F + scale, 1.0F + scale);
        graphics.pose().translate(-w / 2.0F, -h / 2.0F);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TCScreenTextures.RESEARCH_BOOK, 0, 0, (float) u, (float) v, w, h, w, h, TCScreenTextures.TEX_SIZE, TCScreenTextures.TEX_SIZE);
        graphics.pose().popMatrix();
    }

    private boolean mouseInside(int x, int y, int w, int h, int mx, int my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private ItemStack pickRotatingItem(ResearchRequirement req, int slotIndex) {
        int size = req.items().size();
        if (size == 0)
            return ItemStack.EMPTY;
        int index = (int) ((slotIndex + System.currentTimeMillis() / 1000L) % size);
        if (index < 0)
            index += size;
        return new ItemStack(req.items().get(index), Math.max(1, req.amount()), req.components());
    }

    private int countMatching(Player player, ResearchRequirement req) {
        int total = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty())
                continue;
            if (req.matches(stack)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mx = event.x();
            double my = event.y();
            int arrowYBase = sh + 189;
            int leftHitX = sw + ARROW_LEFT_HIT_OFFSET_X;
            int rightHitX = sw + ARROW_RIGHT_HIT_OFFSET_X;
            int backX = sw + BACK_OFFSET_X;
            int backY = sh + ARROW_Y_OFFSET;
            if (!insertOpen() && currentPage > 0 && mx >= leftHitX && mx < leftHitX + ARROW_LEFT_HIT_W && my >= arrowYBase && my < arrowYBase + ARROW_LEFT_HIT_H) {
                prevPage();
                return true;
            }
            if (!insertOpen() && hasNextPage() && mx >= rightHitX && mx < rightHitX + ARROW_LEFT_HIT_W && my >= arrowYBase && my < arrowYBase + ARROW_LEFT_HIT_H) {
                nextPage();
                return true;
            }
            if (mx >= backX && mx < backX + BACK_W && my >= backY && my < backY + BACK_H) {
                goBack();
                return true;
            }
            int aspectHitX = sw - 48;
            int aspectHitY = sh + BOOKMARK_ASPECT_CLICK_Y;
            if (knowsResearch(FIRSTSTEPS_RESEARCH) && mx >= aspectHitX && mx < aspectHitX + BOOKMARK_W && my >= aspectHitY && my < aspectHitY + BOOKMARK_H) {
                shownRecipe = null;
                showingKnowledge = false;
                showingConstruct = false;
                showingAspects = !showingAspects;
                history.clear();
                if (aspectsPage > maxAspectPages())
                    aspectsPage = 0;
                playSound(TCSounds.PAGE.get(), 0.7F, 0.9F);
                return true;
            }
            int knowHitX = sw - 48;
            int knowHitY = sh + BOOKMARK_KNOWLEDGE_CLICK_Y;
            if (knowsResearch(KNOWLEDGETYPES_RESEARCH) && !entryId.equals(KNOWLEDGETYPES_RESEARCH) && mx >= knowHitX && mx < knowHitX + BOOKMARK_W && my >= knowHitY && my < knowHitY + BOOKMARK_H) {
                shownRecipe = null;
                showingAspects = false;
                showingConstruct = false;
                showingKnowledge = !showingKnowledge;
                history.clear();
                playSound(TCSounds.PAGE.get(), 0.7F, 0.9F);
                return true;
            }
            if (showingAspects) {
                int aspectNavLeftX = sw + 38;
                int aspectNavRightX = sw + 205;
                int aspectNavY = sh + 192;
                if (aspectsPage > 0 && mx >= aspectNavLeftX && mx < aspectNavLeftX + 14 && my >= aspectNavY && my < aspectNavY + 14) {
                    aspectsPage--;
                    playSound(TCSounds.PAGE.get(), 0.7F, 0.9F);
                    return true;
                }
                if (aspectsPage < maxAspectPages() - 1 && mx >= aspectNavRightX && mx < aspectNavRightX + 14 && my >= aspectNavY && my < aspectNavY + 14) {
                    aspectsPage++;
                    playSound(TCSounds.PAGE.get(), 0.7F, 0.9F);
                    return true;
                }
            }
            if (shownRecipe != null && handleRecipeIngredientClick(mx, my)) {
                return true;
            }
            if (shownRecipe != null) {
                int recipeNavLeftX = sw + 38;
                int recipeNavRightX = sw + 205;
                int recipeNavY = sh + 192;
                List<RecipeDisplay> displays = RecipeDisplayCache.get(shownRecipe);
                int max = displays == null ? 0 : displays.size() - 1;
                if (recipePage > 0 && mx >= recipeNavLeftX && mx < recipeNavLeftX + 14 && my >= recipeNavY && my < recipeNavY + 14) {
                    recipePage--;
                    playSound(TCSounds.PAGE.get(), 0.7F, 0.9F);
                    return true;
                }
                if (recipePage < max && mx >= recipeNavRightX && mx < recipeNavRightX + 14 && my >= recipeNavY && my < recipeNavY + 14) {
                    recipePage++;
                    playSound(TCSounds.PAGE.get(), 0.7F, 0.9F);
                    return true;
                }
            }
            IResearchStage stage = entry.value().stages().get(currentStageIndex());
            int hitRecipe = hitRecipeBookmark(mx, my, stage);
            if (hitRecipe >= 0) {
                Identifier rid = displayRecipes(stage).get(hitRecipe);
                if (rid.equals(shownRecipe)) {
                    shownRecipe = null;
                } else {
                    shownRecipe = rid;
                }
                showingAspects = false;
                showingKnowledge = false;
                showingConstruct = false;
                history.clear();
                playSound(TCSounds.PAGE.get(), 0.7F, 0.9F);
                return true;
            }
            if (hitConstructBookmark(mx, my, stage)) {
                showingConstruct = !showingConstruct;
                shownRecipe = null;
                showingAspects = false;
                showingKnowledge = false;
                history.clear();
                playSound(TCSounds.PAGE.get(), 0.7F, 0.9F);
                return true;
            }
            if (currentPage == 0 && !isComplete && !insertOpen() && handleTheoryNoteClick(mx, my, stage)) {
                return true;
            }
            if (currentPage == 0 && !isComplete && !hold && !insertOpen()) {
                if (hitStageComplete(mx, my, stage)) {
                    ClientPacketDistributor.sendToServer(new ServerboundAdvanceStagePayload(entryId));
                    playSound(TCSounds.WRITE.get(), 0.66F, 1.0F);
                    lastStage = KnowledgeAccess.of(minecraft.player).researchStage(entryId);
                    holdSince = minecraft.player.level().getGameTime();
                    hold = true;
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean handleRecipeIngredientClick(double mx, double my) {
        if (shownRecipe == null || minecraft == null || minecraft.player == null)
            return false;
        List<RecipeDisplay> displays = RecipeDisplayCache.get(shownRecipe);
        if (displays == null || displays.isEmpty())
            return false;
        RecipeDisplay current = displays.get(Math.min(recipePage, displays.size() - 1));
        int cx = width / 2;
        int cy = height / 2;
        int gridW = RecipeDisplayWidget.width();
        int gridH = RecipeDisplayWidget.height();
        long gameTime = minecraft.player.level().getGameTime();
        ItemStack hover = RecipeDisplayWidget.hoverStackForDisplay(cx - gridW / 2, cy - gridH / 2, current, gameTime, (int) mx, (int) my);
        if (hover == null || hover.isEmpty())
            return false;
        Identifier itemId = hover.getItem().builtInRegistryHolder().unwrapKey().map(ResourceKey::identifier).orElse(null);
        if (itemId == null)
            return false;
        ClientPacketDistributor.sendToServer(new ServerboundRequestItemRecipePayload(itemId));
        return true;
    }

    public void openRecipeFromNavigation(Identifier recipeId) {
        if (recipeId.equals(shownRecipe))
            return;
        if (shownRecipe != null) {
            history.push(shownRecipe);
        }
        shownRecipe = recipeId;
        recipePage = 0;
        showingAspects = false;
        showingKnowledge = false;
        playSound(TCSounds.PAGE.get(), 0.7F, 0.9F);
    }

    private void nextPage() {
        if (currentPage < parsedPages.size() - 2) {
            currentPage += 2;
            playSound(TCSounds.PAGE.get(), 0.66F, 1.0F);
        }
    }

    private void prevPage() {
        if (currentPage >= 2) {
            currentPage -= 2;
            playSound(TCSounds.PAGE.get(), 0.66F, 1.0F);
        }
    }

    private void goBack() {
        if (showingConstruct) {
            showingConstruct = false;
            playSound(TCSounds.PAGE.get(), 0.66F, 1.0F);
            return;
        }
        if (!history.isEmpty()) {
            playSound(TCSounds.PAGE.get(), 0.66F, 1.0F);
            shownRecipe = history.pop();
        } else {
            shownRecipe = null;
        }
    }

    private int maxAspectPages() {
        int n = listedAspects().size();
        return n == 0 ? 0 : Mth.ceil(n / (float) ASPECT_PAGE_ROWS);
    }

    private void playSound(SoundEvent sound, float volume, float pitch) {
        if (minecraft == null || minecraft.player == null)
            return;
        minecraft.player.playSound(sound, volume, pitch);
    }

    private int hitRecipeBookmark(double mx, double my, IResearchStage stage) {
        List<Identifier> recipes = displayRecipes(stage);
        if (recipes.isEmpty())
            return -1;
        int space = recipeBookmarkSpace(stage);
        int slotY = sh + RECIPE_BOOKMARK_BASE_Y_OFFSET;
        int x = sw + RECIPE_BOOKMARK_OFFSET_X;
        for (int i = 0; i < recipes.size(); i++) {
            if (mx >= x && mx < x + RECIPE_BOOKMARK_HOVER_W && my >= slotY - 1 && my < slotY - 1 + RECIPE_BOOKMARK_H) {
                return i;
            }
            slotY += space;
        }
        return -1;
    }

    private int recipeBookmarkSpace(IResearchStage stage) {
        int total = displayRecipes(stage).size() + (stage.construct().isPresent() ? 1 : 0);
        return Math.min(RECIPE_BOOKMARK_MAX_STEP, RECIPE_BOOKMARK_TOTAL_BUDGET / Math.max(1, total));
    }

    private boolean hitConstructBookmark(double mx, double my, IResearchStage stage) {
        if (stage.construct().isEmpty())
            return false;
        int space = recipeBookmarkSpace(stage);
        int slotY = sh + RECIPE_BOOKMARK_BASE_Y_OFFSET + space * displayRecipes(stage).size();
        int x = sw + RECIPE_BOOKMARK_OFFSET_X;
        return mx >= x && mx < x + RECIPE_BOOKMARK_HOVER_W && my >= slotY - 1 && my < slotY - 1 + RECIPE_BOOKMARK_H;
    }

    private boolean handleTheoryNoteClick(double mx, double my, IResearchStage stage) {
        if (minecraft == null || minecraft.player == null || stage.requiredKnowledge().isEmpty()) {
            return false;
        }
        int rowsAbove = 1;
        if (!stage.requiredResearch().isEmpty())
            rowsAbove++;
        if (!stage.obtain().isEmpty())
            rowsAbove++;
        if (!stage.craft().isEmpty())
            rowsAbove++;
        int rowY = sh + REQ_TOP_Y_OFFSET - REQ_ROW_STEP * rowsAbove;
        List<KnowledgeReward> rewards = stage.requiredKnowledge();
        int spacing = knowledgeSpacing(rewards.size());
        int innerX = sw + SLOT_INNER_OFFSET_X;
        int[] slotXs = knowledgeSlotXs(rewards, innerX, spacing);
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        int theoryOrdinal = ResearchNotes.theoryRowsBefore(entry.value(), currentStageIndex());
        for (int i = 0; i < rewards.size(); i++) {
            if (rewards.get(i).type() != KnowledgeType.THEORY) {
                continue;
            }
            int ordinal = theoryOrdinal++;
            if (!mouseInside(slotXs[i], rowY, SLOT_HIT_SIZE, SLOT_HIT_SIZE, (int) mx, (int) my)) {
                continue;
            }
            Identifier learnKey = ResearchNoteData.learnKey(entryId, ordinal);
            if (knowledge.isResearchKnown(learnKey) || ResearchNotes.hasNoteFor(minecraft.player, learnKey)) {
                return false;
            }
            ClientPacketDistributor.sendToServer(new ServerboundObtainNotePayload(entryId, ordinal));
            return true;
        }
        return false;
    }

    private boolean hitStageComplete(double mx, double my, IResearchStage stage) {
        if (minecraft == null || minecraft.player == null)
            return false;
        Player player = minecraft.player;
        IPlayerKnowledge knowledge = KnowledgeAccess.of(player);
        boolean[] researchSatisfied = new boolean[stage.requiredResearch().size()];
        boolean[] obtainSatisfied = new boolean[stage.obtain().size()];
        boolean[] craftSatisfied = new boolean[stage.craft().size()];
        boolean[] knowSatisfied = new boolean[stage.requiredKnowledge().size()];
        for (int i = 0; i < stage.requiredResearch().size(); i++) {
            researchSatisfied[i] = knowledge.isResearchComplete(stage.requiredResearch().get(i));
        }
        for (int i = 0; i < stage.obtain().size(); i++) {
            obtainSatisfied[i] = countMatching(player, stage.obtain().get(i)) >= stage.obtain().get(i).amount();
        }
        for (int i = 0; i < stage.craft().size(); i++) {
            craftSatisfied[i] = ResearchManager.isCraftSatisfied(player, knowledge, stage.craft().get(i));
        }
        int theoryOrdinal = ResearchNotes.theoryRowsBefore(entry.value(), currentStageIndex());
        for (int i = 0; i < stage.requiredKnowledge().size(); i++) {
            KnowledgeReward reward = stage.requiredKnowledge().get(i);
            if (reward.type() == KnowledgeType.THEORY) {
                knowSatisfied[i] = knowledge.isResearchKnown(ResearchNoteData.learnKey(entryId, theoryOrdinal));
                theoryOrdinal++;
            } else {
                knowSatisfied[i] = AspectPools.canAfford(player, ResearchNotes.observationCost(entry.value(), reward.amount()));
            }
        }
        if (!allTrue(researchSatisfied) || !allTrue(obtainSatisfied) || !allTrue(craftSatisfied) || !allTrue(knowSatisfied)) {
            return false;
        }
        boolean hasAny = !stage.requiredResearch().isEmpty() || !stage.obtain().isEmpty() || !stage.craft().isEmpty() || !stage.requiredKnowledge().isEmpty();
        if (!hasAny)
            return false;
        int reqY = sh + REQ_TOP_Y_OFFSET;
        if (!stage.requiredResearch().isEmpty())
            reqY -= REQ_ROW_STEP;
        if (!stage.obtain().isEmpty())
            reqY -= REQ_ROW_STEP;
        if (!stage.craft().isEmpty())
            reqY -= REQ_ROW_STEP;
        if (!stage.requiredKnowledge().isEmpty())
            reqY -= REQ_ROW_STEP;
        reqY -= 12;
        int hrx = sw + COMPLETE_BUTTON_OFFSET_X;
        int hry = reqY + COMPLETE_BUTTON_Y_OFFSET;
        return mx >= hrx && mx < hrx + COMPLETE_BUTTON_W && my >= hry && my < hry + COMPLETE_BUTTON_H;
    }

    private boolean hasNextPage() {
        return currentPage < parsedPages.size() - 2;
    }

    private int clampPage(int page) {
        int max = Math.max(0, parsedPages.size() - 1);
        return Math.min(Math.max(0, page), max);
    }

    @Override
    public void onClose() {
        if (shownRecipe != null || showingAspects || showingKnowledge) {
            shownRecipe = null;
            showingAspects = false;
            showingKnowledge = false;
            history.clear();
            playSound(TCSounds.PAGE.get(), 0.4F, 1.1F);
            return;
        }
        if (minecraft != null)
            minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
