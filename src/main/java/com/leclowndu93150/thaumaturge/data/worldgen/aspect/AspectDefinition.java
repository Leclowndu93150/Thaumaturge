package com.leclowndu93150.thaumaturge.data.worldgen.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.aspect.Aspect;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record AspectDefinition(
        ResourceKey<IAspect> key,
        int color,
        Optional<String> chatColor,
        int blend,
        List<ResourceKey<IAspect>> components) {
    IAspect create(HolderGetter<IAspect> aspects) {
        ResourceLocation id = key.location();
        List<Holder<IAspect>> parents =
                components.stream().<Holder<IAspect>>map(aspects::getOrThrow).toList();
        return new Aspect(
                id.getPath(),
                color,
                parents,
                chatColor,
                ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "textures/aspects/" + id.getPath() + ".png"),
                blend);
    }
}
