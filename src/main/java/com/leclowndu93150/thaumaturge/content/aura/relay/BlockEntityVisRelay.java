package com.leclowndu93150.thaumaturge.content.aura.relay;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayCapabilities;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEntityVisRelay extends BlockEntity {
    public static final int LINK_RANGE = 8;
    public static final int HOP_CAP = 16;

    public static final int PULSE_TICKS = 5;
    private static final int RELINK_INTERVAL = 40;
    private static final int PULSE_EVENT = 0;
    private static final long NO_PULSE = -1000L;
    private static final int WHITE = 0xFFFFFF;

    private @Nullable BlockPos parentPos;
    private int depth;
    private long lastPulseSent = NO_PULSE;
    private long pulseStart = NO_PULSE;
    private int pulseColor = WHITE;

    public BlockEntityVisRelay(BlockPos pos, BlockState state) {
        super(TTBlockEntities.VIS_RELAY.get(), pos, state);
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

    public long pulseStart() {
        return pulseStart;
    }

    public int pulseColor() {
        return pulseColor;
    }

    public void triggerConsumeEffect(ServerLevel level, Holder<IAspect> aspect) {
        long now = level.getGameTime();
        if (!aspect.value().isPrimal() || now - lastPulseSent < PULSE_TICKS) {
            return;
        }
        lastPulseSent = now;
        level.blockEvent(
                worldPosition,
                getBlockState().getBlock(),
                PULSE_EVENT,
                aspect.value().color());
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id != PULSE_EVENT) {
            return super.triggerEvent(id, param);
        }
        if (level != null && level.isClientSide()) {
            long now = level.getGameTime();
            startPulse(param, now);
            BlockPos next = parentPos;
            for (int hop = 0;
                    next != null
                            && hop < HOP_CAP
                            && level.getBlockEntity(next) instanceof BlockEntityVisRelay parent
                            && !parent.isPulsing(now);
                    hop++) {
                parent.startPulse(param, now);
                next = parent.parentPos;
            }
        }
        return true;
    }

    private void startPulse(int color, long now) {
        pulseColor = color;
        pulseStart = now;
    }

    private boolean isPulsing(long now) {
        return now - pulseStart < PULSE_TICKS;
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
        if (!Objects.equals(previousParent, parentPos) || previousDepth != depth) {
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
        if (!RelaySightline.isClear(level, worldPosition, parentPos)) {
            return false;
        }
        if (level.getBlockEntity(parentPos) instanceof BlockEntityVisRelay relay) {
            return relay.isLinked() && relay.depth == depth - 1 && depth <= HOP_CAP;
        }
        return depth == 1 && usableSource(level, parentPos) != null;
    }

    private void relink(ServerLevel level) {
        BlockPos bestSource = null;
        double bestSourceDistance = Double.MAX_VALUE;
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
                    if (level.getBlockEntity(cursor) instanceof BlockEntityVisRelay relay) {
                        double distance = cursor.distSqr(worldPosition);
                        if (relay.isLinked()
                                && relay.depth < HOP_CAP
                                && (relay.depth < bestRelayDepth
                                        || (relay.depth == bestRelayDepth && distance < bestRelayDistance))
                                && RelaySightline.isClear(level, worldPosition, cursor)) {
                            bestRelayDepth = relay.depth;
                            bestRelayDistance = distance;
                            bestRelay = cursor.immutable();
                        }
                    } else if (usableSource(level, cursor) != null) {
                        double distance = cursor.distSqr(worldPosition);
                        if (distance < bestSourceDistance && RelaySightline.isClear(level, worldPosition, cursor)) {
                            bestSourceDistance = distance;
                            bestSource = cursor.immutable();
                        }
                    }
                }
            }
        }
        if (bestSource != null) {
            parentPos = bestSource;
            depth = 1;
        } else if (bestRelay != null) {
            parentPos = bestRelay;
            depth = bestRelayDepth + 1;
        }
    }

    private static @Nullable IVisRelaySource usableSource(ServerLevel level, BlockPos pos) {
        IVisRelaySource source = level.getCapability(VisRelayCapabilities.SOURCE, pos, null);
        return source != null && source.canSupply() ? source : null;
    }

    public @Nullable LinkedRelaySource resolveSource(ServerLevel level) {
        BlockEntityVisRelay current = this;
        for (int hops = 0; hops <= HOP_CAP; hops++) {
            BlockPos parent = current.parentPos;
            if (parent == null) {
                return null;
            }
            if (level.getBlockEntity(parent) instanceof BlockEntityVisRelay relay) {
                current = relay;
                continue;
            }
            IVisRelaySource source = usableSource(level, parent);
            return source == null ? null : new LinkedRelaySource(parent, source);
        }
        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (parentPos != null) {
            TTNbt.store(output, "Parent", BlockPos.CODEC, registries, parentPos);
            output.putInt("Depth", depth);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        parentPos = TTNbt.read(input, "Parent", BlockPos.CODEC, registries).orElse(null);
        depth = input.getInt("Depth");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
