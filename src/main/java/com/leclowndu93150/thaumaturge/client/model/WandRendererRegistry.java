package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.api.wands.render.IWandRenderer;
import com.leclowndu93150.thaumaturge.api.wands.render.WandRenderContext;
import com.leclowndu93150.thaumaturge.api.wands.render.WandRenderers;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class WandRendererRegistry implements WandRenderers.Bindings {
    private volatile List<Registration> registrations = List.of();
    private long nextOrder;

    @Override
    public synchronized void register(int priority, Predicate<WandRenderContext> predicate, IWandRenderer renderer) {
        List<Registration> updated = new ArrayList<>(registrations);
        updated.add(new Registration(
                priority, nextOrder++, Objects.requireNonNull(predicate), Objects.requireNonNull(renderer)));
        updated.sort(Comparator.comparingInt(Registration::priority).reversed().thenComparingLong(Registration::order));
        registrations = List.copyOf(updated);
    }

    @Override
    public void render(WandRenderContext context) {
        for (Registration registration : registrations) {
            if (registration.predicate().test(context)) {
                registration.renderer().render(context);
                return;
            }
        }
        context.renderDefault();
    }

    private record Registration(
            int priority, long order, Predicate<WandRenderContext> predicate, IWandRenderer renderer) {}
}
