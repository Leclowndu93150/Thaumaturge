package com.leclowndu93150.thaumaturge.data.worldgen.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.aspect.Aspect;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public record AspectDefinition(ResourceKey<IAspect> key, int color, Optional<String> chatColor, int blend, List<ResourceKey<IAspect>> components) {
    IAspect create(HolderGetter<IAspect> aspects) {
        Identifier id = key.identifier();
        List<Holder<IAspect>> parents = components.stream().<Holder<IAspect>>map(aspects::getOrThrow).toList();
        return new Aspect(id.getPath(), color, parents, chatColor, Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/aspects/" + id.getPath() + ".png"), blend);
    }
}
