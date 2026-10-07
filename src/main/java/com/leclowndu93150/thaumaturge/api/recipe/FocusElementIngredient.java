package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.FocusUnit;
import com.leclowndu93150.thaumaturge.content.casters.ItemFocus;
import com.leclowndu93150.thaumaturge.registry.TTFocusElements;
import com.leclowndu93150.thaumaturge.registry.TTIngredientTypes;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Matches focus items that contain all the given elements.
 *
 * <p>In the recipe JSON, {@code elements} takes a single id or a list of ids.
 */
public record FocusElementIngredient(Set<Identifier> elements) implements ICustomIngredient {

    public static final MapCodec<FocusElementIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.withAlternative(Identifier.CODEC.listOf(), Identifier.CODEC, List::of)
            .validate(list -> list.isEmpty() ? DataResult.error(() -> "Focus ingredient needs at least one element") : DataResult.success(list)).fieldOf("elements")
            .forGetter(ingredient -> List.copyOf(ingredient.elements()))).apply(instance, list -> new FocusElementIngredient(Set.copyOf(list))));

    private static final List<Holder<Item>> FOCUS_ITEMS = List.of(TTItems.FOCUS_1, TTItems.FOCUS_2, TTItems.FOCUS_3);

    public FocusElementIngredient {
        elements = Set.copyOf(elements);
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("Focus ingredient needs at least one element");
        }
    }

    public static Ingredient of(Identifier... elements) {
        return new FocusElementIngredient(Set.of(elements)).toVanilla();
    }

    public static Ingredient of(Collection<Identifier> elements) {
        return new FocusElementIngredient(Set.copyOf(elements)).toVanilla();
    }

    @Override
    public boolean test(ItemStack stack) {
        FocusPackage pack = ItemFocus.getPackage(stack);
        if (pack == null) {
            return false;
        }
        Set<Identifier> present = new HashSet<>();
        collectElements(pack, present);
        return present.containsAll(elements);
    }

    private static void collectElements(FocusPackage pack, Set<Identifier> into) {
        for (FocusUnit unit : pack.units()) {
            into.add(unit.element());
            for (FocusPackage branch : unit.branches()) {
                collectElements(branch, into);
            }
        }
    }

    @Override
    public Stream<Holder<Item>> items() {
        return FOCUS_ITEMS.stream();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TTIngredientTypes.FOCUS_ELEMENT.get();
    }

    public List<ItemStack> displayStacks() {
        FocusPackage.Builder builder = FocusPackage.builder().add(TTFocusElements.ROOT.getId());
        elements.stream().sorted().forEach(builder::add);
        FocusPackage sample = builder.build();
        return FOCUS_ITEMS.stream().map(item -> {
            ItemStack stack = new ItemStack(item.value());
            ItemFocus.setPackage(stack, sample);
            return stack;
        }).toList();
    }
}
