package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public final class TTDamageTypeTags {
    public static final TagKey<DamageType> WARDED_BY_SHIELDS =
            TagKey.create(Registries.DAMAGE_TYPE, TTIds.rl("warded_by_shields"));

    private TTDamageTypeTags() {}
}
