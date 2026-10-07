package com.leclowndu93150.thaumaturge.data.model;

import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.client.model.TTModelsHandlers;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import org.jspecify.annotations.Nullable;

public final class TTMeshLoaderBuilder extends CustomLoaderBuilder {
    private @Nullable Identifier mesh;
    private boolean flipV;
    private boolean cornerSpace;

    public TTMeshLoaderBuilder() {
        super(TTModelsHandlers.MESH_LOADER_ID, false);
    }

    public TTMeshLoaderBuilder mesh(Identifier mesh) {
        this.mesh = Objects.requireNonNull(mesh);
        return this;
    }

    public TTMeshLoaderBuilder flipV(boolean flipV) {
        this.flipV = flipV;
        return this;
    }

    public TTMeshLoaderBuilder cornerSpace(boolean cornerSpace) {
        this.cornerSpace = cornerSpace;
        return this;
    }

    @Override
    protected CustomLoaderBuilder copyInternal() {
        TTMeshLoaderBuilder copy = new TTMeshLoaderBuilder();
        copy.mesh = mesh;
        copy.flipV = flipV;
        copy.cornerSpace = cornerSpace;
        return copy;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        JsonObject result = super.toJson(json);
        result.addProperty("model", Objects.requireNonNull(mesh, "mesh must be set").toString());
        if (flipV) {
            result.addProperty("flip_v", true);
        }
        if (cornerSpace) {
            result.addProperty("corner_space", true);
        }
        return result;
    }
}
