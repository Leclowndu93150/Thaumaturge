package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectSource;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import java.util.ArrayList;
import java.util.Comparator;
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
    private static final long RETRY_DELAY_TICKS = 200L;
    private static final int STREAM_TYPE_TAG = 0;
    private static final int STREAM_COUNT_BOUND = 8;
    private static final float STREAM_SCALE = 0.1F;
    private static final double STREAM_DRIFT = 0.0;
    private static final int UNIT = 1;
    private static final double FACING_SLACK = 0.5;

    private final BlockPos owner;
    private final int range;
    private @Nullable Direction facing;
    private @Nullable Predicate<BlockEntity> ignore;
    private @Nullable Vec3 drainTarget;
    private @Nullable List<BlockPos> cache;
    private long retryAfter;

    public EssentiaSources(BlockPos owner) {
        this(owner, DEFAULT_RANGE);
    }

    public EssentiaSources(BlockPos owner, int range) {
        this.owner = owner.immutable();
        this.range = range;
    }

    public void invalidate() {
        cache = null;
        retryAfter = 0L;
    }

    public EssentiaSources facing(Direction facing) {
        this.facing = facing;
        return this;
    }

    public EssentiaSources ignoring(Predicate<BlockEntity> ignore) {
        this.ignore = ignore;
        return this;
    }

    public EssentiaSources drainEffectTarget(Vec3 target) {
        this.drainTarget = target;
        return this;
    }

    public boolean drain(ServerLevel level, Holder<IAspect> aspect, int extend) {
        long now = level.getGameTime();
        if (now < retryAfter) {
            return false;
        }
        List<Candidate> candidates = candidates(level, aspect);
        candidates.sort(
                Comparator.comparingInt(Candidate::priority).reversed().thenComparingDouble(Candidate::distance));
        for (Candidate candidate : candidates) {
            if (candidate.source().isBlocked() || !candidate.source().drain(aspect, UNIT)) {
                continue;
            }
            markChanged(level, candidate.pos());
            Vec3 target = drainTarget != null ? drainTarget : Vec3.atCenterOf(owner.below());
            stream(
                    level,
                    Vec3.atCenterOf(candidate.pos()).add(candidate.source().drainEffectOffset()),
                    target,
                    aspect,
                    extend);
            return true;
        }
        return fail(now);
    }

    public boolean insert(ServerLevel level, Holder<IAspect> aspect, int extend) {
        long now = level.getGameTime();
        if (now < retryAfter) {
            return false;
        }
        List<Candidate> candidates = new ArrayList<>();
        for (Candidate candidate : candidates(level, aspect)) {
            if (!candidate.source().isBlocked() && candidate.source().accepts(aspect)) {
                candidates.add(candidate);
            }
        }
        candidates.sort(Comparator.comparingInt(Candidate::priority)
                .reversed()
                .thenComparing(Comparator.comparingInt(Candidate::suction).reversed())
                .thenComparing(Comparator.comparing(Candidate::empty))
                .thenComparingDouble(Candidate::distance));
        for (Candidate candidate : candidates) {
            if (candidate.source().fill(aspect, UNIT) != 0) {
                continue;
            }
            markChanged(level, candidate.pos());
            stream(level, Vec3.atCenterOf(owner), Vec3.atCenterOf(candidate.pos()), aspect, extend);
            return true;
        }
        return fail(now);
    }

    private boolean fail(long now) {
        cache = null;
        retryAfter = now + RETRY_DELAY_TICKS;
        return false;
    }

    private void stream(ServerLevel level, Vec3 from, Vec3 to, Holder<IAspect> aspect, int extend) {
        EffectDispatch.spawnEssentiaStream(
                level,
                from,
                to,
                aspect.value().color(),
                STREAM_TYPE_TAG,
                level.getRandom().nextInt(STREAM_COUNT_BOUND),
                STREAM_SCALE,
                extend,
                STREAM_DRIFT);
    }

    private static void markChanged(ServerLevel level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity != null) {
            entity.setChanged();
        }
    }

    private List<Candidate> candidates(ServerLevel level, Holder<IAspect> aspect) {
        if (cache == null) {
            cache = scan(level);
        }
        List<Candidate> candidates = new ArrayList<>(cache.size());
        for (BlockPos pos : cache) {
            if (LoadedChunks.contains(level, pos)
                    && LevelSpaces.withinCube(level, owner, pos, range)
                    && level.getCapability(AspectCapabilities.CONTAINER, pos, null) instanceof IAspectSource source) {
                candidates.add(new Candidate(
                        pos,
                        source,
                        source.getSourcePriority(),
                        suction(level, pos, aspect),
                        source.getAspects().isEmpty(),
                        LevelSpaces.distanceSqr(level, Vec3.atCenterOf(owner), Vec3.atCenterOf(pos))));
            }
        }
        return candidates;
    }

    private static int suction(ServerLevel level, BlockPos pos, Holder<IAspect> aspect) {
        IEssentiaConduit conduit = EssentiaAccess.conduit(level, pos, null);
        if (conduit == null) {
            return 0;
        }
        EssentiaPull pull = conduit.pull(null);
        return aspect.equals(pull.aspect()) ? pull.strength() : 0;
    }

    private List<BlockPos> scan(ServerLevel level) {
        int minX = owner.getX() - range;
        int maxX = owner.getX() + range;
        int minY = owner.getY() - range;
        int maxY = owner.getY() + range;
        int minZ = owner.getZ() - range;
        int maxZ = owner.getZ() + range;
        if (facing != null) {
            int step = facing.getAxisDirection().getStep();
            int near = 0;
            int far = step * (range - 1);
            int low = owner.get(facing.getAxis()) + Math.min(near, far);
            int high = owner.get(facing.getAxis()) + Math.max(near, far);
            switch (facing.getAxis()) {
                case X -> {
                    minX = low;
                    maxX = high;
                }
                case Y -> {
                    minY = low;
                    maxY = high;
                }
                case Z -> {
                    minZ = low;
                    maxZ = high;
                }
            }
        }
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (!pos.equals(owner) && isSource(level, pos)) {
                found.add(pos.immutable());
            }
        }
        for (BlockPos center : LevelSpaces.foreignCentersNear(level, owner, range * Math.sqrt(3.0))) {
            for (BlockPos pos :
                    BlockPos.betweenClosed(center.offset(-range, -range, -range), center.offset(range, range, range))) {
                if (LevelSpaces.withinCube(level, owner, pos, range) && inFront(level, pos) && isSource(level, pos)) {
                    found.add(pos.immutable());
                }
            }
        }
        return found;
    }

    private boolean isSource(ServerLevel level, BlockPos pos) {
        return LoadedChunks.contains(level, pos)
                && level.getCapability(AspectCapabilities.CONTAINER, pos, null) instanceof IAspectSource
                && !rejected(level, pos);
    }

    private boolean inFront(ServerLevel level, BlockPos pos) {
        if (facing == null) {
            return true;
        }
        double ahead = LevelSpaces.worldCenter(level, pos)
                .subtract(LevelSpaces.worldCenter(level, owner))
                .dot(LevelSpaces.faceNormal(level, owner, facing));
        return ahead >= -FACING_SLACK && ahead <= range - 1 + FACING_SLACK;
    }

    private boolean rejected(ServerLevel level, BlockPos pos) {
        if (ignore == null) {
            return false;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && ignore.test(entity);
    }

    private record Candidate(
            BlockPos pos, IAspectSource source, int priority, int suction, boolean empty, double distance) {}
}
