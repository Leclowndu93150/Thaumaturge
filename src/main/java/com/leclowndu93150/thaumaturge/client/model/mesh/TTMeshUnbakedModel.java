package com.leclowndu93150.thaumaturge.client.model.mesh;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public final class TTMeshUnbakedModel extends AbstractUnbakedModel {
    private final TTMeshGeometry geometry;

    public TTMeshUnbakedModel(StandardModelParameters parameters, Identifier model, boolean flipV, boolean cornerSpace) {
        super(parameters);
        this.geometry = new TTMeshGeometry(model, flipV, cornerSpace);
    }

    @Override
    public UnbakedGeometry geometry() {
        return geometry;
    }

    public static final class Loader implements UnbakedModelLoader<TTMeshUnbakedModel> {
        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public TTMeshUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
            StandardModelParameters parameters = StandardModelParameters.parse(jsonObject, context);
            Identifier model = Identifier.parse(GsonHelper.getAsString(jsonObject, "model"));
            boolean flipV = GsonHelper.getAsBoolean(jsonObject, "flip_v", false);
            boolean cornerSpace = GsonHelper.getAsBoolean(jsonObject, "corner_space", false);
            return new TTMeshUnbakedModel(parameters, model, flipV, cornerSpace);
        }
    }
}
