package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerboundRecipeDisplayHandler {
    private ServerboundRecipeDisplayHandler() {}

    public static void handle(ServerboundRequestRecipeDisplayPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;
            if (!(player.level() instanceof ServerLevel level))
                return;
            RecipeManager manager = level.recipeAccess();
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, payload.recipeId());
            manager.byKey(key).ifPresent(holder -> {
                List<RecipeDisplay> displays = holder.value().display();
                PacketDistributor.sendToPlayer(player, new ClientboundRecipeDisplayPayload(payload.recipeId(), displays));
            });
        });
    }

    public static void handleItemRequest(ServerboundRequestItemRecipePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player))
                return;
            if (!(player.level() instanceof ServerLevel level))
                return;
            ContextMap displayContext = SlotDisplayContext.fromLevel(level);
            RecipeHolder<?> best = null;
            for (RecipeHolder<?> holder : level.recipeAccess().getRecipes()) {
                List<RecipeDisplay> displays = holder.value().display();
                if (displays.isEmpty())
                    continue;
                ItemStack result = displays.get(0).result().resolveForFirstStack(displayContext);
                Identifier resultId = result.getItem().builtInRegistryHolder().unwrapKey().map(ResourceKey::identifier).orElse(null);
                if (!payload.itemId().equals(resultId))
                    continue;
                boolean thaumaturge = holder.id().identifier().getNamespace().equals(TCIds.MODID);
                if (best == null || (thaumaturge && !best.id().identifier().getNamespace().equals(TCIds.MODID))) {
                    best = holder;
                }
                if (thaumaturge)
                    break;
            }
            if (best != null) {
                PacketDistributor.sendToPlayer(player, new ClientboundItemRecipePayload(best.id().identifier(), best.value().display()));
            }
        });
    }
}
