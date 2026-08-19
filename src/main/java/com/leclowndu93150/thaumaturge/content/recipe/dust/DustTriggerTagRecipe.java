package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerInput;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class DustTriggerTagRecipe implements DustTrigger {
    public static final MapCodec<DustTriggerTagRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(TagKey.codec(Registries.BLOCK).fieldOf("target_tag").forGetter(r -> r.targetTag),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result), ResearchGate.CODEC.optionalFieldOf("research").forGetter(r -> r.research)).apply(i, DustTriggerTagRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DustTriggerTagRecipe> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC.map(rl -> TagKey.create(Registries.BLOCK, rl), TagKey::location), r -> r.targetTag, ItemStackTemplate.STREAM_CODEC, r -> r.result,
            ByteBufCodecs.optional(ResearchGate.STREAM_CODEC), r -> r.research, DustTriggerTagRecipe::new);

    public static final RecipeSerializer<DustTriggerTagRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final TagKey<Block> targetTag;
    private final ItemStackTemplate result;
    private final Optional<ResearchGate> research;

    public DustTriggerTagRecipe(TagKey<Block> targetTag, ItemStackTemplate result, Optional<ResearchGate> research) {
        this.targetTag = targetTag;
        this.result = result;
        this.research = research;
    }

    public TagKey<Block> targetTag() {
        return this.targetTag;
    }

    public ItemStack result() {
        return this.result.create();
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return this.research;
    }

    @Override
    public boolean matches(DustTriggerInput input, Level level) {
        return input.clicked().is(this.targetTag);
    }

    @Override
    public ItemStack assemble(DustTriggerInput input) {
        return this.result.create();
    }

    @Override
    public void execute(DustTriggerInput input, Player player, @org.jspecify.annotations.Nullable DustTriggerPlacement placement, Direction useFace) {
        if (!(input.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        DustTriggerSwapQueue.enqueueDrop(serverLevel, input.pos(), input.clicked(), this.result.create(), 50);
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<DustTriggerTagRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<DustTrigger> getType() {
        return TCRecipeTypes.DUST_TRIGGER.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
