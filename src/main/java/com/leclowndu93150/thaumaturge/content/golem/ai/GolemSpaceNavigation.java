package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.content.golem.GolemTrace;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import com.leclowndu93150.thaumaturge.content.world.space.WorldFrame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Routes between physical decks; the native navigator still owns paths inside one frame. */
public final class GolemSpaceNavigation extends PathNavigation {
    private static final double GRID = 0.5;
    private static final double SURFACE_RAY_MARGIN = 0.01;
    private static final int MAX_VISITED = 2048;
    private static final int VALIDATE_EVERY = 5;
    // Rotating frame transforms can turn a nominal one-block stair into
    // 1.0000005 world blocks. Use the same tolerance as route validation.
    private static final double JUMP_RISE_EPSILON = 0.01;
    private static final long FAILED_ASCENT_TTL_TICKS = 160;
    private static final int MAX_FAILED_ASCENTS = 6;
    // A* generates diagonals of (1, 1) at the coarse 0.5 grid. Their horizontal
    // length is sqrt(2), so rejecting anything beyond sqrt(1.3) rejects valid routes.
    private static final double MAX_VALID_EDGE_HORIZONTAL_SQR = 2.25;
    private static final double JUMP_APPROACH_DISTANCE_SQR = 1.55 * 1.55;
    private static final double MIN_JUMP_FORWARD_DISTANCE_SQR = 0.1 * 0.1;
    // An inter-frame jump is different from climbing a stair. Wait until the
    // golem has actually reached a safe takeoff on the departure surface.
    private static final double CROSS_FRAME_TAKEOFF_RADIUS_SQR = 0.18 * 0.18;
    // TaskGoal polls every five ticks. Replacing a Sable-native route at that
    // cadence resets its waypoint progress, so retain it while it makes progress.
    private static final int NATIVE_STUCK_TICKS = 45;
    private static final int NATIVE_FALLBACK_TICKS = 120;
    private static final double NATIVE_PROGRESS_LOCAL_SQR = 0.25 * 0.25;
    private static final double NATIVE_GOAL_LOCAL_TOLERANCE_SQR = 0.5 * 0.5;
    private final PathNavigation nativeNavigation;
    private final boolean flying;
    private int routeAge;
    private int progressNode = -1;
    private double bestDistance = Double.POSITIVE_INFINITY;
    private int stalled;
    private GolemPathGeometry.@Nullable Surface avoided;
    private @Nullable BlockPos avoidedTarget;
    private long avoidUntil;
    // A repeatedly missed rotating stair must be avoided even if the next
    // search asks for a different tile beside the same chest. All coordinates
    // are in the stair's deck-local frame, never transient world coordinates.
    private final List<FailedAscent> failedAscents = new ArrayList<>();
    private @Nullable SpacePath recentJumpRoute;
    private int recentJumpNode = -1;
    private long recentJumpTick = Long.MIN_VALUE;

    private record FailedAscent(SpaceFrame frame, Vec3 localFeet, long expiresAt) {}

    static boolean sameFailedAscent(SpaceFrame a, Vec3 aLocal, SpaceFrame b, Vec3 bLocal) {
        return a.equals(b) && aLocal.distanceToSqr(bLocal) < 0.30 * 0.30;
    }

    static boolean allowableJumpRise(double rise, double jumpHeight) {
        return rise <= jumpHeight + JUMP_RISE_EPSILON;
    }

    private void recordFailedAscent(SpacePath route, int node, String reason) {
        if (flying || node <= 0 || node >= route.points.size() || !route.jumpEdge(node)) return;
        GolemPathGeometry.Surface from = route.points.get(node - 1);
        GolemPathGeometry.Surface landing = route.points.get(node);
        if (from.frame().isWorld()
                || !from.frame().equals(landing.frame())
                || landing.worldFeet().y - from.worldFeet().y < 0.65) return;
        long now = level.getGameTime();
        failedAscents.removeIf(failed -> failed.expiresAt() <= now
                || sameFailedAscent(failed.frame(), failed.localFeet(), landing.frame(), landing.localFeet()));
        if (failedAscents.size() >= MAX_FAILED_ASCENTS) failedAscents.removeFirst();
        failedAscents.add(new FailedAscent(landing.frame(), landing.localFeet(), now + FAILED_ASCENT_TTL_TICKS));
        if (GolemTrace.DEBUG_TRACE) {
            GolemTrace.log(
                    "[golem-move] tick={} golem={} event=avoid_failed_ascent "
                            + "reason={} frame={} landingLocal={} expiresInTicks={}",
                    now,
                    mob.getUUID(),
                    reason,
                    landing.frame(),
                    landing.localFeet(),
                    FAILED_ASCENT_TTL_TICKS);
        }
    }

    private void recordRecentFailedAscent(SpacePath route, int node, String reason) {
        recordFailedAscent(route, node, reason);
        // Stalling often occurs on the waypoint *after* a jump while the
        // golem is still in the air. Remember the actual recently launched
        // ascent rather than banning an unrelated walkable waypoint.
        if (recentJumpRoute == route && level.getGameTime() - recentJumpTick <= 32)
            recordFailedAscent(route, recentJumpNode, reason);
    }

    private boolean recentlyFailedAscent(GolemPathGeometry.Surface landing) {
        long now = level.getGameTime();
        failedAscents.removeIf(failed -> failed.expiresAt() <= now);
        return failedAscents.stream()
                .anyMatch(failed ->
                        sameFailedAscent(failed.frame(), failed.localFeet(), landing.frame(), landing.localFeet()));
    }

    private @Nullable BlockPos failedTarget;
    private @Nullable UUID failedEntity;
    private @Nullable SpaceFrame failedFrame;
    private Vec3 failedStart = Vec3.ZERO;
    private Vec3 failedGoal = Vec3.ZERO;
    private long retryAfter;
    private @Nullable SearchJob pendingSearch;
    private @Nullable SearchOutcome finishedSearch;
    private @Nullable Entity nativeGoalEntity;
    private @Nullable SpaceFrame nativeGoalFrame;
    private Vec3 nativeGoalLocal = Vec3.ZERO;
    private Vec3 nativeProgressLocal = Vec3.ZERO;
    private int nativeProgressNode;
    private long nativeProgressTick;
    private @Nullable UUID nativeBlockedEntity;
    private long nativeBlockedUntil;
    // JumpControl only queues an action. On a rotating/tilted deck the entity
    // may lose ground contact before that action supplies any upward velocity.
    // Do not mistake "not onGround" for a successful launch.
    private @Nullable SpacePath awaitingJumpLift;
    private int awaitingJumpNode = -1;
    private long awaitingJumpSince;

    private void clearAwaitingJump() {
        awaitingJumpLift = null;
        awaitingJumpNode = -1;
    }

    static boolean hasJumpLift(Vec3 feet, Vec3 takeoff, double verticalVelocity) {
        return verticalVelocity > 0.035 && feet.y >= takeoff.y - 0.02;
    }

    // A* work is cooperative, always on the server thread. Never offload Level
    // collision/transform queries to a worker thread.
    private static final int RESULT_TTL_TICKS = 40;
    private static final int ABANDON_SEARCH_AFTER_TICKS = 30;
    private static final int CLAIM_READY_ROUTE_TICKS = 10;
    private static final double SEARCH_ORIGIN_TOLERANCE_SQR = 0.36;
    private static final double SEARCH_GOAL_TOLERANCE_SQR = 0.36;
    // A deck can translate while A* is yielding. Its *local* destination is
    // stable, but a large transform change makes cached world collision samples
    // stale, especially along transitions between two different frames.
    private static final double SEARCH_TRANSFORM_DRIFT_SQR = 3.0 * 3.0;
    // A golem standing on a fast-turning deck can remain at the same local
    // foothold while its initial WORLD coordinates travel many blocks. Give
    // foreign-frame delivery searches a chance to finish. The completed route
    // is still checked against live collision geometry before it can be followed.
    private static final double FOREIGN_FRAME_SEARCH_DRIFT_SQR = 9.0 * 9.0;

    static double allowedStartTransformDriftSqr(SpaceFrame source, SpaceFrame target) {
        return !source.isWorld() && !source.equals(target)
                ? FOREIGN_FRAME_SEARCH_DRIFT_SQR
                : SEARCH_TRANSFORM_DRIFT_SQR;
    }

    /**
     * Rigid deck rotation does not invalidate a search between two points on
     * that same deck. Both the sampled surfaces and the goal are stored in
     * deck-local coordinates and follow the deck's live transform. Requiring
     * their old world positions to be nearby cancelled every search after 11
     * ticks on fast-turning decks, even with unchanged local start and goal.
     *
     * For transitions between distinct frames, still bound pose drift: their
     * intersection and collision samples can become obsolete while searching.
     * The completed route is checked again against live geometry on movement.
     */
    static boolean invalidatedByTransformDrift(
            SpaceFrame source, SpaceFrame target, double sourceDriftSqr, double goalDriftSqr) {
        if (!source.isWorld() && source.equals(target)) return false;
        double allowedGoal = target.isWorld() ? SEARCH_TRANSFORM_DRIFT_SQR : FOREIGN_FRAME_SEARCH_DRIFT_SQR;
        return sourceDriftSqr > allowedStartTransformDriftSqr(source, target) || goalDriftSqr > allowedGoal;
    }

