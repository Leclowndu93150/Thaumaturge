package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeFeature;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeFeatureConfig;
import com.leclowndu93150.thaumaturge.content.manabean.ManaPodFeature;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalClusterConfig;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalClusterFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.CrimsonPortalFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.HilltopStonesFeature;
import com.leclowndu93150.thaumaturge.content.world.objects.ObsidianTotemFeature;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicForestFloraConfig;
import com.leclowndu93150.thaumaturge.content.world.plant.MagicForestFloraFeature;
import com.leclowndu93150.thaumaturge.content.world.tree.BigMagicTreeConfig;
import com.leclowndu93150.thaumaturge.content.world.tree.BigMagicTreeFeature;
import com.leclowndu93150.thaumaturge.content.world.tree.BigTreeConfig;
import com.leclowndu93150.thaumaturge.content.world.tree.BigTreeFeature;
import com.leclowndu93150.thaumaturge.content.world.tree.SilverwoodTreeConfig;
import com.leclowndu93150.thaumaturge.content.world.tree.SilverwoodTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, TCIds.MODID);

    public static final DeferredHolder<Feature<?>, BigTreeFeature> BIG_TREE = FEATURES.register("big_tree", () -> new BigTreeFeature(BigTreeConfig.CODEC));

    public static final DeferredHolder<Feature<?>, BigMagicTreeFeature> BIG_MAGIC_TREE = FEATURES.register("big_magic_tree", () -> new BigMagicTreeFeature(BigMagicTreeConfig.CODEC));

    public static final DeferredHolder<Feature<?>, SilverwoodTreeFeature> SILVERWOOD_TREE = FEATURES.register("silverwood_tree", () -> new SilverwoodTreeFeature(SilverwoodTreeConfig.CODEC));

    public static final DeferredHolder<Feature<?>, CrystalClusterFeature> CRYSTAL_CLUSTER = FEATURES.register("crystal_cluster", () -> new CrystalClusterFeature(CrystalClusterConfig.CODEC));

    public static final DeferredHolder<Feature<?>, MagicForestFloraFeature> MAGIC_FOREST_FLORA = FEATURES.register("magic_forest_flora",
            () -> new MagicForestFloraFeature(MagicForestFloraConfig.CODEC));

    public static final DeferredHolder<Feature<?>, ManaPodFeature> MANA_PODS = FEATURES.register("mana_pods", () -> new ManaPodFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<NodeFeatureConfig>> NODE = FEATURES.register("node", () -> new NodeFeature(NodeFeatureConfig.CODEC));

    public static final DeferredHolder<Feature<?>, ObsidianTotemFeature> OBSIDIAN_TOTEM = FEATURES.register("obsidian_totem", () -> new ObsidianTotemFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, CrimsonPortalFeature> CRIMSON_PORTAL = FEATURES.register("crimson_portal", () -> new CrimsonPortalFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, HilltopStonesFeature> HILLTOP_STONES = FEATURES.register("hilltop_stones", () -> new HilltopStonesFeature(NoneFeatureConfiguration.CODEC));

    private TCFeatures() {}

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
