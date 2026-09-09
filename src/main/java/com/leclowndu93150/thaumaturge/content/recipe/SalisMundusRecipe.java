package com.leclowndu93150.thaumaturge.content.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCRecipeSerializers;
import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;

public final class SalisMundusRecipe extends CustomRecipe {
    public static final SalisMundusRecipe INSTANCE = new SalisMundusRecipe();
    public static final MapCodec<SalisMundusRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, SalisMundusRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    private SalisMundusRecipe() {
        super(CraftingBookCategory.MISC);
    }

    private static final int REQUIRED_CRYSTALS = 3;
    private static final Ingredient FLINT = Ingredient.of(Items.FLINT);
    private static final Ingredient REDSTONE = Ingredient.of(Tags.Items.DUSTS_REDSTONE);
    private static final Ingredient BOWL = Ingredient.of(Items.BOWL);
    private static final Ingredient CRYSTAL = Ingredient.of(TCItems.ESSENTIA_CRYSTAL.get());

    public static List<Ingredient> displayIngredients() {
        return List.of(FLINT, BOWL, REDSTONE, CRYSTAL, CRYSTAL, CRYSTAL);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean bowl = false;
        boolean flint = false;
        boolean redstone = false;
        Set<ResourceLocation> crystals = new HashSet<>();
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (BOWL.test(stack)) {
                if (bowl) {
                    return false;
                }
                bowl = true;
            } else if (FLINT.test(stack)) {
                if (flint) {
                    return false;
                }
                flint = true;
            } else if (REDSTONE.test(stack)) {
                if (redstone) {
                    return false;
                }
                redstone = true;
            } else {
                AspectInstance aspect = stack.get(TCDataComponents.CRYSTAL_ASPECT.get());
                if (!stack.is(TCItems.ESSENTIA_CRYSTAL.get()) || aspect == null) {
                    return false;
                }
                if (crystals.size() >= REQUIRED_CRYSTALS
                        || !crystals.add(aspect.aspect().getKey().location())) {
                    return false;
                }
            }
        }
        return bowl && flint && redstone && crystals.size() == REQUIRED_CRYSTALS;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(TCItems.SALIS_MUNDUS.get());
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(TCItems.SALIS_MUNDUS.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 6;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < result.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (FLINT.test(stack) || BOWL.test(stack)) {
                result.set(slot, stack.copyWithCount(1));
            } else if (stack.getItem().hasCraftingRemainingItem(stack)) {
                result.set(slot, stack.getItem().getCraftingRemainingItem(stack));
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<SalisMundusRecipe> getSerializer() {
        return TCRecipeSerializers.SALIS_MUNDUS.get();
    }
}
