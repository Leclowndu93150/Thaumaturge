package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAspects;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryItem;
import com.leclowndu93150.thaumaturge.api.warp.ItemWarp;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintedProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TTDataMaps {
    public static final DataMapType<Biome, BiomeAuraModifier> BIOME_AURA_MODIFIER = DataMapType.builder(TTIds.rl("aura_modifier"), Registries.BIOME, BiomeAuraModifier.CODEC)
            .synced(BiomeAuraModifier.CODEC, false).build();

    public static final DataMapType<Biome, BiomeAspects> BIOME_ASPECTS = DataMapType.builder(TTIds.rl("biome_aspects"), Registries.BIOME, BiomeAspects.CODEC).build();

    public static final DataMapType<EntityType<?>, TaintedProfile> TAINTED_PROFILE = DataMapType.builder(TTIds.rl("tainted_profile"), Registries.ENTITY_TYPE, TaintedProfile.CODEC).build();

    public static final DataMapType<Item, ItemWarp> ITEM_WARP = DataMapType.builder(TTIds.rl("warp"), Registries.ITEM, ItemWarp.CODEC).synced(ItemWarp.CODEC, false).build();

    private TTDataMaps() {}

    @SubscribeEvent
    public static void onRegister(RegisterDataMapTypesEvent event) {
        event.register(BIOME_AURA_MODIFIER);
        event.register(BIOME_ASPECTS);
        event.register(ITEM_WARP);
        event.register(GolemAccessoryItem.DATA_MAP);
        event.register(TAINTED_PROFILE);
        event.register(FocusTier.DATA_MAP);
    }
}
