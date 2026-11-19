package com.leclowndu93150.thaumaturge.config;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.junit.jupiter.api.Test;

class BiomeGenerationConditionTest {
    private boolean included(String path) throws Exception {
        var file = Path.of(
                System.getProperty("thaumaturge.testResourceRoot"), "data/thaumaturge/lithostitched", path + ".json");
        return ICondition.conditionsMatched(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)));
    }

    @Test
    void defaultsAndIndependentBiomeControlsKeepTheSharedRegionAvailable() throws Exception {
        var forest = ThaumaturgeCommonConfig.GENERATE_MAGICAL_FOREST;
        var caves = ThaumaturgeCommonConfig.GENERATE_MAGICAL_FOREST_CAVES;
        var taint = ThaumaturgeCommonConfig.GENERATE_TAINTED_LANDS;
        boolean oldForest = forest.get(), oldCaves = caves.get(), oldTaint = taint.get();
        try {
            assertEquals(true, forest.getDefault());
            assertEquals(true, caves.getDefault());
            assertEquals(true, taint.getDefault());
            forest.set(true);
            caves.set(true);
            taint.set(true);
            assertTrue(included("biome_injector/magical_forest"));
            assertTrue(included("biome_injector/magical_forest_caves"));
            assertTrue(included("region/magical_forest"));
            assertTrue(included("biome_injector/tainted_lands"));
            assertTrue(included("region/tainted_lands"));
            forest.set(false);
            taint.set(false);
            assertFalse(included("biome_injector/magical_forest"));
            assertTrue(included("biome_injector/magical_forest_caves"));
            assertTrue(included("region/magical_forest"));
            assertFalse(included("biome_injector/tainted_lands"));
            assertFalse(included("region/tainted_lands"));
            forest.set(true);
            caves.set(false);
            assertTrue(included("biome_injector/magical_forest"));
            assertFalse(included("biome_injector/magical_forest_caves"));
            assertTrue(included("region/magical_forest"));
            forest.set(false);
            assertFalse(included("region/magical_forest"));
        } finally {
            forest.set(oldForest);
            caves.set(oldCaves);
            taint.set(oldTaint);
        }
    }
}
