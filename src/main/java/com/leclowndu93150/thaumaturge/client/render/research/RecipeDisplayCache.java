package com.leclowndu93150.thaumaturge.client.render.research;

import com.leclowndu93150.thaumaturge.network.ServerboundRequestRecipeDisplayPayload;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

public final class RecipeDisplayCache {
    private static final Map<Identifier, List<RecipeDisplay>> CACHE = new HashMap<>();
    private static final Set<Identifier> REQUESTED = new HashSet<>();

    private RecipeDisplayCache() {}

    public static @Nullable List<RecipeDisplay> get(Identifier id) {
        return CACHE.get(id);
    }

    public static void ensureRequested(Identifier id) {
        if (CACHE.containsKey(id) || REQUESTED.contains(id))
            return;
        REQUESTED.add(id);
        ClientPacketDistributor.sendToServer(new ServerboundRequestRecipeDisplayPayload(id));
    }

    public static void put(Identifier id, List<RecipeDisplay> displays) {
        CACHE.put(id, displays);
        REQUESTED.remove(id);
    }

    public static void clear() {
        CACHE.clear();
        REQUESTED.clear();
    }
}
