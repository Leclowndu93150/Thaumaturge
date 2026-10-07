package com.leclowndu93150.thaumaturge.client.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.client.model.entity.BrainModel;
import com.leclowndu93150.thaumaturge.client.model.entity.TTBannerModel;
import com.leclowndu93150.thaumaturge.client.model.gear.FortressArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.KnightArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.PraetorArmorModel;
import com.leclowndu93150.thaumaturge.client.model.gear.RobeArmorModel;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshLoader;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalShards;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

class RebrandResourceTest {
    @Test
    void everyCustomArmorLayerSupportsTheNativeHumanoidConstructor() {
        for (var layer : List.of(
                FortressArmorModel.createHead(), FortressArmorModel.createChest(), FortressArmorModel.createLegs())) {
            assertDoesNotThrow(() -> new FortressArmorModel(layer.bakeRoot()));
        }
        for (var layer :
                List.of(KnightArmorModel.createHead(), KnightArmorModel.createChest(), KnightArmorModel.createLegs())) {
            assertDoesNotThrow(() -> new KnightArmorModel(layer.bakeRoot()));
        }
        for (var layer : List.of(
                PraetorArmorModel.createHead(), PraetorArmorModel.createChest(), PraetorArmorModel.createLegs())) {
            assertDoesNotThrow(() -> new PraetorArmorModel(layer.bakeRoot()));
        }
        for (var layer :
                List.of(RobeArmorModel.createHead(), RobeArmorModel.createChest(), RobeArmorModel.createLegs())) {
            assertDoesNotThrow(() -> new RobeArmorModel(layer.bakeRoot()));
        }
    }

    @Test
    void everyAuthoredMeshLoadsThroughTheNativeParser() throws Exception {
        Path assets = Path.of(System.getProperty("thaumaturge.testResourceRoot"), "assets");
        ResourceManager resources = mock(ResourceManager.class);
        when(resources.openAsReader(any(ResourceLocation.class))).thenAnswer(call -> {
            ResourceLocation id = call.getArgument(0);
            return Files.newBufferedReader(assets.resolve(id.getNamespace()).resolve(id.getPath()));
        });
        try (var files = Files.walk(assets.resolve("thaumaturge/models/mesh"))) {
            List<Path> meshes =
                    files.filter(path -> path.toString().endsWith(".ttmesh")).toList();
            assertFalse(meshes.isEmpty());
            for (Path path : meshes) {
                String relative = assets.resolve("thaumaturge")
                        .relativize(path)
                        .toString()
                        .replace('\\', '/');
                var mesh = TTMeshLoader.load(resources, ResourceLocation.fromNamespaceAndPath("thaumaturge", relative));
                assertFalse(mesh.parts().isEmpty(), relative);
                for (var part : mesh.parts()) {
                    assertEquals(part.quadCount() * 12, part.positions().length, relative);
                    assertEquals(part.positions().length, part.normals().length, relative);
                    for (float value : part.positions()) assertTrue(Float.isFinite(value), relative);
                    for (float value : part.normals()) assertTrue(Float.isFinite(value), relative);
                }
            }
        }
    }

    @Test
    void crystalShardSelectionIsRepeatableForEveryFace() {
        for (long seed : new long[] {0, 1, -1, Long.MIN_VALUE, Long.MAX_VALUE, 123456789}) {
            for (Direction face : Direction.values()) {
                List<Integer> order = CrystalShards.order(face, seed);
                assertEquals(CrystalShards.COUNT, new HashSet<>(order).size());
                assertEquals(order, CrystalShards.order(face, seed));
                assertTrue(order.contains(CrystalShards.unsupported(seed)));
            }
        }
    }

    @Test
    void brainAnimationDoesNotLeakAnXpPoseIntoTheNextJar() {
        BrainModel reused = new BrainModel(BrainModel.createLayer().bakeRoot());
        reused.setupAnim(47, 1);
        reused.setupAnim(13, 0);
        BrainModel fresh = new BrainModel(BrainModel.createLayer().bakeRoot());
        fresh.setupAnim(13, 0);
        assertPoseEquals(fresh.root, reused.root);
    }

    @Test
    void bannerAnimationDoesNotAccumulateItsWallOffset() {
        TTBannerModel reused = new TTBannerModel(TTBannerModel.createLayer().bakeRoot(), false);
        for (int i = 0; i < 20; i++) reused.setupAnim(true, i);
        reused.setupAnim(false, 4);
        TTBannerModel fresh = new TTBannerModel(TTBannerModel.createLayer().bakeRoot(), false);
        fresh.setupAnim(false, 4);
        assertPoseEquals(fresh.root, reused.root);
        assertTrue(reused.root.getChild("root").getChild("standing_support").visible);
        assertFalse(reused.root.getChild("root").getChild("wall_mount").visible);
    }

    private static void assertPoseEquals(ModelPart expected, ModelPart actual) {
        List<ModelPart> left = expected.getAllParts().toList();
        List<ModelPart> right = actual.getAllParts().toList();
        assertEquals(left.size(), right.size());
        for (int i = 0; i < left.size(); i++) {
            assertEquals(left.get(i).x, right.get(i).x);
            assertEquals(left.get(i).y, right.get(i).y);
            assertEquals(left.get(i).z, right.get(i).z);
            assertEquals(left.get(i).xRot, right.get(i).xRot);
            assertEquals(left.get(i).yRot, right.get(i).yRot);
            assertEquals(left.get(i).zRot, right.get(i).zRot);
            assertEquals(left.get(i).xScale, right.get(i).xScale);
            assertEquals(left.get(i).yScale, right.get(i).yScale);
            assertEquals(left.get(i).zScale, right.get(i).zScale);
        }
    }
}
