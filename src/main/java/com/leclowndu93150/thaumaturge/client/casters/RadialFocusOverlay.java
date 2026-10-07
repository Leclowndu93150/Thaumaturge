package com.leclowndu93150.thaumaturge.client.casters;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.content.casters.FocusPouchItem;
import com.leclowndu93150.thaumaturge.content.casters.ItemFocus;
import com.leclowndu93150.thaumaturge.network.ServerboundFocusChangePayload;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class RadialFocusOverlay implements LayeredDraw.Layer {
    private static final ResourceLocation RADIAL_1 = TTIds.rl("textures/misc/radial.png");
    private static final ResourceLocation RADIAL_2 = TTIds.rl("textures/misc/radial2.png");

    private static final int RADIAL_TEX_SIZE = 256;
    private static final float BASE_RADIUS = 16.0F;
    private static final float RADIUS_PER_FOCUS = 2.5F;
    private static final float RING_1_SIZE = 2.75F;
    private static final float RING_2_SIZE = 2.55F;
    private static final int RING_TINT = 0x80808080;
    private static final float RING_SPIN_PERIOD = 720.0F;
    private static final int HOVER_BOX = 10;
    private static final int ITEM_HALF = 8;
    private static final float HOVER_SCALE_MAX = 1.3F;
    private static final long SCALE_UP_MS = 150L;
    private static final long SCALE_DOWN_MS = 250L;
    private static final long HUD_SCALE_MS = 150L;
    private static final int TOOLTIP_X_OFFSET = -4;
    private static final int TOOLTIP_Y_OFFSET = 20;
    private static final int INVENTORY_SIZE = 36;
    private static final int POUCH_SLOT_OFFSET = 1000;

    private static float radialHudScale;
    private static boolean pendingClick;

    private final TreeMap<String, Integer> foci = new TreeMap<>();
    private final Map<String, ItemStack> fociItem = new HashMap<>();
    private final Map<String, Boolean> fociHover = new HashMap<>();
    private final Map<String, Float> fociScale = new HashMap<>();
    private long lastTime;
    private boolean lastState;
    private boolean centerHover;

    public static boolean isAnimating() {
        return radialHudScale > 0.0F;
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (!CasterKeyHandler.radialActive || Minecraft.getInstance().screen != null) {
            return;
        }
        event.setCanceled(true);
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == InputConstants.PRESS) {
            pendingClick = true;
        }
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        long time = Util.getMillis();
        if (mc.player != null && (CasterKeyHandler.radialActive || radialHudScale > 0.0F)) {
            if (CasterKeyHandler.radialActive) {
                if (mc.screen != null) {
                    CasterKeyHandler.radialActive = false;
                    CasterKeyHandler.radialLock = true;
                    pendingClick = false;
                    lastTime = time;
                    return;
                }
                if (radialHudScale == 0.0F) {
                    getFociInfo(mc);
                    if ((!foci.isEmpty() || holdingSocketedCaster(mc)) && mc.mouseHandler.isMouseGrabbed()) {
                        mc.mouseHandler.releaseMouse();
                    }
                }
            } else if (mc.screen == null && lastState) {
                if (mc.isWindowActive() && !mc.mouseHandler.isMouseGrabbed()) {
                    mc.mouseHandler.grabMouse();
                }
                lastState = false;
            }

            renderRadialHud(graphics, mc, deltaTracker.getGameTimeDeltaPartialTick(false), time);

            if (time > lastTime) {
                if (centerHover && !CasterKeyHandler.radialActive && !CasterKeyHandler.radialLock) {
                    PacketDistributor.sendToServer(new ServerboundFocusChangePayload(CasterManager.REMOVE_FOCUS));
                    CasterKeyHandler.radialLock = true;
                }
                for (String key : fociHover.keySet()) {
                    if (fociHover.get(key)) {
                        if (!CasterKeyHandler.radialActive && !CasterKeyHandler.radialLock) {
                            PacketDistributor.sendToServer(new ServerboundFocusChangePayload(key));
                            CasterKeyHandler.radialLock = true;
                        }
                        if (fociScale.get(key) < HOVER_SCALE_MAX) {
                            fociScale.put(
                                    key,
                                    Math.min(
                                            HOVER_SCALE_MAX,
                                            fociScale.get(key) + radialChange(time, lastTime, SCALE_UP_MS)));
                        }
                    } else if (fociScale.get(key) > 1.0F) {
                        fociScale.put(
                                key, Math.max(1.0F, fociScale.get(key) - radialChange(time, lastTime, SCALE_DOWN_MS)));
                    }
                }
                if (!CasterKeyHandler.radialActive) {
                    radialHudScale -= radialChange(time, lastTime, HUD_SCALE_MS);
                } else if (radialHudScale < 1.0F) {
                    radialHudScale += radialChange(time, lastTime, HUD_SCALE_MS);
                }
                if (radialHudScale > 1.0F) {
                    radialHudScale = 1.0F;
                }
                if (radialHudScale < 0.0F) {
                    radialHudScale = 0.0F;
                    CasterKeyHandler.radialLock = false;
                }
                lastState = CasterKeyHandler.radialActive;
            }
        }
        pendingClick = false;
        lastTime = time;
    }

    private static boolean holdingSocketedCaster(Minecraft mc) {
        ItemStack stack = mc.player.getMainHandItem();
        if (!(stack.getItem() instanceof ICaster)) {
            stack = mc.player.getOffhandItem();
        }
        return stack.getItem() instanceof ICaster caster
                && caster.getFocusStack(stack).getItem() instanceof ItemFocus;
    }

    private static float radialChange(long time, long lastTime, long total) {
        return (float) (time - lastTime) / (float) total;
    }

    private void getFociInfo(Minecraft mc) {
        foci.clear();
        fociItem.clear();
        fociHover.clear();
        fociScale.clear();
        centerHover = false;
        int pouchCount = 0;
        if (ModList.get().isLoaded(TTIds.CURIOS)) {
            for (ThaumaturgeCuriosCompat.CurioPouchRef ref : ThaumaturgeCuriosCompat.equippedPouches(
                    mc.player, stack -> stack.getItem() instanceof FocusPouchItem)) {
                pouchCount++;
                indexPouch(ref.stack(), pouchCount);
            }
        }
        NonNullList<ItemStack> main = mc.player.getInventory().items;
        for (int slot = 0; slot < Math.min(INVENTORY_SIZE, main.size()); slot++) {
            ItemStack item = main.get(slot);
            if (item.getItem() instanceof ItemFocus focus) {
                String sortKey = focus.getSortingHelper(item);
                if (sortKey != null) {
                    addEntry(sortKey, slot, item);
                }
            }
            if (item.getItem() instanceof FocusPouchItem) {
                pouchCount++;
                indexPouch(item, pouchCount);
            }
        }
    }

    private void indexPouch(ItemStack pouch, int pouchId) {
        NonNullList<ItemStack> contents = FocusPouchItem.getInventory(pouch);
        for (int slot = 0; slot < contents.size(); slot++) {
            ItemStack stack = contents.get(slot);
            if (stack.getItem() instanceof ItemFocus focus) {
                String sortKey = focus.getSortingHelper(stack);
                if (sortKey != null) {
                    addEntry(sortKey, slot + pouchId * POUCH_SLOT_OFFSET, stack);
                }
            }
        }
    }

    private void addEntry(String key, int slot, ItemStack stack) {
        foci.put(key, slot);
        fociItem.put(key, stack.copy());
        fociScale.put(key, 1.0F);
        fociHover.put(key, false);
    }

    private void renderRadialHud(GuiGraphics graphics, Minecraft mc, float partialTicks, long time) {
        ItemStack casterStack = mc.player.getMainHandItem();
        if (!(casterStack.getItem() instanceof ICaster)) {
            casterStack = mc.player.getOffhandItem();
        }
        if (!(casterStack.getItem() instanceof ICaster wand)) {
            return;
        }
        if (fociItem.isEmpty() && !(wand.getFocusStack(casterStack).getItem() instanceof ItemFocus)) {
            return;
        }
        int mouseX = (int) (mc.mouseHandler.xpos()
                * mc.getWindow().getGuiScaledWidth()
                / mc.getWindow().getScreenWidth());
        int mouseY = (int) (mc.mouseHandler.ypos()
                * mc.getWindow().getGuiScaledHeight()
                / mc.getWindow().getScreenHeight());
        int cx = graphics.guiWidth() / 2;
        int cy = graphics.guiHeight() / 2;
        ItemStack tooltipStack = ItemStack.EMPTY;

        float width = BASE_RADIUS + fociItem.size() * RADIUS_PER_FOCUS;
        float ringAngle = partialTicks + mc.player.tickCount % RING_SPIN_PERIOD / 2.0F;
        drawRing(graphics, RADIAL_1, cx, cy, ringAngle, width * RING_1_SIZE * radialHudScale);
        drawRing(graphics, RADIAL_2, cx, cy, -ringAngle, width * RING_2_SIZE * radialHudScale);

        ItemStack socketed = wand.getFocusStack(casterStack);
        if (socketed.getItem() instanceof ItemFocus) {
            graphics.renderItem(socketed.copy(), cx - ITEM_HALF, cy - ITEM_HALF);
            int mx = mouseX - cx;
            int my = mouseY - cy;
            if (mx >= -HOVER_BOX && mx <= HOVER_BOX && my >= -HOVER_BOX && my <= HOVER_BOX) {
                tooltipStack = socketed;
                if (!CasterKeyHandler.radialLock && CasterKeyHandler.radialActive) {
                    centerHover = true;
                    if (pendingClick) {
                        CasterKeyHandler.radialActive = false;
                        CasterKeyHandler.radialLock = true;
                        PacketDistributor.sendToServer(new ServerboundFocusChangePayload(CasterManager.REMOVE_FOCUS));
                        if (mc.isWindowActive() && !mc.mouseHandler.isMouseGrabbed()) {
                            mc.mouseHandler.grabMouse();
                        }
                    }
                }
            } else {
                centerHover = false;
            }
        }

        float currentRot = -90.0F * radialHudScale;
        float pieSlice = fociItem.isEmpty() ? 0.0F : 360.0F / fociItem.size();
        String key = fociItem.isEmpty() ? null : foci.firstKey();
        for (int a = 0; a < fociItem.size() && key != null; a++) {
            double xx = Mth.cos(currentRot / 180.0F * (float) Math.PI) * width;
            double yy = Mth.sin(currentRot / 180.0F * (float) Math.PI) * width;
            currentRot += pieSlice;

            graphics.pose().pushPose();
            graphics.pose().translate(cx, cy, 0.0F);
            graphics.pose().scale(radialHudScale, radialHudScale, 1.0F);
            graphics.pose().translate((int) xx, (int) yy, 0.0F);
            float scale = fociScale.get(key);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.renderItem(fociItem.get(key).copy(), -ITEM_HALF, -ITEM_HALF);
            graphics.pose().popPose();

            if (!CasterKeyHandler.radialLock && CasterKeyHandler.radialActive) {
                int mx = (int) (mouseX - cx - xx);
                int my = (int) (mouseY - cy - yy);
                if (mx >= -HOVER_BOX && mx <= HOVER_BOX && my >= -HOVER_BOX && my <= HOVER_BOX) {
                    fociHover.put(key, true);
                    tooltipStack = fociItem.get(key);
                    if (pendingClick) {
                        CasterKeyHandler.radialActive = false;
                        CasterKeyHandler.radialLock = true;
                        PacketDistributor.sendToServer(new ServerboundFocusChangePayload(key));
                        if (mc.isWindowActive() && !mc.mouseHandler.isMouseGrabbed()) {
                            mc.mouseHandler.grabMouse();
                        }
                        break;
                    }
                } else {
                    fociHover.put(key, false);
                }
            }
            key = foci.higherKey(key);
        }

        if (!tooltipStack.isEmpty()) {
            graphics.renderTooltip(mc.font, tooltipStack, cx + TOOLTIP_X_OFFSET, cy + TOOLTIP_Y_OFFSET);
        }
    }

    private static void drawRing(
            GuiGraphics graphics, ResourceLocation texture, int cx, int cy, float angleDeg, float size) {
        if (size <= 0.0F) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0.0F);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(angleDeg - 90.0F));
        graphics.pose().translate(-size / 2.0F, -size / 2.0F, 0.0F);
        graphics.pose().scale(size / RADIAL_TEX_SIZE, size / RADIAL_TEX_SIZE, 1.0F);
        GuiBlend.blitTinted(
                graphics,
                texture,
                0,
                0,
                0.0F,
                0.0F,
                RADIAL_TEX_SIZE,
                RADIAL_TEX_SIZE,
                RADIAL_TEX_SIZE,
                RADIAL_TEX_SIZE,
                RING_TINT);
        graphics.pose().popPose();
    }
}
