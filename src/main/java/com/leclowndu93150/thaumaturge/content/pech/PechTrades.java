package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

public final class PechTrades {
    public record Entry(int tier, ItemStack stack) {
    }

    private PechTrades() {}

    public static List<Entry> tradesFor(int pechType, HolderLookup.Provider registries) {
        String table = switch (pechType) {
            case EntityPech.TYPE_MAGE -> "mage";
            case EntityPech.TYPE_STALKER -> "stalker";
            default -> "forager";
        };
        ResourceKey<PechTradeTable> key = ResourceKey.create(PechTradeTable.REGISTRY_KEY, TCIds.rl(table));
        return registries.lookupOrThrow(PechTradeTable.REGISTRY_KEY).get(key).map(ref -> ref.value().trades().stream().map(trade -> new Entry(trade.tier(), trade.result().create())).toList())
                .orElse(List.of());
    }
}
