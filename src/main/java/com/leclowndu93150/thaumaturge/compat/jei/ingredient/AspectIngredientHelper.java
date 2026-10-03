package com.leclowndu93150.thaumaturge.compat.jei.ingredient;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectKnowledgeAccess;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class AspectIngredientHelper implements IIngredientHelper<AspectInstance> {
    public static final AspectIngredientHelper INSTANCE = new AspectIngredientHelper();

    private static final ResourceLocation UNKNOWN = ResourceLocation.fromNamespaceAndPath("thaumaturge", "unknown");

    private AspectIngredientHelper() {}

    @Override
    public IIngredientType<AspectInstance> getIngredientType() {
        return AspectIngredientType.INSTANCE;
    }

    @Override
    public String getDisplayName(AspectInstance ingredient) {
        if (!AspectKnowledgeAccess.isKnown(ingredient.aspect())) {
            return Component.translatable("tc.aspect.unknown").getString();
        }
        return AspectComponents.name(ingredient.aspect()).getString();
    }

    @Override
    public Object getUid(AspectInstance ingredient, UidContext context) {
        return aspectUid(ingredient);
    }

    // JEI still asks for the old method in latest version...
    @SuppressWarnings("removal")
    @Override
    public String getUniqueId(AspectInstance ingredient, UidContext context) {
        return aspectUid(ingredient);
    }

    private static String aspectUid(AspectInstance ingredient) {
        return ingredient
                .aspect()
                .unwrapKey()
                .map(ResourceKey::location)
                .orElse(UNKNOWN)
                .toString();
    }

    @Override
    public ResourceLocation getResourceLocation(AspectInstance ingredient) {
        return ingredient.aspect().unwrapKey().map(ResourceKey::location).orElse(UNKNOWN);
    }

    @Override
    public AspectInstance copyIngredient(AspectInstance ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(AspectInstance ingredient) {
        if (ingredient == null) {
            return "null";
        }
        return ingredient
                .aspect()
                .unwrapKey()
                .map(key -> key.location().toString())
                .orElse("unbound aspect holder");
    }

    @Override
    public boolean isValidIngredient(AspectInstance ingredient) {
        return ingredient != null && ingredient.aspect().isBound();
    }

    @Override
    public ItemStack getCheatItemStack(AspectInstance ingredient) {
        return PhialItem.makeFilled(ingredient.aspect());
    }
}
