package com.leclowndu93150.thaumaturge.content.taint.item;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class EssentiaCrystalFactory {
    private EssentiaCrystalFactory() {}

    public static ItemStack of(Holder<IAspect> aspect, int count) {
        ItemStack stack = new ItemStack(TCItems.ESSENTIA_CRYSTAL.get(), count);
        stack.set(TCDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1));
        return stack;
    }

    public static ItemStack of(Holder<IAspect> aspect) {
        return of(aspect, 1);
    }

    public static ItemStack of(HolderLookup.Provider registries, ResourceKey<IAspect> key) {
        Holder<IAspect> aspect = registries.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
        return of(aspect, 1);
    }

    public static List<ItemStack> discoveredCrystals(Player player) {
        List<ItemStack> crystals = new ArrayList<>();
        player.level().registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().filter(aspect -> AspectPools.isDiscovered(player, aspect))
                .sorted(Comparator.comparing(aspect -> !aspect.value().isPrimal())).forEach(aspect -> crystals.add(of(aspect)));
        return crystals;
    }
}