    /** A completed cooperative route can be reused instead of searching for a second chest-side tile. */
    static boolean isCooperativePath(@Nullable Path route) {
        return route instanceof SpacePath;
    }

    /** A fixed spot on a movable deck, independent of the deck's world pose. */
    private record GoalAnchor(SpaceFrame frame, Vec3 localFeet) {
        Vec3 worldFeet() {
            return frame.toWorld(localFeet);
        }

        boolean samePlace(@Nullable GoalAnchor other) {
            return other != null
                    && frame.equals(other.frame)
                    && localFeet.distanceToSqr(other.localFeet) <= SEARCH_GOAL_TOLERANCE_SQR;
        }
    }

    private static boolean sameDestination(
            BlockPos expectedTarget,
            @Nullable Entity expectedEntity,
            SpaceFrame expectedFrame,
            Vec3 expectedLocalGoal,
            @Nullable GoalAnchor expectedAnchor,
            BlockPos actualTarget,
            @Nullable Entity actualEntity,
            SpaceFrame actualFrame,
            Vec3 actualWorldGoal,
            @Nullable GoalAnchor actualAnchor) {
        if (expectedEntity != actualEntity || !expectedFrame.equals(actualFrame)) return false;
        if (expectedAnchor != null || actualAnchor != null)
            return expectedAnchor != null && expectedAnchor.samePlace(actualAnchor);
        if (actualEntity != null)
            return expectedLocalGoal.distanceToSqr(actualFrame.toLocal(actualWorldGoal)) <= SEARCH_GOAL_TOLERANCE_SQR;
        return expectedTarget.equals(actualTarget)
                && expectedLocalGoal.distanceToSqr(actualFrame.toLocal(actualWorldGoal)) <= SEARCH_GOAL_TOLERANCE_SQR;
    }

    private static boolean sameStart(SpaceFrame startFrame, Vec3 startLocal, Vec3 currentWorld) {
        return startLocal.distanceToSqr(startFrame.toLocal(currentWorld)) <= SEARCH_ORIGIN_TOLERANCE_SQR;
    }

