package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.casters.CasterTriggerRegistry;
import com.leclowndu93150.thaumaturge.api.casters.ICasterTriggerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TCIds.MODID)
public final class NodeJarTriggers implements ICasterTriggerManager {
    private static final int EVENT_JAR_NODE = 0;

    private NodeJarTriggers() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        CasterTriggerRegistry.registerCasterBlockTagTrigger(new NodeJarTriggers(), EVENT_JAR_NODE, Tags.Blocks.GLASS_BLOCKS);
    }

    @Override
    public boolean performTrigger(Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, int event) {
        BlockPos nodePos = findEnclosedNode(level, pos);
        if (nodePos == null) {
            return false;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return true;
        }
        return NodeJarRitual.tryJarNode(serverLevel, nodePos, player);
    }

    private static @Nullable BlockPos findEnclosedNode(Level level, BlockPos clicked) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -2; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    BlockPos candidate = clicked.offset(dx, dy, dz);
                    if (level.getBlockEntity(candidate) instanceof BlockEntityNode node && !(node instanceof BlockEntityJarNode)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }
}
