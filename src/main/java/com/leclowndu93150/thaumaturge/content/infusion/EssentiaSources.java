package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectSource;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EssentiaSources {
    private static final int DEFAULT_RANGE = 12;
    private static final int RETRY_DELAY_TICKS = 200;

    private final BlockPos center;
    private final int range;
    private Predicate<BlockPos> sourceFilter = pos -> true;
    private @Nullable Direction facing;
    private @Nullable Predicate<BlockEntity> ignored;
    private @Nullable Vec3 drainEffectTarget;
    private @Nullable List<BlockPos> sources;
    private long retryAt;

    public EssentiaSources(BlockPos center) {
        this(center, DEFAULT_RANGE);
    }

    public EssentiaSources(BlockPos center, int range) {
        this.center = center.immutable();
        this.range = range;
    }

    public void invalidate() {
        this.sources = null;
        this.retryAt = 0L;
    }

    public EssentiaSources drainEffectTarget(Vec3 target) {
        this.drainEffectTarget = target;
        return this;
    }

    public EssentiaSources sourceFilter(Predicate<BlockPos> filter) {
        this.sourceFilter = filter;
        invalidate();
        return this;
    }

    public EssentiaSources facing(Direction direction) {
        this.facing = direction;
        invalidate();
        return this;
    }

    public EssentiaSources ignoring(Predicate<BlockEntity> filter) {
        this.ignored = filter;
        invalidate();
        return this;
    }

    public boolean drain(ServerLevel level, Holder<IAspect> aspect, int fxExtendTicks) {
        if (level.getGameTime() < retryAt) {
            return false;
        }
        if (sources == null) {
            sources = scan(level);
        }
        for (BlockPos sourcePos : sources) {
            IAspectContainer container = level.getCapability(AspectCapabilities.CONTAINER, sourcePos, null);
            if (container instanceof IAspectSource source
                    && !source.isBlocked()
                    && source.takeFromContainer(aspect, 1)) {
                BlockEntity be = level.getBlockEntity(sourcePos);
                if (be != null) be.setChanged();
                EffectDispatch.spawnEssentiaStream(
                        level,
                        Vec3.atCenterOf(sourcePos),
                        drainEffectTarget != null ? drainEffectTarget : Vec3.atCenterOf(center.below()),
                        aspect.value().color(),
                        0,
                        level.getRandom().nextInt(8),
                        0.1F,
                        fxExtendTicks,
                        0.0);
                return true;
            }
        }
        sources = null;
        retryAt = level.getGameTime() + RETRY_DELAY_TICKS;
        return false;
    }

    public boolean insert(ServerLevel level, Holder<IAspect> aspect, int fxExtendTicks) {
        if (level.getGameTime() < retryAt) {
            return false;
        }
        if (sources == null) {
            sources = scan(level);
        }
        for (BlockPos sourcePos : sources) {
            IAspectContainer container = level.getCapability(AspectCapabilities.CONTAINER, sourcePos, null);
            if (container instanceof IAspectSource source
                    && !source.isBlocked()
                    && source.doesContainerAccept(aspect)
                    && source.addToContainer(aspect, 1) == 0) {
                BlockEntity be = level.getBlockEntity(sourcePos);
                if (be != null) be.setChanged();
                EffectDispatch.spawnEssentiaStream(
                        level,
                        Vec3.atCenterOf(center),
                        Vec3.atCenterOf(sourcePos),
                        aspect.value().color(),
                        0,
                        level.getRandom().nextInt(8),
                        0.1F,
                        fxExtendTicks,
                        0.0);
                return true;
            }
        }
        sources = null;
        retryAt = level.getGameTime() + RETRY_DELAY_TICKS;
        return false;
    }

    private List<BlockPos> scan(ServerLevel level) {
        List<BlockPos> found = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int alongMin = facing == null ? -range : 0;
        int alongMax = facing == null ? range : range - 1;
        for (int across = -range; across <= range; across++) {
            for (int up = -range; up <= range; up++) {
                for (int along = alongMin; along <= alongMax; along++) {
                    if (across == 0 && up == 0 && along == 0) {
                        continue;
                    }
                    place(cursor, across, up, along);
                    if (!sourceFilter.test(cursor) || !level.isLoaded(cursor)) {
                        continue;
                    }
                    IAspectContainer container = level.getCapability(AspectCapabilities.CONTAINER, cursor, null);
                    if (container instanceof IAspectSource && !isIgnored(level, cursor)) {
                        found.add(cursor.immutable());
                    }
                }
            }
        }
        found.sort((a, b) -> Double.compare(a.distSqr(center), b.distSqr(center)));
        return found;
    }

    private boolean isIgnored(ServerLevel level, BlockPos pos) {
        if (ignored == null) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && ignored.test(blockEntity);
    }

    private void place(BlockPos.MutableBlockPos cursor, int across, int up, int along) {
        if (facing == null) {
            cursor.set(center.getX() + across, center.getY() + along, center.getZ() + up);
            return;
        }
        if (facing.getStepY() != 0) {
            cursor.set(center.getX() + across, center.getY() + along * facing.getStepY(), center.getZ() + up);
            return;
        }
        if (facing.getStepX() == 0) {
            cursor.set(center.getX() + across, center.getY() + up, center.getZ() + along * facing.getStepZ());
            return;
        }
        cursor.set(center.getX() + along * facing.getStepX(), center.getY() + across, center.getZ() + up);
    }
}
