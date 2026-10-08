package com.leclowndu93150.thaumaturge.content.aura.pressure;

import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeLocationIndex;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

public final class NodeMutationPressureEvent extends AbstractFluxPressureEvent {
    private static final String NAME = "node_mutation";
    private static final int WEIGHT = 3;
    private static final float COST = 20.0F;
    private static final boolean ALLOWED_NEAR_TAINT = true;
    private static final double RANGE = 16.0;
    private static final NodeType[] MUTATIONS = {
        NodeType.NORMAL, NodeType.UNSTABLE, NodeType.DARK, NodeType.TAINTED, NodeType.HUNGRY
    };

    public NodeMutationPressureEvent() {
        super(NAME, WEIGHT, COST, ALLOWED_NEAR_TAINT);
    }

    @Override
    public boolean fire(ServerLevel level, BlockPos origin, FluxPressureState state) {
        Optional<BlockPos> nearest = NodeLocationIndex.get(level).findNearestAny(origin, RANGE);
        if (nearest.isEmpty()
                || !level.hasChunkAt(nearest.get())
                || !(level.getBlockEntity(nearest.get()) instanceof BlockEntityNode node)) {
            return false;
        }
        RandomSource random = level.getRandom();
        node.setNodeType(random.nextBoolean() ? NodeType.TAINTED : MUTATIONS[random.nextInt(MUTATIONS.length)]);
        node.nodeChange();
        return true;
    }
}