    public GolemSpaceNavigation(Mob mob, Level level, PathNavigation nativeNavigation, boolean flying) {
        super(mob, level);
        this.nativeNavigation = nativeNavigation;
        this.flying = flying;
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        nodeEvaluator = new WalkNodeEvaluator();
        return new PathFinder(nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected Vec3 getTempMobPos() {
        return mob.position();
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }

    @Override
    protected @Nullable Path createPath(
            Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy, float followRange) {
        BlockPos target = targets.stream()
                .min(Comparator.comparingDouble(
                        pos -> mob.position().distanceToSqr(LevelSpaces.worldCenter(level, pos))))
                .orElse(null);
        if (target == null) return null;
        return createPathTo(
                target,
                LevelSpaces.worldCenter(level, target),
                LevelSpaces.frameOf(level, target),
                null,
                accuracy,
                followRange);
    }

    /** Entity positions are world-space even when Sable tracks the entity on a local sublevel. */
    @Override
    public @Nullable Path createPath(Entity entity, int accuracy) {
        if (!entity.isAlive()) return null;
        return createPathTo(
                BlockPos.containing(entity.position()),
                entity.position(),
                LevelSpaces.trackedFrame(entity),
                entity,
                accuracy,
                (float) mob.getAttributeValue(Attributes.FOLLOW_RANGE));
    }

    @Override
    public boolean moveTo(Entity entity, double speed) {
        SpaceFrame frame = LevelSpaces.trackedFrame(mob);
        // Do not reset Sable's local waypoint follower on every five-tick task
        // poll. It needs uninterrupted time to negotiate stairs and narrow turns.
        if (!frame.isWorld()
                && entity == nativeGoalEntity
                && frame.equals(nativeGoalFrame)
                && frame.equals(LevelSpaces.trackedFrame(entity))
                && path != null
                && !(path instanceof SpacePath)
                && !path.isDone()
                && entity.isAlive()
                && frame.toLocal(entity.position()).distanceToSqr(nativeGoalLocal) <= NATIVE_GOAL_LOCAL_TOLERANCE_SQR) {
            return true;
        }
        // Custom Sable recovery must include jump-capable search variants. Keep
        // the existing accuracy for ordinary-world navigation unchanged.
        Path route = createPath(entity, frame.isWorld() ? 0 : 1);
        boolean accepted = moveTo(route, speed);
        if (accepted
                && route != null
                && !(route instanceof SpacePath)
                && !frame.isWorld()
                && frame.equals(LevelSpaces.trackedFrame(entity))) {
            nativeGoalEntity = entity;
            nativeGoalFrame = frame;
            nativeGoalLocal = frame.toLocal(entity.position());
            nativeProgressLocal = frame.toLocal(mob.position());
            nativeProgressNode = route.getNextNodeIndex();
            nativeProgressTick = level.getGameTime();
        }
        return accepted;
    }

    private void clearNativeTracking() {
        nativeGoalEntity = null;
        nativeGoalFrame = null;
    }

    private void tickNativeProgress() {
        if (nativeGoalEntity == null) return;
        SpaceFrame frame = LevelSpaces.trackedFrame(mob);
        if (!frame.equals(nativeGoalFrame)
                || !nativeGoalEntity.isAlive()
                || !frame.equals(LevelSpaces.trackedFrame(nativeGoalEntity))) {
            clearNativeTracking();
            return;
        }
        // Sable may exhaust a plot-local path without bringing the world-space
        // golem close enough to its item. Treat this as a failed approach, not
        // permission to restart the same six nodes on every AI poll.
        if (path == null || path.isDone()) {
            if (mob.distanceToSqr(nativeGoalEntity) > 3.5) {
                rejectStalledNativeRoute("endpoint_too_far");
            } else {
                clearNativeTracking();
            }
            return;
        }
        Vec3 local = frame.toLocal(mob.position());
        int node = path.getNextNodeIndex();
        // A world-vertical bounce on a tilted deck also shifts local X/Z.
        // Count local displacement only while grounded, so repeated jumps that
        // land at the same spot cannot keep an impossible route alive forever.
        if ((mob.onGround() && local.subtract(nativeProgressLocal).horizontalDistanceSqr() >= NATIVE_PROGRESS_LOCAL_SQR)
                || node > nativeProgressNode) {
            nativeProgressLocal = local;
            nativeProgressNode = node;
            nativeProgressTick = level.getGameTime();
            return;
        }
        if (level.getGameTime() - nativeProgressTick <= NATIVE_STUCK_TICKS) return;
        rejectStalledNativeRoute("no_local_progress");
    }

    private void rejectStalledNativeRoute(String reason) {
        if (nativeGoalEntity == null) return;
        // A route that makes no local progress must not be requested 40+ more
        // times. Temporarily use the collision-aware cooperative pathfinder.
        nativeBlockedEntity = nativeGoalEntity.getUUID();
        nativeBlockedUntil = level.getGameTime() + NATIVE_FALLBACK_TICKS;
        if (GolemTrace.DEBUG_TRACE) {
            GolemTrace.log(
                    "[golem-move] tick={} golem={} event=native_stalled reason={} item={} "
                            + "node={} position={} local={} collision={} fallbackTicks={}",
                    level.getGameTime(),
                    mob.getUUID(),
                    reason,
                    nativeBlockedEntity,
                    path == null ? -1 : path.getNextNodeIndex(),
                    mob.position(),
                    LevelSpaces.trackedFrame(mob).toLocal(mob.position()),
                    mob.horizontalCollision,
                    NATIVE_FALLBACK_TICKS);
        }
        nativeNavigation.stop();
        path = null;
        clearNativeTracking();
        holdPosition();
    }

    private @Nullable Path createPathTo(
            BlockPos target,
            Vec3 worldTarget,
            SpaceFrame targetFrame,
            @Nullable Entity targetEntity,
            int accuracy,
            float followRange) {
        return createPathTo(target, worldTarget, targetFrame, targetEntity, null, accuracy, followRange);
    }

    /** Navigate to a block fixed on a deck even while that deck translates or rotates. */
    public boolean moveToFrame(SpaceFrame frame, Vec3 localFeet, double speed) {
        GoalAnchor anchor = new GoalAnchor(frame, localFeet);
        if (path instanceof SpacePath current
                && !current.isDone()
                && current.anchor != null
                && current.anchor.samePlace(anchor)
                && valid(current, flying || mob.onGround())) {
            return true;
        }
        Vec3 goalWorld = anchor.worldFeet();
        BlockPos key = BlockPos.containing(goalWorld);
        Path candidate = createPathTo(
                key, goalWorld, frame, null, anchor, 1, (float) mob.getAttributeValue(Attributes.FOLLOW_RANGE));
        return candidate != null && moveTo(candidate, speed);
    }

    private @Nullable Path createPathTo(
            BlockPos target,
            Vec3 worldTarget,
            SpaceFrame targetFrame,
            @Nullable Entity targetEntity,
            @Nullable GoalAnchor anchor,
            int accuracy,
            float followRange) {
        var sourceFrame = LevelSpaces.trackedFrame(mob);
        boolean sameFrame = sourceFrame.equals(targetFrame);
        boolean ordinaryWorld = sourceFrame.isWorld()
                && targetFrame.isWorld()
                && LevelSpaces.framesTouching(level, new AABB(mob.position(), worldTarget).inflate(2.0)).stream()
                        .allMatch(frame -> frame.isWorld());
        // Sable 2.0.6 patches GroundPathNavigation and PathNavigation to pathfind
        // in a tracked sublevel's own plot coordinates and project its waypoints
        // back to world space. Our previous entity-only custom A* bypassed that
        // integration entirely, causing NO_ROUTE on ordinary Sable stairs and
        // rotated decks. Prefer Sable's native navigator for targets on the SAME
        // sublevel; never use its single-plot pathfinder for cross-frame travel.
        boolean nativeRecovery = targetEntity != null
                && targetEntity.getUUID().equals(nativeBlockedEntity)
                && (level.getGameTime() < nativeBlockedUntil
                        || path instanceof SpacePath current
                                && current.targetEntity == targetEntity
                                && !current.isDone());
        // A frame-anchored home target needs its dynamic local destination in
        // the resulting path; native block paths cannot carry that anchor.
        boolean canUseNative =
                anchor == null && sameFrame && (!sourceFrame.isWorld() || ordinaryWorld) && !nativeRecovery;
        if (canUseNative) {
            // A cooperative fallback search is already running or has a result.
            // Do not re-run synchronous native pathfinding on every task poll.
            Vec3 start = mob.position();
            long now = level.getGameTime();
            boolean customPending = pendingSearch != null
                    && pendingSearch.matches(target, targetEntity, targetFrame, start, worldTarget, anchor);
            boolean customCompleted = finishedSearch != null
                    && finishedSearch.matches(target, targetEntity, targetFrame, start, worldTarget, anchor, now);
            if (!customPending && !customCompleted) {
                Path nativePath = targetEntity != null
                        ? nativeNavigation.createPath(targetEntity, accuracy)
                        : nativeNavigation.createPath(target, accuracy);
                if (ordinaryWorld) return nativePath;
                if (nativePath != null) {
                    // Sable's native paths contain plot-local nodes. Transform
                    // their endpoint back to world coordinates before accepting.
                    // A partial path is useful only if the golem can still
                    // physically reach its item or block target from its end.
                    double reachSqr = targetEntity != null ? 3.5 : Math.max(1.5, accuracy * accuracy + 0.5);
                    Vec3 endpoint = pathEndWorld(level, nativePath);
                    if (endpoint.distanceToSqr(worldTarget) <= reachSqr) {
                        return nativePath;
                    }
                }
            }
        }
        if (path instanceof SpacePath current
                && !current.isDone()
                && (anchor == null
                        ? current.anchor == null && current.getTarget().equals(target)
                        : current.anchor != null && current.anchor.samePlace(anchor))
                && current.targetEntity == targetEntity
                && valid(current, flying || mob.onGround())) {
            return current;
        }
        return findPath(target, worldTarget, targetFrame, targetEntity, anchor, accuracy, followRange);
    }

    public boolean hasPendingSearch() {
        return pendingSearch != null;
    }

    /** A cooperative search finished this tick; its owner still needs to claim it. */
    public boolean hasReadySearch() {
        return finishedSearch != null
                && finishedSearch.result != null
                && level.getGameTime() <= finishedSearch.expires();
    }

    @Override
    public @Nullable BlockPos getTargetPos() {
        return path == null ? null : path.getTarget();
    }

    @Override
    public boolean moveTo(@Nullable Path route, double speed) {
        if (route == null) {
            stop();
            return false;
        }
        // Other AI goals can supply their own movement multiplier. Keep ground
        // traversal at walking pace even when a caller requests a sprint.
        double walkingSpeed = flying ? speed : Math.min(speed, 1.0);
        speedModifier = walkingSpeed;
        if (route != path) {
            clearAwaitingJump();
            clearNativeTracking();
            if (GolemTrace.DEBUG_TRACE) {
                GolemTrace.log(
                        "[golem-move] tick={} golem={} event=route_assigned target={} type={} nodes={} from={} "
                                + "speed={} frame={}",
                        level.getGameTime(),
                        mob.getUUID(),
                        route.getTarget(),
                        route instanceof SpacePath ? "space" : "native",
                        route.getNodeCount(),
                        mob.position(),
                        walkingSpeed,
                        LevelSpaces.trackedFrame(mob));
            }
            path = route;
            routeAge = 0;
            stalled = 0;
            progressNode = -1;
            bestDistance = Double.POSITIVE_INFINITY;
        }
        if (route instanceof SpacePath) {
            nativeNavigation.stop();
            return !route.isDone();
        }
        return nativeNavigation.moveTo(route, walkingSpeed);
    }

    @Override
    public void tick() {
        tickPendingSearch();
        if (!(path instanceof SpacePath route)) {
            nativeNavigation.tick();
            path = nativeNavigation.getPath();
            tickNativeProgress();
            return;
        }
        if (route.isDone()) {
            holdPosition();
            return;
        }
        routeAge++;
        // Frame transforms can change every tick. Check route continuity immediately,
        // but only repeat expensive collision/surface sweeps periodically.
        // A jump temporarily overlaps the edge being climbed, so check collision after landing.
        boolean checkCollision = routeAge % VALIDATE_EVERY == 0 && (flying || mob.onGround());
        if (!valid(route, checkCollision)) {
            int rejectedNode = route.getNextNodeIndex();
            if (!flying
                    && rejectedNode < route.points.size()
                    && route.jumpEdge(rejectedNode)
                    && rejectedNode > 0
                    && mob.getY() < route.points.get(rejectedNode - 1).worldFeet().y - 0.9) {
                avoided = route.points.get(rejectedNode);
                avoidedTarget = route.getTarget();
                avoidUntil = level.getGameTime() + 160;
            }
            if (GolemTrace.DEBUG_TRACE) {
                GolemTrace.log(
                        "[golem-move] tick={} golem={} event=route_invalid target={} node={} position={} "
                                + "next={} jumpEdge={} collisionCheck={} grounded={} frame={}",
                        level.getGameTime(),
                        mob.getUUID(),
                        route.getTarget(),
                        route.getNextNodeIndex(),
                        mob.position(),
                        route.points.get(route.getNextNodeIndex()).worldFeet(),
                        route.jumpEdge(route.getNextNodeIndex()),
                        checkCollision,
                        mob.onGround(),
                        LevelSpaces.trackedFrame(mob));
            }
            BlockPos target = route.getTarget();
            finishedSearch = null; // Never replay a route proven invalid.
            stop();
            Path replanned = route.anchor != null
                    ? createPathTo(
                            BlockPos.containing(route.anchor.worldFeet()),
                            route.anchor.worldFeet(),
                            route.anchor.frame(),
                            null,
                            route.anchor,
                            1,
                            (float) mob.getAttributeValue(Attributes.FOLLOW_RANGE))
                    : route.targetEntity == null ? createPath(target, 1) : createPath(route.targetEntity, 1);
            moveTo(replanned, speedModifier);
            return;
        }
        Vec3 next = route.points.get(route.getNextNodeIndex()).worldFeet();
        if (mob.position().distanceToSqr(next) < 0.12) {
            route.advance();
            if (route.isDone()) {
                holdPosition();
                return;
            }
            next = route.points.get(route.getNextNodeIndex()).worldFeet();
        }
        // Vertical progress is still progress while climbing. Horizontal-only tracking
        // can incorrectly mark a golem stuck halfway through a one-block jump.
        double remaining = mob.position().distanceToSqr(next);
        if (progressNode != route.getNextNodeIndex() || remaining < bestDistance - 0.01) {
            progressNode = route.getNextNodeIndex();
            bestDistance = remaining;
            stalled = 0;
        } else if (++stalled > 40) {
            if (GolemTrace.DEBUG_TRACE) {
                GolemTrace.log(
                        "[golem-move] tick={} golem={} event=route_stalled target={} node={} at={} "
                                + "next={} grounded={} velocity={} frame={}",
                        level.getGameTime(),
                        mob.getUUID(),
                        route.getTarget(),
                        route.getNextNodeIndex(),
                        mob.position(),
                        next,
                        mob.onGround(),
                        mob.getDeltaMovement(),
                        LevelSpaces.trackedFrame(mob));
            }
            recordRecentFailedAscent(route, route.getNextNodeIndex(), "route_stalled");
            avoided = route.points.get(route.getNextNodeIndex());
            avoidedTarget = route.getTarget();
            avoidUntil = level.getGameTime() + 100;
            stop();
            return;
        }
        double horizontalDistanceSqr = mob.position().subtract(next).horizontalDistanceSqr();
        // Jump only on edges explicitly classified by the search. In particular,
        // a slanted but continuously walkable Sable surface must not cause hops.
        int node = route.getNextNodeIndex();
        boolean needsJump = route.jumpEdge(node);
        // Cross-frame leaps and stair jumps on a moving Sable deck both need
        // a frame-anchored departure. The old cross-frame-only check let a
        // rotating stair jump launch from almost a block behind its takeoff.
        boolean stagedJump = needsJump
                && node > 0
                && shouldStageJump(
                        flying,
                        route.points.get(node - 1).frame(),
                        route.points.get(node).frame());
        var frame = LevelSpaces.trackedFrame(mob);
        double walkingSpeed = !flying && !frame.isWorld() && frame.directionToWorld(new Vec3(0, 1, 0)).y < 0.98
                ? speedModifier * 0.65
                : speedModifier;
        Vec3 launch = stagedJump ? jumpTakeoff(route, node).worldFeet() : next;
        if (awaitingJumpLift == route && awaitingJumpNode == node) {
            if (hasJumpLift(mob.position(), launch, mob.getDeltaMovement().y)) {
                clearAwaitingJump();
            } else {
                long waiting = level.getGameTime() - awaitingJumpSince;
                if (waiting >= 5 || mob.getY() < launch.y - 0.18) {
                    if (GolemTrace.DEBUG_TRACE) {
                        GolemTrace.log(
                                "[golem-move] tick={} golem={} event=jump_no_lift "
                                        + "at={} takeoff={} landing={} elapsed={} velocity={} grounded={}",
                                level.getGameTime(),
                                mob.getUUID(),
                                mob.position(),
                                launch,
                                next,
                                waiting,
                                mob.getDeltaMovement(),
                                mob.onGround());
                    }
                    // This edge was reachable in A* but failed physically. Avoid
                    // the same landing on the next recovery search.
                    recordFailedAscent(route, node, "jump_no_lift");
                    avoided = route.points.get(node);
                    avoidedTarget = route.getTarget();
                    avoidUntil = level.getGameTime() + 100;
                    stop();
                    return;
                }
                // Re-request after two ticks if Sable kept the entity grounded;
                // otherwise simply hold the supported foothold until lift.
                if (waiting >= 2 && mob.onGround()) mob.getJumpControl().jump();
                mob.getMoveControl().setWantedPosition(launch.x, launch.y, launch.z, Math.min(walkingSpeed, 0.25));
                return;
            }
        } else if (awaitingJumpLift != null) {
            clearAwaitingJump();
        }
        // A moving Sable staircase needs the same takeoff discipline as a
        // frame boundary. Being near the landing does not mean the feet have
        // reached the departure edge.
        // Reach the prevalidated, frame-anchored takeoff before jumping; do not
        // walk blindly towards a landing over the void.
        boolean readyToLaunch = !stagedJump || nearCrossFrameTakeoff(mob.position(), launch, next);
        if (stagedJump && mob.onGround() && !readyToLaunch) {
            mob.getMoveControl().setWantedPosition(launch.x, launch.y, launch.z, walkingSpeed);
            return;
        }
        if (!flying
                && mob.onGround()
                && needsJump
                && readyToLaunch
                && horizontalDistanceSqr > MIN_JUMP_FORWARD_DISTANCE_SQR
                && (!stagedJump || horizontalDistanceSqr <= JUMP_APPROACH_DISTANCE_SQR)) {
            if (GolemTrace.DEBUG_TRACE) {
                GolemTrace.log(
                        "[golem-move] tick={} golem={} event=jump_request at={} takeoff={} next={} "
                                + "jumpEdge={} rise={} horizontalDistance={} takeoffDistance={} "
                                + "stepHeight={} frame={} velocity={}",
                        level.getGameTime(),
                        mob.getUUID(),
                        mob.position(),
                        launch,
                        next,
                        needsJump,
                        next.y - mob.getY(),
                        Math.sqrt(horizontalDistanceSqr),
                        Math.sqrt(mob.position().subtract(launch).horizontalDistanceSqr()),
                        mob.maxUpStep(),
                        frame,
                        mob.getDeltaMovement());
            }
            mob.getJumpControl().jump();
            if (stagedJump) {
                awaitingJumpLift = route;
                awaitingJumpNode = node;
                awaitingJumpSince = level.getGameTime();
                recentJumpRoute = route;
                recentJumpNode = node;
                recentJumpTick = level.getGameTime();
                // JumpControl queues the jump for the mob's next control tick.
                // Moving at the landing *now* can carry a golem beyond the
                // narrow supported edge before its upward velocity exists.
                // This matters on a rotating staircase as well as on gaps.
                // Stay over the verified departure foothold for this tick;
                // only begin forward air steering after leaving the ground.
                mob.getMoveControl().setWantedPosition(launch.x, launch.y, launch.z, Math.min(walkingSpeed, 0.35));
                return;
            }
        }
        // Once a same-deck, one-block stair jump has gained enough height,
        // supply a small, capped forward correction. The regular move control
        // often advances only ~0.3 block before the jump starts descending,
        // leaving the golem against the first rotating stair's vertical face.
        // Never assist gap crossings, ordinary-world jumps, or grounded mobs.
        if (stagedJump
                && !mob.onGround()
                && recentJumpRoute == route
                && recentJumpNode == node
                && isMovingDeckStairAscent(
                        route.points.get(node - 1).frame(),
                        route.points.get(node).frame(),
                        launch,
                        next)) {
            Vec3 original = mob.getDeltaMovement();
            Vec3 assisted =
                    movingStairAirAssist(mob.position(), original, launch, next, level.getGameTime() - recentJumpTick);
            if (assisted != original) {
                mob.setDeltaMovement(assisted);
                if (GolemTrace.DEBUG_TRACE) {
                    GolemTrace.log(
                            "[golem-move] tick={} golem={} event=stair_air_assist "
                                    + "at={} takeoff={} landing={} velocityBefore={} velocityAfter={}",
                            level.getGameTime(),
                            mob.getUUID(),
                            mob.position(),
                            launch,
                            next,
                            original,
                            assisted);
                }
            }
        }
        // Do not apply tilted-deck walking slowdown to airborne jumps. In
        // particular a moving stair landing can rotate away during the leap.
        double speed = stagedJump ? speedModifier : walkingSpeed;
        mob.getMoveControl().setWantedPosition(next.x, next.y, next.z, speed);
    }

    /** Narrowly scoped air assistance: only a one-block ascent on one moving deck. */
    static boolean isMovingDeckStairAscent(SpaceFrame departure, SpaceFrame landing, Vec3 takeoff, Vec3 target) {
        double rise = target.y - takeoff.y;
        return !departure.isWorld() && departure.equals(landing) && rise >= 0.75 && rise <= 1.0 + JUMP_RISE_EPSILON;
    }

    /**
     * A rotating stair may leave the golem airborne at the right height but
     * short of the ledge. Only near the *upper* portion of an actual jump,
     * modestly increase forward velocity toward its already-validated landing.
     * Do not push while low against the wall, after the jump, or at the target.
     * The correction is bounded so it cannot become an uncontrolled dash.
     */
    static Vec3 movingStairAirAssist(Vec3 feet, Vec3 velocity, Vec3 takeoff, Vec3 landing, long elapsedTicks) {
        if (elapsedTicks < 2 || elapsedTicks > 9) return velocity;
        double climbed = feet.y - takeoff.y;
        if (climbed < 0.90 || climbed > 1.40) return velocity;
        double dx = landing.x - feet.x;
        double dz = landing.z - feet.z;
        double distance = Math.hypot(dx, dz);
        // Already within half a block of landing: normal steering is enough.
        if (distance < 0.50 || distance > 1.40) return velocity;
        double speedSqr = velocity.x * velocity.x + velocity.z * velocity.z;
        if (speedSqr >= 0.30 * 0.30) return velocity;
        double unitX = dx / distance;
        double unitZ = dz / distance;
        double forward = velocity.x * unitX + velocity.z * unitZ;
        double requested = Math.min(0.22, distance * 0.30);
        double additional = Math.min(0.09, requested - forward);
        if (additional <= 0.001) return velocity;
        return velocity.add(unitX * additional, 0, unitZ * additional);
    }

    /** Stage Sable stairs as well as frame changes, but preserve vanilla step/jump timing. */
    static boolean shouldStageJump(boolean flying, SpaceFrame departure, SpaceFrame landing) {
        return !flying && (!departure.equals(landing) || !departure.isWorld());
    }

    /** Only launch close to the *departure* surface, never just because the far ledge is in range. */
    static boolean nearCrossFrameTakeoff(Vec3 position, Vec3 takeoff, Vec3 landing) {
        if (Math.abs(position.y - takeoff.y) > 0.50) return false;
        Vec3 direction = new Vec3(landing.x - takeoff.x, 0, landing.z - takeoff.z);
        Vec3 delta = new Vec3(position.x - takeoff.x, 0, position.z - takeoff.z);
        if (delta.lengthSqr() <= CROSS_FRAME_TAKEOFF_RADIUS_SQR) return true;
        double length = direction.length();
        if (length < 0.001) return false;
        double ahead = delta.dot(direction) / length;
        // Allow a small overshoot toward the gap, but never a launch while
        // still well behind the chosen takeoff or off to its side.
        double sidewaysSqr = delta.lengthSqr() - ahead * ahead;
        return ahead >= 0 && ahead <= 0.28 && sidewaysSqr <= CROSS_FRAME_TAKEOFF_RADIUS_SQR;
    }

    private GolemPathGeometry.Surface jumpTakeoff(SpacePath route, int node) {
        if (route.takeoffNode == node && route.cachedTakeoff != null) return route.cachedTakeoff;
        GolemPathGeometry.Surface source = route.points.get(node - 1);
        GolemPathGeometry.Surface landing = route.points.get(node);
        Vec3 from = source.worldFeet();
        Vec3 to = landing.worldFeet();
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double horizontal = Math.hypot(dx, dz);
        GolemPathGeometry.Surface takeoff = source;
        // Sample a few points *on the departure frame only*. That lets the
        // golem run to the usable edge of a slanted platform, reducing the
        // required jump distance. Do this once per edge, not per search node.
        // Full-body footing, walkable approach, and the remaining jump arc all
        // need to be valid, or the original departure waypoint is retained.
        if (horizontal > 0.90) {
            for (double advance = 0.20; advance <= Math.min(0.80, horizontal - 0.40); advance += 0.20) {
                double x = from.x + dx / horizontal * advance;
                double z = from.z + dz / horizontal * advance;
                GolemPathGeometry.Surface candidate =
                        GolemPathGeometry.surface(level, mob, x, z, from.y - 0.85, from.y + 0.85);
                if (candidate == null || !candidate.frame().equals(source.frame())) break;
                if (!GolemPathGeometry.safeJumpLanding(level, mob, candidate)) break;
                Vec3 at = candidate.worldFeet();
                if (!edgeClear(from, at) || !jumpClear(at, to, route.jumpHeight)) break;
                takeoff = candidate;
            }
        }
        route.takeoffNode = node;
        route.cachedTakeoff = takeoff;
        if (GolemTrace.DEBUG_TRACE) {
            GolemTrace.log(
                    "[golem-move] tick={} golem={} event=jump_takeoff_planned "
                            + "from={} takeoff={} landing={} originalGap={} stagedGap={} frame={}",
                    level.getGameTime(),
                    mob.getUUID(),
                    from,
                    takeoff.worldFeet(),
                    to,
                    horizontal,
                    Math.sqrt(takeoff.worldFeet().subtract(to).horizontalDistanceSqr()),
                    source.frame());
        }
        return takeoff;
    }

    /**
     * A one-block-high stair on a Sable deck must be approached through its face,
     * not diagonally across the corner. The broad A* neighbor grid can otherwise
     * accept a 1.0-by-0.5 local step as a leap. Its swept arc is geometrically
     * clear, but the golem clips the side of the stair and drops off the deck.
     *
     * Work in deck-local axes: world X/Z rotate constantly and cannot tell a
     * head-on approach from a diagonal one. This is deliberately restricted to
     * one-block ascents on a single moving deck; it does not alter ordinary
     * stair stepping, vanilla movement, or jumps between separate frames.
     */
    static boolean headOnMovingStairJump(SpaceFrame departure, SpaceFrame landing, Vec3 from, Vec3 to) {
        if (departure.isWorld() || !departure.equals(landing) || to.y - from.y < 0.65) return true;
        Vec3 localFrom = departure.toLocal(from);
        Vec3 localTo = departure.toLocal(to);
        double dx = Math.abs(localTo.x - localFrom.x);
        double dz = Math.abs(localTo.z - localFrom.z);
        double forward = Math.max(dx, dz);
        double lateral = Math.min(dx, dz);
        // In the half-block search grid, a (1.0, 0.5) or (0.5, 0.5) step
        // approaches a stair corner. A (1.0, 0.0) or (0.5, 0.0) step is face-on.
        if (forward <= 0.40 || lateral > Math.max(0.15, forward * 0.28)) return false;
        // The upper stair's *side* is a frequent false landing on a rotating
        // deck. Prefer a lane through the center of its block face, leaving
        // enough lateral margin for the golem's upright bounding box.
        double crossingAxis = dx >= dz ? localTo.z : localTo.x;
        double fraction = crossingAxis - Math.floor(crossingAxis);
        return Math.abs(fraction - 0.5) <= 0.30;
    }

    /**
     * Require a shorter gap when ascending from a moving Sable deck to fixed
     * terrain. The A* arc check only verifies empty space, not whether a mob
     * can gain enough *horizontal* distance before it falls. Three real golems
     * failed the same 1.0-block gap with a 0.62-block rise. A shorter landing
     * slightly to the side is preferable to repeating the fatal straight jump.
     */
    static boolean safeElevatedExit(SpaceFrame departure, SpaceFrame landing, Vec3 from, Vec3 to) {
        if (departure.isWorld() || !landing.isWorld() || departure.equals(landing)) return true;
        double rise = to.y - from.y;
        return rise < 0.45 || from.subtract(to).horizontalDistanceSqr() <= 0.82 * 0.82;
    }

    /** Larger cross-frame jumps are riskier than nearby supported corners. */
    static double crossFrameJumpPenalty(Vec3 from, Vec3 to) {
        double gapSqr = from.subtract(to).horizontalDistanceSqr();
        return 0.5 + 2.25 * gapSqr + 0.75 * Math.max(0.0, to.y - from.y);
    }

    @Override
    public void recomputePath() {
        if (path instanceof SpacePath route) {
            BlockPos target = route.getTarget();
            path = null;
            Path replanned = route.anchor != null
                    ? createPathTo(
                            BlockPos.containing(route.anchor.worldFeet()),
                            route.anchor.worldFeet(),
                            route.anchor.frame(),
                            null,
                            route.anchor,
                            1,
                            (float) mob.getAttributeValue(Attributes.FOLLOW_RANGE))
                    : route.targetEntity == null ? createPath(target, 1) : createPath(route.targetEntity, 1);
            moveTo(replanned, speedModifier);
        } else {
            nativeNavigation.recomputePath();
        }
    }

    @Override
    public void stop() {
        clearAwaitingJump();
        recentJumpRoute = null;
        recentJumpNode = -1;
        super.stop();
        nativeNavigation.stop();
        clearNativeTracking();
        holdPosition();
    }

    private void holdPosition() {
        mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);
    }

    private boolean valid(SpacePath route, boolean checkCollision) {
        int node = route.getNextNodeIndex();
        // Validate the full *planned* edge. While climbing, the golem's real
        // position can be below or above its takeoff; measuring to the landing
        // from there spuriously rejects a still-valid route and causes jitter.
        Vec3 previous = node == 0 ? mob.position() : route.points.get(node - 1).worldFeet();
        // A moving deck can leave the entity behind. In that situation we really
        // must replan rather than blindly following a previously valid edge.
        Vec3 deviation = mob.position().subtract(previous);
        if (deviation.horizontalDistanceSqr() > 2.25 * 2.25 || Math.abs(deviation.y) > 3.0)
            return rejectRoute("deviated_from_planned_edge", route, node, previous, mob.position());
        // Validate only the next few edges. Checking the complete route every
        // tick is both expensive and invalidates distant edges on moving decks.
        for (int i = node; i < Math.min(route.points.size(), node + 3); i++) {
            Vec3 next = route.points.get(i).worldFeet();
            Vec3 delta = next.subtract(previous);
            if (flying
                    ? delta.lengthSqr() > 3.0
                    : delta.horizontalDistanceSqr() > MAX_VALID_EDGE_HORIZONTAL_SQR
                            || delta.y > route.jumpHeight + 0.01
                            || delta.y < -Math.min(3, mob.getMaxFallDistance()) - 0.01)
                return rejectRoute("edge_exceeds_plan_limits", route, i, previous, next);
            if (checkCollision && i == route.getNextNodeIndex()) {
                // Validate the planned edge, not a shortened fragment of it from the
                // golem's current position. Otherwise a legal leap gets invalidated
                // as the golem approaches its takeoff or climbs onto the landing.
                Vec3 takeoff = i == 0 ? previous : route.points.get(i - 1).worldFeet();
                boolean clear = route.jumpEdge(i)
                        ? headOnMovingStairJump(
                                        route.points.get(i - 1).frame(),
                                        route.points.get(i).frame(),
                                        takeoff,
                                        next)
                                && safeElevatedExit(
                                        route.points.get(i - 1).frame(),
                                        route.points.get(i).frame(),
                                        takeoff,
                                        next)
                                && jumpClear(takeoff, next, route.jumpHeight)
                                && GolemPathGeometry.safeJumpLanding(level, mob, route.points.get(i))
                        : edgeClear(takeoff, next);
                boolean frameTransition = i > 0
                        && !route.points
                                .get(i - 1)
                                .frame()
                                .equals(route.points.get(i).frame());
                if (!clear || frameTransition && !GolemPathGeometry.safeJumpLanding(level, mob, route.points.get(i))) {
                    // A live collision/support rejection on a rotating stair
                    // is stronger evidence than a mere stale route target.
                    // Remember just this failed ascent across future tasks.
                    if (!clear) recordFailedAscent(route, i, "collision_or_missing_support");
                    return rejectRoute("collision_or_missing_support", route, i, takeoff, next);
                }
            }
            previous = next;
        }
        Vec3 endpoint = route.points.getLast().worldFeet();
        Vec3 goal = route.anchor != null
                ? route.anchor.worldFeet()
                : route.targetEntity == null
                        ? LevelSpaces.worldCenter(level, route.getTarget())
                        : route.targetEntity.position();
        if (route.targetEntity != null
                && (!route.targetEntity.isAlive()
                        || !LevelSpaces.trackedFrame(route.targetEntity).equals(route.targetFrame)))
            return rejectRoute(
                    "target_entity_changed_frame_or_disappeared", route, route.points.size() - 1, endpoint, goal);
        if (endpoint.distanceToSqr(goal) > route.reachSqr)
            return rejectRoute("target_frame_moved", route, route.points.size() - 1, endpoint, goal);
        return true;
    }

    private boolean rejectRoute(String reason, SpacePath route, int edge, Vec3 from, Vec3 to) {
        if (GolemTrace.DEBUG_TRACE) {
            GolemTrace.log(
                    "[golem-move] tick={} golem={} event=route_rejected reason={} target={} "
                            + "edge={} from={} to={} delta={} jumpEdge={}",
                    level.getGameTime(),
                    mob.getUUID(),
                    reason,
                    route.getTarget(),
                    edge,
                    from,
                    to,
                    to.subtract(from),
                    route.jumpEdge(edge));
        }
        return false;
    }

    // Native paths use frame-local node coordinates. Custom paths encode world feet
    // in their nodes. Do not apply a sublevel transform twice.
    static Vec3 pathEndWorld(Level level, Path candidate) {
        Node end = candidate.getEndNode();
        if (end == null) return Vec3.ZERO;
        return candidate instanceof SpacePath route
                ? route.points.getLast().worldFeet()
                : LevelSpaces.worldCenter(level, end.asBlockPos());
    }

    private @Nullable SpacePath findPath(
            BlockPos target,
            Vec3 goal,
            SpaceFrame targetFrame,
            @Nullable Entity targetEntity,
            @Nullable GoalAnchor anchor,
            int accuracy,
            float range) {
        Vec3 start = mob.position();
        long now = level.getGameTime();
        if (finishedSearch != null) {
            if (finishedSearch.matches(target, targetEntity, targetFrame, start, goal, anchor, now)) {
                return finishedSearch.result;
            }
            // Give the goal that requested a completed path a chance to claim it
            // before another lower-priority goal overwrites the result.
            if (finishedSearch.result != null
                    && path != finishedSearch.result
                    && now <= finishedSearch.completedTick + CLAIM_READY_ROUTE_TICKS
                    && sameStart(finishedSearch.startFrame, finishedSearch.startLocal, start)) {
                if (level instanceof ServerLevel server) GolemPathSearchBudget.markDeferred(server, mob.getUUID());
                return null;
            }
        }
        if (pendingSearch != null) {
            if (pendingSearch.matches(target, targetEntity, targetFrame, start, goal, anchor)) {
                if (level instanceof ServerLevel server) GolemPathSearchBudget.markDeferred(server, mob.getUUID());
                return null;
            }
            // Multiple goals query this navigator in the same tick. An item
            // search must not be replaced by a different task/home request every
            // five ticks, otherwise it can never finish.
            if (now - pendingSearch.lastRequestedTick <= ABANDON_SEARCH_AFTER_TICKS) {
                if (level instanceof ServerLevel server) GolemPathSearchBudget.markDeferred(server, mob.getUUID());
                return null;
            }
            pendingSearch = null;
        }
        finishedSearch = null;
        if (level instanceof ServerLevel
                && target.equals(failedTarget)
                && (targetEntity == null
                        ? failedEntity == null
                        : targetEntity.getUUID().equals(failedEntity))
                && targetFrame.equals(failedFrame)
                && now < retryAfter
                && start.distanceToSqr(failedStart) < 0.04
                && goal.distanceToSqr(failedGoal) < 0.04) return null;
        if (start.distanceToSqr(goal) > range * range) return null;
        GolemPathGeometry.Surface first = flying
                ? new GolemPathGeometry.Surface(WorldFrame.INSTANCE, start)
                : GolemPathGeometry.surface(level, mob, start.x, start.z, start.y - 1.0, start.y + 0.1);
        if (first == null && !flying) {
            first = GolemPathGeometry.startingSurface(
                    level, mob, start.x, start.z, start.y - 1.0, start.y + mob.maxUpStep() + 0.1);
        }
        if (first == null && !flying && mob.onGround()) {
            // A golem can be just outside the sampling column near a platform edge.
            // Select the *closest* viable recovery foothold, not the farthest.
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    GolemPathGeometry.Surface recovery = GolemPathGeometry.startingSurface(
                            level,
                            mob,
                            start.x + dx * 0.25,
                            start.z + dz * 0.25,
                            start.y - 1.0,
                            start.y + mob.maxUpStep() + 0.1);
                    if (recovery != null
                            && (first == null
                                    || recovery.worldFeet().distanceToSqr(goal)
                                            < first.worldFeet().distanceToSqr(goal))) first = recovery;
                }
            }
        }
        if (first == null) {
            failedTarget = target.immutable();
            failedEntity = targetEntity == null ? null : targetEntity.getUUID();
            failedFrame = targetFrame;
            failedStart = start;
            failedGoal = goal;
            retryAfter = now + 20;
            return null;
        }
        SearchJob job = new SearchJob(
                first, target.immutable(), targetFrame, targetEntity, anchor, start, goal, accuracy, range, now);
        // Client-side/mock-level path queries in tests do not have a server tick
        // driving the cooperative search. Production server searches always yield.
        if (!(level instanceof ServerLevel server)) {
            while (!job.finished) job.advance(Long.MAX_VALUE);
            return job.result;
        }
        pendingSearch = job;
        GolemPathSearchBudget.markDeferred(server, mob.getUUID());
        return null;
    }

    private void tickPendingSearch() {
        if (pendingSearch == null || !(level instanceof ServerLevel server)) return;
        SearchJob job = pendingSearch;
        if (level.getGameTime() - job.lastRequestedTick > ABANDON_SEARCH_AFTER_TICKS) {
            pendingSearch = null;
            return;
        }
        Vec3 now = mob.position();
        if (job.targetEntity != null
                && (!job.targetEntity.isAlive()
                        || !LevelSpaces.trackedFrame(job.targetEntity).equals(job.targetFrame))) {
            pendingSearch = null;
            return;
        }
        Vec3 currentGoal = job.currentGoal();
        // Compare in the entities' own frames. World position changes caused
        // solely by a moving deck are not a new search request. Large physical
        // pose changes still invalidate samples collected at the old transform.
        boolean movedLocally = !sameStart(job.startFrame, job.startLocal, now);
        boolean changedDestination = !sameDestination(
                job.target,
                job.targetEntity,
                job.targetFrame,
                job.goalLocal,
                job.anchor,
                job.target,
                job.targetEntity,
                job.targetFrame,
                currentGoal,
                job.anchor);
        double sourceDrift = job.startFrame.toWorld(job.startLocal).distanceToSqr(job.start);
        double goalDrift = job.targetFrame.toWorld(job.goalLocal).distanceToSqr(job.goal);
        if (movedLocally
                || changedDestination
                || invalidatedByTransformDrift(job.startFrame, job.targetFrame, sourceDrift, goalDrift)) {
            if (GolemTrace.DEBUG_TRACE) {
                GolemTrace.log(
                        "[golem-search] tick={} golem={} event=search_cancelled "
                                + "target={} localMoved={} destinationChanged={} "
                                + "startDrift={} goalDrift={} ageTicks={}",
                        level.getGameTime(),
                        mob.getUUID(),
                        job.target,
                        movedLocally,
                        changedDestination,
                        Math.sqrt(sourceDrift),
                        Math.sqrt(goalDrift),
                        level.getGameTime() - job.startedTick);
            }
            pendingSearch = null;
            return;
        }
        if (!GolemPathSearchBudget.tryAcquire(server, mob.getUUID())) return;
        long allotted = GolemPathSearchBudget.availableNanos(server);
        if (allotted <= 0) return;
        long begun = System.nanoTime();
        job.advance(begun + allotted);
        long spent = System.nanoTime() - begun;
        GolemPathSearchBudget.recordWork(server, spent);
        if (!job.finished) return;
        pendingSearch = null;
        finishedSearch = new SearchOutcome(
                job.target,
                job.targetEntity,
                job.targetFrame,
                job.anchor,
                job.startFrame,
                job.startLocal,
                job.goalLocal,
                job.result,
                level.getGameTime(),
                level.getGameTime() + RESULT_TTL_TICKS);
        if (job.result == null) {
            failedTarget = job.target;
            failedEntity = job.targetEntity == null ? null : job.targetEntity.getUUID();
            failedFrame = job.targetFrame;
            failedStart = job.start;
            failedGoal = job.goal;
            retryAfter = level.getGameTime() + 40;
        }
        if (GolemTrace.DEBUG_TRACE) {
            GolemTrace.log(
                    "[golem-search] tick={} golem={} target={} found={} nodes={} attempts={} "
                            + "totalMs={} ageTicks={} start={} goal={} startFrame={} targetFrame={}",
                    level.getGameTime(),
                    mob.getUUID(),
                    job.target,
                    job.result != null,
                    job.visited,
                    job.attempts,
                    job.nanos / 1_000_000.0,
                    level.getGameTime() - job.startedTick,
                    job.start,
                    job.goal,
                    LevelSpaces.trackedFrame(mob),
                    job.targetFrame);
        }
    }

    private record SearchOutcome(
            BlockPos target,
            @Nullable Entity targetEntity,
            SpaceFrame targetFrame,
            @Nullable GoalAnchor anchor,
            SpaceFrame startFrame,
            Vec3 startLocal,
            Vec3 goalLocal,
            @Nullable SpacePath result,
            long completedTick,
            long expires) {
        boolean matches(
                BlockPos requested,
                @Nullable Entity entity,
                SpaceFrame frame,
                Vec3 currentStart,
                Vec3 currentGoal,
                @Nullable GoalAnchor newAnchor,
                long now) {
            return now <= expires
                    && sameDestination(
                            target,
                            targetEntity,
                            targetFrame,
                            goalLocal,
                            anchor,
                            requested,
                            entity,
                            frame,
                            currentGoal,
                            newAnchor)
                    && sameStart(startFrame, startLocal, currentStart);
        }
    }

    private final class SearchJob {
        private final GolemPathGeometry.Surface first;
        private final BlockPos target;
        private final SpaceFrame targetFrame;
        private final @Nullable Entity targetEntity;
        private final @Nullable GoalAnchor anchor;
        private final SpaceFrame startFrame;
        private final Vec3 startLocal;
        private final Vec3 goalLocal;
        private final Vec3 start;
        private final Vec3 goal;
        private final int accuracy;
        private final float range;
        private final long startedTick;
        private long lastRequestedTick;
        private final List<SearchVariant> variants = new ArrayList<>();
        private int variantIndex;
        private @Nullable SearchVariant active;
        private @Nullable SpacePath result;
        private boolean finished;
        private int visited;
        private int attempts;
        private long nanos;

        SearchJob(
                GolemPathGeometry.Surface first,
                BlockPos target,
                SpaceFrame targetFrame,
                @Nullable Entity targetEntity,
                @Nullable GoalAnchor anchor,
                Vec3 start,
                Vec3 goal,
                int accuracy,
                float range,
                long startedTick) {
            this.first = first;
            this.target = target;
            this.targetFrame = targetFrame;
            this.targetEntity = targetEntity;
            this.anchor = anchor;
            this.startFrame = LevelSpaces.trackedFrame(mob);
            this.startLocal = startFrame.toLocal(start);
            this.goalLocal = targetFrame.toLocal(goal);
            this.start = start;
            this.goal = goal;
            this.accuracy = accuracy;
            this.range = range;
            this.startedTick = startedTick;
            this.lastRequestedTick = startedTick;
            // Prefer deck-aligned paths on a rotating Sable staircase.
            // World-axis sampling can choose physically awkward diagonals that
            // pass A* and then fail the real mob collision/jump controller.
            // A fine local-grid route is safer than a coarse world-grid route.
            boolean sameDeck = !first.frame().isWorld() && first.frame().equals(targetFrame);
            double jump = 1.0;
            if (!flying && accuracy != 0) {
                Vec3 up = first.frame().directionToWorld(new Vec3(0, 1, 0));
                jump = !first.frame().equals(targetFrame)
                        ? 1.20
                        : !first.frame().isWorld() && up.y > 0.5 ? 1.0 + Math.min(0.35, up.horizontalDistance()) : 1.0;
            }
            if (sameDeck) variants.add(new SearchVariant(GRID, 1.0, true));
            if (sameDeck && jump > 1.01) variants.add(new SearchVariant(GRID, jump, true));
            if (sameDeck && !flying && accuracy != 0) variants.add(new SearchVariant(GRID / 2, jump, true));
            variants.add(new SearchVariant(GRID, 1.0, false));
            if (!flying && accuracy != 0) {
                if (jump > 1.01) variants.add(new SearchVariant(GRID, jump, false));
                if (!sameDeck && !first.frame().isWorld()) variants.add(new SearchVariant(GRID / 2, jump, true));
                variants.add(new SearchVariant(GRID / 2, jump, false));
            }
        }

        double reachSqr() {
            // Home arrival is stricter than item pickup. Do not finish a path
            // a whole block away and immediately start the same return again.
            return anchor != null ? 0.75 * 0.75 : Math.max(1.0, accuracy * accuracy + 0.5);
        }

        Vec3 currentGoal() {
            return anchor != null
                    ? anchor.worldFeet()
                    : targetEntity != null ? targetEntity.position() : targetFrame.toWorld(goalLocal);
        }

        boolean matches(
                BlockPos requested,
                @Nullable Entity entity,
                SpaceFrame frame,
                Vec3 currentStart,
                Vec3 currentGoal,
                @Nullable GoalAnchor requestedAnchor) {
            boolean match = sameDestination(
                            target,
                            targetEntity,
                            targetFrame,
                            goalLocal,
                            anchor,
                            requested,
                            entity,
                            frame,
                            currentGoal,
                            requestedAnchor)
                    && sameStart(startFrame, startLocal, currentStart);
            if (match) lastRequestedTick = level.getGameTime();
            return match;
        }

        void advance(long deadline) {
            long begun = System.nanoTime();
            try {
                while (!finished && System.nanoTime() < deadline) {
                    if (active == null) {
                        if (variantIndex >= variants.size()) {
                            finished = true;
                            return;
                        }
                        active = variants.get(variantIndex++);
                        attempts++;
                    }
                    active.advanceOne();
                    if (active.found != null) {
                        visited += active.visited;
                        result = active.found;
                        finished = true;
                    } else if (active.finished) {
                        visited += active.visited;
                        active = null;
                    }
                }
            } finally {
                nanos += System.nanoTime() - begun;
            }
        }

        private final class SearchVariant {
            private final double grid;
            private final double jumpHeight;
            private final boolean localGrid;
            private final int limit;
            private final int dyCount;
            private final int width;
            private final PriorityQueue<Step> open =
                    new PriorityQueue<>(Comparator.comparingDouble(step -> step.cost + 1.5 * step.estimate));
            private final Map<BlockPos, Double> best = new HashMap<>();
            private final Map<SurfaceQuery, Optional<GolemPathGeometry.Surface>> surfaces = new HashMap<>();
            private @Nullable Step expanding;
            private @Nullable SpacePath found;
            private boolean finished;
            private int neighborIndex;
            private int visited;

            SearchVariant(double grid, double jumpHeight, boolean localGrid) {
                this.grid = grid;
                this.jumpHeight = jumpHeight;
                this.localGrid = localGrid;
                this.limit = grid == GRID ? 2 : 4;
                this.dyCount = flying ? 3 : 1;
                this.width = 2 * limit + 1;
                Step root = new Step(first, null, 0, first.worldFeet().distanceTo(currentGoal()), false);
                open.add(root);
                best.put(key(localGrid ? first.localFeet() : first.worldFeet(), grid), 0.0);
            }

            void advanceOne() {
                if (expanding == null) {
                    int max = grid == GRID ? MAX_VISITED : MAX_VISITED / 4;
                    if (open.isEmpty() || visited >= max) {
                        finished = true;
                        return;
                    }
                    // Pop only one queue entry per slice, including stale entries.
                    // Avoid an unbounded loop when the queue contains many duplicates.
                    Step step = open.remove();
                    visited++;
                    Vec3 from = step.surface.worldFeet();
                    BlockPos fromKey = key(localGrid ? first.frame().toLocal(from) : from, grid);
                    if (step.cost > best.getOrDefault(fromKey, Double.POSITIVE_INFINITY)) return;
                    if (from.distanceToSqr(currentGoal()) <= reachSqr()) {
                        List<GolemPathGeometry.Surface> points = new ArrayList<>();
                        List<Boolean> jumpEdges = new ArrayList<>();
                        for (Step cursor = step; cursor != null; cursor = cursor.previous) {
                            points.add(cursor.surface);
                            jumpEdges.add(cursor.jumpFromPrevious);
                        }
                        Collections.reverse(points);
                        Collections.reverse(jumpEdges);
                        found = new SpacePath(
                                points, jumpEdges, target, targetFrame, targetEntity, anchor, reachSqr(), jumpHeight);
                        return;
                    }
                    expanding = step;
                    neighborIndex = 0;
                    return;
                }
                int total = width * width * dyCount;
                if (neighborIndex >= total) {
                    expanding = null;
                    return;
                }
                int index = neighborIndex++;
                int dx = index / (width * dyCount) - limit;
                int dz = (index / dyCount) % width - limit;
                int dy = flying ? index % dyCount - 1 : 0;
                if (Math.abs(dx) == limit && Math.abs(dz) == limit) return;
                if (flying && (Math.abs(dx) > 1 || Math.abs(dz) > 1)) return;
                if (dx == 0 && dz == 0 && dy == 0) return;
                Vec3 from = expanding.surface.worldFeet();
                double spacing = flying ? GRID : grid;
                Vec3 offset = localGrid
                        ? first.frame().toWorld(first.frame().toLocal(from).add(dx * spacing, 0, dz * spacing))
                        : from.add(dx * spacing, 0, dz * spacing);
                GolemPathGeometry.Surface surface = flying
                        ? new GolemPathGeometry.Surface(
                                WorldFrame.INSTANCE, new Vec3(offset.x, from.y + dy * spacing, offset.z))
                        : surface(
                                surfaces,
                                offset.x,
                                offset.z,
                                from.y - Math.min(3, mob.getMaxFallDistance()),
                                from.y + Math.max(jumpHeight, mob.maxUpStep()) + SURFACE_RAY_MARGIN);
                if (surface == null) return;
                // At a boundary between a Sable deck and vanilla terrain, a
                // point-contact ledge is not a safe landing even for a step.
                if (!flying
                        && !expanding.surface.frame().equals(surface.frame())
                        && !GolemPathGeometry.safeJumpLanding(level, mob, surface)) return;
                Vec3 to = surface.worldFeet();
                // A physically missed landing must not be retried from a
                // slightly different preceding node. Compare in the landing
                // frame's *local* coordinates so the ban rotates with the deck.
                if (avoided != null
                        && target.equals(avoidedTarget)
                        && level.getGameTime() < avoidUntil
                        && surface.frame().equals(avoided.frame())
                        && surface.localFeet().distanceToSqr(avoided.localFeet()) < 0.09) return;
                if (!flying && !allowableJumpRise(to.y - from.y, Math.max(jumpHeight, mob.maxUpStep()))) return;
                if (to.distanceToSqr(startFrame.toWorld(startLocal)) > range * range) return;
                double walkingCost = expanding.cost + from.distanceTo(to);
                BlockPos node = key(localGrid ? first.frame().toLocal(to) : to, grid);
                if (walkingCost >= best.getOrDefault(node, Double.POSITIVE_INFINITY)) return;
                boolean supported = edgeClear(from, to, surfaces);
                // A half-block horizontal approach can still require a jump
                // when crossing an elevated block edge on another frame.
                boolean leap = !supported
                        && !flying
                        && headOnMovingStairJump(expanding.surface.frame(), surface.frame(), from, to)
                        && safeElevatedExit(expanding.surface.frame(), surface.frame(), from, to)
                        && jumpClear(from, to, jumpHeight)
                        && GolemPathGeometry.safeJumpLanding(level, mob, surface);
                if (!supported && !leap) return;
                // Do not repeat a rotating-deck ascent that physically stalled
                // on an earlier route, even if this attempt uses another chest
                // approach block. Ban just the landing, not an entire staircase.
                if (leap && to.y - from.y >= 0.65 && recentlyFailedAscent(surface)) return;
                // Favor the short crossing next to a rotated platform corner
                // over a longer diagonal jump that merely saves a grid step.
                // Same-frame stair jumps keep their existing cost.
                double cost = walkingCost
                        + (leap
                                ? expanding.surface.frame().equals(surface.frame())
                                        ? 0.5
                                        : crossFrameJumpPenalty(from, to)
                                : 0.0);
                if (cost >= best.getOrDefault(node, Double.POSITIVE_INFINITY)) return;
                best.put(node, cost);
                open.add(new Step(surface, expanding, cost, to.distanceTo(currentGoal()), leap));
            }
        }
    }

    private GolemPathGeometry.@Nullable Surface surface(
            Map<SurfaceQuery, Optional<GolemPathGeometry.Surface>> cache,
            double x,
            double z,
            double minY,
            double maxY) {
        return cache.computeIfAbsent(
                        new SurfaceQuery(x, z, minY, maxY),
                        ignored -> Optional.ofNullable(GolemPathGeometry.surface(level, mob, x, z, minY, maxY)))
                .orElse(null);
    }

    private boolean edgeClear(Vec3 from, Vec3 to) {
        return edgeClear(from, to, new HashMap<>());
    }

    boolean jumpClear(Vec3 from, Vec3 to) {
        return jumpClear(from, to, 1.0);
    }

    private boolean jumpClear(Vec3 from, Vec3 to, double jumpHeight) {
        double horizontal = from.subtract(to).horizontalDistanceSqr();
        if (horizontal <= 0.40 * 0.40
                || horizontal > 1.55 * 1.55
                || !allowableJumpRise(to.y - from.y, jumpHeight)
                || to.y - from.y < -1) return false;
        for (int i = 1; i < 8; i++) {
            double t = (double) i / 8;
            double height =
                    Math.min(jumpHeight + 0.2, (to.y - from.y) * t + (jumpHeight - 0.3) * Math.sin(Math.PI * t));
            Vec3 point = new Vec3(from.x + (to.x - from.x) * t, from.y + height + 0.001, from.z + (to.z - from.z) * t);
            if (!GolemPathGeometry.clear(level, mob, GolemPathGeometry.body(mob, point))) return false;
        }
        return true;
    }

    private boolean edgeClear(Vec3 from, Vec3 to, Map<SurfaceQuery, Optional<GolemPathGeometry.Surface>> cache) {
        int samples = Math.max(1, (int) Math.ceil(from.distanceTo(to) / 0.2));
        double previousFeetY = from.y;
        for (int i = 1; i <= samples; i++) {
            Vec3 point = from.lerp(to, (double) i / samples);
            if (!flying) {
                var surface = surface(
                        cache,
                        point.x,
                        point.z,
                        Math.min(from.y, to.y) - 0.1,
                        Math.max(from.y, to.y) + mob.maxUpStep());
                if (surface == null) return false;
                point = surface.worldFeet();
                // A continuously rising tilted floor is walkable. A sudden
                // full-block rise is not, even when both endpoints have clear
                // body space: require an explicitly planned jump for that edge.
                if (point.y - previousFeetY > mob.maxUpStep() + 0.05) return false;
                previousFeetY = point.y;
            }
            double half = mob.getBbWidth() / 2.0;
            if (!GolemPathGeometry.clear(
                    level,
                    mob,
                    new AABB(
                            point.x - half,
                            point.y + 1.0E-4,
                            point.z - half,
                            point.x + half,
                            point.y + mob.getBbHeight(),
                            point.z + half))) return false;
        }
        return true;
    }

    private static BlockPos key(Vec3 pos, double grid) {
        return BlockPos.containing(pos.x / grid, pos.y / grid, pos.z / grid);
    }

    private record SurfaceQuery(double x, double z, double minY, double maxY) {}

    private record Step(
            GolemPathGeometry.Surface surface,
            @Nullable Step previous,
            double cost,
            double estimate,
            boolean jumpFromPrevious) {}

    private static final class SpacePath extends Path {
        private final List<GolemPathGeometry.Surface> points;
        private final List<Boolean> jumpEdges;
        private final SpaceFrame targetFrame;
        private final @Nullable Entity targetEntity;
        private final @Nullable GoalAnchor anchor;
        private final double reachSqr;
        private final double jumpHeight;
        private int takeoffNode = -1;
        private GolemPathGeometry.@Nullable Surface cachedTakeoff;

        private SpacePath(
                List<GolemPathGeometry.Surface> points,
                List<Boolean> jumpEdges,
                BlockPos target,
                SpaceFrame targetFrame,
                @Nullable Entity targetEntity,
                @Nullable GoalAnchor anchor,
                double reachSqr,
                double jumpHeight) {
            super(
                    points.stream()
                            .map(point -> {
                                BlockPos pos = BlockPos.containing(point.worldFeet());
                                return new Node(pos.getX(), pos.getY(), pos.getZ());
                            })
                            .toList(),
                    target,
                    true);
            this.points = points;
            this.jumpEdges = jumpEdges;
            this.targetFrame = targetFrame;
            this.targetEntity = targetEntity;
            this.anchor = anchor;
            this.reachSqr = reachSqr;
            this.jumpHeight = jumpHeight;
        }

        private boolean jumpEdge(int index) {
            return jumpEdges.get(index);
        }
    }
}
