package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.casters.CasterBlockAction;
import com.leclowndu93150.thaumaturge.api.casters.CasterBlockActions;
import com.leclowndu93150.thaumaturge.api.casters.CasterBlockUse;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID)
public final class NodeJarTriggers implements CasterBlockAction {
    private NodeJarTriggers() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        NodeJarTriggers action = new NodeJarTriggers();
        CasterBlockActions.onTag(Tags.Blocks.GLASS_BLOCKS, action);
        CasterBlockActions.onState(TTBlocks.WARDED_GLASS.get().defaultBlockState(), action);
    }

    @Override
    public boolean handle(CasterBlockUse use) {
        BlockPos nodePos = findEnclosedNode(use.level(), use.pos());
        if (nodePos == null) {
            return false;
        }
        if (!(use.level() instanceof ServerLevel serverLevel)) {
            return true;
        }
        return NodeJarRitual.tryJarNode(serverLevel, nodePos, use.player());
    }

    private static @Nullable BlockPos findEnclosedNode(Level level, BlockPos clicked) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    BlockPos candidate = clicked.offset(dx, dy, dz);
                    if (level.getBlockEntity(candidate) instanceof BlockEntityNode node
                            && !(node instanceof BlockEntityJarNode)
                            && NodeJarRitual.fitsStructure(level, candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }
}
