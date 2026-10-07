package com.leclowndu93150.thaumaturge.content.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTRecipeSerializers;
import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

public final class SalisMundusRecipe extends CustomRecipe {
    public static final SalisMundusRecipe INSTANCE = new SalisMundusRecipe();
    public static final MapCodec<SalisMundusRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, SalisMundusRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private SalisMundusRecipe() {}

    private static final int REQUIRED_CRYSTALS = 3;

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean bowl = false;
        boolean flint = false;
        boolean redstone = false;
        Set<Identifier> crystals = new HashSet<>();
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(Items.BOWL)) {
                if (bowl) {
                    return false;
                }
                bowl = true;
            } else if (stack.is(Items.FLINT)) {
                if (flint) {
                    return false;
                }
                flint = true;
            } else if (stack.is(Tags.Items.DUSTS_REDSTONE)) {
                if (redstone) {
                    return false;
                }
                redstone = true;
            } else {
                AspectInstance aspect = stack.get(TTDataComponents.CRYSTAL_ASPECT.get());
                if (!stack.is(TTItems.ESSENTIA_CRYSTAL.get()) || aspect == null) {
                    return false;
                }
                if (crystals.size() >= REQUIRED_CRYSTALS || !crystals.add(aspect.aspect().getKey().identifier())) {
                    return false;
                }
            }
        }
        return bowl && flint && redstone && crystals.size() == REQUIRED_CRYSTALS;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return new ItemStack(TTItems.SALIS_MUNDUS.get());
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> result = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < result.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(Items.FLINT) || stack.is(Items.BOWL)) {
                result.set(slot, stack.copyWithCount(1));
            } else {
                ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
                if (remainder != null) {
                    result.set(slot, remainder.create());
                }
            }
        }
        return result;
    }

    @Override
    public List<RecipeDisplay> display() {
        SlotDisplay crystal = new SlotDisplay.ItemSlotDisplay(TTItems.ESSENTIA_CRYSTAL.get().builtInRegistryHolder());
        return List.of(new ShapelessCraftingRecipeDisplay(
                List.of(new SlotDisplay.ItemSlotDisplay(Items.FLINT.builtInRegistryHolder()), new SlotDisplay.ItemSlotDisplay(Items.BOWL.builtInRegistryHolder()),
                        new SlotDisplay.TagSlotDisplay(Tags.Items.DUSTS_REDSTONE), crystal, crystal, crystal),
                new SlotDisplay.ItemSlotDisplay(TTItems.SALIS_MUNDUS.get().builtInRegistryHolder()), new SlotDisplay.ItemSlotDisplay(Blocks.CRAFTING_TABLE.asItem().builtInRegistryHolder())));
    }

    @Override
    public RecipeSerializer<SalisMundusRecipe> getSerializer() {
        return TTRecipeSerializers.SALIS_MUNDUS.get();
    }
}
