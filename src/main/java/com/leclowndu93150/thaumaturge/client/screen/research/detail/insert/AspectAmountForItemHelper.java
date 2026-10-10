package com.leclowndu93150.thaumaturge.client.screen.research.detail.insert;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.Optional;

/**
 * Client-side helper that finds which of the player's scanned items
 * contain a given aspect, and how much of it.
 *
 * <p>Scanned items are retrieved from the {@link IPlayerKnowledge} data attachment: every entry
 * whose path starts with {@value #PREFIX} is used as a  scan record for the item.
 * The aspects of each scanned item are then looked up through {@link AspectIndexAccess}.
 *
 */
public class AspectAmountForItemHelper {
    private static final String PREFIX = "scanned/item/";

    public static Map<Item, Integer> aspectAmountByItem(Holder<IAspect> aspect) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !minecraft.isSameThread()) {
            return Map.of();
        }

        Registry<Item> itemRegistry = player.level().registryAccess().lookupOrThrow(Registries.ITEM);
        Object2IntMap<Item> result = new Object2IntOpenHashMap<>();

        for (Identifier researchId : KnowledgeAccess.of(player).researchList()) {
            if (!researchId.getNamespace().equals(TTIds.MODID))
                continue;

            String path = researchId.getPath();
            if (!path.startsWith(PREFIX))
                continue;

            Identifier itemId = Identifier.bySeparator(path.substring(PREFIX.length()), '/');

            Optional<Item> item = itemRegistry.getOptional(itemId);
            if (item.isEmpty())
                continue;

            int amount = AspectIndexAccess.of(item.get()).amountOf(aspect);
            if (amount > 0) {
                result.put(item.get(), amount);
            }
        }
        return result;
    }
}
