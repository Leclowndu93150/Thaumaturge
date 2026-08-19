package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.api.recipe.IInfusionRecipe;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantmentHelper;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

public final class InfusionEnchantmentRecipe implements Recipe<InfusionInput>, IInfusionRecipe {
    public static final int INSTABILITY = 4;
    private static final float OTHER_ENCHANT_COST_STEP = 0.33F;
    private static final int WARP_ROLL_BOUND = 10;

    public static final MapCodec<InfusionEnchantmentRecipe> MAP_CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(InfusionEnchantment.CODEC.fieldOf("enchantment").forGetter(r -> r.enchantment), Ingredient.CODEC.listOf(1, 64).fieldOf("components").forGetter(r -> r.components),
                    AspectList.NON_EMPTY_CODEC.fieldOf("aspects").forGetter(r -> r.aspects), Ingredient.CODEC.fieldOf("display_catalyst").forGetter(r -> r.displayCatalyst),
                    ResearchGate.CODEC.optionalFieldOf("research").forGetter(r -> r.research)).apply(i, InfusionEnchantmentRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionEnchantmentRecipe> STREAM_CODEC = StreamCodec.composite(InfusionEnchantment.STREAM_CODEC, r -> r.enchantment,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.components, AspectList.STREAM_CODEC, r -> r.aspects, Ingredient.CONTENTS_STREAM_CODEC, r -> r.displayCatalyst,
            ByteBufCodecs.optional(ResearchGate.STREAM_CODEC), r -> r.research, InfusionEnchantmentRecipe::new);

    public static final RecipeSerializer<InfusionEnchantmentRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final InfusionEnchantment enchantment;
    private final List<Ingredient> components;
    private final AspectList aspects;
    private final Ingredient displayCatalyst;
    private final Optional<ResearchGate> research;

    public InfusionEnchantmentRecipe(InfusionEnchantment enchantment, List<Ingredient> components, AspectList aspects, Ingredient displayCatalyst, Optional<ResearchGate> research) {
        this.enchantment = enchantment;
        this.components = List.copyOf(components);
        this.aspects = aspects;
        this.displayCatalyst = displayCatalyst;
        this.research = research;
    }

    public InfusionEnchantment enchantment() {
        return enchantment;
    }

    @Override
    public boolean matches(InfusionInput input, Level level) {
        ItemStack catalyst = input.catalyst();
        if (!InfusionEnchantmentHelper.canApply(catalyst, enchantment)) {
            return false;
        }
        if (InfusionEnchantmentHelper.level(catalyst, enchantment) >= enchantment.maxLevel()) {
            return false;
        }
        return matchComponents(input.components()) != null;
    }

    public AspectList scaledAspects(ItemStack catalyst) {
        int nextLevel = InfusionEnchantmentHelper.level(catalyst, enchantment) + 1;
        if (nextLevel > enchantment.maxLevel()) {
            return AspectList.EMPTY;
        }
        List<InfusionEnchantment> existing = InfusionEnchantmentHelper.list(catalyst);
        int others = existing.size() - (existing.contains(enchantment) ? 1 : 0);
        float modifier = nextLevel + others * OTHER_ENCHANT_COST_STEP;
        AspectList out = AspectList.EMPTY;
        for (AspectInstance instance : aspects.entries()) {
            int amount = (int) (instance.amount() * modifier);
            if (amount > 0) {
                out = out.add(instance.aspect(), amount);
            }
        }
        return out;
    }

    public ItemStack enchantedResult(ItemStack catalyst, RandomSource random) {
        ItemStack out = catalyst.copyWithCount(1);
        int existing = InfusionEnchantmentHelper.level(out, enchantment);
        if (existing >= enchantment.maxLevel()) {
            return out;
        }
        if (random.nextInt(WARP_ROLL_BOUND) < InfusionEnchantmentHelper.list(catalyst).size()) {
            out.set(TCDataComponents.STACK_WARP.get(), out.getOrDefault(TCDataComponents.STACK_WARP.get(), 0) + 1);
        }
        InfusionEnchantmentHelper.add(out, enchantment, existing + 1);
        return out;
    }

    @Override
    public Ingredient catalyst() {
        return displayCatalyst;
    }

    @Override
    public List<Ingredient> components() {
        return components;
    }

    @Override
    public AspectList aspects() {
        return aspects;
    }

    @Override
    public int instability() {
        return INSTABILITY;
    }

    @Override
    public ItemStack resultItem() {
        ItemStack base = displayCatalyst.items().findFirst().map(holder -> new ItemStack(holder.value())).orElse(ItemStack.EMPTY);
        if (!base.isEmpty()) {
            InfusionEnchantmentHelper.add(base, enchantment, 1);
        }
        return base;
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return research;
    }

    @Override
    public List<RecipeDisplay> display() {
        ItemStack out = resultItem();
        SlotDisplay resultDisplay = out.isEmpty() ? SlotDisplay.Empty.INSTANCE : new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(out));
        return List
                .of(new InfusionRecipeDisplay(displayCatalyst.display(), components().stream().map(Ingredient::display).map(d -> (SlotDisplay) d).toList(), aspects(), instability(), resultDisplay));
    }

    @Override
    public ItemStack assemble(InfusionInput input) {
        ItemStack out = input.catalyst().copyWithCount(1);
        InfusionEnchantmentHelper.add(out, enchantment, InfusionEnchantmentHelper.level(out, enchantment) + 1);
        return out;
    }

    @Override
    public RecipeSerializer<InfusionEnchantmentRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<InfusionEnchantmentRecipe> getType() {
        return TCRecipeTypes.INFUSION_ENCHANTMENT.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }
}
