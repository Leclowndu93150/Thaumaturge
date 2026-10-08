package com.leclowndu93150.thaumaturge.content.entity.loot;

import com.leclowndu93150.thaumaturge.registry.TTLootTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record NoNearbyKinCondition(double radius) implements LootItemCondition {
    public static final MapCodec<NoNearbyKinCondition> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.doubleRange(0.0, Double.MAX_VALUE)
                            .fieldOf("radius")
                            .forGetter(NoNearbyKinCondition::radius))
                    .apply(instance, NoNearbyKinCondition::new));

    public static LootItemCondition.Builder noNearbyKin(double radius) {
        return () -> new NoNearbyKinCondition(radius);
    }

    @Override
    public LootItemConditionType getType() {
        return TTLootTypes.NO_NEARBY_KIN.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY);
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity == null) {
            return true;
        }
        return entity.level()
                .getEntities(entity.getType(), entity.getBoundingBox().inflate(radius), other -> other != entity)
                .isEmpty();
    }
}
