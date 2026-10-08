package com.leclowndu93150.thaumaturge.content.spell.modifier;

import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStat;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public record StatChange(
        ResourceLocation stat,
        Operation operation,
        float base,
        float perLevel,
        Optional<String> level,
        Optional<Float> unwritten) {
    public static final Codec<StatChange> CODEC = RecordCodecBuilder.create(i -> i.group(
                    ResourceLocation.CODEC.fieldOf("stat").forGetter(StatChange::stat),
                    Operation.CODEC.fieldOf("operation").forGetter(StatChange::operation),
                    Codec.FLOAT.optionalFieldOf("base", 0.0F).forGetter(StatChange::base),
                    Codec.FLOAT.optionalFieldOf("per_level", 0.0F).forGetter(StatChange::perLevel),
                    Codec.STRING.optionalFieldOf("level").forGetter(StatChange::level),
                    Codec.FLOAT.optionalFieldOf("default").forGetter(StatChange::unwritten))
            .apply(i, StatChange::new));

    public SpellState applyTo(CastContext ctx, SpellState state) {
        float amount = base + perLevel * level.map(ctx::setting).orElse(0);
        SpellStat key = new SpellStat(stat, unwritten.orElse(operation.identity()));
        float current = state.get(key);
        return switch (operation) {
            case ADD -> state.with(key, current + amount);
            case MULTIPLY -> state.with(key, current * amount);
            case SET -> state.with(key, amount);
            case MAX -> state.with(key, Math.max(current, amount));
        };
    }

    public enum Operation implements StringRepresentable {
        ADD("add", 0.0F),
        MULTIPLY("multiply", 1.0F),
        SET("set", 0.0F),
        MAX("max", 0.0F);

        public static final Codec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values);

        private final String name;
        private final float identity;

        Operation(String name, float identity) {
            this.name = name;
            this.identity = identity;
        }

        float identity() {
            return identity;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
