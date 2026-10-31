package com.leclowndu93150.thaumaturge.content.aura.relay;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayCapabilities;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.world.space.BlockSightline;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

public final class BlockEntityVisRelay extends AbstractSyncedBlockEntity {
    public static final int LINK_RANGE = 8;
    public static final int HOP_CAP = 16;

    public static final int PULSE_TICKS = 5;
    private static final int RELINK_INTERVAL = 40;
    private static final double LINK_REACH = LINK_RANGE * Math.sqrt(3.0) + 1.0;
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
        if (LevelSpaces.distanceSqr(level, Vec3.atCenterOf(worldPosition), Vec3.atCenterOf(parentPos))
                > LINK_REACH * LINK_REACH) {
            return false;
        }
        if (!level.isLoaded(parentPos)) {
            return true;
        }
        if (!sightClear(level, parentPos)) {
            return false;
        }
        if (level.getBlockEntity(parentPos) instanceof BlockEntityVisRelay relay) {
            return relay.isLinked() && relay.depth == depth - 1 && depth <= HOP_CAP;
        }
        return depth == 1 && usableSource(level, parentPos) != null;
    }

    private void relink(ServerLevel level) {
        LinkSearch search = new LinkSearch();
        scanAround(level, worldPosition, search, false);
        for (BlockPos center : LevelSpaces.foreignCentersNear(level, worldPosition, LINK_REACH)) {
            scanAround(level, center, search, true);
        }
        if (search.bestSource != null) {
            parentPos = search.bestSource;
            depth = 1;
        } else if (search.bestRelay != null) {
            parentPos = search.bestRelay;
            depth = search.bestRelayDepth + 1;
        }
    }

    private void scanAround(ServerLevel level, BlockPos center, LinkSearch search, boolean foreign) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -LINK_RANGE; x <= LINK_RANGE; x++) {
            for (int y = -LINK_RANGE; y <= LINK_RANGE; y++) {
                for (int z = -LINK_RANGE; z <= LINK_RANGE; z++) {
                    cursor.setWithOffset(center, x, y, z);
                    if (cursor.equals(worldPosition)
                            || (foreign
                                    && (!level.isLoaded(cursor)
                                            || !LevelSpaces.withinCube(level, worldPosition, cursor, LINK_RANGE)))) {
                        continue;
                    }
                    consider(level, cursor, search);
                }
            }
        }
    }

    private void consider(ServerLevel level, BlockPos cursor, LinkSearch search) {
        if (level.getBlockEntity(cursor) instanceof BlockEntityVisRelay relay) {
            double distance = linkDistance(level, cursor);
            if (relay.isLinked()
                    && relay.depth < HOP_CAP
                    && (relay.depth < search.bestRelayDepth
                            || (relay.depth == search.bestRelayDepth && distance < search.bestRelayDistance))
                    && sightClear(level, cursor)) {
                search.bestRelayDepth = relay.depth;
                search.bestRelayDistance = distance;
                search.bestRelay = cursor.immutable();
            }
        } else if (usableSource(level, cursor) != null) {
            double distance = linkDistance(level, cursor);
            if (distance < search.bestSourceDistance && sightClear(level, cursor)) {
                search.bestSourceDistance = distance;
                search.bestSource = cursor.immutable();
            }
        }
    }

    private double linkDistance(Level level, BlockPos target) {
        return LevelSpaces.distanceSqr(level, Vec3.atCenterOf(worldPosition), Vec3.atCenterOf(target));
    }

    private boolean sightClear(Level level, BlockPos target) {
        return BlockSightline.isClear(
                level,
                Vec3.atCenterOf(worldPosition),
                Vec3.atCenterOf(target),
                worldPosition,
                target,
                LINK_REACH,
                CollisionContext.empty());
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

    private static final class LinkSearch {
        private @Nullable BlockPos bestSource;
        private double bestSourceDistance = Double.MAX_VALUE;
        private @Nullable BlockPos bestRelay;
        private int bestRelayDepth = Integer.MAX_VALUE;
        private double bestRelayDistance = Double.MAX_VALUE;
    }
}
