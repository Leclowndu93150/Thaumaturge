package com.leclowndu93150.thaumaturge.client.lighting;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.misc.nitor.BlockNitor;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class NitorDynamicLights {
    private static final int LIGHT_LEVEL = 15;
    private static volatile Sources current = new Sources(null, Map.of());

    private NitorDynamicLights() {}

    public static int emission(Object engine, long blockNode) {
        Sources sources = current;
        return sources.engine == engine ? sources.positions.getOrDefault(blockNode, 0) : 0;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            clear();
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        Map<Long, Integer> positions = new HashMap<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living
                    && living.isAlive()
                    && !living.isSpectator()
                    && (isNitor(living.getMainHandItem()) || isNitor(living.getOffhandItem()))) {
                positions.put(
                        BlockPos.containing(living.getX(), living.getEyeY() - 0.3, living.getZ())
                                .asLong(),
                        LIGHT_LEVEL);
            } else if (entity instanceof ItemEntity item && item.isAlive() && isNitor(item.getItem())) {
                positions.put(
                        BlockPos.containing(item.getX(), item.getY() + 0.1, item.getZ())
                                .asLong(),
                        LIGHT_LEVEL);
            }
        }
        LayerLightEventListener engine = level.getLightEngine().getLayerListener(LightLayer.BLOCK);
        updateSources(engine, positions);
    }

    static void updateSources(LayerLightEventListener engine, Map<Long, Integer> positions) {
        Sources previous = current;
        current = new Sources(engine, Map.copyOf(positions));
        if (previous.engine == engine) {
            for (long position : previous.positions.keySet()) {
                if (!positions.containsKey(position)) {
                    engine.checkBlock(BlockPos.of(position));
                }
            }
        }
        // Recheck active sources too: incoming server light data can replace the client's light sections.
        for (long position : positions.keySet()) {
            engine.checkBlock(BlockPos.of(position));
        }
    }

    private static boolean isNitor(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item && item.getBlock() instanceof BlockNitor;
    }

    static void clear() {
        Sources previous = current;
        current = new Sources(null, Map.of());
        if (previous.engine != null) {
            for (long position : previous.positions.keySet()) {
                previous.engine.checkBlock(BlockPos.of(position));
            }
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    private record Sources(@Nullable LayerLightEventListener engine, Map<Long, Integer> positions) {}
}
