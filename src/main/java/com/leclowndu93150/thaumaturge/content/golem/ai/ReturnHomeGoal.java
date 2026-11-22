package com.leclowndu93150.thaumaturge.content.golem.ai;

import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemTrace;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ReturnHomeGoal extends Goal {
    private static final int FIRST_CHECK = 10;
    private static final int CHECK_INTERVAL = 15;
    // Placement uses the precise horizontal center of the home block. Being
    // within 0.8 blocks was enough to stop the old goal at a visible offset.
    private static final double ARRIVED_HORIZONTAL_SQR = 0.20 * 0.20;
    private static final double ARRIVED_VERTICAL_TOLERANCE = 0.70;
    private static final double CENTER_APPROACH_SQR = 1.15 * 1.15;
    private static final double CENTER_APPROACH_SPEED = 0.45;
    private static final int STEP_RANGE = 16;
    private static final int STEP_HEIGHT = 7;

    private final EntityThaumaturgeGolem golem;
    private int wait = FIRST_CHECK;
    private @Nullable Vec3 heading;
    private @Nullable SpaceFrame homeFrame;
    private Vec3 homeLocal = Vec3.ZERO;
    private @Nullable SpaceFrame intermediateFrame;
    private Vec3 intermediateLocal = Vec3.ZERO;
    private boolean centering;

    public ReturnHomeGoal(EntityThaumaturgeGolem golem) {
        this.golem = golem;
        setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        if (golem.getTask() != null || golem.recentlyFinishedTask()) return false;
        // A golem carrying cargo needs a delivery task, not an idle home route.
        // On fast rotating decks the latter can otherwise occupy the MOVE goal
        // just as the cooperative chest search is about to complete. A missing
        // chest path should be retried by BlockTaskGoal, not replaced by homing.
        var hands = golem.hands();
        if (hands != null && hands.contents().stream().anyMatch(stack -> !stack.isEmpty())) return false;
        // Let an in-progress item/block task search finish before issuing a
        // competing home route that would otherwise cancel or starve it.
        if (golem.getNavigation() instanceof GolemSpaceNavigation space && space.hasPendingSearch()) return false;
        if (wait > 0) {
            wait--;
            return false;
        }
        wait = CHECK_INTERVAL;
        // The restriction center belongs to a physical Sable frame. Never
        // cache only its world position when the deck can move each tick.
        homeFrame = LevelSpaces.frameOf(golem.level(), golem.getRestrictCenter());
        homeLocal = Vec3.atBottomCenterOf(golem.getRestrictCenter());
        Vec3 homeWorld = homeFrame.toWorld(homeLocal);
        double awaySqr = golem.position().distanceToSqr(homeWorld);
        if (arrivedAtHome(golem.position(), homeWorld, LevelSpaces.trackedFrame(golem), homeFrame)) return false;
        // Follow range is 40 (56 for scouts), but the old artificial 14-block
        // cap sent the golem to random nearby waypoints on each goal restart.
        // On a fast rotating deck those waypoints orbit in world space: every
        // cooperative search succeeds, yet no stable home route is ever used.
        // Search the actual, frame-anchored home directly whenever it lies
        // within the navigator's real reach. Only use a staging hop beyond it.
        double directRange = Math.max(3.0, golem.getAttributeValue(Attributes.FOLLOW_RANGE) * 0.90);
        heading = awaySqr > directRange * directRange
                ? DefaultRandomPos.getPosTowards(golem, STEP_RANGE, STEP_HEIGHT, homeWorld, Math.PI / 2.0)
                : null;
        intermediateFrame = null;
        centering = false;
        // Intermediate waypoints on a moving deck must move WITH that deck.
        // Otherwise the path to a once-valid world coordinate chases an empty
        // point in space and triggers target_frame_moved on every turn.
        if (heading != null && !LevelSpaces.trackedFrame(golem).isWorld()) {
            var surface = GolemPathGeometry.surface(
                    golem.level(), golem, heading.x, heading.z, heading.y - 1.0, heading.y + golem.maxUpStep() + 0.2);
            if (surface != null && !surface.frame().isWorld()) {
                intermediateFrame = surface.frame();
                intermediateLocal = surface.localFeet();
            }
        }
        if (GolemTrace.DEBUG_TRACE) {
            GolemTrace.log(
                    "[golem-home] tick={} golem={} event=home_goal home={} homeFrame={} "
                            + "away={} directRange={} intermediate={} startFrame={}",
                    golem.level().getGameTime(),
                    golem.getUUID(),
                    homeWorld,
                    homeFrame,
                    Math.sqrt(awaySqr),
                    directRange,
                    heading,
                    LevelSpaces.trackedFrame(golem));
        }
        return true;
    }

    /** Arrival requires the correct physical frame and an actual centered stance. */
    static boolean arrivedAtHome(Vec3 position, Vec3 home, SpaceFrame current, SpaceFrame destination) {
        if (!current.equals(destination) || Math.abs(position.y - home.y) > ARRIVED_VERTICAL_TOLERANCE) return false;
        double dx = position.x - home.x;
        double dz = position.z - home.z;
        return dx * dx + dz * dz <= ARRIVED_HORIZONTAL_SQR;
    }

    private boolean arrived() {
        return homeFrame != null
                && arrivedAtHome(golem.position(), worldHome(), LevelSpaces.trackedFrame(golem), homeFrame);
    }

    /**
     * Native paths stop at a waypoint tolerance that is wider than the home
     * block's center. Steer the final short, supported segment exactly to the
     * original spawn center instead of repeatedly asking A* for another route.
     * Never use this steering to cross between frames or over a gap.
     */
    private boolean canCenter() {
        if (homeFrame == null || !homeFrame.equals(LevelSpaces.trackedFrame(golem)) || !golem.onGround()) return false;
        Vec3 start = golem.position();
        Vec3 destination = worldHome();
        if (start.distanceToSqr(destination) > CENTER_APPROACH_SQR
                || Math.abs(start.y - destination.y) > ARRIVED_VERTICAL_TOLERANCE) return false;
        for (int step = 1; step <= 4; step++) {
            Vec3 sample = start.lerp(destination, step * 0.25);
            var surface = GolemPathGeometry.surface(
                    golem.level(), golem, sample.x, sample.z, sample.y - 0.55, sample.y + 0.55);
            if (surface == null
                    || !surface.frame().equals(homeFrame)
                    || Math.abs(surface.worldFeet().y - sample.y) > 0.35
                    || !GolemPathGeometry.safeJumpLanding(golem.level(), golem, surface)) return false;
        }
        return true;
    }

    private boolean steerToCenter() {
        if (!canCenter()) return false;
        if (!centering) {
            golem.getNavigation().stop();
            centering = true;
            if (GolemTrace.DEBUG_TRACE) {
                GolemTrace.log(
                        "[golem-home] tick={} golem={} event=centering_started pos={} target={}",
                        golem.level().getGameTime(),
                        golem.getUUID(),
                        golem.position(),
                        worldHome());
            }
        }
        Vec3 destination = worldHome();
        golem.getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, CENTER_APPROACH_SPEED);
        return true;
    }

    private Vec3 worldHome() {
        SpaceFrame frame = LevelSpaces.frameOf(golem.level(), golem.getRestrictCenter());
        return frame.toWorld(Vec3.atBottomCenterOf(golem.getRestrictCenter()));
    }

    private void issueRoute() {
        if (intermediateFrame != null && golem.getNavigation() instanceof GolemSpaceNavigation spaces) {
            spaces.moveToFrame(intermediateFrame, intermediateLocal, golem.getGolemMoveSpeed());
        } else if (heading != null) {
            golem.getNavigation().moveTo(heading.x, heading.y, heading.z, golem.getGolemMoveSpeed());
        } else if (homeFrame != null
                && !homeFrame.isWorld()
                && golem.getNavigation() instanceof GolemSpaceNavigation spaces) {
            spaces.moveToFrame(homeFrame, homeLocal, golem.getGolemMoveSpeed());
        } else {
            golem.getNavigation().moveTo(worldHome().x, worldHome().y, worldHome().z, golem.getGolemMoveSpeed());
        }
    }

    @Override
    public void start() {
        if (!steerToCenter()) issueRoute();
    }

    @Override
    public boolean canContinueToUse() {
        boolean waitingOnRoute = golem.getNavigation() instanceof GolemSpaceNavigation space
                && (space.hasPendingSearch() || space.hasReadySearch());
        return golem.getTask() == null
                && !arrived()
                && (centering
                        || canCenter()
                        || waitingOnRoute
                        || !golem.getNavigation().isDone());
    }

    @Override
    public void tick() {
        // A completed native path may be a fraction of a block short. Handle
        // that final distance without another full pathfinder restart.
        if (steerToCenter()) return;
        centering = false;
        // Cross-frame A* yields across ticks. Keep requesting the same stable
        // destination until its ready result can be claimed and followed.
        if (golem.getNavigation().isDone()) issueRoute();
    }

    @Override
    public void stop() {
        if (centering && arrived() && GolemTrace.DEBUG_TRACE) {
            GolemTrace.log(
                    "[golem-home] tick={} golem={} event=centered pos={} target={}",
                    golem.level().getGameTime(),
                    golem.getUUID(),
                    golem.position(),
                    worldHome());
        }
        centering = false;
        wait = CHECK_INTERVAL;
        golem.getNavigation().stop();
    }
}
