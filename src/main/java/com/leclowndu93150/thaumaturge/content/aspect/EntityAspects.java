package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IEntityAspectSource;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public final class EntityAspects {
    private EntityAspects() {}

    public static AspectList of(Entity entity) {
        AspectList aspects =
                entity instanceof IEntityAspectSource source ? source.entityAspects() : of(entity.getType());
        if (entity instanceof LivingEntity living) {
            for (Holder<MobTrait> trait : MobTraits.traits(living)) {
                aspects = aspects.add(trait.value().aspects(living));
            }
        }
        return aspects;
    }

    public static AspectList of(EntityType<?> type) {
        AspectList aspects = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).getData(AspectDataMaps.ENTITY_ASPECTS);
        return aspects == null ? AspectList.EMPTY : aspects;
    }
}
