package com.leclowndu93150.thaumaturge.client.casters;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.client.input.TCKeybinds;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.network.ServerboundCasterKeyPayload;
import com.leclowndu93150.thaumaturge.network.ServerboundFocusChangePayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class CasterKeyHandler {
    private static final int MOD_GROW = 0;
    private static final int MOD_CYCLE_DIM = 1;
    private static final int MOD_SHIFT = 2;

    private static boolean keyPressedF;
    private static boolean keyPressedG;
    static boolean radialActive;
    static boolean radialLock;

    private CasterKeyHandler() {}

    public static boolean isRadialActive() {
        return radialActive;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        boolean holdingCaster = player.getMainHandItem().getItem() instanceof ICaster || player.getOffhandItem().getItem() instanceof ICaster;
        if (mc.screen == null && holdingCaster && TCKeybinds.CHANGE_FOCUS.same(mc.options.keySwapOffhand)) {
            boolean drained = mc.options.keySwapOffhand.consumeClick();
            while (drained) {
                drained = mc.options.keySwapOffhand.consumeClick();
            }
        }
        boolean inGame = mc.screen == null && (mc.mouseHandler.isMouseGrabbed() || radialActive || RadialFocusOverlay.isAnimating());
        if (TCKeybinds.CHANGE_FOCUS.isDown()) {
            if (inGame) {
                if (!keyPressedF) {
                    radialLock = false;
                }
                if (!radialLock && (player.getMainHandItem().getItem() instanceof ICaster || player.getOffhandItem().getItem() instanceof ICaster)) {
                    if (player.isShiftKeyDown()) {
                        ClientPacketDistributor.sendToServer(new ServerboundFocusChangePayload(CasterManager.REMOVE_FOCUS));
                    } else {
                        radialActive = true;
                    }
                }
                keyPressedF = true;
            }
        } else {
            radialActive = false;
            keyPressedF = false;
        }
        if (TCKeybinds.MISC_TOGGLE.isDown()) {
            if (mc.screen == null && mc.mouseHandler.isMouseGrabbed()) {
                if (!keyPressedG) {
                    int mod = InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)
                            ? MOD_CYCLE_DIM
                            : InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) ? MOD_SHIFT : MOD_GROW;
                    ClientPacketDistributor.sendToServer(new ServerboundCasterKeyPayload(mod));
                }
                keyPressedG = true;
            }
        } else {
            keyPressedG = false;
        }
    }
}
