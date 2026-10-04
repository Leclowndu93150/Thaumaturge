package com.leclowndu93150.thaumaturge.content.aura.relay;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayHelper;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public final class VisRelayNetwork implements VisRelayHelper.Bindings {
    public static final int CONSUMER_RANGE = 8;

    @Override
    public int drainCentivis(
            ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        BlockEntityVisRelay relay = findRelayNear(level, consumerPos);
        LinkedRelaySource source = relay == null ? null : relay.resolveSource(level);
        Holder<IAspect> aspect = Aspects.resolve(level.registryAccess(), primal);
        if (source == null || aspect == null) {
            return 0;
        }
        if (simulate) {
            return clamp(source.source().availableCentivis(primal), amount);
        }
        int drained = drainNow(source, primal, amount);
        if (drained > 0) {
            relay.triggerConsumeEffect(level, aspect);
        }
        return drained;
    }

    public static int drainNow(LinkedRelaySource source, ResourceKey<IAspect> primal, int amount) {
        if (amount <= 0) {
            return 0;
        }
        return clamp(source.source().drainCentivis(primal, amount, false), amount);
    }

    public static int drainEverySourceNear(
            ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount) {
        if (amount <= 0) {
            return 0;
        }
        Holder<IAspect> aspect = Aspects.resolve(level.registryAccess(), primal);
        Set<BlockPos> drainedSources = null;
        int drained = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -CONSUMER_RANGE; drained < amount && x <= CONSUMER_RANGE; x++) {
            for (int y = -CONSUMER_RANGE; drained < amount && y <= CONSUMER_RANGE; y++) {
                for (int z = -CONSUMER_RANGE; drained < amount && z <= CONSUMER_RANGE; z++) {
                    cursor.setWithOffset(consumerPos, x, y, z);
                    if (!(level.getBlockEntity(cursor) instanceof BlockEntityVisRelay relay) || !relay.isLinked()) {
                        continue;
                    }
                    LinkedRelaySource source = relay.resolveSource(level);
                    if (source == null) {
                        continue;
                    }
                    if (drainedSources == null) {
                        drainedSources = new HashSet<>();
                    }
                    if (!drainedSources.add(source.position())) {
                        continue;
                    }
                    int taken = drainNow(source, primal, amount - drained);
                    if (taken > 0) {
                        drained += taken;
                        if (aspect != null) {
                            relay.triggerConsumeEffect(level, aspect);
                        }
                    }
                }
            }
        }
        return drained;
    }

    public static int drainNodesNear(ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount) {
        int drained = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -CONSUMER_RANGE; drained < amount && x <= CONSUMER_RANGE; x++) {
            for (int y = -CONSUMER_RANGE; drained < amount && y <= CONSUMER_RANGE; y++) {
                for (int z = -CONSUMER_RANGE; drained < amount && z <= CONSUMER_RANGE; z++) {
                    cursor.setWithOffset(consumerPos, x, y, z);
                    if (!level.isLoaded(cursor)
                            || !(level.getBlockEntity(cursor) instanceof BlockEntityNode node)
                            || node instanceof BlockEntityJarNode) {
                        continue;
                    }
                    drained += drainNow(
                            new LinkedRelaySource(cursor.immutable(), node.relaySource()), primal, amount - drained);
                }
            }
        }
        return drained;
    }

    private static int clamp(int supplied, int amount) {
        return Math.max(0, Math.min(amount, supplied));
    }

    public static @Nullable BlockEntityVisRelay findRelayNear(ServerLevel level, BlockPos consumerPos) {
        BlockEntityVisRelay best = null;
        double bestDistance = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -CONSUMER_RANGE; x <= CONSUMER_RANGE; x++) {
            for (int y = -CONSUMER_RANGE; y <= CONSUMER_RANGE; y++) {
                for (int z = -CONSUMER_RANGE; z <= CONSUMER_RANGE; z++) {
                    cursor.setWithOffset(consumerPos, x, y, z);
                    BlockEntity be = level.getBlockEntity(cursor);
                    if (be instanceof BlockEntityVisRelay relay && relay.isLinked()) {
                        double distance = cursor.distSqr(consumerPos);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = relay;
                        }
                    }
                }
            }
        }
        return best;
    }
}
