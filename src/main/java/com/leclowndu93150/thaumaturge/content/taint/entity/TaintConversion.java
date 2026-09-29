package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

public record TaintConversion(EntityType<?> into, boolean naturalSpawns) {
    public static final Codec<TaintConversion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("into").forGetter(TaintConversion::into),
                    Codec.BOOL.optionalFieldOf("natural_spawns", true).forGetter(TaintConversion::naturalSpawns))
            .apply(instance, TaintConversion::new));
}
