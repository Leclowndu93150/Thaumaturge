package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeFeature;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeFeatureConfig;
import com.leclowndu93150.thaumaturge.content.manabean.ManaPodFeature;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalClusterConfig;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalClusterFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.CrimsonPortalFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.HilltopStonesFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.MagicalCaveBushFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.MagicalCavePondFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.ObsidianTotemFeature;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicForestFloraConfig;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicForestFloraFeature;
import com.leclowndu93150.thaumaturge.content.world.taint.TaintBiomeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, TTIds.MODID);

    public static final DeferredHolder<Feature<?>, CrystalClusterFeature> CRYSTAL_CLUSTER =
            FEATURES.register("crystal_cluster", () -> new CrystalClusterFeature(CrystalClusterConfig.CODEC));

    public static final DeferredHolder<Feature<?>, MagicForestFloraFeature> MAGIC_FOREST_FLORA =
            FEATURES.register("magic_forest_flora", () -> new MagicForestFloraFeature(MagicForestFloraConfig.CODEC));

    public static final DeferredHolder<Feature<?>, ManaPodFeature> MANA_PODS =
            FEATURES.register("mana_pods", () -> new ManaPodFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NodeFeatureConfig>> NODE =
            FEATURES.register("node", () -> new NodeFeature(NodeFeatureConfig.CODEC));

    public static final DeferredHolder<Feature<?>, CrimsonPortalFeature> CRIMSON_PORTAL =
            FEATURES.register("crimson_portal", () -> new CrimsonPortalFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, ObsidianTotemFeature> OBSIDIAN_TOTEM =
            FEATURES.register("obsidian_totem", () -> new ObsidianTotemFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, HilltopStonesFeature> HILLTOP_STONES =
            FEATURES.register("hilltop_stones", () -> new HilltopStonesFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, MagicalCavePondFeature> MAGICAL_CAVE_POND =
            FEATURES.register("magical_cave_pond", () -> new MagicalCavePondFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, MagicalCaveBushFeature> MAGICAL_CAVE_BUSH =
            FEATURES.register("magical_cave_bush", () -> new MagicalCaveBushFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, TaintBiomeFeature> TAINT_BIOME =
            FEATURES.register("taint_biome", () -> new TaintBiomeFeature(NoneFeatureConfiguration.CODEC));

    private TTFeatures() {}

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
