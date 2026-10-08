package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;

public record GuardianTable(
        SimpleWeightedRandomList<EntityType<?>> entries, float championChance, IntProvider perPost) {
    public static final float DEFAULT_CHAMPION_CHANCE = 0.15F;
    public static final IntProvider DEFAULT_PER_POST = UniformInt.of(1, 2);
    public static final Codec<GuardianTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    SimpleWeightedRandomList.wrappedCodecAllowingEmpty(BuiltInRegistries.ENTITY_TYPE.byNameCodec())
                            .fieldOf("entries")
                            .forGetter(GuardianTable::entries),
                    Codec.floatRange(0.0F, 1.0F)
                            .optionalFieldOf("champion_chance", DEFAULT_CHAMPION_CHANCE)
                            .forGetter(GuardianTable::championChance),
                    IntProvider.NON_NEGATIVE_CODEC
                            .optionalFieldOf("per_post", DEFAULT_PER_POST)
                            .forGetter(GuardianTable::perPost))
            .apply(instance, GuardianTable::new));
}
