package com.leclowndu93150.thaumaturge.api.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;

/**
 * Immutable per-aspect vis storage held by a wand as a data component. Amounts are in centivis:
 * one hundred centivis equal one vis unit. Aspects are keyed by {@link ResourceKey} so the record
 * stays valid across datapack reloads.
 *
 * <p>All mutators return a new instance. Use {@link #CODEC} for persistence and
 * {@link #STREAM_CODEC} for network sync. Read access from an item stack is exposed through
 * {@link WandAccess}.
 *
 * @param centivis the per-aspect centivis amounts; copied defensively on construction
 * @since 1.0.0
 */
public record WandVis(Map<ResourceKey<IAspect>, Integer> centivis) {
    public static final WandVis EMPTY = new WandVis(Map.of());

    public static final Codec<WandVis> CODEC = Codec.unboundedMap(ResourceKey.codec(IAspect.REGISTRY_KEY), ExtraCodecs.NON_NEGATIVE_INT).xmap(WandVis::new, WandVis::centivis);

    public static final StreamCodec<ByteBuf, WandVis> STREAM_CODEC = ByteBufCodecs.map(LinkedHashMap::new, ResourceKey.streamCodec(IAspect.REGISTRY_KEY), ByteBufCodecs.VAR_INT)
            .map(map -> new WandVis(Map.copyOf(map)), wandVis -> new LinkedHashMap<>(wandVis.centivis));

    public WandVis {
        centivis = Map.copyOf(centivis);
    }

    public int amount(ResourceKey<IAspect> aspect) {
        return centivis.getOrDefault(aspect, 0);
    }

    public WandVis with(ResourceKey<IAspect> aspect, int amount) {
        Map<ResourceKey<IAspect>, Integer> updated = new LinkedHashMap<>(centivis);
        if (amount <= 0) {
            updated.remove(aspect);
        } else {
            updated.put(aspect, amount);
        }
        return new WandVis(updated);
    }

    public boolean isEmpty() {
        return centivis.isEmpty();
    }
}
