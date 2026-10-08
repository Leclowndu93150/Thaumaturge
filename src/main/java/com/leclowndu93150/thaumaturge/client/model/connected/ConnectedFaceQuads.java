package com.leclowndu93150.thaumaturge.client.model.connected;

import net.minecraft.core.Direction;

public interface ConnectedFaceQuads {
    boolean connects(Direction face);

    void addFace(Direction face, int connections, ConnectedQuads.Builder builder);
}
