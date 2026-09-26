package com.leclowndu93150.thaumaturge.client.item;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.client.render.aspect.AspectTagRenderer;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.client.screen.TCTooltips;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.network.ServerboundScanSlotPayload;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class InventoryScanHandler {
    private static final int SCAN_TICKS = 25;
    private static final int SOUND_INTERVAL = 2;
    private static final float SOUND_VOLUME = 0.2F;
    private static final float SOUND_PITCH = 0.45F;
    private static final float SOUND_PITCH_SPREAD = 0.1F;
    private static final int PLAYER_PANEL_X = 26;
    private static final int PLAYER_PANEL_Y = 8;
    private static final int PLAYER_PANEL_WIDTH = 52;
    private static final int PLAYER_PANEL_HEIGHT = 70;
    private static final int CURSOR_TOP_OFFSET = -20;
    private static final int TAG_TOP_OFFSET = -28;
    private static final int TAG_SPACING = 18;
    private static final int PROGRESS_COLOR = 0xFFFFAA00;
    private static final int HINT_COLOR = 0xFFAA00AA;
    private static final int NO_TARGET = Integer.MIN_VALUE;

    private static int target = NO_TARGET;
    private static int ticks;

    private InventoryScanHandler() {}

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !(event.getScreen() instanceof AbstractContainerScreen<?> screen) || screen instanceof CreativeModeInventoryScreen
                || !(player.containerMenu.getCarried().getItem() instanceof ThaumometerItem)) {
            target = NO_TARGET;
            ticks = 0;
            return;
        }
        int hovered = targetUnderMouse(screen, player, event.getMouseX(), event.getMouseY());
        if (hovered != target) {
            target = hovered;
            ticks = 0;
        }
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        Slot slot = hoveredSlot(player);
        if (slot != null) {
            graphics.setTooltipForNextFrame(minecraft.font, slot.getItem(), event.getMouseX(), event.getMouseY());
        }
        int top = event.getMouseY() + CURSOR_TOP_OFFSET;
        if (ticks > 0) {
            Component progress = TCTooltips.scanning(ticks / (float) SCAN_TICKS);
            graphics.text(minecraft.font, progress, event.getMouseX() - minecraft.font.width(progress) / 2, top, PROGRESS_COLOR, true);
            return;
        }
        if (slot == null) {
            return;
        }
        renderKnownAspects(graphics, minecraft, player, slot.getItem(), event.getMouseX(), event.getMouseY() + TAG_TOP_OFFSET);
        Component hint = studyHint(player, slot.getItem());
        if (hint != null) {
            graphics.text(minecraft.font, hint, event.getMouseX() - minecraft.font.width(hint) / 2, top, HINT_COLOR, true);
        }
    }

    private static @Nullable Component studyHint(LocalPlayer player, ItemStack stack) {
        for (AspectInstance entry : AspectIndexAccess.index().of(stack).entries()) {
            if (!AspectPools.isDiscovered(player, entry.aspect())) {
                return AspectPools.missingComponentHint(player, entry.aspect());
            }
        }
        return null;
    }

    private static void renderKnownAspects(GuiGraphicsExtractor graphics, Minecraft minecraft, LocalPlayer player, ItemStack stack, int centreX, int y) {
        if (!KnowledgeAccess.of(player).isResearchKnown(ScanKeys.item(stack.getItem()))) {
            return;
        }
        AspectList aspects = AspectIndexAccess.index().of(stack);
        if (aspects.isEmpty()) {
            return;
        }
        int x = centreX - (aspects.entries().size() * TAG_SPACING - (TAG_SPACING - AspectTagRenderer.TAG_SIZE)) / 2;
        for (AspectInstance entry : aspects.entries()) {
            AspectTagRenderer.render(graphics, minecraft.font, x, y, entry.aspect(), entry.amount());
            x += TAG_SPACING;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || target == NO_TARGET || !(minecraft.screen instanceof AbstractContainerScreen<?>) || !(player.containerMenu.getCarried().getItem() instanceof ThaumometerItem)) {
            target = NO_TARGET;
            ticks = 0;
            return;
        }
        Object scanned = resolveTarget(player);
        if (scanned == null || !ScanningManager.isThingStillScannable(player, scanned)) {
            ticks = 0;
            return;
        }
        ticks++;
        if (ticks % SOUND_INTERVAL == 0) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(TCSounds.CAMERA_TICKS.get(), SOUND_PITCH + player.getRandom().nextFloat() * SOUND_PITCH_SPREAD, SOUND_VOLUME));
        }
        if (ticks >= SCAN_TICKS) {
            ClientPacketDistributor.sendToServer(new ServerboundScanSlotPayload(target));
            ticks = 0;
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof ThaumometerItem) {
            event.getToolTip().add(TCTooltips.inventoryScanHint());
        }
    }

    private static int targetUnderMouse(AbstractContainerScreen<?> screen, LocalPlayer player, int mouseX, int mouseY) {
        if (screen instanceof InventoryScreen && overPlayerPanel(screen, mouseX, mouseY)) {
            return ServerboundScanSlotPayload.SELF;
        }
        Slot slot = screen.getSlotUnderMouse();
        if (slot == null || !slot.hasItem() || !slot.mayPickup(player) || slot instanceof ResultSlot) {
            return NO_TARGET;
        }
        return slot.index;
    }

    private static boolean overPlayerPanel(AbstractContainerScreen<?> screen, int mouseX, int mouseY) {
        int left = screen.getGuiLeft() + PLAYER_PANEL_X;
        int top = screen.getGuiTop() + PLAYER_PANEL_Y;
        return mouseX >= left && mouseX < left + PLAYER_PANEL_WIDTH && mouseY >= top && mouseY < top + PLAYER_PANEL_HEIGHT;
    }

    private static @Nullable Slot hoveredSlot(LocalPlayer player) {
        if (target == NO_TARGET || target == ServerboundScanSlotPayload.SELF || target >= player.containerMenu.slots.size()) {
            return null;
        }
        Slot slot = player.containerMenu.getSlot(target);
        return slot.hasItem() ? slot : null;
    }

    private static @Nullable Object resolveTarget(LocalPlayer player) {
        if (target == ServerboundScanSlotPayload.SELF) {
            return player;
        }
        Slot slot = hoveredSlot(player);
        return slot == null ? null : slot.getItem();
    }
}
