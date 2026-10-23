package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class TTStructureTags {
    public static final TagKey<Structure> ON_CULT_MAPS = TagKey.create(Registries.STRUCTURE, TTIds.rl("on_cult_maps"));

    private TTStructureTags() {}
}
