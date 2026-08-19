package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAspects;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumaturge.api.warp.ItemWarp;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCDataMaps {
    public static final DataMapType<Biome, BiomeAuraModifier> BIOME_AURA_MODIFIER = DataMapType.builder(TCIds.rl("aura_modifier"), Registries.BIOME, BiomeAuraModifier.CODEC)
            .synced(BiomeAuraModifier.CODEC, false).build();

    public static final DataMapType<Biome, BiomeAspects> BIOME_ASPECTS = DataMapType.builder(TCIds.rl("biome_aspects"), Registries.BIOME, BiomeAspects.CODEC).build();

    public static final DataMapType<Item, ItemWarp> ITEM_WARP = DataMapType.builder(TCIds.rl("warp"), Registries.ITEM, ItemWarp.CODEC).synced(ItemWarp.CODEC, false).build();

    private TCDataMaps() {}

    @SubscribeEvent
    public static void onRegister(RegisterDataMapTypesEvent event) {
        event.register(BIOME_AURA_MODIFIER);
        event.register(BIOME_ASPECTS);
        event.register(ITEM_WARP);
    }
}
