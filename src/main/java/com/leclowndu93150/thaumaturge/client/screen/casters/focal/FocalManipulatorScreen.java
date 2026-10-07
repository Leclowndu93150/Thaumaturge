package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.SpellProblem;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.content.spell.SpellText;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.InscriptionCheck;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.BlockEntityFocalManipulator;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.MenuFocalManipulator;
import com.leclowndu93150.thaumaturge.network.ServerboundSpellDraftPayload;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public final class FocalManipulatorScreen extends AbstractContainerScreen<MenuFocalManipulator> implements DraftHost {
    private static final int SYNC_DELAY = 4;
    private static final int NO_HOVER = -10000;
    private static final float UI_VOLUME = 0.25F;
    private static final float REFUSE_PITCH = 0.6F;

    private final PartPalette palette = new PartPalette(this);
    private final SpellCanvas canvas = new SpellCanvas(this);
    private final NodeInspector inspector = new NodeInspector(this);
    private final SpellSummaryPanel summaryPanel = new SpellSummaryPanel(this);
    private FocalLayout layout = FocalLayout.of(0, 0);
    private Spell draft = Spell.empty();
    private String name = "";
    private Selection selection = Selection.ROOT;
    private @Nullable SpellSummary summary;
    private @Nullable Spell lastSent;
    private @Nullable Spell lastSeen;
    private ItemStack lastFocus = ItemStack.EMPTY;
    private boolean dirty;
    private int quietTicks;
    private boolean adopted;
    private @Nullable EditBox nameBox;
    private @Nullable EditBox searchBox;

    public FocalManipulatorScreen(MenuFocalManipulator menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        leftPos = 0;
        topPos = 0;
        layout = FocalLayout.of(width, height);
        if (!adopted) {
            menu.table().or(this::clientTable).ifPresent(table -> {
                draft = table.draft();
                name = table.name();
                lastSeen = draft;
            });
            adopted = true;
        }
        nameBox = new EditBox(font, layout.nameX0() + FocalLayout.NAME_TEXT_X, layout.nameY() + FocalLayout.NAME_TEXT_Y, layout.nameX1() - layout.nameX0() - FocalLayout.NAME_TEXT_X,
                FocalLayout.NAME_H, Component.empty());
        nameBox.setBordered(false);
        nameBox.setMaxLength(BlockEntityFocalManipulator.MAX_NAME_LENGTH);
        nameBox.setValue(name);
        nameBox.setTextColor(FocalColors.WHITE);
        nameBox.setHint(SpellText.inspectorLabel("name_hint"));
        nameBox.setResponder(value -> {
            if (!value.equals(name)) {
                name = value;
                markDirty();
            }
        });
        addRenderableWidget(nameBox);
        searchBox = new EditBox(font, layout.leftX0() + FocalLayout.SEARCH_TEXT_X, layout.bodyY0() + FocalLayout.SEARCH_Y + FocalLayout.SEARCH_TEXT_Y,
                FocalLayout.LEFT_W - FocalLayout.SEARCH_TEXT_X * 2, FocalLayout.SEARCH_H, Component.empty());
        searchBox.setBordered(false);
        searchBox.setTextColor(FocalColors.WHITE);
        searchBox.setHint(SpellText.inspectorLabel("search_hint"));
        searchBox.setResponder(palette::setSearch);
        addRenderableWidget(searchBox);
        palette.refresh();
        refresh();
    }

    private Optional<BlockEntityFocalManipulator> clientTable() {
        return minecraft != null && minecraft.level != null && minecraft.level.getBlockEntity(menu.pos()) instanceof BlockEntityFocalManipulator table ? Optional.of(table) : Optional.empty();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, FocalSprites.CABINET, FocalLayout.CABINET_X, FocalLayout.CABINET_Y, 0.0F, 0.0F, FocalSprites.CABINET_W, FocalSprites.CABINET_H,
                FocalSprites.STRIP_TEXTURE, FocalSprites.STRIP_TEXTURE);
        FocalDraw.sprite(graphics, FocalSprites.FRAME, layout.panelX0() - FocalLayout.FRAME_OUTSET, layout.panelY0() - FocalLayout.FRAME_OUTSET,
                layout.panelX1() - layout.panelX0() + FocalLayout.FRAME_OUTSET * 2, layout.panelY1() - layout.panelY0() + FocalLayout.FRAME_OUTSET * 2);
        FocalDraw.sprite(graphics, FocalSprites.SLOT, layout.innerX0(), layout.innerY0(), FocalSprites.SLOT_SIZE, FocalSprites.SLOT_SIZE);
        FocalDraw.field(graphics, layout.nameX0(), layout.nameY(), layout.nameX1() - layout.nameX0(), FocalLayout.NAME_H);
        int hoverX = summaryPanel.open() ? NO_HOVER : mouseX;
        int hoverY = summaryPanel.open() ? NO_HOVER : mouseY;
        confirmButton(graphics, hoverX, hoverY);
        palette.render(graphics, layout, hoverX, hoverY);
        canvas.render(graphics, layout, hoverX, hoverY);
        int top = inspector.render(graphics, layout, hoverX, hoverY);
        summaryPanel.render(graphics, layout, top, inscription(), hoverX, hoverY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.nextStratum();
        summaryPanel.renderOverlay(graphics, mouseX, mouseY);
    }

    private void confirmButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        boolean hovered = FocalDraw.over(mouseX, mouseY, layout.confirmX(), layout.confirmY(), FocalLayout.CONFIRM_W, FocalLayout.CONFIRM_H);
        InscriptionCheck check = inscribeCheck();
        FocalDraw.button(graphics, layout.confirmX(), layout.confirmY(), FocalLayout.CONFIRM_W, FocalLayout.CONFIRM_H, FocalSprites.GLYPH_CHECK, FocalSprites.GLYPH_CHECK_SIZE, hovered, check.busy(),
                check.ready() || check.busy());
        if (hovered) {
            graphics.setComponentTooltipForNextFrame(font, SpellText.inscribeTooltip(summary(), check), mouseX, mouseY);
        }
    }

    @Override
    public InscriptionCheck inscribeCheck() {
        ItemStack focus = menu.getSlot(BlockEntityFocalManipulator.SLOT_FOCUS).getItem();
        boolean focused = !focus.isEmpty() && Spells.tierOf(focus).isPresent();
        return InscriptionCheck.of(player(), focused, inscription() > 0.0F, summary(), FocusItems.aspects(summary(), registries()));
    }

    private boolean canInscribe() {
        return inscribeCheck().ready();
    }

    private float inscription() {
        return clientTable().filter(BlockEntityFocalManipulator::inscribing).map(BlockEntityFocalManipulator::progress).orElse(0.0F);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        return mouseX < 0 || mouseY < 0 || mouseX >= width || mouseY >= height;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {}

    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack focus = menu.getSlot(BlockEntityFocalManipulator.SLOT_FOCUS).getItem();
        if (!ItemStack.isSameItemSameComponents(focus, lastFocus)) {
            lastFocus = focus.copy();
            refresh();
        }
        if (dirty && ++quietTicks >= SYNC_DELAY) {
            flush();
        } else if (!dirty) {
            clientTable().ifPresent(table -> {
                if (!table.draft().equals(lastSeen) && !table.draft().equals(lastSent)) {
                    draft = table.draft();
                    name = table.name();
                    lastSeen = draft;
                    if (nameBox != null) {
                        nameBox.setValue(name);
                    }
                    if (!SpellPaths.exists(draft.root(), selection.path())) {
                        selection = Selection.ROOT;
                    }
                    refresh();
                }
            });
        }
    }

    private void markDirty() {
        dirty = true;
        quietTicks = 0;
    }

    private void flush() {
        dirty = false;
        quietTicks = 0;
        lastSent = draft;
        lastSeen = draft;
        ClientPacketDistributor.sendToServer(new ServerboundSpellDraftPayload(menu.pos(), name, draft));
    }

    private void refresh() {
        summary = null;
        canvas.relayout();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (summaryPanel.open()) {
            summaryPanel.mouseClicked(mouseX, mouseY);
            return true;
        }
        if (FocalDraw.over(mouseX, mouseY, layout.confirmX(), layout.confirmY(), FocalLayout.CONFIRM_W, FocalLayout.CONFIRM_H)) {
            inscribe();
            return true;
        }
        if (!overTextField(mouseX, mouseY)) {
            setFocused(null);
        }
        if (palette.mouseClicked(layout, mouseX, mouseY) || canvas.mouseClicked(layout, mouseX, mouseY, event.button()) || inspector.mouseClicked(mouseX, mouseY)
                || summaryPanel.mouseClicked(mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean overTextField(double mouseX, double mouseY) {
        return nameBox != null && nameBox.isMouseOver(mouseX, mouseY) || searchBox != null && searchBox.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (palette.mouseDragged(layout, event.y()) || canvas.mouseDragged(dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        palette.mouseReleased();
        canvas.mouseReleased();
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (palette.mouseScrolled(layout, mouseX, mouseY, scrollY) || canvas.mouseScrolled(layout, mouseX, mouseY, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        boolean typing = nameBox != null && nameBox.isFocused() || searchBox != null && searchBox.isFocused();
        if (typing && event.key() != GLFW.GLFW_KEY_ESCAPE) {
            getFocused().keyPressed(event);
            return true;
        }
        if ((event.key() == GLFW.GLFW_KEY_DELETE || event.key() == GLFW.GLFW_KEY_BACKSPACE) && !selection.ghost() && !selection.path().isEmpty()) {
            List<Integer> path = selection.path();
            edit(spell -> spell.withRoot(SpellPaths.remove(spell.root(), path, this::maxChildren)));
            select(Selection.node(SpellPaths.parent(path)));
            playClick();
            return true;
        }
        return super.keyPressed(event);
    }

    private void inscribe() {
        if (!canInscribe() || minecraft == null || minecraft.gameMode == null) {
            playSound(SoundEvents.UI_BUTTON_CLICK.value(), REFUSE_PITCH);
            return;
        }
        flush();
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, MenuFocalManipulator.BUTTON_INSCRIBE);
        playClick();
    }

    @Override
    public void removed() {
        if (dirty) {
            flush();
        }
        super.removed();
    }

    @Override
    public Spell draft() {
        return draft;
    }

    @Override
    public void edit(UnaryOperator<Spell> change) {
        if (inscription() > 0.0F) {
            return;
        }
        Spell next = change.apply(draft);
        if (!next.equals(draft)) {
            draft = next;
            markDirty();
            refresh();
        }
    }

    @Override
    public Selection selection() {
        return selection;
    }

    @Override
    public void select(Selection next) {
        selection = SpellPaths.exists(draft.root(), next.path()) ? next : Selection.ROOT;
        canvas.relayout();
    }

    @Override
    public SpellSummary summary() {
        if (summary == null) {
            ItemStack focus = menu.getSlot(BlockEntityFocalManipulator.SLOT_FOCUS).getItem();
            Optional<FocusTier> tier = Spells.tierOf(focus);
            summary = Spells.analyze(draft, tier.orElse(null), registries(), player());
            if (focus.isEmpty()) {
                summary = withProblem(summary, SpellText.inspectorLabel("needs_focus"));
            }
        }
        return summary;
    }

    private static SpellSummary withProblem(SpellSummary base, Component message) {
        List<SpellProblem> problems = new ArrayList<>(base.problems());
        problems.addFirst(SpellProblem.of(message, true));
        return new SpellSummary(base.complexity(), base.budget(), base.vis(), base.xp(), base.crystals(), base.depth(), base.branches(), base.repeats(), base.cooldown(), base.chargeTicks(),
                base.pulseInterval(), problems);
    }

    @Override
    public RegistryAccess registries() {
        return minecraft.level.registryAccess();
    }

    @Override
    public Player player() {
        return minecraft.player;
    }

    @Override
    public Font font() {
        return font;
    }

    @Override
    public Optional<SpellPart> part(ResourceKey<SpellPart> key) {
        return Spells.part(registries(), key);
    }

    @Override
    public int maxChildren(SpellNode node) {
        return part(node.part()).map(found -> found.behavior().maxChildren()).orElse(0);
    }

    @Override
    public void place(ResourceKey<SpellPart> key) {
        Optional<SpellPart> chosen = part(key);
        if (chosen.isEmpty()) {
            return;
        }
        SpellNode fresh = SpellNode.of(key).withAspect(chosen.get().aspect().selectable() ? chosen.get().aspect().defaultAspect() : Optional.empty());
        SpellNode at = SpellPaths.at(draft.root(), selection.path());
        int room = maxChildren(at) - at.children().size();
        if (selection.ghost() || room > 0) {
            List<Integer> parent = selection.path();
            int index = at.children().size();
            edit(spell -> spell.withRoot(SpellPaths.append(spell.root(), parent, fresh)));
            select(Selection.node(SpellPaths.child(parent, index)));
        } else if (maxChildren(at) > 0 && chosen.get().behavior().maxChildren() >= at.children().size()) {
            List<Integer> path = selection.path();
            edit(spell -> spell.withRoot(SpellPaths.insertBelow(spell.root(), path, fresh)));
            select(Selection.node(SpellPaths.child(path, 0)));
        } else {
            playSound(TTSounds.CRAFTFAIL.get(), 1.0F);
            return;
        }
        playClick();
    }

    @Override
    public void playClick() {
        playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F);
    }

    private void playSound(SoundEvent sound, float pitch) {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, UI_VOLUME));
        }
    }
}
