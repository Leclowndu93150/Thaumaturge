package com.leclowndu93150.thaumaturge.client.input;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.screen.research.ThaumonomiconBrowserScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCKeybinds {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(TCIds.rl("main"));
    public static final KeyMapping OPEN_THAUMONOMICON = new KeyMapping("key.thaumaturge.thaumonomicon", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY);
    public static final KeyMapping CHANGE_FOCUS = new KeyMapping("key.thaumaturge.change_focus", GLFW.GLFW_KEY_F, CATEGORY);
    public static final KeyMapping MISC_TOGGLE = new KeyMapping("key.thaumaturge.misc_toggle", GLFW.GLFW_KEY_G, CATEGORY);

    private TCKeybinds() {}

    @SubscribeEvent
    public static void onRegisterMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN_THAUMONOMICON);
        event.register(CHANGE_FOCUS);
        event.register(MISC_TOGGLE);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().screen != null)
            return;
        while (OPEN_THAUMONOMICON.consumeClick()) {
            Minecraft.getInstance().setScreen(new ThaumonomiconBrowserScreen());
        }
    }
}
