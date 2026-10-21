package com.leclowndu93150.thaumaturge.api.nodes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public record NodeBehaviorState(Identifier id, int version, String data) {
    public static final int MAX_DATA_BYTES = 16384;
    private static final Codec<String> DATA_CODEC = Codec.STRING
            .validate(data -> data.getBytes(StandardCharsets.UTF_8).length <= MAX_DATA_BYTES ? DataResult.success(data) : DataResult.error(() -> "Node behavior state exceeds size limit"));
    public static final Codec<NodeBehaviorState> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(Identifier.CODEC.fieldOf("id").forGetter(NodeBehaviorState::id), Codec.intRange(0, Integer.MAX_VALUE).fieldOf("version").forGetter(NodeBehaviorState::version),
                    DATA_CODEC.fieldOf("data").forGetter(NodeBehaviorState::data)).apply(instance, NodeBehaviorState::new));

    public NodeBehaviorState {
        Objects.requireNonNull(id);
        Objects.requireNonNull(data);
        if (version < 0 || data.getBytes(StandardCharsets.UTF_8).length > MAX_DATA_BYTES) {
            throw new IllegalArgumentException("Invalid node behavior state");
        }
    }
}
