package com.leclowndu93150.thaumaturge.mixin.world.entity;

import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ExperienceOrb.class)
public interface ExperienceOrbAccessor {
    @Invoker("setValue")
    void thaumaturge$setValue(int value);

    @Accessor("count")
    int thaumaturge$getCount();
}
