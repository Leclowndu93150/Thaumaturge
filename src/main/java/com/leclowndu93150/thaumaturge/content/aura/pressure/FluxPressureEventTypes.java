package com.leclowndu93150.thaumaturge.content.aura.pressure;

import java.util.List;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public final class FluxPressureEventTypes {
    public static final List<FluxPressureEvent> ALL = List.of(
            new WispPressureEvent(),
            new LightningPressureEvent(),
            new RainPressureEvent(),
            new CrawlerPressureEvent(),
            new WarpPressureEvent(),
            new ExhaustPressureEvent(),
            new NodeMutationPressureEvent());

    private FluxPressureEventTypes() {}

    public static @Nullable FluxPressureEvent byName(String name) {
        for (FluxPressureEvent event : ALL) {
            if (event.name().equals(name)) {
                return event;
            }
        }
        return null;
    }

    public static FluxPressureEvent choose(RandomSource random) {
        int total = 0;
        for (FluxPressureEvent event : ALL) {
            total += event.weight();
        }
        int roll = random.nextInt(total);
        for (FluxPressureEvent event : ALL) {
            roll -= event.weight();
            if (roll < 0) {
                return event;
            }
        }
        return ALL.getFirst();
    }
}
