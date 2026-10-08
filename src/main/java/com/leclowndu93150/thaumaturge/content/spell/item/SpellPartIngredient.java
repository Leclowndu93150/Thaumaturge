package com.leclowndu93150.thaumaturge.content.spell.item;

import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.registry.TTIngredientTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

public record SpellPartIngredient(Set<ResourceKey<SpellPart>> parts) implements ICustomIngredient {
    public static final MapCodec<SpellPartIngredient> CODEC =
            RecordCodecBuilder.mapCodec(i -> i.group(Codec.withAlternative(
                                    ResourceKey.codec(SpellPart.REGISTRY_KEY).listOf(),
                                    ResourceKey.codec(SpellPart.REGISTRY_KEY),
                                    List::of)
                            .validate(list -> list.isEmpty()
                                    ? DataResult.error(() -> "Spell part ingredient needs at least one part")
                                    : DataResult.success(list))
                            .fieldOf("parts")
                            .forGetter(ingredient -> List.copyOf(ingredient.parts())))
                    .apply(i, list -> new SpellPartIngredient(Set.copyOf(list))));

    public SpellPartIngredient {
        parts = Set.copyOf(parts);
    }

    public static Ingredient of(List<ResourceKey<SpellPart>> parts) {
        return new SpellPartIngredient(Set.copyOf(parts)).toVanilla();
    }

    @Override
    public boolean test(ItemStack stack) {
        Spell spell = Spells.spellOf(stack);
        if (spell == null) {
            return false;
        }
        Set<ResourceKey<SpellPart>> present = new HashSet<>();
        spell.root().forEach(node -> present.add(node.part()));
        return present.containsAll(parts);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return BuiltInRegistries.ITEM
                .holders()
                .filter(holder -> holder.getData(FocusTier.DATA_MAP) != null)
                .map(holder -> new ItemStack(holder.value()));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TTIngredientTypes.SPELL_PART.get();
    }

    public List<ItemStack> displayStacks() {
        List<ResourceKey<SpellPart>> ordered = new ArrayList<>(parts);
        ordered.sort((a, b) -> a.location().compareTo(b.location()));
        SpellNode chain = null;
        for (int index = ordered.size() - 1; index >= 0; index--) {
            SpellNode node = SpellNode.of(ordered.get(index));
            chain = chain == null ? node : node.then(chain);
        }
        Spell sample = Spell.empty().withRoot(SpellNode.of(Spell.ORIGIN).then(chain));
        return getItems()
                .map(stack -> {
                    Spells.setSpell(stack, sample);
                    return stack;
                })
                .toList();
    }
}
