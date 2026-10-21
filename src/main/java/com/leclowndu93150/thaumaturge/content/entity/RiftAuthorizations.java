package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.rifts.RiftAuthorization;
import com.leclowndu93150.thaumaturge.api.rifts.RiftAuthorization.Action;
import com.leclowndu93150.thaumaturge.api.rifts.RiftAuthorization.Handle;
import com.leclowndu93150.thaumaturge.api.rifts.RiftAuthorization.Machine;
import com.leclowndu93150.thaumaturge.api.rifts.RiftOperationContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;

public final class RiftAuthorizations implements RiftAuthorization.Bindings {
    private record Policy(RiftAuthorization.Resolver resolver, boolean offline) {
    }
    private static final Map<Identifier, Policy> POLICIES = new HashMap<>();
    private static boolean frozen;

    public static synchronized void freeze() {
        frozen = true;
    }

    @Override
    public synchronized void register(Identifier id, RiftAuthorization.Resolver resolver, boolean supportsOffline) {
        if (frozen || POLICIES.putIfAbsent(Objects.requireNonNull(id, "id"), new Policy(Objects.requireNonNull(resolver, "resolver"), supportsOffline)) != null) {
            throw new IllegalStateException("Rift machine policies must be uniquely registered before server use");
        }
    }

    @Override
    public Optional<Handle> grant(ServerPlayer owner, BlockPos position, Identifier resolver, Set<Action> actions, int radius, long lifetimeTicks) {
        ServerLevel level = owner.level();
        MinecraftServer server = level.getServer();
        if (!server.isSameThread() || server.getPlayerList().getPlayer(owner.getUUID()) != owner || owner.isRemoved() || !owner.isAlive() || owner.isSpectator() || !owner.mayBuild()
                || actions.isEmpty() || radius < 0 || radius > 64 || lifetimeTicks <= 0 || lifetimeTicks > 168000 || !level.hasChunkAt(position) || !level.getWorldBorder().isWithinBounds(position)
                || !level.mayInteract(owner, position)) {
            return Optional.empty();
        }
        frozen = true;
        Policy policy = POLICIES.get(resolver);
        Optional<Machine> machine = resolve(policy, level, position);
        if (machine.isEmpty() || !machine.get().owner().equals(owner.getUUID())) {
            return Optional.empty();
        }
        Handle handle = new Handle(UUID.randomUUID());
        RiftOperationContext context = RiftOperationContext.machine(level, position, handle, resolver);
        for (Action action : actions) {
            if (!permitted(context, action, position, owner.getUUID())) {
                return Optional.empty();
            }
        }
        RiftAuthorizationData data = RiftAuthorizationData.get(server);
        long now = server.overworld().getGameTime();
        if (now > Long.MAX_VALUE - lifetimeTicks) {
            return Optional.empty();
        }
        data.grants.values().removeIf(grant -> grant.expires() <= now);
        if (data.grants.size() >= 65536) {
            return Optional.empty();
        }
        data.grants.put(handle.id(), new RiftAuthorizationData.Grant(handle.id(), owner.getUUID(), machine.get().instance(), resolver, level.dimension().identifier(), position,
                actions.stream().map(Enum::name).toList(), radius, now + lifetimeTicks));
        data.setDirty();
        return Optional.of(handle);
    }

    @Override
    public boolean revoke(ServerPlayer owner, Handle handle) {
        MinecraftServer server = owner.level().getServer();
        if (!server.isSameThread() || server.getPlayerList().getPlayer(owner.getUUID()) != owner) {
            return false;
        }
        RiftAuthorizationData data = RiftAuthorizationData.get(server);
        RiftAuthorizationData.Grant grant = data.grants.get(handle.id());
        if (grant == null || !grant.owner().equals(owner.getUUID())) {
            return false;
        }
        data.grants.remove(handle.id());
        data.setDirty();
        return true;
    }

