package com.leclowndu93150.thaumaturge.compat.sable;

import dev.ryanhcode.sable.api.event.SablePrePhysicsTickEvent;
import dev.ryanhcode.sable.api.physics.force.ForceTotal;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

final class SablePushes implements SablePrePhysicsTickEvent {
    private static final double STANDARD_GRAVITY = 11.0;
    private static final long PUSH_LIFETIME = 1L;

    private final Map<UUID, Map<BlockPos, Push>> pending = new ConcurrentHashMap<>();

    void queue(
            ServerSubLevel ship,
            BlockPos source,
            Vec3 worldPoint,
            Vec3 worldDirection,
            double liftBlocks,
            double maxSpeed) {
        pending.computeIfAbsent(ship.getUniqueId(), id -> new ConcurrentHashMap<>())
                .put(
                        source.immutable(),
                        new Push(
                                worldPoint,
                                worldDirection.normalize(),
                                liftBlocks,
                                maxSpeed,
                                ship.getLevel().getGameTime()));
    }

    @Override
    public void prePhysicsTick(SubLevelPhysicsSystem system, double timeStep) {
        if (pending.isEmpty()) {
            return;
        }
        ServerLevel level = system.getLevel();
        long now = level.getGameTime();
        pending.values().forEach(pushes -> pushes.values().removeIf(push -> now - push.tick() > PUSH_LIFETIME));
        pending.values().removeIf(Map::isEmpty);
        SubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) {
            return;
        }
        pending.forEach((id, pushes) -> {
            SubLevel subLevel = container.getSubLevel(id);
            if (subLevel instanceof ServerSubLevel ship && !ship.isRemoved()) {
                RigidBodyHandle handle = system.getPhysicsHandle(ship);
                if (handle != null && handle.isValid()) {
                    apply(handle, ship, pushes.values(), timeStep);
                }
            }
        });
    }

    private static void apply(RigidBodyHandle handle, ServerSubLevel ship, Collection<Push> pushes, double timeStep) {
        MassData mass = ship.getMassTracker();
        if (mass == null || mass.isInvalid()) {
            return;
        }
        Vector3dc velocity = handle.getLinearVelocity();
        Pose3dc pose = ship.logicalPose();
        ForceTotal total = new ForceTotal();
        for (Push push : pushes) {
            Vec3 direction = push.direction();
            double along = velocity.x() * direction.x + velocity.y() * direction.y + velocity.z() * direction.z;
            if (along >= push.maxSpeed()) {
                continue;
            }
            double impulse = Math.min(
                    push.liftBlocks() * STANDARD_GRAVITY * timeStep, mass.getMass() * (push.maxSpeed() - along));
            Vec3 point = pose.transformPositionInverse(push.point());
            Vec3 localImpulse = pose.transformNormalInverse(direction).scale(impulse);
            total.applyImpulseAtPoint(
                    ship,
                    new Vector3d(point.x, point.y, point.z),
                    new Vector3d(localImpulse.x, localImpulse.y, localImpulse.z));
        }
        handle.applyForcesAndReset(total);
    }

    private record Push(Vec3 point, Vec3 direction, double liftBlocks, double maxSpeed, long tick) {}
}
