package com.leclowndu93150.thaumaturge.client.model.mesh;

import java.util.List;

public record TTMesh(List<TTMeshPart> parts) {
    public static final TTMesh EMPTY = new TTMesh(List.of());
}
