package com.leclowndu93150.thaumaturge.debug.client;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanRaycastHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class RaycastDebugOverlay implements GuiLayer {

    public static @Nullable HitResult serverHitResult = null;
    private static boolean enabled = false;
    private static @Nullable HitResult clientHitResult = null;

    private static String formatResult(HitResult result) {
        if (result == null)
            return "Null";
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return "Level Null ?????";
        }
        if (result.getType() == HitResult.Type.MISS)
            return "Miss";
        return switch (result) {
            case BlockHitResult block -> "Block : " + block.getBlockPos().toShortString() + " | " + mc.level.getBlockState(block.getBlockPos()).getBlock().builtInRegistryHolder().key().identifier()
                    + " | " + block.getDirection();
            case EntityHitResult entity ->
                "Entity : " + entity.getEntity().getName().getString() + " | " + entity.getEntity().getType().builtInRegistryHolder().key().identifier() + " | " + entity.getLocation();
            default -> "Invalid : " + result.getType() + " | " + result.getLocation();
        };
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        clientHitResult = ScanRaycastHelper.performRaycast(player);
    }

    public static void toggle() {
        enabled = !enabled;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!enabled)
            return;
        graphics.text(Minecraft.getInstance().font, "Client Raycast : " + formatResult(clientHitResult), 2, 2, 0xFFFFFFFF);
        graphics.text(Minecraft.getInstance().font, "Server Raycast : " + formatResult(serverHitResult), 2, 12, 0xFFFFFFFF);
    }
}
