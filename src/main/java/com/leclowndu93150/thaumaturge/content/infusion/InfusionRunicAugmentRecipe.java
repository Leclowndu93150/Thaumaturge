package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.IInfusionRecipe;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItemTags;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;

public final class InfusionRunicAugmentRecipe implements Recipe<InfusionInput>, IInfusionRecipe {
    public static final int BASE_INSTABILITY = 5;
    private static final int BASE_COST = 20;
    private static final int MAX_CHARGE = 120;

    public static final MapCodec<InfusionRunicAugmentRecipe> MAP_CODEC = RecordCodecBuilder
            .mapCodec(i -> i.group(Ingredient.CODEC.listOf(1, 64).fieldOf("components").forGetter(r -> r.baseComponents), Ingredient.CODEC.fieldOf("per_level").forGetter(r -> r.perLevel),
                    AspectList.NON_EMPTY_CODEC.fieldOf("aspects").forGetter(r -> r.baseAspects), Ingredient.CODEC.fieldOf("display_catalyst").forGetter(r -> r.displayCatalyst),
                    ResearchGate.CODEC.optionalFieldOf("research").forGetter(r -> r.research)).apply(i, InfusionRunicAugmentRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionRunicAugmentRecipe> STREAM_CODEC = StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
            r -> r.baseComponents, Ingredient.CONTENTS_STREAM_CODEC, r -> r.perLevel, AspectList.STREAM_CODEC, r -> r.baseAspects, Ingredient.CONTENTS_STREAM_CODEC, r -> r.displayCatalyst,
            ByteBufCodecs.optional(ResearchGate.STREAM_CODEC), r -> r.research, InfusionRunicAugmentRecipe::new);

    public static final RecipeSerializer<InfusionRunicAugmentRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final List<Ingredient> baseComponents;
    private final Ingredient perLevel;
    private final AspectList baseAspects;
    private final Ingredient displayCatalyst;
    private final Optional<ResearchGate> research;

    public InfusionRunicAugmentRecipe(List<Ingredient> baseComponents, Ingredient perLevel, AspectList baseAspects, Ingredient displayCatalyst, Optional<ResearchGate> research) {
        this.baseComponents = List.copyOf(baseComponents);
        this.perLevel = perLevel;
        this.baseAspects = baseAspects;
        this.displayCatalyst = displayCatalyst;
        this.research = research;
    }

    public static int charge(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.RUNIC_CHARGE.get(), 0);
    }

    public static boolean isShieldable(ItemStack stack) {
        if (stack.is(TCItemTags.RUNIC_SHIELDABLE)) {
            return true;
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }

    public List<Ingredient> scaledComponents(ItemStack catalyst) {
        List<Ingredient> out = new ArrayList<>(baseComponents);
        for (int c = 0; c < charge(catalyst); c++) {
            out.add(perLevel);
        }
        return out;
    }

    public List<ItemStack> matchScaled(ItemStack catalyst, List<ItemStack> available) {
        List<Ingredient> needed = scaledComponents(catalyst);
        if (available.size() != needed.size()) {
            return null;
        }
        List<ItemStack> remaining = new ArrayList<>(available);
        List<ItemStack> consumed = new ArrayList<>(needed.size());
        for (Ingredient component : needed) {
            ItemStack found = null;
            for (ItemStack candidate : remaining) {
                if (component.test(candidate)) {
                    found = candidate;
                    break;
                }
            }
            if (found == null) {
                return null;
            }
            remaining.remove(found);
            consumed.add(found.copyWithCount(1));
        }
        return consumed;
    }

    public AspectList scaledAspects(ItemStack catalyst) {
        double factor = (1.0 + Math.pow(2.0, charge(catalyst))) / 2.0;
        AspectList out = AspectList.EMPTY;
        for (AspectInstance instance : baseAspects.entries()) {
            int amount = (int) (instance.amount() * factor);
            if (amount > 0) {
                out = out.add(instance.aspect(), amount);
            }
        }
        return out;
    }

    public int scaledInstability(ItemStack catalyst) {
        return BASE_INSTABILITY + charge(catalyst) / 2;
    }

    public ItemStack augmentedResult(ItemStack catalyst) {
        ItemStack out = catalyst.copyWithCount(1);
        out.set(TCDataComponents.RUNIC_CHARGE.get(), Math.min(MAX_CHARGE, charge(catalyst) + 1));
        return out;
    }

    @Override
    public boolean matches(InfusionInput input, Level level) {
        if (!isShieldable(input.catalyst())) {
            return false;
        }
        return matchScaled(input.catalyst(), input.components()) != null;
    }

    @Override
    public Ingredient catalyst() {
        return displayCatalyst;
    }

    @Override
    public List<Ingredient> components() {
        return baseComponents;
    }

    @Override
    public AspectList aspects() {
        return baseAspects;
    }

    @Override
    public int instability() {
        return BASE_INSTABILITY;
    }

    @Override
    public ItemStack resultItem() {
        ItemStack base = displayCatalyst.items().findFirst().map(holder -> new ItemStack(holder.value())).orElse(ItemStack.EMPTY);
        if (!base.isEmpty()) {
            base.set(TCDataComponents.RUNIC_CHARGE.get(), 1);
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
        return List.of(new InfusionRecipeDisplay(displayCatalyst.display(), baseComponents.stream().map(Ingredient::display).map(d -> (SlotDisplay) d).toList(), baseAspects, BASE_INSTABILITY,
                resultDisplay));
    }

    @Override
    public ItemStack assemble(InfusionInput input) {
        return augmentedResult(input.catalyst());
    }

    @Override
    public RecipeSerializer<InfusionRunicAugmentRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<InfusionRunicAugmentRecipe> getType() {
        return TCRecipeTypes.RUNIC_AUGMENT.get();
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
