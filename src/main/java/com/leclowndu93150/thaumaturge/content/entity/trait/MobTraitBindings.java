package com.leclowndu93150.thaumaturge.content.entity.trait;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;

public final class MobTraitBindings implements MobTraits.Bindings {
    @Override
    public boolean add(LivingEntity mob, Holder<MobTrait> trait) {
        return MobTraitEngine.add(mob, trait);
    }

    @Override
    public boolean remove(LivingEntity mob, Holder<MobTrait> trait) {
        return MobTraitEngine.remove(mob, trait);
    }

    @Override
    public List<Holder<MobTrait>> traits(LivingEntity mob) {
        return MobTraitEngine.traits(mob);
    }
}
