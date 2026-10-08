package com.leclowndu93150.thaumaturge.client.model.connected;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public final class ConnectedModelLoader<T extends IUnbakedGeometry<T>> implements IGeometryLoader<T> {
    private final MapCodec<T> codec;

    public ConnectedModelLoader(MapCodec<T> codec) {
        this.codec = codec;
    }

    @Override
    public T read(JsonObject json, JsonDeserializationContext context) {
        return codec.codec().parse(JsonOps.INSTANCE, json).getOrThrow();
    }
}
