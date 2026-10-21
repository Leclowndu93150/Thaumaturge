package com.leclowndu93150.thaumaturge.api.nodes;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.Optional;

public record NodeSpec(AspectList current, AspectList base, NodeType type, Optional<NodeModifier> modifier, Optional<NodeBehaviorState> behavior) {
}
