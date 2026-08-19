package com.leclowndu93150.thaumaturge.content.aura.relay;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayHelper;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public final class VisRelayNetwork implements VisRelayHelper.Bindings {
    public static final int CONSUMER_RANGE = 8;

    @Override
    public int drainCentivis(ServerLevel level, BlockPos consumerPos, ResourceKey<IAspect> primal, int amount, boolean simulate) {
        BlockEntityNode source = findSource(level, consumerPos);
        if (source == null) {
            return 0;
        }
        Holder<IAspect> aspect = Aspects.resolve(level.registryAccess(), primal);
        if (aspect == null) {
            return 0;
        }
        if (simulate) {
            return Math.min(amount, source.availableCentivis(aspect));
        }
        return source.drainCentivis(aspect, amount);
    }

    public static @Nullable BlockEntityNode findSource(ServerLevel level, BlockPos consumerPos) {
        BlockEntityVisRelay relay = findRelayNear(level, consumerPos);
        return relay == null ? null : relay.resolveSource(level);
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
