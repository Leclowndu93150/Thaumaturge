package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

public record TaintedProfile(
        Map<Holder<Attribute>, Double> attributes,
        boolean huntsVillagers,
        boolean huntsAnimals,
        List<Holder<MobTrait>> traits,
        boolean naturalSpawns,
        Optional<ResourceKey<LootTable>> lootTable) {
    public static final Codec<TaintedProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(Attribute.CODEC, Codec.DOUBLE)
                            .optionalFieldOf("attributes", Map.of())
                            .forGetter(TaintedProfile::attributes),
                    Codec.BOOL.optionalFieldOf("hunts_villagers", false).forGetter(TaintedProfile::huntsVillagers),
                    Codec.BOOL.optionalFieldOf("hunts_animals", false).forGetter(TaintedProfile::huntsAnimals),
                    RegistryFixedCodec.create(MobTrait.REGISTRY_KEY)
                            .listOf()
                            .optionalFieldOf("traits", List.of())
                            .forGetter(TaintedProfile::traits),
                    Codec.BOOL.optionalFieldOf("natural_spawns", false).forGetter(TaintedProfile::naturalSpawns),
                    ResourceKey.codec(Registries.LOOT_TABLE)
                            .optionalFieldOf("loot_table")
                            .forGetter(TaintedProfile::lootTable))
            .apply(instance, TaintedProfile::new));

    public static @Nullable TaintedProfile of(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).getData(TTDataMaps.TAINTED_PROFILE);
    }
}
