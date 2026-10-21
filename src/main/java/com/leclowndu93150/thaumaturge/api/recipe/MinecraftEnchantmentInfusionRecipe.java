package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Applies one level of a registered Minecraft enchantment to a copy of the catalyst.
 * The recipe cap never overrides the enchantment's own maximum level. The essentia
 * cost scales by the next level plus 0.33 for each other enchantment on the catalyst.
 */
public record MinecraftEnchantmentInfusionRecipe(ResourceKey<Enchantment> enchantment, Ingredient catalyst, List<Ingredient> components, AspectList aspects, int instability, int maxLevel,
        Optional<ResearchGate> researchGate) implements DynamicInfusionRecipe {
    public static final MapCodec<MinecraftEnchantmentInfusionRecipe> MAP_CODEC = RecordCodecBuilder
            .mapCodec(
                    instance -> instance
                            .group(ResourceKey.codec(Registries.ENCHANTMENT).fieldOf("enchantment").forGetter(MinecraftEnchantmentInfusionRecipe::enchantment),
                                    Ingredient.CODEC.fieldOf("catalyst").forGetter(MinecraftEnchantmentInfusionRecipe::catalyst),
                                    Ingredient.CODEC.listOf(1, 64).fieldOf("components").forGetter(MinecraftEnchantmentInfusionRecipe::components),
                                    AspectList.NON_EMPTY_CODEC.fieldOf("aspects").forGetter(MinecraftEnchantmentInfusionRecipe::aspects),
                                    Codec.intRange(0, 100).optionalFieldOf("instability", 0).forGetter(MinecraftEnchantmentInfusionRecipe::instability),
                                    Codec.intRange(1, 255).optionalFieldOf("max_level", 255).forGetter(MinecraftEnchantmentInfusionRecipe::maxLevel),
                                    ResearchGate.CODEC.optionalFieldOf("research").forGetter(MinecraftEnchantmentInfusionRecipe::researchGate))
                            .apply(instance, MinecraftEnchantmentInfusionRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MinecraftEnchantmentInfusionRecipe> STREAM_CODEC = StreamCodec.composite(ResourceKey.streamCodec(Registries.ENCHANTMENT),
            MinecraftEnchantmentInfusionRecipe::enchantment, Ingredient.CONTENTS_STREAM_CODEC, MinecraftEnchantmentInfusionRecipe::catalyst,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), MinecraftEnchantmentInfusionRecipe::components, AspectList.STREAM_CODEC, MinecraftEnchantmentInfusionRecipe::aspects,
            ByteBufCodecs.VAR_INT, MinecraftEnchantmentInfusionRecipe::instability, ByteBufCodecs.VAR_INT, MinecraftEnchantmentInfusionRecipe::maxLevel,
            ByteBufCodecs.optional(ResearchGate.STREAM_CODEC), MinecraftEnchantmentInfusionRecipe::researchGate, MinecraftEnchantmentInfusionRecipe::new);

    public static final RecipeSerializer<MinecraftEnchantmentInfusionRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public MinecraftEnchantmentInfusionRecipe {
        Objects.requireNonNull(enchantment);
        Objects.requireNonNull(catalyst);
        Objects.requireNonNull(aspects);
        Objects.requireNonNull(researchGate);
        components = List.copyOf(components);
        if (components.isEmpty() || components.size() > 64 || aspects.isEmpty() || instability < 0 || instability > 100 || maxLevel < 1 || maxLevel > 255) {
            throw new IllegalArgumentException("Invalid enchantment infusion recipe");
        }
    }

    @Override
    public boolean matches(InfusionRecipeInput input, HolderLookup.Provider registries) {
        return eligibleEnchantment(input.catalyst(), registries).isPresent() && matchComponents(input.components()) != null;
    }

    @Override
    public AspectList quote(InfusionRecipeInput input, HolderLookup.Provider registries) {
        if (!matches(input, registries)) {
            return AspectList.EMPTY;
        }
        Holder<Enchantment> resolved = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
        ItemEnchantments existing = input.catalyst().getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        int current = existing.getLevel(resolved);
        int others = existing.keySet().size() - (current > 0 ? 1 : 0);
        double modifier = current + 1 + 0.33D * others;
        AspectList cost = AspectList.EMPTY;
        for (AspectInstance aspect : aspects.entries()) {
            double amount = aspect.amount() * modifier;
            if (amount > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Enchantment infusion cost exceeds integer range");
            }
            cost = cost.add(aspect.aspect(), (int) amount);
        }
        return cost;
    }

    @Override
    public ItemStack prepareResult(InfusionRecipeInput input, HolderLookup.Provider registries) {
        if (!matches(input, registries)) {
            return ItemStack.EMPTY;
        }
        Holder<Enchantment> resolved = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
        ItemStack result = input.catalyst().copyWithCount(1);
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(result.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
        enchantments.set(resolved, enchantments.getLevel(resolved) + 1);
        result.set(DataComponents.ENCHANTMENTS, enchantments.toImmutable());
        return result;
    }

    @Override
    public ItemStack resultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<MinecraftEnchantmentInfusionRecipe> getSerializer() {
        return SERIALIZER;
    }

    private Optional<Holder.Reference<Enchantment>> eligibleEnchantment(ItemStack stack, HolderLookup.Provider registries) {
        Optional<Holder.Reference<Enchantment>> resolved = registries.lookup(Registries.ENCHANTMENT).flatMap(registry -> registry.get(enchantment));
        if (stack.isEmpty() || !catalyst.test(stack) || resolved.isEmpty()) {
            return Optional.empty();
        }
        Holder<Enchantment> selected = resolved.get();
        ItemEnchantments existing = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!selected.value().canEnchant(stack) || existing.getLevel(selected) >= Math.min(maxLevel, selected.value().getMaxLevel())) {
            return Optional.empty();
        }
        for (Holder<Enchantment> other : existing.keySet()) {
            if (!other.is(enchantment) && !Enchantment.areCompatible(selected, other)) {
                return Optional.empty();
            }
        }
        return resolved;
    }
}
