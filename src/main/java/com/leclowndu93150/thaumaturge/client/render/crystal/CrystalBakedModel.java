package com.leclowndu93150.thaumaturge.client.render.crystal;

import com.leclowndu93150.thaumaturge.client.model.mesh.TCMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshPart;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
    private static final List<Integer> PART_INDICES = List.of(0, 1, 2, 3, 4, 5, 6, 7);
    private static final Direction[] FACES = Direction.values();

    private final TCMesh mesh;
    private final Material.Baked particleMaterial;

    public CrystalBakedModel(TCMesh mesh, Material.Baked particleMaterial) {
        this.mesh = mesh;
        this.particleMaterial = particleMaterial;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        int growth = state.hasProperty(BlockCrystal.SIZE) ? state.getValue(BlockCrystal.SIZE) : 0;
        int partsPerFace = growth + 1;
        long seed = random.nextLong();
        QuadCollection.Builder builder = new QuadCollection.Builder();
        boolean any = false;
        for (Direction face : FACES) {
            BlockPos neighbour = pos.relative(face);
            BlockState neighbourState = level.getBlockState(neighbour);
            if (!neighbourState.isFaceSturdy(level, neighbour, face.getOpposite())) {
                continue;
            }
            Matrix4f transform = CrystalFaceTransforms.forFace(face);
            List<Integer> shuffled = new ArrayList<>(PART_INDICES);
            Collections.shuffle(shuffled, new Random(seed + CrystalFaceTransforms.seedOffset(face)));
            for (int i = 0; i < partsPerFace; i++) {
                TCMeshPart part = mesh.parts().get(shuffled.get(i));
                List<BakedQuad> quads = new ArrayList<>();
                CrystalQuadBaker.bakePart(part, particleMaterial, 0, transform, quads);
                for (BakedQuad q : quads) {
                    builder.addUnculledFace(q);
                    any = true;
                }
            }
        }
        if (!any) {
            int unsupportedSeed = (int) (seed & 0x7);
            Matrix4f transform = CrystalFaceTransforms.forFace(Direction.DOWN);
            TCMeshPart part = mesh.parts().get(unsupportedSeed % 8);
            List<BakedQuad> quads = new ArrayList<>();
            CrystalQuadBaker.bakePart(part, particleMaterial, 0, transform, quads);
            for (BakedQuad q : quads) {
                builder.addUnculledFace(q);
            }
        }
        parts.add(new SimpleModelWrapper(builder.build(), true, particleMaterial));
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
