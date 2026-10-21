package com.leclowndu93150.thaumaturge.api.nodes;

import net.minecraft.util.RandomSource;

/** A server callback scope. Updates are staged and commit together with successfully encoded state. */
public interface NodeBehaviorContext {
    NodeSnapshot snapshot();

    RandomSource random();

    /** Stages one update; capture, unload and detach callbacks are notifications and cannot mutate the node. */
    NodeResult update(NodeChange change);
}
