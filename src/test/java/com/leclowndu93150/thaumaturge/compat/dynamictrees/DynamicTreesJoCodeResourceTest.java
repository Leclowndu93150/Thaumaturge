package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

class DynamicTreesJoCodeResourceTest {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    @Test
    void everyRadiusHasSeveralTallBroadAndLoopFreeShapes() throws Exception {
        for (String species : new String[] {"greatwood", "silverwood"}) {
            int[] variants = new int[9];
            for (String line : resource("trees/thaumaturge/jo_codes/" + species + ".txt")
                    .lines()
                    .toList()) {
                String[] entry = line.split(":", 2);
                int radius = Integer.parseInt(entry[0]);
                assertTrue(radius >= 2 && radius <= 8);
                variants[radius]++;
                Set<BlockPos> branches = decode(entry[1]);
                int minHeight = species.equals("greatwood") ? 12 : 9;
                assertTrue(branches.stream().mapToInt(BlockPos::getY).max().orElseThrow() >= minHeight, line);
                assertTrue(branches.stream().anyMatch(p -> p.getX() >= 2), line);
                assertTrue(branches.stream().anyMatch(p -> p.getX() <= -2), line);
                assertTrue(branches.stream().anyMatch(p -> p.getZ() >= 2), line);
                assertTrue(branches.stream().anyMatch(p -> p.getZ() <= -2), line);
                assertTrue(branches.size() >= (species.equals("greatwood") ? 55 : 38), line);
                int edges = 0;
                for (BlockPos branch : branches) {
                    for (Direction side : Direction.values()) {
                        if (branches.contains(branch.relative(side))) edges++;
                    }
                }
                assertEquals(branches.size() - 1, edges / 2, "Branch loops: " + line);
            }
            for (int radius = 2; radius <= 8; radius++) assertEquals(3, variants[radius]);
        }
    }

    @Test
    void thickTrunksAndDenseLeavesHaveMatchingFamilyModelsAndAtlasSources() throws Exception {
        String pack = "resourcepacks/dynamictrees/assets/";
        String atlas = resource(pack + "minecraft/atlases/blocks.json");
        for (String species : new String[] {"greatwood", "silverwood"}) {
            JsonObject family = json("trees/thaumaturge/families/" + species + ".json");
            JsonObject config = json("trees/thaumaturge/species/" + species + ".json");
            assertEquals(24, family.get("max_branch_radius").getAsInt());
            assertEquals(24, config.get("max_branch_radius").getAsInt());
            assertEquals(
                    "dynamictrees:dark_oak",
                    json("trees/thaumaturge/leaves_properties/" + species + ".json")
                            .get("cell_kit")
                            .getAsString());
            for (String prefix : new String[] {"", "stripped_"}) {
                JsonObject model = json(pack + "dynamictrees/models/block/" + prefix + species + "_branch.json");
                assertEquals("dynamictrees:thick_branch", model.get("loader").getAsString());
                assertEquals("thaumaturge:" + species, model.get("family").getAsString());
                assertTrue(atlas.contains(
                        model.getAsJsonObject("textures").get("rings").getAsString()));
            }
        }
    }

    @Test
    void worldgenKeepsNativePlacementAndSelectionInsteadOfAddingPoissonSpawns() throws Exception {
        ClassLoader loader = getClass().getClassLoader();
        assertNull(loader.getResource("trees/thaumaturge/world_gen/default.json"));
        assertNull(loader.getResource("data/thaumaturge/neoforge/biome_modifier/dynamic_trees_worldgen.json"));
        for (String tree : new String[] {"greatwood", "silverwood"}) {
            assertEquals(
                    "thaumaturge:compat_tree",
                    json("data/thaumaturge/worldgen/configured_feature/" + tree + "_tree.json")
                            .get("type")
                            .getAsString());
            assertEquals(
                    "minecraft:tree",
                    json("data/thaumaturge/worldgen/configured_feature/" + tree + "_tree_grown.json")
                            .get("type")
                            .getAsString());
        }
        assertRarity("greatwood_natural", 25);
        assertRarity("greatwood_natural_rare", 125);
        assertRarity("silverwood_natural", 60);
        JsonObject selection = json("data/thaumaturge/worldgen/configured_feature/magic_forest_trees.json");
        var choices = selection.getAsJsonObject("config").getAsJsonArray("features");
        assertEquals(
                1.0F / 18.0F, choices.get(0).getAsJsonObject().get("chance").getAsFloat());
        assertEquals(
                1.0F / 12.0F, choices.get(1).getAsJsonObject().get("chance").getAsFloat());
    }

    private static void assertRarity(String feature, int rarity) throws Exception {
        var placements = json("data/thaumaturge/worldgen/placed_feature/" + feature + ".json")
                .getAsJsonArray("placement");
        assertEquals(
                "minecraft:rarity_filter",
                placements.get(0).getAsJsonObject().get("type").getAsString());
        assertEquals(rarity, placements.get(0).getAsJsonObject().get("chance").getAsInt());
    }

    private static Set<BlockPos> decode(String code) {
        Set<BlockPos> branches = new HashSet<>();
        ArrayDeque<BlockPos> forks = new ArrayDeque<>();
        BlockPos position = BlockPos.ZERO;
        branches.add(position);
        for (int i = 0; i < code.length() * 2; i++) {
            int encoded = ALPHABET.indexOf(code.charAt(i / 2));
            assertTrue(encoded >= 0);
            int instruction = i % 2 == 0 ? encoded >> 3 : encoded & 7;
            if (instruction == 6) forks.push(position);
            else if (instruction == 7) {
                if (forks.isEmpty()) {
                    assertEquals(code.length() * 2 - 1, i, "Unexpected return");
                    break;
                }
                position = forks.pop();
            } else {
                position = position.relative(Direction.from3DDataValue(instruction));
                assertTrue(branches.add(position), "Duplicate branch at " + position);
            }
        }
        assertTrue(forks.isEmpty());
        return branches;
    }

    private static JsonObject json(String path) throws Exception {
        return JsonParser.parseString(resource(path)).getAsJsonObject();
    }

    private static String resource(String path) throws Exception {
        InputStream stream =
                DynamicTreesJoCodeResourceTest.class.getClassLoader().getResourceAsStream(path);
        if (stream != null) {
            try (stream) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        Path generated = Path.of("src/generated/resources", path);
        assertTrue(Files.isRegularFile(generated), path);
        return Files.readString(generated, StandardCharsets.UTF_8);
    }
}
