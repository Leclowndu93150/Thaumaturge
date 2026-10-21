package com.leclowndu93150.thaumaturge.api.nodes;

import com.leclowndu93150.thaumaturge.TTIds;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public record NodeBehaviorType<S>(int version, Codec<S> codec, NodeBehavior<S> behavior, Optional<Codec<S>> visualCodec) {
    public static final ResourceKey<Registry<NodeBehaviorType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(TTIds.rl("node_behavior_type"));

    public NodeBehaviorType(int version, Codec<S> codec, NodeBehavior<S> behavior) {
        this(version, codec, behavior, Optional.empty());
    }

    public NodeBehaviorType {
        if (version < 0) {
            throw new IllegalArgumentException("Negative node behavior version");
        }
        Objects.requireNonNull(codec);
        Objects.requireNonNull(behavior);
        Objects.requireNonNull(visualCodec);
    }

    public DataResult<NodeBehaviorState> encode(Identifier id, S state) {
        return codec.encodeStart(JsonOps.INSTANCE, state).flatMap(encoded -> {
            try {
                return DataResult.success(new NodeBehaviorState(id, version, encoded.toString()));
            } catch (IllegalArgumentException exception) {
                return DataResult.error(exception::getMessage);
            }
        });
    }

    public DataResult<S> decode(NodeBehaviorState state) {
        if (state.version() != version) {
            return DataResult.error(() -> "Unsupported node behavior version");
        }
        try {
            return codec.parse(JsonOps.INSTANCE, JsonParser.parseString(state.data()));
        } catch (RuntimeException exception) {
            return DataResult.error(() -> "Invalid node behavior state");
        }
    }
}
