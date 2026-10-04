package com.leclowndu93150.thaumaturge.api.client.golems;

import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Lets addons draw their golem accessories. Posted to every mod's event bus each time the golem
 * renderer is created, which happens on every resource reload, so handlers must register the same
 * renderers every time. Client only.
 *
 * <p>An accessory with no renderer is still worn and still applies its stats, but draws nothing.
 * Several renderers may be registered for one accessory, on one anchor or on several; they draw in
 * registration order. Thaumaturge registers its own accessories through this event.
 *
 * @since 1.0.0
 */
public final class RegisterGolemAccessoryRenderersEvent extends Event implements IModBusEvent {
    private final EntityRendererProvider.Context context;
    private final List<Registration> registrations = new ArrayList<>();

    /**
     * Creates the event. Intended for the implementation that fires it.
     *
     * @param context the context the golem renderer is being created with
     */
    public RegisterGolemAccessoryRenderersEvent(EntityRendererProvider.Context context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    /**
     * The context the golem renderer is being created with, for baking model layers and resolving
     * item models.
     *
     * @return the renderer context
     */
    public EntityRendererProvider.Context context() {
        return context;
    }

    /**
     * Registers a renderer for an accessory.
     *
     * @param accessory the accessory to draw
     * @param anchor    the body part the renderer draws on
     * @param renderer  the renderer
     * @throws NullPointerException when an argument is null
     */
    public void register(GolemAccessory accessory, GolemAccessoryAnchor anchor, GolemAccessoryRenderer renderer) {
        registrations.add(new Registration(accessory, anchor, renderer));
    }

    /**
     * Returns the renderers registered by this event, in registration order. Intended for the
     * implementation that fires the event; addons register through {@link #register}.
     *
     * @return an unmodifiable copy of the registrations
     */
    public List<Registration> registrations() {
        return List.copyOf(registrations);
    }

    /**
     * One renderer registered for one accessory and anchor.
     *
     * @param accessory the accessory to draw
     * @param anchor    the body part the renderer draws on
     * @param renderer  the renderer
     * @since 1.0.0
     */
    public record Registration(GolemAccessory accessory, GolemAccessoryAnchor anchor, GolemAccessoryRenderer renderer) {
        /**
         * Validates the components.
         *
         * @throws NullPointerException when a component is null
         */
        public Registration {
            Objects.requireNonNull(accessory, "accessory");
            Objects.requireNonNull(anchor, "anchor");
            Objects.requireNonNull(renderer, "renderer");
        }
    }
}
