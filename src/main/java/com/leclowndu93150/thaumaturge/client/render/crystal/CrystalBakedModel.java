package com.leclowndu93150.thaumaturge.client.render.crystal;

import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalFaceTransforms;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalShards;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import org.joml.Matrix4f;

public final class CrystalBakedModel implements DynamicBlockStateModel {
    private static final Direction[] FACES = Direction.values();

    private final TTMesh mesh;
    private final Material.Baked particleMaterial;

    public CrystalBakedModel(TTMesh mesh, Material.Baked particleMaterial) {
        this.mesh = mesh;
        this.particleMaterial = particleMaterial;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        int growth = state.hasProperty(BlockCrystal.SIZE) ? state.getValue(BlockCrystal.SIZE) : 0;
        int partsPerFace = growth + 1;
        long seed = CrystalShards.seed(state, pos);
        QuadCollection.Builder builder = new QuadCollection.Builder();
        boolean any = false;
        for (Direction face : FACES) {
            if (!CrystalShards.supports(level, pos, face)) {
                continue;
            }
            Matrix4f transform = CrystalFaceTransforms.forFace(face);
            List<Integer> order = CrystalShards.order(face, seed);
            for (int i = 0; i < partsPerFace; i++) {
                addPart(mesh.parts().get(order.get(i)), transform, builder);
                any = true;
            }
        }
        if (!any) {
            addPart(mesh.parts().get(CrystalShards.unsupported(seed)), CrystalFaceTransforms.forFace(Direction.DOWN), builder);
        }
        parts.add(new SimpleModelWrapper(builder.build(), true, particleMaterial));
    }

    private void addPart(TTMeshPart part, Matrix4f transform, QuadCollection.Builder builder) {
        List<BakedQuad> quads = new ArrayList<>();
        CrystalQuadBaker.bakePart(part, particleMaterial, 0, transform, quads);
        for (BakedQuad quad : quads) {
            builder.addUnculledFace(quad);
        }
    }

    @Override
    public Material.Baked particleMaterial() {
        return particleMaterial;
    }

    @Override
    public int materialFlags() {
        return 0;
    }
}
