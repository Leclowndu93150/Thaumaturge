package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

public final class InfusionCraftJob {
    private static final int PENALTY_UNITS = 1;

    public static final Codec<InfusionCraftJob> CODEC = RecordCodecBuilder.create(i -> i
            .group(ItemStack.CODEC.listOf().fieldOf("ingredients").forGetter(j -> j.ingredients), AspectList.CODEC.fieldOf("essentia").forGetter(j -> j.essentia),
                    AspectList.CODEC.optionalFieldOf("quoted_essentia", AspectList.EMPTY).forGetter(j -> j.quotedEssentia), ItemStack.CODEC.fieldOf("result").forGetter(j -> j.result),
                    ItemStack.CODEC.fieldOf("catalyst").forGetter(j -> j.catalyst), Codec.INT.fieldOf("instability").forGetter(j -> j.instability),
                    UUIDUtil.CODEC.optionalFieldOf("player").forGetter(j -> j.player), PreparedRecipe.CODEC.optionalFieldOf("prepared_recipe").forGetter(j -> j.preparedRecipe))
            .apply(i, InfusionCraftJob::new));

    private final List<ItemStack> ingredients;
    private AspectList essentia;
    private final AspectList quotedEssentia;
    private final ItemStack result;
    private final ItemStack catalyst;
    private final int instability;
    private final Optional<UUID> player;
    private final Optional<PreparedRecipe> preparedRecipe;

    public InfusionCraftJob(List<ItemStack> ingredients, AspectList essentia, AspectList quotedEssentia, ItemStack result, ItemStack catalyst, int instability, Optional<UUID> player) {
        this(ingredients, essentia, quotedEssentia, result, catalyst, instability, player, Optional.empty());
    }

    public InfusionCraftJob(List<ItemStack> ingredients, AspectList essentia, AspectList quotedEssentia, ItemStack result, ItemStack catalyst, int instability, Optional<UUID> player, Optional<PreparedRecipe> preparedRecipe) {
        this.ingredients = new ArrayList<>(ingredients.stream().map(ItemStack::copy).toList());
        this.essentia = essentia;
        this.quotedEssentia = quotedEssentia.isEmpty() ? essentia : quotedEssentia;
        this.result = result.copy();
        this.catalyst = catalyst.copy();
        this.instability = instability;
        this.player = player;
        this.preparedRecipe = preparedRecipe;
    }

    public List<ItemStack> ingredients() {
        return ingredients;
    }

    public AspectList essentia() {
        return essentia;
    }

    public void setEssentia(AspectList essentia) {
        this.essentia = essentia;
    }

    public AspectList quotedEssentia() {
        return quotedEssentia;
    }

    public void addPenaltyEssentia(RandomSource random) {
        List<AspectInstance> quoted = quotedEssentia.entries();
        if (quoted.isEmpty()) {
            return;
        }
        essentia = essentia.add(quoted.get(random.nextInt(quoted.size())).aspect(), PENALTY_UNITS);
    }

    public ItemStack result() {
        return result;
    }

    public ItemStack catalyst() {
        return catalyst;
    }

    public int instability() {
        return instability;
    }

    public Optional<UUID> player() {
        return player;
    }

    public Optional<PreparedRecipe> preparedRecipe() {
        return preparedRecipe;
    }

    public record PreparedRecipe(ResourceKey<Recipe<?>> id, int version, List<ItemStack> components) {
        public static final Codec<PreparedRecipe> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(ResourceKey.codec(Registries.RECIPE).fieldOf("id").forGetter(PreparedRecipe::id), Codec.intRange(1, 1).fieldOf("version").forGetter(PreparedRecipe::version),
                        ItemStack.CODEC.listOf(0, 64).fieldOf("components").forGetter(PreparedRecipe::components)).apply(instance, PreparedRecipe::new));

        public PreparedRecipe {
            components = components.stream().map(ItemStack::copy).toList();
        }

        @Override
        public List<ItemStack> components() {
            return components.stream().map(ItemStack::copy).toList();
        }
    }
}
