package com.leclowndu93150.thaumaturge.api.casters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

/**
 * The serialized form of a complete spell: an ordered list of {@link FocusUnit units} plus
 * the base power, compiled complexity, and the id of the caster it belongs to. Packages are
 * immutable values; executing one never changes it.
 *
 * <p>Each unit encodes as {@code key} plus optional {@code settings} and {@code packages} fields,
 * so packages stored on items or entities keep decoding.
 *
 * @param power      the base power the cast starts at; node multipliers fold into a working
 *                   copy during execution
 * @param complexity the total complexity of the focus this package was compiled from
 * @param casterId   the id of the casting entity, empty when the package has none
 * @param units      the spell nodes in execution order
 * @since 1.0.0
 */
public record FocusPackage(float power, int complexity, Optional<UUID> casterId, List<FocusUnit> units) {
    private static final float DEFAULT_POWER = 1.0F;

    /**
     * Serializes power, complexity, the caster id when known, and every unit with its
     * settings and, for splits, branch packages. Decoding fails when a unit references an
     * element id absent from the bound registry.
     */
    public static final Codec<FocusPackage> CODEC = Codec.recursive("FocusPackage", self -> {
        Codec<FocusUnit> unitCodec = RecordCodecBuilder.<FocusUnit>create(i -> i
                .group(Identifier.CODEC.fieldOf("key").forGetter(FocusUnit::element), Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("settings", Map.of()).forGetter(FocusUnit::settings),
                        self.listOf().optionalFieldOf("packages", List.of()).forGetter(FocusUnit::branches))
                .apply(i, FocusUnit::new)).validate(FocusPackage::validateUnit);
        return RecordCodecBuilder
                .create(i -> i
                        .group(Codec.FLOAT.optionalFieldOf("power", DEFAULT_POWER).forGetter(FocusPackage::power), Codec.INT.optionalFieldOf("complexity", 0).forGetter(FocusPackage::complexity),
                                UUIDUtil.CODEC.optionalFieldOf("caster").forGetter(FocusPackage::casterId), unitCodec.listOf().fieldOf("nodes").forGetter(FocusPackage::units))
                        .apply(i, FocusPackage::new));
    });

    /** Network encoding mirroring {@link #CODEC}. */
    public static final StreamCodec<ByteBuf, FocusPackage> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    /**
     * Canonicalizes the components into immutable copies.
     */
    public FocusPackage {
        units = List.copyOf(units);
    }

    /**
     * Starts building a package.
     *
     * @return a fresh builder with power 1.0, complexity 0, and no caster
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A hash over every unit's element id and setting values, used to group identical foci
     * regardless of runtime state.
     *
     * @return the sorting hash
     */
    public int sortingHash() {
        StringBuilder sb = new StringBuilder();
        appendSortKey(this, sb);
        return sb.toString().hashCode();
    }

    private static void appendSortKey(FocusPackage pack, StringBuilder sb) {
        for (FocusUnit unit : pack.units) {
            sb.append(unit.element());
            for (Map.Entry<String, Integer> setting : new TreeMap<>(unit.settings()).entrySet()) {
                sb.append(setting.getValue());
            }
            for (FocusPackage branch : unit.branches()) {
                appendSortKey(branch, sb);
            }
        }
    }

    private static DataResult<FocusUnit> validateUnit(FocusUnit unit) {
        if (FocusEngine.element(unit.element()) == null) {
            return DataResult.error(() -> "Unknown focus element: " + unit.element());
        }
        return DataResult.success(unit);
    }

    /**
     * Accumulates units into an immutable {@link FocusPackage}.
     *
     * @since 1.0.0
     */
    public static final class Builder {
        private final List<FocusUnit> units = new ArrayList<>();
        private float power = DEFAULT_POWER;
        private int complexity;
        private @Nullable UUID casterId;

        private Builder() {}

        /**
         * Sets the base power.
         *
         * @param power the base power
         * @return this builder
         */
        public Builder power(float power) {
            this.power = power;
            return this;
        }

        /**
         * Sets the compiled complexity.
         *
         * @param complexity the complexity value
         * @return this builder
         */
        public Builder complexity(int complexity) {
            this.complexity = complexity;
            return this;
        }

        /**
         * Assigns the caster by entity.
         *
         * @param caster the casting entity
         * @return this builder
         */
        public Builder caster(LivingEntity caster) {
            this.casterId = caster.getUUID();
            return this;
        }

        /**
         * Assigns the caster by id.
         *
         * @param casterId the caster id, or null for none
         * @return this builder
         */
        public Builder caster(@Nullable UUID casterId) {
            this.casterId = casterId;
            return this;
        }

        /**
         * Appends a unit.
         *
         * @param unit the unit to append
         * @return this builder
         */
        public Builder add(FocusUnit unit) {
            units.add(unit);
            return this;
        }

        /**
         * Appends an element with default settings.
         *
         * @param element the focus element id
         * @return this builder
         */
        public Builder add(Identifier element) {
            return add(FocusUnit.of(element));
        }

        /**
         * Appends an element with explicit settings.
         *
         * @param element  the focus element id
         * @param settings the setting values
         * @return this builder
         */
        public Builder add(Identifier element, Map<String, Integer> settings) {
            return add(FocusUnit.of(element, settings));
        }

        /**
         * Builds the package.
         *
         * @return the immutable package
         */
        public FocusPackage build() {
            return new FocusPackage(power, complexity, Optional.ofNullable(casterId), units);
        }
    }
}
