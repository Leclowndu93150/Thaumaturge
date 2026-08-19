package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public final class BlockEntityThaumatorium extends BlockEntity implements IEssentiaTransport {
    private static final int CHECK_INTERVAL = 40;
    private static final int WORK_INTERVAL = 5;
    private static final int SUCTION = 128;
    private static final int BASE_RECIPES = 1;
    private static final int BRAIN_BOX_BONUS = 2;

    private final ItemStacksResourceHandler catalyst = new ItemStacksResourceHandler(1) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            super.onContentsChanged(index, previousContents);
            setChanged();
        }
    };

    private AspectList essentia = AspectList.EMPTY;
    private final List<Identifier> queue = new ArrayList<>();
    private int maxRecipes = BASE_RECIPES;
    private int currentCraft = -1;
    private @Nullable Holder<IAspect> currentSuction;
    private @Nullable CrucibleRecipe currentRecipe;
    private int counter;
    private boolean heated;

    public BlockEntityThaumatorium(BlockPos pos, BlockState state) {
        super(TCBlockEntities.THAUMATORIUM.get(), pos, state);
    }

    public ItemStacksResourceHandler catalyst() {
        return catalyst;
    }

    public AspectList essentia() {
        return essentia;
    }

    public List<Identifier> queue() {
        return queue;
    }

    public int maxRecipes() {
        return maxRecipes;
    }

    public ItemStack catalystStack() {
        return catalyst.getResource(0).toStack(catalyst.getAmountAsInt(0));
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalDirectionalBlock.FACING) ? state.getValue(HorizontalDirectionalBlock.FACING) : Direction.NORTH;
    }

    public void toggleRecipe(ServerLevel server, Player player, Identifier recipeId) {
        if (queue.remove(recipeId)) {
            afterQueueChange();
            return;
        }
        RecipeHolder<?> holder = findRecipe(server, recipeId);
        if (holder == null || !(holder.value() instanceof CrucibleRecipe recipe)) {
            return;
        }
        if (!recipe.doesPassGate(player) || queue.size() >= maxRecipes) {
            return;
        }
        queue.add(recipeId);
        afterQueueChange();
    }

    private void afterQueueChange() {
        currentCraft = -1;
        currentRecipe = null;
        currentSuction = null;
        setChanged();
        syncToClient();
    }

    private @Nullable RecipeHolder<?> findRecipe(ServerLevel server, Identifier recipeId) {
        for (RecipeHolder<?> holder : server.recipeAccess().getRecipes()) {
            if (holder.id().identifier().equals(recipeId)) {
                return holder;
            }
        }
        return null;
    }

    public List<CrucibleRecipe> candidateRecipes(ServerLevel server, Player player, List<Identifier> idsOut) {
        List<CrucibleRecipe> found = new ArrayList<>();
        ItemStack stack = catalystStack();
        for (RecipeHolder<?> holder : server.recipeAccess().getRecipes()) {
            if (!(holder.value() instanceof CrucibleRecipe recipe)) {
                continue;
            }
            Identifier id = holder.id().identifier();
            boolean queued = queue.contains(id);
            boolean matches = !stack.isEmpty() && recipe.catalyst().test(stack) && recipe.doesPassGate(player);
            if (queued || matches) {
                found.add(recipe);
                idsOut.add(id);
            }
        }
        return found;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityThaumatorium machine) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (machine.counter == 0 || machine.counter % CHECK_INTERVAL == 0) {
            machine.heated = server.getBlockState(pos.below(2)).is(TCBlockTags.CRUCIBLE_HEAT_SOURCES);
            machine.updateUpgrades(server);
        }
        machine.counter++;
        if (!machine.heated || machine.gettingPower(server) || machine.counter % WORK_INTERVAL != 0 || machine.queue.isEmpty()) {
            return;
        }
        ItemStack stack = machine.catalystStack();
        if (stack.isEmpty()) {
            machine.currentSuction = null;
            return;
        }
        if (machine.currentCraft < 0 || machine.currentCraft >= machine.queue.size() || machine.currentRecipe == null || !machine.currentRecipe.catalyst().test(stack)) {
            machine.currentCraft = -1;
            machine.currentRecipe = null;
            for (int a = 0; a < machine.queue.size(); a++) {
                RecipeHolder<?> holder = machine.findRecipe(server, machine.queue.get(a));
                if (holder != null && holder.value() instanceof CrucibleRecipe recipe && recipe.catalyst().test(stack)) {
                    machine.currentCraft = a;
                    machine.currentRecipe = recipe;
                    break;
                }
            }
        }
        if (machine.currentCraft < 0 || machine.currentRecipe == null) {
            return;
        }
        boolean done = true;
        machine.currentSuction = null;
        for (var entry : machine.currentRecipe.aspects().sortedByTag()) {
            if (machine.essentia.amountOf(entry.aspect()) < entry.amount()) {
                machine.currentSuction = entry.aspect();
                done = false;
                break;
            }
        }
        if (done) {
            machine.completeRecipe(server);
        } else if (machine.currentSuction != null) {
            machine.fill(server);
        }
    }

    private boolean gettingPower(ServerLevel server) {
        return server.hasNeighborSignal(getBlockPos()) || server.hasNeighborSignal(getBlockPos().above()) || server.hasNeighborSignal(getBlockPos().below());
    }

    private void updateUpgrades(ServerLevel server) {
        Direction facing = facing();
        int max = BASE_RECIPES;
        for (int yy = 0; yy <= 1; yy++) {
            for (Direction dir : Direction.values()) {
                if (dir == Direction.DOWN || dir == facing) {
                    continue;
                }
                BlockPos bp = getBlockPos().above(yy).relative(dir);
                BlockState bs = server.getBlockState(bp);
                if (bs.is(TCBlocks.BRAIN_BOX.get()) && bs.getValue(BlockStateProperties.FACING) == dir.getOpposite()) {
                    max += BRAIN_BOX_BONUS;
                }
            }
        }
        if (max != maxRecipes) {
            maxRecipes = max;
            while (queue.size() > maxRecipes) {
                queue.removeLast();
            }
            setChanged();
            syncToClient();
        }
    }

    private void completeRecipe(ServerLevel server) {
        ItemStack stack = catalystStack();
        if (currentRecipe == null || !currentRecipe.matches(new CrucibleRecipeInput(stack, essentia), server)) {
            return;
        }
        try (Transaction ctx = Transaction.openRoot()) {
            if (catalyst.extract(ItemResource.of(stack), 1, ctx) != 1) {
                return;
            }
            ctx.commit();
        }
        ItemStack result = currentRecipe.assemble(new CrucibleRecipeInput(stack, essentia));
        essentia = AspectList.EMPTY;
        InvHelper.ejectStackAt(server, getBlockPos(), facing(), result);
        server.playSound(null, getBlockPos(), SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.25F, 2.6F + (server.getRandom().nextFloat() - server.getRandom().nextFloat()) * 0.8F);
        currentCraft = -1;
        currentRecipe = null;
        setChanged();
        syncToClient();
    }

    private void fill(ServerLevel server) {
        Direction facing = facing();
        for (int y = 0; y <= 1; y++) {
            for (Direction dir : Direction.values()) {
                if (dir == facing || dir == Direction.DOWN || y == 0 && dir == Direction.UP) {
                    continue;
                }
                BlockPos from = getBlockPos().above(y);
                IEssentiaTransport ic = EssentiaFlowHandler.transport(server, from.relative(dir), dir.getOpposite());
                if (ic == null) {
                    continue;
                }
                if (ic.getEssentiaAmount(dir.getOpposite()) > 0 && ic.getSuctionAmount(dir.getOpposite()) < getSuctionAmount(dir) && getSuctionAmount(dir) >= ic.getMinimumSuction()) {
                    int taken = ic.takeEssentia(currentSuction, 1, dir.getOpposite());
                    if (taken > 0) {
                        acceptEssentia(currentSuction, taken);
                        return;
                    }
                }
            }
        }
    }

    private int acceptEssentia(Holder<IAspect> aspect, int amount) {
        if (currentRecipe == null) {
            return 0;
        }
        int needed = currentRecipe.aspects().amountOf(aspect) - essentia.amountOf(aspect);
        if (needed <= 0) {
            return 0;
        }
        int added = Math.min(needed, amount);
        essentia = essentia.add(aspect, added);
        setChanged();
        syncToClient();
        return added;
    }

    void syncToClient() {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, 3);
        }
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != facing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != facing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {
        this.currentSuction = aspect;
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        return currentSuction;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return currentSuction != null ? SUCTION : 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return canInputFrom(face) ? acceptEssentia(aspect, amount) : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        essentia = input.read("Essentia", AspectList.CODEC).orElse(AspectList.EMPTY);
        maxRecipes = input.getIntOr("MaxRecipes", BASE_RECIPES);
        queue.clear();
        input.read("Queue", LegacyIds.IDENTIFIER_CODEC.listOf()).ifPresent(queue::addAll);
        input.child("Catalyst").ifPresent(catalyst::deserialize);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!essentia.isEmpty()) {
            output.store("Essentia", AspectList.CODEC, essentia);
        }
        output.putInt("MaxRecipes", maxRecipes);
        output.store("Queue", Identifier.CODEC.listOf(), List.copyOf(queue));
        catalyst.serialize(output.child("Catalyst"));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput out = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(out);
            nbt.merge(out.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
