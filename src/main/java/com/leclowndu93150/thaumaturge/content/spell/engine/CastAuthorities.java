package com.leclowndu93150.thaumaturge.content.spell.engine;

import com.leclowndu93150.thaumaturge.api.spell.cast.CastAuthority;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellAuthorities;
import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class CastAuthorities implements SpellAuthorities.Bindings {
    private record Policy(long revision, SpellAuthorities.Resolver resolver) {
    }

    private static final Map<Identifier, Policy> POLICIES = new ConcurrentHashMap<>();
    private static final int MAX_GRANTS = 4096;
    private static final int MAX_TOKENS = 4096;

    @Override
    public void register(Identifier scope, long revision, SpellAuthorities.Resolver resolver) {
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(resolver, "resolver");
        if (revision < 0 || POLICIES.putIfAbsent(scope, new Policy(revision, resolver)) != null) {
            throw new IllegalArgumentException("Invalid or duplicate spell authority scope: " + scope);
        }
    }

    @Override
    public Optional<CastAuthority> grant(ServerPlayer owner, LivingEntity source, Identifier scope, int radius, long lifetimeTicks, boolean allowOffline) {
        ServerLevel level = owner.level();
        if (!level.getServer().isSameThread() || level.getServer().getPlayerList().getPlayer(owner.getUUID()) != owner || !eligibleOwner(owner) || source.level() != level || !source.isAlive()
                || level.getEntity(source.getUUID()) != source || !(source instanceof SpellAuthorities.Source machine) || !owner.getUUID().equals(machine.spellOwner()) || radius < 1 || radius > 128
                || lifetimeTicks < 1 || lifetimeTicks > 72000) {
            return Optional.empty();
        }
        Policy policy = POLICIES.get(scope);
        if (policy == null) {
            return Optional.empty();
        }
        CastAuthorityData data = CastAuthorityData.get(level.getServer());
        long now = level.getServer().overworld().getGameTime();
        if (data.grants.values().removeIf(grant -> grant.expires <= now)) {
            data.setDirty();
        }
        if (data.grants.size() >= MAX_GRANTS || now > Long.MAX_VALUE - lifetimeTicks) {
            return Optional.empty();
        }
        CastAuthority authority = new CastAuthority(source.getUUID(), owner.getUUID(), level.dimension().identifier(), scope, UUID.randomUUID(), policy.revision(), UUID.randomUUID());
        var grant = new CastAuthorityData.Grant(authority, source.position(), radius, now + lifetimeTicks, allowOffline, 0, Map.of());
        data.grants.put(authority.grantId(), grant);
        data.setDirty();
        if (!permitted(level, grant, source.position(), null)) {
            data.grants.remove(authority.grantId(), grant);
            data.setDirty();
            return Optional.empty();
        }
        return Optional.of(authority);
    }

    @Override
    public boolean revoke(ServerPlayer owner, CastAuthority authority) {
        ServerLevel level = owner.level();
        if (!level.getServer().isSameThread() || level.getServer().getPlayerList().getPlayer(owner.getUUID()) != owner || !owner.getUUID().equals(authority.ownerId())) {
            return false;
        }
        CastAuthorityData data = CastAuthorityData.get(level.getServer());
        var grant = data.grants.get(authority.grantId());
        if (grant == null || !grant.authority.equals(authority)) {
            return false;
        }
        data.grants.remove(authority.grantId());
        data.setDirty();
        return true;
    }

    public static boolean canBegin(LivingEntity source, CastAuthority authority) {
        if (!(source.level() instanceof ServerLevel level) || !level.getServer().isSameThread() || !source.getUUID().equals(authority.sourceId())) {
            return false;
        }
        var grant = CastAuthorityData.get(level.getServer()).grants.get(authority.grantId());
        return grant != null && grant.state == 0 && grant.authority.equals(authority) && permitted(level, grant, source.position(), null);
    }

    public static boolean begin(LivingEntity source, CastAuthority authority, BooleanSupplier payment) {
        if (!canBegin(source, authority)) {
            return false;
        }
        ServerLevel level = (ServerLevel) source.level();
        CastAuthorityData data = CastAuthorityData.get(level.getServer());
        var grant = data.grants.get(authority.grantId());
        grant.state = 1;
        data.setDirty();
        if (!payment.getAsBoolean() || data.grants.get(authority.grantId()) != grant || !permitted(level, grant, source.position(), null)) {
            return false;
        }
        grant.state = 2;
        data.setDirty();
        return true;
    }

    public static boolean valid(ServerLevel level, CastAuthority authority) {
        if (!level.getServer().isSameThread()) {
            return false;
        }
        var grant = CastAuthorityData.get(level.getServer()).grants.get(authority.grantId());
        return grant != null && grant.state == 2 && grant.authority.equals(authority) && permitted(level, grant, grant.origin, null);
    }

    public static boolean valid(ServerLevel level, CastAuthority authority, Vec3 position, @Nullable Entity target) {
        if (!level.getServer().isSameThread()) {
            return false;
        }
        var grant = CastAuthorityData.get(level.getServer()).grants.get(authority.grantId());
        return grant != null && grant.state == 2 && grant.authority.equals(authority) && permitted(level, grant, position, target);
    }

    public static Optional<UUID> issue(ServerLevel level, CastAuthority authority) {
        if (!valid(level, authority)) {
            return Optional.empty();
        }
        CastAuthorityData data = CastAuthorityData.get(level.getServer());
        var grant = data.grants.get(authority.grantId());
        if (grant.tokens.size() >= MAX_TOKENS) {
            return Optional.empty();
        }
        UUID token = UUID.randomUUID();
        grant.tokens.put(token, -1L);
        data.setDirty();
        return Optional.of(token);
    }

    public static boolean claim(ServerLevel level, CastAuthority authority, UUID token, long sequence) {
        if (sequence < 0 || !valid(level, authority)) {
            return false;
        }
        CastAuthorityData data = CastAuthorityData.get(level.getServer());
        var grant = data.grants.get(authority.grantId());
        Long previous = grant.tokens.get(token);
        if (previous == null || previous == Long.MAX_VALUE || sequence != previous + 1) {
            return false;
        }
        grant.tokens.put(token, sequence);
        data.setDirty();
        return true;
    }

    private static boolean permitted(ServerLevel level, CastAuthorityData.Grant grant, Vec3 position, @Nullable Entity target) {
        return permitted(level, grant, position, target, true);
    }

    private static boolean eligibleOwner(ServerPlayer owner) {
        return owner.isAlive() && !owner.isRemoved() && !owner.isSpectator() && owner.mayBuild();
    }

    private static boolean permitted(ServerLevel level, CastAuthorityData.Grant grant, Vec3 position, @Nullable Entity target, boolean resolvePolicy) {
        CastAuthority authority = grant.authority;
        Policy policy = POLICIES.get(authority.scope());
        if (CastAuthorityData.get(level.getServer()).grants.get(authority.grantId()) != grant || policy == null || policy.revision() != authority.revision()
                || !level.dimension().identifier().equals(authority.dimension()) || grant.expires <= level.getServer().overworld().getGameTime() || !Double.isFinite(position.x)
                || !Double.isFinite(position.y) || !Double.isFinite(position.z) || position.distanceToSqr(grant.origin) > (double) grant.radius * grant.radius
                || !level.hasChunkAt(BlockPos.containing(position)) || !(level.getEntity(authority.sourceId()) instanceof LivingEntity source) || !source.isAlive()
                || source.position().distanceToSqr(grant.origin) > (double) grant.radius * grant.radius) {
            return false;
        }
        if (!(source instanceof SpellAuthorities.Source machine) || !authority.ownerId().equals(machine.spellOwner())) {
            CastAuthorityData data = CastAuthorityData.get(level.getServer());
            data.grants.remove(authority.grantId());
            data.setDirty();
            return false;
        }
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(authority.ownerId());
        if (owner == null) {
            if (!grant.offline || target instanceof Player || !Files.isRegularFile(level.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(authority.ownerId() + ".dat"))) {
                return false;
            }
        } else if (!eligibleOwner(owner) || !level.mayInteract(owner, BlockPos.containing(position)) || (target != null && SpellTargeting.isAlly(owner, target))) {
            return false;
        }
        if (target != null && (target.level() != level || !target.isAlive() || target.position().distanceToSqr(grant.origin) > (double) grant.radius * grant.radius)) {
            return false;
        }
        if (!resolvePolicy) {
            return true;
        }
        int state = grant.state;
        try {
            return policy.resolver().permits(level, authority, source, position, target) && grant.state == state && level.getEntity(authority.sourceId()) == source
                    && level.getServer().getPlayerList().getPlayer(authority.ownerId()) == owner && permitted(level, grant, position, target, false);
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
