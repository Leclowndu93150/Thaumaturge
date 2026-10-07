package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.world.objects.ConfigRarityFilter;
import com.leclowndu93150.thaumaturge.content.world.objects.ConfigNodeSpawnFilter;
import com.leclowndu93150.thaumaturge.content.world.objects.MagicalCaveFloorPlacement;
import com.leclowndu93150.thaumaturge.content.world.objects.SurfaceBiomeSearch;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTPlacementModifiers {
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, TTIds.MODID);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ConfigRarityFilter>> CRIMSON_PORTAL_RARITY = PLACEMENT_MODIFIERS.register("crimson_portal_rarity",
            () -> () -> ConfigRarityFilter.CODEC);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ConfigNodeSpawnFilter>> NODE_SPAWN_CHANCE = PLACEMENT_MODIFIERS.register("node_spawn_chance",
            () -> () -> ConfigNodeSpawnFilter.CODEC);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<MagicalCaveFloorPlacement>> MAGICAL_CAVE_FLOOR = PLACEMENT_MODIFIERS.register("magical_cave_floor",
            () -> () -> MagicalCaveFloorPlacement.CODEC);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<SurfaceBiomeSearch>> SURFACE_BIOME_SEARCH = PLACEMENT_MODIFIERS.register("surface_biome_search",
            () -> () -> SurfaceBiomeSearch.CODEC);

    private TTPlacementModifiers() {}

    public static void register(IEventBus bus) {
        PLACEMENT_MODIFIERS.register(bus);
    }
}