    public static boolean authorized(RiftOperationContext context, Action action, BlockPos target) {
        ServerLevel level = context.level();
        MinecraftServer server = level.getServer();
        if (!server.isSameThread() || !level.hasChunkAt(target) || !level.getWorldBorder().isWithinBounds(target) || level.isOutsideBuildHeight(target)) {
            return false;
        }
        frozen = true;
        ServerPlayer owner = context.player();
        if (context.authorization() == null) {
            return owner != null && server.getPlayerList().getPlayer(owner.getUUID()) == owner && owner.level() == level && owner.isAlive() && !owner.isRemoved() && !owner.isSpectator()
                    && owner.mayBuild() && level.mayInteract(owner, target) && permitted(context, action, target);
        }
        RiftAuthorizationData.Grant grant = RiftAuthorizationData.get(server).grants.get(context.authorization().id());
        if (grant == null || !grant.dimension().equals(level.dimension().identifier()) || !grant.position().equals(context.machinePosition()) || grant.expires() <= server.overworld().getGameTime()
                || !grant.actions().contains(action.name()) || grant.position().distSqr(target) > (double) grant.radius() * grant.radius() || !level.hasChunkAt(grant.position())) {
            return false;
        }
        owner = server.getPlayerList().getPlayer(grant.owner());
        Policy policy = POLICIES.get(grant.resolver());
        Optional<Machine> machine = resolve(policy, level, grant.position());
        if (machine.isEmpty() || !machine.get().owner().equals(grant.owner()) || !machine.get().instance().equals(grant.instance())) {
            RiftAuthorizationData data = RiftAuthorizationData.get(server);
            data.grants.remove(grant.id());
            data.setDirty();
            return false;
        }
        return machine.isPresent() && machine.get().owner().equals(grant.owner()) && machine.get().instance().equals(grant.instance())
                && (owner != null || offline(policy, level, grant.position(), machine.get(), action, new AABB(target)))
                && (owner == null
                        || owner.isAlive() && !owner.isRemoved() && !owner.isSpectator() && owner.mayBuild() && level.mayInteract(owner, target) && level.mayInteract(owner, grant.position()))
                && permitted(context, action, grant.position()) && permitted(context, action, target) && RiftAuthorizationData.get(server).grants.get(grant.id()) == grant;
    }

    public static boolean valid(RiftOperationContext context, Action action, BlockPos target) {
        return authorized(context, action, target);
    }

    public static boolean validate(RiftOperationContext context, Action action, AABB volume) {
        if (!Double.isFinite(volume.minX) || !Double.isFinite(volume.minY) || !Double.isFinite(volume.minZ) || !Double.isFinite(volume.maxX) || !Double.isFinite(volume.maxY)
                || !Double.isFinite(volume.maxZ) || volume.getXsize() > 128 || volume.getYsize() > 128 || volume.getZsize() > 128) {
            return false;
        }
        BlockPos minimum = BlockPos.containing(volume.minX, volume.minY, volume.minZ);
        BlockPos maximum = BlockPos.containing(volume.maxX, volume.maxY, volume.maxZ);
        if (!authorized(context, action, minimum) || !authorized(context, action, maximum)) {
            return false;
        }
        if (context.authorization() != null) {
            RiftAuthorizationData.Grant grant = RiftAuthorizationData.get(context.level().getServer()).grants.get(context.authorization().id());
            if (grant == null) {
                return false;
            }
            Policy policy = POLICIES.get(grant.resolver());
            Optional<Machine> machine = resolve(policy, context.level(), grant.position());
            if (machine.isEmpty() || !machine.get().owner().equals(grant.owner()) || !machine.get().instance().equals(grant.instance())) {
                return false;
            }
            if (context.level().getServer().getPlayerList().getPlayer(grant.owner()) == null && !offline(policy, context.level(), grant.position(), machine.get(), action, volume)) {
                return false;
            }
        }
        for (BlockPos position : BlockPos.betweenClosed(minimum, maximum)) {
            if (!authorized(context, action, position)) {
                return false;
            }
        }
        return true;
    }

    public static Optional<UUID> actor(RiftOperationContext context) {
        MinecraftServer server = context.level().getServer();
        if (!server.isSameThread()) {
            return Optional.empty();
        }
        if (context.player() != null) {
            return server.getPlayerList().getPlayer(context.player().getUUID()) == context.player() ? Optional.of(context.player().getUUID()) : Optional.empty();
        }
        RiftAuthorizationData.Grant grant = RiftAuthorizationData.get(server).grants.get(context.authorization().id());
        return grant != null && grant.expires() > server.overworld().getGameTime() && grant.dimension().equals(context.level().dimension().identifier())
                && grant.position().equals(context.machinePosition()) ? Optional.of(grant.owner()) : Optional.empty();
    }

    private static boolean offline(Policy policy, ServerLevel level, BlockPos position, Machine machine, Action action, AABB volume) {
        try {
            return policy.offline() && policy.resolver().authorizeOffline(level, position, machine, action, volume);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static Optional<Machine> resolve(Policy policy, ServerLevel level, BlockPos position) {
        if (policy == null) {
            return Optional.empty();
        }
        try {
            return Objects.requireNonNull(policy.resolver().resolve(level, position));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static boolean permitted(RiftOperationContext context, Action action, BlockPos target) {
        return actor(context).map(owner -> permitted(context, action, target, owner)).orElse(false);
    }

    private static boolean permitted(RiftOperationContext context, Action action, BlockPos target, UUID owner) {
        try {
            return !NeoForge.EVENT_BUS.post(new RiftAuthorization.PermissionCheck(context, action, target, owner)).isCanceled();
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
