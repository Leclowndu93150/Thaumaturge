package com.leclowndu93150.thaumaturge.api.spell.part;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * One integer knob on a part, shown as a spinner in the focal manipulator.
 *
 * <p>The values run from {@link #min()} to {@link #max()} in increments of {@link #step()}. Every
 * step above the minimum adds {@link #complexity()} complexity and {@link #vis()} vis per cast.
 * With {@link #labels()} present the spinner cycles named options instead of numbers.
 *
 * @param key          the setting id, unique within the part
 * @param min          the inclusive minimum
 * @param max          the inclusive maximum, reachable from {@code min} in whole steps
 * @param step         the increment between values, at least 1
 * @param defaultValue the value of a freshly placed node
 * @param complexity   the complexity added per step above the minimum
 * @param vis          the vis per cast added per step above the minimum
 * @param labels       one translation key per value, or empty for numbers
 * @param name         the display name key, or empty for {@code spell.thaumaturge.setting.<key>}
 * @since 1.0.0
 */
public record SettingSpec(
        String key,
        int min,
        int max,
        int step,
        int defaultValue,
        float complexity,
        float vis,
        Optional<List<String>> labels,
        Optional<String> name) {
    /** Disk and network codec. {@code step} defaults to 1 and {@code default} to {@code min}. */
    public static final Codec<SettingSpec> CODEC = RecordCodecBuilder.<SettingSpec>create(i -> i.group(
                            Codec.STRING.fieldOf("key").forGetter(SettingSpec::key),
                            Codec.INT.fieldOf("min").forGetter(SettingSpec::min),
                            Codec.INT.fieldOf("max").forGetter(SettingSpec::max),
                            Codec.INT.optionalFieldOf("step", 1).forGetter(SettingSpec::step),
                            Codec.INT.optionalFieldOf("default").forGetter(spec -> Optional.of(spec.defaultValue())),
                            Codec.FLOAT.optionalFieldOf("complexity", 0.0F).forGetter(SettingSpec::complexity),
                            Codec.FLOAT.optionalFieldOf("vis", 0.0F).forGetter(SettingSpec::vis),
                            Codec.STRING.listOf().optionalFieldOf("labels").forGetter(SettingSpec::labels),
                            Codec.STRING.optionalFieldOf("name").forGetter(SettingSpec::name))
                    .apply(
                            i,
                            (key, min, max, step, def, complexity, vis, labels, name) -> new SettingSpec(
                                    key, min, max, step, def.orElse(min), complexity, vis, labels, name)))
            .validate(SettingSpec::check);

    /**
     * Snaps a value onto this spec's range and step grid.
     *
     * @param value the raw value
     * @return the nearest valid value
     */
    public int clamp(int value) {
        int clamped = Mth.clamp(value, min, max);
        return min + Math.round((clamped - min) / (float) step) * step;
    }

    /**
     * The number of steps a value sits above the minimum.
     *
     * @param value the value, snapped first
     * @return the step count, zero or more
     */
    public int steps(int value) {
        return (clamp(value) - min) / step;
    }

    /**
     * The value one step up or down, clamped to the range.
     *
     * @param value the current value
     * @param delta the number of steps to move, negative for down
     * @return the new value
     */
    public int offset(int value, int delta) {
        return clamp(clamp(value) + delta * step);
    }

    /**
     * The translation key of this setting's display name.
     *
     * @return the key
     */
    public String nameKey() {
        return name.orElse("spell.thaumaturge.setting." + key);
    }

    /**
     * The text shown for a value.
     *
     * @param value the value, snapped first
     * @return the label, or the number when the spec has no labels
     */
    public Component label(int value) {
        int clamped = clamp(value);
        if (labels.isPresent()) {
            return Component.translatable(labels.get().get(steps(clamped)));
        }
        return Component.literal(Integer.toString(clamped));
    }

    private static DataResult<SettingSpec> check(SettingSpec spec) {
        if (spec.step < 1 || spec.max < spec.min || (spec.max - spec.min) % spec.step != 0) {
            return DataResult.error(() -> "Setting " + spec.key + " needs max reachable from min in whole steps");
        }
        if (spec.defaultValue < spec.min
                || spec.defaultValue > spec.max
                || (spec.defaultValue - spec.min) % spec.step != 0) {
            return DataResult.error(() -> "Setting " + spec.key + " default lies off its range");
        }
        if (spec.labels.isPresent() && spec.labels.get().size() != (spec.max - spec.min) / spec.step + 1) {
            return DataResult.error(() -> "Setting " + spec.key + " needs one label per value");
        }
        return DataResult.success(spec);
    }
}
