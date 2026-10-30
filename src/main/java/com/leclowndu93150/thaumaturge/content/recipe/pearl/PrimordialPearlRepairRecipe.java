package com.leclowndu93150.thaumaturge.content.recipe.pearl;

import com.leclowndu93150.thaumaturge.content.item.PrimordialPearlItem;
import com.leclowndu93150.thaumaturge.content.recipe.SimpleRecipeSerializer;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class PrimordialPearlRepairRecipe extends CustomRecipe {
    public static final PrimordialPearlRepairRecipe INSTANCE = new PrimordialPearlRepairRecipe();
    public static final MapCodec<PrimordialPearlRepairRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, PrimordialPearlRepairRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<PrimordialPearlRepairRecipe> SERIALIZER =
            new SimpleRecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private PrimordialPearlRepairRecipe() {
        super(CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return remainingDurability(input) >= 0;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        int remainingDurability = remainingDurability(input);
        if (remainingDurability < 0) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(TTItems.PRIMORDIAL_PEARL.get());
        result.setDamageValue(Math.max(0, PrimordialPearlItem.MAX_DAMAGE - remainingDurability));
        return result;
    }

    private static int remainingDurability(CraftingInput input) {
        int pearls = 0;
        int durability = 0;
        for (ItemStack stack : input.items()) {
            if (stack.isEmpty()) continue;
            if (!isUsablePearl(stack) || ++pearls > 2) return -1;
            durability += PrimordialPearlItem.MAX_DAMAGE - stack.getDamageValue();
        }
        return pearls == 2 ? durability : -1;
    }

    private static boolean isUsablePearl(ItemStack stack) {
        return stack.is(TTItems.PRIMORDIAL_PEARL)
                && stack.getCount() == 1
                && stack.getDamageValue() < PrimordialPearlItem.MAX_DAMAGE;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }
}
