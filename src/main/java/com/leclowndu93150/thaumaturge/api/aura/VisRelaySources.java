package com.leclowndu93150.thaumaturge.api.aura;

import java.util.Objects;

/** Registration facade for addon sources consumed by native Thaumaturge vis relays. */
public final class VisRelaySources {
    private static Bindings bindings;

    private VisRelaySources() {}

    public static VisRelaySourceRegistration register(VisRelaySourceContext context, IVisRelaySource source) {
        if (bindings == null) throw new IllegalStateException("Vis relay sources are not initialized");
        return bindings.register(Objects.requireNonNull(context), Objects.requireNonNull(source));
    }

    /** Internal binding installed by Thaumaturge during initialization. */
    public static void bind(Bindings implementation) {
        bindings = Objects.requireNonNull(implementation);
    }

    public interface Bindings {
        VisRelaySourceRegistration register(VisRelaySourceContext context, IVisRelaySource source);
    }
}
