package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work.AdjacentEssentiaDraw;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work.BrainBoxScanner;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work.CraftSelection;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work.RecipeQueue;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work.RequirementTracker;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.work.ThaumatoriumScheduler;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityThaumatorium extends AbstractSyncedBlockEntity implements SinkOnlyEssentiaFace {
    private static final String ESSENTIA_KEY = "Essentia";
    private static final String MAX_RECIPES_KEY = "MaxRecipes";
    private static final String QUEUE_KEY = "Queue";
    private static final String CATALYST_KEY = "Catalyst";
    private static final int CATALYST_SLOTS = 1;
    private static final int CATALYST_SLOT = 0;
    private static final int HEAT_DEPTH = 2;
    private static final int CATALYST_COST = 1;
    private static final float HISS_VOLUME = 0.3F;
    private static final float HISS_PITCH_LOW = 1.7F;
    private static final float HISS_PITCH_RANGE = 0.25F;

    private final CatalystHandler catalyst = new CatalystHandler();
    private final RecipeQueue queue = new RecipeQueue();
    private final RequirementTracker requirements = new RequirementTracker();
    private final CraftSelection selection = new CraftSelection();
    private final AdjacentEssentiaDraw draw = new AdjacentEssentiaDraw();
    private final Map<ResourceLocation, UUID> crafters = new HashMap<>();
    private final BrainBoxScanner brainBoxes = new BrainBoxScanner();
    private final ThaumatoriumScheduler scheduler = new ThaumatoriumScheduler();
    private boolean heated;

    public BlockEntityThaumatorium(BlockPos pos, BlockState state) {
        super(TTBlockEntities.THAUMATORIUM.get(), pos, state);
    }

    public ItemStackHandler catalyst() {
        return catalyst;
    }

    public AspectList essentia() {
        return requirements.received();
    }

    public List<ResourceLocation> queue() {
        return queue.ids();
    }

    public int maxRecipes() {
        return queue.capacity();
    }

    public ItemStack catalystStack() {
        return catalyst.getStackInSlot(CATALYST_SLOT).copy();
    }

    public void dropContents() {
        if (level instanceof ServerLevel server) {
            BlockPos pos = worldPosition;
            Containers.dropItemStack(
                    server, pos.getX(), pos.getY(), pos.getZ(), catalyst.getStackInSlot(CATALYST_SLOT));
            catalyst.setStackInSlot(CATALYST_SLOT, ItemStack.EMPTY);
        }
    }

    public void toggleRecipe(ServerLevel level, Player player, ResourceLocation recipeId) {
        if (queue.remove(recipeId)) {
            crafters.remove(recipeId);
            resetSelection();
            setChangedAndSync();
            return;
        }
        CrucibleRecipe recipe = recipe(level, recipeId);
        if (recipe == null || !recipe.doesPassGate(player) || queue.isFull()) {
            return;
        }
        queue.add(recipeId);
        crafters.put(recipeId, player.getUUID());
        resetSelection();
        setChangedAndSync();
    }

    public List<CrucibleRecipe> candidateRecipes(ServerLevel level, Player player, List<ResourceLocation> idsOut) {
        List<CrucibleRecipe> found = new ArrayList<>();
        ItemStack inSlot = catalystStack();
        for (RecipeHolder<CrucibleRecipe> entry :
                level.getRecipeManager().getAllRecipesFor(TTRecipeTypes.CRUCIBLE.get())) {
            ResourceLocation recipeId = entry.id();
            if (isOffered(entry.value(), recipeId, inSlot, player)) {
                found.add(entry.value());
                idsOut.add(recipeId);
            }
        }
        return found;
    }

    private boolean isOffered(CrucibleRecipe recipe, ResourceLocation recipeId, ItemStack inSlot, Player player) {
        if (queue.ids().contains(recipeId)) {
            return true;
        }
        if (inSlot.isEmpty()) {
            return false;
        }
        return recipe.catalyst().test(inSlot) && recipe.doesPassGate(player);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityThaumatorium machine) {
        if (level instanceof ServerLevel server) {
            machine.tick(server, pos);
        }
    }

    private void tick(ServerLevel level, BlockPos pos) {
        scheduler.advance();
        if (scheduler.refreshDue()) {
            refresh(level, pos);
        }
        if (!scheduler.workDue()) {
            return;
        }
        if (canWork(level, pos)) {
            work(level, pos);
        } else {
            selection.request(null);
        }
    }

    private void refresh(ServerLevel level, BlockPos pos) {
        heated = level.getBlockState(pos.below(HEAT_DEPTH)).is(TTBlockTags.CRUCIBLE_HEAT_SOURCES);
        int capacity = RecipeQueue.capacityFor(brainBoxes.count(level, pos, front()));
        if (capacity == queue.capacity()) {
            return;
        }
        if (queue.resize(capacity)) {
            crafters.keySet().retainAll(queue.ids());
            resetSelection();
        }
        setChangedAndSync();
    }

    private void work(ServerLevel level, BlockPos pos) {
        ItemStack inSlot = catalystStack();
        if (inSlot.isEmpty()) {
            selection.request(null);
            return;
        }
        CrucibleRecipe recipe = selectRecipe(level, inSlot);
        if (recipe == null) {
            return;
        }
        Holder<IAspect> missing = requirements.firstUnmet();
        if (missing == null) {
            selection.request(null);
            complete(level, pos, recipe, inSlot);
            return;
        }
        selection.request(missing.unwrapKey().orElse(null));
        pullUnit(level, pos, missing);
    }

    private @Nullable CrucibleRecipe selectRecipe(ServerLevel level, ItemStack inSlot) {
        CrucibleRecipe kept = keptSelection(level, inSlot);
        return kept != null ? kept : adoptFirstFit(level, inSlot);
    }

    private @Nullable CrucibleRecipe keptSelection(ServerLevel level, ItemStack inSlot) {
        ResourceLocation current = selection.recipe();
        if (current == null || !queue.ids().contains(current)) {
            return null;
        }
        CrucibleRecipe recipe = recipe(level, current);
        return recipe != null && recipe.catalyst().test(inSlot) ? recipe : null;
    }

    private @Nullable CrucibleRecipe adoptFirstFit(ServerLevel level, ItemStack inSlot) {
        for (ResourceLocation candidate : queue.ids()) {
            CrucibleRecipe recipe = recipe(level, candidate);
            if (recipe != null && recipe.catalyst().test(inSlot)) {
                selection.choose(candidate);
                requirements.planFor(recipe);
                return recipe;
            }
        }
        resetSelection();
        return null;
    }

    private void complete(ServerLevel level, BlockPos pos, CrucibleRecipe recipe, ItemStack inSlot) {
        CrucibleRecipeInput check = new CrucibleRecipeInput(inSlot, requirements.received());
        if (!recipe.matches(check, level)) {
            return;
        }
        ItemStack result = recipe.assemble(check, level.registryAccess());
        useCatalyst(inSlot);
        requirements.clearReceived();
        credit(level, result);
        InvHelper.eject(level, pos, front(), result);
        float pitch = HISS_PITCH_LOW + level.getRandom().nextFloat() * HISS_PITCH_RANGE;
        level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, HISS_VOLUME, pitch);
        setChangedAndSync();
    }

    private void useCatalyst(ItemStack inSlot) {
        int left = inSlot.getCount() - CATALYST_COST;
        catalyst.setStackInSlot(CATALYST_SLOT, left <= NOTHING ? ItemStack.EMPTY : inSlot.copyWithCount(left));
    }

    private void credit(ServerLevel level, ItemStack result) {
        ResourceLocation current = selection.recipe();
        UUID crafterId = current == null ? null : crafters.get(current);
        ServerPlayer crafter =
                crafterId == null ? null : level.getServer().getPlayerList().getPlayer(crafterId);
        if (crafter == null || result.isEmpty()) {
            return;
        }
        crafter.awardStat(Stats.ITEM_CRAFTED.get(result.getItem()), result.getCount());
        ResearchProgressionEvents.recordCrafted(crafter, result);
    }

    private boolean canWork(ServerLevel level, BlockPos pos) {
        return heated && !queue.isEmpty() && !isPowered(level, pos);
    }

    private static boolean isPowered(ServerLevel level, BlockPos pos) {
        return level.hasNeighborSignal(pos)
                || level.hasNeighborSignal(pos.above())
                || level.hasNeighborSignal(pos.below());
    }

    private void pullUnit(ServerLevel level, BlockPos pos, Holder<IAspect> aspect) {
        int drawn = draw.drawOne(level, pos, front(), aspect);
        if (drawn <= NOTHING) {
            return;
        }
        if (requirements.accept(aspect, drawn) > NOTHING) {
            setChangedAndSync();
        }
    }

    private void resetSelection() {
        selection.reset();
        requirements.clearPlan();
    }

    private @Nullable CrucibleRecipe recipe(ServerLevel level, ResourceLocation id) {
        RecipeHolder<?> holder = level.getRecipeManager().byKey(id).orElse(null);
        return holder != null && holder.value() instanceof CrucibleRecipe recipe ? recipe : null;
    }

    private Direction front() {
        return getBlockState()
                .getOptionalValue(HorizontalDirectionalBlock.FACING)
                .orElse(Direction.NORTH);
    }

    private boolean isSideAllowed(@Nullable Direction face) {
        if (face == null) {
            return false;
        }
        return face != front();
    }

    @Override
    public boolean connectsOn(Direction face) {
        return isSideAllowed(face);
    }

    @Override
    public boolean receivesOn(Direction face) {
        return isSideAllowed(face);
    }

    @Override
    public EssentiaPull pull(@Nullable Direction face) {
        ResourceKey<IAspect> requested = selection.requested();
        if (requested == null) {
            return EssentiaPull.NONE;
        }
        return EssentiaPull.of(Aspects.resolve(level, requested), AdjacentEssentiaDraw.DRAW_SUCTION);
    }

    @Override
    public int roomFor(Holder<IAspect> aspect, Direction face) {
        if (selection.recipe() == null || !isSideAllowed(face)) {
            return NOTHING;
        }
        return requirements.shortfall(aspect);
    }

    @Override
    public int insert(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        if (selection.recipe() == null || !isSideAllowed(face)) {
            return NOTHING;
        }
        if (simulate) {
            return Math.max(NOTHING, Math.min(amount, requirements.shortfall(aspect)));
        }
        int stored = requirements.accept(aspect, amount);
        if (stored <= NOTHING) {
            return NOTHING;
        }
        setChangedAndSync();
        return stored;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.put(CATALYST_KEY, catalyst.serializeNBT(registries));
        TTNbt.store(output, QUEUE_KEY, LegacyIds.IDENTIFIER_CODEC.listOf(), registries, queue.ids());
        output.putInt(MAX_RECIPES_KEY, queue.capacity());
        AspectList stored = requirements.received();
        if (!stored.isEmpty()) {
            TTNbt.store(output, ESSENTIA_KEY, AspectList.CODEC, registries, stored);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        if (input.contains(CATALYST_KEY)) catalyst.deserializeNBT(registries, input.getCompound(CATALYST_KEY));
        List<ResourceLocation> savedIds = TTNbt.read(input, QUEUE_KEY, LegacyIds.IDENTIFIER_CODEC.listOf(), registries)
                .orElse(List.of());
        queue.restore(
                input.contains(MAX_RECIPES_KEY) ? input.getInt(MAX_RECIPES_KEY) : RecipeQueue.BASE_CAPACITY, savedIds);
        requirements.restoreReceived(
                TTNbt.read(input, ESSENTIA_KEY, AspectList.CODEC, registries).orElse(AspectList.EMPTY));
    }

    private final class CatalystHandler extends ItemStackHandler {
        CatalystHandler() {
            super(CATALYST_SLOTS);
        }

        @Override
        protected void onContentsChanged(int index) {
            setChanged();
        }
    }
}
