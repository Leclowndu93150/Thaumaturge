package com.leclowndu93150.thaumaturge.content.entity.loot;

import com.leclowndu93150.thaumaturge.registry.TTLootTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public record DifficultyValue(Map<Difficulty, Float> values, float fallback) implements NumberProvider {
    public static final MapCodec<DifficultyValue> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.unboundedMap(Difficulty.CODEC, Codec.FLOAT)
                            .fieldOf("values")
                            .forGetter(DifficultyValue::values),
                    Codec.FLOAT.optionalFieldOf("default", 0.0F).forGetter(DifficultyValue::fallback))
            .apply(instance, DifficultyValue::new));

    public static DifficultyValue of(Map<Difficulty, Float> values, float fallback) {
        return new DifficultyValue(values, fallback);
    }

    @Override
    public float getFloat(LootContext context) {
        return values.getOrDefault(context.getLevel().getDifficulty(), fallback);
    }

    @Override
    public LootNumberProviderType getType() {
        return TTLootTypes.BY_DIFFICULTY.get();
    }
}
