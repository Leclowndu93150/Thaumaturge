package com.leclowndu93150.thaumaturge.config;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public record BiomeGenerationCondition(String biome) implements ICondition {
    public static final MapCodec<BiomeGenerationCondition> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Codec.STRING.fieldOf("biome").forGetter(BiomeGenerationCondition::biome))
                    .apply(instance, BiomeGenerationCondition::new));
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, TTIds.MODID);

    static {
        CONDITIONS.register("biome_generation", () -> CODEC);
    }

    public static void register(IEventBus bus) {
        CONDITIONS.register(bus);
    }

    @Override
    public boolean test(IContext context) {
        return switch (biome) {
            case "magical_forest" -> ThaumaturgeCommonConfig.GENERATE_MAGICAL_FOREST.get();
            case "magical_forest_caves" -> ThaumaturgeCommonConfig.GENERATE_MAGICAL_FOREST_CAVES.get();
            case "magical_forest_region" ->
                ThaumaturgeCommonConfig.GENERATE_MAGICAL_FOREST.get()
                        || ThaumaturgeCommonConfig.GENERATE_MAGICAL_FOREST_CAVES.get();
            case "tainted_lands" -> ThaumaturgeCommonConfig.GENERATE_TAINTED_LANDS.get();
            default -> false;
        };
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
