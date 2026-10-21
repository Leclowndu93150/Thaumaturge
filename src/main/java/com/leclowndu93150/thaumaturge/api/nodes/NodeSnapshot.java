package com.leclowndu93150.thaumaturge.api.nodes;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import java.util.Optional;
import java.util.UUID;

public record NodeSnapshot(UUID identity, long revision, AspectList current, AspectList base, Optional<AspectList> originalBase, NodeType type, Optional<NodeModifier> modifier, boolean energized,
        boolean locked, boolean busy, Optional<NodeBehaviorState> behavior) {
    private static final Codec<AspectList> ASPECTS_CODEC = AspectInstance.CODEC.listOf(0, 256).flatXmap(entries -> {
        long total = 0;
        for (AspectInstance entry : entries) {
            total += entry.amount();
            if (entry.amount() <= 0 || total > 1_000_000) {
                return DataResult.error(() -> "Invalid captured node aspect amount");
            }
        }
        return DataResult.success(AspectList.ofEntries(entries));
    }, aspects -> DataResult.success(aspects.entries()));
    public static final Codec<NodeSnapshot> CODEC = RecordCodecBuilder
            .create(instance -> instance
                    .group(UUIDUtil.CODEC.fieldOf("identity").forGetter(NodeSnapshot::identity),
                            Codec.LONG.validate(value -> value >= 0 ? DataResult.success(value) : DataResult.error(() -> "Negative node revision")).fieldOf("revision")
                                    .forGetter(NodeSnapshot::revision),
                            ASPECTS_CODEC.fieldOf("current").forGetter(NodeSnapshot::current), ASPECTS_CODEC.fieldOf("base").forGetter(NodeSnapshot::base),
                            ASPECTS_CODEC.optionalFieldOf("original_base").forGetter(NodeSnapshot::originalBase), NodeType.CODEC.fieldOf("type").forGetter(NodeSnapshot::type),
                            NodeModifier.CODEC.optionalFieldOf("modifier").forGetter(NodeSnapshot::modifier), Codec.BOOL.fieldOf("energized").forGetter(NodeSnapshot::energized),
                            Codec.BOOL.fieldOf("locked").forGetter(NodeSnapshot::locked), Codec.BOOL.fieldOf("busy").forGetter(NodeSnapshot::busy),
                            NodeBehaviorState.CODEC.optionalFieldOf("behavior").forGetter(NodeSnapshot::behavior))
                    .apply(instance, NodeSnapshot::new));
}
