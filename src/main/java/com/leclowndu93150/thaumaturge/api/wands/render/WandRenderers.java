package com.leclowndu93150.thaumaturge.api.wands.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Registration and dispatch facade for custom per-wand renderers.
 *
 * <p>The renderer with the highest priority whose predicate matches is selected. Registrations at
 * equal priority keep their registration order. When no predicate matches, the standard wand model
 * is rendered. This client API accepts registration before or during client setup, regardless of mod loading order.
 */
public final class WandRenderers {
    private static volatile Bindings impl;
    private static final List<PendingRegistration> pending = new ArrayList<>();

    private WandRenderers() {}

    /**
     * Registers a renderer selection rule.
     *
     * @param priority higher values are considered first
     * @param predicate selects stacks and wand parts handled by the renderer
     * @param renderer renderer to invoke when the predicate matches
     */
    public static synchronized void register(
            int priority, Predicate<WandRenderContext> predicate, IWandRenderer renderer) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(renderer);
        if (impl == null) {
            pending.add(new PendingRegistration(priority, predicate, renderer));
        } else {
            impl.register(priority, predicate, renderer);
        }
    }

    /**
     * Dispatches a render context to the selected renderer or the standard renderer.
     *
     * @param context render input and targets
     */
    public static void render(WandRenderContext context) {
        bindingOrThrow().render(context);
    }

    /**
     * Installs the client implementation. Thaumaturge already binds this during client setup.
     * <br>
     * Do not call this method externally as it will throw.
     *
     * @param bindings renderer registry implementation
     */
    public static synchronized void bind(Bindings bindings) {
        if (impl != null) {
            throw new IllegalStateException("WandRenderers already bound");
        }
        Objects.requireNonNull(bindings);
        for (PendingRegistration registration : pending) {
            bindings.register(registration.priority(), registration.predicate(), registration.renderer());
        }
        pending.clear();
        impl = bindings;
    }

    private record PendingRegistration(int priority, Predicate<WandRenderContext> predicate, IWandRenderer renderer) {}

    private static Bindings bindingOrThrow() {
        if (impl == null) {
            throw new IllegalStateException("WandRenderers accessed before client setup");
        }
        return impl;
    }

    /**
     * Operations delegated to the client-side renderer registry.
     */
    public interface Bindings {
        /**
         * Registers a renderer selection rule.
         *
         * @param priority higher values are considered first
         * @param predicate selects stacks and wand parts handled by the renderer
         * @param renderer renderer invoked when the predicate matches
         */
        void register(int priority, Predicate<WandRenderContext> predicate, IWandRenderer renderer);

        /**
         * Dispatches to a matching custom renderer or the standard renderer.
         *
         * @param context render input and targets
         */
        void render(WandRenderContext context);
    }
}
