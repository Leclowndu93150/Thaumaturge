package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;

final class WeightedPick {
    private WeightedPick() {}

    static <T> Optional<T> pick(List<T> items, ToDoubleFunction<T> weight, RandomSource random) {
        double total = 0.0;
        for (T item : items) {
            total += Math.max(0.0, weight.applyAsDouble(item));
        }
        if (total <= 0.0) {
            return Optional.empty();
        }
        double roll = random.nextDouble() * total;
        for (T item : items) {
            roll -= Math.max(0.0, weight.applyAsDouble(item));
            if (roll < 0.0) {
                return Optional.of(item);
            }
        }
        for (int i = items.size() - 1; i >= 0; i--) {
            if (weight.applyAsDouble(items.get(i)) > 0.0) {
                return Optional.of(items.get(i));
            }
        }
        return Optional.empty();
    }

    static <T> Optional<T> byRoomWeight(List<T> items, Function<T, Holder<RoomType>> room, RandomSource random) {
        Optional<T> weighted = pick(items, item -> room.apply(item).value().weight(), random);
        return weighted.isPresent() || items.isEmpty()
                ? weighted
                : Optional.of(items.get(random.nextInt(items.size())));
    }

    static int any(IntList values, RandomSource random) {
        return values.getInt(random.nextInt(values.size()));
    }

    static void shuffle(int[] values, RandomSource random) {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = values[i];
            values[i] = values[j];
            values[j] = swap;
        }
    }
}
