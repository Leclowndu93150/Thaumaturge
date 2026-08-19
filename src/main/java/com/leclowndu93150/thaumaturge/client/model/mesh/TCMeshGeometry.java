package com.leclowndu93150.thaumaturge.client.model.mesh;

import com.mojang.math.Transformation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.neoforged.neoforge.client.model.ExtendedUnbakedGeometry;
import net.neoforged.neoforge.client.model.NeoForgeModelProperties;
import net.neoforged.neoforge.client.model.UnbakedElementsHelper;
import org.joml.Matrix4f;

public final class TCMeshGeometry implements ExtendedUnbakedGeometry {
    private static final float CENTER_OFFSET = 0.5F;
    private static final int NO_TINT = -1;

    private final Identifier model;
    private final boolean flipV;
    private final boolean cornerSpace;

    public TCMeshGeometry(Identifier model, boolean flipV, boolean cornerSpace) {
        this.model = model;
        this.flipV = flipV;
        this.cornerSpace = cornerSpace;
    }

    @Override
    public QuadCollection bake(TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name, ContextMap additionalProperties) {
        Transformation rootTransform = additionalProperties.getOrDefault(NeoForgeModelProperties.TRANSFORM, Transformation.IDENTITY);
        if (!rootTransform.isIdentity()) {
            modelState = UnbakedElementsHelper.composeRootTransformIntoModelState(modelState, rootTransform);
        }
        TCMesh mesh = loadMesh();
        Matrix4f transform = new Matrix4f().translate(CENTER_OFFSET, CENTER_OFFSET, CENTER_OFFSET).mul(modelState.transformation().getMatrix());
        if (cornerSpace) {
            transform.translate(-CENTER_OFFSET, -CENTER_OFFSET, -CENTER_OFFSET);
        }
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (TCMeshPart part : mesh.parts()) {
            String slot = part.materialSlot();
            Material material = textureSlots.getMaterial(slot);
            if (material == null) {
                throw new IllegalStateException("Missing texture slot '" + slot + "' for mesh model " + model + " in " + name.debugName());
            }
            Material.Baked baked = modelBaker.materials().get(material, name);
            boolean itemsAtlas = baked.sprite().atlasLocation().equals(TextureAtlas.LOCATION_ITEMS);
            boolean translucent = baked.forceTranslucent();
            RenderType sheet;
            if (itemsAtlas) {
                sheet = translucent ? Sheets.translucentItemSheet() : Sheets.cutoutItemSheet();
            } else {
                sheet = translucent ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet();
            }
            BakedQuad.MaterialInfo info = new BakedQuad.MaterialInfo(baked.sprite(), translucent ? ChunkSectionLayer.TRANSLUCENT : ChunkSectionLayer.CUTOUT, sheet, NO_TINT, false, 0);
            List<BakedQuad> quads = new ArrayList<>();
            TCMeshQuadBaker.bakePart(part, baked, NO_TINT, transform, info, flipV, quads);
            for (BakedQuad quad : quads) {
                builder.addUnculledFace(quad);
            }
        }
        return builder.build();
    }

    private TCMesh loadMesh() {
        try {
            return TCMeshLoader.load(Minecraft.getInstance().getResourceManager(), model);
        } catch (IOException e) {
            throw new RuntimeException("Could not load mesh model at " + model, e);
        }
    }
}
