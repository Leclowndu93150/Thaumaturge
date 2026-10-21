package com.leclowndu93150.thaumaturge.api.nodes;

@FunctionalInterface
public interface NodeBehavior<S> {
    S apply(NodeBehaviorContext context, S state, Event event);

    enum Event {
        ATTACH, DETACH, LOAD, UNLOAD, CAPTURE, RELEASE, TICK
    }
}
