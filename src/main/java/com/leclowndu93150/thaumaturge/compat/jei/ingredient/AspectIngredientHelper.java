package com.leclowndu93150.thaumaturge.compat.jei.ingredient;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.content.item.PhialItem;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

public final class AspectIngredientHelper implements IIngredientHelper<AspectInstance> {
    public static final AspectIngredientHelper INSTANCE = new AspectIngredientHelper();

    private static final Identifier UNKNOWN = Identifier.fromNamespaceAndPath("thaumaturge", "unknown");

    private AspectIngredientHelper() {}

    @Override
    public IIngredientType<AspectInstance> getIngredientType() {
        return AspectIngredientType.INSTANCE;
    }

    @Override
    public String getDisplayName(AspectInstance ingredient) {
        return AspectComponents.name(ingredient.aspect()).getString();
    }

    @Override
    public Object getUid(AspectInstance ingredient, UidContext context) {
        return ingredient.aspect().unwrapKey().map(ResourceKey::identifier).orElse(UNKNOWN);
    }

    @Override
    public Identifier getIdentifier(AspectInstance ingredient) {
        return ingredient.aspect().unwrapKey().map(ResourceKey::identifier).orElse(UNKNOWN);
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
        return ingredient.aspect().unwrapKey().map(key -> key.identifier().toString()).orElse("unbound aspect holder");
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
