package com.leclowndu93150.thaumaturge.client.effect.instance;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.content.effect.StreamPathfinder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EssentiaStreamInstance extends StreamInstance {
    private static final double MOTION_LIMIT = 0.05;
    private static final double PULL = 0.01;
    private static final float LAUNCH_AMPLITUDE = 0.015F;
    private static final double LAUNCH_SPEED = 0.05;
    private static final float LAUNCH_WOBBLE = 0.005F;
    private static final double WAYPOINT_RADIUS_SQ = 0.4 * 0.4;
    private static final int MIN_LENGTH = 20;
    private static final int MIN_EXTEND = 5;
    private static final double DROP_SPREAD = 0.075;
    private static final float DROP_SCALE = 0.5F;
    private static final float WOBBLE = 0.03F;
    private static final double TICKS_PER_BLOCK = 21.0;
    private static final int REROUTE_INTERVAL = 40;

    private final int fixedTail;
    private List<Vec3> route;
    private int leg;

    public EssentiaStreamInstance(Vec3 start, List<Vec3> route, @Nullable Vec3 launchDirection, int fixedTail, int color, int count, float scale, int extend, double my) {
        super(start.x, start.y, start.z, color, count, scale);
        this.route = List.copyOf(route);
        this.fixedTail = Math.min(fixedTail, route.size());
        this.length = Math.max(MIN_LENGTH, extend);
        this.maxAge = routeTicks(start, this.route);
        if (launchDirection != null) {
            launchToward(launchDirection, LAUNCH_SPEED, LAUNCH_WOBBLE);
        } else {
            launchMotion(LAUNCH_AMPLITUDE, my);
        }
    }

    private static int routeTicks(Vec3 start, List<Vec3> route) {
        double total = 0.0;
        Vec3 prev = start;
        for (Vec3 point : route) {
            total += prev.distanceTo(point);
            prev = point;
        }
        return Math.max(1, (int) (total * TICKS_PER_BLOCK));
    }

    public void extend(int amount) {
        int add = Math.max(amount, MIN_EXTEND);
        this.length += add;
        this.maxAge += add;
    }

    private Vec3 destination() {
        return this.route.getLast();
    }

    private boolean finalLeg() {
        return this.leg == this.route.size() - 1;
    }

    @Override
    protected Vec3 seekTarget(ClientLevel level) {
        if (!finalLeg() && position().distanceToSqr(this.route.get(this.leg)) < WAYPOINT_RADIUS_SQ) {
            this.leg++;
        }
        return this.route.get(this.leg);
    }

    @Override
    protected void restrainMotion(double distance) {
        clampMotion(MOTION_LIMIT);
    }

    @Override
    protected double pullStrength(double distance) {
        return PULL;
    }

    @Override
    protected double shrinkRange() {
        return finalLeg() ? 1.0 : 0.0;
    }

    @Override
    protected void onDissolve(ClientLevel level) {
        RandomSource random = level.getRandom();
        Vec3 destination = destination();
        ClientEffects.essentiaDrop(level, destination.x + random.nextGaussian() * DROP_SPREAD, destination.y + random.nextGaussian() * DROP_SPREAD, destination.z + random.nextGaussian() * DROP_SPREAD,
                this.colorR, this.colorG, this.colorB, DROP_SCALE);
    }

    @Override
    protected void afterStep(ClientLevel level) {
        if (this.age % REROUTE_INTERVAL == 0) {
            reroute(level);
        }
        RandomSource random = level.getRandom();
        if (trailSize() > 2 && random.nextBoolean()) {
            int pick = random.nextInt(3);
            if (random.nextBoolean()) {
                pick = trailSize() - 2;
            }
            TrailPoint sample = trailPoint(pick);
            ClientEffects.essentiaDrop(level, sample.x() + this.originX, sample.y() + this.originY, sample.z() + this.originZ, this.colorR, this.colorG, this.colorB, DROP_SCALE);
        }
    }

    private void reroute(ClientLevel level) {
        int tailStart = this.route.size() - this.fixedTail;
        if (this.leg > tailStart) {
            return;
        }
        Vec3 head = position();
        if (StreamPathfinder.hasLineOfSight(level, head, this.route.get(this.leg))) {
            return;
        }
        Vec3 rejoin = this.route.get(tailStart);
        List<Vec3> detour = StreamPathfinder.findRoute(level, head, rejoin);
        if (detour == null) {
            return;
        }
        List<Vec3> rebuilt = new ArrayList<>(detour.size() + this.fixedTail);
        rebuilt.addAll(detour);
        for (int i = tailStart; i < this.route.size(); i++) {
            rebuilt.add(this.route.get(i));
        }
        this.route = List.copyOf(rebuilt);
        this.leg = 0;
    }

    public Snapshot snapshot(float partialTick) {
        return buildSnapshot(partialTick, WOBBLE, true, 1.0F);
    }
}
