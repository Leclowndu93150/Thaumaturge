package com.leclowndu93150.thaumaturge.api.nodes;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.Optional;

public record NodeChange(AspectList current, AspectList base, NodeType type, Optional<NodeModifier> modifier, Optional<NodeBehaviorState> behavior) {
    public static NodeChange of(NodeSnapshot snapshot) {
        return new NodeChange(snapshot.current(), snapshot.base(), snapshot.type(), snapshot.modifier(), snapshot.behavior());
    }
}
