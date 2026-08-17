package com.leclowndu93150.thaumaturge.client.item;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectChipsTooltip;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanKeys;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.client.tooltip.AspectChipsClientTooltip;
import com.leclowndu93150.thaumaturge.content.item.ThaumometerItem;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanRaycastHelper;
import com.leclowndu93150.thaumaturge.network.ServerboundInventoryScanPayload;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ThaumometerClientHandler {
    private static final int HIGHLIGHT_INTERVAL_TICKS = 5;
    private static final double HIGHLIGHT_ENTITY_RANGE = 16.0;
    private static final float HIGHLIGHT_ENTITY_PADDING = 5.0F;
    private static final double WILD_RAY_RANGE = 16.0;
    private static final int WILD_RAY_ANGLE_SPREAD = 25;
    private static final int SCAN_RUNE_BURSTS = 10;
    private static final float RUNE_ENTITY_HEIGHT_SCALE = 15.0F;
    private static final int RUNE_BLOCK_DURATION = 15;
    private static final float RUNE_GRAVITY = 0.03F;
    private static final float SCAN_VOLUME = 0.5F;
    private static final float SCAN_PITCH = 1.0F;

    private static final int SCAN_TICK_SOUND_INTERVAL = 2;
    private static final float SCAN_TICK_VOLUME = 0.2F;
    private static final float SCAN_TICK_PITCH_BASE = 0.45F;
    private static final float SCAN_TICK_PITCH_SPREAD = 0.1F;
    private static final int INVENTORY_SCAN_TICKS = 20;
    private static final int SLOT_SIZE = 16;
    private static final int PROGRESS_HEIGHT = 2;
    private static final int SCAN_OVERLAY_RGB = 0x299DE5;
    private static final int SCAN_PROGRESS_COLOR = 0xFF72D5FF;

    private static @Nullable String scanTargetKey;
    private static int inventoryHoverTicks;
    private static int inventoryHoverSlot = -1;
    private static ItemStack inventoryHoverStack = ItemStack.EMPTY;
    private static boolean inventoryScanSent;
    private static boolean inventoryScanActive;

    private ThaumometerClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.isPaused()) {
            return;
        }
        tickInventoryScanning(mc, player);
        tickScanning(mc, player);
        boolean held = player.getMainHandItem().is(TCItems.THAUMOMETER.get())
                || player.getOffhandItem().is(TCItems.THAUMOMETER.get());
        if (!held) {
            return;
        }
        if (player.tickCount % HIGHLIGHT_INTERVAL_TICKS != 0) {
            return;
        }
        HitResult hitResult = ScanRaycastHelper.performRaycast(player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() == HitResult.Type.BLOCK
                && ScanningManager.isThingStillScannable(player, ((BlockHitResult) hitResult).getBlockPos())) {
            ClientEffects.scanHighlight(mc.level, ((BlockHitResult) hitResult).getBlockPos());
        }
        if (hitResult instanceof EntityHitResult result
                && ScanningManager.isThingStillScannable(player, result.getEntity())) {
            ClientEffects.scanHighlight(result.getEntity());
        }
    }

    private static void tickInventoryScanning(Minecraft mc, LocalPlayer player) {
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)
                || !screen.getMenu().getCarried().is(TCItems.THAUMOMETER.get())
                || !inventoryScanActive) {
            inventoryScanActive = false;
            clearInventoryScan();
            return;
        }
        Slot slot = screen.getSlotUnderMouse();
        if (slot == null || slot instanceof ResultSlot || !slot.hasItem() || !slot.mayPickup(player)) {
            clearInventoryScan();
            return;
        }
        ItemStack stack = slot.getItem();
        int menuSlotIndex = screen.getMenu().slots.indexOf(slot);
        if (menuSlotIndex < 0) {
            clearInventoryScan();
            return;
        }
        if (menuSlotIndex != inventoryHoverSlot || !ItemStack.isSameItemSameComponents(stack, inventoryHoverStack)) {
            inventoryHoverSlot = menuSlotIndex;
            inventoryHoverStack = stack.copy();
            inventoryHoverTicks = 0;
            inventoryScanSent = false;
        }
        if (inventoryScanSent) {
            return;
        }
        if (!ScanningManager.isThingStillScannable(player, stack)) {
            inventoryScanSent = true;
            return;
        }
        inventoryHoverTicks++;
        if (inventoryHoverTicks % SCAN_TICK_SOUND_INTERVAL == 0) {
            player.level()
                    .playLocalSound(
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            TCSounds.CAMERA_TICKS.get(),
                            SoundSource.PLAYERS,
                            SCAN_TICK_VOLUME,
                            SCAN_TICK_PITCH_BASE + player.level().getRandom().nextFloat() * SCAN_TICK_PITCH_SPREAD,
                            false);
        }
        if (inventoryHoverTicks >= INVENTORY_SCAN_TICKS) {
            PacketDistributor.sendToServer(new ServerboundInventoryScanPayload(
                    screen.getMenu().containerId, menuSlotIndex, stack.copyWithCount(1)));
            inventoryScanSent = true;
        }
    }

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT
                || !(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        Slot slot = screen.getSlotUnderMouse();
        if (player == null
                || !screen.getMenu().getCarried().is(TCItems.THAUMOMETER.get())
                || slot == null
                || slot instanceof ResultSlot
                || !slot.hasItem()
                || !slot.mayPickup(player)) {
            return;
        }
        event.setCanceled(true);
        inventoryScanActive = true;
        clearInventoryScan();
    }

    @SubscribeEvent
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT && inventoryScanActive) {
            event.setCanceled(true);
            inventoryScanActive = false;
            clearInventoryScan();
        }
    }

    @SubscribeEvent
    public static void onContainerForeground(ContainerScreenEvent.Render.Foreground event) {
        if (!inventoryScanActive
                || inventoryScanSent
                || inventoryHoverTicks <= 0
                || inventoryHoverSlot < 0
                || inventoryHoverSlot
                        >= event.getContainerScreen().getMenu().slots.size()) {
            return;
        }
        AbstractContainerScreen<?> screen = event.getContainerScreen();
        Slot slot = screen.getMenu().getSlot(inventoryHoverSlot);
        if (screen.getSlotUnderMouse() != slot) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int pulseAlpha = 48 + (int) ((Mth.sin(inventoryHoverTicks * 0.7F) + 1.0F) * 24.0F);
        int overlayColor = pulseAlpha << 24 | SCAN_OVERLAY_RGB;
        int progress =
                Mth.clamp(Mth.ceil((float) inventoryHoverTicks / INVENTORY_SCAN_TICKS * SLOT_SIZE), 1, SLOT_SIZE);
        graphics.fill(slot.x, slot.y, slot.x + SLOT_SIZE, slot.y + SLOT_SIZE, overlayColor);
        graphics.fill(
                slot.x,
                slot.y + SLOT_SIZE - PROGRESS_HEIGHT,
                slot.x + progress,
                slot.y + SLOT_SIZE,
                SCAN_PROGRESS_COLOR);
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)
                || !screen.getMenu().getCarried().is(TCItems.THAUMOMETER.get())) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Slot slot = screen.getSlotUnderMouse();
        if (player == null || slot == null || !slot.hasItem()) {
            return;
        }
        ItemStack stack = slot.getItem();
        if (!KnowledgeAccess.of(player).isResearchKnown(ScanKeys.item(stack.getItem()))) {
            return;
        }
        AspectList aspects = AspectIndexAccess.index().of(stack);
        if (aspects.isEmpty()) {
            return;
        }
        AspectChipsClientTooltip tooltip = new AspectChipsClientTooltip(new AspectChipsTooltip(aspects));
        int x = Mth.clamp(event.getMouseX() + 9, 4, Math.max(4, screen.width - tooltip.getWidth(mc.font) - 4));
        int y = Math.max(4, event.getMouseY() - 34);
        tooltip.renderImage(mc.font, x, y, event.getGuiGraphics());
    }

    private static void clearInventoryScan() {
        inventoryHoverTicks = 0;
        inventoryHoverSlot = -1;
        inventoryHoverStack = ItemStack.EMPTY;
        inventoryScanSent = false;
    }

    private static void tickScanning(Minecraft mc, LocalPlayer player) {
        boolean scanning = player.isUsingItem() && player.getUseItem().is(TCItems.THAUMOMETER.get());
        if (!scanning) {
            scanTargetKey = null;
            return;
        }
        Level level = player.level();
        int elapsed = player.getTicksUsingItem();
        Object target = ThaumometerItem.resolveTarget(level, player);
        if (!ScanningManager.isThingStillScannable(player, target)) {
            player.stopUsingItem();
            scanTargetKey = null;
            return;
        }
        String key = keyOf(target);
        if (elapsed <= 1) {
            scanTargetKey = key;
        } else if (!key.equals(scanTargetKey)) {
            player.stopUsingItem();
            scanTargetKey = null;
            return;
        }
        if (elapsed % SCAN_TICK_SOUND_INTERVAL == 0) {
            player.level()
                    .playLocalSound(
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            TCSounds.CAMERA_TICKS.get(),
                            SoundSource.PLAYERS,
                            SCAN_TICK_VOLUME,
                            SCAN_TICK_PITCH_BASE + level.getRandom().nextFloat() * SCAN_TICK_PITCH_SPREAD,
                            false);
            drawScanTickFx(level, target);
        }
        if (elapsed >= ThaumometerItem.SCAN_COMPLETE_ELAPSED_TICKS) {
            player.stopUsingItem();
            scanTargetKey = null;
        }
    }

    private static String keyOf(@Nullable Object target) {
        if (target instanceof Entity entity) {
            return "e:" + entity.getId();
        }
        if (target instanceof BlockPos pos) {
            return "b:" + pos.asLong();
        }
        return "none";
    }

    private static void drawScanTickFx(Level level, @Nullable Object target) {
        RandomSource rand = level.getRandom();
        if (target instanceof Entity entity) {
            ClientEffects.blockRunes(
                    level,
                    entity.getX() - 0.5,
                    entity.getY() + entity.getEyeHeight() / 2.0F,
                    entity.getZ() - 0.5,
                    0.3F + rand.nextFloat() * 0.7F,
                    0.0F,
                    0.3F + rand.nextFloat() * 0.7F,
                    (int) (entity.getBbHeight() * RUNE_ENTITY_HEIGHT_SCALE),
                    RUNE_GRAVITY);
        } else if (target instanceof BlockPos pos) {
            ClientEffects.blockRunes(
                    level,
                    pos.getX(),
                    pos.getY() + 0.25,
                    pos.getZ(),
                    0.3F + rand.nextFloat() * 0.7F,
                    0.0F,
                    0.3F + rand.nextFloat() * 0.7F,
                    RUNE_BLOCK_DURATION,
                    RUNE_GRAVITY);
        }
    }

    private static BlockHitResult wildBlockRay(Level level, Player player) {
        RandomSource rand = level.getRandom();
        float pitch = player.getXRot() + rand.nextInt(WILD_RAY_ANGLE_SPREAD) - rand.nextInt(WILD_RAY_ANGLE_SPREAD);
        float yaw = player.getYRot() + rand.nextInt(WILD_RAY_ANGLE_SPREAD) - rand.nextInt(WILD_RAY_ANGLE_SPREAD);
        Vec3 from = player.getEyePosition();
        Vec3 direction = Vec3.directionFromRotation(pitch, yaw);
        Vec3 to = from.add(direction.scale(WILD_RAY_RANGE));
        return level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
    }
}
