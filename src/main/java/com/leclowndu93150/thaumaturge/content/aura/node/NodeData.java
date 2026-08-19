package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record NodeData(NodeType type, Optional<NodeModifier> modifier, AspectList aspects, AspectList aspectsBase) {
    public static final Codec<NodeData> CODEC = RecordCodecBuilder
            .create(instance -> instance
                    .group(NodeType.CODEC.fieldOf("type").forGetter(NodeData::type), NodeModifier.CODEC.optionalFieldOf("modifier").forGetter(NodeData::modifier),
                            AspectList.CODEC.fieldOf("aspects").forGetter(NodeData::aspects), AspectList.CODEC.fieldOf("aspects_base").forGetter(NodeData::aspectsBase))
                    .apply(instance, NodeData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, NodeData> STREAM_CODEC = StreamCodec.composite(NodeType.STREAM_CODEC, NodeData::type, ByteBufCodecs.optional(NodeModifier.STREAM_CODEC),
            NodeData::modifier, AspectList.STREAM_CODEC, NodeData::aspects, AspectList.STREAM_CODEC, NodeData::aspectsBase, NodeData::new);
}
