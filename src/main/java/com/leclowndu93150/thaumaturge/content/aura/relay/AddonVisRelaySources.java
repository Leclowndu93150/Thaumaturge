package com.leclowndu93150.thaumaturge.content.aura.relay;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.IVisRelaySource;
import com.leclowndu93150.thaumaturge.api.aura.VisRelaySourceContext;
import com.leclowndu93150.thaumaturge.api.aura.VisRelaySourceRegistration;
import com.leclowndu93150.thaumaturge.api.aura.VisRelaySources;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

public final class AddonVisRelaySources implements VisRelaySources.Bindings {
    public static final AddonVisRelaySources INSTANCE = new AddonVisRelaySources();

    private static final ThreadLocal<Set<UUID>> ACTIVE_HOSTS = ThreadLocal.withInitial(HashSet::new);
    private final List<Registration> registrations = new ArrayList<>();

    private AddonVisRelaySources() {}

    @Override
    public VisRelaySourceRegistration register(VisRelaySourceContext context, IVisRelaySource source) {
        if (!context.level().getServer().isSameThread()) {
            throw new IllegalStateException("Vis relay sources must register on the server thread");
        }
        Registration registration = new Registration(context, source);
        registrations.add(registration);
        return registration;
    }

    public boolean isUsableAt(ServerLevel level, BlockPos position) {
        return find(level, position) != null;
    }

    public boolean isRegisteredAt(ServerLevel level, BlockPos position) {
        return registrations.stream()
                .anyMatch(registration -> registration.valid
                        && registration.context.level() == level
                        && registration.context.position().equals(position));
    }

    public int drain(
            ServerLevel level, BlockPos position, ResourceKey<IAspect> primal, int requested, boolean simulate) {
        if (requested <= 0) return 0;
        Registration registration = find(level, position);
        if (registration == null) return 0;
        UUID host = registration.context.hostIdentity();
        Set<UUID> active = ACTIVE_HOSTS.get();
        if (!active.add(host)) return 0;
        try (IVisRelaySource.Reservation reservation = registration.source.reserve(primal, requested)) {
            if (reservation == null || !registration.usable()) return 0;
            int reserved = clamp(reservation.amount(), requested);
            if (reserved <= 0 || simulate) return reserved;
            return clamp(reservation.commit(), reserved);
        } finally {
            active.remove(host);
            if (active.isEmpty()) ACTIVE_HOSTS.remove();
        }
    }

    private @Nullable Registration find(ServerLevel level, BlockPos position) {
        for (Iterator<Registration> iterator = registrations.iterator(); iterator.hasNext(); ) {
            Registration registration = iterator.next();
            if (!registration.valid) {
                iterator.remove();
                continue;
            }
            if (registration.context.level() == level
                    && registration.context.position().equals(position)
                    && registration.usable()) {
                return registration;
            }
        }
        return null;
    }

    private static int clamp(int supplied, int requested) {
        return Math.max(0, Math.min(requested, supplied));
    }

    private final class Registration implements VisRelaySourceRegistration {
        private final VisRelaySourceContext context;
        private final IVisRelaySource source;
        private boolean valid = true;

        private Registration(VisRelaySourceContext context, IVisRelaySource source) {
            this.context = context;
            this.source = source;
        }

        private boolean usable() {
            return valid
                    && context.level().getServer().isSameThread()
                    && context.level().isLoaded(context.position())
                    && source.isValid()
                    && source.isActive()
                    && source.isLinked();
        }

        @Override
        public void invalidate() {
            valid = false;
            registrations.remove(this);
        }

        @Override
        public boolean isValid() {
            return valid && source.isValid();
        }
    }
}
