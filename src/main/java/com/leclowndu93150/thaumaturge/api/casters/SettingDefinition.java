package com.leclowndu93150.thaumaturge.api.casters;

import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * The schema of one configurable knob on a focus element, such as projectile speed or
 * effect potency. Definitions describe the selectable values; the chosen value itself is an
 * integer stored in {@link FocusUnit#settings()}.
 *
 * <p>The schema lives on the element; values live in the spell data, and nothing is shared between
 * casts and screens.
 *
 * @param key      the setting id, unique within its element
 * @param nameKey  the translation key for the setting's display name
 * @param values   the selectable value model
 * @param research the research required to change this setting, or null when ungated
 * @since 1.0.0
 */
public record SettingDefinition(String key, String nameKey, Values values, @Nullable ResearchGate research) {
    /**
     * Creates an ungated definition.
     *
     * @param key     the setting id, unique within its element
     * @param nameKey the translation key for the setting's display name
     * @param values  the selectable value model
     */
    public SettingDefinition(String key, String nameKey, Values values) {
        this(key, nameKey, values, null);
    }

    /**
     * The value a fresh element starts at.
     *
     * @return the default value
     */
    public int defaultValue() {
        return values.valueAt(0);
    }

    /**
     * The ordered set of values a setting can take. Values are addressed by index so screens
     * can step through them.
     *
     * @since 1.0.0
     */
    public sealed interface Values permits IntRange, IntList {
        /**
         * The number of selectable values.
         *
         * @return the value count, at least 1
         */
        int count();

        /**
         * The value at an index.
         *
         * @param index the index; clamped into range
         * @return the value
         */
        int valueAt(int index);

        /**
         * A display component for the value at an index.
         *
         * @param index the index; clamped into range
         * @return the display text, never null
         */
        Component labelAt(int index);

        /**
         * The index whose value matches, used to seat screens on a stored value.
         *
         * @param value the value to find
         * @return the matching index, or 0 when no value matches
         */
        default int indexOf(int value) {
            for (int i = 0; i < count(); i++) {
                if (valueAt(i) == value) {
                    return i;
                }
            }
            return 0;
        }
    }

    /**
     * A contiguous inclusive integer range; the first value is the minimum.
     *
     * @param min the inclusive lower bound and default value
     * @param max the inclusive upper bound
     * @since 1.0.0
     */
    public record IntRange(int min, int max) implements Values {
        @Override
        public int count() {
            return max - min + 1;
        }

        @Override
        public int valueAt(int index) {
            return min + Mth.clamp(index, 0, count() - 1);
        }

        @Override
        public Component labelAt(int index) {
            return Component.literal(Integer.toString(valueAt(index)));
        }
    }

    /**
     * A fixed list of values, each with its own translation key for display; the first value
     * is the default.
     *
     * @param entries the selectable values in display order
     * @param labels  one translation key per value, same length and order as {@code entries}
     * @since 1.0.0
     */
    public record IntList(int[] entries, String[] labels) implements Values {
        @Override
        public int count() {
            return entries.length;
        }

        @Override
        public int valueAt(int index) {
            return entries[Mth.clamp(index, 0, entries.length - 1)];
        }

        @Override
        public Component labelAt(int index) {
            return Component.translatable(labels[Mth.clamp(index, 0, labels.length - 1)]);
        }
    }
}
