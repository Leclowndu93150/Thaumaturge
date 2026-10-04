package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownFoliagePlacer;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownTrunkPlacer;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.SpiderNestDecorator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCTreePlacers {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACERS = DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, TCIds.MODID);
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS = DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, TCIds.MODID);
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS = DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, TCIds.MODID);

    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<CrownTrunkPlacer>> CROWN_TRUNK = TRUNK_PLACERS.register("crown_trunk_placer",
            () -> new TrunkPlacerType<>(CrownTrunkPlacer.CODEC));
    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<CrownFoliagePlacer>> CROWN_FOLIAGE = FOLIAGE_PLACERS.register("crown_foliage_placer",
            () -> new FoliagePlacerType<>(CrownFoliagePlacer.CODEC));
    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<SpiderNestDecorator>> SPIDER_NEST = TREE_DECORATORS.register("spider_nest",
            () -> new TreeDecoratorType<>(SpiderNestDecorator.CODEC));

    private TCTreePlacers() {}

    public static void register(IEventBus bus) {
        TRUNK_PLACERS.register(bus);
        FOLIAGE_PLACERS.register(bus);
        TREE_DECORATORS.register(bus);
    }
}
