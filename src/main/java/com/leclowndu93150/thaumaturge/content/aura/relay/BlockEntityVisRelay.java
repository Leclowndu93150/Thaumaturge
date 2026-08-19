package com.leclowndu93150.thaumaturge.content.aura.relay;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityVisRelay extends BlockEntity {
    public static final int LINK_RANGE = 8;
    public static final int HOP_CAP = 16;

    private static final int RELINK_INTERVAL = 40;

    private @Nullable BlockPos parentPos;
    private int depth;

    public BlockEntityVisRelay(BlockPos pos, BlockState state) {
        super(TCBlockEntities.VIS_RELAY.get(), pos, state);
    }

    public @Nullable BlockPos parentPos() {
        return parentPos;
    }

    public int depth() {
        return depth;
    }

    public boolean isLinked() {
        return parentPos != null && depth > 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityVisRelay relay) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if ((level.getGameTime() + pos.hashCode()) % RELINK_INTERVAL != 0) {
            return;
        }
        relay.refreshLink(serverLevel);
    }

    public void refreshLink(ServerLevel level) {
        BlockPos previousParent = parentPos;
        int previousDepth = depth;
        if (!isLinkValid(level)) {
            parentPos = null;
            depth = 0;
            relink(level);
        }
        if (!java.util.Objects.equals(previousParent, parentPos) || previousDepth != depth) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private boolean isLinkValid(ServerLevel level) {
        if (parentPos == null || depth <= 0) {
            return false;
        }
        if (!level.isLoaded(parentPos)) {
            return true;
        }
        BlockEntity parent = level.getBlockEntity(parentPos);
        if (parent instanceof BlockEntityNode node) {
            return depth == 1 && sourceUsable(node);
        }
        if (parent instanceof BlockEntityVisRelay relay) {
            return relay.isLinked() && relay.depth == depth - 1 && depth <= HOP_CAP;
        }
        return false;
    }

    private void relink(ServerLevel level) {
        BlockPos bestNode = null;
        double bestNodeDistance = Double.MAX_VALUE;
        BlockPos bestRelay = null;
        int bestRelayDepth = Integer.MAX_VALUE;
        double bestRelayDistance = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -LINK_RANGE; x <= LINK_RANGE; x++) {
            for (int y = -LINK_RANGE; y <= LINK_RANGE; y++) {
                for (int z = -LINK_RANGE; z <= LINK_RANGE; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    cursor.setWithOffset(worldPosition, x, y, z);
                    BlockEntity be = level.getBlockEntity(cursor);
                    if (be instanceof BlockEntityNode node && sourceUsable(node)) {
                        double distance = cursor.distSqr(worldPosition);
                        if (distance < bestNodeDistance) {
                            bestNodeDistance = distance;
                            bestNode = cursor.immutable();
                        }
                    } else if (be instanceof BlockEntityVisRelay relay && relay.isLinked() && relay.depth < HOP_CAP) {
                        double distance = cursor.distSqr(worldPosition);
                        if (relay.depth < bestRelayDepth || (relay.depth == bestRelayDepth && distance < bestRelayDistance)) {
                            bestRelayDepth = relay.depth;
                            bestRelayDistance = distance;
                            bestRelay = cursor.immutable();
                        }
                    }
                }
            }
        }
        if (bestNode != null) {
            parentPos = bestNode;
            depth = 1;
        } else if (bestRelay != null) {
            parentPos = bestRelay;
            depth = bestRelayDepth + 1;
        }
    }

    private static boolean sourceUsable(BlockEntityNode node) {
        return node.isEnergized() && !(node instanceof BlockEntityJarNode);
    }

    public @Nullable BlockEntityNode resolveSource(ServerLevel level) {
        BlockEntityVisRelay current = this;
        for (int hops = 0; hops <= HOP_CAP; hops++) {
            BlockPos parent = current.parentPos;
            if (parent == null) {
                return null;
            }
            BlockEntity be = level.getBlockEntity(parent);
            if (be instanceof BlockEntityNode node) {
                return sourceUsable(node) ? node : null;
            }
            if (!(be instanceof BlockEntityVisRelay relay)) {
                return null;
            }
            current = relay;
        }
        return null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (parentPos != null) {
            output.store("Parent", BlockPos.CODEC, parentPos);
            output.putInt("Depth", depth);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        parentPos = input.read("Parent", BlockPos.CODEC).orElse(null);
        depth = input.getIntOr("Depth", 0);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector collector = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(collector, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
