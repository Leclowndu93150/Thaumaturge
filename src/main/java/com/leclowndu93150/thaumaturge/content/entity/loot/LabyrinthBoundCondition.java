package com.leclowndu93150.thaumaturge.content.entity.loot;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthHelper;
import com.leclowndu93150.thaumaturge.registry.TTLootTypes;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class LabyrinthBoundCondition implements LootItemCondition {
    private static final LabyrinthBoundCondition INSTANCE = new LabyrinthBoundCondition();
    public static final MapCodec<LabyrinthBoundCondition> MAP_CODEC = MapCodec.unit(INSTANCE);

    private LabyrinthBoundCondition() {}

    public static LootItemCondition.Builder labyrinthBound() {
        return () -> INSTANCE;
    }

    @Override
    public LootItemConditionType getType() {
        return TTLootTypes.LABYRINTH_BOUND.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY);
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        return entity != null && LabyrinthHelper.isLabyrinthBound(entity);
    }
}
