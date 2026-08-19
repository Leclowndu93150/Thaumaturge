package com.leclowndu93150.thaumaturge.api.aura;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.resources.ResourceKey;

/**
 * Data map value listing the aspects associated with a biome. Worldgen uses it to bias vis crystal
 * cluster types toward the biome's character; other systems may sample it for biome-flavoured
 * effects.
 *
 * @param aspects the candidate aspects for the biome; never empty
 * @since 1.0.0
 */
public record BiomeAspects(List<ResourceKey<IAspect>> aspects) {
    public static final Codec<BiomeAspects> CODEC = ResourceKey.codec(IAspect.REGISTRY_KEY).listOf().xmap(BiomeAspects::new, BiomeAspects::aspects);
}
