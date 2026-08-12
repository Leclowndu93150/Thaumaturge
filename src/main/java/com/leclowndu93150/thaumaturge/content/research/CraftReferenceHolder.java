package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class CraftReferenceHolder {
    private static volatile Set<Item> items = Set.of();

    private CraftReferenceHolder() {}

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) return;
        rebuild(event.getPlayerList().getServer().registryAccess());
    }

    public static void rebuild(RegistryAccess access) {
        Set<Item> found = new HashSet<>();
        access.lookup(IResearchEntry.REGISTRY_KEY)
                .ifPresent(lookup -> lookup.listElements().forEach(entry -> {
                    for (IResearchStage stage : entry.value().stages()) {
                        for (ResearchRequirement req : stage.craft()) {
                            for (Holder<Item> holder : req.items()) {
                                found.add(holder.value());
                            }
                        }
                    }
                }));
        items = Set.copyOf(found);
    }

    public static boolean isReference(RegistryAccess access, Item item) {
        Set<Item> current = items;
        if (current.isEmpty()) {
            rebuild(access);
            current = items;
        }
        return current.contains(item);
    }
}
