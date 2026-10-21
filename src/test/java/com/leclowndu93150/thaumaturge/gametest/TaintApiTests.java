package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.api.taint.TaintBiomeEvent;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.NeoForge;

public final class TaintApiTests {
    private TaintApiTests() {}

    public static void register(TTTestRegistrar registrar) {
        registrar.add("api/taint/biome_policy", 20, helper -> {
            var level = helper.getLevel();
            BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
            boolean wuss = ThaumaturgeCommonConfig.WUSS_MODE.get();
            double spread = ThaumaturgeCommonConfig.TAINT_SPREAD_RATE.get();
            int frontier = ThaumaturgeCommonConfig.TAINT_FRONTIER_RATE.get();
            Consumer<TaintBiomeEvent> denial = event -> {
                if (event.level() == level && event.position().equals(pos)) {
                    event.setCanceled(true);
                }
            };
            try {
                ThaumaturgeCommonConfig.WUSS_MODE.set(false);
                ThaumaturgeCommonConfig.TAINT_SPREAD_RATE.set(100.0);
                ThaumaturgeCommonConfig.TAINT_FRONTIER_RATE.set(200);
                TaintBiomeManager.replaceColumn(level, pos, Biomes.PLAINS);
                ThaumaturgeCommonConfig.WUSS_MODE.set(true);
                helper.assertTrue(TaintApi.tryTaintBiome(level, pos, null) == TaintApi.BiomeChange.DISABLED, "Wuss mode must deny biome mutation");
                ThaumaturgeCommonConfig.WUSS_MODE.set(false);
                ThaumaturgeCommonConfig.TAINT_SPREAD_RATE.set(0.0);
                helper.assertTrue(TaintApi.tryTaintBiome(level, pos, null) == TaintApi.BiomeChange.DISABLED, "Disabled spread must deny biome mutation");
                ThaumaturgeCommonConfig.TAINT_SPREAD_RATE.set(100.0);
                NeoForge.EVENT_BUS.addListener(TaintBiomeEvent.class, denial);
                helper.assertTrue(TaintApi.tryTaintBiome(level, pos, null) == TaintApi.BiomeChange.DENIED, "Protection event must veto biome mutation");
                NeoForge.EVENT_BUS.unregister(denial);
                helper.assertTrue(!TaintBiomeManager.isTainted(level, pos), "Denied mutation must leave the biome intact");
                TaintBlooms.register(level, pos);
                helper.assertTrue(TaintApi.tryTaintBiome(level, pos, null) == TaintApi.BiomeChange.DENIED, "Blooms must protect the column");
                TaintBlooms.unregister(level, pos);
                var neighbor = level.getBiome(pos.offset(4, 0, 0));
                helper.assertTrue(TaintApi.tryTaintBiome(level, pos, null) == TaintApi.BiomeChange.CHANGED, "Allowed mutation must change the column");
                helper.assertTrue(TaintBiomeManager.isTainted(level, pos), "The changed biome must be tainted");
                helper.assertTrue(level.getBiome(pos.offset(4, 0, 0)).equals(neighbor), "Neighboring quart columns must remain unchanged");
                helper.assertTrue(TaintApi.tryTaintBiome(level, pos, null) == TaintApi.BiomeChange.UNCHANGED, "Repeated mutation must not report another change");
                BlockPos unloaded = new BlockPos(25000000, pos.getY(), 25000000);
                helper.assertTrue(TaintApi.tryTaintBiome(level, unloaded, null) == TaintApi.BiomeChange.UNLOADED, "Unloaded columns must not be loaded");
            } finally {
                NeoForge.EVENT_BUS.unregister(denial);
                TaintBlooms.unregister(level, pos);
                TaintBiomeManager.restoreColumn(level, pos);
                ThaumaturgeCommonConfig.WUSS_MODE.set(wuss);
                ThaumaturgeCommonConfig.TAINT_SPREAD_RATE.set(spread);
                ThaumaturgeCommonConfig.TAINT_FRONTIER_RATE.set(frontier);
            }
            helper.succeed();
        });
    }
}
