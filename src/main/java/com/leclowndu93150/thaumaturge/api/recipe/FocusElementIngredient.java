package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.FocusUnit;
import com.leclowndu93150.thaumaturge.content.casters.ItemFocus;
import com.leclowndu93150.thaumaturge.registry.TCFocusElements;
import com.leclowndu93150.thaumaturge.registry.TCIngredientTypes;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/**
 * Matches focus items that contain all the given elements.
 *
 * <p>In the recipe JSON, {@code elements} takes a single id or a list of ids.
 */
public record FocusElementIngredient(Set<ResourceLocation> elements) implements ICustomIngredient {

    public static final MapCodec<FocusElementIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.withAlternative(ResourceLocation.CODEC.listOf(), ResourceLocation.CODEC, List::of)
                            .validate(list -> list.isEmpty()
                                    ? DataResult.error(() -> "Focus ingredient needs at least one element")
                                    : DataResult.success(list))
                            .fieldOf("elements")
                            .forGetter(ingredient -> List.copyOf(ingredient.elements())))
            .apply(instance, list -> new FocusElementIngredient(Set.copyOf(list))));

    private static final List<Supplier<? extends Item>> FOCUS_ITEMS =
            List.of(TCItems.FOCUS_1, TCItems.FOCUS_2, TCItems.FOCUS_3);

    public FocusElementIngredient {
        elements = Set.copyOf(elements);
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Focus ingredient needs at least one element");
        }
    }

    public static Ingredient of(ResourceLocation... elements) {
        return new FocusElementIngredient(Set.of(elements)).toVanilla();
    }

    public static Ingredient of(Collection<ResourceLocation> elements) {
        return new FocusElementIngredient(Set.copyOf(elements)).toVanilla();
    }

    @Override
    public boolean test(ItemStack stack) {
        FocusPackage pack = ItemFocus.getPackage(stack);
        if (pack == null) {
            return false;
        }
        Set<ResourceLocation> present = new HashSet<>();
        collectElements(pack, present);
        return present.containsAll(elements);
    }

    private static void collectElements(FocusPackage pack, Set<ResourceLocation> into) {
        for (FocusUnit unit : pack.units()) {
            into.add(unit.element());
            for (FocusPackage branch : unit.branches()) {
                collectElements(branch, into);
            }
        }
    }

    @Override
    public Stream<ItemStack> getItems() {
        return displayStacks().stream();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TCIngredientTypes.FOCUS_ELEMENT.get();
    }

    public List<ItemStack> displayStacks() {
        FocusPackage.Builder builder = FocusPackage.builder().add(TCFocusElements.ROOT.getId());
        elements.stream().sorted().forEach(builder::add);
        FocusPackage sample = builder.build();
        return FOCUS_ITEMS.stream()
                .map(item -> {
                    ItemStack stack = new ItemStack(item.get());
                    ItemFocus.setPackage(stack, sample);
                    return stack;
                })
                .toList();
    }
}
